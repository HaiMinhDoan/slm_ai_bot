package vn.slmglobal.slmai.services.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.media.entity.Topic;
import vn.slmglobal.slmai.services.media.repository.TopicRepository;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bộ chủ đề cố định cho media (mục 6.2), sửa trên admin, có hiệu lực ngay. Không xoá chủ đề (media cũ đang gắn):
 * tắt thì không chọn mới được.
 */
@Service
@RequiredArgsConstructor
public class MediaTopicService {

    private final TopicRepository topics;
    private final AuditLogService audit;

    public record TopicResponse(String code, String name, String description, int sortOrder, boolean active,
                                String updatedBy, OffsetDateTime updatedAt) {
    }

    /** @param code chỉ dùng khi tạo mới; không đổi được vì media lưu theo mã */
    public record TopicRequest(
            @Size(max = 50) @Pattern(regexp = "^$|^[a-z0-9]+(-[a-z0-9]+)*$",
                    message = "Mã chủ đề viết thường không dấu, nối bằng gạch, ví dụ pin-luu-tru") String code,
            @NotBlank(message = "Chưa nhập tên chủ đề") @Size(max = 100, message = "Tên tối đa 100 ký tự") String name,
            @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự") String description,
            Integer sortOrder,
            Boolean active) {
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> list() {
        return topics.findAllByOrderBySortOrderAscCodeAsc().stream().map(MediaTopicService::toResponse).toList();
    }

    @Transactional
    public TopicResponse create(AuthInfo actor, TopicRequest r) {
        String code = r.code() == null ? "" : r.code().strip();
        if (code.isEmpty()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Chưa nhập mã chủ đề");
        }
        if (topics.existsById(code)) {
            throw new CommonException(HttpStatus.CONFLICT, "Đã có chủ đề mã " + code);
        }
        Topic t = topics.saveAndFlush(Topic.builder()
                .code(code)
                .name(r.name().strip())
                .description(MediaAdminService.blankToNull(r.description()))
                .sortOrder(r.sortOrder() == null ? 0 : r.sortOrder())
                .active(r.active() == null || r.active())
                .updatedBy(actor.email())
                .build());
        audit.record(actor.id(), "topic.create", "topic", code, null, snapshot(t));
        return toResponse(t);
    }

    @Transactional
    public TopicResponse update(AuthInfo actor, String code, TopicRequest r) {
        Topic t = topics.findById(code).orElseThrow(() -> CommonException.notFound("Không có chủ đề " + code));
        Map<String, Object> before = snapshot(t);
        t.setName(r.name().strip());
        t.setDescription(MediaAdminService.blankToNull(r.description()));
        if (r.sortOrder() != null) {
            t.setSortOrder(r.sortOrder());
        }
        if (r.active() != null) {
            t.setActive(r.active());
        }
        t.setUpdatedBy(actor.email());
        topics.saveAndFlush(t);
        audit.record(actor.id(), "topic.update", "topic", code, before, snapshot(t));
        return toResponse(t);
    }

    private static TopicResponse toResponse(Topic t) {
        return new TopicResponse(t.getCode(), t.getName(), t.getDescription(), t.getSortOrder(), t.isActive(),
                t.getUpdatedBy(), t.getUpdatedAt());
    }

    private static Map<String, Object> snapshot(Topic t) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", t.getName());
        data.put("description", t.getDescription());
        data.put("sortOrder", t.getSortOrder());
        data.put("active", t.isActive());
        return data;
    }
}
