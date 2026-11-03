package vn.slmglobal.slmai.services.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.ResponseData;
import vn.slmglobal.slmai.services.ai.AiReplySchema;

import java.util.LinkedHashMap;
import java.util.Map;

/** Cài đặt vận hành AI và công tắc dừng (mục 33.2), có hiệu lực ngay. */
@Tag(name = "Quản trị - Cài đặt AI", description = "Công tắc dừng, loại câu hỏi tự gửi, giờ làm việc, model")
@RestController
@RequestMapping("/api/v1/admin/ai-settings")
@RequiredArgsConstructor
public class AiSettingsController {

    private final AiSettingsService settings;

    /** @param on bật / tắt; reason bắt buộc khi bật */
    public record KillSwitchRequest(boolean on, @Size(max = 500) String reason) {
    }

    @Operation(summary = "Cài đặt hiện hành, giá trị mặc định (application.yml / .env) và các mục đang ghi đè")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping
    public ResponseEntity<ResponseData<Map<String, Object>>> get() {
        AiSettingsService.View v = settings.view();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("values", v.values());
        out.put("defaults", v.defaults());
        out.put("overridden", v.overridden());
        out.put("intents", AiReplySchema.INTENTS);
        out.put("models", AiSettingsService.MODELS);
        out.put("efforts", AiSettingsService.EFFORTS);
        return ApiResponses.ok(out, "AI_SETTINGS_SUCCESS");
    }

    @Operation(summary = "Lưu cài đặt (trừ công tắc dừng); giá trị bằng mặc định thì bỏ ghi đè")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping
    public ResponseEntity<ResponseData<AiSettingsService.View>> update(@RequestBody AiSettings request) {
        return ApiResponses.ok(settings.update(request, RequestContext.requireAuthInfo()), "AI_SETTINGS_SAVED");
    }

    @Operation(summary = "Bật / tắt công tắc dừng: bật thì mọi tin AI chỉ lưu nháp chờ duyệt")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/kill-switch")
    public ResponseEntity<ResponseData<AiSettings.KillSwitch>> killSwitch(@RequestBody KillSwitchRequest request) {
        return ApiResponses.ok(settings.setKillSwitch(request.on(), request.reason(),
                RequestContext.requireAuthInfo()), request.on() ? "AI_KILL_SWITCH_ON" : "AI_KILL_SWITCH_OFF");
    }
}
