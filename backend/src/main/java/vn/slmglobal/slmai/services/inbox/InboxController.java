package vn.slmglobal.slmai.services.inbox;

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
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.common.web.ResponseData;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;

import java.util.List;
import java.util.UUID;

/** Trang chat với khách (inbox admin): nhân viên và quản trị hệ thống xem mọi hội thoại. */
@Tag(name = "Quản trị - Chat với khách", description = "Hội thoại theo kênh, chuyển AI / nhân viên, xử lý nháp AI")
@RestController
@RequestMapping("/api/v1/admin/inbox")
@RequiredArgsConstructor
public class InboxController {

    private static final int MAX_PAGE_SIZE = 100;

    private final InboxService inbox;

    @Operation(summary = "Danh sách hội thoại đang mở, mới nhắn trước; view = NEEDS_STAFF / STAFF / AI / DRAFT")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.STAFF})
    @GetMapping("/conversations")
    public ResponseEntity<ResponseData<PageResponse<InboxRepository.Row>>> list(
            @RequestParam(required = false) ChannelCode channel,
            @RequestParam(required = false) InboxRepository.View view,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(inbox.list(new InboxRepository.Filter(channel == null ? null : channel.name(), view,
                        keyword), Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE)),
                "INBOX_LIST_SUCCESS");
    }

    @Operation(summary = "Số hội thoại đang mở theo kênh: tổng, cần nhân viên, có nháp, nhân viên phụ trách")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.STAFF})
    @GetMapping("/stats")
    public ResponseEntity<ResponseData<List<InboxRepository.ChannelStats>>> stats() {
        return ApiResponses.ok(inbox.stats(), "INBOX_STATS_SUCCESS");
    }

    @Operation(summary = "Một hội thoại: tin nhắn (kèm nháp và lý do), thông tin khách, hồ sơ tư vấn, lead, cửa sổ gửi")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.STAFF})
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ResponseData<InboxService.Detail>> detail(@PathVariable UUID id) {
        return ApiResponses.ok(inbox.detail(id), "INBOX_CONVERSATION_SUCCESS");
    }

    @Operation(summary = "Chuyển cho sale: AI dừng, hội thoại chờ nhân viên, báo mọi nhân viên")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.STAFF})
    @PostMapping("/conversations/{id}/to-staff")
    public ResponseEntity<ResponseData<InboxService.Detail>> toStaff(
            @PathVariable UUID id, @Valid @RequestBody(required = false) InboxService.ToStaffRequest request) {
        return ApiResponses.ok(inbox.toStaff(id, request, RequestContext.requireAuthInfo()), "INBOX_TO_STAFF_SUCCESS");
    }

    @Operation(summary = "Chuyển cho AI: AI phụ trách lại ngay và trả lời các tin khách còn chờ; nháp cũ bị bỏ")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.STAFF})
    @PostMapping("/conversations/{id}/to-ai")
    public ResponseEntity<ResponseData<InboxService.Detail>> toAi(@PathVariable UUID id) {
        return ApiResponses.ok(inbox.toAi(id, RequestContext.requireAuthInfo()), "INBOX_TO_AI_SUCCESS");
    }

    @Operation(summary = "Gửi nháp AI (hoặc gửi lại tin lỗi), text khác nháp thì lưu bản sửa")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.STAFF})
    @PostMapping("/messages/{id}/send")
    public ResponseEntity<ResponseData<InboxService.Detail>> send(
            @PathVariable UUID id, @Valid @RequestBody(required = false) InboxService.SendDraftRequest request) {
        return ApiResponses.ok(inbox.sendDraft(id, request, RequestContext.requireAuthInfo()), "INBOX_SEND_SUCCESS");
    }

    @Operation(summary = "Bỏ nháp AI / tin gửi lỗi")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.STAFF})
    @PostMapping("/messages/{id}/discard")
    public ResponseEntity<ResponseData<InboxService.Detail>> discard(@PathVariable UUID id) {
        return ApiResponses.ok(inbox.discardDraft(id, RequestContext.requireAuthInfo()), "INBOX_DISCARD_SUCCESS");
    }
}
