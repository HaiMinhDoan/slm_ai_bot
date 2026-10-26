package vn.slmglobal.slmai.services.lead;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.lead.entity.Lead;
import vn.slmglobal.slmai.services.lead.entity.LeadLevel;
import vn.slmglobal.slmai.services.lead.entity.LeadStatus;
import vn.slmglobal.slmai.services.lead.repository.LeadRepository;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Khách cho số điện thoại thì luôn tạo lead (mục 23, 58.5). Khoá chống trùng hoi_thoai_id + sdt nên chạy lại lượt
 * không tạo lead trùng. Gửi sang Sales API là mốc sau (adapters/salesapi); ở đây lead dừng ở trạng thái NEW.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository repository;
    private final NotificationService notifications;

    public Lead upsert(Conversation conversation, String phone, ConsultProfile profile) {
        String key = conversation.getId() + ":" + phone;
        Lead lead = repository.findByIdempotencyKey(key).orElse(null);
        if (lead == null) {
            lead = Lead.builder()
                    .customerId(conversation.getCustomerId())
                    .conversationId(conversation.getId())
                    .sourceChannel(conversation.getChannel().name())
                    .phone(phone)
                    .idempotencyKey(key)
                    .build();
            log.info("Tạo lead mới từ hội thoại {} ({})", conversation.getId(), profile.getLeadLevel());
        } else if (lead.getStatus() != LeadStatus.NEW) {
            // Đã gửi Sales API: cập nhật lead đã gửi thuộc mốc Sales API
            return lead;
        }
        boolean becameHot = profile.getLeadLevel() == LeadLevel.HOT && lead.getLeadLevel() != LeadLevel.HOT;
        JsonNode slots = profile.getSlots();
        lead.setProvince(slots.hasNonNull(ConsultSlots.KHU_VUC) ? slots.get(ConsultSlots.KHU_VUC).asText() : null);
        // Miền (BAC / NAM) chờ danh sách tỉnh của doanh nghiệp (mục 4.2.9)
        lead.setNeedSummary(needSummary(slots));
        lead.setLeadLevel(profile.getLeadLevel());
        lead.setLeadLevelReason(profile.getLeadLevelReason());
        lead.setQualification(profile.getQualification().deepCopy());
        lead.setRecommendedPackageIds(profile.getRecommendedPackageIds());
        Lead saved = repository.save(lead);
        if (becameHot) {
            // Báo sales ngay (mục 58.5), chỉ lúc vừa chuyển sang HOT
            log.info("Lead HOT ở hội thoại {}: {}", conversation.getId(), profile.getLeadLevelReason());
            notifyHot(conversation, saved);
        }
        return saved;
    }

    private void notifyHot(Conversation conversation, Lead lead) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("leadId", lead.getId());
        data.put("phone", lead.getPhone());
        data.put("province", lead.getProvince());
        data.put("needSummary", lead.getNeedSummary());
        data.put("leadLevelReason", lead.getLeadLevelReason());
        String need = lead.getNeedSummary() == null ? "" : " Nhu cầu: " + lead.getNeedSummary() + ".";
        notifications.notify(NotificationDraft.builder()
                .type(NotificationType.LEAD_HOT)
                .severity(NotificationSeverity.URGENT)
                .title("Khách HOT" + (lead.getProvince() == null ? "" : " ở " + lead.getProvince()))
                .body("Khách đã để lại số điện thoại, nên liên hệ ngay." + need)
                .data(data)
                .conversationId(conversation.getId())
                .customerId(conversation.getCustomerId())
                .dedupeKey("LEAD_HOT:" + lead.getId())
                .build());
    }

    /** Tóm tắt nhu cầu cho sales, ví dụ "Hà Nội; 1 pha; tiền điện 2.000.000đ/tháng; dùng ban ngày nhiều; mái tôn" */
    static String needSummary(JsonNode slots) {
        List<String> parts = new ArrayList<>();
        if (slots.hasNonNull(ConsultSlots.KHU_VUC)) {
            parts.add(slots.get(ConsultSlots.KHU_VUC).asText());
        }
        if (slots.hasNonNull(ConsultSlots.SO_PHA)) {
            parts.add(slots.get(ConsultSlots.SO_PHA).asInt() + " pha");
        }
        if (slots.hasNonNull(ConsultSlots.HOA_DON_THANG)) {
            NumberFormat f = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
            parts.add("tiền điện " + f.format(slots.get(ConsultSlots.HOA_DON_THANG).asLong()) + "đ/tháng");
        }
        if (slots.hasNonNull(ConsultSlots.TY_LE_NGAY_DEM)) {
            parts.add(switch (slots.get(ConsultSlots.TY_LE_NGAY_DEM).asText()) {
                case "NGAY_NHIEU" -> "dùng ban ngày nhiều";
                case "DEM_NHIEU" -> "dùng buổi tối nhiều";
                default -> "dùng ngày đêm cân bằng";
            });
        }
        if (slots.hasNonNull(ConsultSlots.LOAI_MAI)) {
            parts.add(switch (slots.get(ConsultSlots.LOAI_MAI).asText()) {
                case "TON" -> "mái tôn";
                case "NGOI" -> "mái ngói";
                default -> "mái bằng";
            });
        }
        if (slots.hasNonNull(ConsultSlots.DIEN_TICH_MAI)) {
            parts.add("mái khoảng " + slots.get(ConsultSlots.DIEN_TICH_MAI).asText() + " m2");
        }
        return parts.isEmpty() ? null : String.join("; ", parts);
    }
}
