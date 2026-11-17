import type {
  LinkRole,
  LinkTargetType,
  MediaLinkInput,
  MediaLinkModel,
} from '@/api/media/model/mediaModel';

/** Một dòng liên kết trên form (form sửa và hộp Thêm media dùng chung) */
export interface LinkRow {
  uid: number;
  targetType: LinkTargetType;
  targetKey?: string;
  role: LinkRole;
  sortOrder: number;
  options: { value: string; label: string; twinKey?: string; twinLabel?: string }[];
  loading: boolean;
}

let uid = 0;

export function newLinkRow(partial: Partial<LinkRow> = {}): LinkRow {
  return {
    uid: ++uid,
    targetType: 'PACKAGE',
    role: 'RELATED',
    sortOrder: 0,
    options: [],
    loading: false,
    ...partial,
  };
}

export function toLinkRows(links: MediaLinkModel[]): LinkRow[] {
  return links.map((l) =>
    newLinkRow({
      targetType: l.targetType,
      targetKey: l.targetKey,
      role: l.role,
      sortOrder: l.sortOrder,
      options: [{ value: l.targetKey, label: l.label ?? `${l.targetKey} (không còn)` }],
    }),
  );
}

export function toLinkInputs(rows: LinkRow[]): MediaLinkInput[] {
  return rows.map((r) => ({
    targetType: r.targetType,
    targetKey: r.targetKey!,
    role: r.role,
    sortOrder: r.sortOrder ?? 0,
  }));
}

/** Chuỗi so sánh để biết danh sách liên kết có đổi không */
export function linksKey(rows: LinkRow[]): string {
  return JSON.stringify(rows.map((r) => [r.targetType, r.targetKey, r.role, r.sortOrder ?? 0]));
}
