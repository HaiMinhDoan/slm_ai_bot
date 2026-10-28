package vn.slmglobal.slmai.services.lead;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.services.ai.AiReply;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.lead.repository.ConsultProfileRepository;

import java.util.List;
import java.util.UUID;

/**
 * Cập nhật ho_so_tu_van sau mỗi lượt được chốt (mục 19, 58.5): gộp thông tin khách vừa cung cấp, tăng số câu đã
 * trả lời, gộp tín hiệu phân loại, tính lại mức tiềm năng. Chạy trong transaction chốt lượt của AiTurnStore.
 */
@Service
@RequiredArgsConstructor
public class ConsultProfileService {

    private final ConsultProfileRepository repository;

    /**
     * @param reply              kết quả AI; null khi lượt lỗi (vẫn ghi nhận số điện thoại)
     * @param phoneGivenThisTurn khách vừa gửi số điện thoại trong các tin của lượt này
     * @param hasPhone           khách đã có số điện thoại (lượt này hoặc trước đó)
     * @param questionDelivered  câu hỏi trong reply thật sự được gửi khách (AUTO); nháp chưa tính
     * @param quotedPackageIds   gói tool đề xuất trong lượt (theo hoá đơn của khách); chỉ ghi khi tin được gửi
     * @param cheapestQuotedVnd  giá thấp nhất trong các gói đề xuất; chỉ ghi khi tin được gửi
     */
    public record Update(AiReply reply, boolean phoneGivenThisTurn, boolean hasPhone, boolean questionDelivered,
                         List<Integer> quotedPackageIds, Long cheapestQuotedVnd) {

        public Update(AiReply reply, boolean phoneGivenThisTurn, boolean hasPhone, boolean questionDelivered) {
            this(reply, phoneGivenThisTurn, hasPhone, questionDelivered, List.of(), null);
        }
    }

    public ConsultProfile get(UUID conversationId) {
        return repository.findById(conversationId).orElse(null);
    }

    public ConsultProfile apply(UUID conversationId, UUID customerId, Update u) {
        ConsultProfile profile = repository.findById(conversationId).orElseGet(() -> ConsultProfile.builder()
                .conversationId(conversationId)
                .customerId(customerId)
                .build());
        // Bản sao mới để Hibernate chắc chắn thấy cột JSON đã đổi
        ObjectNode slots = profile.getSlots().deepCopy();
        ObjectNode qualification = profile.getQualification().deepCopy();
        AiReply reply = u.reply();
        AiReply.Collected collected = reply == null ? null : reply.collected();
        AiReply.Qualification newQualification = reply == null ? null : reply.qualification();

        if (answeredLastQuestion(profile.getLastQuestionAsked(), collected, newQualification, qualification,
                u.phoneGivenThisTurn())) {
            profile.setAnsweredQuestions(profile.getAnsweredQuestions() + 1);
        }
        if (collected != null) {
            putIfPresent(slots, ConsultSlots.KHU_VUC, collected.province());
            putIfPresent(slots, ConsultSlots.SO_PHA, collected.phases());
            putIfPresent(slots, ConsultSlots.HOA_DON_THANG, collected.monthlyBillVnd());
            putIfPresent(slots, ConsultSlots.TY_LE_NGAY_DEM, collected.dayNightUsage());
            putIfPresent(slots, ConsultSlots.LOAI_MAI, collected.roofType());
            putIfPresent(slots, ConsultSlots.DIEN_TICH_MAI, collected.roofAreaM2());
        }
        mergeQualification(qualification, newQualification);

        profile.setSlots(slots);
        profile.setQualification(qualification);
        List<String> missing = ConsultSlots.REQUIRED.stream().filter(k -> isMissing(slots, k)).toList();
        profile.setMissingSlots(missing.toArray(String[]::new));
        if (reply != null) {
            profile.setLastQuestionAsked(u.questionDelivered() ? reply.questionAsked() : null);
        }
        // Lần báo giá mới nhất thay lần trước: khách có thể vừa sửa hoá đơn, số pha, tỉnh
        if (u.questionDelivered() && u.quotedPackageIds() != null && !u.quotedPackageIds().isEmpty()) {
            profile.setRecommendedPackageIds(u.quotedPackageIds().toArray(Integer[]::new));
            profile.setCheapestQuotedPrice(u.cheapestQuotedVnd());
        }

        LeadScoring.Score score = LeadScoring.score(new LeadScoring.Signals(
                u.hasPhone(),
                textOrNull(qualification.path("timeline")),
                missing.isEmpty(),
                profile.getAnsweredQuestions(),
                budgetMax(qualification),
                profile.getCheapestQuotedPrice()));
        profile.setLeadLevel(score.level());
        profile.setLeadLevelReason(score.reason());
        return repository.save(profile);
    }

    /** Tin sau của khách trả lời đúng thông tin AI vừa hỏi (so với hồ sơ trước lượt này). */
    private static boolean answeredLastQuestion(String lastQuestion, AiReply.Collected collected,
                                                AiReply.Qualification newQ, ObjectNode oldQ, boolean phoneGiven) {
        if (lastQuestion == null) {
            return false;
        }
        return switch (lastQuestion) {
            case ConsultSlots.Q_SO_DIEN_THOAI -> phoneGiven;
            case ConsultSlots.Q_NGAN_SACH -> newQ != null && newQ.budgetVnd() != null && newQ.budgetVnd().isKnown()
                    && !oldQ.hasNonNull("budget_vnd");
            case ConsultSlots.Q_THOI_DIEM -> newQ != null && newQ.timeline() != null && !oldQ.hasNonNull("timeline");
            case ConsultSlots.Q_KHAC -> false;
            default -> collected != null && collectedValue(collected, lastQuestion) != null;
        };
    }

    private static Object collectedValue(AiReply.Collected c, String key) {
        return switch (key) {
            case ConsultSlots.KHU_VUC -> c.province();
            case ConsultSlots.SO_PHA -> c.phases();
            case ConsultSlots.HOA_DON_THANG -> c.monthlyBillVnd();
            case ConsultSlots.TY_LE_NGAY_DEM -> c.dayNightUsage();
            case ConsultSlots.LOAI_MAI -> c.roofType();
            case ConsultSlots.DIEN_TICH_MAI -> c.roofAreaM2();
            case ConsultSlots.TEN_KHACH -> c.customerName();
            case ConsultSlots.DANH_XUNG -> c.salutation();
            default -> null;
        };
    }

    private static void mergeQualification(ObjectNode target, AiReply.Qualification q) {
        if (q == null) {
            return;
        }
        if (q.researchLevel() != null && q.researchLevel().value() != null) {
            target.putObject("research_level")
                    .put("value", q.researchLevel().value())
                    .put("evidence", q.researchLevel().evidence());
        }
        if (q.budgetVnd() != null && q.budgetVnd().isKnown()) {
            ObjectNode b = target.putObject("budget_vnd");
            if (q.budgetVnd().min() != null) {
                b.put("min", q.budgetVnd().min());
            }
            if (q.budgetVnd().max() != null) {
                b.put("max", q.budgetVnd().max());
            }
        }
        if (q.timeline() != null) {
            target.put("timeline", q.timeline());
        }
    }

    private static Long budgetMax(ObjectNode qualification) {
        JsonNode b = qualification.path("budget_vnd");
        if (b.hasNonNull("max")) {
            return b.get("max").asLong();
        }
        return b.hasNonNull("min") ? b.get("min").asLong() : null;
    }

    private static boolean isMissing(ObjectNode slots, String key) {
        JsonNode v = slots.get(key);
        return v == null || v.isNull() || (v.isTextual() && v.asText().isBlank());
    }

    private static void putIfPresent(ObjectNode node, String key, Object value) {
        if (value instanceof String s && !s.isBlank()) {
            node.put(key, s.strip());
        } else if (value instanceof Integer i) {
            node.put(key, i);
        } else if (value instanceof Long l) {
            node.put(key, l);
        } else if (value instanceof Double d) {
            node.put(key, d);
        }
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode() ? null : node.asText();
    }
}
