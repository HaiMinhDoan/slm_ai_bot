package vn.slmglobal.slmai.services.sizing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.ResponseData;

import java.util.ArrayList;
import java.util.List;

/**
 * Bảng PV out EVN theo năm (mục 60.3): xem, nạp bảng năm mới (chạy thử để so với năm trước rồi mới lưu).
 */
@Tag(name = "Quản trị - PV out EVN", description = "Hệ số PV out theo tỉnh, nạp lại mỗi năm khi EVN công bố")
@RestController
@RequestMapping("/api/v1/admin/pvout")
@RequiredArgsConstructor
public class PvoutAdminController {

    private final PvoutService pvoutService;
    private final ProvinceDirectory provinces;

    public record ProvinceRow(String code, String name, String region, List<Double> monthly, double annual) {
    }

    public record TableResponse(int year, String source, List<ProvinceRow> rows) {
    }

    @Operation(summary = "Các năm đã nạp")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @GetMapping
    public ResponseEntity<ResponseData<List<PvoutRepository.TableInfo>>> list() {
        return ApiResponses.ok(pvoutService.list(), "PVOUT_LIST_SUCCESS");
    }

    @Operation(summary = "Bảng của một năm, theo thứ tự tỉnh từ Bắc vào Nam")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @GetMapping("/{year}")
    public ResponseEntity<ResponseData<TableResponse>> table(@PathVariable int year) {
        PvoutRepository.Table t = pvoutService.load(year)
                .orElseThrow(() -> CommonException.notFound("Chưa có bảng PV out năm " + year));
        List<ProvinceRow> rows = new ArrayList<>();
        for (ProvinceDirectory.Province p : provinces.all()) {
            t.of(p.code()).ifPresent(m -> {
                List<Double> months = new ArrayList<>();
                for (double v : m) {
                    months.add(v);
                }
                rows.add(new ProvinceRow(p.code(), p.name(), provinces.regionName(p.region()), months,
                        PvoutRepository.Table.annual(m)));
            });
        }
        return ApiResponses.ok(new TableResponse(t.year(), t.source(), rows), "PVOUT_TABLE_SUCCESS");
    }

    @Operation(summary = "Nạp bảng của một năm (dryRun = true: chỉ kiểm tra và so với năm trước)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/import")
    public ResponseEntity<ResponseData<PvoutService.ImportResult>> importTable(
            @Valid @RequestBody PvoutService.ImportRequest request) {
        AuthInfo actor = RequestContext.requireAuthInfo();
        PvoutService.ImportResult result = pvoutService.importTable(request, actor.id(), actor.email());
        return ApiResponses.ok(result, result.saved() ? "PVOUT_IMPORT_SUCCESS" : "PVOUT_IMPORT_CHECKED");
    }
}
