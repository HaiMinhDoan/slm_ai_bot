package vn.slmglobal.slmai.services.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.services.ai.AiProperties;
import vn.slmglobal.slmai.services.ai.AiReplySchema;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.conversation.ConversationProperties;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cài đặt vận hành AI (mục 33.2, 35): giá trị trong application.yml / .env là mặc định, bảng {@code cai_dat_ai}
 * ghi đè. Có hiệu lực ngay sau khi lưu (nạp lại sau commit), dự phòng tự nạp lại mỗi 30 giây.
 *
 * <p>Mọi chỗ đọc cấu hình động gọi {@link #ai()} / {@link #conversation()} mỗi lần dùng, không giữ bản sao.
 */
@Slf4j
@Service
public class AiSettingsService {

    public static final String KILL_SWITCH = "kill_switch";
    public static final String DEFAULT_ASSISTANT_MODEL = "claude-opus-5-5";
    public static final List<String> MODELS = List.of("claude-sonnet-5-5", "claude-opus-5-5", "claude-haiku-5-5");
    public static final List<String> EFFORTS = List.of("low", "medium", "high", "xhigh", "max");
    private static final String TARGET = "ai_settings";

    /** Sự kiện sau khi lưu cài đặt: nạp lại sau commit */
    public record Changed() {
    }

    /** Cài đặt hiện hành, mặc định và các khoá đang bị ghi đè */
    public record View(AiSettings values, AiSettings defaults, List<String> overridden) {
    }

    private record Effective(AiSettings settings, AiProperties ai, ConversationProperties conversation,
                             List<String> overridden) {
    }

    private final AiProperties baseAi;
    private final ConversationProperties baseConversation;
    private final AiSettingRepository repository;
    private final AuditLogService audit;
    private final NotificationService notifications;
    private final ApplicationEventPublisher events;
    /** Lưu cài đặt với khoá snake_case (khớp tên dòng trong cai_dat_ai) */
    private final ObjectMapper snake;
    private final AiSettings defaults;
    private final AtomicReference<Effective> current = new AtomicReference<>();

    @Autowired
    public AiSettingsService(AiProperties baseAi, ConversationProperties baseConversation,
                             AiSettingRepository repository, ObjectMapper objectMapper, AuditLogService audit,
                             NotificationService notifications, ApplicationEventPublisher events) {
        this.baseAi = baseAi;
        this.baseConversation = baseConversation;
        this.repository = repository;
        this.audit = audit;
        this.notifications = notifications;
        this.events = events;
        this.snake = objectMapper.copy().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.defaults = normalize(defaultsOf(baseAi, baseConversation));
    }

    /** Không đọc CSDL: chỉ dùng giá trị truyền vào (test). */
    public static AiSettingsService fixed(AiProperties ai, ConversationProperties conversation) {
        return new AiSettingsService(ai, conversation, null, new ObjectMapper(), null, null, null);
    }

    public AiProperties ai() {
        return effective().ai();
    }

    public ConversationProperties conversation() {
        return effective().conversation();
    }

    public AiSettings settings() {
        return effective().settings();
    }

    public View view() {
        Effective e = effective();
        return new View(e.settings(), defaults, e.overridden());
    }

    @Transactional
    public View update(AiSettings incoming, AuthInfo actor) {
        List<String> errors = validate(incoming);
        if (!errors.isEmpty()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }
        incoming = normalize(incoming);
        AiSettings before = settings();
        ObjectNode in = snake.valueToTree(incoming);
        ObjectNode def = snake.valueToTree(defaults);
        for (Iterator<String> it = in.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            if (KILL_SWITCH.equals(key)) {
                continue; // bật / tắt qua setKillSwitch: bắt buộc lý do, có thông báo
            }
            if (Objects.equals(in.get(key), def.get(key))) {
                repository.delete(key);
            } else {
                repository.upsert(key, in.get(key), actor.email());
            }
        }
        audit.record(actor.id(), "ai.settings.update", TARGET, "all", withoutKillSwitch(before),
                withoutKillSwitch(incoming));
        events.publishEvent(new Changed());
        return new View(merge(incoming, before.killSwitch()), defaults, List.of());
    }

    /** Bật / tắt công tắc dừng (mục 33.2): ghi audit và báo quản trị hệ thống. */
    @Transactional
    public AiSettings.KillSwitch setKillSwitch(boolean on, String reason, AuthInfo actor) {
        if (on && (reason == null || reason.isBlank())) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Nhập lý do bật công tắc dừng");
        }
        AiSettings.KillSwitch before = settings().killSwitch();
        AiSettings.KillSwitch after = new AiSettings.KillSwitch(on, reason == null ? null : reason.strip(),
                actor.email(), OffsetDateTime.now().toString());
        repository.upsert(KILL_SWITCH, snake.valueToTree(after), actor.email());
        audit.record(actor.id(), on ? "ai.kill_switch.on" : "ai.kill_switch.off", TARGET, KILL_SWITCH, before, after);
        notifications.notifyAdmins(NotificationDraft.builder()
                .type(NotificationType.AI_KILL_SWITCH)
                .severity(on ? NotificationSeverity.URGENT : NotificationSeverity.INFO)
                .title(on ? "Đã BẬT công tắc dừng AI: mọi tin AI chỉ lưu nháp" : "Đã tắt công tắc dừng AI")
                .body((on ? "Lý do: " + after.reason() + ". " : "") + "Người thao tác: " + actor.email())
                .data(Map.of("on", on, "reason", after.reason() == null ? "" : after.reason(),
                        "by", actor.email()))
                .dedupeKey("AI_KILL_SWITCH:" + after.updatedAt())
                .build());
        events.publishEvent(new Changed());
        return after;
    }

    @TransactionalEventListener
    public void onChanged(Changed event) {
        reload();
    }

    /** Dự phòng khi chạy nhiều instance hoặc sửa thẳng CSDL */
    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void poll() {
        if (repository != null) {
            reload();
        }
    }

    public void reload() {
        current.set(compute(repository == null ? Map.of() : repository.findAll()));
    }

    private Effective effective() {
        Effective e = current.get();
        if (e == null) {
            reload();
            e = current.get();
        }
        return e;
    }

    private Effective compute(Map<String, JsonNode> overrides) {
        AiSettings s = defaults;
        List<String> applied = new ArrayList<>();
        if (!overrides.isEmpty()) {
            ObjectNode node = snake.valueToTree(defaults);
            for (Map.Entry<String, JsonNode> o : overrides.entrySet()) {
                if (!node.has(o.getKey())) {
                    log.warn("cai_dat_ai: bỏ qua khoá lạ {}", o.getKey());
                    continue;
                }
                ObjectNode trial = node.deepCopy();
                trial.set(o.getKey(), o.getValue());
                try {
                    snake.treeToValue(trial, AiSettings.class);
                    node = trial;
                    applied.add(o.getKey());
                } catch (Exception ex) {
                    log.error("cai_dat_ai.{} không đọc được, dùng giá trị mặc định: {}", o.getKey(), ex.getMessage());
                }
            }
            try {
                s = snake.treeToValue(node, AiSettings.class);
            } catch (Exception ex) {
                log.error("cai_dat_ai không đọc được, dùng mặc định: {}", ex.getMessage());
                applied.clear();
            }
        }
        return new Effective(s, toAiProperties(s), toConversation(s), List.copyOf(applied));
    }

    private AiProperties toAiProperties(AiSettings s) {
        AiProperties.BusinessHours bh = baseAi.businessHours();
        AiProperties.Templates t = baseAi.templates();
        return new AiProperties(baseAi.apiKey(), s.model(), s.effort(), baseAi.maxTokens(), baseAi.timeout(),
                baseAi.serverSideFallback(), s.killSwitch() != null && s.killSwitch().on(), baseAi.workerEnabled(),
                baseAi.workerConcurrency(), baseAi.historyLimit(), s.maxAttempts(), seconds(s.retryBackoffSeconds()),
                baseAi.turnLease(), s.typingIndicator(), s.companyName(), s.personaName(), s.autoIntents(),
                s.handoffIntents(),
                new AiProperties.BusinessHours(bh == null ? null : bh.zone(), s.businessStart(), s.businessEnd(),
                        s.businessDays()),
                new AiProperties.Templates(blankToNull(s.outOfHoursAck()), blankToNull(s.handoffAck()),
                        hours(s.ackCooldownHours())),
                baseAi.maxToolRounds(), baseAi.baseUrl());
    }

    private ConversationProperties toConversation(AiSettings s) {
        return new ConversationProperties(seconds(s.debounceSeconds()), hours(s.autoHandbackHours()),
                baseConversation.messagingWindow(), baseConversation.workerEnabled(),
                baseConversation.workerBatchSize());
    }

    static AiSettings defaultsOf(AiProperties a, ConversationProperties c) {
        AiProperties.BusinessHours bh = a.businessHours();
        AiProperties.Templates t = a.templates();
        return new AiSettings(
                new AiSettings.KillSwitch(a.forceDraft(), null, null, null),
                a.autoIntents() == null ? List.of() : List.copyOf(a.autoIntents()),
                a.handoffIntents() == null ? List.of() : List.copyOf(a.handoffIntents()),
                bh == null ? "08:00" : bh.start(),
                bh == null ? "17:30" : bh.end(),
                bh == null || bh.days() == null ? List.of() : List.copyOf(bh.days()),
                t == null ? null : t.outOfHoursAck(),
                t == null ? null : t.handoffAck(),
                t == null || t.ackCooldown() == null ? 0 : t.ackCooldown().toMillis() / 3_600_000.0,
                a.personaName(),
                a.companyName(),
                a.typingIndicator(),
                c.debounce() == null ? 0 : c.debounce().toMillis() / 1000.0,
                c.autoHandback() == null ? 0 : c.autoHandback().toMillis() / 3_600_000.0,
                a.model(),
                a.effort(),
                DEFAULT_ASSISTANT_MODEL,
                a.maxAttempts(),
                a.retryBackoff() == null ? 20 : a.retryBackoff().toMillis() / 1000.0);
    }

    static List<String> validate(AiSettings s) {
        List<String> errors = new ArrayList<>();
        Set<String> auto = new HashSet<>(s.autoIntents() == null ? List.of() : s.autoIntents());
        Set<String> handoff = new HashSet<>(s.handoffIntents() == null ? List.of() : s.handoffIntents());
        for (String i : auto) {
            if (!AiReplySchema.INTENTS.contains(i)) {
                errors.add("Loại câu hỏi gửi tự động không hợp lệ: " + i);
            }
        }
        for (String i : handoff) {
            if (!AiReplySchema.INTENTS.contains(i)) {
                errors.add("Loại câu hỏi chuyển nhân viên không hợp lệ: " + i);
            }
            if (auto.contains(i)) {
                errors.add(i + " không thể vừa gửi tự động vừa chuyển nhân viên");
            }
        }
        try {
            if (!LocalTime.parse(s.businessStart()).isBefore(LocalTime.parse(s.businessEnd()))) {
                errors.add("Giờ bắt đầu làm việc phải trước giờ kết thúc");
            }
        } catch (DateTimeParseException | NullPointerException e) {
            errors.add("Giờ làm việc phải theo dạng HH:mm");
        }
        if (s.businessDays() == null || s.businessDays().isEmpty()) {
            errors.add("Chọn ít nhất một ngày làm việc");
        }
        checkLength(errors, "Câu báo nhận ngoài giờ", s.outOfHoursAck(), 500);
        checkLength(errors, "Câu báo nhận khi chuyển nhân viên", s.handoffAck(), 500);
        range(errors, "Không gửi lại câu báo nhận trong (giờ)", s.ackCooldownHours(), 0, 168);
        if (s.personaName() == null || s.personaName().isBlank() || s.personaName().length() > 50) {
            errors.add("Tên tư vấn viên bắt buộc, tối đa 50 ký tự");
        }
        if (s.companyName() == null || s.companyName().isBlank() || s.companyName().length() > 100) {
            errors.add("Tên công ty bắt buộc, tối đa 100 ký tự");
        }
        range(errors, "Thời gian gộp tin (giây)", s.debounceSeconds(), 0, 120);
        range(errors, "Tự trả lại AI sau (giờ)", s.autoHandbackHours(), 0, 720);
        if (!MODELS.contains(s.model())) {
            errors.add("Model trả lời khách phải là một trong " + MODELS);
        }
        if (!EFFORTS.contains(s.effort())) {
            errors.add("Mức suy nghĩ phải là một trong " + EFFORTS);
        }
        if (!MODELS.contains(s.assistantModel())) {
            errors.add("Model AI hỗ trợ soạn phải là một trong " + MODELS);
        }
        range(errors, "Số lần thử lại khi gọi AI lỗi", s.maxAttempts(), 1, 10);
        range(errors, "Chờ trước lần thử lại (giây)", s.retryBackoffSeconds(), 1, 600);
        return errors;
    }

    private static void checkLength(List<String> errors, String label, String value, int max) {
        if (value != null && value.length() > max) {
            errors.add(label + " tối đa " + max + " ký tự");
        }
    }

    private static void range(List<String> errors, String label, double v, double min, double max) {
        if (Double.isNaN(v) || v < min || v > max) {
            errors.add(label + " phải từ " + format(min) + " đến " + format(max));
        }
    }

    private static String format(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private static Duration seconds(double s) {
        return Duration.ofMillis(Math.round(s * 1000));
    }

    private static Duration hours(double h) {
        return Duration.ofMillis(Math.round(h * 3_600_000));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static AiSettings merge(AiSettings s, AiSettings.KillSwitch ks) {
        return new AiSettings(ks, s.autoIntents(), s.handoffIntents(), s.businessStart(), s.businessEnd(),
                s.businessDays(), s.outOfHoursAck(), s.handoffAck(), s.ackCooldownHours(), s.personaName(),
                s.companyName(), s.typingIndicator(), s.debounceSeconds(), s.autoHandbackHours(), s.model(),
                s.effort(), s.assistantModel(), s.maxAttempts(), s.retryBackoffSeconds());
    }

    private Map<String, Object> withoutKillSwitch(AiSettings s) {
        Map<String, Object> m = new LinkedHashMap<>(snake.convertValue(s, Map.class));
        m.remove(KILL_SWITCH);
        return m;
    }

    /**
     * Thứ tự cố định cho danh sách (loại câu hỏi theo AiReplySchema.INTENTS, ngày theo tuần) và bỏ khoảng trắng thừa,
     * để so với mặc định không lệch chỉ vì thứ tự chọn trên form.
     */
    static AiSettings normalize(AiSettings s) {
        return new AiSettings(s.killSwitch(), ordered(s.autoIntents()), ordered(s.handoffIntents()),
                strip(s.businessStart()), strip(s.businessEnd()),
                s.businessDays() == null ? List.of() : s.businessDays().stream().distinct().sorted().toList(),
                blankToNull(strip(s.outOfHoursAck())), blankToNull(strip(s.handoffAck())), s.ackCooldownHours(),
                strip(s.personaName()), strip(s.companyName()), s.typingIndicator(), s.debounceSeconds(),
                s.autoHandbackHours(), strip(s.model()), strip(s.effort()), strip(s.assistantModel()), s.maxAttempts(),
                s.retryBackoffSeconds());
    }

    private static List<String> ordered(List<String> intents) {
        return intents == null ? List.of() : AiReplySchema.INTENTS.stream().filter(intents::contains).toList();
    }

    private static String strip(String s) {
        return s == null ? null : s.strip();
    }
}
