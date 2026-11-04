import { ref } from 'vue';
import { useMessage } from '@/hooks/web/useMessage';
import { knowledgeDiffApi, knowledgeDiscardDraftApi, knowledgeSaveRuleApi } from '@/api/knowledge';
import type { KnowledgeDocDetail } from '@/api/knowledge/model/knowledgeModel';
import { apiError } from '../shared/errors';

/**
 * Lưu nháp một rule từ form: lỗi kiểm tra hiện ở `errors`; 409 (đã có bản mới hơn / người khác vừa lưu) mở modal
 * diff qua `openDiff`, bấm "Vẫn lưu đè" thì gọi lại `save(value, true)`.
 */
export function useRuleSave(
  name: string,
  detail: () => KnowledgeDocDetail,
  onSaved: (d: KnowledgeDocDetail) => void,
  openDiff: (data: Recordable) => void,
) {
  const { createMessage } = useMessage();
  const saving = ref(false);
  const errors = ref<string[]>([]);

  async function save(value: Recordable, force = false) {
    const d = detail();
    saving.value = true;
    errors.value = [];
    try {
      const saved = await knowledgeSaveRuleApi(name, {
        title: d.doc.title,
        value,
        baseVersionId: d.latest?.id,
        expectedDraftUpdatedAt: d.draft?.updatedAt,
        force,
      });
      createMessage.success('Đã lưu nháp. Vào trang Phát hành để áp dụng cho bot.');
      onSaved(saved);
    } catch (e) {
      const err = apiError(e);
      if (err.status === 409) {
        openDiff({
          title: 'Có thay đổi mới hơn bản bạn đang sửa',
          message: err.message,
          diff: err.data?.diff,
          okText: 'Vẫn lưu đè',
        });
      } else {
        errors.value = err.data?.errors ?? [err.message];
      }
    } finally {
      saving.value = false;
    }
  }

  async function discard() {
    onSaved(await knowledgeDiscardDraftApi(detail().doc.id));
    errors.value = [];
    createMessage.success('Đã bỏ bản nháp');
  }

  async function diff() {
    const d = detail();
    const r = await knowledgeDiffApi(d.doc.id);
    openDiff({
      title: `${d.doc.key}: ${r.from ? 'v' + r.from.versionNo : 'trống'} → ${r.to ? 'v' + r.to.versionNo : 'trống'}`,
      diff: r.diff,
    });
  }

  return { saving, errors, save, discard, diff };
}
