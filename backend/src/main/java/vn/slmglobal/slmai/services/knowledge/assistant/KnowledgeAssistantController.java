package vn.slmglobal.slmai.services.knowledge.assistant;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.ResponseData;

import java.util.List;
import java.util.UUID;

/** AI hỗ trợ soạn kiến thức (mục 5.10 B): đề xuất → xem diff → duyệt thành bản nháp. */
@Tag(name = "Quản trị - AI hỗ trợ soạn kiến thức", description = "Phiên chat, đề xuất sửa tài liệu, duyệt / từ chối")
@RestController
@RequestMapping("/api/v1/admin/knowledge/assistant")
@RequiredArgsConstructor
public class KnowledgeAssistantController {

    private final KnowledgeAssistantService assistant;

    @Operation(summary = "Các phiên gần đây của tôi")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/sessions")
    public ResponseEntity<ResponseData<List<KnowledgeAssistantService.SessionRow>>> sessions() {
        return ApiResponses.ok(assistant.list(RequestContext.requireAuthInfo()), "ASSISTANT_SESSIONS_SUCCESS");
    }

    @Operation(summary = "Mở phiên mới (có thể gắn sẵn một tài liệu đích)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/sessions")
    public ResponseEntity<ResponseData<KnowledgeAssistantService.SessionView>> start(
            @Valid @RequestBody KnowledgeAssistantService.StartRequest request) {
        return ApiResponses.ok(assistant.start(request, RequestContext.requireAuthInfo()), "ASSISTANT_SESSION_STARTED");
    }

    @Operation(summary = "Xem phiên: tin nhắn, đề xuất; pending = true khi AI đang trả lời (hỏi lại sau vài giây)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ResponseData<KnowledgeAssistantService.SessionView>> session(@PathVariable UUID id) {
        return ApiResponses.ok(assistant.get(id, RequestContext.requireAuthInfo()), "ASSISTANT_SESSION_SUCCESS");
    }

    @Operation(summary = "Gửi yêu cầu; AI trả lời chạy nền")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ResponseData<KnowledgeAssistantService.SessionView>> send(
            @PathVariable UUID id, @Valid @RequestBody KnowledgeAssistantService.SendRequest request) {
        return ApiResponses.ok(assistant.send(id, request, RequestContext.requireAuthInfo()), "ASSISTANT_MESSAGE_SENT");
    }

    @Operation(summary = "Duyệt đề xuất thành bản nháp (force = true để ghi đè bản nháp khác đang có)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/proposals/{id}/accept")
    public ResponseEntity<ResponseData<KnowledgeAssistantService.ProposalView>> accept(
            @PathVariable Long id, @RequestParam(defaultValue = "false") boolean force) {
        return ApiResponses.ok(assistant.accept(id, force, RequestContext.requireAuthInfo()),
                "ASSISTANT_PROPOSAL_ACCEPTED");
    }

    @Operation(summary = "Từ chối đề xuất")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/proposals/{id}/reject")
    public ResponseEntity<ResponseData<KnowledgeAssistantService.ProposalView>> reject(@PathVariable Long id) {
        return ApiResponses.ok(assistant.reject(id, RequestContext.requireAuthInfo()), "ASSISTANT_PROPOSAL_REJECTED");
    }
}
