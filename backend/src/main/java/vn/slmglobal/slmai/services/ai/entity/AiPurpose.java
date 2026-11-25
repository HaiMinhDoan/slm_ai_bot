package vn.slmglobal.slmai.services.ai.entity;

/** Mục đích của một lần gọi Claude (luot_goi_ai.muc_dich). */
public enum AiPurpose {
    CUSTOMER_CHAT,
    CONTENT_ASSISTANT,
    DAILY_BRIEF,
    KNOWLEDGE_ASSISTANT,
    RELEASE_EVAL,
    /** Tin nhắc khách im lặng (mục 56.9, V17) */
    FOLLOW_UP
}
