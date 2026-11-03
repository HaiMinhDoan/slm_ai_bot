package vn.slmglobal.slmai.services.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.slmglobal.slmai.services.knowledge.rules.RuleSet;
import vn.slmglobal.slmai.services.lead.LeadScoringRule;
import vn.slmglobal.slmai.services.lead.LeadScoringRuleSource;
import vn.slmglobal.slmai.services.settings.AiSettingsService;
import vn.slmglobal.slmai.services.sizing.PricingProperties;
import vn.slmglobal.slmai.services.sizing.PricingSource;
import vn.slmglobal.slmai.services.sizing.ProvinceDirectory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Kiến thức và rule bot đang dùng: bản phát hành ACTIVE trong CSDL (mục 5.2, 5.3). Sửa trên admin không ảnh hưởng
 * bot cho tới khi kích hoạt một bản phát hành; kích hoạt xong thì nạp lại sau commit, dự phòng tự kiểm tra mỗi
 * 30 giây (chạy nhiều instance, sửa thẳng CSDL).
 *
 * <p>Lần khởi động đầu (CSDL chưa có bản phát hành) nhập kiến thức trong repo thành bản #1. Không đọc được CSDL thì
 * dùng tạm kiến thức trong repo để bot vẫn trả lời.
 *
 * <p>Thứ tự tài liệu cố định theo mã để khối kiến thức không đổi giữa các lượt (prompt cache, mục 5.4).
 */
@Slf4j
@Component
public class KnowledgeBase implements PricingSource, LeadScoringRuleSource {

    /** Chỗ thay tên công ty và tên tư vấn viên trong tài liệu và prompt (lấy từ Cài đặt AI) */
    public static final String COMPANY_PLACEHOLDER = "{{ten_cong_ty}}";
    public static final String PERSONA_PLACEHOLDER = "{{ten_tu_van_vien}}";

    private final KnowledgeStore store;
    private final KnowledgeSeeder seeder;
    private final AiSettingsService settings;
    private final ProvinceDirectory provinces;
    private final PricingProperties defaultPricing;
    private final ObjectMapper objectMapper;
    private final AtomicReference<KnowledgeSnapshot> current = new AtomicReference<>();

    public KnowledgeBase(KnowledgeStore store, KnowledgeSeeder seeder, AiSettingsService settings,
                         ProvinceDirectory provinces, PricingProperties defaultPricing, ObjectMapper objectMapper) {
        this.store = store;
        this.seeder = seeder;
        this.settings = settings;
        this.provinces = provinces;
        this.defaultPricing = defaultPricing;
        this.objectMapper = objectMapper;
    }

    /** Snapshot đang dùng; lần đầu thì nhập (nếu cần) và nạp */
    public KnowledgeSnapshot current() {
        KnowledgeSnapshot s = current.get();
        if (s == null) {
            synchronized (this) {
                s = current.get();
                if (s == null) {
                    s = initialize();
                }
            }
        }
        return s;
    }

    /** kt_phat_hanh.phien_ban_kt của bản đang chạy (luot_goi_ai.phien_ban_kt) */
    public String version() {
        return current().version();
    }

    public RuleSet rules() {
        return current().rules();
    }

    @Override
    public PricingProperties pricing() {
        return rules().pricing();
    }

    @Override
    public LeadScoringRule leadScoring() {
        return rules().leadScoring();
    }

    /** Khối kiến thức trong prompt, đã thay tên công ty / tư vấn viên theo Cài đặt AI */
    public String renderKnowledge(KnowledgeSnapshot s) {
        return s.renderKnowledge(settings.ai().companyName(), settings.ai().personaName());
    }

    /** Hướng dẫn hệ thống cho AI (prompts/customer-chat), đã thay tên */
    public String renderPrompt(KnowledgeSnapshot s) {
        return s.renderPrompt(settings.ai().companyName(), settings.ai().personaName());
    }

    /** Rule dự phòng: cấu hình đang chạy trước khi có Knowledge Editor */
    public RuleSet defaultRules() {
        return ClasspathKnowledge.defaultRules(defaultPricing, objectMapper);
    }

    /** Kiến thức trong repo (dùng để nhập lần đầu, xem lại bản gốc) */
    public List<KnowledgeSnapshot.Doc> classpathDocs() {
        return ClasspathKnowledge.docs(defaultPricing, objectMapper);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        current();
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onActivated(KnowledgeReleaseActivated event) {
        reload();
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void poll() {
        KnowledgeSnapshot s = current.get();
        if (s == null) {
            return;
        }
        try {
            Long active = store.activeReleaseId().orElse(null);
            if (active != null && !Objects.equals(active, s.releaseId())) {
                reload();
            }
        } catch (RuntimeException e) {
            log.warn("Không kiểm tra được bản phát hành kiến thức đang chạy: {}", e.getMessage());
        }
    }

    /** Nạp lại bản phát hành ACTIVE; lỗi thì giữ bản đang dùng */
    public void reload() {
        try {
            Optional<KnowledgeSnapshot> loaded = store.loadActive(defaultRules());
            loaded.ifPresent(this::use);
        } catch (RuntimeException e) {
            log.error("Nạp lại kiến thức lỗi, giữ bản đang dùng: {}", e.getMessage(), e);
        }
    }

    private KnowledgeSnapshot initialize() {
        try {
            seeder.seedIfEmpty(classpathDocs());
            Optional<KnowledgeSnapshot> loaded = store.loadActive(defaultRules());
            if (loaded.isPresent()) {
                return use(loaded.get());
            }
        } catch (RuntimeException e) {
            log.error("Không đọc được kiến thức trong CSDL, tạm dùng kiến thức trong repo: {}", e.getMessage(), e);
        }
        return use(classpathSnapshot());
    }

    private KnowledgeSnapshot classpathSnapshot() {
        List<KnowledgeSnapshot.Doc> docs = classpathDocs();
        java.util.Map<String, String> hashes = new java.util.LinkedHashMap<>();
        docs.forEach(d -> hashes.put(d.key(), KnowledgeHash.content(d.content())));
        return new KnowledgeSnapshot(null, KnowledgeHash.release(hashes), docs, defaultRules());
    }

    private KnowledgeSnapshot use(KnowledgeSnapshot s) {
        try {
            provinces.apply(s.rules().regions());
        } catch (RuntimeException e) {
            log.error("Rule tỉnh → miền của bản phát hành #{} không dùng được, giữ danh mục cũ: {}", s.releaseId(),
                    e.getMessage());
        }
        KnowledgeSnapshot previous = current.getAndSet(s);
        if (previous == null || !Objects.equals(previous.version(), s.version())) {
            log.info("Kiến thức đang chạy: bản phát hành #{} ({} tài liệu, phiên bản {})", s.releaseId(),
                    s.docs().size(), s.version().substring(0, 12));
        }
        return s;
    }
}
