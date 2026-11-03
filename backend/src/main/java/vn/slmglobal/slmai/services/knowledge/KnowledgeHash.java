package vn.slmglobal.slmai.services.knowledge;

import vn.slmglobal.slmai.common.util.HashUtil;

import java.util.Map;
import java.util.TreeMap;

/** Mã băm nội dung và mã phiên bản bản phát hành (kt_phat_hanh.phien_ban_kt). */
public final class KnowledgeHash {

    private KnowledgeHash() {
    }

    public static String content(String content) {
        return HashUtil.sha256Hex(content == null ? "" : content);
    }

    /** Hash của các cặp (mã tài liệu, mã băm nội dung), xếp theo mã: hai bản phát hành cùng nội dung có cùng mã */
    public static String release(Map<String, String> contentHashByKey) {
        StringBuilder sb = new StringBuilder();
        new TreeMap<>(contentHashByKey).forEach((k, h) -> sb.append(k).append(':').append(h).append('\n'));
        return HashUtil.sha256Hex(sb.toString());
    }
}
