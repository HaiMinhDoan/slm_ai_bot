package vn.slmglobal.slmai.services.notification;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInterceptor;
import vn.slmglobal.slmai.common.security.JwtService;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.common.web.ResponseData;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Thông báo cho nhân viên trên trang admin. Vai trò STAFF (SYSTEM_ADMIN luôn qua).
 * Stream SSE xác thực bằng header Authorization như mọi API khác (frontend đọc stream bằng fetch).
 */
@Tag(name = "Thông báo nhân viên", description = "Chuông thông báo, đánh dấu đã đọc, stream thời gian thực")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationService notificationService;
    private final NotificationSseHub sseHub;

    public record NotificationResponse(long id, NotificationType type, NotificationSeverity severity, String title,
                                       String body, JsonNode data, UUID conversationId, UUID customerId,
                                       OffsetDateTime createdAt, boolean read) {

        static NotificationResponse of(NotificationRepository.Row r) {
            return new NotificationResponse(r.id(), r.type(), r.severity(), r.title(), r.body(), r.data(),
                    r.conversationId(), r.customerId(), r.createdAt(), r.readAt() != null);
        }
    }

    @Operation(summary = "Danh sách thông báo (mới trước)")
    @RequireAuth(roles = RoleCode.STAFF)
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<NotificationResponse>>> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID uid = RequestContext.currentUserId();
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        long total = notificationService.count(uid, unreadOnly, type);
        var items = notificationService.list(uid, unreadOnly, type, p, s).stream()
                .map(NotificationResponse::of).toList();
        return ApiResponses.ok(new PageResponse<>(items, total, p, s, (int) ((total + s - 1) / s)),
                "NOTIFICATION_LIST_SUCCESS");
    }

    @Operation(summary = "Số thông báo chưa đọc")
    @RequireAuth(roles = RoleCode.STAFF)
    @GetMapping("/unread-count")
    public ResponseEntity<ResponseData<Map<String, Long>>> unreadCount() {
        long n = notificationService.count(RequestContext.currentUserId(), true, null);
        return ApiResponses.ok(Map.of("count", n), "NOTIFICATION_UNREAD_COUNT_SUCCESS");
    }

    @Operation(summary = "Chi tiết một thông báo (kèm nháp AI, lý do, số điện thoại)")
    @RequireAuth(roles = RoleCode.STAFF)
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<NotificationResponse>> get(@PathVariable long id) {
        return notificationService.get(RequestContext.currentUserId(), id)
                .map(r -> ApiResponses.ok(NotificationResponse.of(r), "NOTIFICATION_DETAIL_SUCCESS"))
                .orElseThrow(() -> CommonException.notFound("Không tìm thấy thông báo"));
    }

    @Operation(summary = "Đánh dấu đã đọc")
    @RequireAuth(roles = RoleCode.STAFF)
    @PostMapping("/{id}/read")
    public ResponseEntity<ResponseData<Boolean>> markRead(@PathVariable long id) {
        if (!notificationService.markRead(RequestContext.currentUserId(), id)) {
            throw CommonException.notFound("Không tìm thấy thông báo");
        }
        return ApiResponses.ok(true, "NOTIFICATION_READ_SUCCESS");
    }

    @Operation(summary = "Đọc hết tới thông báo maxId (thông báo đến sau vẫn chưa đọc)")
    @RequireAuth(roles = RoleCode.STAFF)
    @PostMapping("/read-all")
    public ResponseEntity<ResponseData<Map<String, Integer>>> markAllRead(@RequestParam long maxId) {
        int n = notificationService.markAllRead(RequestContext.currentUserId(), maxId);
        return ApiResponses.ok(Map.of("marked", n), "NOTIFICATION_READ_ALL_SUCCESS");
    }

    @Operation(summary = "Stream thông báo thời gian thực (Server-Sent Events)")
    @RequireAuth(roles = RoleCode.STAFF)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpServletRequest request, HttpServletResponse response) {
        Object token = request.getAttribute(AuthInterceptor.VERIFIED_TOKEN_ATTRIBUTE);
        if (!(token instanceof JwtService.VerifiedToken verified)) {
            throw CommonException.unauthorized("Chưa đăng nhập");
        }
        // nginx (CMS và nginx-proxy-manager) không gom buffer để sự kiện tới ngay
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache, no-transform");
        long unread = notificationService.count(verified.authInfo().id(), true, null);
        return sseHub.open(verified, unread);
    }
}
