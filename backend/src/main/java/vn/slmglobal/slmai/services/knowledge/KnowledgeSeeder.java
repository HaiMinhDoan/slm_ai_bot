package vn.slmglobal.slmai.services.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeDoc;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeRelease;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeReleaseItem;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeVersion;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeDocRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeReleaseItemRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeReleaseRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeVersionRepository;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nhập kiến thức trong repo thành bản phát hành #1 khi CSDL chưa có bản phát hành nào (lần khởi động đầu sau V10),
 * để bot chạy y như trước khi có Knowledge Editor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSeeder {

    static final String SYSTEM_AUTHOR = "system";

    private final KnowledgeDocRepository docs;
    private final KnowledgeVersionRepository versions;
    private final KnowledgeReleaseRepository releases;
    private final KnowledgeReleaseItemRepository items;

    /** @return true khi vừa nhập */
    @Transactional
    public boolean seedIfEmpty(List<KnowledgeSnapshot.Doc> seed) {
        if (releases.count() > 0) {
            return false;
        }
        Map<String, String> hashes = new LinkedHashMap<>();
        Map<Long, Long> versionByDoc = new LinkedHashMap<>();
        for (KnowledgeSnapshot.Doc d : seed) {
            KnowledgeDoc doc = docs.findByKey(d.key()).orElseGet(() -> docs.save(KnowledgeDoc.builder()
                    .key(d.key()).type(d.type()).format(d.type().format()).createdBy(SYSTEM_AUTHOR).build()));
            String hash = KnowledgeHash.content(d.content());
            KnowledgeVersion v = versions.save(KnowledgeVersion.builder()
                    .docId(doc.getId())
                    .versionNo(versions.maxVersionNo(doc.getId()) + 1)
                    .status(KnowledgeVersion.Status.FROZEN)
                    .title(d.title())
                    .content(d.content())
                    .contentHash(hash)
                    .changeNote("Nhập từ repo khi bật Knowledge Editor")
                    .author(SYSTEM_AUTHOR)
                    .build());
            hashes.put(d.key(), hash);
            versionByDoc.put(doc.getId(), v.getId());
        }
        KnowledgeRelease release = releases.save(KnowledgeRelease.builder()
                .kbVersion(KnowledgeHash.release(hashes))
                .status(KnowledgeRelease.Status.ACTIVE)
                .notes("Bản đầu: nhập kiến thức và rule đang chạy từ repo")
                .createdBy(SYSTEM_AUTHOR)
                .activatedBy(SYSTEM_AUTHOR)
                .activatedAt(OffsetDateTime.now())
                .build());
        versionByDoc.forEach((docId, versionId) -> items.save(new KnowledgeReleaseItem(release.getId(), docId,
                versionId)));
        log.info("Đã nhập {} tài liệu kiến thức từ repo thành bản phát hành #{}", seed.size(), release.getId());
        return true;
    }
}
