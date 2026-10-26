package vn.slmglobal.slmai.services.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.JwtService;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.security.TokenBlacklist;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Đẩy thông báo tới các tab admin đang mở qua Server-Sent Events.
 *
 * <ul>
 *   <li>Sự kiện được đẩy sau khi transaction ghi thông báo commit, trên executor riêng để kết nối chậm không làm
 *       chậm luồng AI.</li>
 *   <li>Ping theo chu kỳ trên scheduler riêng (scheduler mặc định của Spring chỉ có một luồng, dùng chung với các
 *       worker); mỗi lần ping kiểm tra token đã thu hồi (đăng xuất) hoặc hết hạn thì gửi {@code auth-expired} rồi
 *       đóng.</li>
 *   <li>Chạy trong một instance: chạy nhiều instance thì cần kênh phát chung (Redis / PostgreSQL NOTIFY).</li>
 * </ul>
 */
@Slf4j
@Component
public class NotificationSseHub {

    private final NotificationProperties properties;
    private final TokenBlacklist tokenBlacklist;
    private final ObjectMapper objectMapper;
    private final Map<UUID, List<Connection>> connections = new ConcurrentHashMap<>();
    private final ExecutorService sender = Executors.newSingleThreadExecutor(daemon("notify-sse"));
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(
            daemon("notify-sse-ping"));

    private record Connection(SseEmitter emitter, UUID userId, AuthInfo authInfo, String jti, Instant expiresAt) {
    }

    public NotificationSseHub(NotificationProperties properties, TokenBlacklist tokenBlacklist,
                              ObjectMapper objectMapper) {
        this.properties = properties;
        this.tokenBlacklist = tokenBlacklist;
        this.objectMapper = objectMapper;
        long every = Math.max(5, properties.heartbeat().toSeconds());
        heartbeat.scheduleAtFixedRate(this::ping, every, every, TimeUnit.SECONDS);
    }

    /** Mở kết nối cho người dùng đã xác thực; sự kiện đầu tiên là {@code ready} kèm số chưa đọc. */
    public SseEmitter open(JwtService.VerifiedToken token, long unreadCount) {
        AuthInfo auth = token.authInfo();
        long untilExpiry = Duration.between(Instant.now(), token.expiresAt()).toMillis();
        long timeout = Math.max(1_000, Math.min(properties.streamTimeout().toMillis(), untilExpiry));
        SseEmitter emitter = new SseEmitter(timeout);
        Connection conn = new Connection(emitter, auth.id(), auth, token.jti(), token.expiresAt());

        List<Connection> mine = connections.computeIfAbsent(auth.id(), k -> new CopyOnWriteArrayList<>());
        mine.add(conn);
        while (mine.size() > Math.max(1, properties.maxConnectionsPerUser())) {
            close(mine.get(0));
        }
        emitter.onCompletion(() -> remove(conn));
        emitter.onTimeout(() -> remove(conn));
        emitter.onError(e -> remove(conn));

        send(conn, SseEmitter.event().name("ready").data(Map.of("unreadCount", unreadCount),
                MediaType.APPLICATION_JSON));
        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(NotificationCreated event) {
        sender.execute(() -> broadcast(event));
    }

    void broadcast(NotificationCreated e) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", e.id());
        payload.put("type", e.type().name());
        payload.put("severity", e.severity().name());
        payload.put("title", e.title());
        payload.put("body", e.body());
        payload.put("conversationId", e.conversationId());
        payload.put("createdAt", e.createdAt());
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (IOException ex) {
            log.warn("Không chuyển được thông báo #{} sang JSON: {}", e.id(), ex.getMessage());
            return;
        }
        connections.values().forEach(list -> list.forEach(conn -> {
            if (canReceive(conn, e)) {
                send(conn, SseEmitter.event().id(String.valueOf(e.id())).name("notification")
                        .data(json, MediaType.APPLICATION_JSON));
            }
        }));
    }

    /** Gửi cho mọi người: chỉ người có vai trò STAFF hoặc SYSTEM_ADMIN; gửi riêng: đúng người nhận. */
    private static boolean canReceive(Connection conn, NotificationCreated e) {
        if (e.recipientId() != null) {
            return e.recipientId().equals(conn.userId());
        }
        return conn.authInfo().hasAnyRole(RoleCode.STAFF, RoleCode.SYSTEM_ADMIN);
    }

    private void ping() {
        Instant now = Instant.now();
        connections.values().forEach(list -> list.forEach(conn -> {
            if (tokenBlacklist.isRevoked(conn.jti()) || !now.isBefore(conn.expiresAt())) {
                send(conn, SseEmitter.event().name("auth-expired").data("{}", MediaType.APPLICATION_JSON));
                close(conn);
            } else {
                send(conn, SseEmitter.event().comment("ping"));
            }
        }));
    }

    private void send(Connection conn, SseEmitter.SseEventBuilder event) {
        try {
            conn.emitter().send(event);
        } catch (IOException | IllegalStateException e) {
            // Trình duyệt đã đóng tab / mất mạng
            remove(conn);
        }
    }

    private void close(Connection conn) {
        remove(conn);
        try {
            conn.emitter().complete();
        } catch (RuntimeException ignored) {
            // Kết nối đã đóng
        }
    }

    private void remove(Connection conn) {
        List<Connection> list = connections.get(conn.userId());
        if (list != null) {
            list.remove(conn);
            if (list.isEmpty()) {
                connections.remove(conn.userId(), list);
            }
        }
    }

    /** Số kết nối đang mở (theo dõi, test) */
    public int openConnections() {
        return connections.values().stream().mapToInt(List::size).sum();
    }

    @PreDestroy
    public void shutdown() {
        heartbeat.shutdownNow();
        sender.shutdownNow();
        connections.values().forEach(list -> list.forEach(this::close));
    }

    private static ThreadFactory daemon(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }
}
