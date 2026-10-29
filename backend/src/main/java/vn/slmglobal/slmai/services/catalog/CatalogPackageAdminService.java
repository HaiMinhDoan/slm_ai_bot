package vn.slmglobal.slmai.services.catalog;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.PackageOverride;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.PackageSource;

import java.util.List;
import java.util.Objects;

/**
 * Sửa, tắt, xoá mềm gói trên admin (V8). Chỉ ghi dm_tron_goi_dieu_chinh: dm_tron_goi và slm_v2 không đổi,
 * đồng bộ cũng không đè mất. Trường nhập trùng số liệu slm_v2 lưu null, để sau này slm_v2 sửa thì bot nhận ngay.
 * Mọi thao tác ghi nhật ký thao tác.
 */
@Service
@RequiredArgsConstructor
public class CatalogPackageAdminService {

    private static final String TARGET = "catalog_package";

    private final CatalogRepository repository;
    private final AuditLogService audit;

    /** Dữ liệu mở form sửa: số liệu bot đang dùng, số liệu slm_v2, điều chỉnh đang lưu */
    public record PackageEdit(CatalogPackage current, PackageSource source, PackageOverride override) {
    }

    /**
     * Giá trị mong muốn của từng trường (form gửi cả trường không đổi). null hoặc trùng slm_v2 = dùng slm_v2.
     *
     * @param enabled false = tắt gói trong bot (không gợi ý, không báo giá)
     */
    public record UpdateRequest(
            @Size(max = 400, message = "Tên gói tối đa 400 ký tự") String name,
            @Pattern(regexp = "ON_GRID|HYBRID", message = "Loại hệ chỉ là ON_GRID hoặc HYBRID") String systemType,
            Integer phases,
            @Positive(message = "Công suất phải lớn hơn 0") Double kwp,
            @Positive(message = "Số tấm pin phải lớn hơn 0") Integer panelCount,
            @Positive(message = "Công suất tấm pin phải lớn hơn 0") Double panelWp,
            @PositiveOrZero(message = "Dung lượng lưu trữ không được âm") Double storageKwh,
            @Positive(message = "Giá phải lớn hơn 0") Double totalPrice,
            Boolean bestSeller,
            boolean enabled,
            @Size(max = 2000, message = "Ghi chú tối đa 2000 ký tự") String note) {
    }

    public record SetActiveRequest(@NotEmpty(message = "Chưa chọn gói nào") List<Integer> ids, boolean active) {
    }

    @Transactional(readOnly = true)
    public PackageEdit edit(int id) {
        CatalogPackage current = repository.findPackage(id).orElseThrow(() -> notFound(id));
        PackageSource source = repository.findSource(id).orElseThrow(() -> notFound(id));
        return new PackageEdit(current, source, repository.findOverride(id).orElse(null));
    }

    @Transactional
    public CatalogPackage update(AuthInfo actor, int id, UpdateRequest r) {
        if (r.phases() != null && r.phases() != 1 && r.phases() != 3) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Số pha chỉ là 1 hoặc 3");
        }
        PackageSource src = repository.findSource(id).orElseThrow(() -> notFound(id));
        PackageOverride before = repository.findOverride(id).orElse(null);
        PackageOverride after = new PackageOverride(
                differs(blankToNull(r.name()), src.name()),
                differs(r.systemType(), src.systemType()),
                differs(r.phases(), src.phases()),
                differs(r.kwp(), src.kwp(), 3),
                differs(r.panelCount(), src.panelCount()),
                differs(r.panelWp(), src.panelWp(), 1),
                differs(r.storageKwh(), src.storageKwh(), 2),
                differs(r.totalPrice(), src.totalPrice(), 0),
                differs(r.bestSeller(), src.bestSeller()),
                !r.enabled(),
                blankToNull(r.note()),
                before == null ? null : before.deletedAt(),
                before == null ? null : before.deletedBy(),
                actor.email(), null);
        repository.saveOverride(id, after, actor.email());
        audit.record(actor.id(), "catalog.package.update", TARGET, id, before, after);
        return repository.findPackage(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public int setActive(AuthInfo actor, SetActiveRequest request) {
        List<Integer> ids = request.ids().stream().filter(Objects::nonNull).distinct().toList();
        int affected = repository.setDisabled(ids, !request.active(), actor.email());
        audit.record(actor.id(), request.active() ? "catalog.package.enable" : "catalog.package.disable", TARGET,
                ids, null, java.util.Map.of("ids", ids, "affected", affected));
        return affected;
    }

    @Transactional
    public void delete(AuthInfo actor, int id) {
        repository.findSource(id).orElseThrow(() -> notFound(id));
        if (repository.softDelete(id, actor.email())) {
            audit.record(actor.id(), "catalog.package.delete", TARGET, id, null, null);
        }
    }

    @Transactional
    public CatalogPackage restore(AuthInfo actor, int id) {
        repository.findSource(id).orElseThrow(() -> notFound(id));
        if (repository.restore(id, actor.email())) {
            audit.record(actor.id(), "catalog.package.restore", TARGET, id, null, null);
        }
        return repository.findPackage(id).orElseThrow(() -> notFound(id));
    }

    private static <T> T differs(T value, T source) {
        return value == null || value.equals(source) ? null : value;
    }

    /** So số theo đúng số chữ số thập phân của cột, để 8.75 và 8.750 coi là bằng nhau */
    private static Double differs(Double value, Double source, int scale) {
        if (value == null) {
            return null;
        }
        if (source != null && Math.abs(value - source) < 0.5 * Math.pow(10, -scale)) {
            return null;
        }
        return value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static CommonException notFound(int id) {
        return CommonException.notFound("Không có gói mã " + id);
    }
}
