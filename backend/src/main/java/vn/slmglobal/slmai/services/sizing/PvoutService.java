package vn.slmglobal.slmai.services.sizing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.auth.AuditLogService;

import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Bảng PV out lý thuyết EVN theo năm (mục 60.3). Mỗi năm EVN công bố bảng mới (thường tháng 1); quản trị nạp qua
 * trang admin, PvoutReminderJob nhắc khi đến hạn. Năm chưa có bảng thì dùng bảng gần nhất trước đó.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PvoutService {

    static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    /** Giới hạn hợp lý của một tháng (kWh/kWp): bảng 2026 nằm trong 31–161 */
    static final double MIN_MONTHLY = 10;
    static final double MAX_MONTHLY = 300;

    private final PvoutRepository repository;
    private final ProvinceDirectory provinces;
    private final AuditLogService auditLog;

    private volatile PvoutRepository.Table cached;
    private volatile int cachedFor;

    /** Một dòng khi nạp: tỉnh (tên mới hoặc mã) và 12 tháng */
    public record ImportRow(@NotBlank String province, @NotNull List<Double> monthly) {
    }

    public record ImportRequest(@NotNull Integer year, @NotBlank String source, String note,
                                @NotEmpty List<ImportRow> rows, boolean dryRun) {
    }

    public record PreviewRow(String code, String name, List<Double> monthly, double annual, Double previousAnnual,
                             Double changePercent) {
    }

    /**
     * @param comparedYear bảng dùng để so (năm gần nhất trước năm nạp); null nếu chưa có
     */
    public record ImportResult(int year, boolean saved, List<String> errors, Integer comparedYear,
                               List<PreviewRow> rows) {
    }

    /** Bảng dùng cho năm hiện tại (giờ Việt Nam) */
    public Optional<PvoutRepository.Table> current() {
        int year = Year.now(ZONE).getValue();
        PvoutRepository.Table t = cached;
        if (t != null && cachedFor == year) {
            return Optional.of(t);
        }
        Optional<PvoutRepository.Table> loaded = repository.latestYearUpTo(year).flatMap(repository::load);
        loaded.ifPresent(table -> {
            cached = table;
            cachedFor = year;
        });
        return loaded;
    }

    public List<PvoutRepository.TableInfo> list() {
        return repository.list();
    }

    public Optional<PvoutRepository.Table> load(int year) {
        return repository.load(year);
    }

    /** Kiểm tra và (nếu không phải chạy thử, không có lỗi) ghi bảng của một năm, thay bảng cũ cùng năm. */
    @Transactional
    public ImportResult importTable(ImportRequest req, UUID actorId, String actorEmail) {
        List<String> errors = new ArrayList<>();
        int year = req.year();
        if (year < 2020 || year > 2100) {
            errors.add("Năm không hợp lệ: " + year);
        }
        Map<String, double[]> monthly = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < req.rows().size(); i++) {
            ImportRow row = req.rows().get(i);
            String where = "Dòng " + (i + 1) + " (" + row.province() + ")";
            ProvinceDirectory.Province p = provinces.byCode(row.province().strip().toUpperCase())
                    .or(() -> provinces.resolve(row.province()).filter(r -> r.formerName() == null)
                            .map(ProvinceDirectory.Resolution::province))
                    .orElse(null);
            if (p == null) {
                errors.add(where + ": không nhận ra tỉnh (dùng tên 34 tỉnh mới)");
                continue;
            }
            if (!seen.add(p.code())) {
                errors.add(where + ": " + p.name() + " bị lặp");
                continue;
            }
            List<Double> values = row.monthly();
            if (values == null || values.size() != 12 || values.stream().anyMatch(v -> v == null)) {
                errors.add(where + ": cần đủ 12 tháng");
                continue;
            }
            double[] months = values.stream().mapToDouble(Double::doubleValue).toArray();
            for (int m = 0; m < 12; m++) {
                if (months[m] < MIN_MONTHLY || months[m] > MAX_MONTHLY) {
                    errors.add(where + ": tháng " + (m + 1) + " = " + months[m] + " nằm ngoài " + (int) MIN_MONTHLY
                            + "–" + (int) MAX_MONTHLY);
                }
            }
            monthly.put(p.code(), months);
        }
        for (ProvinceDirectory.Province p : provinces.all()) {
            if (!seen.contains(p.code())) {
                errors.add("Thiếu " + p.name());
            }
        }

        Integer comparedYear = repository.latestYearUpTo(year - 1).filter(y -> y < year).orElse(null);
        PvoutRepository.Table previous = comparedYear == null ? null : repository.load(comparedYear).orElse(null);
        List<PreviewRow> preview = new ArrayList<>();
        for (Map.Entry<String, double[]> e : monthly.entrySet()) {
            double annual = PvoutRepository.Table.annual(e.getValue());
            Double prev = previous == null ? null
                    : previous.of(e.getKey()).map(PvoutRepository.Table::annual).orElse(null);
            preview.add(new PreviewRow(e.getKey(), provinces.byCode(e.getKey()).map(ProvinceDirectory.Province::name)
                    .orElse(e.getKey()), toList(e.getValue()), annual, prev,
                    prev == null || prev == 0 ? null : Math.round((annual - prev) / prev * 1000) / 10.0));
        }

        boolean save = errors.isEmpty() && !req.dryRun();
        if (save) {
            boolean replaced = repository.exists(year);
            repository.replace(year, req.source().strip(), blankToNull(req.note()), actorEmail, monthly);
            auditLog.record(actorId, "pvout.import", "PVOUT_TABLE", year,
                    replaced ? Map.of("replaced", true) : null,
                    Map.of("source", req.source().strip(), "provinces", monthly.size()));
            cached = null;
            log.info("Nạp bảng PV out năm {} ({} tỉnh) bởi {}", year, monthly.size(), actorEmail);
        }
        return new ImportResult(year, save, List.copyOf(errors), comparedYear, List.copyOf(preview));
    }

    private static List<Double> toList(double[] values) {
        List<Double> out = new ArrayList<>(values.length);
        for (double v : values) {
            out.add(v);
        }
        return out;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
