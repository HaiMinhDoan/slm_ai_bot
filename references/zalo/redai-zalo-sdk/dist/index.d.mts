import { AxiosInstance } from 'axios';

/**
 * Common types and interfaces for Zalo SDK
 */
/**
 * Standard Zalo API response wrapper
 */
interface ZaloResponse<T = any> {
    /**
     * Error code (0 = success)
     */
    error: number;
    /**
     * Response message
     */
    message: string;
    /**
     * Response data
     */
    data?: T;
}
/**
 * Zalo API error response
 */
interface ZaloErrorResponse {
    /**
     * Error code
     */
    error: number;
    /**
     * Error name
     */
    error_name?: string;
    /**
     * Error reason
     */
    error_reason?: string;
    /**
     * Error description
     */
    error_description?: string;
    /**
     * Reference documentation link
     */
    ref_doc?: string;
}
/**
 * Union type for Zalo API responses
 */
type ZaloApiResponse<T> = ZaloResponse<T> | (ZaloErrorResponse & Partial<T>);
/**
 * SDK Configuration
 */
interface ZaloSDKConfig {
    /**
     * Application ID
     */
    appId: string;
    /**
     * Application Secret
     */
    appSecret: string;
    /**
     * API timeout in milliseconds (default: 30000)
     */
    timeout?: number;
    /**
     * Enable debug logging (default: false)
     */
    debug?: boolean;
    /**
     * Custom API base URL (optional)
     */
    apiBaseUrl?: string;
    /**
     * Retry configuration
     */
    retry?: {
        /**
         * Number of retry attempts (default: 3)
         */
        attempts?: number;
        /**
         * Delay between retries in milliseconds (default: 1000)
         */
        delay?: number;
    };
}
/**
 * HTTP method types
 */
type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
/**
 * Request configuration
 */
interface RequestConfig {
    /**
     * HTTP method
     */
    method: HttpMethod;
    /**
     * Request URL
     */
    url: string;
    /**
     * Request headers
     */
    headers?: Record<string, string>;
    /**
     * Query parameters
     */
    params?: Record<string, any>;
    /**
     * Request body data
     */
    data?: any;
    /**
     * Request timeout
     */
    timeout?: number;
}
/**
 * Pagination parameters
 */
interface PaginationParams {
    /**
     * Offset for pagination
     */
    offset?: number;
    /**
     * Number of items per page
     */
    count?: number;
    /**
     * Maximum items per page (default: 50)
     */
    limit?: number;
}
/**
 * Paginated response
 */
interface PaginatedResponse<T> {
    /**
     * Array of items
     */
    items: T[];
    /**
     * Total number of items
     */
    total: number;
    /**
     * Current offset
     */
    offset: number;
    /**
     * Number of items in current page
     */
    count: number;
    /**
     * Whether there are more items
     */
    hasMore: boolean;
    /**
     * Next offset for pagination
     */
    nextOffset?: number;
}
/**
 * File upload information
 */
interface FileUpload {
    /**
     * File buffer or stream
     */
    file: Buffer | NodeJS.ReadableStream;
    /**
     * Original filename
     */
    filename: string;
    /**
     * MIME type
     */
    mimetype: string;
    /**
     * File size in bytes
     */
    size?: number;
}
/**
 * Attachment information
 */
interface Attachment {
    /**
     * Attachment ID from Zalo
     */
    attachment_id: string;
    /**
     * File URL
     */
    url: string;
    /**
     * File type
     */
    type?: string;
    /**
     * File size in bytes
     */
    size?: number;
}
/**
 * SDK Error class
 */
declare class ZaloSDKError extends Error {
    readonly code: number;
    readonly details?: any;
    constructor(message: string, code?: number, details?: any);
}
/**
 * Logger interface
 */
interface Logger {
    debug(message: string, ...args: any[]): void;
    info(message: string, ...args: any[]): void;
    warn(message: string, ...args: any[]): void;
    error(message: string, ...args: any[]): void;
}
/**
 * Default console logger implementation
 */
declare class ConsoleLogger implements Logger {
    private readonly enableDebug;
    constructor(enableDebug?: boolean);
    debug(message: string, ...args: any[]): void;
    info(message: string, ...args: any[]): void;
    warn(message: string, ...args: any[]): void;
    error(message: string, ...args: any[]): void;
}

/**
 * Authentication related types and interfaces
 */
/**
 * Access token information
 */
interface AccessToken {
    /**
     * Access token string
     */
    access_token: string;
    /**
     * Token expiration time in seconds
     */
    expires_in: number;
    /**
     * Refresh token (if available)
     */
    refresh_token?: string;
}
/**
 * Refresh token response
 */
interface RefreshTokenResponse {
    /**
     * New access token
     */
    access_token: string;
    /**
     * Token expiration time in seconds
     */
    expires_in: number;
    /**
     * New refresh token (if available)
     */
    refresh_token?: string;
}
/**
 * OAuth authorization parameters
 */
interface OAuthParams {
    /**
     * Application ID
     */
    app_id: string;
    /**
     * Redirect URI
     */
    redirect_uri: string;
    /**
     * OAuth state parameter
     */
    state?: string;
    /**
     * Code verifier for PKCE (Social API)
     */
    code_verifier?: string;
    /**
     * Code challenge for PKCE (Social API)
     */
    code_challenge?: string;
    /**
     * Code challenge method for PKCE (Social API)
     */
    code_challenge_method?: 'S256' | 'plain';
}
/**
 * Authorization code exchange parameters
 */
interface AuthCodeParams {
    /**
     * Application ID
     */
    app_id: string;
    /**
     * Application secret
     */
    app_secret: string;
    /**
     * Authorization code
     */
    code: string;
    /**
     * Redirect URI (must match the one used in authorization)
     */
    redirect_uri: string;
    /**
     * Code verifier for PKCE (supports both Social API and Official Account API)
     */
    code_verifier?: string;
}
/**
 * Refresh token parameters
 */
interface RefreshTokenParams {
    /**
     * Application ID
     */
    app_id: string;
    /**
     * Application secret
     */
    app_secret: string;
    /**
     * Refresh token
     */
    refresh_token: string;
}
/**
 * Social user information from Zalo
 */
interface SocialUserInfo$1 {
    /**
     * User ID
     */
    id: string;
    /**
     * Display name
     */
    name: string;
    /**
     * Profile picture
     */
    picture?: {
        data: {
            url: string;
        };
    };
    /**
     * Gender
     */
    gender?: string;
    /**
     * Birthday
     */
    birthday?: string;
    /**
     * Location
     */
    location?: {
        name: string;
    };
    /**
     * Whether the account is sensitive (under 18)
     */
    is_sensitive?: boolean;
}
/**
 * Token validation result
 */
interface TokenValidation {
    /**
     * Whether the token is valid
     */
    valid: boolean;
    /**
     * Token expiration timestamp
     */
    expires_at?: number;
    /**
     * Associated user information (if available)
     */
    user_info?: SocialUserInfo$1;
}
/**
 * Authentication scope for different APIs
 */
declare enum AuthScope {
    /**
     * Official Account API scope
     */
    OA = "oa",
    /**
     * Social API scope
     */
    SOCIAL = "social",
    /**
     * ZNS (Zalo Notification Service) scope
     */
    ZNS = "zns"
}
/**
 * Authentication method
 */
declare enum AuthMethod {
    /**
     * OAuth 2.0 Authorization Code flow
     */
    AUTHORIZATION_CODE = "authorization_code",
    /**
     * Refresh token flow
     */
    REFRESH_TOKEN = "refresh_token"
}
/**
 * PKCE (Proof Key for Code Exchange) configuration
 */
interface PKCEConfig {
    /**
     * Code verifier
     */
    code_verifier: string;
    /**
     * Code challenge
     */
    code_challenge: string;
    /**
     * Code challenge method
     */
    code_challenge_method: 'S256' | 'plain';
}
/**
 * Authentication URLs
 */
interface AuthUrls {
    /**
     * Official Account authorization URL
     */
    oa_auth_url: string;
    /**
     * Social API authorization URL
     */
    social_auth_url: string;
    /**
     * Token exchange URL
     */
    token_url: string;
    /**
     * Token refresh URL
     */
    refresh_url: string;
}
/**
 * Official Account authorization result
 */
interface OAAuthResult {
    /**
     * Authorization URL
     */
    url: string;
    /**
     * State parameter used (auto-generated if not provided)
     */
    state: string;
    /**
     * PKCE configuration used (if PKCE was enabled)
     */
    pkce?: PKCEConfig;
}

/**
 * Official Account (OA) related types and interfaces
 */
/**
 * Official Account information
 */
interface OAInfo {
    /**
     * Official Account ID
     */
    oa_id: string;
    /**
     * OA name
     */
    name: string;
    /**
     * OA description
     */
    description: string;
    /**
     * OA alias
     */
    oa_alias: string;
    /**
     * Verification status
     */
    is_verified: boolean;
    /**
     * OA type (number)
     */
    oa_type: number;
    /**
     * Category name
     */
    cate_name: string;
    /**
     * Number of followers
     */
    num_follower: number;
    /**
     * Avatar URL
     */
    avatar: string;
    /**
     * Cover image URL
     */
    cover: string;
    /**
     * Package name
     */
    package_name: string;
    /**
     * Package expiration date
     */
    package_valid_through_date: string;
    /**
     * Package auto-renewal date
     */
    package_auto_renew_date: string;
    /**
     * Linked Zalo Cloud Account
     */
    linked_ZCA: string;
}
/**
 * Message quota information
 */
interface MessageQuota {
    /**
     * Daily quota limit
     */
    daily_quota: number;
    /**
     * Remaining quota for today
     */
    remaining_quota: number;
    /**
     * Quota type (e.g., "free", "paid")
     */
    quota_type: string;
    /**
     * Quota reset time (Unix timestamp)
     */
    reset_time: number;
}
/**
 * Detailed quota information request
 */
interface QuotaMessageRequest {
    /**
     * Quota owner (OA or APP)
     */
    quota_owner: string;
    /**
     * Product type
     */
    product_type?: 'cs' | 'transaction' | 'gmf10' | 'gmf50' | 'gmf100';
    /**
     * Quota type
     */
    quota_type?: 'sub_quota' | 'purchase_quota' | 'reward_quota';
}
/**
 * Quota asset information
 */
interface QuotaAsset {
    /**
     * Asset ID
     */
    asset_id: string;
    /**
     * Product type
     */
    product_type: string;
    /**
     * Quota type
     */
    quota_type: string;
    /**
     * Expiration date
     */
    valid_through: string;
    /**
     * Auto-renewal status
     */
    auto_renew: boolean;
    /**
     * Asset status
     */
    status: 'available' | 'used';
    /**
     * Used ID (for GMF, this is group_id)
     */
    used_id: string | null;
    /**
     * Total quota (legacy)
     */
    total?: number;
    /**
     * Remaining quota (legacy)
     */
    remain?: number;
}
/**
 * Quota message response
 */
interface QuotaMessageResponse {
    /**
     * List of quota assets
     */
    data: QuotaAsset[];
    /**
     * Error code
     */
    error: number;
    /**
     * Response message
     */
    message: string;
}
/**
 * GMF product types
 */
declare enum GMFProductType {
    GMF10 = "gmf10",
    GMF50 = "gmf50",
    GMF100 = "gmf100"
}
/**
 * Quota types
 */
declare enum QuotaType {
    SUB_QUOTA = "sub_quota",
    PURCHASE_QUOTA = "purchase_quota",
    REWARD_QUOTA = "reward_quota"
}
/**
 * OA settings
 */
interface OASettings {
    /**
     * Auto-reply settings
     */
    auto_reply?: {
        enabled: boolean;
        message?: string;
    };
    /**
     * Welcome message settings
     */
    welcome_message?: {
        enabled: boolean;
        message?: string;
    };
    /**
     * Business hours
     */
    business_hours?: {
        enabled: boolean;
        schedule?: Array<{
            day: number;
            start_time: string;
            end_time: string;
        }>;
    };
}
/**
 * OA statistics
 */
interface OAStatistics {
    /**
     * Total followers
     */
    total_followers: number;
    /**
     * New followers today
     */
    new_followers_today: number;
    /**
     * Messages sent today
     */
    messages_sent_today: number;
    /**
     * Messages received today
     */
    messages_received_today: number;
    /**
     * Engagement rate
     */
    engagement_rate?: number;
}
/**
 * OA profile update request
 */
interface UpdateOAProfileRequest {
    /**
     * OA name
     */
    name?: string;
    /**
     * OA description
     */
    description?: string;
    /**
     * Avatar image (base64 or URL)
     */
    avatar?: string;
    /**
     * Cover image (base64 or URL)
     */
    cover?: string;
}
/**
 * OA verification request
 */
interface OAVerificationRequest {
    /**
     * Business license number
     */
    business_license: string;
    /**
     * Business name
     */
    business_name: string;
    /**
     * Contact person name
     */
    contact_name: string;
    /**
     * Contact phone number
     */
    contact_phone: string;
    /**
     * Contact email
     */
    contact_email: string;
    /**
     * Additional documents
     */
    documents?: Array<{
        type: string;
        url: string;
    }>;
}

/**
 * User management related types and interfaces
 */
/**
 * User information from Zalo API v3.0
 */
interface UserInfo {
    /**
     * User ID
     */
    user_id: string;
    /**
     * User ID by app
     */
    user_id_by_app: string;
    /**
     * External user ID in business system
     */
    user_external_id: string;
    /**
     * Display name
     */
    display_name: string;
    /**
     * User alias
     */
    user_alias: string;
    /**
     * Whether user is under 18
     */
    is_sensitive: boolean;
    /**
     * Last interaction date (dd/MM/yyyy)
     */
    user_last_interaction_date: string;
    /**
     * Whether user is following OA
     */
    user_is_follower: boolean;
    /**
     * Avatar URL
     */
    avatar: string;
    /**
     * Avatar URLs with different sizes
     */
    avatars: {
        "120": string;
        "240": string;
    };
    /**
     * Dynamic param from last access
     */
    dynamic_param?: string;
    /**
     * Tags and notes information
     */
    tags_and_notes_info: {
        notes: string[];
        tag_names: string[];
    };
    /**
     * Shared information from user
     */
    shared_info?: {
        address?: string;
        city?: string;
        district?: string;
        phone?: string;
        name?: string;
        user_dob?: string;
    };
}
/**
 * User list request parameters
 */
interface UserListRequest {
    /**
     * Offset for pagination
     */
    offset: number;
    /**
     * Number of users to fetch (max 50)
     */
    count: number;
    /**
     * Filter by tag name
     */
    tag_name?: string;
    /**
     * Filter by last interaction period
     */
    last_interaction_period?: "TODAY" | "YESTERDAY" | "L7D" | "L30D" | string;
    /**
     * Filter by follower status
     */
    is_follower?: boolean;
}
/**
 * User list response
 */
interface UserListResponse {
    /**
     * Total number of users
     */
    total: number;
    /**
     * Number of users returned
     */
    count: number;
    /**
     * Current offset
     */
    offset: number;
    /**
     * List of user IDs
     */
    users: Array<{
        user_id: string;
    }>;
}
/**
 * User label/tag information
 */
interface UserLabel {
    /**
     * Label ID
     */
    label_id: string;
    /**
     * Label name
     */
    label_name: string;
    /**
     * Label description
     */
    description?: string;
    /**
     * Label color (hex)
     */
    color?: string;
    /**
     * Creation time (Unix timestamp)
     */
    created_time?: number;
    /**
     * Number of users with this label
     */
    user_count?: number;
}
/**
 * Create label request
 */
interface CreateLabelRequest {
    /**
     * Label name
     */
    label_name: string;
    /**
     * Label description
     */
    description?: string;
    /**
     * Label color (hex)
     */
    color?: string;
}
/**
 * User label operation request (legacy - for label_id based operations)
 */
interface UserLabelRequest {
    /**
     * User ID
     */
    user_id: string;
    /**
     * Label ID
     */
    label_id: string;
}
/**
 * Tag user request - for Zalo API v2.0/oa/tag/tagfollower
 */
interface TagUserRequest {
    /**
     * User ID (long in API spec, but sent as string)
     */
    user_id: string;
    /**
     * Tag name to assign to user
     * Note: If tag doesn't exist, API will create it automatically
     */
    tag_name: string;
}
/**
 * Remove tag from user request - for Zalo API v2.0/oa/tag/rmfollowerfromtag
 */
interface RemoveTagFromUserRequest {
    /**
     * User ID
     */
    user_id: string;
    /**
     * Tag name to remove from user
     */
    tag_name: string;
}
/**
 * Get tags list response - for Zalo API v2.0/oa/tag/gettagsofoa
 */
interface GetTagsResponse {
    /**
     * Error code (0 = success)
     */
    error: number;
    /**
     * Response message
     */
    message: string;
    /**
     * Array of tag names
     */
    data: string[];
}
/**
 * Delete tag request - for Zalo API v2.0/oa/tag/rmtag
 */
interface DeleteTagRequest {
    /**
     * Tag name to delete
     */
    tag_name: string;
}
/**
 * Update user request - for Zalo API v3.0/oa/user/update
 */
interface UpdateUserRequest {
    /**
     * User ID (required)
     */
    user_id: string;
    /**
     * Shared information to update
     */
    shared_info?: {
        /**
         * Display name
         */
        name?: string;
        /**
         * Phone number
         */
        phone?: string;
        /**
         * Address
         */
        address?: string;
        /**
         * City ID (see Zalo documentation for city codes)
         */
        city_id?: number;
        /**
         * District ID (see Zalo documentation for district codes)
         */
        district_id?: number;
        /**
         * Date of birth (dd/MM/yyyy format, from 1/1/1970)
         */
        user_dob?: string;
    };
    /**
     * User alias name
     */
    user_alias?: string;
    /**
     * External user ID in business system (unique per customer)
     */
    user_external_id?: string;
}
/**
 * Delete user info request - for Zalo API v2.0/oa/deletefollowerinfo
 */
interface DeleteUserInfoRequest {
    /**
     * User ID to delete info for
     */
    user_id: string;
}
/**
 * Get user custom info request - for Zalo API v3.0/oa/user/detail/custominfo
 */
interface GetUserCustomInfoRequest {
    /**
     * User ID to get custom info for
     */
    user_id: string;
    /**
     * List of custom fields to export (optional)
     * If not specified, API returns all available custom info
     * For table type fields, only parent field is accepted
     */
    fields_to_export?: string[];
}
/**
 * User custom info response - for Zalo API v3.0/oa/user/detail/custominfo
 */
interface UserCustomInfoResponse {
    /**
     * Error code (0 = success)
     */
    error: number;
    /**
     * Response message
     */
    message: string;
    /**
     * Custom info data
     */
    data: {
        /**
         * User ID
         */
        user_id: string;
        /**
         * Custom information fields
         * All values are returned as strings for consistency
         */
        custom_info: Record<string, any>;
    };
}
/**
 * Update user custom info request - for Zalo API v3.0/oa/user/update/custominfo
 */
interface UpdateUserCustomInfoRequest {
    /**
     * User ID to update custom info for
     */
    user_id: string;
    /**
     * Custom information to update
     * Structure depends on OA's custom field configuration
     */
    custom_info: Record<string, any>;
}
/**
 * User custom information
 */
interface UserCustomInfo {
    /**
     * User ID
     */
    user_id: string;
    /**
     * Custom fields (key-value pairs)
     */
    custom_fields: Record<string, any>;
    /**
     * Last updated timestamp
     */
    last_updated?: number;
}
/**
 * Update custom info request
 */
interface UpdateCustomInfoRequest {
    /**
     * User ID
     */
    user_id: string;
    /**
     * Custom fields to update
     */
    custom_fields: Record<string, any>;
}
/**
 * User info field types
 */
declare enum UserInfoFieldType {
    TEXT = "text",
    NUMBER = "number",
    DATE = "date",
    SELECT = "select"
}
/**
 * Field option for select type
 */
interface FieldOption {
    /**
     * Option value
     */
    value: string;
    /**
     * Option label
     */
    label: string;
}
/**
 * User info field definition
 */
interface UserInfoField {
    /**
     * Field ID
     */
    field_id: string;
    /**
     * Field name
     */
    field_name: string;
    /**
     * Field type
     */
    field_type: UserInfoFieldType;
    /**
     * Field description
     */
    description?: string;
    /**
     * Whether field is required
     */
    required?: boolean;
    /**
     * Options for select type
     */
    options?: FieldOption[];
    /**
     * Default value
     */
    default_value?: string;
    /**
     * Display order
     */
    display_order?: number;
    /**
     * Whether it's a system field
     */
    is_system_field?: boolean;
    /**
     * Creation time
     */
    created_time?: number;
    /**
     * Last update time
     */
    updated_time?: number;
}
/**
 * Create user info field request
 */
interface CreateUserInfoFieldRequest {
    /**
     * Field name
     */
    field_name: string;
    /**
     * Field type
     */
    field_type: UserInfoFieldType;
    /**
     * Field description
     */
    description?: string;
    /**
     * Whether field is required
     */
    required?: boolean;
    /**
     * Options for select type
     */
    options?: FieldOption[];
    /**
     * Default value
     */
    default_value?: string;
    /**
     * Display order
     */
    display_order?: number;
}
/**
 * Update user info field request
 */
interface UpdateUserInfoFieldRequest {
    /**
     * Field name
     */
    field_name?: string;
    /**
     * Field description
     */
    description?: string;
    /**
     * Whether field is required
     */
    required?: boolean;
    /**
     * Options for select type
     */
    options?: FieldOption[];
    /**
     * Default value
     */
    default_value?: string;
    /**
     * Display order
     */
    display_order?: number;
}
/**
 * Advanced filter options for getAllUsersWithAdvancedFilters
 * Dựa trên các trường trong UserInfo
 */
interface AdvancedUserFilters {
    /**
     * Filter by specific user IDs (exact match)
     * Chỉ lấy những users có ID trong danh sách này
     */
    userIds?: string[];
    /**
     * Filter by tag name (existing from basic filters)
     */
    tag_name?: string;
    /**
     * Filter by last interaction period (existing from basic filters)
     */
    last_interaction_period?: "TODAY" | "YESTERDAY" | "L7D" | "L30D" | string;
    /**
     * Filter by follower status (existing from basic filters)
     */
    is_follower?: boolean;
    /**
     * Filter by display name (contains search)
     */
    display_name_contains?: string;
    /**
     * Filter by user alias (contains search)
     */
    user_alias_contains?: string;
    /**
     * Filter by whether user is sensitive (under 18)
     */
    is_sensitive?: boolean;
    /**
     * Filter by specific interaction date range
     */
    last_interaction_date_range?: {
        from: string;
        to: string;
    };
    /**
     * Filter by specific tags (multiple tags - OR operation)
     */
    tag_names?: string[];
    /**
     * Filter by tags (ALL tags must match - AND operation)
     */
    require_all_tags?: string[];
    /**
     * Filter by notes content (contains search)
     */
    notes_contains?: string;
    /**
     * Filter by shared info - city
     */
    city?: string;
    /**
     * Filter by shared info - district
     */
    district?: string;
    /**
     * Filter by shared info - phone (contains search)
     */
    phone_contains?: string;
    /**
     * Filter by shared info - name (contains search)
     */
    name_contains?: string;
    /**
     * Filter by shared info - address (contains search)
     */
    address_contains?: string;
    /**
     * Filter by date of birth range
     */
    dob_range?: {
        from: string;
        to: string;
    };
    /**
     * Filter by age range (calculated from dob)
     */
    age_range?: {
        min: number;
        max: number;
    };
    /**
     * Filter by external user ID (contains search)
     */
    external_id_contains?: string;
    /**
     * Exclude users with specific tags
     */
    exclude_tags?: string[];
    /**
     * Filter by users who have shared info vs those who don't
     */
    has_shared_info?: boolean;
    /**
     * Filter by users who have phone number
     */
    has_phone?: boolean;
    /**
     * Filter by users who have address
     */
    has_address?: boolean;
}

/**
 * ZNS (Zalo Notification Service) Type Definitions
 */
declare enum ZNSTemplateType {
    /** ZNS tùy chỉnh */
    CUSTOM = 1,
    /** ZNS xác thực */
    AUTHENTICATION = 2,
    /** ZNS yêu cầu thanh toán */
    PAYMENT_REQUEST = 3,
    /** ZNS voucher */
    VOUCHER = 4,
    /** ZNS Đánh giá dịch vụ */
    SERVICE_RATING = 5
}
declare enum ZNSTemplateTag {
    /** Transaction */
    TRANSACTION = "1",
    /** Customer care */
    CUSTOMER_CARE = "2",
    /** Promotion */
    PROMOTION = "3"
}
declare enum ZNSButtonType {
    /** Trang doanh nghiệp */
    ENTERPRISE_PAGE = 1,
    /** Gọi điện */
    PHONE = 2,
    /** Trang OA */
    OA_PAGE = 3,
    /** Zalo Mini App */
    ZALO_MINI_APP = 4,
    /** Tải App */
    DOWNLOAD_APP = 5,
    /** Phân phối sản phẩm */
    PRODUCT_DISTRIBUTION = 6,
    /** Web/Zalo Mini App khác */
    OTHER_WEB_APP = 7,
    /** App khác */
    OTHER_APP = 8,
    /** Bài viết OA */
    OA_ARTICLE = 9,
    /** Sao chép */
    COPY = 10,
    /** Thanh toán ngay */
    PAY_NOW = 11,
    /** Xem chi tiết */
    VIEW_DETAILS = 12
}
declare enum ZNSParamType {
    /** Tên khách hàng (30) */
    CUSTOMER_NAME = "1",
    /** Số điện thoại (15) */
    PHONE_NUMBER = "2",
    /** Địa chỉ (200) */
    ADDRESS = "3",
    /** Mã số (30) */
    CODE = "4",
    /** Nhãn tùy chỉnh (30) */
    CUSTOM_LABEL = "5",
    /** Trạng thái giao dịch (30) */
    TRANSACTION_STATUS = "6",
    /** Thông tin liên hệ (50) */
    CONTACT_INFO = "7",
    /** Giới tính / Danh xưng (5) */
    GENDER_TITLE = "8",
    /** Tên sản phẩm / Thương hiệu (200) */
    PRODUCT_BRAND = "9",
    /** Số lượng / Số tiền (20) */
    QUANTITY_AMOUNT = "10",
    /** Thời gian (20) */
    TIME = "11",
    /** OTP (10) */
    OTP = "12",
    /** URL (200) */
    URL = "13",
    /** Tiền tệ (VNĐ) (12) */
    CURRENCY = "14",
    /** Bank transfer note (90) */
    BANK_TRANSFER_NOTE = "15"
}
interface ZNSMessage {
    phone: string;
    template_id: string;
    template_data: Record<string, any>;
    tracking_id?: string;
}
interface ZNSHashPhoneMessage {
    phone_hash: string;
    template_id: string;
    template_data: Record<string, any>;
    tracking_id?: string;
}
interface ZNSDevModeMessage {
    phone: string;
    template_id: string;
    template_data: Record<string, any>;
    tracking_id?: string;
    mode: "development";
}
interface ZNSRsaMessage {
    phone_rsa: string;
    template_id: string;
    template_data: Record<string, any>;
    tracking_id?: string;
}
interface ZNSJourneyMessage {
    phone: string;
    template_id: string;
    template_data: Record<string, any>;
    tracking_id?: string;
    journey_id: string;
}
/**
 * ZBS/ZNS Template Message by UID
 * API: POST https://openapi.zalo.me/v3.0/oa/message/template
 * Send template message using user_id instead of phone number
 */
interface ZNSUidMessage {
    /** ID của người nhận - Định danh người dùng theo OA */
    user_id: string;
    /** ID của template muốn sử dụng */
    template_id: string;
    /** Các thuộc tính của template mà đối tác đã đăng ký với Zalo */
    template_data: Record<string, any>;
}
/**
 * Response from sending ZBS/ZNS template message by UID
 */
interface ZNSUidSendResult {
    error: number;
    message: string;
    data?: {
        /** ID của tin nhắn */
        message_id: string;
        /** ID của người dùng */
        user_id: string;
    };
    /** Thời gian gửi tin (timestamp) */
    sent_time?: string;
}
interface ZNSSendResult {
    error: number;
    message: string;
    data?: {
        msg_id: string;
        sent_time: string;
        quota?: {
            remainingQuota: number;
            dailyQuota: number;
        };
    };
}
interface ZNSTemplate {
    templateId: string;
    templateName: string;
    status: "PENDING_REVIEW" | "ENABLE" | "REJECT" | "DISABLE";
    listParams: Array<{
        name: string;
        require: boolean;
        type: "STRING" | "NUMBER" | "DATE";
        maxLength?: number;
        minLength?: number;
    }>;
    timeout?: number;
    previewUrl?: string;
    templateQuality?: "HIGH" | "MEDIUM" | "LOW";
    templateTag?: string;
    price?: number;
    applyTemplateQuota?: boolean;
    templateDailyQuota?: number;
    templateRemainingQuota?: number;
}
interface ZNSTemplateDetails {
    error: number;
    message: string;
    data: {
        templateId: string;
        templateName: string;
        status: "ENABLE" | "PENDING_REVIEW" | "DELETE" | "REJECT" | "DISABLE";
        reason?: string;
        listParams: Array<{
            name: string;
            require: boolean;
            type: string;
            maxLength: number;
            minLength: number;
            acceptNull: boolean;
        }>;
        listButtons: Array<{
            type: number;
            title: string;
            content: string;
        }>;
        timeout: number;
        previewUrl: string;
        templateQuality: string;
        templateTag: "TRANSACTION" | "CUSTOMER_CARE" | "PROMOTION";
        price_sdt: string;
        price_uid: string;
        price: string;
    };
}
interface ZNSTemplateList {
    error: number;
    message: string;
    data: Array<{
        templateId: string;
        templateName: string;
        createdTime: number;
        status: "PENDING_REVIEW" | "DISABLE" | "ENABLE" | "REJECT";
        templateQuality: "HIGH" | "MEDIUM" | "LOW" | "UNDEFINED";
    }>;
    metadata: {
        total: number;
    };
}
/**
 * ZNS Template Create Request - theo chuẩn Zalo API
 * API: POST https://business.openapi.zalo.me/template/create
 */
interface ZNSCreateTemplateRequest {
    /** Tên mẫu tin (10-60 ký tự) */
    template_name: string;
    /** Loại mẫu tin */
    template_type: 1 | 2 | 3 | 4 | 5;
    /** Tag mẫu tin */
    tag: "1" | "2" | "3";
    /** Layout của template */
    layout: ZNSTemplateLayout;
    /** Thông tin về param (optional) */
    params?: ZNSTemplateParam[];
    /** Ghi chú kiểm duyệt (1-400 ký tự, optional) */
    note?: string;
    /** Mã tracking do đối tác tự định nghĩa */
    tracking_id: string;
}
/**
 * ZNS Template Edit Request - theo chuẩn Zalo API
 * API: POST https://business.openapi.zalo.me/template/edit
 * Chỉ có thể chỉnh sửa template có trạng thái REJECT
 */
interface ZNSUpdateTemplateRequest {
    /** Template ID được hệ thống tự tạo */
    template_id: string;
    /** Tên mẫu tin (10-60 ký tự) */
    template_name: string;
    /** Loại mẫu tin */
    template_type: 1 | 2 | 3 | 4 | 5;
    /** Tag mẫu tin */
    tag: "1" | "2" | "3";
    /** Layout của template */
    layout: ZNSTemplateLayout;
    /** Thông tin về param (optional) */
    params?: ZNSTemplateParam[];
    /** Ghi chú kiểm duyệt (1-400 ký tự, optional) */
    note?: string;
    /** Mã tracking do đối tác tự định nghĩa */
    tracking_id: string;
}
/**
 * Layout của ZNS Template
 */
interface ZNSTemplateLayout {
    /** Header components (optional) */
    header?: {
        components: ZNSValidationComponent[];
    };
    /** Body components (required) */
    body: {
        components: ZNSValidationComponent[];
    };
    /** Footer components (optional) */
    footer?: {
        components: ZNSValidationComponent[];
    };
}
/**
 * Template parameter theo chuẩn Zalo API
 */
interface ZNSTemplateParam {
    /** Loại param */
    type: "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" | "10" | "11" | "12" | "13" | "14" | "15";
    /** Tên của param */
    name: string;
    /** Dữ liệu mẫu của param */
    sample_value: string;
}
/**
 * Response từ API create/edit template
 */
interface ZNSTemplateCreateEditResponse {
    error: number;
    message: string;
    data: {
        /** Template ID */
        template_id: string;
        /** Tên template */
        template_name: string;
        /** Loại template */
        template_type: number;
        /** Trạng thái template sau khi submit */
        status: "PENDING_REVIEW";
        /** Template tag */
        tag: number;
        /** App ID của template */
        app_id: string;
        /** OA ID của template */
        oa_id: string;
        /** Đơn giá gửi tin qua SĐT */
        price_sdt: string;
        /** Đơn giá gửi tin qua UID */
        price_uid: string;
        /** Timeout của template */
        timeout: number;
        /** Link preview template */
        preview_url: string;
    };
}
interface ZNSUploadImageResult {
    error: number;
    message: string;
    data?: {
        media_id: string;
    };
}
interface ZNSStatusInfo {
    error: number;
    message: string;
    data?: {
        status: "PENDING" | "SENT" | "RECEIVED" | "FAILED";
        sent_time?: string;
        received_time?: string;
        fail_reason?: string;
    };
}
interface ZNSMessageStatusInfo {
    error: number;
    message: string;
    data?: {
        delivery_time: string;
        message: string;
        status: number;
    };
}
interface ZNSBatchMessageStatusRequest {
    message_ids: string[];
}
interface ZNSBatchMessageStatusResponse {
    error: number;
    message: string;
    data?: {
        [messageId: string]: {
            delivery_time: string;
            message: string;
            status: number;
        };
    };
}
interface ZNSQuotaInfo {
    error: number;
    message: string;
    data?: {
        dailyQuota: string;
        remainingQuota: string;
        dailyQuotaPromotion: string | null;
        remainingQuotaPromotion: string | null;
        monthlyPromotionQuota: number;
        remainingMonthlyPromotionQuota: number;
        estimatedNextMonthPromotionQuota: number;
    };
}
interface ZNSAllowedContentTypes {
    error: number;
    message: string;
    data?: string[];
}
interface ZNSTemplateSampleData {
    error: number;
    message: string;
    data: Record<string, any>;
}
interface ZNSCustomerRatingResponse {
    error: number;
    data: {
        total: number;
        data: Array<{
            note: string;
            rate: number;
            submitDate: string;
            msgId: string;
            feedbacks: string[];
            trackingId: string;
        }>;
    };
}
interface ZNSOAQualityInfo {
    error: number;
    message: string;
    data: {
        oaCurrentQuality: "HIGH" | "MEDIUM" | "LOW" | "UNDEFINED";
        oa7dayQuality: "HIGH" | "MEDIUM" | "LOW" | "UNDEFINED";
    };
}
interface ZNSQualityHistoryList {
    error: number;
    message: string;
    data?: {
        history: Array<{
            date: string;
            quality: "HIGH" | "MEDIUM" | "LOW";
            qualityScore: number;
            metrics: {
                sent: number;
                delivered: number;
                read: number;
                responded: number;
                complained: number;
            };
        }>;
    };
}
interface ZNSTemplateStatus {
    templateId: string;
    status: "PENDING_REVIEW" | "ENABLE" | "REJECT" | "DISABLE";
    rejectReason?: string;
    enabledTime?: string;
    disabledTime?: string;
}
interface ZNSComponent {
    type: "HEADER" | "BODY" | "FOOTER";
    format?: "TEXT" | "IMAGE" | "VIDEO";
    text?: string;
    url?: string;
    example?: {
        header_text?: string[];
        body_text?: string[][];
        header_handle?: string[];
    };
}
interface ZNSTemplateComponent {
    type: "TITLE" | "PARAGRAPH" | "TABLE" | "LOGO" | "IMAGE" | "BUTTONS";
    content?: string;
    url?: string;
    buttons?: Array<{
        title: string;
        type: "PHONE_NUMBER" | "URL";
        payload: string;
    }>;
    table?: {
        headers: string[];
        rows: string[][];
    };
}
interface ZNSError {
    error: number;
    message: string;
    details?: {
        field?: string;
        code?: string;
        description?: string;
    };
}
interface ZNSAnalytics {
    templateId: string;
    period: {
        from: string;
        to: string;
    };
    metrics: {
        sent: number;
        delivered: number;
        read: number;
        clicked: number;
        failed: number;
        deliveryRate: number;
        readRate: number;
        clickRate: number;
    };
    breakdown?: {
        daily?: Array<{
            date: string;
            sent: number;
            delivered: number;
            read: number;
            clicked: number;
            failed: number;
        }>;
        hourly?: Array<{
            hour: number;
            sent: number;
            delivered: number;
            read: number;
            clicked: number;
            failed: number;
        }>;
    };
}
/** Loại mục đích sử dụng hình ảnh */
type ZNSImageUsageType = "LOGO" | "IMAGE";
/** Kết quả validation chuẩn hóa cho ZNS (không throw error, trả về danh sách lỗi để Backend & AI Agent phân tích) */
interface ZNSValidationResult {
    /** Template hoặc Image có hợp lệ hay không */
    isValid: boolean;
    /** Danh sách các lỗi vi phạm quy chuẩn Zalo ZNS */
    errors: string[];
    /** Danh sách các cảnh báo / khuyến nghị để tăng tỷ lệ duyệt */
    warnings?: string[];
}
/** Attachment object cho LOGO và IMAGES */
interface ZNSAttachment {
    type: "IMAGE";
    media_id: string;
    /** Link CTA khi user click vào ảnh (tùy chọn theo chuẩn Zalo module ảnh) */
    url?: string;
}
/** TITLE component */
interface ZNSTitleComponent {
    value: string;
}
/** PARAGRAPH component */
interface ZNSParagraphComponent {
    value: string;
}
/** OTP component */
interface ZNSOTPComponent {
    value: string;
}
/** TABLE component */
interface ZNSTableComponent {
    rows: ZNSTableRow[];
}
interface ZNSTableRow {
    title: string;
    value: string;
    row_type?: 0 | 1 | 2 | 3 | 4 | 5;
}
/** LOGO component */
interface ZNSLogoComponent {
    light: ZNSAttachment;
    dark: ZNSAttachment;
}
/** IMAGES component */
interface ZNSImagesComponent {
    items: ZNSAttachment[];
}
/** BUTTONS component */
interface ZNSButtonsComponent {
    items: ZNSButtonItem[];
}
interface ZNSButtonItem {
    type: 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12;
    title: string;
    content: string;
}
/** PAYMENT component */
interface ZNSPaymentComponent {
    bank_code: string;
    account_name: string;
    bank_account: string;
    amount: string;
    note?: string;
}
/** VOUCHER component */
interface ZNSVoucherComponent {
    name: string;
    condition: string;
    start_date?: string;
    end_date: string;
    voucher_code: string;
}
/** RATING component */
interface ZNSRatingComponent {
    items: ZNSRatingItem[];
}
interface ZNSRatingItem {
    star: 1 | 2 | 3 | 4 | 5;
    title: string;
    question?: string;
    answers?: string[];
    thanks: string;
    description: string;
}
/** Union type cho tất cả các component */
type ZNSValidationComponent = {
    TITLE: ZNSTitleComponent;
} | {
    PARAGRAPH: ZNSParagraphComponent;
} | {
    OTP: ZNSOTPComponent;
} | {
    TABLE: ZNSTableComponent;
} | {
    LOGO: ZNSLogoComponent;
} | {
    IMAGES: ZNSImagesComponent;
} | {
    BUTTONS: ZNSButtonsComponent;
} | {
    PAYMENT: ZNSPaymentComponent;
} | {
    VOUCHER: ZNSVoucherComponent;
} | {
    RATING: ZNSRatingComponent;
};

/**
 * Types for Zalo Official Account Article Management
 * Based on Zalo Article API v2.0
 */

/**
 * Article cover configuration
 */
interface ArticleCover {
    /** Cover type: photo or video */
    cover_type: 'photo' | 'video';
    /** Photo URL (required when cover_type is 'photo') */
    photo_url?: string;
    /** Video ID (required when cover_type is 'video') */
    video_id?: string;
    /** Cover view orientation for video */
    cover_view?: 'horizontal' | 'vertical' | 'square';
    /** Cover status */
    status: 'show' | 'hide';
}
/**
 * Article body content item
 */
interface ArticleBodyItem {
    /** Content type */
    type: 'text' | 'image' | 'video' | 'product';
    /** Text content (for type: text) */
    content?: string;
    /** Image URL (for type: image) */
    url?: string;
    /** Video ID (for type: video) */
    video_id?: string;
    /** Product ID (for type: product) */
    id?: string;
    /** Additional properties for video */
    width?: number;
    height?: number;
    thumbnail?: string;
}
/**
 * Normal article creation request
 */
interface ArticleNormalRequest {
    type: 'normal';
    title: string;
    author: string;
    cover: ArticleCover;
    description: string;
    body: ArticleBodyItem[];
    related_medias?: string[];
    tracking_link?: string;
    status?: 'show' | 'hide';
    comment?: 'show' | 'hide';
}
/**
 * Video article creation request
 */
interface ArticleVideoRequest {
    type: 'video';
    title: string;
    description: string;
    video_id: string;
    avatar: string;
    status?: 'show' | 'hide';
    comment?: 'show' | 'hide';
}
/**
 * Article creation request (union type)
 */
type ArticleRequest = ArticleNormalRequest | ArticleVideoRequest;
/**
 * Article creation response
 */
type ArticleResponse = ZaloResponse<{
    token: string;
}>;
/**
 * Article process check response
 */
interface ArticleProcessResponse {
    /** Article ID if creation is successful */
    article_id?: string;
    /** Process status */
    status: 'processing' | 'success' | 'failed';
    /** Error message if failed */
    error_message?: string;
    /** Creation timestamp */
    created_time: number;
    /** Last update timestamp */
    updated_time: number;
}
/**
 * Article verification response
 */
interface ArticleVerifyResponse {
    /** Article ID */
    id: string;
}
/**
 * Article cite information
 */
interface ArticleCite {
    url: string;
    description: string;
}
/**
 * Article detail (normal type)
 */
interface ArticleDetailNormal {
    id: string;
    type: 'normal';
    title: string;
    author: string;
    cover: ArticleCover;
    description: string;
    body: ArticleBodyItem[];
    related_medias: string[];
    tracking_link: string;
    status: 'show' | 'hide';
    comment: 'show' | 'hide';
    cite?: ArticleCite;
    link_view: string;
    total_view: number;
    total_share: number;
    total_like: number;
    total_comment: number;
}
/**
 * Article detail (video type)
 */
interface ArticleDetailVideo {
    id: string;
    type: 'video';
    title: string;
    description: string;
    video_id: string;
    avatar: string;
    status: 'show' | 'hide';
    comment: 'show' | 'hide';
    link_view: string;
    total_view: number;
    total_share: number;
    total_like: number;
    total_comment: number;
}
/**
 * Article detail (union type)
 */
type ArticleDetail = ArticleDetailNormal | ArticleDetailVideo;
/**
 * Article list request
 */
interface ArticleListRequest {
    /** Starting position */
    offset: number;
    /** Number of articles to return (max 10) */
    limit: number;
    /** Article type filter */
    type: 'normal' | 'video';
}
/**
 * Article list item - matches Zalo API response structure exactly
 */
interface ArticleListItem {
    /** Article ID */
    id: string;
    /** Article type */
    type: 'normal' | 'video';
    /** Article title */
    title: string;
    /** Article status */
    status: 'show' | 'hide';
    /** Total view count */
    total_view: number;
    /** Total share count */
    total_share: number;
    /** Article creation date (timestamp) */
    create_date: number;
    /** Article update date (timestamp) */
    update_date: number;
    /** Article thumbnail URL */
    thumb: string;
    /** Article view link */
    link_view: string;
}
/**
 * Article list response data - matches Zalo API response structure exactly
 */
interface ArticleListData {
    /** Array of articles (called "medias" in Zalo API) */
    medias: ArticleListItem[];
    /** Total number of articles */
    total: number;
}
/**
 * Article list response
 */
type ArticleListResponse = ZaloApiResponse<ArticleListData>;
/**
 * Normal article update request
 */
interface ArticleUpdateNormalRequest {
    id: string;
    type: 'normal';
    title: string;
    author: string;
    cover: ArticleCover;
    description: string;
    body: ArticleBodyItem[];
    related_medias?: string[];
    tracking_link?: string;
    status?: 'show' | 'hide';
    comment?: 'show' | 'hide';
}
/**
 * Video article update request
 */
interface ArticleUpdateVideoRequest {
    id: string;
    type: 'video';
    title: string;
    description: string;
    video_id: string;
    avatar: string;
    status?: 'show' | 'hide';
    comment?: 'show' | 'hide';
}
/**
 * Article update request (union type)
 */
type ArticleUpdateRequest = ArticleUpdateNormalRequest | ArticleUpdateVideoRequest;
/**
 * Article update response
 */
interface ArticleUpdateResponse {
    /** Token for tracking article update progress */
    token: string;
}
/**
 * Article removal request
 */
interface ArticleRemoveRequest {
    /** Article ID to remove */
    id: string;
}
/**
 * Article removal response
 */
interface ArticleRemoveResponse {
    /** Success message */
    message: string;
}
/**
 * Bulk article removal result for individual item
 */
interface BulkRemoveItemResult {
    /** Article ID that was processed */
    article_id: string;
    /** Whether the removal was successful */
    success: boolean;
    /** Success message if removal succeeded */
    message?: string;
    /** Error message if removal failed */
    error?: string;
    /** Processing time in milliseconds */
    processing_time?: number;
}
/**
 * Progress information for bulk article removal
 */
interface BulkRemoveProgress {
    /** Current number of articles processed */
    current_count: number;
    /** Total number of articles to process */
    total_count: number;
    /** Progress percentage (0-100) */
    percentage: number;
    /** Number of successful removals so far */
    successful_count: number;
    /** Number of failed removals so far */
    failed_count: number;
    /** Whether the operation is complete */
    is_complete: boolean;
    /** Current batch being processed */
    current_batch?: number;
    /** Total number of batches */
    total_batches?: number;
}
/**
 * Options for bulk article removal
 */
interface BulkRemoveOptions {
    /** Number of articles to process in each batch (default: 10) */
    batch_size?: number;
    /** Maximum number of concurrent requests (default: 3) */
    max_concurrency?: number;
    /** Whether to continue processing if some articles fail (default: true) */
    continue_on_error?: boolean;
    /** Delay between batches in milliseconds (default: 100) */
    batch_delay?: number;
    /** Timeout for each removal request in milliseconds (default: 10000) */
    request_timeout?: number;
}
/**
 * Complete result for bulk article removal operation
 */
interface BulkRemoveResult {
    /** Total number of articles processed */
    total_processed: number;
    /** Number of successful removals */
    successful_count: number;
    /** Number of failed removals */
    failed_count: number;
    /** Success rate as percentage (0-100) */
    success_rate: number;
    /** Total processing time in milliseconds */
    total_time: number;
    /** Detailed results for each article */
    results: BulkRemoveItemResult[];
    /** Summary of errors encountered */
    error_summary?: {
        [errorType: string]: number;
    };
}
/**
 * Result for individual article detail fetch in bulk operation
 */
interface BulkDetailItemResult {
    /** Article ID that was processed */
    article_id: string;
    /** Whether the detail fetch was successful */
    success: boolean;
    /** Article detail if fetch succeeded */
    detail?: ArticleDetail;
    /** Error message if fetch failed */
    error?: string;
    /** Processing time in milliseconds */
    processing_time?: number;
}
/**
 * Progress information for bulk article details fetch
 */
interface BulkDetailProgress {
    /** Current number of articles processed */
    current_count: number;
    /** Total number of articles to process */
    total_count: number;
    /** Progress percentage (0-100) */
    percentage: number;
    /** Number of successful detail fetches so far */
    successful_count: number;
    /** Number of failed detail fetches so far */
    failed_count: number;
    /** Whether the operation is complete */
    is_complete: boolean;
    /** Current phase of operation */
    phase: 'fetching_list' | 'fetching_details' | 'complete';
    /** Current batch being processed (for details phase) */
    current_batch?: number;
    /** Total number of batches (for details phase) */
    total_batches?: number;
}
/**
 * Options for bulk article details fetch
 */
interface BulkDetailOptions {
    /** Number of articles to fetch details for in each batch (default: 5) */
    detail_batch_size?: number;
    /** Maximum number of concurrent detail requests (default: 3) */
    max_concurrency?: number;
    /** Whether to continue processing if some details fail (default: true) */
    continue_on_error?: boolean;
    /** Delay between detail batches in milliseconds (default: 200) */
    batch_delay?: number;
    /** Timeout for each detail request in milliseconds (default: 15000) */
    request_timeout?: number;
    /** Maximum number of articles to process (default: unlimited) */
    max_articles?: number;
    /** Options for the initial article list fetch */
    list_options?: {
        batch_size?: number;
        max_articles?: number;
    };
}
/**
 * Complete result for bulk article details fetch operation
 */
interface BulkDetailResult {
    /** Total number of articles processed */
    total_processed: number;
    /** Number of successful detail fetches */
    successful_count: number;
    /** Number of failed detail fetches */
    failed_count: number;
    /** Success rate as percentage (0-100) */
    success_rate: number;
    /** Total processing time in milliseconds */
    total_time: number;
    /** Detailed results for each article */
    results: BulkDetailItemResult[];
    /** Successfully fetched article details */
    articles_with_details: ArticleDetail[];
    /** Summary of errors encountered */
    error_summary?: {
        [errorType: string]: number;
    };
    /** Statistics from the initial list fetch */
    list_fetch_stats: {
        total_pages_fetched: number;
        list_fetch_time: number;
    };
}
/**
 * Video upload response
 */
interface VideoUploadResponse {
    /** Token for tracking video upload progress */
    token: string;
}
/**
 * Video status response
 */
interface VideoStatusResponse {
    /** Video ID (available when processing is complete) */
    video_id?: string;
    /** Video name */
    video_name?: string;
    /** Video size in bytes */
    video_size?: number;
    /** Processing status */
    status: number;
    /** Status message */
    status_message: string;
    /** Conversion progress percentage */
    convert_percent: number;
    /** Conversion error code */
    convert_error_code: number;
}
/**
 * Article validation error
 */
interface ArticleValidationError {
    field: string;
    message: string;
    code: string;
}
/**
 * Video file validation constraints
 */
interface VideoFileConstraints {
    /** Maximum file size in bytes (50MB) */
    maxSize: number;
    /** Allowed file extensions */
    allowedExtensions: string[];
    /** Allowed MIME types */
    allowedMimeTypes: string[];
}
/**
 * Article validation constants
 */
declare const ARTICLE_CONSTRAINTS: {
    readonly TITLE_MAX_LENGTH: 150;
    readonly AUTHOR_MAX_LENGTH: 50;
    readonly DESCRIPTION_MAX_LENGTH: 300;
    readonly IMAGE_MAX_SIZE: number;
    readonly VIDEO_MAX_SIZE: number;
    readonly LIST_MAX_LIMIT: 10;
};
/**
 * Video upload constraints
 */
declare const VIDEO_CONSTRAINTS: VideoFileConstraints;
/**
 * Article status enum
 */
declare enum ArticleStatus {
    SHOW = "show",
    HIDE = "hide"
}
/**
 * Comment status enum
 */
declare enum CommentStatus {
    SHOW = "show",
    HIDE = "hide"
}
/**
 * Article type enum
 */
declare enum ArticleType {
    NORMAL = "normal",
    VIDEO = "video"
}
/**
 * Cover type enum
 */
declare enum CoverType {
    PHOTO = "photo",
    VIDEO = "video"
}
/**
 * Body item type enum
 */
declare enum BodyItemType {
    TEXT = "text",
    IMAGE = "image",
    VIDEO = "video",
    PRODUCT = "product"
}
/**
 * Video upload status enum
 */
declare enum VideoUploadStatus {
    UNKNOWN = 0,// Trạng thái không xác định
    SUCCESS = 1,// Video đã được xử lý thành công và có thể sử dụng
    LOCKED = 2,// Video đã bị khóa
    PROCESSING = 3,// Video đang được xử lý
    FAILED = 4,// Video xử lý thất bại
    DELETED = 5
}

/**
 * Broadcast Message Type Definitions for Zalo OA
 * API: https://openapi.zalo.me/v2.0/oa/message
 */
/**
 * Broadcast recipient target criteria
 */
interface BroadcastTarget {
    /**
     * Danh sách các nhóm tuổi sẽ nhận thông báo. Các giá trị phân tách nhau bởi dấu ","
     * 0: Tuổi từ 0-12
     * 1: Tuổi từ 13-17
     * 2: Tuổi từ 18-24
     * 3: Tuổi từ 25-34
     * 4: Tuổi từ 35-44
     * 5: Tuổi từ 45-54
     * 6: Tuổi từ 55-64
     * 7: Tuổi lớn hơn hay bằng 65
     */
    ages?: string;
    /**
     * Danh sách nhóm giới tính sẽ nhận thông báo. Các giá trị phân tách nhau bởi dấu ","
     * 0: Tất cả các giới tính
     * 1: Nam
     * 2: Nữ
     */
    gender?: string;
    /**
     * Danh sách các địa điểm sẽ nhận thông báo. Các giá trị phân tách nhau bởi dấu ","
     * 0: Miền Bắc Việt Nam
     * 1: Miền Trung Việt Nam
     * 2: Miền Nam Việt Nam
     */
    locations?: string;
    /**
     * Danh sách các tỉnh, thành phố sẽ nhận thông báo. Các giá trị phân tách nhau bởi dấu ","
     * Nếu được thiết lập, thuộc tính này sẽ thay thế thuộc tính "locations"
     */
    cities?: string;
    /**
     * Danh sách các hệ điều hành di động sẽ nhận thông báo. Các giá trị phân tách nhau bởi dấu ","
     * 1: IOS
     * 2: Android
     * 3: Window Phone
     */
    platform?: string;
}
/**
 * Broadcast recipient
 */
interface BroadcastRecipient {
    target: BroadcastTarget;
}
/**
 * Broadcast message attachment payload element
 */
interface BroadcastMessageElement {
    /**
     * Loại media - bắt buộc phải là "article"
     */
    media_type: "article";
    /**
     * ID của bài viết muốn broadcast
     */
    attachment_id: string;
}
/**
 * Broadcast message attachment payload
 */
interface BroadcastMessagePayload {
    /**
     * Loại template - bắt buộc phải là "media"
     */
    template_type: "media";
    /**
     * Chứa các đối tượng của template
     */
    elements: BroadcastMessageElement[];
}
/**
 * Broadcast message attachment
 */
interface BroadcastMessageAttachment {
    /**
     * Loại attachment - bắt buộc phải là "template"
     */
    type: "template";
    /**
     * Chứa payload của attachment muốn gửi
     */
    payload: BroadcastMessagePayload;
}
/**
 * Broadcast message
 */
interface BroadcastMessage {
    attachment: BroadcastMessageAttachment;
}
/**
 * Broadcast request
 */
interface BroadcastRequest {
    /**
     * Thông tin người nhận
     */
    recipient: BroadcastRecipient;
    /**
     * Nội dung và các attachment cần gửi
     */
    message: BroadcastMessage;
}
/**
 * Broadcast response data
 */
interface BroadcastResponseData {
    /**
     * ID của tin broadcast
     */
    message_id: string;
}
/**
 * Broadcast response
 */
interface BroadcastResponse {
    /**
     * Dữ liệu phản hồi
     */
    data: BroadcastResponseData;
    /**
     * Mã lỗi (0 = thành công)
     */
    error: number;
    /**
     * Thông báo
     */
    message: string;
}
/**
 * City codes mapping for broadcast targeting
 */
declare const BROADCAST_CITY_CODES: {
    readonly "\u0110\u1ED3ng Th\u00E1p": "0";
    readonly "B\u00ECnh Ph\u01B0\u1EDBc": "1";
    readonly "Ninh B\u00ECnh": "2";
    readonly "B\u1EA1c Li\u00EAu": "3";
    readonly "H\u1ED3 Ch\u00ED Minh": "4";
    readonly "V\u0129nh Long": "5";
    readonly "L\u00E2m \u0110\u1ED3ng": "6";
    readonly "Y\u00EAn B\u00E1i": "7";
    readonly "H\u00E0 Nam": "8";
    readonly "H\u00E0 N\u1ED9i": "9";
    readonly "H\u1EA3i D\u01B0\u01A1ng": "10";
    readonly "H\u1EADu Giang": "11";
    readonly "An Giang": "12";
    readonly "Tr\u00E0 Vinh": "13";
    readonly "Ti\u1EC1n Giang": "14";
    readonly "T\u00E2y Ninh": "15";
    readonly "\u0110\u1ED3ng Nai": "16";
    readonly "\u0110\u1EAFk L\u1EAFk": "17";
    readonly "B\u00ECnh \u0110\u1ECBnh": "18";
    readonly "Kon Tum": "19";
    readonly "\u0110\u00E0 N\u1EB5ng": "20";
    readonly "B\u1EAFc Giang": "21";
    readonly "B\u1EAFc K\u1EA1n": "22";
    readonly "\u0110i\u1EC7n Bi\u00EAn": "23";
    readonly "H\u00F2a B\u00ECnh": "24";
    readonly "Th\u00E1i B\u00ECnh": "25";
    readonly "V\u0129nh Ph\u00FAc": "26";
    readonly "H\u00E0 Giang": "27";
    readonly "Ki\u00EAn Giang": "28";
    readonly "B\u00ECnh D\u01B0\u01A1ng": "29";
    readonly "B\u00ECnh Thu\u1EADn": "30";
    readonly "\u0110\u1EAFk N\u00F4ng": "31";
    readonly "Kh\u00E1nh H\u00F2a": "32";
    readonly "Gia Lai": "33";
    readonly "Qu\u1EA3ng Nam": "34";
    readonly "Qu\u1EA3ng Tr\u1ECB": "35";
    readonly "H\u00E0 T\u0129nh": "36";
    readonly "H\u01B0ng Y\u00EAn": "37";
    readonly "Qu\u1EA3ng Ninh": "38";
    readonly "Thanh H\u00F3a": "39";
    readonly "Ph\u00FA Th\u1ECD": "40";
    readonly "Lai Ch\u00E2u": "41";
    readonly "Th\u00E1i Nguy\u00EAn": "42";
    readonly "Cao B\u1EB1ng": "43";
    readonly "C\u00E0 Mau": "44";
    readonly "C\u1EA7n Th\u01A1": "45";
    readonly "S\u00F3c Tr\u0103ng": "46";
    readonly "B\u1EBFn Tre": "47";
    readonly "Long An": "48";
    readonly "B\u00E0 R\u1ECBa V\u0169ng T\u00E0u": "49";
    readonly "Ninh Thu\u1EADn": "50";
    readonly "Ph\u00FA Y\u00EAn": "51";
    readonly "Qu\u00E3ng Ng\u00E3i": "52";
    readonly "Th\u1EEBa Thi\u00EAn Hu\u1EBF": "53";
    readonly "Qu\u1EA3ng B\u00ECnh": "54";
    readonly "Ngh\u1EC7 An": "55";
    readonly "Nam \u0110\u1ECBnh": "56";
    readonly "H\u1EA3i Ph\u00F2ng": "57";
    readonly "L\u1EA1ng S\u01A1n": "58";
    readonly "L\u00E0o Cai": "59";
    readonly "S\u01A1n La": "60";
    readonly "B\u1EAFc Ninh": "61";
    readonly "Tuy\u00EAn Quang": "62";
    readonly "Kh\u00F4ng Thu\u1ED9c Vi\u1EC7t Nam": "63";
};
/**
 * Age group codes for broadcast targeting
 */
declare const BROADCAST_AGE_CODES: {
    readonly "0-12": "0";
    readonly "13-17": "1";
    readonly "18-24": "2";
    readonly "25-34": "3";
    readonly "35-44": "4";
    readonly "45-54": "5";
    readonly "55-64": "6";
    readonly "65+": "7";
};
/**
 * Gender codes for broadcast targeting
 */
declare const BROADCAST_GENDER_CODES: {
    readonly ALL: "0";
    readonly MALE: "1";
    readonly FEMALE: "2";
};
/**
 * Location codes for broadcast targeting
 */
declare const BROADCAST_LOCATION_CODES: {
    readonly NORTH: "0";
    readonly CENTRAL: "1";
    readonly SOUTH: "2";
};
/**
 * Platform codes for broadcast targeting
 */
declare const BROADCAST_PLATFORM_CODES: {
    readonly IOS: "1";
    readonly ANDROID: "2";
    readonly WINDOWS_PHONE: "3";
};
/**
 * Result for individual broadcast message
 */
interface BroadcastMessageResult {
    /**
     * Article attachment ID
     */
    attachmentId: string;
    /**
     * Whether the broadcast was successful
     */
    success: boolean;
    /**
     * Message ID if successful
     */
    messageId: string | null;
    /**
     * Error if failed
     */
    error: Error | null;
    /**
     * Timestamp when sent
     */
    sentAt: Date;
}
/**
 * Progress information for multiple broadcast
 */
interface MultipleBroadcastProgress {
    /**
     * Total number of messages to send
     */
    total: number;
    /**
     * Number of messages completed
     */
    completed: number;
    /**
     * Number of successful messages
     */
    successful: number;
    /**
     * Number of failed messages
     */
    failed: number;
    /**
     * Current attachment ID being processed
     */
    currentAttachmentId: string;
    /**
     * Whether all messages are completed
     */
    isCompleted: boolean;
}
/**
 * Result for multiple broadcast messages
 */
interface MultipleBroadcastResult {
    /**
     * Total number of messages sent
     */
    totalMessages: number;
    /**
     * Number of successful messages
     */
    successfulMessages: number;
    /**
     * Number of failed messages
     */
    failedMessages: number;
    /**
     * Individual message results
     */
    results: BroadcastMessageResult[];
    /**
     * Total duration in milliseconds
     */
    totalDuration: number;
    /**
     * Sending mode used
     */
    mode: 'parallel' | 'sequential';
    /**
     * Target criteria used
     */
    targetCriteria: BroadcastTarget;
}

/**
 * Message related types and interfaces
 */

/**
 * Base message interface
 */
interface BaseMessage {
    /**
     * Message type
     */
    type: string;
}
/**
 * Text message
 */
interface TextMessage extends BaseMessage {
    type: "text";
    text: string;
}
/**
 * Image message
 */
interface ImageMessage extends BaseMessage {
    type: "image";
    url?: string;
    attachment_id?: string;
    caption?: string;
    attachment?: {
        type: string;
        payload: {
            url: string;
        };
    };
}
/**
 * File message
 */
interface FileMessage extends BaseMessage {
    type: "file";
    url: string;
    filename: string;
    attachment?: {
        type: string;
        payload: {
            url: string;
        };
    };
}
/**
 * Sticker message
 */
interface StickerMessage extends BaseMessage {
    type: "sticker";
    sticker_id: string;
    attachment?: {
        type: string;
        payload: {
            id: string;
        };
    };
}
/**
 * Template message
 */
interface TemplateMessage$1 extends BaseMessage {
    type: "template";
    template_id: string;
    template_data: Record<string, string>;
}
/**
 * Union type for all message types
 */
type Message = TextMessage | ImageMessage | FileMessage | StickerMessage | TemplateMessage$1;
/**
 * Message recipient
 */
interface MessageRecipient {
    /**
     * User ID
     */
    user_id?: string;
    /**
     * Phone number (for ZNS)
     */
    phone?: string;
}
/**
 * Send message request
 */
interface SendMessageRequest {
    /**
     * Message recipient
     */
    recipient: MessageRecipient;
    /**
     * Message content
     */
    message: Message;
    /**
     * Message type category
     */
    messaging_type?: "consultation" | "transaction" | "promotion" | "response" | "update" | "message_tag";
    /**
     * Tracking ID
     */
    tracking_id?: string;
}
/**
 * Send message response
 */
interface SendMessageResponse {
    /**
     * Message ID
     */
    message_id: string;
    /**
     * Recipient user ID
     */
    user_id: string;
    /**
     * Quota information (optional - only present for free messages)
     */
    quota?: {
        /** Quota type: "reply" | "sub_quota" | "purchase_quota" | "reward_quota" */
        quota_type: "reply" | "sub_quota" | "purchase_quota" | "reward_quota";
        /** Remaining quota (for reply and sub_quota types) */
        remain?: string;
        /** Total quota (for reply and sub_quota types) */
        total?: string;
        /** Expiration date (for sub_quota type) */
        expired_date?: string;
        /** Owner type (for purchase_quota and reward_quota types) */
        owner_type?: "OA" | "App";
        /** Owner ID (for purchase_quota and reward_quota types) */
        owner_id?: string;
    };
}
/**
 * Upload file response
 */
interface UploadFileResponse {
    /**
     * Attachment ID
     */
    attachment_id: string;
    /**
     * File URL
     */
    url: string;
    /**
     * File size
     */
    size?: number;
    /**
     * File type
     */
    type?: string;
}
/**
 * Message status
 */
declare enum MessageStatus {
    SENT = "sent",
    DELIVERED = "delivered",
    READ = "read",
    FAILED = "failed"
}
/**
 * Message event
 */
interface MessageEvent {
    /**
     * Event type
     */
    event: "message" | "delivery" | "read";
    /**
     * Message ID
     */
    message_id: string;
    /**
     * User ID
     */
    user_id: string;
    /**
     * Timestamp
     */
    timestamp: number;
    /**
     * Message content (for incoming messages)
     */
    message?: Message;
}
/**
 * Consultation message types
 */
interface ConsultationTextMessage extends BaseMessage {
    type: "consultation_text";
    text: string;
}
interface ConsultationImageMessage extends BaseMessage {
    type: "consultation_image";
    url: string;
    message?: string;
}
interface ConsultationFileMessage extends BaseMessage {
    type: "consultation_file";
    url: string;
    filename: string;
    message?: string;
}
interface ConsultationStickerMessage extends BaseMessage {
    type: "consultation_sticker";
    sticker_id: string;
    message?: string;
}
interface ConsultationQuoteMessage extends BaseMessage {
    type: "consultation_quote";
    text: string;
    quote: {
        message_id: string;
        content: string;
    };
}
interface ConsultationRequestInfoMessage extends BaseMessage {
    type: "consultation_request_info";
    title: string;
    subtitle?: string;
    image_url?: string;
    elements: Array<{
        title: string;
        type: "text" | "phone" | "email" | "date";
        required?: boolean;
        placeholder?: string;
    }>;
}
/**
 * Transaction message types
 */
interface TransactionMessage extends BaseMessage {
    type: "transaction";
    template_id?: string;
    template_data?: Record<string, string>;
    mode?: "development" | "production";
    attachment?: {
        type: string;
        payload: {
            template_type: string;
            elements: Array<{
                title: string;
                subtitle?: string;
                image_url?: string;
                default_action?: {
                    type: string;
                    url: string;
                };
                buttons?: Array<{
                    type: string;
                    title: string;
                    url?: string;
                    payload?: string;
                }>;
            }>;
        };
    };
}
/**
 * Anonymous message types
 */
interface AnonymousTextMessage extends BaseMessage {
    type: "anonymous_text";
    text: string;
}
interface AnonymousImageMessage extends BaseMessage {
    type: "anonymous_image";
    url: string;
    message?: string;
}
interface AnonymousFileMessage extends BaseMessage {
    type: "anonymous_file";
    url: string;
    filename: string;
    message?: string;
}
interface AnonymousStickerMessage extends BaseMessage {
    type: "anonymous_sticker";
    sticker_id: string;
    message?: string;
}
/**
 * Reaction message
 */
interface ReactionMessage$1 extends BaseMessage {
    type: "reaction";
    message_id: string;
    reaction_type: "heart" | "like" | "haha" | "wow" | "sad" | "angry";
}
/**
 * Mini app message
 */
interface MiniAppMessage extends BaseMessage {
    type: "miniapp";
    app_id: string;
    title: string;
    subtitle?: string;
    image_url?: string;
    data?: Record<string, any>;
}
/**
 * Anonymous message recipient for sending messages to anonymous users
 */
interface AnonymousMessageRecipient {
    /** ID đại diện cho người dùng ẩn danh */
    anonymous_id: string;
    /** ID của cuộc hội thoại */
    conversation_id: string;
}
/**
 * Anonymous message response - matches Zalo API docs exactly
 */
interface AnonymousMessageResponse {
    /** ID của tin nhắn */
    message_id: string;
    /** ID đại diện người dùng ẩn danh */
    anonymous_id: string;
    /** ID của cuộc hội thoại */
    conversation_id: string;
}
/**
 * Message reaction icon types - matches Zalo API docs exactly
 */
type ReactIcon = ":>" | "--b" | ":-((" | "/-strong" | "/-heart" | ":-h" | ":o" | "/-remove";
/**
 * Sender action for message reaction - matches Zalo API docs exactly
 */
interface SenderAction {
    /** Biểu tượng cảm xúc */
    react_icon: ReactIcon;
    /** message_id của tin nhắn cần thả cảm xúc */
    react_message_id: string;
}
/**
 * Message reaction response - matches Zalo API docs exactly
 */
interface MessageReactionResponse {
    /** ID của thông báo */
    message_id: string;
    /** ID của người nhận */
    user_id: string;
}
/**
 * Mini app message request - matches Zalo API docs exactly
 */
interface MiniAppMessageRequest {
    /** Token của template miniapp */
    template_type: string;
    /** Các thuộc tính của mẫu tin */
    template_data: Record<string, any>;
}
/**
 * Mini app message button - matches Zalo API docs exactly
 */
interface MiniAppMessageButton {
    /** Tiêu đề button */
    title: string;
    /** Icon của button */
    image_icon: string;
    /** Link khi click vào button */
    url: string;
}
/**
 * Mini app message response - matches Zalo API docs exactly
 */
interface MiniAppMessageResponse {
    /** ID của thông báo */
    message_id: string;
    /** ID của người nhận */
    user_id: string;
    /** Thông tin quota (nếu có) */
    quota?: {
        /** Loại tin đã gửi (legacy) */
        type?: "reply" | "OA Tier";
        /** Nguồn quota sử dụng */
        quota_type: "reply" | "sub_quota" | "purchase_quota" | "reward_quota";
        /** Số lượt gửi tin còn lại */
        remain?: string;
        /** Tổng số lượng gửi tin */
        total?: string;
        /** Ngày hết hạn (cho sub_quota) */
        expired_date?: string;
        /** Thực thể sở hữu Quota Package (cho purchase_quota, reward_quota) */
        owner_type?: "OA" | "App";
        /** ID thực thể sở hữu Quota Package */
        owner_id?: string;
    };
}
/**
 * Extended message union type
 */
type ExtendedMessage = Message | ConsultationTextMessage | ConsultationImageMessage | ConsultationFileMessage | ConsultationStickerMessage | ConsultationQuoteMessage | ConsultationRequestInfoMessage | TransactionMessage | PromotionMessage | AnonymousTextMessage | AnonymousImageMessage | AnonymousFileMessage | AnonymousStickerMessage | ReactionMessage$1 | MiniAppMessage;

/**
 * Button payload types for different actions
 */
interface OpenUrlPayload {
    url: string;
}
interface OpenSmsPayload {
    content: string;
    phone_code: string;
}
interface OpenPhonePayload {
    phone_code: string;
}
type QueryPayload = string;
interface OpenSmsPayloadInput {
    content: string;
    phone_code?: string;
    phoneCode?: string;
}
interface OpenPhonePayloadInput {
    phone_code?: string;
    phoneCode?: string;
}
/**
 * Promotion message element types for v3.0 API
 */
interface PromotionBannerElement {
    type: "banner";
    attachment_id?: string;
    image_url?: string;
}
interface PromotionHeaderElement {
    type: "header";
    content: string;
    align?: "left" | "center" | "right";
}
interface PromotionTextElement {
    type: "text";
    content: string;
    align?: "left" | "center" | "right";
}
interface PromotionTableElement {
    type: "table";
    content: Array<{
        key: string;
        value: string;
    }>;
}
type PromotionElement = PromotionBannerElement | PromotionHeaderElement | PromotionTextElement | PromotionTableElement;
/**
 * Specific button types with proper payload typing
 */
interface OpenUrlButton {
    title: string;
    image_icon?: string;
    type: "oa.open.url";
    payload: OpenUrlPayload;
}
interface QueryShowButton {
    title: string;
    image_icon?: string;
    type: "oa.query.show";
    payload: QueryPayload;
}
interface QueryHideButton {
    title: string;
    image_icon?: string;
    type: "oa.query.hide";
    payload: QueryPayload;
}
interface OpenSmsButton {
    title: string;
    image_icon?: string;
    type: "oa.open.sms";
    payload: OpenSmsPayload;
}
interface OpenPhoneButton {
    title: string;
    image_icon?: string;
    type: "oa.open.phone";
    payload: OpenPhonePayload;
}
/**
 * Union type for all supported button types
 */
type PromotionButton = OpenUrlButton | QueryShowButton | QueryHideButton | OpenSmsButton | OpenPhoneButton;
/**
 * Input button interfaces with proper typing (discriminated union)
 */
interface PromotionButtonInputOpenUrl {
    title: string;
    imageIcon?: string;
    type: "oa.open.url";
    payload: OpenUrlPayload;
}
interface PromotionButtonInputQueryShow {
    title: string;
    imageIcon?: string;
    type: "oa.query.show";
    payload: QueryPayload;
}
interface PromotionButtonInputQueryHide {
    title: string;
    imageIcon?: string;
    type: "oa.query.hide";
    payload: QueryPayload;
}
interface PromotionButtonInputOpenSms {
    title: string;
    imageIcon?: string;
    type: "oa.open.sms";
    payload: OpenSmsPayloadInput;
}
interface PromotionButtonInputOpenPhone {
    title: string;
    imageIcon?: string;
    type: "oa.open.phone";
    payload: OpenPhonePayloadInput | string;
}
/**
 * Union type for all input button types with proper payload typing
 */
type PromotionButtonInput = PromotionButtonInputOpenUrl | PromotionButtonInputQueryShow | PromotionButtonInputQueryHide | PromotionButtonInputOpenSms | PromotionButtonInputOpenPhone;
/**
 * Promotion message types for v3.0 API
 */
interface PromotionMessage extends BaseMessage {
    type: "promotion";
    attachment: {
        type: "template";
        payload: {
            template_type: "promotion";
            language?: "VI" | "EN";
            elements: PromotionElement[];
            buttons?: PromotionButton[];
        };
    };
}
/**
 * Result for individual user in multiple promotion sending
 */
interface PromotionUserResult {
    userId: string;
    success: boolean;
    result: SendMessageResponse | null;
    error: Error | null;
    timestamp: Date;
}
/**
 * Progress tracking for multiple promotion sending
 */
interface MultiplePromotionProgress {
    total: number;
    completed: number;
    successful: number;
    failed: number;
    currentUserId: string | null;
    startTime: number;
    estimatedTimeRemaining: number | null;
}
/**
 * Final result for multiple promotion sending
 */
interface MultiplePromotionResult {
    total: number;
    successful: number;
    failed: number;
    results: PromotionUserResult[];
    executionTime: number;
    mode: "sequential" | "parallel";
    startTime: Date;
    endTime: Date;
    successRate: number;
}
/**
 * Banner configuration for promotion messages
 * Must provide either image_url OR attachment_id, not both
 */
type BannerConfig = {
    image_url: string;
    attachment_id?: never;
} | {
    attachment_id: string;
    image_url?: never;
};
/**
 * Generic button interface for flexible usage (backward compatibility)
 */
interface GenericPromotionButton {
    title: string;
    imageIcon?: string;
    image_icon?: string;
    type: "oa.open.url" | "oa.query.show" | "oa.query.hide" | "oa.open.sms" | "oa.open.phone";
    payload: OpenUrlPayload | QueryPayload | OpenSmsPayload | OpenPhonePayload;
}

/**
 * Types cho Consultation Service - Zalo OA API
 *
 * Consultation messages (tin nhắn tư vấn) là loại tin nhắn đặc biệt
 * cho phép OA gửi tin nhắn chủ động đến người dùng trong khung thời gian nhất định
 *
 * API Endpoint: https://openapi.zalo.me/v3.0/oa/message/cs
 * Method: POST
 * Content-Type: application/json
 */
/**
 * Interface cho template action (default_action trong elements)
 */
interface TemplateAction {
    /** Loại action */
    type: "oa.open.url" | "oa.query.show" | "oa.query.hide" | "oa.open.sms" | "oa.open.phone";
    /** URL (cho oa.open.url) */
    url?: string;
    /** Payload string (cho oa.query.show, oa.query.hide) hoặc object (cho oa.open.sms, oa.open.phone) */
    payload?: string | {
        content?: string;
        phone_code?: string;
    } | {
        phone_code?: string;
    };
}
/**
 * Interface cho template element
 *
 * Quy tắc elements:
 * - elements là mảng JSON, tối đa 5 phần tử
 * - title: bắt buộc, ≤ 100 ký tự
 * - subtitle: bắt buộc cho element đầu tiên, tùy chọn cho các element sau, ≤ 500 ký tự
 * - image_url: URL ảnh (tùy chọn)
 * - default_action: hành động khi click vào element (tùy chọn)
 */
interface TemplateElement {
    /** Tiêu đề (bắt buộc, ≤ 100 ký tự) */
    title: string;
    /** Tiêu đề phụ (bắt buộc cho element đầu tiên, tùy chọn cho các element sau, ≤ 500 ký tự) */
    subtitle?: string;
    /** URL ảnh (tùy chọn) */
    image_url?: string;
    /** Hành động khi click vào element (tùy chọn) */
    default_action?: TemplateAction;
}
/**
 * Interface cho template button
 *
 * Quy tắc buttons:
 * - buttons là mảng JSON, tối đa 5 phần tử
 * - title: bắt buộc, ≤ 100 ký tự
 * - type: loại action
 * - payload: dữ liệu của action, phụ thuộc vào loại type
 */
interface TemplateButton {
    /** Tiêu đề button (bắt buộc, ≤ 100 ký tự) */
    title: string;
    /** Loại button */
    type: "oa.open.url" | "oa.query.show" | "oa.query.hide" | "oa.open.sms" | "oa.open.phone";
    /** Payload của button, phụ thuộc vào type */
    payload: string | {
        url?: string;
    } | {
        content?: string;
        phone_code?: string;
    } | {
        phone_code?: string;
    };
}
/**
 * Interface cho từng tin nhắn trong chuỗi tin nhắn
 */
interface MessageItem {
    /** Loại tin nhắn */
    type: "text" | "image" | "gif" | "file" | "sticker" | "request_user_info";
    /** Nội dung tin nhắn (cho text message) */
    text?: string;
    /** URL hình ảnh (cho image/gif message) */
    imageUrl?: string;
    /** URL GIF (cho gif message) */
    gifUrl?: string;
    /** Chiều rộng GIF (bắt buộc cho gif) */
    width?: number;
    /** Chiều cao GIF (bắt buộc cho gif) */
    height?: number;
    /** Token file (cho file message) */
    fileToken?: string;
    /** Attachment ID (cho image/sticker message) */
    attachmentId?: string;
    /** Sticker attachment ID (cho sticker message) */
    stickerAttachmentId?: string;
    /** Tiêu đề (cho request_user_info message) */
    title?: string;
    /** Tiêu đề phụ (cho request_user_info message) */
    subtitle?: string;
    /** Delay sau khi gửi tin nhắn này (milliseconds) */
    delay?: number;
}
/**
 * Interface cho request gửi chuỗi tin nhắn
 */
interface SendMessageSequenceRequest {
    /** Access token của Official Account */
    accessToken: string;
    /** ID người nhận */
    userId: string;
    /** Danh sách tin nhắn */
    messages: MessageItem[];
    /** Delay mặc định giữa các tin nhắn (milliseconds) - nếu không có delay riêng */
    defaultDelay?: number;
}
/**
 * Interface cho response gửi chuỗi tin nhắn
 */
interface SendMessageSequenceResponse {
    /** Tổng số tin nhắn đã gửi thành công */
    successCount: number;
    /** Tổng số tin nhắn thất bại */
    failureCount: number;
    /** Chi tiết kết quả từng tin nhắn */
    results: Array<{
        /** Index của tin nhắn trong danh sách */
        index: number;
        /** Loại tin nhắn */
        type: string;
        /** Trạng thái gửi */
        success: boolean;
        /** Thông tin response nếu thành công */
        response?: any;
        /** Thông tin lỗi nếu thất bại */
        error?: string;
        /** Thời gian gửi */
        timestamp: number;
    }>;
    /** Tổng thời gian thực hiện (milliseconds) */
    totalDuration: number;
}
/**
 * Interface cho request gửi chuỗi tin nhắn tới nhiều users
 */
interface SendMessageSequenceToMultipleUsersRequest {
    /** Access token của Official Account */
    accessToken: string;
    /** Danh sách user IDs */
    userIds: string[];
    /** Danh sách tin nhắn */
    messages: MessageItem[];
    /** Delay mặc định giữa các tin nhắn (milliseconds) */
    defaultDelay?: number;
    /** Delay giữa các user (milliseconds) để tránh rate limit */
    delayBetweenUsers?: number;
    /** Callback function để tracking tiến trình */
    onProgress?: (progress: UserProgressInfo) => void;
}
/**
 * Interface cho thông tin tiến trình từng user
 */
interface UserProgressInfo {
    /** User ID hiện tại */
    userId: string;
    /** Index của user trong danh sách (bắt đầu từ 0) */
    userIndex: number;
    /** Tổng số users */
    totalUsers: number;
    /** Trạng thái: 'started' | 'completed' | 'failed' */
    status: 'started' | 'completed' | 'failed';
    /** Kết quả gửi tin nhắn (nếu đã hoàn thành) */
    result?: SendMessageSequenceResponse;
    /** Thông tin lỗi (nếu thất bại) */
    error?: string;
    /** Thời gian bắt đầu */
    startTime: number;
    /** Thời gian kết thúc (nếu đã hoàn thành) */
    endTime?: number;
}
/**
 * Interface cho response gửi chuỗi tin nhắn tới nhiều users
 */
interface SendMessageSequenceToMultipleUsersResponse {
    /** Tổng số users */
    totalUsers: number;
    /** Số users gửi thành công */
    successfulUsers: number;
    /** Số users gửi thất bại */
    failedUsers: number;
    /** Chi tiết kết quả từng user */
    userResults: Array<{
        /** User ID */
        userId: string;
        /** Index của user trong danh sách */
        userIndex: number;
        /** Trạng thái gửi cho user này */
        success: boolean;
        /** Kết quả chi tiết gửi tin nhắn */
        messageSequenceResult?: SendMessageSequenceResponse;
        /** Thông tin lỗi nếu thất bại */
        error?: string;
        /** Thời gian bắt đầu gửi */
        startTime: number;
        /** Thời gian kết thúc */
        endTime: number;
        /** Thời gian thực hiện (milliseconds) */
        duration: number;
    }>;
    /** Tổng thời gian thực hiện (milliseconds) */
    totalDuration: number;
    /** Thống kê tin nhắn tổng cộng */
    messageStats: {
        /** Tổng số tin nhắn đã gửi thành công */
        totalSuccessfulMessages: number;
        /** Tổng số tin nhắn thất bại */
        totalFailedMessages: number;
        /** Tổng số tin nhắn */
        totalMessages: number;
    };
}
/**
 * Các loại template action types
 */
type TemplateActionType = "oa.open.url" | "oa.query.show" | "oa.query.hide" | "oa.open.sms" | "oa.open.phone";
/**
 * Các loại template button types (giống với action types)
 */
type TemplateButtonType = TemplateActionType;
/**
 * Payload cho URL action/button
 */
interface UrlPayload {
    url: string;
}
/**
 * Payload cho SMS action/button
 */
interface SmsPayload {
    phone_code: string;
    content?: string;
}
/**
 * Payload cho Phone action/button
 */
interface PhonePayload {
    phone_code: string;
}
/**
 * Union type cho tất cả các loại payload
 */
type ConsultationTemplatePayload = string | UrlPayload | SmsPayload | PhonePayload;
/**
 * Template types được hỗ trợ
 */
type TemplateType = "request_user_info" | "media" | string;
/**
 * Interface cho tin nhắn tùy chỉnh của từng user
 */
interface UserCustomMessage {
    /** User ID */
    userId: string;
    /** Danh sách tin nhắn riêng cho user này */
    messages: MessageItem[];
}
/**
 * Interface cho request gửi tin nhắn tùy chỉnh tới nhiều users
 */
interface SendCustomMessageSequenceToMultipleUsersRequest {
    /** Access token của Official Account */
    accessToken: string;
    /** Danh sách user và tin nhắn tùy chỉnh */
    userCustomMessages: UserCustomMessage[];
    /** Delay mặc định giữa các tin nhắn (milliseconds) */
    defaultDelay?: number;
    /** Delay giữa các user (milliseconds) để tránh rate limit */
    delayBetweenUsers?: number;
    /** Callback function để tracking tiến trình */
    onProgress?: (progress: UserProgressInfo) => void;
}
/**
 * Interface cho response gửi tin nhắn tùy chỉnh tới nhiều users
 */
interface SendCustomMessageSequenceToMultipleUsersResponse {
    /** Tổng số users */
    totalUsers: number;
    /** Số users gửi thành công */
    successfulUsers: number;
    /** Số users gửi thất bại */
    failedUsers: number;
    /** Chi tiết kết quả từng user */
    userResults: Array<{
        /** User ID */
        userId: string;
        /** Index của user trong danh sách */
        userIndex: number;
        /** Trạng thái gửi cho user này */
        success: boolean;
        /** Kết quả chi tiết gửi tin nhắn */
        messageSequenceResult?: SendMessageSequenceResponse;
        /** Thông tin lỗi nếu thất bại */
        error?: string;
        /** Thời gian bắt đầu gửi */
        startTime: number;
        /** Thời gian kết thúc */
        endTime: number;
        /** Thời gian thực hiện (milliseconds) */
        duration: number;
    }>;
    /** Tổng thời gian thực hiện (milliseconds) */
    totalDuration: number;
    /** Thống kê tin nhắn tổng cộng */
    messageStats: {
        /** Tổng số tin nhắn đã gửi thành công */
        totalSuccessfulMessages: number;
        /** Tổng số tin nhắn thất bại */
        totalFailedMessages: number;
        /** Tổng số tin nhắn */
        totalMessages: number;
    };
}

/**
 * Purchase API types for Zalo Official Account
 *
 * API để tạo đơn hàng mua sản phẩm/dịch vụ OA
 * Yêu cầu: Ứng dụng cần được cấp quyền quản lý "Mua sản phẩm dịch vụ OA"
 */
/**
 * Beneficiary types - Đối tượng thụ hưởng
 */
type BeneficiaryType = "OA" | "APP";
/**
 * Request để tạo đơn hàng với product_id
 */
interface CreateOrderWithProductRequest {
    /**
     * Đối tượng được thụ hưởng khi mua đơn hàng
     * - OA: Official Account
     * - APP: Application
     * Lưu ý: Mỗi sản phẩm sẽ quy định đối tượng khả dụng riêng
     */
    beneficiary: BeneficiaryType;
    /**
     * ID của sản phẩm muốn mua
     * Chỉ sử dụng product_id HOẶC redeem_code, không được dùng cả hai
     */
    product_id: number;
    /**
     * Mã giảm giá (tùy chọn)
     * VD: Giảm 100K, Giảm 10%, ...
     */
    voucher_code?: string;
}
/**
 * Request để tạo đơn hàng với redeem_code
 */
interface CreateOrderWithRedeemRequest {
    /**
     * Đối tượng được thụ hưởng khi mua đơn hàng
     */
    beneficiary: BeneficiaryType;
    /**
     * Mã quà tặng
     * Chỉ sử dụng product_id HOẶC redeem_code, không được dùng cả hai
     */
    redeem_code: string;
    /**
     * Mã giảm giá (tùy chọn)
     */
    voucher_code?: string;
}
/**
 * Union type cho request tạo đơn hàng
 */
type CreateOrderRequest = CreateOrderWithProductRequest | CreateOrderWithRedeemRequest;
/**
 * Response data khi tạo đơn hàng thành công
 */
interface OrderData {
    /**
     * ID đơn hàng
     * Lưu ý: Order chỉ được tạo từ 00:01 đến 23h54
     */
    order_id: string;
    /**
     * Loại đối tượng thụ hưởng
     */
    beneficiary_type: BeneficiaryType;
    /**
     * ID của OA (nếu beneficiary_type = OA) hoặc ID của App (nếu beneficiary_type = APP)
     */
    beneficiary_id: number;
    /**
     * ID sản phẩm
     */
    product_id: number;
    /**
     * Tên sản phẩm
     */
    product_name: string;
    /**
     * Tài khoản ZCA thanh toán đơn hàng
     */
    zca_id: number;
    /**
     * Voucher code đã được sử dụng (nếu có)
     */
    voucher_code?: string;
    /**
     * Mã quà tặng (nếu có)
     */
    redeem_code?: string;
    /**
     * Giá trị của voucher_code
     * VD: Giảm 10% hay Giảm 100K, ...
     */
    discount?: number;
    /**
     * Giá tiền của đơn hàng (chưa áp dụng mã giảm giá)
     */
    amount: number;
    /**
     * Giá tiền chính thức sau khi đã sử dụng mã giảm giá
     */
    final_amount: number;
    /**
     * Thời điểm tạo đơn hàng, tính bằng millisecond
     */
    created_time: number;
    /**
     * OTT - One Time Token
     * Mã xác thực dùng để xác nhận thanh toán đơn hàng
     * Sử dụng tại API xác nhận thanh toán đơn hàng
     * OTT sẽ có hiệu lực trong vòng 5 phút kể từ khi Order được khởi tạo
     */
    verified_token: string;
}
/**
 * Response khi tạo đơn hàng thành công
 */
interface CreateOrderResponse {
    error: 0;
    message: "Success";
    data: OrderData;
}
/**
 * Helper type để kiểm tra request type
 */
declare function isProductOrderRequest(request: CreateOrderRequest): request is CreateOrderWithProductRequest;
/**
 * Helper type để kiểm tra request type
 */
declare function isRedeemOrderRequest(request: CreateOrderRequest): request is CreateOrderWithRedeemRequest;
/**
 * Validation errors cho Purchase API
 */
interface PurchaseValidationError {
    field: string;
    message: string;
}
/**
 * Request để xác nhận thanh toán đơn hàng
 */
interface ConfirmOrderRequest {
    /**
     * ID đơn hàng
     * Lấy từ response của API tạo đơn hàng
     */
    order_id: string;
    /**
     * OTT - One Time Token
     * Mã xác thực dùng để xác nhận thanh toán đơn hàng
     * Lấy từ response của API tạo đơn hàng
     * OTT sẽ có hiệu lực trong vòng 5 phút kể từ khi Order được khởi tạo
     */
    verified_token: string;
}
/**
 * Response data khi xác nhận thanh toán đơn hàng thành công
 * Lưu ý: Response không có verified_token như khi tạo đơn hàng
 */
interface ConfirmedOrderData {
    /**
     * ID đơn hàng
     */
    order_id: string;
    /**
     * Loại đối tượng thụ hưởng
     */
    beneficiary_type: BeneficiaryType;
    /**
     * ID của OA (nếu beneficiary_type = OA) hoặc ID của App (nếu beneficiary_type = APP)
     */
    beneficiary_id: number;
    /**
     * ID sản phẩm
     */
    product_id: number;
    /**
     * Tên sản phẩm
     */
    product_name: string;
    /**
     * Tài khoản ZCA thanh toán đơn hàng
     */
    zca_id: number;
    /**
     * Voucher code đã được sử dụng (nếu có)
     */
    voucher_code?: string;
    /**
     * Mã quà tặng (nếu có)
     */
    redeem_code?: string;
    /**
     * Giá trị của voucher_code
     * VD: Giảm 10% hay Giảm 100K, ...
     */
    discount?: number;
    /**
     * Giá tiền của đơn hàng (chưa áp dụng mã giảm giá)
     */
    amount: number;
    /**
     * Giá tiền chính thức sau khi đã sử dụng mã giảm giá
     */
    final_amount: number;
    /**
     * Thời điểm tạo đơn hàng, tính bằng millisecond
     */
    created_time: number;
}
/**
 * Response khi xác nhận thanh toán đơn hàng thành công
 */
interface ConfirmOrderResponse {
    error: 0;
    message: "Success";
    data: ConfirmedOrderData;
}
/**
 * Danh sách sản phẩm OA có sẵn
 */
declare const OA_PRODUCTS: {
    readonly OA_ADVANCED_6M: 866836109767958100;
    readonly OA_ADVANCED_12M: 1302963828004138500;
    readonly OA_PREMIUM_6M: 757295129578622300;
    readonly OA_PREMIUM_12M: 3071996459978069000;
    readonly GMF_10_MEMBERS: 739448264820568800;
    readonly GMF_50_MEMBERS: 2405469629611791400;
    readonly GMF_100_MEMBERS: 2275350247265190700;
    readonly GMF_1000_MEMBERS: 3678557233392100000;
    readonly TRANSACTION_5K: 4609774892111012000;
    readonly TRANSACTION_50K: 2038298593847789600;
    readonly TRANSACTION_500K: 2544831667685732000;
};
/**
 * Thông tin chi tiết sản phẩm
 */
interface ProductInfo {
    id: number;
    name: string;
    beneficiary: BeneficiaryType[];
    category: 'subscription' | 'gmf' | 'quota';
}
/**
 * Danh sách thông tin chi tiết sản phẩm
 */
declare const PRODUCT_INFO: Record<number, ProductInfo>;
/**
 * Purchase API error codes
 */
declare enum PurchaseErrorCode {
    INVALID_BENEFICIARY = 1001,
    INVALID_PRODUCT_ID = 1002,
    INVALID_REDEEM_CODE = 1003,
    INVALID_VOUCHER_CODE = 1004,
    PRODUCT_NOT_AVAILABLE = 1005,
    INSUFFICIENT_BALANCE = 1006,
    ORDER_TIME_RESTRICTED = 1007,
    DUPLICATE_ORDER = 1008,
    PERMISSION_DENIED = 1009,
    INVALID_ORDER_ID = 1010,
    INVALID_VERIFIED_TOKEN = 1011,
    ORDER_EXPIRED = 1012,
    ORDER_ALREADY_CONFIRMED = 1013,
    SYSTEM_ERROR = 9999
}

interface SocialUserInfo {
    id: string;
    name: string;
    picture?: {
        data: {
            url: string;
        };
    };
    birthday?: string;
    gender?: 'male' | 'female';
    locale?: string;
    email?: string;
    phone?: string;
    verified?: boolean;
}
interface SocialProfile extends SocialUserInfo {
    cover?: {
        source: string;
    };
    about?: string;
    location?: {
        name: string;
        id: string;
    };
    hometown?: {
        name: string;
        id: string;
    };
    education?: Array<{
        school: {
            name: string;
            id: string;
        };
        type: string;
        year?: {
            name: string;
        };
    }>;
    work?: Array<{
        employer: {
            name: string;
            id: string;
        };
        position?: {
            name: string;
        };
        start_date?: string;
        end_date?: string;
    }>;
    relationship_status?: string;
    website?: string;
    timezone?: number;
    updated_time?: string;
}
interface SocialFriend {
    id: string;
    name: string;
    picture?: {
        data: {
            url: string;
        };
    };
    mutual_friends?: {
        data: Array<{
            id: string;
            name: string;
        }>;
        summary: {
            total_count: number;
        };
    };
}
interface SocialFriendsList {
    data: SocialFriend[];
    paging?: {
        cursors?: {
            before: string;
            after: string;
        };
        next?: string;
        previous?: string;
    };
    summary?: {
        total_count: number;
    };
}
interface SocialPost {
    id: string;
    message?: string;
    story?: string;
    link?: string;
    name?: string;
    caption?: string;
    description?: string;
    picture?: string;
    source?: string;
    type: 'status' | 'photo' | 'video' | 'link' | 'offer' | 'music' | 'note';
    created_time: string;
    updated_time?: string;
    privacy?: {
        value: 'EVERYONE' | 'ALL_FRIENDS' | 'FRIENDS_OF_FRIENDS' | 'CUSTOM';
        description?: string;
        friends?: 'ALL_FRIENDS' | 'FRIENDS_OF_FRIENDS' | 'SOME_FRIENDS';
        networks?: string;
        allow?: string;
        deny?: string;
    };
    likes?: {
        data: Array<{
            id: string;
            name: string;
        }>;
        summary: {
            total_count: number;
            can_like: boolean;
            has_liked: boolean;
        };
    };
    comments?: {
        data: Array<{
            id: string;
            from: {
                id: string;
                name: string;
            };
            message: string;
            created_time: string;
            like_count: number;
            user_likes: boolean;
        }>;
        summary: {
            order: 'chronological' | 'reverse_chronological';
            total_count: number;
            can_comment: boolean;
        };
    };
    shares?: {
        count: number;
    };
}
interface SocialFeed {
    data: SocialPost[];
    paging?: {
        previous?: string;
        next?: string;
    };
}
interface SocialMessage {
    id: string;
    from: {
        id: string;
        name: string;
    };
    to: {
        data: Array<{
            id: string;
            name: string;
        }>;
    };
    message?: string;
    created_time: string;
    updated_time?: string;
    attachments?: {
        data: Array<{
            id: string;
            mime_type: string;
            name: string;
            size: number;
            file_url?: string;
            image_data?: {
                url: string;
                width: number;
                height: number;
            };
        }>;
    };
}
interface SocialConversation {
    id: string;
    participants: {
        data: Array<{
            id: string;
            name: string;
        }>;
    };
    updated_time: string;
    message_count: number;
    unread_count: number;
    can_reply: boolean;
    snippet?: string;
}
interface SocialAlbum {
    id: string;
    name: string;
    description?: string;
    cover_photo?: {
        id: string;
        source: string;
    };
    count: number;
    type: 'profile' | 'mobile' | 'wall' | 'normal' | 'album';
    created_time: string;
    updated_time: string;
    privacy?: string;
    can_upload: boolean;
}
interface SocialPhoto {
    id: string;
    album?: {
        id: string;
        name: string;
    };
    name?: string;
    picture: string;
    source: string;
    height: number;
    width: number;
    images: Array<{
        height: number;
        width: number;
        source: string;
    }>;
    created_time: string;
    updated_time?: string;
    tags?: {
        data: Array<{
            id: string;
            name: string;
            x: number;
            y: number;
        }>;
    };
    likes?: {
        data: Array<{
            id: string;
            name: string;
        }>;
        summary: {
            total_count: number;
        };
    };
    comments?: {
        data: Array<{
            id: string;
            from: {
                id: string;
                name: string;
            };
            message: string;
            created_time: string;
        }>;
        summary: {
            total_count: number;
        };
    };
}
interface SocialVideo {
    id: string;
    title?: string;
    description?: string;
    embed_html?: string;
    picture: string;
    source?: string;
    format: Array<{
        embed_html: string;
        filter: string;
        height: number;
        width: number;
        picture: string;
    }>;
    length: number;
    created_time: string;
    updated_time?: string;
    privacy?: {
        value: string;
        description: string;
    };
}
interface SocialEvent {
    id: string;
    name: string;
    description?: string;
    start_time: string;
    end_time?: string;
    timezone?: string;
    location?: string;
    venue?: {
        id: string;
        name: string;
        street: string;
        city: string;
        state: string;
        country: string;
        zip: string;
        latitude: number;
        longitude: number;
    };
    privacy: 'OPEN' | 'CLOSED' | 'SECRET';
    attending_count: number;
    declined_count: number;
    maybe_count: number;
    noreply_count: number;
    owner: {
        id: string;
        name: string;
    };
    updated_time: string;
    cover?: {
        source: string;
        offset_x: number;
        offset_y: number;
    };
    picture?: {
        data: {
            url: string;
        };
    };
}
interface SocialGroup {
    id: string;
    name: string;
    description?: string;
    privacy: 'OPEN' | 'CLOSED' | 'SECRET';
    member_count?: number;
    cover?: {
        source: string;
        offset_x: number;
        offset_y: number;
    };
    picture?: {
        data: {
            url: string;
        };
    };
    owner?: {
        id: string;
        name: string;
    };
    parent?: {
        id: string;
        name: string;
    };
    venue?: {
        id: string;
        name: string;
        street: string;
        city: string;
        state: string;
        country: string;
        zip: string;
        latitude: number;
        longitude: number;
    };
    updated_time: string;
}
interface OAuthConfig {
    app_id: string;
    app_secret: string;
    redirect_uri: string;
    scope: string[];
}
interface AuthorizationRequest {
    app_id: string;
    redirect_uri: string;
    state: string;
    code_challenge: string;
    scope: string;
    response_type: 'code';
}
interface TokenRequest {
    app_id: string;
    app_secret: string;
    code: string;
    code_verifier: string;
    grant_type: 'authorization_code';
}
interface RefreshTokenRequest {
    app_id: string;
    app_secret: string;
    refresh_token: string;
    grant_type: 'refresh_token';
}
interface SocialApiError {
    error: {
        message: string;
        type: string;
        code: number;
        error_subcode?: number;
        fbtrace_id?: string;
    };
}
interface CursorPagination {
    cursors: {
        before: string;
        after: string;
    };
    next?: string;
    previous?: string;
}
interface OffsetPagination {
    offset: number;
    limit: number;
    total?: number;
}

/**
 * ZNS Constants - Các hằng số theo chuẩn Zalo API
 */

/**
 * Template Type mapping với description
 */
declare const ZNS_TEMPLATE_TYPES: {
    readonly 1: {
        readonly value: ZNSTemplateType.CUSTOM;
        readonly name: "ZNS tùy chỉnh";
        readonly description: "Template tùy chỉnh cho các mục đích khác nhau";
    };
    readonly 2: {
        readonly value: ZNSTemplateType.AUTHENTICATION;
        readonly name: "ZNS xác thực";
        readonly description: "Template cho việc xác thực OTP, mã PIN";
    };
    readonly 3: {
        readonly value: ZNSTemplateType.PAYMENT_REQUEST;
        readonly name: "ZNS yêu cầu thanh toán";
        readonly description: "Template yêu cầu thanh toán";
    };
    readonly 4: {
        readonly value: ZNSTemplateType.VOUCHER;
        readonly name: "ZNS voucher";
        readonly description: "Template gửi voucher, khuyến mãi";
    };
    readonly 5: {
        readonly value: ZNSTemplateType.SERVICE_RATING;
        readonly name: "ZNS đánh giá dịch vụ";
        readonly description: "Template yêu cầu đánh giá dịch vụ";
    };
};
/**
 * Template Tag mapping với description
 */
declare const ZNS_TEMPLATE_TAGS: {
    readonly "1": {
        readonly value: ZNSTemplateTag.TRANSACTION;
        readonly name: "Transaction";
        readonly description: "Giao dịch, thanh toán";
    };
    readonly "2": {
        readonly value: ZNSTemplateTag.CUSTOMER_CARE;
        readonly name: "Customer Care";
        readonly description: "Chăm sóc khách hàng";
    };
    readonly "3": {
        readonly value: ZNSTemplateTag.PROMOTION;
        readonly name: "Promotion";
        readonly description: "Khuyến mãi, quảng cáo";
    };
};
/**
 * Button Type mapping với description
 */
declare const ZNS_BUTTON_TYPES: {
    readonly 1: {
        readonly value: ZNSButtonType.ENTERPRISE_PAGE;
        readonly name: "Trang doanh nghiệp";
        readonly description: "Đến trang của doanh nghiệp hoặc trang tra cứu hoá đơn điện tử";
    };
    readonly 2: {
        readonly value: ZNSButtonType.PHONE;
        readonly name: "Phone";
        readonly description: "Gọi điện thoại";
    };
    readonly 3: {
        readonly value: ZNSButtonType.OA_PAGE;
        readonly name: "Trang OA";
        readonly description: "Đến trang thông tin OA";
    };
    readonly 4: {
        readonly value: ZNSButtonType.ZALO_MINI_APP;
        readonly name: "Zalo Mini App";
        readonly description: "Đến ứng dụng Zalo Mini App của doanh nghiệp";
    };
    readonly 5: {
        readonly value: ZNSButtonType.DOWNLOAD_APP;
        readonly name: "Tải App";
        readonly description: "Đến trang tải ứng dụng";
    };
    readonly 6: {
        readonly value: ZNSButtonType.PRODUCT_DISTRIBUTION;
        readonly name: "Phân phối sản phẩm";
        readonly description: "Đến trang phân phối sản phẩm";
    };
    readonly 7: {
        readonly value: ZNSButtonType.OTHER_WEB_APP;
        readonly name: "Web/App khác";
        readonly description: "Đến trang web/Zalo Mini App khác";
    };
    readonly 8: {
        readonly value: ZNSButtonType.OTHER_APP;
        readonly name: "App khác";
        readonly description: "Đến ứng dụng khác";
    };
    readonly 9: {
        readonly value: ZNSButtonType.OA_ARTICLE;
        readonly name: "Bài viết OA";
        readonly description: "Đến bài viết của OA";
    };
    readonly 10: {
        readonly value: ZNSButtonType.COPY;
        readonly name: "Copy";
        readonly description: "Sao chép nội dung";
    };
    readonly 11: {
        readonly value: ZNSButtonType.PAY_NOW;
        readonly name: "Thanh toán ngay";
        readonly description: "Thanh toán ngay";
    };
    readonly 12: {
        readonly value: ZNSButtonType.VIEW_DETAILS;
        readonly name: "Xem chi tiết";
        readonly description: "Xem chi tiết";
    };
};
/**
 * Parameter Type mapping với description và max length
 */
declare const ZNS_PARAM_TYPES: {
    readonly "1": {
        readonly value: ZNSParamType.CUSTOMER_NAME;
        readonly name: "Tên khách hàng";
        readonly maxLength: 30;
    };
    readonly "2": {
        readonly value: ZNSParamType.PHONE_NUMBER;
        readonly name: "Số điện thoại";
        readonly maxLength: 15;
    };
    readonly "3": {
        readonly value: ZNSParamType.ADDRESS;
        readonly name: "Địa chỉ";
        readonly maxLength: 200;
    };
    readonly "4": {
        readonly value: ZNSParamType.CODE;
        readonly name: "Mã số";
        readonly maxLength: 30;
    };
    readonly "5": {
        readonly value: ZNSParamType.CUSTOM_LABEL;
        readonly name: "Nhãn tùy chỉnh";
        readonly maxLength: 30;
    };
    readonly "6": {
        readonly value: ZNSParamType.TRANSACTION_STATUS;
        readonly name: "Trạng thái giao dịch";
        readonly maxLength: 30;
    };
    readonly "7": {
        readonly value: ZNSParamType.CONTACT_INFO;
        readonly name: "Thông tin liên hệ";
        readonly maxLength: 50;
    };
    readonly "8": {
        readonly value: ZNSParamType.GENDER_TITLE;
        readonly name: "Giới tính / Danh xưng";
        readonly maxLength: 5;
    };
    readonly "9": {
        readonly value: ZNSParamType.PRODUCT_BRAND;
        readonly name: "Tên sản phẩm / Thương hiệu";
        readonly maxLength: 200;
    };
    readonly "10": {
        readonly value: ZNSParamType.QUANTITY_AMOUNT;
        readonly name: "Số lượng / Số tiền";
        readonly maxLength: 20;
    };
    readonly "11": {
        readonly value: ZNSParamType.TIME;
        readonly name: "Thời gian";
        readonly maxLength: 20;
    };
    readonly "12": {
        readonly value: ZNSParamType.OTP;
        readonly name: "OTP";
        readonly maxLength: 10;
    };
    readonly "13": {
        readonly value: ZNSParamType.URL;
        readonly name: "URL";
        readonly maxLength: 200;
    };
    readonly "14": {
        readonly value: ZNSParamType.CURRENCY;
        readonly name: "Tiền tệ (VNĐ)";
        readonly maxLength: 12;
    };
    readonly "15": {
        readonly value: ZNSParamType.BANK_TRANSFER_NOTE;
        readonly name: "Bank transfer note";
        readonly maxLength: 90;
    };
};
/**
 * Template Type và Tag compatibility matrix
 */
declare const ZNS_TEMPLATE_TAG_COMPATIBILITY: Record<ZNSTemplateType, ZNSTemplateTag[]>;
/**
 * Validation functions
 */
declare const ZNSValidation: {
    /**
     * Kiểm tra template name hợp lệ
     */
    readonly isValidTemplateName: (name: string) => boolean;
    /**
     * Kiểm tra note hợp lệ
     */
    readonly isValidNote: (note: string) => boolean;
    /**
     * Kiểm tra tag có tương thích với template type không
     */
    readonly isTagCompatibleWithType: (templateType: ZNSTemplateType, tag: ZNSTemplateTag) => boolean;
    /**
     * Kiểm tra param value có hợp lệ với type không
     */
    readonly isValidParamValue: (paramType: ZNSParamType, value: string) => boolean;
};

/**
 * Types for Zalo Webhook Events
 * Based on Zalo Official Account Webhook API
 */
/**
 * Base webhook event structure
 */
interface BaseWebhookEvent {
    /** Application ID */
    app_id: string;
    /** User ID for Social API */
    user_id_by_app: string;
    /** Event name */
    event_name: string;
    /** Event timestamp */
    timestamp: string;
}
/**
 * Webhook sender information
 */
interface WebhookSender {
    /** Sender ID */
    id: string;
}
/**
 * Webhook recipient information
 */
interface WebhookRecipient {
    /** Recipient ID */
    id: string;
}
/**
 * Webhook follower information (for follow events)
 */
interface WebhookFollower {
    /** Follower ID */
    id: string;
}
/**
 * Webhook customer information (for shop events)
 */
interface WebhookCustomer {
    /** Customer ID */
    id: string;
}
/**
 * User submitted information
 */
interface UserSubmittedInfo {
    /** User address */
    address?: string;
    /** User phone number */
    phone?: string;
    /** User city */
    city?: string;
    /** User district */
    district?: string;
    /** User name */
    name?: string;
    /** User ward */
    ward?: string;
}
/**
 * Webhook user information (for click events)
 */
interface WebhookUser {
    /** User ID */
    id?: string;
}
/**
 * Message with reaction
 */
interface ReactionMessage {
    /** Message ID */
    msg_id: string;
    /** Reaction icon */
    react_icon: string;
}
/**
 * Anonymous message with conversation ID
 */
interface AnonymousMessage extends WebhookBaseMessage {
    /** Conversation ID for anonymous chat */
    conversation_id: string;
}
/**
 * Call information for call events
 */
interface CallInfo {
    /** Call ID */
    call_id: string;
    /** Call type (AUDIO, VIDEO) */
    call_type: string;
    /** Call initialization time */
    init_time: string;
    /** Total call duration in milliseconds */
    call_duration: string;
    /** Waiting time before call answered */
    waiting_time: string;
    /** Actual talk time */
    talk_time: string;
    /** Call status code */
    status_code: string;
    /** Phone number */
    phone?: string;
}
/**
 * Template message payload
 */
interface TemplatePayload {
    /** Template checksum */
    checksum: string;
    /** Template link URL */
    link_url: string;
    /** Zalo instant ID */
    zinstant_id: string;
    /** Template text content */
    text: string;
}
/**
 * Template message source
 */
interface TemplateSource {
    /** Service name */
    service: string;
    /** Application ID */
    app_id: string;
}
/**
 * Template message
 */
interface TemplateMessage {
    /** Client message ID */
    client_msg_id: string;
    /** Template payload */
    payload: TemplatePayload;
    /** Message source */
    source: TemplateSource;
    /** Message ID */
    msg_id: string;
    /** Is reply flag */
    is_reply: boolean;
}
/**
 * Business card payload
 */
interface BusinessCardPayload {
    /** Business card thumbnail */
    thumbnail: string;
    /** Business card description (JSON string) */
    description: string;
    /** Business card URL */
    url: string;
}
/**
 * Business card message
 */
interface BusinessCardMessage extends WebhookBaseMessage {
    /** Client message ID */
    client_msg_id: string;
    /** Business card attachments */
    attachments: Array<MessageAttachment & {
        type: "link";
        payload: BusinessCardPayload;
    }>;
}
/**
 * User feedback message
 */
interface FeedbackMessage {
    /** Feedback note */
    note: string;
    /** Rating score */
    rate: number;
    /** Submit time */
    submit_time: string;
    /** Feedback options */
    feedbacks: string[];
    /** Message ID */
    msg_id: string;
    /** Tracking ID */
    tracking_id: string;
}
/**
 * Quota change information
 */
interface QuotaChange {
    /** Previous quota value */
    prev_value: number;
    /** New quota value */
    new_value: number;
}
/**
 * Tag level change information
 */
interface TagLevelChange {
    /** Previous tag level */
    prev_value: number;
    /** New tag level */
    new_value: number;
}
/**
 * ZNS delivery message
 */
interface ZNSDeliveryMessage {
    /** Delivery time */
    delivery_time: string;
    /** Message ID */
    msg_id: string;
    /** Tracking ID */
    tracking_id: string;
}
/**
 * Group information
 */
interface GroupInfo$1 {
    /** Group ID */
    group_id: string;
    /** Official Account ID */
    oa_id: string;
    /** Application ID */
    app_id: string;
    /** User list (for group events) */
    users?: string[];
}
/**
 * Widget interaction data
 */
interface WidgetInteractionData {
    /** User ID by OA */
    user_id_by_oa: string;
    /** User external ID */
    user_external_id: string;
    /** Widget URL */
    url: string;
}
/**
 * Widget sync failure data
 */
interface WidgetSyncFailureData {
    /** Error message */
    message: string;
    /** User ID by OA */
    user_id_by_oa: string;
    /** User external ID */
    user_external_id: string;
}
/**
 * Template status change
 */
interface TemplateStatusChange {
    /** Previous status */
    prev_status: string;
    /** New status */
    new_status: string;
}
/**
 * Permission revoked data
 */
interface PermissionRevokedData {
    /** Action performed by (APP or OA) */
    action_by: string;
    /** Revoked timestamp */
    revoked_at: string;
}
/**
 * Extension subscription info
 */
interface ExtensionSubscriptionInfo {
    /** Valid through date */
    valid_through_date: string;
    /** Duration in months */
    duration_month: string;
    /** Valid start date */
    valid_start_date: string;
}
/**
 * Update user info data
 */
interface UpdateUserInfoData {
    /** Update method */
    method: string;
}
/**
 * Anonymous message with attachments and conversation ID
 */
interface AnonymousMessageWithAttachments extends WebhookMessageWithAttachments {
    /** Conversation ID for anonymous chat */
    conversation_id: string;
}
/**
 * Location coordinates
 */
interface LocationCoordinates {
    /** Latitude */
    latitude: string;
    /** Longitude */
    longitude: string;
}
/**
 * Location attachment payload
 */
interface LocationPayload {
    /** Location coordinates */
    coordinates: LocationCoordinates;
}
/**
 * Image attachment payload
 */
interface ImagePayload {
    /** Thumbnail URL */
    thumbnail: string;
    /** Full image URL */
    url: string;
}
/**
 * Link attachment payload
 */
interface LinkPayload {
    /** Link thumbnail URL */
    thumbnail: string;
    /** Link description */
    description: string;
    /** Link URL */
    url: string;
}
/**
 * Sticker attachment payload
 */
interface StickerPayload {
    /** Sticker ID */
    id: string;
    /** Sticker URL */
    url: string;
}
/**
 * GIF attachment payload
 */
interface GifPayload {
    /** GIF thumbnail URL */
    thumbnail: string;
    /** GIF URL */
    url: string;
}
/**
 * Audio attachment payload
 */
interface AudioPayload {
    /** Audio file URL */
    url: string;
}
/**
 * Video attachment payload
 */
interface VideoPayload {
    /** Video thumbnail URL */
    thumbnail: string;
    /** Video description */
    description: string;
    /** Video file URL */
    url: string;
}
/**
 * File attachment payload
 */
interface FilePayload {
    /** File download URL */
    url: string;
    /** File size in bytes */
    size: string;
    /** File name */
    name: string;
    /** File checksum */
    checksum: string;
    /** File type/MIME type */
    type: string;
}
/**
 * Enhanced link payload for OA list messages
 */
interface EnhancedLinkPayload extends LinkPayload {
    /** Link title */
    title: string;
}
/**
 * Union type for all attachment payloads
 */
type AttachmentPayload = LocationPayload | ImagePayload | LinkPayload | StickerPayload | GifPayload | AudioPayload | VideoPayload | FilePayload | EnhancedLinkPayload;
/**
 * Message attachment
 */
interface MessageAttachment {
    /** Attachment payload */
    payload: AttachmentPayload;
    /** Attachment type */
    type: "location" | "image" | "link" | "sticker" | "gif" | "audio" | "video" | "file";
}
/**
 * Base webhook message structure
 */
interface WebhookBaseMessage {
    /** Message ID */
    msg_id: string;
    /** Quoted message ID when the message is a reply */
    quote_msg_id?: string;
    /** Message text content */
    text?: string;
}
/**
 * Webhook message with attachments
 */
interface WebhookMessageWithAttachments extends WebhookBaseMessage {
    /** Message attachments */
    attachments: MessageAttachment[];
}
/**
 * Message for seen event (contains multiple message IDs)
 */
interface SeenMessage {
    /** Array of message IDs that were seen */
    msg_ids: string[];
}
/**
 * User follows Official Account event
 */
interface UserFollowEvent extends BaseWebhookEvent {
    event_name: "follow";
    /** Official Account ID */
    oa_id: string;
    /** Event source */
    source: string;
    /** Follower information */
    follower: WebhookFollower;
}
/**
 * User unfollows Official Account event
 */
interface UserUnfollowEvent extends BaseWebhookEvent {
    event_name: "unfollow";
    /** Official Account ID */
    oa_id: string;
    /** Event source */
    source: string;
    /** Follower information */
    follower: WebhookFollower;
}
/**
 * Shop has order event
 */
interface ShopHasOrderEvent extends BaseWebhookEvent {
    event_name: "shop_has_order";
    /** Official Account ID */
    oa_id: string;
    /** Customer information */
    customer: WebhookCustomer;
}
/**
 * Add user to tag event
 */
interface AddUserToTagEvent extends BaseWebhookEvent {
    event_name: "add_user_to_tag";
    /** Official Account ID */
    oa_id: string;
}
/**
 * OA sends text message event
 */
interface OASendTextEvent extends BaseWebhookEvent {
    event_name: "oa_send_text";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookBaseMessage;
}
/**
 * OA sends image message event
 */
interface OASendImageEvent extends BaseWebhookEvent {
    event_name: "oa_send_image";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "image";
            payload: ImagePayload;
        }>;
    };
}
/**
 * OA sends GIF message event
 */
interface OASendGifEvent extends BaseWebhookEvent {
    event_name: "oa_send_gif";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "gif";
            payload: GifPayload;
        }>;
    };
}
/**
 * OA sends list message event (interactive message with multiple links)
 */
interface OASendListEvent extends BaseWebhookEvent {
    event_name: "oa_send_list";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "link";
            payload: EnhancedLinkPayload;
        }>;
    };
}
/**
 * OA sends file message event
 */
interface OASendFileEvent extends BaseWebhookEvent {
    event_name: "oa_send_file";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "file";
            payload: FilePayload;
        }>;
    };
}
/**
 * OA sends sticker message event
 */
interface OASendStickerEvent extends BaseWebhookEvent {
    event_name: "oa_send_sticker";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "sticker";
            payload: StickerPayload;
        }>;
    };
}
/**
 * User clicks "Chat Now" button event
 */
interface UserClickChatNowEvent extends BaseWebhookEvent {
    event_name: "user_click_chatnow";
    /** Official Account ID */
    oa_id: string;
    /** User ID (for OA API) */
    user_id: string;
}
/**
 * User reacts to message event
 */
interface UserReactedMessageEvent extends BaseWebhookEvent {
    event_name: "user_reacted_message";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: ReactionMessage;
}
/**
 * OA reacts to message event
 */
interface OAReactedMessageEvent extends BaseWebhookEvent {
    event_name: "oa_reacted_message";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: ReactionMessage;
}
/**
 * OA sends consent request event
 */
interface OASendConsentEvent extends BaseWebhookEvent {
    event_name: "oa_send_consent";
    /** Official Account ID */
    oa_id: string;
    /** Request type */
    request_type: string;
    /** Create time */
    create_time: string;
    /** Expired time */
    expired_time: string;
    /** Phone number */
    phone: string;
}
/**
 * User replies to consent request event
 */
interface UserReplyConsentEvent extends BaseWebhookEvent {
    event_name: "user_reply_consent";
    /** Official Account ID */
    oa_id: string;
    /** Expired time */
    expired_time: string;
    /** Confirmed time */
    confirmed_time: string;
    /** Phone number */
    phone: string;
}
/**
 * Anonymous user sends text message event
 */
interface AnonymousSendTextEvent extends BaseWebhookEvent {
    event_name: "anonymous_send_text";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessage;
}
/**
 * Anonymous user sends image message event
 */
interface AnonymousSendImageEvent extends BaseWebhookEvent {
    event_name: "anonymous_send_image";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "image";
            payload: ImagePayload;
        }>;
    };
}
/**
 * Anonymous user sends file message event
 */
interface AnonymousSendFileEvent extends BaseWebhookEvent {
    event_name: "anonymous_send_file";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "file";
            payload: FilePayload;
        }>;
    };
}
/**
 * Anonymous user sends sticker message event
 */
interface AnonymousSendStickerEvent extends BaseWebhookEvent {
    event_name: "anonymous_send_sticker";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "sticker";
            payload: StickerPayload;
        }>;
    };
}
/**
 * OA sends anonymous text message event
 */
interface OASendAnonymousTextEvent extends BaseWebhookEvent {
    event_name: "oa_send_anonymous_text";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessage;
}
/**
 * OA sends anonymous image message event
 */
interface OASendAnonymousImageEvent extends BaseWebhookEvent {
    event_name: "oa_send_anonymous_image";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "image";
            payload: ImagePayload;
        }>;
    };
}
/**
 * OA sends anonymous file message event
 */
interface OASendAnonymousFileEvent extends BaseWebhookEvent {
    event_name: "oa_send_anonymous_file";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "file";
            payload: FilePayload;
        }>;
    };
}
/**
 * OA sends anonymous sticker message event
 */
interface OASendAnonymousStickerEvent extends BaseWebhookEvent {
    event_name: "oa_send_anonymous_sticker";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: AnonymousMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "sticker";
            payload: StickerPayload;
        }>;
    };
}
/**
 * OA call user event
 */
interface OACallUserEvent extends BaseWebhookEvent {
    event_name: "oa_call_user";
    /** Official Account ID */
    oa_id: string;
    /** Call information */
    call_id: string;
    call_type: string;
    init_time: string;
    call_duration: string;
    waiting_time: string;
    talk_time: string;
    status_code: string;
    phone: string;
}
/**
 * User call OA event
 */
interface UserCallOAEvent extends BaseWebhookEvent {
    event_name: "user_call_oa";
    /** Official Account ID */
    oa_id: string;
    /** User ID (for OA API) */
    user_id: string;
    /** Call information */
    call_id: string;
    call_type: string;
    init_time: string;
    call_duration: string;
    waiting_time: string;
    talk_time: string;
    status_code: string;
}
/**
 * OA sends template message event
 */
interface OASendTemplateEvent extends BaseWebhookEvent {
    event_name: "oa_send_template";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: TemplateMessage;
}
/**
 * User sends business card event
 */
interface UserSendBusinessCardEvent extends BaseWebhookEvent {
    event_name: "user_send_business_card";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: BusinessCardMessage;
}
/**
 * User feedback event
 */
interface UserFeedbackEvent extends BaseWebhookEvent {
    event_name: "user_feedback";
    message: FeedbackMessage;
}
/**
 * Change OA daily quota event
 */
interface ChangeOADailyQuotaEvent extends BaseWebhookEvent {
    event_name: "change_oa_daily_quota";
    /** Official Account ID */
    oa_id: string;
    /** Quota change information */
    quota: QuotaChange;
}
/**
 * Change OA template tags event
 */
interface ChangeOATemplateTagsEvent extends BaseWebhookEvent {
    event_name: "change_oa_template_tags";
    /** Official Account ID */
    oa_id: string;
    /** Tag level change information */
    tag_level: TagLevelChange;
}
/**
 * Change template quality event
 */
interface ChangeTemplateQualityEvent extends BaseWebhookEvent {
    event_name: "change_template_quality";
    /** Official Account ID */
    oa_id: string;
    /** Template ID */
    template_id: string;
    /** Template quality */
    quality: string;
}
/**
 * Change template quota event
 */
interface ChangeTemplateQuotaEvent extends BaseWebhookEvent {
    event_name: "change_template_quota";
    /** Official Account ID */
    oa_id: string;
    /** Template ID */
    template_id: string;
    /** Quota change information */
    quota: QuotaChange;
}
/**
 * Journey timeout event
 */
interface JourneyTimeoutEvent extends BaseWebhookEvent {
    event_name: "event_journey_time_out";
    /** Official Account ID */
    oa_id: string;
    /** Journey ID */
    journey_id: string;
    /** Application ID */
    app_id: string;
}
/**
 * Journey acknowledged event
 */
interface JourneyAcknowledgedEvent extends BaseWebhookEvent {
    event_name: "event_journey_acknowledged";
    /** Official Account ID */
    oa_id: string;
    /** Journey ID */
    journey_id: string;
    /** Message ID */
    msg_id: string;
    /** Application ID */
    app_id: string;
}
/**
 * ZNS user received message event
 */
interface ZNSUserReceivedMessageEvent extends BaseWebhookEvent {
    event_name: "user_received_message";
    /** Application ID */
    app_id: string;
    /** ZNS delivery message */
    message: ZNSDeliveryMessage;
}
/**
 * Create group event
 */
interface CreateGroupEvent extends BaseWebhookEvent {
    event_name: "create_group";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
}
/**
 * User join group event
 */
interface UserJoinGroupEvent extends BaseWebhookEvent {
    event_name: "user_join_group";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
    /** User list */
    users: string[];
}
/**
 * User request join group event
 */
interface UserRequestJoinGroupEvent extends BaseWebhookEvent {
    event_name: "user_request_join_group";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
    /** User list */
    users: string[];
}
/**
 * React request join group event
 */
interface ReactRequestJoinGroupEvent extends BaseWebhookEvent {
    event_name: "react_request_join_group";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
    /** User list */
    users: string[];
}
/**
 * Reject request join group event
 */
interface RejectRequestJoinGroupEvent extends BaseWebhookEvent {
    event_name: "reject_request_join_group";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
    /** User list */
    users: string[];
}
/**
 * Add group admin event
 */
interface AddGroupAdminEvent extends BaseWebhookEvent {
    event_name: "add_group_admin";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
    /** User list */
    users: string[];
}
/**
 * Remove group admin event
 */
interface RemoveGroupAdminEvent extends BaseWebhookEvent {
    event_name: "remove_group_admin";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
    /** User list */
    users: string[];
}
/**
 * Update group info event
 */
interface UpdateGroupInfoEvent extends BaseWebhookEvent {
    event_name: "update_group_info";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
}
/**
 * User out group event
 */
interface UserOutGroupEvent extends BaseWebhookEvent {
    event_name: "user_out_group";
    /** Official Account ID */
    oa_id: string;
    /** Group ID */
    group_id: string;
    /** Application ID */
    app_id: string;
    /** User list */
    users: string[];
}
/**
 * OA send group text event
 */
interface OASendGroupTextEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_text";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookBaseMessage;
}
/**
 * OA send group image event
 */
interface OASendGroupImageEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_image";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "image";
            payload: ImagePayload;
        }>;
    };
}
/**
 * OA send group link event
 */
interface OASendGroupLinkEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_link";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "link";
            payload: LinkPayload;
        }>;
    };
}
/**
 * OA send group audio event
 */
interface OASendGroupAudioEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_audio";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "audio";
            payload: AudioPayload;
        }>;
    };
}
/**
 * OA send group location event
 */
interface OASendGroupLocationEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_location";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "location";
            payload: LocationPayload;
        }>;
    };
}
/**
 * OA send group video event
 */
interface OASendGroupVideoEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_video";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "video";
            payload: VideoPayload;
        }>;
    };
}
/**
 * OA send group business card event
 */
interface OASendGroupBusinessCardEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_business_card";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: BusinessCardMessage;
}
/**
 * OA send group sticker event
 */
interface OASendGroupStickerEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_sticker";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "sticker";
            payload: StickerPayload;
        }>;
    };
}
/**
 * OA send group GIF event
 */
interface OASendGroupGifEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_gif";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "gif";
            payload: GifPayload;
        }>;
    };
}
/**
 * OA send group file event
 */
interface OASendGroupFileEvent extends BaseWebhookEvent {
    event_name: "oa_send_group_file";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "file";
            payload: FilePayload;
        }>;
    };
}
/**
 * User send group text event
 */
interface UserSendGroupTextEvent extends BaseWebhookEvent {
    event_name: "user_send_group_text";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookBaseMessage;
}
/**
 * User send group image event
 */
interface UserSendGroupImageEvent extends BaseWebhookEvent {
    event_name: "user_send_group_image";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "image";
            payload: ImagePayload;
        }>;
    };
}
/**
 * User send group link event
 */
interface UserSendGroupLinkEvent extends BaseWebhookEvent {
    event_name: "user_send_group_link";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "link";
            payload: LinkPayload;
        }>;
    };
}
/**
 * User send group audio event
 */
interface UserSendGroupAudioEvent extends BaseWebhookEvent {
    event_name: "user_send_group_audio";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "audio";
            payload: AudioPayload;
        }>;
    };
}
/**
 * User send group location event
 */
interface UserSendGroupLocationEvent extends BaseWebhookEvent {
    event_name: "user_send_group_location";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "location";
            payload: LocationPayload;
        }>;
    };
}
/**
 * User send group video event
 */
interface UserSendGroupVideoEvent extends BaseWebhookEvent {
    event_name: "user_send_group_video";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "video";
            payload: VideoPayload;
        }>;
    };
}
/**
 * User send group business card event
 */
interface UserSendGroupBusinessCardEvent extends BaseWebhookEvent {
    event_name: "user_send_group_business_card";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: BusinessCardMessage;
}
/**
 * User send group sticker event
 */
interface UserSendGroupStickerEvent extends BaseWebhookEvent {
    event_name: "user_send_group_sticker";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "sticker";
            payload: StickerPayload;
        }>;
    };
}
/**
 * User send group GIF event
 */
interface UserSendGroupGifEvent extends BaseWebhookEvent {
    event_name: "user_send_group_gif";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "gif";
            payload: GifPayload;
        }>;
    };
}
/**
 * User send group file event
 */
interface UserSendGroupFileEvent extends BaseWebhookEvent {
    event_name: "user_send_group_file";
    /** Official Account ID */
    oa_id: string;
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "file";
            payload: FilePayload;
        }>;
    };
}
/**
 * Widget interaction accepted event
 */
interface WidgetInteractionAcceptedEvent extends BaseWebhookEvent {
    event_name: "widget_interaction_accepted";
    /** Official Account ID */
    oa_id: string;
    /** Widget interaction data */
    data: WidgetInteractionData;
}
/**
 * Widget failed to sync user external ID event
 */
interface WidgetFailedToSyncUserExternalIdEvent extends BaseWebhookEvent {
    event_name: "widget_failed_to_sync_user_external_id";
    /** Official Account ID */
    oa_id: string;
    /** Widget sync failure data */
    data: WidgetSyncFailureData;
}
/**
 * Change template status event
 */
interface ChangeTemplateStatusEvent extends BaseWebhookEvent {
    event_name: "change_template_status";
    /** Official Account ID */
    oa_id: string;
    /** Template ID */
    template_id: string;
    /** Status change information */
    status: TemplateStatusChange;
    /** Reason for status change */
    reason: string;
    /** Application ID */
    app_id: string;
}
/**
 * Permission revoked event
 */
interface PermissionRevokedEvent extends BaseWebhookEvent {
    event_name: "permission_revoked";
    /** Official Account ID */
    oa_id: string;
    /** Permission revoked data */
    data: PermissionRevokedData;
    /** Application ID */
    app_id: string;
}
/**
 * Extension purchased event
 */
interface ExtensionPurchasedEvent extends BaseWebhookEvent {
    event_name: "extension_purchased";
    /** Extension ID */
    extension_id: string;
    /** Official Account ID */
    oa_id: string;
    /** Extension subscription info */
    extension_sub_info: ExtensionSubscriptionInfo;
}
/**
 * Update user info event
 */
interface UpdateUserInfoEvent extends BaseWebhookEvent {
    event_name: "update_user_info";
    /** Official Account ID */
    oa_id: string;
    /** User ID (for OA API) */
    user_id: string;
    /** Update user info data */
    data: UpdateUserInfoData;
    /** Application ID */
    app_id: string;
}
/**
 * User sends location event
 */
interface UserSendLocationEvent extends BaseWebhookEvent {
    event_name: "user_send_location";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "location";
            payload: LocationPayload;
        }>;
    };
}
/**
 * User sends image event
 */
interface UserSendImageEvent extends BaseWebhookEvent {
    event_name: "user_send_image";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "image";
            payload: ImagePayload;
        }>;
    };
}
/**
 * User sends link event
 */
interface UserSendLinkEvent extends BaseWebhookEvent {
    event_name: "user_send_link";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "link";
            payload: LinkPayload;
        }>;
    };
}
/**
 * User sends text event
 */
interface UserSendTextEvent extends BaseWebhookEvent {
    event_name: "user_send_text";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookBaseMessage;
}
/**
 * User sends sticker event
 */
interface UserSendStickerEvent extends BaseWebhookEvent {
    event_name: "user_send_sticker";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "sticker";
            payload: StickerPayload;
        }>;
    };
}
/**
 * User sends GIF event
 */
interface UserSendGifEvent extends BaseWebhookEvent {
    event_name: "user_send_gif";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "gif";
            payload: GifPayload;
        }>;
    };
}
/**
 * User sends audio event
 */
interface UserSendAudioEvent extends BaseWebhookEvent {
    event_name: "user_send_audio";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "audio";
            payload: AudioPayload;
        }>;
    };
}
/**
 * User received message event
 */
interface UserReceivedMessageEvent extends BaseWebhookEvent {
    event_name: "user_received_message";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookBaseMessage;
}
/**
 * User seen message event
 */
interface UserSeenMessageEvent extends BaseWebhookEvent {
    event_name: "user_seen_message";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: SeenMessage;
}
/**
 * User sends video event
 */
interface UserSendVideoEvent extends BaseWebhookEvent {
    event_name: "user_send_video";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "video";
            payload: VideoPayload;
        }>;
    };
}
/**
 * User sends file event
 */
interface UserSendFileEvent extends BaseWebhookEvent {
    event_name: "user_send_file";
    sender: WebhookSender;
    recipient: WebhookRecipient;
    message: WebhookMessageWithAttachments & {
        attachments: Array<MessageAttachment & {
            type: "file";
            payload: FilePayload;
        }>;
    };
}
/**
 * User submit info event
 */
interface UserSubmitInfoEvent extends BaseWebhookEvent {
    event_name: "user_submit_info";
    sender: {
        id: string;
    };
    recipient: {
        id: string;
    };
    info: {
        name?: string;
        phone?: string;
        email?: string;
        address?: string;
        city?: string;
        district?: string;
        ward?: string;
        date_of_birth?: string;
    };
}
/**
 * User click button event
 */
interface UserClickButtonEvent extends BaseWebhookEvent {
    event_name: "user_click_chatnow" | "user_click_button";
    sender: {
        id: string;
    };
    recipient: {
        id: string;
    };
    button?: {
        title: string;
        payload: string;
    };
}
/**
 * OA send message event
 */
interface OASendMessageEvent extends BaseWebhookEvent {
    event_name: "oa_send_text" | "oa_send_image" | "oa_send_file" | "oa_send_sticker" | "oa_send_gif" | "oa_send_audio" | "oa_send_video" | "oa_send_list";
    sender: {
        id: string;
    };
    recipient: {
        id: string;
    };
    message: {
        text?: string;
        msg_id: string;
        /** Quoted message ID when the message is a reply */
        quote_msg_id?: string;
        attachments?: Array<{
            type: string;
            payload: {
                url?: string;
                title?: string;
                description?: string;
            };
        }>;
    };
}
/**
 * Anonymous user send message event
 */
interface AnonymousUserSendMessageEvent extends BaseWebhookEvent {
    event_name: "anonymous_send_text" | "anonymous_send_image" | "anonymous_send_sticker" | "anonymous_send_file";
    sender: {
        id: string;
    };
    recipient: {
        id: string;
    };
    message: {
        text?: string;
        msg_id: string;
        /** Quoted message ID when the message is a reply */
        quote_msg_id?: string;
        attachments?: Array<{
            type: string;
            payload: {
                url?: string;
            };
        }>;
    };
}
/**
 * ZNS message status event
 */
interface ZNSMessageStatusEvent extends BaseWebhookEvent {
    event_name: "zns_message_status";
    message_id: string;
    phone: string;
    status: "sent" | "delivered" | "read" | "failed";
    error_code?: number;
    error_message?: string;
    timestamp: string;
}
/**
 * Template status event
 */
interface TemplateStatusEvent extends BaseWebhookEvent {
    event_name: "template_status_update";
    template_id: string;
    template_name: string;
    status: "ENABLE" | "PENDING_REVIEW" | "REJECT" | "DISABLE" | "DELETE";
    reason?: string;
    quality?: "HIGH" | "MEDIUM" | "LOW" | "UNDEFINED";
}
/**
 * Group message events
 */
interface GroupUserJoinEvent extends BaseWebhookEvent {
    event_name: "group_user_join";
    group_id: string;
    user: {
        id: string;
        name: string;
    };
}
interface GroupUserLeaveEvent extends BaseWebhookEvent {
    event_name: "group_user_leave";
    group_id: string;
    user: {
        id: string;
        name: string;
    };
}
interface GroupSendMessageEvent extends BaseWebhookEvent {
    event_name: "group_send_text" | "group_send_image" | "group_send_sticker" | "group_send_file";
    group_id: string;
    sender: {
        id: string;
        name: string;
    };
    message: {
        text?: string;
        msg_id: string;
        /** Quoted message ID when the message is a reply */
        quote_msg_id?: string;
        attachments?: Array<{
            type: string;
            payload: {
                url?: string;
            };
        }>;
    };
}
/**
 * All user message-related webhook events
 */
type UserMessageWebhookEvent = UserSendLocationEvent | UserSendImageEvent | UserSendLinkEvent | UserSendTextEvent | UserSendStickerEvent | UserSendGifEvent | UserSendAudioEvent | UserSendVideoEvent | UserSendFileEvent | UserReceivedMessageEvent | UserSeenMessageEvent;
/**
 * All OA message-related webhook events
 */
type OAMessageWebhookEvent = OASendTextEvent | OASendImageEvent | OASendGifEvent | OASendListEvent | OASendFileEvent | OASendStickerEvent | OASendAnonymousTextEvent | OASendAnonymousImageEvent | OASendAnonymousFileEvent | OASendAnonymousStickerEvent | OASendTemplateEvent;
/**
 * All message-related webhook events
 */
type MessageWebhookEvent = UserMessageWebhookEvent | OAMessageWebhookEvent;
/**
 * All user action webhook events
 */
type UserActionWebhookEvent = UserFollowEvent | UserUnfollowEvent | UserSubmitInfoEvent | UserClickChatNowEvent | UserReactedMessageEvent | UserReplyConsentEvent | UserSendBusinessCardEvent | UserFeedbackEvent;
/**
 * All shop/business webhook events
 */
type ShopWebhookEvent = ShopHasOrderEvent;
/**
 * All call-related webhook events
 */
type CallWebhookEvent = OACallUserEvent | UserCallOAEvent;
/**
 * All ZNS-related webhook events
 */
type ZNSWebhookEvent = ChangeOADailyQuotaEvent | ChangeOATemplateTagsEvent | ChangeTemplateQualityEvent | ChangeTemplateQuotaEvent | JourneyTimeoutEvent | JourneyAcknowledgedEvent | ZNSUserReceivedMessageEvent | ChangeTemplateStatusEvent;
/**
 * All widget-related webhook events
 */
type WidgetWebhookEvent = WidgetInteractionAcceptedEvent | WidgetFailedToSyncUserExternalIdEvent;
/**
 * All group-related webhook events
 */
type GroupWebhookEvent$1 = CreateGroupEvent | UserJoinGroupEvent | UserRequestJoinGroupEvent | ReactRequestJoinGroupEvent | RejectRequestJoinGroupEvent | AddGroupAdminEvent | RemoveGroupAdminEvent | UpdateGroupInfoEvent | UserOutGroupEvent | OASendGroupTextEvent | OASendGroupImageEvent | OASendGroupLinkEvent | OASendGroupAudioEvent | OASendGroupLocationEvent | OASendGroupVideoEvent | OASendGroupBusinessCardEvent | OASendGroupStickerEvent | OASendGroupGifEvent | OASendGroupFileEvent | UserSendGroupTextEvent | UserSendGroupImageEvent | UserSendGroupLinkEvent | UserSendGroupAudioEvent | UserSendGroupLocationEvent | UserSendGroupVideoEvent | UserSendGroupBusinessCardEvent | UserSendGroupStickerEvent | UserSendGroupGifEvent | UserSendGroupFileEvent;
/**
 * All system/admin webhook events
 */
type SystemWebhookEvent = AddUserToTagEvent | OAReactedMessageEvent | OASendConsentEvent | PermissionRevokedEvent | ExtensionPurchasedEvent | UpdateUserInfoEvent;
/**
 * All anonymous user webhook events
 */
type AnonymousWebhookEvent = AnonymousSendTextEvent | AnonymousSendImageEvent | AnonymousSendFileEvent | AnonymousSendStickerEvent;
/**
 * Legacy webhook events (keeping for backward compatibility)
 */
type LegacyWebhookEvent = UserSubmitInfoEvent | UserClickButtonEvent | OASendMessageEvent | AnonymousUserSendMessageEvent | ZNSMessageStatusEvent | TemplateStatusEvent | GroupUserJoinEvent | GroupUserLeaveEvent | GroupSendMessageEvent;
/**
 * All webhook events union type
 */
type WebhookEvent = MessageWebhookEvent | UserActionWebhookEvent | ShopWebhookEvent | SystemWebhookEvent | AnonymousWebhookEvent | CallWebhookEvent | ZNSWebhookEvent | GroupWebhookEvent$1 | WidgetWebhookEvent | LegacyWebhookEvent;
/**
 * Webhook payload wrapper
 */
interface WebhookPayload {
    /**
     * Event data
     */
    data: WebhookEvent;
    /**
     * Event signature for verification
     */
    signature?: string;
}
/**
 * Webhook verification result
 */
interface WebhookVerification {
    /**
     * Whether the webhook is valid
     */
    valid: boolean;
    /**
     * Error message if invalid
     */
    error?: string;
}
/**
 * Webhook handler function type
 */
type WebhookHandler<T extends WebhookEvent = WebhookEvent> = (event: T) => Promise<void> | void;
/**
 * Webhook event handlers map
 */
interface WebhookHandlers {
    user_send_text?: WebhookHandler<UserSendTextEvent>;
    user_send_image?: WebhookHandler<UserSendImageEvent>;
    user_send_location?: WebhookHandler<UserSendLocationEvent>;
    user_send_link?: WebhookHandler<UserSendLinkEvent>;
    user_send_sticker?: WebhookHandler<UserSendStickerEvent>;
    user_send_gif?: WebhookHandler<UserSendGifEvent>;
    user_send_audio?: WebhookHandler<UserSendAudioEvent>;
    user_send_video?: WebhookHandler<UserSendVideoEvent>;
    user_send_file?: WebhookHandler<UserSendFileEvent>;
    user_received_message?: WebhookHandler<UserReceivedMessageEvent>;
    user_seen_message?: WebhookHandler<UserSeenMessageEvent>;
    follow?: WebhookHandler<UserFollowEvent>;
    unfollow?: WebhookHandler<UserUnfollowEvent>;
    user_submit_info?: WebhookHandler<UserSubmitInfoEvent>;
    oa_send_text?: WebhookHandler<OASendTextEvent>;
    oa_send_image?: WebhookHandler<OASendImageEvent>;
    oa_send_gif?: WebhookHandler<OASendGifEvent>;
    oa_send_list?: WebhookHandler<OASendListEvent>;
    oa_send_file?: WebhookHandler<OASendFileEvent>;
    oa_send_sticker?: WebhookHandler<OASendStickerEvent>;
    user_click_chatnow?: WebhookHandler<UserClickChatNowEvent>;
    user_reacted_message?: WebhookHandler<UserReactedMessageEvent>;
    user_reply_consent?: WebhookHandler<UserReplyConsentEvent>;
    oa_reacted_message?: WebhookHandler<OAReactedMessageEvent>;
    oa_send_consent?: WebhookHandler<OASendConsentEvent>;
    anonymous_send_text?: WebhookHandler<AnonymousSendTextEvent>;
    anonymous_send_image?: WebhookHandler<AnonymousSendImageEvent>;
    anonymous_send_file?: WebhookHandler<AnonymousSendFileEvent>;
    anonymous_send_sticker?: WebhookHandler<AnonymousSendStickerEvent>;
    oa_send_anonymous_text?: WebhookHandler<OASendAnonymousTextEvent>;
    oa_send_anonymous_image?: WebhookHandler<OASendAnonymousImageEvent>;
    oa_send_anonymous_file?: WebhookHandler<OASendAnonymousFileEvent>;
    oa_send_anonymous_sticker?: WebhookHandler<OASendAnonymousStickerEvent>;
    oa_call_user?: WebhookHandler<OACallUserEvent>;
    user_call_oa?: WebhookHandler<UserCallOAEvent>;
    oa_send_template?: WebhookHandler<OASendTemplateEvent>;
    user_send_business_card?: WebhookHandler<UserSendBusinessCardEvent>;
    user_feedback?: WebhookHandler<UserFeedbackEvent>;
    change_oa_daily_quota?: WebhookHandler<ChangeOADailyQuotaEvent>;
    change_oa_template_tags?: WebhookHandler<ChangeOATemplateTagsEvent>;
    change_template_quality?: WebhookHandler<ChangeTemplateQualityEvent>;
    change_template_quota?: WebhookHandler<ChangeTemplateQuotaEvent>;
    change_template_status?: WebhookHandler<ChangeTemplateStatusEvent>;
    event_journey_time_out?: WebhookHandler<JourneyTimeoutEvent>;
    event_journey_acknowledged?: WebhookHandler<JourneyAcknowledgedEvent>;
    zns_user_received_message?: WebhookHandler<ZNSUserReceivedMessageEvent>;
    create_group?: WebhookHandler<CreateGroupEvent>;
    user_join_group?: WebhookHandler<UserJoinGroupEvent>;
    user_request_join_group?: WebhookHandler<UserRequestJoinGroupEvent>;
    react_request_join_group?: WebhookHandler<ReactRequestJoinGroupEvent>;
    reject_request_join_group?: WebhookHandler<RejectRequestJoinGroupEvent>;
    add_group_admin?: WebhookHandler<AddGroupAdminEvent>;
    remove_group_admin?: WebhookHandler<RemoveGroupAdminEvent>;
    update_group_info?: WebhookHandler<UpdateGroupInfoEvent>;
    user_out_group?: WebhookHandler<UserOutGroupEvent>;
    oa_send_group_text?: WebhookHandler<OASendGroupTextEvent>;
    oa_send_group_image?: WebhookHandler<OASendGroupImageEvent>;
    oa_send_group_link?: WebhookHandler<OASendGroupLinkEvent>;
    oa_send_group_audio?: WebhookHandler<OASendGroupAudioEvent>;
    oa_send_group_location?: WebhookHandler<OASendGroupLocationEvent>;
    oa_send_group_video?: WebhookHandler<OASendGroupVideoEvent>;
    oa_send_group_business_card?: WebhookHandler<OASendGroupBusinessCardEvent>;
    oa_send_group_sticker?: WebhookHandler<OASendGroupStickerEvent>;
    oa_send_group_gif?: WebhookHandler<OASendGroupGifEvent>;
    oa_send_group_file?: WebhookHandler<OASendGroupFileEvent>;
    user_send_group_text?: WebhookHandler<UserSendGroupTextEvent>;
    user_send_group_image?: WebhookHandler<UserSendGroupImageEvent>;
    user_send_group_link?: WebhookHandler<UserSendGroupLinkEvent>;
    user_send_group_audio?: WebhookHandler<UserSendGroupAudioEvent>;
    user_send_group_location?: WebhookHandler<UserSendGroupLocationEvent>;
    user_send_group_video?: WebhookHandler<UserSendGroupVideoEvent>;
    user_send_group_business_card?: WebhookHandler<UserSendGroupBusinessCardEvent>;
    user_send_group_sticker?: WebhookHandler<UserSendGroupStickerEvent>;
    user_send_group_gif?: WebhookHandler<UserSendGroupGifEvent>;
    user_send_group_file?: WebhookHandler<UserSendGroupFileEvent>;
    widget_interaction_accepted?: WebhookHandler<WidgetInteractionAcceptedEvent>;
    widget_failed_to_sync_user_external_id?: WebhookHandler<WidgetFailedToSyncUserExternalIdEvent>;
    permission_revoked?: WebhookHandler<PermissionRevokedEvent>;
    extension_purchased?: WebhookHandler<ExtensionPurchasedEvent>;
    update_user_info?: WebhookHandler<UpdateUserInfoEvent>;
    shop_has_order?: WebhookHandler<ShopHasOrderEvent>;
    add_user_to_tag?: WebhookHandler<AddUserToTagEvent>;
    user_click_button?: WebhookHandler<UserClickButtonEvent>;
    oa_send_audio?: WebhookHandler<OASendMessageEvent>;
    oa_send_video?: WebhookHandler<OASendMessageEvent>;
    zns_message_status?: WebhookHandler<ZNSMessageStatusEvent>;
    template_status_update?: WebhookHandler<TemplateStatusEvent>;
    group_user_join?: WebhookHandler<GroupUserJoinEvent>;
    group_user_leave?: WebhookHandler<GroupUserLeaveEvent>;
    group_send_text?: WebhookHandler<GroupSendMessageEvent>;
    group_send_image?: WebhookHandler<GroupSendMessageEvent>;
    group_send_sticker?: WebhookHandler<GroupSendMessageEvent>;
    group_send_file?: WebhookHandler<GroupSendMessageEvent>;
    "*"?: WebhookHandler<WebhookEvent>;
}
/**
 * Webhook event names enum
 */
declare enum WebhookEventName {
    USER_SEND_LOCATION = "user_send_location",
    USER_SEND_IMAGE = "user_send_image",
    USER_SEND_LINK = "user_send_link",
    USER_SEND_TEXT = "user_send_text",
    USER_SEND_STICKER = "user_send_sticker",
    USER_SEND_GIF = "user_send_gif",
    USER_SEND_AUDIO = "user_send_audio",
    USER_SEND_VIDEO = "user_send_video",
    USER_SEND_FILE = "user_send_file",
    USER_RECEIVED_MESSAGE = "user_received_message",
    USER_SEEN_MESSAGE = "user_seen_message",
    FOLLOW = "follow",
    UNFOLLOW = "unfollow",
    USER_SUBMIT_INFO = "user_submit_info",
    OA_SEND_TEXT = "oa_send_text",
    OA_SEND_IMAGE = "oa_send_image",
    OA_SEND_GIF = "oa_send_gif",
    OA_SEND_LIST = "oa_send_list",
    OA_SEND_FILE = "oa_send_file",
    OA_SEND_STICKER = "oa_send_sticker",
    USER_CLICK_CHATNOW = "user_click_chatnow",
    USER_REACTED_MESSAGE = "user_reacted_message",
    USER_REPLY_CONSENT = "user_reply_consent",
    OA_REACTED_MESSAGE = "oa_reacted_message",
    OA_SEND_CONSENT = "oa_send_consent",
    ANONYMOUS_SEND_TEXT = "anonymous_send_text",
    ANONYMOUS_SEND_IMAGE = "anonymous_send_image",
    ANONYMOUS_SEND_FILE = "anonymous_send_file",
    ANONYMOUS_SEND_STICKER = "anonymous_send_sticker",
    OA_SEND_ANONYMOUS_TEXT = "oa_send_anonymous_text",
    OA_SEND_ANONYMOUS_IMAGE = "oa_send_anonymous_image",
    OA_SEND_ANONYMOUS_FILE = "oa_send_anonymous_file",
    OA_SEND_ANONYMOUS_STICKER = "oa_send_anonymous_sticker",
    OA_CALL_USER = "oa_call_user",
    USER_CALL_OA = "user_call_oa",
    OA_SEND_TEMPLATE = "oa_send_template",
    USER_SEND_BUSINESS_CARD = "user_send_business_card",
    USER_FEEDBACK = "user_feedback",
    CHANGE_OA_DAILY_QUOTA = "change_oa_daily_quota",
    CHANGE_OA_TEMPLATE_TAGS = "change_oa_template_tags",
    CHANGE_TEMPLATE_QUALITY = "change_template_quality",
    CHANGE_TEMPLATE_QUOTA = "change_template_quota",
    CHANGE_TEMPLATE_STATUS = "change_template_status",
    EVENT_JOURNEY_TIME_OUT = "event_journey_time_out",
    EVENT_JOURNEY_ACKNOWLEDGED = "event_journey_acknowledged",
    ZNS_USER_RECEIVED_MESSAGE = "user_received_message",
    CREATE_GROUP = "create_group",
    USER_JOIN_GROUP = "user_join_group",
    USER_REQUEST_JOIN_GROUP = "user_request_join_group",
    REACT_REQUEST_JOIN_GROUP = "react_request_join_group",
    REJECT_REQUEST_JOIN_GROUP = "reject_request_join_group",
    ADD_GROUP_ADMIN = "add_group_admin",
    REMOVE_GROUP_ADMIN = "remove_group_admin",
    UPDATE_GROUP_INFO = "update_group_info",
    USER_OUT_GROUP = "user_out_group",
    OA_SEND_GROUP_TEXT = "oa_send_group_text",
    OA_SEND_GROUP_IMAGE = "oa_send_group_image",
    OA_SEND_GROUP_LINK = "oa_send_group_link",
    OA_SEND_GROUP_AUDIO = "oa_send_group_audio",
    OA_SEND_GROUP_LOCATION = "oa_send_group_location",
    OA_SEND_GROUP_VIDEO = "oa_send_group_video",
    OA_SEND_GROUP_BUSINESS_CARD = "oa_send_group_business_card",
    OA_SEND_GROUP_STICKER = "oa_send_group_sticker",
    OA_SEND_GROUP_GIF = "oa_send_group_gif",
    OA_SEND_GROUP_FILE = "oa_send_group_file",
    USER_SEND_GROUP_TEXT = "user_send_group_text",
    USER_SEND_GROUP_IMAGE = "user_send_group_image",
    USER_SEND_GROUP_LINK = "user_send_group_link",
    USER_SEND_GROUP_AUDIO = "user_send_group_audio",
    USER_SEND_GROUP_LOCATION = "user_send_group_location",
    USER_SEND_GROUP_VIDEO = "user_send_group_video",
    USER_SEND_GROUP_BUSINESS_CARD = "user_send_group_business_card",
    USER_SEND_GROUP_STICKER = "user_send_group_sticker",
    USER_SEND_GROUP_GIF = "user_send_group_gif",
    USER_SEND_GROUP_FILE = "user_send_group_file",
    WIDGET_INTERACTION_ACCEPTED = "widget_interaction_accepted",
    WIDGET_FAILED_TO_SYNC_USER_EXTERNAL_ID = "widget_failed_to_sync_user_external_id",
    PERMISSION_REVOKED = "permission_revoked",
    EXTENSION_PURCHASED = "extension_purchased",
    UPDATE_USER_INFO = "update_user_info",
    SHOP_HAS_ORDER = "shop_has_order",
    ADD_USER_TO_TAG = "add_user_to_tag"
}
/**
 * Attachment types enum
 */
declare enum AttachmentType {
    LOCATION = "location",
    IMAGE = "image",
    LINK = "link",
    STICKER = "sticker",
    GIF = "gif",
    AUDIO = "audio",
    VIDEO = "video",
    FILE = "file"
}
/**
 * Type guard to check if event is a message event
 */
declare function isMessageEvent(event: WebhookEvent): event is MessageWebhookEvent;
/**
 * Type guard to check if event is a follow/unfollow event
 */
declare function isFollowEvent(event: WebhookEvent): event is UserFollowEvent | UserUnfollowEvent;
/**
 * Type guard to check if event is a user action event
 */
declare function isUserActionEvent(event: WebhookEvent): event is UserActionWebhookEvent;
/**
 * Type guard to check if event is a shop event
 */
declare function isShopEvent(event: WebhookEvent): event is ShopWebhookEvent;
/**
 * Type guard to check if event is an OA message event
 */
declare function isOAMessageEvent(event: WebhookEvent): event is OAMessageWebhookEvent;
/**
 * Type guard to check if event is an anonymous user event
 */
declare function isAnonymousEvent(event: WebhookEvent): event is AnonymousWebhookEvent;
/**
 * Type guard to check if event is a call event
 */
declare function isCallEvent(event: WebhookEvent): event is CallWebhookEvent;
/**
 * Type guard to check if event is a template event
 */
declare function isTemplateEvent(event: WebhookEvent): event is OASendTemplateEvent;
/**
 * Type guard to check if event is a business card event
 */
declare function isBusinessCardEvent(event: WebhookEvent): event is UserSendBusinessCardEvent;
/**
 * Type guard to check if event is a feedback event
 */
declare function isFeedbackEvent(event: WebhookEvent): event is UserFeedbackEvent;
/**
 * Type guard to check if event is a ZNS event
 */
declare function isZNSEvent(event: WebhookEvent): event is ZNSWebhookEvent;
/**
 * Type guard to check if event is a group event
 */
declare function isGroupEvent(event: WebhookEvent): event is GroupWebhookEvent$1;
/**
 * Type guard to check if event is a journey event
 */
declare function isJourneyEvent(event: WebhookEvent): event is JourneyTimeoutEvent | JourneyAcknowledgedEvent;
/**
 * Type guard to check if event is a quota/template change event
 */
declare function isQuotaTemplateEvent(event: WebhookEvent): event is ChangeOADailyQuotaEvent | ChangeOATemplateTagsEvent | ChangeTemplateQualityEvent | ChangeTemplateQuotaEvent | ChangeTemplateStatusEvent;
/**
 * Type guard to check if event is a widget event
 */
declare function isWidgetEvent(event: WebhookEvent): event is WidgetWebhookEvent;
/**
 * Type guard to check if event is a system event
 */
declare function isSystemEvent(event: WebhookEvent): event is SystemWebhookEvent;
/**
 * Type guard to check if event is a permission event
 */
declare function isPermissionEvent(event: WebhookEvent): event is PermissionRevokedEvent;
/**
 * Type guard to check if event is an extension event
 */
declare function isExtensionEvent(event: WebhookEvent): event is ExtensionPurchasedEvent;
/**
 * Type guard to check if event is an update user info event
 */
declare function isUpdateUserInfoEvent(event: WebhookEvent): event is UpdateUserInfoEvent;
/**
 * Type guard to check if event is a reaction event
 */
declare function isReactionEvent(event: WebhookEvent): event is UserReactedMessageEvent | OAReactedMessageEvent;
/**
 * Type guard to check if event is a consent event
 */
declare function isConsentEvent(event: WebhookEvent): event is OASendConsentEvent | UserReplyConsentEvent;
/**
 * Type guard to check if message has attachments
 */
declare function hasAttachments(message: WebhookBaseMessage): message is WebhookMessageWithAttachments;
/**
 * Type guard to check if event is a user message (from individual user to OA)
 */
declare function isUserMessageEvent(event: WebhookEvent): event is UserMessageWebhookEvent;
/**
 * Type guard to check if event is a group message (from user in group)
 */
declare function isGroupMessageEvent(event: WebhookEvent): event is GroupWebhookEvent$1;
/**
 * Type guard to check if event is OA sending message to individual user
 */
declare function isOAToUserMessageEvent(event: WebhookEvent): event is OAMessageWebhookEvent;
/**
 * Type guard to check if event is OA sending message to group
 */
declare function isOAToGroupMessageEvent(event: WebhookEvent): event is GroupWebhookEvent$1;
/**
 * Helper function to get message direction and target type
 */
declare function getMessageDirection(event: WebhookEvent): {
    direction: 'incoming' | 'outgoing' | 'unknown';
    target: 'user' | 'group' | 'unknown';
    description: string;
};
interface WebhookEventIdentifiers {
    oaId: string | null;
    appId: string | null;
    senderId: string | null;
    recipientId: string | null;
}
declare function getWebhookEventAppId(event: WebhookEvent): string | null;
declare function getWebhookEventOaId(event: WebhookEvent): string | null;
declare function getWebhookEventSenderId(event: WebhookEvent): string | null;
declare function getWebhookEventRecipientId(event: WebhookEvent): string | null;
declare function getWebhookEventIdentifiers(event: WebhookEvent): WebhookEventIdentifiers;

/**
 * Type guard utilities for Zalo SDK webhook events
 *
 * This file provides type-safe functions to check webhook event types
 * and narrow down TypeScript types for better development experience.
 */

/**
 * Type union for user message events in groups
 */
type UserGroupMessageEvent = UserSendGroupTextEvent | UserSendGroupImageEvent | UserSendGroupVideoEvent | UserSendGroupAudioEvent | UserSendGroupFileEvent;
/**
 * Check if event is a user sending text message to group
 */
declare function isUserSendGroupTextEvent(event: WebhookEvent): event is UserSendGroupTextEvent;
/**
 * Check if event is a user sending image message to group
 */
declare function isUserSendGroupImageEvent(event: WebhookEvent): event is UserSendGroupImageEvent;
/**
 * Check if event is a user sending video message to group
 */
declare function isUserSendGroupVideoEvent(event: WebhookEvent): event is UserSendGroupVideoEvent;
/**
 * Check if event is a user sending audio message to group
 */
declare function isUserSendGroupAudioEvent(event: WebhookEvent): event is UserSendGroupAudioEvent;
/**
 * Check if event is a user sending file to group
 */
declare function isUserSendGroupFileEvent(event: WebhookEvent): event is UserSendGroupFileEvent;
/**
 * Check if event is any user group message event
 */
declare function isUserGroupMessageEvent(event: WebhookEvent): event is UserGroupMessageEvent;
/**
 * Check if event is OA sending text message to user
 */
declare function isOASendTextEvent(event: WebhookEvent): event is OASendTextEvent;
/**
 * Check if event is OA sending image message to user
 */
declare function isOASendImageEvent(event: WebhookEvent): event is OASendImageEvent;
/**
 * Check if event is OA sending file to user
 */
declare function isOASendFileEvent(event: WebhookEvent): event is OASendFileEvent;
/**
 * Check if event is OA sending sticker to user
 */
declare function isOASendStickerEvent(event: WebhookEvent): event is OASendStickerEvent;
/**
 * Check if event is OA sending GIF to user
 */
declare function isOASendGifEvent(event: WebhookEvent): event is OASendGifEvent;
/**
 * Check if event is OA sending text message to group
 */
declare function isOASendGroupTextEvent(event: WebhookEvent): event is OASendGroupTextEvent;
/**
 * Check if event is OA sending image to group
 */
declare function isOASendGroupImageEvent(event: WebhookEvent): event is OASendGroupImageEvent;
/**
 * Check if event is OA sending file to group
 */
declare function isOASendGroupFileEvent(event: WebhookEvent): event is OASendGroupFileEvent;
/**
 * Check if event is OA sending sticker to group
 */
declare function isOASendGroupStickerEvent(event: WebhookEvent): event is OASendGroupStickerEvent;
/**
 * Check if event is OA sending GIF to group
 */
declare function isOASendGroupGifEvent(event: WebhookEvent): event is OASendGroupGifEvent;
/**
 * Check if event is from a group context
 */
declare function isFromGroup(event: WebhookEvent): boolean;
/**
 * Check if event is from a personal context
 */
declare function isFromPersonal(event: WebhookEvent): boolean;

/**
 * Group Message Framework (GMF) Type Definitions
 */
interface GroupMessage {
    type: "text" | "image" | "file" | "sticker" | "mention";
}
interface GroupTextMessage extends GroupMessage {
    type: "text";
    text: string;
}
interface GroupImageMessage extends GroupMessage {
    type: "image";
    imageUrl?: string;
    attachmentId?: string;
    caption?: string;
}
interface GroupFileMessage extends GroupMessage {
    type: "file";
    fileToken: string;
    fileName?: string;
    fileSize?: number;
}
interface GroupStickerMessage extends GroupMessage {
    type: "sticker";
    stickerId: string;
    stickerCategory?: string;
}
interface GroupMentionMessage extends GroupMessage {
    type: "mention";
    text: string;
    mentions: Array<{
        user_id: string;
        offset: number;
        length: number;
    }>;
}
interface GroupMessageResult {
    error: number;
    message: string;
    data?: {
        message_id: string;
        sent_time: string;
        group_id: string;
    };
}
interface GroupInfo {
    error: number;
    message: string;
    data?: {
        group_id: string;
        group_name: string;
        group_description?: string;
        group_avatar?: string;
        member_count: number;
        created_time: string;
        group_type: "PUBLIC" | "PRIVATE";
        group_status: "ACTIVE" | "INACTIVE" | "DISBANDED";
        admin_ids: string[];
        oa_permissions: {
            can_send_message: boolean;
            can_view_members: boolean;
            can_manage_group: boolean;
        };
    };
}
interface GroupMember {
    user_id: string;
    display_name: string;
    avatar?: string;
    role: "ADMIN" | "MEMBER";
    joined_time: string;
    is_active: boolean;
    last_seen?: string;
}
interface GroupMemberList {
    error: number;
    message: string;
    data?: {
        members: GroupMember[];
        total_count: number;
        has_more: boolean;
    };
}
interface GroupSettings {
    group_id: string;
    settings: {
        allow_member_invite: boolean;
        allow_member_add_admin: boolean;
        allow_member_change_info: boolean;
        require_admin_approval: boolean;
        auto_approve_join_request: boolean;
        message_history_visible: boolean;
    };
}
interface GroupJoinRequest {
    request_id: string;
    user_id: string;
    display_name: string;
    avatar?: string;
    request_message?: string;
    request_time: string;
    status: "PENDING" | "APPROVED" | "REJECTED";
}
interface GroupJoinRequestList {
    error: number;
    message: string;
    data?: {
        requests: GroupJoinRequest[];
        total_count: number;
        has_more: boolean;
    };
}
interface GroupInvitation {
    invitation_id: string;
    group_id: string;
    inviter_id: string;
    invitee_phone?: string;
    invitee_user_id?: string;
    invitation_message?: string;
    invitation_time: string;
    expiry_time: string;
    status: "PENDING" | "ACCEPTED" | "REJECTED" | "EXPIRED";
}
interface GroupActivity {
    activity_id: string;
    group_id: string;
    user_id: string;
    activity_type: "JOIN" | "LEAVE" | "MESSAGE" | "ADMIN_ACTION" | "SETTINGS_CHANGE";
    activity_description: string;
    activity_time: string;
    metadata?: Record<string, any>;
}
interface GroupActivityList {
    error: number;
    message: string;
    data?: {
        activities: GroupActivity[];
        total_count: number;
        has_more: boolean;
    };
}
interface GroupStatistics {
    group_id: string;
    period: {
        from: string;
        to: string;
    };
    stats: {
        total_messages: number;
        active_members: number;
        new_members: number;
        left_members: number;
        message_breakdown: {
            text: number;
            image: number;
            file: number;
            sticker: number;
            other: number;
        };
        daily_activity: Array<{
            date: string;
            message_count: number;
            active_member_count: number;
        }>;
        top_active_members: Array<{
            user_id: string;
            display_name: string;
            message_count: number;
        }>;
    };
}
interface GroupMessageTemplate {
    template_id: string;
    template_name: string;
    template_content: string;
    template_type: "ANNOUNCEMENT" | "WELCOME" | "REMINDER" | "CUSTOM";
    variables: Array<{
        name: string;
        type: "TEXT" | "NUMBER" | "DATE";
        required: boolean;
        default_value?: string;
    }>;
    created_time: string;
    last_used?: string;
    usage_count: number;
}
interface GroupMessageSchedule {
    schedule_id: string;
    group_id: string;
    message: GroupMessage;
    schedule_time: string;
    repeat_type?: "NONE" | "DAILY" | "WEEKLY" | "MONTHLY";
    repeat_interval?: number;
    repeat_end_date?: string;
    status: "PENDING" | "SENT" | "CANCELLED" | "FAILED";
    created_time: string;
}
interface GroupBroadcast {
    broadcast_id: string;
    group_ids: string[];
    message: GroupMessage;
    send_time?: string;
    target_criteria?: {
        member_roles?: ("ADMIN" | "MEMBER")[];
        member_join_date_from?: string;
        member_join_date_to?: string;
        active_in_last_days?: number;
    };
    status: "PENDING" | "SENDING" | "COMPLETED" | "FAILED" | "CANCELLED";
    results?: {
        total_groups: number;
        successful_groups: number;
        failed_groups: number;
        total_recipients: number;
        successful_sends: number;
        failed_sends: number;
    };
    created_time: string;
    completed_time?: string;
}
interface GroupPermission {
    group_id: string;
    user_id: string;
    permissions: {
        can_send_message: boolean;
        can_send_media: boolean;
        can_mention_all: boolean;
        can_invite_members: boolean;
        can_remove_members: boolean;
        can_change_group_info: boolean;
        can_pin_messages: boolean;
        can_delete_messages: boolean;
    };
}
interface GroupWebhookEvent {
    event_type: "GROUP_MESSAGE" | "MEMBER_JOIN" | "MEMBER_LEAVE" | "GROUP_SETTINGS_CHANGE";
    group_id: string;
    timestamp: string;
    data: Record<string, any>;
}
interface GroupApiResponse<T = any> {
    error: number;
    message: string;
    data?: T;
}
type GroupCreateResponse = GroupApiResponse<{
    group_id: string;
    group_name: string;
    created_time: string;
}>;
type GroupUpdateResponse = GroupApiResponse<{
    group_id: string;
    updated_fields: string[];
    updated_time: string;
}>;
type GroupDeleteResponse = GroupApiResponse<{
    group_id: string;
    deleted_time: string;
}>;
type GroupMemberActionResponse = GroupApiResponse<{
    group_id: string;
    user_id: string;
    action: "ADDED" | "REMOVED" | "PROMOTED" | "DEMOTED";
    action_time: string;
}>;
/**
 * Group creation request
 */
interface GroupCreateRequest {
    group_name: string;
    group_description?: string;
    asset_id: string;
    member_user_ids: string[];
}
/**
 * Group creation data - theo chuẩn Zalo API response data field
 * API: POST https://openapi.zalo.me/v3.0/oa/group/creategroupwithoa
 */
interface GroupCreateData {
    /** ID của nhóm vừa tạo */
    group_id: string;
    /** Tên nhóm */
    name: string;
    /** URL avatar của nhóm */
    avatar: string;
    /** Link để tham gia nhóm */
    group_link: string;
    /** Mô tả nhóm */
    group_description: string;
    /** Tổng số lượng thành viên trong nhóm */
    total_member: number;
    /** Trạng thái nhóm: "enabled" | "disabled" */
    status: "enabled" | "disabled";
}
/**
 * Group creation result - full Zalo API response wrapper
 * API: POST https://openapi.zalo.me/v3.0/oa/group/creategroupwithoa
 */
interface GroupCreateResult {
    /** Response data */
    data: GroupCreateData;
    /** Error code (0 = success) */
    error: number;
    /** Response message */
    message: string;
}
/**
 * Group update request - theo chuẩn Zalo API
 * API: POST https://openapi.zalo.me/v3.0/oa/group/updateinfo
 */
interface GroupUpdateRequest {
    /** Tên nhóm */
    group_name?: string;
    /** Avatar của nhóm */
    group_avatar?: string;
    /** Mô tả nhóm */
    group_description?: string;
    /** Cài đặt chức năng nhắn tin của các thành viên */
    lock_send_msg?: boolean;
    /** Cài đặt chế độ phê duyệt thành viên mới */
    join_appr?: boolean;
    /** Cài đặt cho phép thành viên mới đọc tin nhắn gần nhất */
    enable_msg_history?: boolean;
    /** Cài đặt bật tắt tham gia nhóm bằng link */
    enable_link_join?: boolean;
}
/**
 * Group update result - theo chuẩn Zalo API response
 * API: POST https://openapi.zalo.me/v3.0/oa/group/updateinfo
 */
interface GroupUpdateResult {
    data: {
        /** Thông tin nhóm */
        group_info: {
            group_id: string;
            group_link: string;
            name: string;
            group_description: string;
            avatar: string;
            status: "enabled" | "disabled";
            total_member: number;
            max_member: string;
            auto_delete_date: string;
        };
        /** Thông tin asset */
        asset_info: {
            asset_type: "gmf10" | "gmf50" | "gmf100";
            asset_id: string;
            valid_through: string;
            auto_renew: string;
        };
        /** Cài đặt nhóm */
        group_setting: {
            lock_send_msg: boolean;
            join_appr: boolean;
            enable_msg_history: boolean;
            enable_link_join: boolean;
        };
    };
    error: number;
    message: string;
}
/**
 * Group asset update request - theo chuẩn Zalo API
 * API: POST https://openapi.zalo.me/v3.0/oa/group/updateasset
 *
 * Sử dụng để:
 * - Tăng giới hạn số lượng thành viên trong nhóm
 * - Gia hạn cho nhóm chat khi hết hạn
 */
interface GroupAssetUpdateRequest {
    /** ID của nhóm */
    group_id: string;
    /** ID của gói hạn mức (lấy từ API kiểm tra hạn mức tạo nhóm GMF) */
    asset_id: string;
}
/**
 * Group avatar update request
 * @deprecated Use GroupUpdateRequest with group_avatar field instead
 */
interface GroupAvatarUpdateRequest {
    avatar_url: string;
}
/**
 * Group member invitation request - theo chuẩn Zalo API
 * API: POST https://openapi.zalo.me/v3.0/oa/group/invite
 */
interface GroupMemberInviteRequest {
    /** Danh sách user id muốn mời tham gia nhóm */
    member_user_ids: string[];
}
/**
 * Group member invitation result - theo chuẩn Zalo API response
 * API: POST https://openapi.zalo.me/v3.0/oa/group/invite
 */
interface GroupInviteResult {
    /** Error code (0 = success) */
    error: number;
    /** Response message */
    message: string;
}
/**
 * Group member action request (for accept/reject)
 */
interface GroupMemberActionRequest {
    member_uids: string[];
}
/**
 * Group admin action request - theo chuẩn Zalo API
 * API: POST https://openapi.zalo.me/v3.0/oa/group/addadmins
 * API: POST https://openapi.zalo.me/v3.0/oa/group/removeadmins
 */
interface GroupAdminActionRequest {
    /** Danh sách các user_id được thêm/xóa làm phó nhóm */
    member_user_ids: string[];
}
/**
 * Group delete request
 */
interface GroupDeleteRequest {
    group_id: string;
}
/**
 * Pending member information - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/listpendinginvite
 */
interface GroupPendingMember {
    /** Tên thành viên chờ tham gia nhóm */
    name: string;
    /** ID thành viên chờ tham gia nhóm */
    user_id: string;
}
/**
 * Group quota information
 */
interface GroupQuota {
    error: number;
    message: string;
    data?: {
        max_groups: number;
        current_groups: number;
        remaining_groups: number;
        max_members_per_group: number;
        asset_info?: GroupQuotaAsset[];
    };
}
/**
 * Group quota asset information - theo chuẩn Zalo API
 * API: POST https://openapi.zalo.me/v3.0/oa/quota/group
 */
interface GroupQuotaAsset {
    /** Loại sản phẩm (gmf10, gmf50, gmf100) */
    product_type: string;
    /** Loại quota (sub_quota, purchase_quota, reward_quota) */
    quota_type: string;
    /** ID gói GMF, sử dụng để làm dịch vụ hoạt động của Nhóm */
    asset_id: string;
    /** Ngày hết hạn của gói */
    valid_through: string;
    /** Có bật tự động gia hạn hay không */
    auto_renew: boolean;
    /** Trạng thái asset (available, used) */
    status: "available" | "used";
    /** ID của đối tượng sử dụng gói (group_id) */
    used_id?: string;
    /** Sử dụng used_id thay thế */
    group_id?: string;
}
/**
 * Recent chat message - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/listrecentchat
 */
interface RecentChatMessage {
    /** Thuộc tính cho biết tin nhắn được gửi từ OA (0) hoặc người dùng (1) */
    src: number;
    /** Thời gian tin nhắn được gửi (timestamp) */
    time: number;
    /** Loại tin nhắn (text, voice, photo, GIF, link, links, sticker, location) */
    type: string;
    /** Nội dung tin nhắn */
    message: string;
    /** ID của tin nhắn */
    message_id: string;
    /** ID của đối tượng gửi tin nhắn (user_id hoặc oa_id) */
    from_id: string;
    /** Tên hiển thị của đối tượng gửi tin nhắn */
    from_display_name: string;
    /** Đường dẫn đến ảnh đại diện của đối tượng gửi tin nhắn */
    from_avatar: string;
    /** ID của nhóm */
    group_id: string;
    /** Danh sách các đường dẫn được đính kèm (chỉ khi type là link hoặc links) */
    links?: string[];
    /** URL của ảnh hoặc GIF (chỉ khi type là GIF hoặc photo) */
    thumb?: string;
    /** Đường dẫn đến audio/ảnh/GIF/sticker */
    url?: string;
    /** Mô tả của ảnh (chỉ khi type là photo) */
    description?: string;
    /** Giá trị longitude và latitude (chỉ khi type là location) */
    location?: string;
}
/**
 * Recent chats response - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/listrecentchat
 */
interface RecentChatsResponse {
    error: number;
    message: string;
    data: RecentChatMessage[];
}
/**
 * Recent chat information (legacy - deprecated)
 * @deprecated Use RecentChatMessage instead
 */
interface GroupRecentChat {
    group_id: string;
    group_name: string;
    last_message: {
        message_id: string;
        content: string;
        sender_id: string;
        sent_time: string;
        message_type: string;
    };
    unread_count: number;
    last_activity: string;
}
/**
 * Group conversation response - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/conversation
 *
 * Note: Response structure giống hệt với listrecentchat API
 */
interface GroupConversationResponse {
    error: number;
    message: string;
    data: RecentChatMessage[];
}
/**
 * Group conversation message (legacy - deprecated)
 * @deprecated Use RecentChatMessage instead - API trả về structure giống hệt listrecentchat
 */
interface GroupConversationMessage {
    message_id: string;
    sender_id: string;
    sender_name: string;
    content: string;
    message_type: "text" | "image" | "file" | "sticker" | "location";
    sent_time: string;
    attachments?: Array<{
        type: string;
        url: string;
        name?: string;
        size?: number;
    }>;
    mentioned_users?: string[];
}
/**
 * OA Group item - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/getgroupsofoa
 */
interface OAGroupItem {
    /** Tên nhóm */
    name: string;
    /** Hình đại diện của nhóm */
    avatar: string;
    /** ID của nhóm */
    group_id: string;
    /** Link để tham gia nhóm */
    group_link: string;
    /** Mô tả nhóm */
    group_description: string;
    /** Tổng số lượng thành viên trong nhóm */
    total_member: number;
    /** Trạng thái nhóm: enabled/disabled */
    status: string;
}
/**
 * Groups of OA response - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/getgroupsofoa
 */
interface GroupsOfOAResponse {
    error: number;
    message: string;
    data?: {
        /** Offset muốn query */
        offset: number;
        /** Số lượng muốn query */
        count: number;
        /** Tổng số lượng các nhóm của OA */
        total: number;
        /** Số lượng các nhóm được trả về */
        group_count: number;
        /** Danh sách các nhóm của OA */
        groups: OAGroupItem[];
    };
}
/**
 * Group detail response - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/getgroup
 */
interface GroupDetailResponse {
    error: number;
    message: string;
    data?: {
        /** Các thông tin của nhóm chat */
        group_info: {
            /** Tên nhóm */
            name: string;
            /** Avatar của nhóm */
            avatar: string;
            /** ID của nhóm */
            group_id: string;
            /** Link tham gia của nhóm */
            group_link: string;
            /** Mô tả nhóm */
            group_description: string;
            /** Trạng thái nhóm (enabled/disabled) */
            status: string;
            /** Tổng số lượng thành viên trong nhóm */
            total_member: number;
            /** Số lượng thành viên tối đa của nhóm */
            max_member: string;
            /** Ngày nhóm chat tự động giải tán */
            auto_delete_date: string;
        };
        /** Các thông tin của dịch vụ gắn với nhóm chat */
        asset_info: {
            /** Loại sản phẩm (gmf10, gmf50, gmf100) */
            asset_type: string;
            /** ID gói GMF */
            asset_id: string;
            /** Ngày hết hạn của gói */
            valid_through: string;
            /** Có bật tự động gia hạn hay không */
            auto_renew: string;
        };
        /** Các thông tin cài đặt trong nhóm chat */
        group_setting: {
            /** Cài đặt chức năng nhắn tin của các thành viên */
            lock_send_msg: boolean;
            /** Cài đặt chế độ phê duyệt thành viên mới */
            join_appr: boolean;
            /** Cài đặt cho phép thành viên mới đọc tin nhắn gần nhất */
            enable_msg_history: boolean;
            /** Cài đặt bật tắt tham gia nhóm bằng link */
            enable_link_join: boolean;
        };
    };
}
/**
 * Pending members response
 */
interface GroupPendingMembersResponse {
    error: number;
    message: string;
    data?: {
        offset: number;
        count: number;
        total: number;
        member_count: number;
        members: GroupPendingMember[];
    };
}
/**
 * Accept pending members request
 */
interface GroupAcceptPendingMembersRequest {
    group_id: string;
    member_user_ids: string[];
}
/**
 * Accept pending members response - theo chuẩn Zalo API
 * API: POST https://openapi.zalo.me/v3.0/oa/group/acceptpendinginvite
 */
interface GroupAcceptPendingMembersResponse {
    /** Error code (0 = success) */
    error: number;
    /** Response message */
    message: string;
}
/**
 * Remove members request
 */
interface GroupRemoveMembersRequest {
    group_id: string;
    member_user_ids: string[];
}
/**
 * Remove members response
 */
interface GroupRemoveMembersResponse {
    error: number;
    message: string;
    data?: {
        removed_count: number;
        failed_members?: string[];
    };
}
/**
 * Group member information for list API - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/listmember
 */
interface GroupMemberInfo {
    /** ID thành viên trong nhóm (cho user thường) */
    user_id?: string;
    /** ID của OA (cho OA member) */
    oa_id?: string;
    /** Tên thành viên trong nhóm */
    name: string;
    /** Hình đại diện của thành viên trong nhóm */
    avatar: string;
}
/**
 * Group members response - theo chuẩn Zalo API
 * API: GET https://openapi.zalo.me/v3.0/oa/group/listmember
 */
interface GroupMembersResponse {
    error: number;
    message: string;
    data?: {
        /** Offset muốn query */
        offset: number;
        /** Số lượng muốn query */
        count: number;
        /** Tổng số lượng các thành viên trong nhóm */
        total: number;
        /** Số lượng các thành viên được trả về */
        member_count: number;
        /** Thông tin các thành viên trong nhóm */
        members: GroupMemberInfo[];
    };
}
/**
 * Quota message request
 */
interface GroupQuotaMessageRequest {
    quota_owner: "OA" | "APP";
    product_type?: string;
    quota_type?: string;
}
/**
 * Quota message response
 */
interface GroupQuotaMessageResponse {
    error: number;
    message: string;
    data?: GroupQuotaAsset[];
}
/**
 * Enhanced response for getAllGroupMembers with additional metadata
 */
interface AllGroupMembersResponse {
    /** Error code (0 = success) */
    error: number;
    /** Response message */
    message: string;
    /** Enhanced data with all members and pagination info */
    data: {
        /** Total number of members in the group */
        total_members: number;
        /** ALL members retrieved */
        members: GroupMemberInfo[];
        /** Total number of API pages fetched */
        pages_fetched: number;
        /** Whether all available members were retrieved */
        is_complete: boolean;
    };
}
/**
 * Enhanced response for getAllGroupsOfOA with additional metadata
 */
interface AllGroupsOfOAResponse {
    /** Error code (0 = success) */
    error: number;
    /** Response message */
    message: string;
    /** Enhanced data with pagination info */
    data: {
        /** Total number of groups for the OA */
        total_groups: number;
        /** Array of all OA groups */
        groups: OAGroupItem[];
        /** Number of API pages fetched to get all groups */
        pages_fetched: number;
        /** Whether all available groups were retrieved (not limited by maxGroups) */
        is_complete: boolean;
    };
}
/**
 * Progress callback interface for getAllGroupMembers
 */
interface GetAllMembersProgress {
    /** Current number of members retrieved */
    currentCount: number;
    /** Total number of members in group (if known) */
    totalCount?: number;
    /** Completion percentage (if totalCount is known) */
    percentage?: number;
    /** Whether the operation is complete */
    isComplete: boolean;
}
/**
 * Enhanced group member with detailed user information
 * Combines basic member info from group API with detailed user info from user API
 */
interface EnhancedGroupMember {
    /** Basic member info from group API (always available) */
    basic_info: GroupMemberInfo;
    /** Detailed user info from user API (null if failed to fetch) */
    detailed_info: UserInfo | null;
    /** Whether detailed info was successfully fetched */
    has_detailed_info: boolean;
    /** Error message if failed to fetch detailed info */
    detail_fetch_error?: string;
}
/**
 * Enhanced response for getAllGroupMembersWithDetails
 */
interface AllGroupMembersWithDetailsResponse {
    /** Error code (0 = success) */
    error: number;
    /** Response message */
    message: string;
    /** Enhanced data with detailed member information */
    data: {
        /** Total number of members in the group */
        total_members: number;
        /** ALL enhanced members retrieved */
        members: EnhancedGroupMember[];
        /** Total number of API pages fetched during member retrieval */
        pages_fetched: number;
        /** Whether all available members were retrieved */
        is_complete: boolean;
        /** Statistics about detailed info fetching */
        detail_fetch_stats: {
            /** Total members processed */
            total_processed: number;
            /** Successfully fetched detailed info */
            successful_details: number;
            /** Failed to fetch detailed info */
            failed_details: number;
            /** Success rate percentage */
            success_rate: number;
        };
    };
}
/**
 * Progress callback interface for getAllGroupMembersWithDetails
 */
interface GetAllMembersWithDetailsProgress {
    /** Current number of members retrieved */
    currentCount: number;
    /** Total number of members in group (if known) */
    totalCount?: number;
    /** Completion percentage (if totalCount is known) */
    percentage?: number;
    /** Whether the operation is complete */
    isComplete: boolean;
    /** Current phase of operation */
    phase: 'fetching_members' | 'fetching_details' | 'complete';
    /** Details about current phase */
    phase_details?: {
        /** For fetching_details phase: number of details fetched */
        details_fetched?: number;
        /** For fetching_details phase: number of details failed */
        details_failed?: number;
        /** Current batch being processed */
        current_batch?: number;
        /** Total batches to process */
        total_batches?: number;
    };
}

/**
 * User Management Type Definitions for Zalo OA
 */
interface UserProfile {
    error: number;
    message: string;
    data?: {
        user_id: string;
        user_id_by_app: string;
        display_name: string;
        user_alias?: string;
        user_is_follower: boolean;
        avatar?: string;
        avatars?: {
            '120': string;
            '240': string;
        };
        user_gender?: 'male' | 'female';
        user_locale?: string;
        shared_info?: {
            address?: string;
            phone?: string;
            name?: string;
            district?: string;
            province?: string;
            ward?: string;
        };
        tags_and_notes_info?: {
            tag_names?: string[];
            notes?: string;
        };
        is_sensitive?: boolean;
        last_interaction_date?: string;
        user_last_interaction_date?: string;
    };
}
interface UserList {
    error: number;
    message: string;
    data?: {
        followers: Array<{
            user_id: string;
            user_id_by_app?: string;
            display_name: string;
            avatar?: string;
            user_is_follower: boolean;
            user_last_interaction_date?: string;
        }>;
        total: number;
    };
}
interface UserTag {
    tag_id?: string;
    tag_name: string;
    tag_color?: string;
    created_time?: string;
    user_count?: number;
}
interface UserTagList {
    error: number;
    message: string;
    data?: {
        tags: UserTag[];
        total: number;
    };
}
interface UserNote {
    note_id: string;
    note_content: string;
    created_time: string;
    created_by?: string;
    updated_time?: string;
    updated_by?: string;
}
interface UserInteraction {
    interaction_id: string;
    interaction_type: 'MESSAGE_SENT' | 'MESSAGE_RECEIVED' | 'FOLLOW' | 'UNFOLLOW' | 'BLOCK' | 'UNBLOCK';
    interaction_time: string;
    interaction_data?: {
        message_id?: string;
        message_type?: 'text' | 'image' | 'file' | 'sticker' | 'location' | 'template';
        message_content?: string;
        attachment_id?: string;
        template_id?: string;
    };
    source?: 'OA_CHAT' | 'ZNS' | 'GROUP_CHAT' | 'SOCIAL';
}
interface UserAnalytics {
    user_id: string;
    period: {
        from: string;
        to: string;
    };
    metrics: {
        total_interactions: number;
        messages_sent: number;
        messages_received: number;
        response_rate: number;
        avg_response_time: number;
        last_interaction: string;
        interaction_frequency: 'HIGH' | 'MEDIUM' | 'LOW';
    };
    interaction_breakdown: {
        by_type: Record<string, number>;
        by_day: Array<{
            date: string;
            interactions: number;
        }>;
        by_hour: Array<{
            hour: number;
            interactions: number;
        }>;
    };
    engagement_score: number;
}
interface UserSegment {
    segment_id: string;
    segment_name: string;
    segment_description?: string;
    criteria: {
        tags?: string[];
        interaction_frequency?: 'HIGH' | 'MEDIUM' | 'LOW';
        last_interaction_days?: number;
        user_attributes?: Record<string, any>;
        custom_fields?: Record<string, any>;
    };
    user_count: number;
    created_time: string;
    updated_time?: string;
}
interface UserCustomField {
    field_id: string;
    field_name: string;
    field_type: 'TEXT' | 'NUMBER' | 'DATE' | 'BOOLEAN' | 'SELECT' | 'MULTI_SELECT';
    field_options?: string[];
    is_required: boolean;
    is_searchable: boolean;
    created_time: string;
}
interface UserCustomFieldValue {
    field_id: string;
    field_name: string;
    field_value: any;
    updated_time: string;
}
interface UserBehavior {
    user_id: string;
    behavior_type: 'PAGE_VIEW' | 'BUTTON_CLICK' | 'FORM_SUBMIT' | 'PURCHASE' | 'CUSTOM';
    behavior_data: Record<string, any>;
    timestamp: string;
    source: string;
    session_id?: string;
}
interface UserJourney {
    user_id: string;
    journey_id: string;
    journey_name: string;
    current_step: string;
    step_history: Array<{
        step_id: string;
        step_name: string;
        entered_time: string;
        completed_time?: string;
        status: 'COMPLETED' | 'IN_PROGRESS' | 'SKIPPED' | 'FAILED';
    }>;
    journey_status: 'ACTIVE' | 'COMPLETED' | 'ABANDONED' | 'PAUSED';
    started_time: string;
    completed_time?: string;
    abandoned_time?: string;
}
interface UserPreference {
    user_id: string;
    preferences: {
        communication_channels: ('OA_CHAT' | 'ZNS' | 'EMAIL' | 'SMS')[];
        message_frequency: 'REAL_TIME' | 'DAILY' | 'WEEKLY' | 'MONTHLY';
        content_types: string[];
        language: string;
        timezone: string;
        opt_in_marketing: boolean;
        opt_in_notifications: boolean;
    };
    updated_time: string;
}
interface UserActivity {
    activity_id: string;
    user_id: string;
    activity_type: string;
    activity_description: string;
    activity_time: string;
    metadata?: Record<string, any>;
}
interface UserExport {
    export_id: string;
    export_type: 'ALL_USERS' | 'FILTERED_USERS' | 'SEGMENT_USERS';
    filters?: {
        tags?: string[];
        date_range?: {
            from: string;
            to: string;
        };
        interaction_criteria?: Record<string, any>;
    };
    segment_id?: string;
    export_format: 'CSV' | 'EXCEL' | 'JSON';
    status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
    file_url?: string;
    total_records?: number;
    created_time: string;
    completed_time?: string;
    expires_at?: string;
}
interface UserImport {
    import_id: string;
    import_type: 'NEW_USERS' | 'UPDATE_USERS' | 'MERGE_USERS';
    file_url: string;
    mapping: Record<string, string>;
    status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
    total_records?: number;
    processed_records?: number;
    successful_records?: number;
    failed_records?: number;
    error_details?: Array<{
        row: number;
        error: string;
    }>;
    created_time: string;
    completed_time?: string;
}
interface UserSearch {
    query: string;
    filters?: {
        tags?: string[];
        user_attributes?: Record<string, any>;
        interaction_date_range?: {
            from: string;
            to: string;
        };
        custom_fields?: Record<string, any>;
    };
    sort?: {
        field: string;
        order: 'ASC' | 'DESC';
    };
    pagination: {
        offset: number;
        limit: number;
    };
}
interface UserSearchResult {
    users: UserProfile[];
    total_count: number;
    has_more: boolean;
    search_time: number;
}
interface BulkUserOperation {
    operation_id: string;
    operation_type: 'TAG_USERS' | 'UNTAG_USERS' | 'DELETE_USERS' | 'UPDATE_USERS';
    user_ids: string[];
    operation_data?: Record<string, any>;
    status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
    total_users: number;
    processed_users?: number;
    successful_operations?: number;
    failed_operations?: number;
    created_time: string;
    completed_time?: string;
}

/**
 * Base HTTP client for Zalo API
 */

/**
 * Base client for making HTTP requests to Zalo API
 */
declare class BaseClient {
    protected readonly axios: AxiosInstance;
    protected readonly logger: Logger;
    protected readonly config: Required<ZaloSDKConfig>;
    constructor(config: ZaloSDKConfig);
    /**
     * Setup axios interceptors for logging and error handling
     */
    private setupInterceptors;
    /**
     * Sanitize headers for logging (remove sensitive information)
     */
    private sanitizeHeaders;
    /**
     * Handle axios errors and log them
     */
    private handleAxiosError;
    /**
     * Make a GET request
     */
    protected get<T = any>(url: string, accessToken?: string, params?: Record<string, any>): Promise<T>;
    /**
     * Make a POST request
     */
    protected post<T = any>(url: string, accessToken?: string, data?: any, params?: Record<string, any>): Promise<T>;
    /**
     * Make a PUT request
     */
    protected put<T = any>(url: string, accessToken?: string, data?: any, params?: Record<string, any>): Promise<T>;
    /**
     * Make a DELETE request
     */
    protected delete<T = any>(url: string, accessToken?: string, params?: Record<string, any>): Promise<T>;
    /**
     * Upload file using FormData
     */
    protected uploadFile<T = any>(url: string, accessToken: string, file: Buffer | NodeJS.ReadableStream, filename: string, additionalFields?: Record<string, any>): Promise<T>;
    /**
     * Make a generic HTTP request with retry logic
     */
    protected request<T = any>(config: RequestConfig): Promise<T>;
    /**
     * Validate Zalo API response for errors
     */
    private validateZaloResponse;
    /**
     * Determine if request should be retried
     */
    private shouldRetry;
    /**
     * Create SDK error from axios error
     */
    private createSDKError;
    /**
     * Delay execution for specified milliseconds
     */
    private delay;
}

/**
 * Main Zalo API client
 */

/**
 * Zalo API client for making HTTP requests
 */
declare class ZaloClient extends BaseClient {
    constructor(config: ZaloSDKConfig);
    /**
     * Make authenticated GET request to Zalo API
     */
    apiGet<T = any>(endpoint: string, accessToken: string, params?: Record<string, any>): Promise<T>;
    /**
     * Make GET request with custom headers
     */
    apiGetWithHeaders<T = any>(endpoint: string, headers: Record<string, string>): Promise<T>;
    /**
     * Make authenticated POST request to Zalo API
     */
    apiPost<T = any>(endpoint: string, accessToken: string, data?: any, params?: Record<string, any>): Promise<T>;
    /**
     * Make authenticated POST request to Zalo API with custom headers
     */
    apiPostWithHeaders<T = any>(endpoint: string, accessToken: string, data?: any, customHeaders?: Record<string, string>): Promise<T>;
    /**
     * Make authenticated PUT request to Zalo API
     */
    apiPut<T = any>(endpoint: string, accessToken: string, data?: any, params?: Record<string, any>): Promise<T>;
    /**
     * Make authenticated DELETE request to Zalo API
     */
    apiDelete<T = any>(endpoint: string, accessToken: string, params?: Record<string, any>): Promise<T>;
    /**
     * Upload file to Zalo API
     */
    apiUploadFile<T = any>(endpoint: string, accessToken: string, file: Buffer | NodeJS.ReadableStream, filename: string, additionalFields?: Record<string, any>): Promise<T>;
    /**
     * Make POST request with FormData (for file uploads)
     */
    apiPostFormData<T = any>(endpoint: string, accessToken: string, formData: FormData): Promise<T>;
    /**
     * Make request to OAuth endpoints (without access token)
     */
    oauthRequest<T = any>(method: "GET" | "POST", endpoint: string, data?: any, headers?: Record<string, string>): Promise<T>;
    /**
     * Make request to ZNS API (different base URL)
     */
    znsRequest<T = any>(method: "GET" | "POST" | "PUT" | "DELETE", endpoint: string, accessToken: string, data?: any, params?: Record<string, any>): Promise<T>;
    /**
     * Make request to OAuth endpoints with custom base URL
     */
    oauthRequestWithUrl<T = any>(method: "GET" | "POST", url: string, data?: any, headers?: Record<string, string>): Promise<T>;
}

/**
 * Authentication service for Zalo API
 */

/**
 * Authentication service for handling OAuth flows and token management
 */
declare class AuthService {
    private readonly client;
    private readonly appId;
    private readonly appSecret;
    private readonly endpoints;
    constructor(client: ZaloClient, appId: string, appSecret: string);
    /**
     * Generate PKCE code verifier and challenge for Social API
     */
    generatePKCE(): PKCEConfig;
    /**
     * Create OAuth authorization URL for Official Account with PKCE support
     *
     * @param redirectUri - The redirect URI after authorization
     * @param state - Optional state parameter for security. If not provided, auto-generates with 'zalo_oa_' prefix
     * @param usePkce - Whether to use PKCE for enhanced security. If true and pkce not provided, will auto-generate
     * @param pkce - Optional PKCE configuration for enhanced security. If usePkce=true and this is not provided, will be auto-generated
     * @returns Object containing the authorization URL, state, and PKCE config (if used)
     */
    createOAAuthUrl(redirectUri: string, state?: string, pkce?: PKCEConfig, usePkce?: boolean): OAAuthResult;
    /**
     * Create OAuth authorization URL for Social API
     */
    createSocialAuthUrl(redirectUri: string, state?: string, pkce?: PKCEConfig): string;
    /**
     * Exchange authorization code for Official Account access token
     * Now supports PKCE code_verifier for enhanced security
     */
    getOAAccessToken(params: AuthCodeParams): Promise<AccessToken>;
    /**
     * Exchange authorization code for Social API access token
     */
    getSocialAccessToken(params: AuthCodeParams): Promise<AccessToken>;
    /**
     * Refresh Official Account access token
     */
    refreshOAAccessToken(params: RefreshTokenParams): Promise<AccessToken>;
    /**
     * Refresh Social API access token
     */
    refreshSocialAccessToken(params: RefreshTokenParams): Promise<RefreshTokenResponse>;
    /**
     * Get Social user information
     */
    getSocialUserInfo(accessToken: string, fields?: string): Promise<SocialUserInfo$1>;
    /**
     * Validate access token by attempting to get user info
     */
    validateAccessToken(accessToken: string, scope?: AuthScope): Promise<TokenValidation>;
    /**
     * Get all authentication URLs with optional PKCE support
     */
    getAuthUrls(redirectUri: string, usePkce?: boolean, pkce?: PKCEConfig): AuthUrls;
}

/**
 * Official Account (OA) service for Zalo API
 */

/**
 * Official Account service for managing OA information and settings
 */
declare class OAService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Get Official Account information
     */
    getOAInfo(accessToken: string): Promise<OAInfo>;
    /**
     * Get detailed quota message information
     */
    getDetailedQuotaMessage(accessToken: string, request: QuotaMessageRequest): Promise<QuotaAsset[]>;
    /**
     * Get quota for specific product type
     */
    getQuotaByProductType(accessToken: string, productType: 'cs' | 'transaction' | 'gmf10' | 'gmf50' | 'gmf100', quotaType?: 'sub_quota' | 'purchase_quota' | 'reward_quota'): Promise<QuotaAsset[]>;
    /**
     * Get available GMF quota
     */
    getGMFQuota(accessToken: string, gmfType?: GMFProductType): Promise<QuotaAsset[]>;
    /**
     * Get consultation message quota
     */
    getConsultationQuota(accessToken: string): Promise<QuotaAsset[]>;
    /**
     * Get transaction message quota
     */
    getTransactionQuota(accessToken: string): Promise<QuotaAsset[]>;
    /**
     * Update OA profile information
     */
    updateOAProfile(accessToken: string, profileData: UpdateOAProfileRequest): Promise<boolean>;
    /**
     * Get OA statistics (if available)
     */
    getOAStatistics(accessToken: string): Promise<OAStatistics>;
    /**
     * Lấy thông tin quota tổng quan cho tin nhắn (tổng số asset và số còn khả dụng)
     * Lưu ý: Zalo hiện cung cấp API chi tiết qua endpoint quota/message theo asset.
     * Hàm này tổng hợp đơn giản để trả về cấu trúc MessageQuota phục vụ SDK.
     */
    getMessageQuota(accessToken: string): Promise<MessageQuota>;
    /**
     * Check if OA has sufficient quota for message type
     */
    hasQuotaForMessageType(accessToken: string, messageType: 'cs' | 'transaction' | 'promotion', requiredCount?: number): Promise<boolean>;
    /**
     * Get quota summary for all message types
     */
    getQuotaSummary(accessToken: string): Promise<{
        consultation: QuotaAsset[];
        transaction: QuotaAsset[];
        gmf10: QuotaAsset[];
        gmf50: QuotaAsset[];
        gmf100: QuotaAsset[];
    }>;
    /**
     * Validate OA access token by getting OA info
     */
    validateOAToken(accessToken: string): Promise<boolean>;
}

/**
 * User management service for Zalo API
 * Handles all user-related operations with specific endpoints
 */

/**
 * User management service for handling user operations
 * Each method uses specific Zalo API endpoints
 */
declare class UserService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Truy xuất chi tiết người dùng
     * Endpoint: https://openapi.zalo.me/v3.0/oa/user/detail
     * Method: GET with data parameter as JSON string
     *
     * @param accessToken OA access token
     * @param userId User ID to get details for
     * @returns Promise<UserInfo> - Detailed user information
     */
    getUserInfo(accessToken: string, userId: string): Promise<UserInfo>;
    /**
   * Truy xuất chi tiết người dùng (bản dùng POST)
   * Endpoint: https://openapi.zalo.me/v3.0/oa/user/detail
   * Method: POST với body JSON
   *
   * @param accessToken OA access token
   * @param userId ID người dùng cần lấy thông tin
   * @returns Promise<UserInfo> - Thông tin chi tiết người dùng
   */
    postUserInfo(accessToken: string, userId: string): Promise<UserInfo>;
    /**
     * Truy xuất danh sách người dùng
     * Endpoint: https://openapi.zalo.me/v3.0/oa/user/getlist
     * Method: GET with data parameter as JSON string
     *
     * @param accessToken OA access token
     * @param request User list request parameters
     * @returns Promise<UserListResponse> - List of users with pagination info
     */
    getUserList(accessToken: string, request: UserListRequest): Promise<UserListResponse>;
    /**
     * Truy xuất danh sách người dùng (POST method)
     * Endpoint: https://openapi.zalo.me/v3.0/oa/user/getlist
     * Method: POST with data in request body
     *
     * @param accessToken OA access token
     * @param request User list request parameters
     * @returns Promise<UserListResponse> - List of users with pagination info
     */
    postUserList(accessToken: string, request: UserListRequest): Promise<UserListResponse>;
    /**
     * Get all users with automatic pagination
     * Lấy tất cả users với phân trang tự động, đảm bảo lấy 100%
     *
     * @param accessToken Access token của OA
     * @param filters Bộ lọc tùy chọn
     * @returns Promise<UserInfo[]> - Danh sách đầy đủ tất cả users
     */
    getAllUsers(accessToken: string, filters?: {
        tag_name?: string;
        last_interaction_period?: string;
        is_follower?: boolean;
    }): Promise<UserInfo[]>;
    /**
     * Get all user IDs only (without detailed info) - Faster version
     * Chỉ lấy danh sách user ID, không lấy thông tin chi tiết - Nhanh hơn
     *
     * @param accessToken Access token của OA
     * @param filters Bộ lọc tùy chọn
     * @returns Promise<string[]> - Danh sách user IDs
     */
    getAllUserIds(accessToken: string, filters?: {
        tag_name?: string;
        last_interaction_period?: string;
        is_follower?: boolean;
    }): Promise<string[]>;
    /**
     * Get all users with advanced filtering based on UserInfo fields
     * Lấy tất cả users với bộ lọc nâng cao dựa trên các trường của UserInfo
     *
     * @param accessToken Access token của OA
     * @param filters Bộ lọc nâng cao
     * @returns Promise<UserInfo[]> - Danh sách users đã được lọc
     */
    getAllUsersWithAdvancedFilters(accessToken: string, filters?: AdvancedUserFilters): Promise<UserInfo[]>;
    /**
     * Parse date from dd/MM/yyyy format to Date object
     * Helper method cho các filter date
     */
    private parseDate;
    /**
     * Calculate age from date of birth
     * Helper method cho age filter
     */
    private calculateAge;
    /**
     * Cập nhật chi tiết người dùng
     * Endpoint: https://openapi.zalo.me/v3.0/oa/user/update
     * Method: POST
     *
     * @param accessToken OA access token
     * @param request Update user request with user_id and optional fields
     * @returns Promise<boolean> - true if successful
     */
    updateUser(accessToken: string, request: UpdateUserRequest): Promise<boolean>;
    /**
     * Xóa thông tin người dùng
     * Endpoint: https://openapi.zalo.me/v2.0/oa/deletefollowerinfo
     * Method: POST
     *
     * @param accessToken OA access token
     * @param userId User ID to delete info for
     * @returns Promise<boolean> - true if successful
     *
     * Note: Chỉ xóa thông tin lưu trữ ở phía OA, không thay đổi tài khoản Zalo
     */
    deleteUserInfo(accessToken: string, userId: string): Promise<boolean>;
    /**
     * Truy xuất thông tin tùy biến của người dùng
     * Endpoint: https://openapi.zalo.me/v3.0/oa/user/detail/custominfo
     * Method: GET with data parameter as JSON string
     *
     * @param accessToken OA access token
     * @param request Get custom info request with user_id and optional fields_to_export
     * @returns Promise<UserCustomInfoResponse> - Custom info data
     *
     * Note: Tất cả giá trị trong custom_info đều được trả về dưới dạng string
     */
    getUserCustomInfo(accessToken: string, request: GetUserCustomInfoRequest): Promise<UserCustomInfoResponse>;
    /**
     * Cập nhật thông tin tùy biến của người dùng
     * Endpoint: https://openapi.zalo.me/v3.0/oa/user/update/custominfo
     * Method: POST
     *
     * @param accessToken OA access token
     * @param request Update custom info request with user_id and custom_info
     * @returns Promise<boolean> - true if successful
     *
     * Note: Cấu trúc custom_info phụ thuộc vào thiết lập OA
     */
    updateUserCustomInfo(accessToken: string, request: UpdateUserCustomInfoRequest): Promise<boolean>;
    /**
     * Lấy danh sách nhãn
     * Endpoint: https://openapi.zalo.me/v2.0/oa/tag/gettagsofoa
     * Method: GET
     *
     * @param accessToken OA access token
     * @returns Promise<string[]> - Array of tag names
     */
    getLabels(accessToken: string): Promise<string[]>;
    /**
     * Gắn nhãn người dùng
     * Endpoint: https://openapi.zalo.me/v2.0/oa/tag/tagfollower
     *
     * @param accessToken OA access token
     * @param request Tag user request with user_id and tag_name
     * @returns Promise<boolean> - true if successful
     *
     * Note: Nếu tag_name không tồn tại, API sẽ tự động tạo nhãn mới
     */
    addTagToUser(accessToken: string, request: TagUserRequest): Promise<boolean>;
    /**
     * Gỡ nhãn người dùng
     * Endpoint: https://openapi.zalo.me/v2.0/oa/tag/rmfollowerfromtag
     *
     * @param accessToken OA access token
     * @param request Remove tag request with user_id and tag_name
     * @returns Promise<boolean> - true if successful
     */
    removeTagFromUser(accessToken: string, request: RemoveTagFromUserRequest): Promise<boolean>;
    /**
     * Xóa nhãn
     * Endpoint: https://openapi.zalo.me/v2.0/oa/tag/rmtag
     * Method: POST
     *
     * @param accessToken OA access token
     * @param tagName Tag name to delete
     * @returns Promise<boolean> - true if successful
     */
    deleteLabel(accessToken: string, tagName: string): Promise<boolean>;
    /**
     * Get users by tag
     */
    getUsersByTag(accessToken: string, tagName: string, offset?: number, count?: number): Promise<UserListResponse>;
    /**
     * Get followers only
     */
    getFollowers(accessToken: string, offset?: number, count?: number): Promise<UserListResponse>;
    /**
     * Get users by interaction period
     */
    getUsersByInteraction(accessToken: string, period: "TODAY" | "YESTERDAY" | "L7D" | "L30D", offset?: number, count?: number): Promise<UserListResponse>;
    /**
     * Bulk tag operations
     */
    bulkAddTag(accessToken: string, userIds: string[], tagName: string): Promise<{
        success: string[];
        failed: string[];
    }>;
    /**
     * Bulk remove tag operations
     */
    bulkRemoveTag(accessToken: string, userIds: string[], tagName: string): Promise<{
        success: string[];
        failed: string[];
    }>;
    private handleError;
}

interface ZNSAllTemplateDetailsResult {
    error: number;
    message: string;
    data: Array<ZNSTemplateDetails["data"]>;
    metadata: {
        total: number;
    };
}
interface ZNSOAGroupedTemplateDetails {
    accessToken: string;
    templates: Array<ZNSTemplateDetails["data"]>;
    total: number;
}
interface ZNSMultiOAAllTemplateDetailsResult {
    error: number;
    message: string;
    data: ZNSOAGroupedTemplateDetails[];
}
/**
 * Service for Zalo Notification Service (ZNS)
 *
 * Requirements for using ZNS API:
 * - Official Account must be approved and have ZNS sending permission
 * - Valid Official Account access token
 * - ZNS template must be approved before use
 * - Recipient phone number must be in correct format and valid
 * - Template data must match defined parameters
 * - Comply with message quantity limits according to service package
 *
 * Reference: https://developers.zalo.me/docs/zalo-notification-service/
 */
declare class ZNSService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Send ZNS message
     * @param accessToken OA access token
     * @param message ZNS message data
     * @returns Send result
     */
    sendMessage(accessToken: string, message: ZNSMessage): Promise<ZNSSendResult>;
    /**
     * Send ZNS message with phone hash
     * @param accessToken OA access token
     * @param message ZNS message with hashed phone
     * @returns Send result
     */
    sendHashPhoneMessage(accessToken: string, message: ZNSHashPhoneMessage): Promise<ZNSSendResult>;
    /**
     * Send ZNS message in development mode
     * @param accessToken OA access token
     * @param message ZNS dev mode message
     * @returns Send result
     */
    sendDevModeMessage(accessToken: string, message: ZNSDevModeMessage): Promise<ZNSSendResult>;
    /**
     * Send ZNS message with RSA encryption
     * @param accessToken OA access token
     * @param message ZNS RSA message
     * @returns Send result
     */
    sendRsaMessage(accessToken: string, message: ZNSRsaMessage): Promise<ZNSSendResult>;
    /**
     * Send ZNS journey message
     * @param accessToken OA access token
     * @param message ZNS journey message
     * @returns Send result
     */
    sendJourneyMessage(accessToken: string, message: ZNSJourneyMessage): Promise<ZNSSendResult>;
    /**
     * Send ZBS/ZNS Template Message by UID
     * Gửi tin nhắn template theo UID người dùng thay vì số điện thoại
     *
     * @param accessToken OA access token
     * @param message ZNS UID message
     * @returns Send result với message_id và user_id
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/message/template
     *
     * Lưu ý:
     * - App phải đã bật quyền gửi tin và thông báo qua OA
     * - Tin Giao dịch: Sử dụng template Tag 1 (TRANSACTION) hoặc Tag 2 (CUSTOMER_CARE)
     * - Tin Hậu mãi: Sử dụng template Tag 3 (PROMOTION)
     * - Có hạn mức nhận tin theo chính sách của Zalo
     *
     * Example:
     * ```typescript
     * const result = await znsService.sendMessageByUid(accessToken, {
     *   user_id: "3485904339058348590",
     *   template_id: "433555",
     *   template_data: {
     *     customer: "Nguyễn Thị Hoàng Anh",
     *     amount: "100000",
     *     // ... các params khác theo template
     *   }
     * });
     * ```
     */
    sendMessageByUid(accessToken: string, message: ZNSUidMessage): Promise<ZNSUidSendResult>;
    /**
     * Get ZNS message status
     * @param accessToken OA access token
     * @param messageId Message ID
     * @returns Message status info
     *
     * Response data:
     * - delivery_time: Thời gian thiết bị nhận được thông báo
     * - status: Trạng thái thông báo
     *   * -1: The message does not exist
     *   * 0: The message is pushed successfully to Zalo server but has not yet delivered to user's phone
     *   * 1: The message was delivered to the user's phone
     * - message: Mô tả trạng thái thông báo
     */
    getMessageStatus(accessToken: string, messageId: string): Promise<ZNSMessageStatusInfo>;
    /**
     * Get ZNS batch message status (custom implementation)
     * @param accessToken OA access token
     * @param messageIds Array of message IDs
     * @returns Batch message status info
     *
     * This is a custom implementation that calls the single message status API
     * multiple times to get status for multiple messages. Since Zalo API doesn't
     * provide a native batch endpoint, this method provides convenience by:
     * - Making concurrent API calls for better performance
     * - Handling errors gracefully (failed requests return error status)
     * - Returning consistent response format
     */
    getBatchMessageStatus(accessToken: string, messageIds: string[]): Promise<ZNSBatchMessageStatusResponse>;
    /**
     * Get ZNS quota information
     * @param accessToken OA access token
     * @returns Quota information
     *
     * Response data:
     * - dailyQuota: Số thông báo ZNS OA được gửi trong 1 ngày
     * - remainingQuota: Số thông báo ZNS OA được gửi trong ngày còn lại
     * - dailyQuotaPromotion: Số tin ZNS hậu mãi OA được gửi trong ngày (null từ 1/11)
     * - remainingQuotaPromotion: Số tin ZNS hậu mãi còn lại OA được gửi trong ngày (null từ 1/11)
     * - monthlyPromotionQuota: Số tin ZNS hậu mãi OA được gửi trong tháng
     * - remainingMonthlyPromotionQuota: Số tin ZNS hậu mãi còn lại OA được gửi trong tháng
     * - estimatedNextMonthPromotionQuota: Số tin ZNS hậu mãi dự kiến mà OA có thể gửi trong tháng tiếp theo
     */
    getQuotaInfo(accessToken: string): Promise<ZNSQuotaInfo>;
    /**
     * Get ZNS allowed content types
     * @param accessToken OA access token
     * @returns Allowed content types
     *
     * Response data:
     * - Mảng các loại nội dung mà OA có thể gửi:
     *   * "TRANSACTION": Giao dịch (cấp độ 1)
     *   * "CUSTOMER_CARE": Chăm sóc khách hàng (cấp độ 2)
     *   * "PROMOTION": Hậu mãi (cấp độ 3)
     * - Dựa theo chất lượng gửi ZNS của OA, Zalo sẽ tự động điều chỉnh loại nội dung OA được gửi
     */
    getAllowedContentTypes(accessToken: string): Promise<ZNSAllowedContentTypes>;
    /**
     * Get ZNS template list
     * @param accessToken OA access token
     * @param offset Offset for pagination (template được tạo gần nhất có thứ tự 0)
     * @param limit Limit for pagination (tối đa 100)
     * @param status Template status filter (optional)
     * @returns Template list
     *
     * Status values:
     * - 1: ENABLE templates
     * - 2: PENDING_REVIEW templates
     * - 3: REJECT templates
     * - 4: DISABLE templates
     * - undefined: All templates
     */
    getTemplateList(accessToken: string, offset?: number, limit?: number, status?: 1 | 2 | 3 | 4): Promise<ZNSTemplateList>;
    /**
     * Get ZNS template details
     * @param accessToken OA access token
     * @param templateId Template ID
     * @returns Template details
     *
     * Response data:
     * - templateId: ID của template
     * - templateName: Tên của template
     * - status: Trạng thái template (ENABLE, PENDING_REVIEW, DELETE, REJECT, DISABLE)
     * - reason: Lý do template có trạng thái hiện tại
     * - listParams: Danh sách các thuộc tính của template
     * - listButtons: Danh sách các buttons/CTAs của template
     * - timeout: Thời gian timeout của template
     * - previewUrl: Đường dẫn đến bản xem trước của template
     * - templateQuality: Chất lượng template (null từ 10/12)
     * - templateTag: Loại nội dung (TRANSACTION, CUSTOMER_CARE, PROMOTION)
     * - price: Đơn giá của template
     */
    getTemplateDetails(accessToken: string, templateId: string): Promise<ZNSTemplateDetails>;
    /**
     * Custom: Lấy tất cả template kèm thông tin chi tiết cho 1 accessToken
     * - Tự động phân trang để lấy đủ tổng số template
     * - Lấy chi tiết song song theo batch để tối ưu thời gian
     */
    getAllTemplatesWithDetails(accessToken: string, status?: 1 | 2 | 3 | 4): Promise<ZNSAllTemplateDetailsResult>;
    /**
     * Custom: Lấy tất cả template chi tiết cho nhiều accessToken (nhiều OA)
     * Đầu ra nhóm theo từng accessToken
     */
    getAllTemplatesWithDetailsByTokens(accessTokens: string[], status?: 1 | 2 | 3 | 4): Promise<ZNSMultiOAAllTemplateDetailsResult>;
    /**
     * Get ZNS template sample data
     * @param accessToken OA access token
     * @param templateId Template ID
     * @returns Template sample data
     *
     * Response data:
     * - Chứa tham số và dữ liệu mẫu của template
     * - Ví dụ: { "balance_debt": 2000, "due_date": "01/01/1970", "customer_name": "customer_name_sample" }
     */
    getTemplateSampleData(accessToken: string, templateId: string): Promise<ZNSTemplateSampleData>;
    /**
     * Get customer rating information
     * @param accessToken OA access token
     * @param templateId Template ID
     * @param fromTime Start time (timestamp in milliseconds)
     * @param toTime End time (timestamp in milliseconds)
     * @param offset Position of first rating to return
     * @param limit Maximum number of ratings to return
     * @returns Customer rating information
     *
     * Lưu ý:
     * - Chỉ có thể lấy thông tin đánh giá từ template đánh giá dịch vụ được tạo bởi ứng dụng
     * - Access token phải ứng với template ID được tạo bởi app và OA
     * - Thời gian theo định dạng timestamp (millisecond)
     */
    getCustomerRating(accessToken: string, templateId: string, fromTime: number, toTime: number, offset: number, limit: number): Promise<ZNSCustomerRatingResponse>;
    /**
     * Get OA ZNS sending quality information
     * @param accessToken OA access token
     * @returns OA quality information
     *
     * Response data:
     * - oaCurrentQuality: Chất lượng gửi ZNS trong 48 giờ gần nhất
     * - oa7dayQuality: Chất lượng gửi ZNS trong 7 ngày gần nhất
     *
     * Quality levels:
     * - HIGH: Mức độ chất lượng tốt
     * - MEDIUM: Mức độ chất lượng trung bình
     * - LOW: Mức độ chất lượng kém
     * - UNDEFINED: Chưa được xác định (OA không gửi ZNS trong khung thời gian đánh giá)
     */
    getOAQuality(accessToken: string): Promise<ZNSOAQualityInfo>;
    /**
     * Create ZNS template
     * @param accessToken OA access token
     * @param templateData Template creation data theo chuẩn Zalo API
     * @returns Created template response
     *
     * API: POST https://business.openapi.zalo.me/template/create
     */
    createTemplate(accessToken: string, templateData: ZNSCreateTemplateRequest): Promise<ZNSTemplateCreateEditResponse>;
    /**
     * Edit ZNS template (chỉnh sửa template có trạng thái REJECT)
     * @param accessToken OA access token
     * @param templateData Template edit data theo chuẩn Zalo API
     * @returns Edited template response
     *
     * Lưu ý:
     * - Chỉ có thể chỉnh sửa template có trạng thái REJECT
     * - Template sau khi chỉnh sửa sẽ chuyển về trạng thái PENDING_REVIEW
     * - Daily quota: 100 requests/ngày
     * - Cần quyền "Quản lý tài sản"
     *
     * API: POST https://business.openapi.zalo.me/template/edit
     */
    updateTemplate(accessToken: string, templateData: ZNSUpdateTemplateRequest): Promise<ZNSTemplateCreateEditResponse>;
    /**
     * Upload image for ZNS template
     * @param accessToken OA access token
     * @param imageFile Image file (Buffer or ReadableStream)
     * @param filename Filename with extension
     * @returns Upload result with media_id
     *
     * API: POST https://business.openapi.zalo.me/upload/image
     */
    uploadImage(accessToken: string, imageFile: Buffer | NodeJS.ReadableStream, filename?: string): Promise<ZNSUploadImageResult>;
    /**
     * Hàm validate file ảnh / logo ZNS độc lập để Backend / AI Agent gọi kiểm tra trước khi upload
     * Trả về kết quả đánh giá (isValid, errors, warnings) thay vì throw exception
     * @param filename Tên file kèm đuôi (.jpg, .jpeg, .png)
     * @param fileSize Dung lượng file theo byte
     * @param imageType Loại ảnh ("LOGO" hoặc "IMAGE")
     * @param dimensions Kích thước ảnh (chiều rộng x chiều cao, optional)
     */
    validateZnsImage(filename: string, fileSize: number, imageType?: "LOGO" | "IMAGE", dimensions?: {
        width?: number;
        height?: number;
    }): ZNSValidationResult;
    /**
     * Comprehensive validation for ZNS template request
     * Trả về kết quả đánh giá (isValid, errors, warnings) thay vì throw exception để Backend và AI Agent phân tích
     */
    validateZNSTemplateRequest(templateData: ZNSCreateTemplateRequest): ZNSValidationResult;
    /**
     * Validate basic required fields
     */
    private validateBasicFields;
    /**
     * Validate template_type and tag compatibility
     */
    private validateTemplateTypeTagCompatibility;
    /**
     * Validate layout structure based on template type
     */
    private validateLayoutStructure;
    /**
     * Check if component belongs to header
     */
    private isHeaderComponent;
    /**
     * Check if component belongs to body
     */
    private isBodyComponent;
    /**
     * Check if component belongs to footer
     */
    private isFooterComponent;
    /**
     * Validate custom template layout (type 1)
     */
    private validateCustomTemplateLayout;
    /**
     * Validate auth template layout (type 2)
     */
    private validateAuthTemplateLayout;
    /**
     * Validate payment template layout (type 3)
     */
    private validatePaymentTemplateLayout;
    /**
     * Validate voucher template layout (type 4)
     */
    private validateVoucherTemplateLayout;
    /**
     * Validate rating template layout (type 5)
     */
    private validateRatingTemplateLayout;
    /**
     * Validate individual component content
     */
    private validateComponent;
    /**
     * Validate TITLE component
     */
    private validateTitleComponent;
    /**
     * Validate PARAGRAPH component
     */
    private validateParagraphComponent;
    /**
     * Validate OTP component
     */
    private validateOTPComponent;
    /**
     * Validate TABLE component
     */
    private validateTableComponent;
    /**
     * Validate LOGO component
     */
    private validateLogoComponent;
    /**
     * Validate IMAGES component
     */
    private validateImagesComponent;
    /**
     * Validate BUTTONS component
     */
    private validateButtonsComponent;
    /**
     * Validate PAYMENT component
     */
    private validatePaymentComponent;
    /**
     * Validate VOUCHER component
     */
    private validateVoucherComponent;
    /**
     * Validate RATING component
     */
    private validateRatingComponent;
    /**
     * Validate attachment object
     */
    private validateAttachment;
    /**
     * Validate params array
     */
    private validateParams;
    /**
     * Validate yêu cầu tham số định danh theo Tag (theo quy định kiểm duyệt của Zalo ZNS):
     * - Tag 1 (GIAO DỊCH) & Tag 2 (CHĂM SÓC KHÁCH HÀNG):
     *   BẮT BUỘC có tên khách hàng (type "1") và ít nhất 1 tham số định danh giao dịch (type "4" - Mã số,
     *   "6" - Trạng thái GD, "10" - SL/Số tiền, "11" - Thời gian, "14" - Tiền tệ).
     *   Trường hợp không có tên khách hàng do bất khả kháng: Bắt buộc có tổng cộng ít nhất 3 tham số định danh.
     */
    private validateTagParametersRequirement;
    /**
     * Validate quy định nút bấm (CTA) cho Tag 3 (HẬU MÃI / PROMOTION):
     * - Nếu có nút gọi điện thoại (type 2 - PHONE): Phải là đầu số tổng đài 1800 hoặc 1900.
     *   Trường hợp dùng số khác thì bắt buộc phải có giải trình trong trường note.
     */
    private validatePromotionButtons;
    private handleZNSError;
}

/**
 * Interface cho từng tin nhắn trong danh sách tin nhắn group
 */
interface GroupMessageItem {
    /** Loại tin nhắn */
    type: "text" | "image" | "file" | "sticker" | "mention";
    /** Nội dung tin nhắn (cho text/mention message) */
    text?: string;
    /** URL hình ảnh (cho image message) */
    imageUrl?: string;
    /** Attachment ID (cho image message) */
    attachmentId?: string;
    /** Caption cho hình ảnh (tối đa 2000 ký tự) */
    caption?: string;
    /** Token file (cho file message) */
    fileToken?: string;
    /** Tên file (cho file message) */
    fileName?: string;
    /** Sticker ID (cho sticker message) */
    stickerId?: string;
    /** Danh sách mentions (cho mention message) */
    mentions?: Array<{
        user_id: string;
        offset: number;
        length: number;
    }>;
    /** Delay sau khi gửi tin nhắn này (milliseconds) */
    delay?: number;
}
/**
 * Interface cho request gửi danh sách tin nhắn tới 1 group
 */
interface SendMessageListToGroupRequest {
    /** Access token của Official Account */
    accessToken: string;
    /** Group ID */
    groupId: string;
    /** Danh sách tin nhắn */
    messages: GroupMessageItem[];
    /** Delay mặc định giữa các tin nhắn (milliseconds) */
    defaultDelay?: number;
    /** Callback function để tracking tiến trình */
    onProgress?: (progress: GroupMessageProgressInfo) => void;
}
/**
 * Interface cho thông tin tiến trình từng tin nhắn
 */
interface GroupMessageProgressInfo {
    /** Group ID */
    groupId: string;
    /** Index của tin nhắn trong danh sách (bắt đầu từ 0) */
    messageIndex: number;
    /** Tổng số tin nhắn */
    totalMessages: number;
    /** Loại tin nhắn */
    messageType: string;
    /** Trạng thái: 'started' | 'completed' | 'failed' */
    status: 'started' | 'completed' | 'failed';
    /** Kết quả gửi tin nhắn (nếu đã hoàn thành) */
    result?: GroupMessageResult;
    /** Thông tin lỗi (nếu thất bại) */
    error?: string;
    /** Thời gian bắt đầu */
    startTime: number;
    /** Thời gian kết thúc (nếu đã hoàn thành) */
    endTime?: number;
}
/**
 * Interface cho response gửi danh sách tin nhắn tới 1 group
 */
interface SendMessageListToGroupResponse {
    /** Group ID */
    groupId: string;
    /** Tổng số tin nhắn */
    totalMessages: number;
    /** Số tin nhắn gửi thành công */
    successfulMessages: number;
    /** Số tin nhắn gửi thất bại */
    failedMessages: number;
    /** Chi tiết kết quả từng tin nhắn */
    messageResults: Array<{
        /** Index của tin nhắn trong danh sách */
        messageIndex: number;
        /** Loại tin nhắn */
        messageType: string;
        /** Trạng thái gửi */
        success: boolean;
        /** Kết quả chi tiết */
        result?: GroupMessageResult;
        /** Thông tin lỗi nếu thất bại */
        error?: string;
        /** Thời gian bắt đầu gửi */
        startTime: number;
        /** Thời gian kết thúc */
        endTime: number;
        /** Thời gian thực hiện (milliseconds) */
        duration: number;
    }>;
    /** Tổng thời gian thực hiện (milliseconds) */
    totalDuration: number;
}
/**
 * Interface cho request gửi danh sách tin nhắn tới nhiều groups
 */
interface SendMessageListToMultipleGroupsRequest {
    /** Access token của Official Account */
    accessToken: string;
    /** Danh sách group IDs */
    groupIds: string[];
    /** Danh sách tin nhắn */
    messages: GroupMessageItem[];
    /** Delay mặc định giữa các tin nhắn (milliseconds) */
    defaultDelay?: number;
    /** Delay giữa các groups (milliseconds) để tránh rate limit */
    delayBetweenGroups?: number;
    /** Callback function để tracking tiến trình */
    onProgress?: (progress: MultipleGroupsProgressInfo) => void;
}
/**
 * Interface cho thông tin tiến trình từng group
 */
interface MultipleGroupsProgressInfo {
    /** Group ID hiện tại */
    groupId: string;
    /** Index của group trong danh sách (bắt đầu từ 0) */
    groupIndex: number;
    /** Tổng số groups */
    totalGroups: number;
    /** Trạng thái: 'started' | 'completed' | 'failed' */
    status: 'started' | 'completed' | 'failed';
    /** Kết quả gửi tin nhắn cho group này (nếu đã hoàn thành) */
    result?: SendMessageListToGroupResponse;
    /** Thông tin lỗi (nếu thất bại) */
    error?: string;
    /** Thời gian bắt đầu */
    startTime: number;
    /** Thời gian kết thúc (nếu đã hoàn thành) */
    endTime?: number;
}
/**
 * Interface cho response gửi danh sách tin nhắn tới nhiều groups
 */
interface SendMessageListToMultipleGroupsResponse {
    /** Tổng số groups */
    totalGroups: number;
    /** Số groups gửi thành công */
    successfulGroups: number;
    /** Số groups gửi thất bại */
    failedGroups: number;
    /** Chi tiết kết quả từng group */
    groupResults: Array<{
        /** Group ID */
        groupId: string;
        /** Index của group trong danh sách */
        groupIndex: number;
        /** Trạng thái gửi cho group này */
        success: boolean;
        /** Kết quả chi tiết gửi tin nhắn */
        messageListResult?: SendMessageListToGroupResponse;
        /** Thông tin lỗi nếu thất bại */
        error?: string;
        /** Thời gian bắt đầu gửi */
        startTime: number;
        /** Thời gian kết thúc */
        endTime: number;
        /** Thời gian thực hiện (milliseconds) */
        duration: number;
    }>;
    /** Tổng thời gian thực hiện (milliseconds) */
    totalDuration: number;
    /** Thống kê tin nhắn tổng cộng */
    messageStats: {
        /** Tổng số tin nhắn đã gửi thành công */
        totalSuccessfulMessages: number;
        /** Tổng số tin nhắn thất bại */
        totalFailedMessages: number;
        /** Tổng số tin nhắn */
        totalMessages: number;
    };
}
/**
 * Interface cho tin nhắn cá nhân hóa cho từng group
 */
interface PersonalizedGroupMessage {
    /** Group ID */
    groupId: string;
    /** Danh sách tin nhắn riêng cho group này */
    messages: GroupMessageItem[];
}
/**
 * Interface cho request gửi tin nhắn cá nhân hóa tới nhiều groups
 */
interface SendPersonalizedMessageToMultipleGroupsRequest {
    /** Access token của Official Account */
    accessToken: string;
    /** Danh sách tin nhắn cá nhân hóa cho từng group */
    personalizedMessages: PersonalizedGroupMessage[];
    /** Delay mặc định giữa các tin nhắn (milliseconds) */
    defaultDelay?: number;
    /** Delay giữa các groups (milliseconds) để tránh rate limit */
    delayBetweenGroups?: number;
    /** Callback function để tracking tiến trình */
    onProgress?: (progress: PersonalizedGroupsProgressInfo) => void;
}
/**
 * Interface cho thông tin tiến trình gửi tin nhắn cá nhân hóa
 */
interface PersonalizedGroupsProgressInfo {
    /** Group ID hiện tại */
    groupId: string;
    /** Index của group trong danh sách (bắt đầu từ 0) */
    groupIndex: number;
    /** Tổng số groups */
    totalGroups: number;
    /** Trạng thái: 'started' | 'completed' | 'failed' */
    status: 'started' | 'completed' | 'failed';
    /** Kết quả gửi tin nhắn cho group này (nếu đã hoàn thành) */
    result?: SendMessageListToGroupResponse;
    /** Thông tin lỗi (nếu thất bại) */
    error?: string;
    /** Thời gian bắt đầu */
    startTime: number;
    /** Thời gian kết thúc (nếu đã hoàn thành) */
    endTime?: number;
}
/**
 * Interface cho response gửi tin nhắn cá nhân hóa tới nhiều groups
 */
interface SendPersonalizedMessageToMultipleGroupsResponse {
    /** Tổng số groups */
    totalGroups: number;
    /** Số groups gửi thành công */
    successfulGroups: number;
    /** Số groups gửi thất bại */
    failedGroups: number;
    /** Chi tiết kết quả từng group */
    groupResults: Array<{
        /** Group ID */
        groupId: string;
        /** Index của group trong danh sách */
        groupIndex: number;
        /** Trạng thái gửi cho group này */
        success: boolean;
        /** Kết quả chi tiết gửi tin nhắn */
        messageListResult?: SendMessageListToGroupResponse;
        /** Thông tin lỗi nếu thất bại */
        error?: string;
        /** Thời gian bắt đầu gửi */
        startTime: number;
        /** Thời gian kết thúc */
        endTime: number;
        /** Thời gian thực hiện (milliseconds) */
        duration: number;
    }>;
    /** Tổng thời gian thực hiện (milliseconds) */
    totalDuration: number;
    /** Thống kê tin nhắn tổng cộng */
    messageStats: {
        /** Tổng số tin nhắn đã gửi thành công */
        totalSuccessfulMessages: number;
        /** Tổng số tin nhắn thất bại */
        totalFailedMessages: number;
        /** Tổng số tin nhắn */
        totalMessages: number;
    };
}
/**
 * Service for handling Zalo Official Account Group Message Framework (GMF) APIs
 *
 * CONDITIONS FOR USING ZALO GMF:
 *
 * 1. OPT-IN CONDITIONS FOR SENDING GROUP MESSAGES:
 *    - OA must have the group_id of the chat group
 *    - OA must be added to the chat group by group admin
 *    - OA must have permission to send messages in the group (granted by group admin)
 *    - Chat group must be active (not locked or disbanded)
 *
 * 2. ACCESS PERMISSIONS:
 *    - Application needs to be granted group chat management permissions
 *    - Access token must have "manage_group" scope or equivalent
 *    - OA must be authenticated and have active status
 *
 * 3. LIMITS AND CONSTRAINTS:
 *    - Can only send messages to groups that OA has joined
 *    - Cannot send messages to private groups that OA hasn't been invited to
 *    - Must comply with Zalo's message sending frequency limits
 *    - Message content must comply with Zalo's content policy
 *
 * 4. SUPPORTED MESSAGE TYPES:
 *    - Text message: Plain text messages
 *    - Image message: Image messages (JPG, PNG, GIF - max 5MB)
 *    - File message: File attachments (max 25MB)
 *    - Sticker message: Stickers from Zalo collection
 *    - Mention message: Tag/mention specific members
 *
 * 5. TECHNICAL REQUIREMENTS:
 *    - Use HTTPS for all API calls
 *    - Content-Type: application/json for text/mention messages
 *    - Content-Type: multipart/form-data for file/image uploads
 */
declare class GroupMessageService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Send text message to group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param message Text message content
     * @returns Send result
     */
    sendTextMessage(accessToken: string, groupId: string, message: GroupTextMessage): Promise<GroupMessageResult>;
    /**
     * Send image message to group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param message Image message content
     * @returns Send result
     */
    sendImageMessage(accessToken: string, groupId: string, message: GroupImageMessage): Promise<GroupMessageResult>;
    /**
     * Send file message to group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param message File message content
     * @returns Send result
     */
    sendFileMessage(accessToken: string, groupId: string, message: GroupFileMessage): Promise<GroupMessageResult>;
    /**
     * Send sticker message to group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param message Sticker message content
     * @returns Send result
     */
    sendStickerMessage(accessToken: string, groupId: string, message: GroupStickerMessage): Promise<GroupMessageResult>;
    /**
     * Send mention message to group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param message Mention message content
     * @returns Send result
     */
    sendMentionMessage(accessToken: string, groupId: string, message: GroupMentionMessage): Promise<GroupMessageResult>;
    /**
     * Get group information
     * @param accessToken OA access token
     * @param groupId Group ID
     * @returns Group information
     */
    getGroupInfo(accessToken: string, groupId: string): Promise<GroupInfo>;
    /**
     * Get group members
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param offset Offset for pagination
     * @param count Number of members to retrieve
     * @returns Group members list
     */
    getGroupMembers(accessToken: string, groupId: string, offset?: number, count?: number): Promise<{
        members: GroupMember[];
        total: number;
    }>;
    /**
     * Gửi danh sách tin nhắn tới 1 group với delay tùy chỉnh
     * Hỗ trợ tất cả các loại tin nhắn: text, image, file, sticker, mention
     *
     * @param request Thông tin request gửi danh sách tin nhắn
     * @returns Kết quả gửi từng tin nhắn
     */
    sendMessageListToGroup(request: SendMessageListToGroupRequest): Promise<SendMessageListToGroupResponse>;
    private handleGroupMessageError;
    /**
     * Gửi danh sách tin nhắn tới nhiều groups với callback tracking
     *
     * @param request Thông tin request gửi danh sách tin nhắn tới nhiều groups
     * @returns Kết quả gửi tin nhắn cho tất cả groups
     */
    sendMessageListToMultipleGroups(request: SendMessageListToMultipleGroupsRequest): Promise<SendMessageListToMultipleGroupsResponse>;
    /**
     * Gửi tin nhắn cá nhân hóa tới nhiều groups - mỗi group có bộ tin nhắn riêng
     *
     * @param request Thông tin request gửi tin nhắn cá nhân hóa tới nhiều groups
     * @returns Kết quả gửi tin nhắn cho tất cả groups
     */
    sendPersonalizedMessageToMultipleGroups(request: SendPersonalizedMessageToMultipleGroupsRequest): Promise<SendPersonalizedMessageToMultipleGroupsResponse>;
    /**
     * Utility method để sleep/delay
     * @param ms Thời gian delay tính bằng milliseconds
     */
    private sleep;
}

/**
 * Service for handling Zalo Official Account Group Management Framework (GMF) APIs
 *
 * CONDITIONS FOR USING ZALO GMF GROUP MANAGEMENT:
 *
 * 1. GENERAL CONDITIONS:
 *    - OA must be granted permission to use GMF (Group Message Framework) feature
 *    - Access token must have "manage_group" and "group_message" scopes
 *    - OA must have active status and be verified
 *    - Must comply with limits on number of groups and members
 *
 * 2. CREATE NEW GROUP:
 *    - Group name: required, max 100 characters, no special characters
 *    - Description: optional, max 500 characters
 *    - Avatar: optional, JPG/PNG format, max 5MB
 *    - Initial members: max 200 people, must be users who have interacted with OA
 *    - OA automatically becomes admin of the group
 *
 * 3. MEMBER MANAGEMENT:
 *    - Only admins can invite/remove members
 *    - Invite members: max 50 people per time, users must have interacted with OA
 *    - Remove members: cannot remove other admins, must have at least 1 admin
 *    - Members can leave group themselves
 *
 * 4. ADMIN MANAGEMENT:
 *    - Only current admins can add/remove other admins
 *    - Must have at least 1 admin in group
 *    - OA always has admin rights and cannot be removed
 *
 * 5. LIMITS AND CONSTRAINTS:
 *    - Maximum groups: according to service package (usually 10-100 groups)
 *    - Maximum members per group: 200 people
 *    - Group creation frequency: max 10 groups/day
 *    - Member invitation frequency: max 500 invitations/day
 */
declare class GroupManagementService {
    private readonly client;
    private readonly userService?;
    private readonly endpoints;
    constructor(client: ZaloClient, userService?: UserService);
    /**
     * Create GroupManagementService with UserService for enhanced member details
     * @param client ZaloClient instance
     * @param userService UserService instance for fetching detailed user info
     * @returns GroupManagementService with enhanced capabilities
     */
    static withUserService(client: ZaloClient, userService: UserService): GroupManagementService;
    /**
     * Create basic GroupManagementService without UserService
     * @param client ZaloClient instance
     * @returns GroupManagementService with basic capabilities only
     */
    static basic(client: ZaloClient): GroupManagementService;
    /**
     * Create new group chat with asset_id
     * @param accessToken OA access token
     * @param groupData Group information to create
     * @returns Created group information
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/creategroupwithoa
     */
    createGroup(accessToken: string, groupData: GroupCreateRequest): Promise<GroupCreateResult>;
    /**
     * Helper method to extract group data from create response
     * @param response Full API response
     * @returns Group data only
     */
    extractGroupData(response: GroupCreateResult): GroupCreateData;
    /**
     * Helper method to extract group info from update response
     * @param response Full API response
     * @returns Group info only
     */
    extractGroupInfo(response: GroupUpdateResult): {
        group_id: string;
        group_link: string;
        name: string;
        group_description: string;
        avatar: string;
        status: "enabled" | "disabled";
        total_member: number;
        max_member: string;
        auto_delete_date: string;
    };
    /**
     * Helper method to extract group settings from update response
     * @param response Full API response
     * @returns Group settings only
     */
    extractGroupSettings(response: GroupUpdateResult): {
        lock_send_msg: boolean;
        join_appr: boolean;
        enable_msg_history: boolean;
        enable_link_join: boolean;
    };
    /**
     * Helper method to extract asset info from update response
     * @param response Full API response
     * @returns Asset info only
     */
    extractAssetInfo(response: GroupUpdateResult): {
        asset_type: "gmf10" | "gmf50" | "gmf100";
        asset_id: string;
        valid_through: string;
        auto_renew: string;
    };
    /**
     * Update group asset (upgrade package or extend expiration)
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param assetId New asset ID for the group
     * @returns Update result with full group information
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/updateasset
     *
     * Use cases:
     * - Increase member limit for the group
     * - Extend group expiration when expired
     */
    updateGroupAsset(accessToken: string, groupId: string, assetId: string): Promise<GroupUpdateResult>;
    /**
     * Update group asset (upgrade package or extend expiration) - Object parameter version
     * @param accessToken OA access token
     * @param updateData Asset update data
     * @returns Update result with full group information
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/updateasset
     */
    updateGroupAsset(accessToken: string, updateData: GroupAssetUpdateRequest): Promise<GroupUpdateResult>;
    /**
     * Get detailed group information
     * @param accessToken OA access token
     * @param groupId Group ID
     * @returns Detailed group information including group_info, asset_info and group_setting
     *
     * API: GET https://openapi.zalo.me/v3.0/oa/group/getgroup
     */
    getGroupInfo(accessToken: string, groupId: string): Promise<GroupDetailResponse>;
    /**
     * Update group information
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param updateData Information to update
     * @returns Update result with full group information
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/updateinfo
     */
    updateGroupInfo(accessToken: string, groupId: string, updateData: GroupUpdateRequest): Promise<GroupUpdateResult>;
    /**
     * Update group avatar
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param avatarData New avatar information
     * @returns Update result
     * @deprecated Use updateGroupInfo() with group_avatar field instead
     */
    updateGroupAvatar(accessToken: string, groupId: string, avatarData: GroupAvatarUpdateRequest): Promise<{
        success: boolean;
    }>;
    /**
     * Invite members to group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param inviteData List of user IDs to invite
     * @returns Invitation result
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/invite
     */
    inviteMembers(accessToken: string, groupId: string, inviteData: GroupMemberInviteRequest): Promise<GroupInviteResult>;
    /**
     * Invite members to group - Array parameter version
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param memberUserIds Array of user IDs to invite
     * @returns Invitation result
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/invite
     */
    inviteMembers(accessToken: string, groupId: string, memberUserIds: string[]): Promise<GroupInviteResult>;
    /**
     * Get list of pending members
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param offset Offset for pagination (default: 0)
     * @param count Maximum number to return (default: 20, max: 50)
     * @returns List of pending members
     */
    getPendingMembers(accessToken: string, groupId: string, offset?: number, count?: number): Promise<GroupPendingMembersResponse>;
    /**
     * Accept pending members to group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param memberUserIds List of user IDs to accept
     * @returns Accept result
     */
    acceptPendingMembers(accessToken: string, groupId: string, memberUserIds: string[]): Promise<GroupAcceptPendingMembersResponse>;
    /**
     * Reject pending members from group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param memberUserIds List of user IDs to reject
     * @returns Reject result
     */
    rejectPendingMembers(accessToken: string, groupId: string, memberUserIds: string[]): Promise<GroupAcceptPendingMembersResponse>;
    /**
     * Remove members from group
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param memberUserIds List of user IDs to remove
     * @returns Remove result
     */
    removeMembers(accessToken: string, groupId: string, memberUserIds: string[]): Promise<GroupRemoveMembersResponse>;
    /**
     * Add admin rights to members - Array parameter version
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param memberUserIds Array of user IDs to add admin rights
     * @returns Add admin result
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/addadmins
     */
    addAdmins(accessToken: string, groupId: string, memberUserIds: string[]): Promise<{
        error: number;
        message: string;
    }>;
    /**
     * Remove admin rights from members - Array parameter version
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param memberUserIds Array of user IDs to remove admin rights
     * @returns Remove admin result
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/group/removeadmins
     */
    removeAdmins(accessToken: string, groupId: string, memberUserIds: string[]): Promise<{
        error: number;
        message: string;
    }>;
    /**
     * Delete group chat (Disband group)
     * @param accessToken OA access token
     * @param groupId Group ID to delete
     * @returns Delete result
     */
    deleteGroup(accessToken: string, groupId: string): Promise<{
        error: number;
        message: string;
    }>;
    /**
     * Get list of OA groups
     * @param accessToken OA access token
     * @param offset Offset for pagination (default: 0)
     * @param count Maximum number to return (default: 5, max: 50)
     * @returns List of OA groups
     *
     * API: GET https://openapi.zalo.me/v3.0/oa/group/getgroupsofoa
     */
    getGroupsOfOA(accessToken: string, offset?: number, count?: number): Promise<GroupsOfOAResponse>;
    /**
     * Get ALL groups of OA (custom function with automatic pagination)
     * @param accessToken OA access token
     * @param maxGroups Maximum number of groups to retrieve (default: unlimited)
     * @param progressCallback Optional callback to track progress
     * @returns All OA groups with pagination info
     *
     * This function automatically handles pagination to retrieve all groups,
     * not limited by the 50-group API limit per request.
     */
    getAllGroupsOfOA(accessToken: string, maxGroups?: number, progressCallback?: (progress: {
        currentCount: number;
        totalCount?: number;
        percentage?: number;
        isComplete: boolean;
    }) => void): Promise<AllGroupsOfOAResponse>;
    /**
     * Get ALL groups of OA with simple interface (no progress tracking)
     * @param accessToken OA access token
     * @param maxGroups Maximum number of groups to retrieve (optional)
     * @returns Array of all OA groups
     *
     * Simplified version of getAllGroupsOfOA that returns just the groups array
     */
    getAllGroupsOfOASimple(accessToken: string, maxGroups?: number): Promise<OAGroupItem[]>;
    /**
     * Get group quota information and asset_id
     * @param accessToken OA access token
     * @param productType Product type (optional)
     * @param quotaType Quota type (optional)
     * @returns Group quota information including asset_id
     *
     * API: POST https://openapi.zalo.me/v3.0/oa/quota/group
     */
    getGroupQuota(accessToken: string, productType?: GMFProductType, quotaType?: QuotaType): Promise<GroupQuotaMessageResponse>;
    /**
     * Get asset_id for creating GMF group
     * @param accessToken OA access token
     * @returns Asset_id for group creation
     */
    getAssetId(accessToken: string): Promise<string>;
    /**
     * Get list of asset_ids available for creating GMF groups
     * @param accessToken OA access token
     * @returns List of asset_ids and quota information
     */
    getAssetIds(accessToken: string): Promise<GroupQuotaMessageResponse>;
    /**
     * Get list of recent chats
     * @param accessToken OA access token
     * @param offset Offset for pagination (default: 0)
     * @param count Maximum number to return (default: 5, max: 50)
     * @returns List of recent chats
     *
     * API: GET https://openapi.zalo.me/v3.0/oa/group/listrecentchat
     */
    getRecentChats(accessToken: string, offset?: number, count?: number): Promise<RecentChatsResponse>;
    /**
     * Lấy thông tin tin nhắn trong một nhóm
     *
     * Lưu ý: Ứng dụng cần được cấp quyền quản lý thông tin nhóm.
     *
     * @param accessToken Access token của Official Account
     * @param groupId ID nhóm muốn query
     * @param offset Offset muốn query (mặc định: 0)
     * @param count Số lượng mong muốn query (mặc định: 5)
     * @returns Lịch sử tin nhắn trong nhóm
     *
     * @example
     * ```typescript
     * const messages = await groupService.getGroupConversation(
     *   accessToken,
     *   'f414c8f76fa586fbdfb4',
     *   0,
     *   2
     * );
     * ```
     *
     * API: GET https://openapi.zalo.me/v3.0/oa/group/conversation
     */
    getGroupConversation(accessToken: string, groupId: string, offset?: number, count?: number): Promise<GroupConversationResponse>;
    /**
     * Get group members list from Zalo API
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param offset Offset for pagination (default: 0)
     * @param count Maximum number to return (default: 5, max: 50)
     * @returns Group members list
     */
    getGroupMembers(accessToken: string, groupId: string, offset?: number, count?: number): Promise<GroupMembersResponse>;
    /**
     * Find a specific group member by user ID
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param userId User ID to search for
     * @returns GroupMemberInfo if found, null otherwise
     *
     * Note: This method iterates through pages of members to find the user.
     */
    getGroupMemberByUserId(accessToken: string, groupId: string, userId: string): Promise<GroupMemberInfo | null>;
    /**
     * Get ALL members of a group (custom function with automatic pagination)
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param maxMembers Maximum number of members to retrieve (default: unlimited)
     * @param progressCallback Optional callback to track progress
     * @returns All group members with pagination info
     *
     * This function automatically handles pagination to retrieve all members,
     * not limited by the 50-member API limit per request.
     */
    getAllGroupMembers(accessToken: string, groupId: string, maxMembers?: number, progressCallback?: (progress: GetAllMembersProgress) => void): Promise<AllGroupMembersResponse>;
    /**
     * Get ALL members of a group with simple interface (no progress tracking)
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param maxMembers Maximum number of members to retrieve (optional)
     * @returns Array of all group members
     *
     * Simplified version of getAllGroupMembers that returns just the members array
     */
    getAllGroupMembersSimple(accessToken: string, groupId: string, maxMembers?: number): Promise<GroupMemberInfo[]>;
    /**
     * Get ALL members of a group with DETAILED user information (advanced API)
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param maxMembers Maximum number of members to retrieve (default: unlimited)
     * @param progressCallback Optional callback to track progress
     * @param options Advanced options for fetching details
     * @returns All group members with detailed user information
     *
     * This function:
     * 1. First gets all basic member info using getAllGroupMembers()
     * 2. Then fetches detailed user info for each member using UserService.getUserInfo()
     * 3. Combines both basic and detailed info into EnhancedGroupMember objects
     * 4. If detailed info fails to fetch, falls back to basic info only
     * 5. Provides detailed progress tracking and statistics
     */
    getAllGroupMembersWithDetails(accessToken: string, groupId: string, maxMembers?: number, progressCallback?: (progress: GetAllMembersWithDetailsProgress) => void, options?: {
        /** Batch size for fetching user details (default: 10) */
        detailBatchSize?: number;
        /** Timeout for each user detail request in ms (default: 5000) */
        detailTimeout?: number;
        /** Whether to continue if UserService is not available (default: true) */
        continueWithoutUserService?: boolean;
        /** Maximum concurrent detail requests (default: 5) */
        maxConcurrentRequests?: number;
    }): Promise<AllGroupMembersWithDetailsResponse>;
    /**
     * Get ALL members of a group with detailed info - Simple interface
     * @param accessToken OA access token
     * @param groupId Group ID
     * @param maxMembers Maximum number of members to retrieve (optional)
     * @returns Array of enhanced group members with detailed info
     *
     * Simplified version that returns just the enhanced members array
     */
    getAllGroupMembersWithDetailsSimple(accessToken: string, groupId: string, maxMembers?: number): Promise<EnhancedGroupMember[]>;
    private handleGroupManagementError;
}

/**
 * Service for handling Zalo Official Account Article Management APIs
 *
 * CONDITIONS FOR USING ZALO ARTICLE MANAGEMENT:
 *
 * 1. GENERAL CONDITIONS:
 *    - OA must have permission to create articles
 *    - Access token must have "manage_article" scope
 *    - OA must have active status and be verified
 *
 * 2. ARTICLE CREATION:
 *    - Title: required, max 150 characters
 *    - Author: required for normal articles, max 50 characters
 *    - Description: required, max 300 characters
 *    - Image size: max 1MB per image
 *    - Status: 'show' (publish immediately) or 'hide' (draft)
 *    - Comment: 'show' (allow comments) or 'hide' (disable comments)
 *
 * 3. CONTENT VALIDATION:
 *    - Normal articles: must have cover, body content, and author
 *    - Video articles: must have video_id and avatar
 *    - Body items: support text, image, video, and product types
 *    - Tracking links: must be valid URLs
 *
 * 4. LIMITS AND CONSTRAINTS:
 *    - Article list: max 10 items per request
 *    - Processing time: use token to check progress
 *    - Update frequency: reasonable intervals recommended
 */
declare class ArticleService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Create normal article with rich content
     * @param accessToken OA access token
     * @param request Normal article information
     * @returns Token for tracking creation progress
     */
    createNormalArticle(accessToken: string, request: ArticleNormalRequest): Promise<ArticleResponse>;
    /**
     * Create video article
     * @param accessToken OA access token
     * @param request Video article information
     * @returns Token for tracking creation progress
     */
    createVideoArticle(accessToken: string, request: ArticleVideoRequest): Promise<ArticleResponse>;
    /**
     * Create article (automatically detect type)
     * @param accessToken OA access token
     * @param request Article information
     * @returns Token for tracking creation progress
     */
    createArticle(accessToken: string, request: ArticleRequest): Promise<ArticleResponse>;
    /**
     * Check article creation progress
     * @param accessToken OA access token
     * @param token Token from article creation response
     * @returns Progress information
     */
    checkArticleProcess(accessToken: string, token: string): Promise<ArticleProcessResponse>;
    /**
     * Verify article and get article ID
     * @param accessToken OA access token
     * @param token Token from article creation or update response
     * @returns Article ID
     */
    verifyArticle(accessToken: string, token: string): Promise<ArticleVerifyResponse>;
    /**
     * Get article details
     * @param accessToken OA access token
     * @param articleId Article ID
     * @returns Article details
     */
    getArticleDetail(accessToken: string, articleId: string): Promise<ArticleDetail>;
    /**
     * Get article list
     * @param accessToken OA access token
     * @param request List parameters
     * @returns Article list
     */
    getArticleList(accessToken: string, request: ArticleListRequest): Promise<ArticleListResponse>;
    /**
     * Get all articles by automatically fetching all pages
     *
     * @example
     * ```typescript
     * // Get all normal articles with progress tracking
     * const result = await articleService.getAllArticles(accessToken, "normal", {
     *   batchSize: 50,
     *   maxArticles: 1000,
     *   onProgress: (progress) => {
     *     console.log(`Batch ${progress.currentBatch}: ${progress.totalFetched} articles fetched`);
     *   }
     * });
     *
     * console.log(`Total: ${result.totalFetched} articles in ${result.totalBatches} batches`);
     * console.log(`Has more: ${result.hasMore}`);
     * ```
     *
     * @param accessToken OA access token
     * @param type Article type ("normal" or "video")
     * @param options Configuration options for fetching
     * @returns All articles with pagination info
     */
    getAllArticles(accessToken: string, type?: "normal" | "video", options?: {
        /** Number of articles to fetch per request (default: 10, max: 10) */
        batchSize?: number;
        /** Maximum number of articles to fetch (default: 1000, 0 = no limit) */
        maxArticles?: number;
        /** Optional callback to track progress */
        onProgress?: (progress: {
            currentBatch: number;
            totalFetched: number;
            hasMore: boolean;
        }) => void;
    }): Promise<{
        /** Array of all fetched articles */
        articles: ArticleListItem[];
        /** Total number of articles fetched */
        totalFetched: number;
        /** Number of API calls made */
        totalBatches: number;
        /** Whether there are more articles available */
        hasMore: boolean;
    }>;
    /**
     * Get all articles of both types (normal and video) combined
     *
     * @example
     * ```typescript
     * // Get all articles (both normal and video)
     * const result = await articleService.getAllArticlesCombined(accessToken, {
     *   batchSize: 50,
     *   maxArticlesPerType: 500,
     *   onProgress: (progress) => {
     *     console.log(`${progress.type}: Batch ${progress.currentBatch}, Total: ${progress.totalFetched}`);
     *   }
     * });
     *
     * console.log(`Total articles: ${result.totalFetched}`);
     * console.log(`Normal articles: ${result.breakdown.normal.totalFetched}`);
     * console.log(`Video articles: ${result.breakdown.video.totalFetched}`);
     * ```
     *
     * @param accessToken OA access token
     * @param options Configuration options for fetching
     * @returns All articles (normal + video) with combined pagination info
     */
    getAllArticlesCombined(accessToken: string, options?: {
        /** Number of articles to fetch per request for each type (default: 10, max: 10) */
        batchSize?: number;
        /** Maximum number of articles to fetch per type (default: 500, 0 = no limit) */
        maxArticlesPerType?: number;
        /** Optional callback to track progress */
        onProgress?: (progress: {
            type: "normal" | "video";
            currentBatch: number;
            totalFetched: number;
            hasMore: boolean;
        }) => void;
    }): Promise<{
        /** Array of all fetched articles (normal + video) */
        articles: ArticleListItem[];
        /** Breakdown by type */
        breakdown: {
            normal: {
                articles: ArticleListItem[];
                totalFetched: number;
                totalBatches: number;
                hasMore: boolean;
            };
            video: {
                articles: ArticleListItem[];
                totalFetched: number;
                totalBatches: number;
                hasMore: boolean;
            };
        };
        /** Total number of articles fetched */
        totalFetched: number;
        /** Total number of API calls made */
        totalBatches: number;
    }>;
    /**
     * Remove article
     * @param accessToken OA access token
     * @param articleId Article ID to remove
     * @returns Removal result
     */
    removeArticle(accessToken: string, articleId: string): Promise<ArticleRemoveResponse>;
    /**
     * Remove multiple articles in bulk (advanced function with progress tracking)
     * @param accessToken OA access token
     * @param articleIds Array of article IDs to remove
     * @param options Options for bulk removal operation
     * @param progressCallback Optional callback to track progress
     * @returns Complete bulk removal result with detailed statistics
     *
     * This function automatically handles:
     * - Batch processing to avoid overwhelming the API
     * - Concurrent requests with configurable limits
     * - Error handling for individual articles
     * - Progress tracking and statistics
     * - Retry mechanism for failed requests
     */
    removeBulkArticles(accessToken: string, articleIds: string[], options?: BulkRemoveOptions, progressCallback?: (progress: BulkRemoveProgress) => void): Promise<BulkRemoveResult>;
    /**
     * Remove multiple articles in bulk with simple interface (no progress tracking)
     * @param accessToken OA access token
     * @param articleIds Array of article IDs to remove
     * @param options Optional settings for bulk removal
     * @returns Simple summary of bulk removal operation
     *
     * Simplified version of removeBulkArticles that returns just the summary statistics
     */
    removeBulkArticlesSimple(accessToken: string, articleIds: string[], options?: BulkRemoveOptions): Promise<{
        total_processed: number;
        successful_count: number;
        failed_count: number;
        success_rate: number;
        failed_articles?: string[];
    }>;
    /**
     * Get all articles with full details (advanced function with progress tracking)
     * @param accessToken OA access token
     * @param type Article type ("normal", "video", or "both")
     * @param options Options for bulk details fetch operation
     * @param progressCallback Optional callback to track progress
     * @returns Complete result with all articles and their full details
     *
     * This function automatically:
     * 1. Fetches all articles list using getAllArticles() or getAllArticlesCombined()
     * 2. Fetches detailed information for each article using getArticleDetail()
     * 3. Handles batch processing and concurrency control
     * 4. Provides detailed progress tracking and error handling
     * 5. Returns both successful and failed results with statistics
     */
    getAllArticlesWithDetails(accessToken: string, type?: "normal" | "video" | "both", options?: BulkDetailOptions, progressCallback?: (progress: BulkDetailProgress) => void): Promise<BulkDetailResult>;
    /**
     * Get all articles with full details - Simple interface (no progress tracking)
     * @param accessToken OA access token
     * @param type Article type ("normal", "video", or "both")
     * @param options Optional settings for details fetch
     * @returns Array of articles with full details
     *
     * Simplified version that returns just the articles with details array
     */
    getAllArticlesWithDetailsSimple(accessToken: string, type?: "normal" | "video" | "both", options?: BulkDetailOptions): Promise<ArticleDetail[]>;
    /**
     * Create article and wait for completion - All-in-one method
     * @param accessToken OA access token
     * @param request Article information
     * @param options Options for tracking and timeout
     * @param progressCallback Optional callback to track progress
     * @returns Article ID when creation is complete
     *
     * This method combines create + tracking + verification into one call:
     * 1. Creates the article and gets token
     * 2. Automatically tracks progress until completion
     * 3. Returns the final article ID
     * 4. Handles all errors and timeouts gracefully
     */
    createArticleAndWaitForCompletion(accessToken: string, request: ArticleRequest, options?: {
        /** Maximum time to wait for completion in milliseconds (default: 60000 = 1 minute) */
        timeout?: number;
        /** Interval between progress checks in milliseconds (default: 2000 = 2 seconds) */
        checkInterval?: number;
        /** Whether to return article details instead of just ID (default: false) */
        returnDetails?: boolean;
    }, progressCallback?: (progress: {
        phase: 'creating' | 'processing' | 'verifying' | 'complete';
        message: string;
        token?: string;
        article_id?: string;
        elapsed_time: number;
    }) => void): Promise<{
        article_id: string;
        token: string;
        total_time: number;
        article_details?: ArticleDetail;
    }>;
    /**
     * Create article and wait for completion - Simple interface
     * @param accessToken OA access token
     * @param request Article information
     * @param timeoutMs Optional timeout in milliseconds (default: 60000)
     * @returns Article ID when creation is complete
     *
     * Simplified version that returns just the article ID
     */
    createArticleAndGetId(accessToken: string, request: ArticleRequest, timeoutMs?: number): Promise<string>;
    private handleArticleError;
    /**
     * Update normal article
     * @param accessToken OA access token
     * @param request Update information
     * @returns Token for tracking update progress
     */
    updateNormalArticle(accessToken: string, request: ArticleUpdateNormalRequest): Promise<ArticleUpdateResponse>;
    /**
     * Update video article
     * @param accessToken OA access token
     * @param request Update information
     * @returns Token for tracking update progress
     */
    updateVideoArticle(accessToken: string, request: ArticleUpdateVideoRequest): Promise<ArticleUpdateResponse>;
    /**
     * Update article (automatically detect type)
     * @param accessToken OA access token
     * @param request Update information
     * @returns Token for tracking update progress
     */
    updateArticle(accessToken: string, request: ArticleUpdateRequest): Promise<ArticleUpdateResponse>;
    /**
     * Update article status only
     * @param accessToken OA access token
     * @param articleId Article ID to update
     * @param status New status ('show' or 'hide')
     * @param comment Optional comment status ('show' or 'hide')
     * @returns Token for tracking update progress
     */
    updateArticleStatus(accessToken: string, articleId: string, status: ArticleStatus, comment?: CommentStatus): Promise<ArticleUpdateResponse>;
    /**
     * Validate article request (universal method)
     * @param request Article creation request
     */
    validateArticleRequest(request: ArticleRequest): void;
    /**
     * Validate normal article creation request
     */
    private validateNormalArticleRequest;
    /**
     * Validate video article creation request
     */
    private validateVideoArticleRequest;
    /**
     * Validate article list request
     */
    private validateArticleListRequest;
    /**
     * Validate normal article update request
     */
    private validateUpdateNormalArticleRequest;
    /**
     * Validate video article update request
     */
    private validateUpdateVideoArticleRequest;
    /**
     * Validate cover information
     */
    private validateCover;
    /**
     * Validate body item
     */
    private validateBodyItem;
    private isValidTrackingLink;
}

/**
 * Service for handling Zalo Official Account Video Upload APIs
 *
 * CONDITIONS FOR USING ZALO VIDEO UPLOAD:
 *
 * 1. GENERAL CONDITIONS:
 *    - OA must have permission to upload videos
 *    - Access token must have "manage_article" scope
 *    - OA must have active status and be verified
 *
 * 2. VIDEO FILE REQUIREMENTS:
 *    - File formats: MP4, AVI only
 *    - Maximum file size: 50MB
 *    - Video processing: asynchronous, use token to check status
 *    - Processing time: varies based on video size and quality
 *
 * 3. UPLOAD PROCESS:
 *    - Step 1: Upload video file and get token
 *    - Step 2: Use token to check processing status
 *    - Step 3: Get video_id when processing is complete
 *    - Step 4: Use video_id in article creation
 *
 * 4. STATUS CODES:
 *    - 0: Trạng thái không xác định
 *    - 1: Video đã được xử lý thành công và có thể sử dụng
 *    - 2: Video đã bị khóa
 *    - 3: Video đang được xử lý
 *    - 4: Video xử lý thất bại
 *    - 5: Video đã bị xóa
 */
declare class VideoUploadService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Upload video file for article
     * @param accessToken OA access token
     * @param file Video file (Buffer or File)
     * @param filename Original filename (optional, will be generated if not provided)
     * @returns Token for tracking upload progress
     */
    uploadVideo(accessToken: string, file: Buffer | File, filename?: string): Promise<VideoUploadResponse>;
    /**
     * Check video upload status
     * @param accessToken OA access token
     * @param token Token from video upload response
     * @returns Video status information
     */
    checkVideoStatus(accessToken: string, token: string): Promise<VideoStatusResponse>;
    /**
     * Wait for video upload completion with polling
     * @param accessToken OA access token
     * @param token Token from video upload response
     * @param maxWaitTime Maximum wait time in milliseconds (default: 5 minutes)
     * @param pollInterval Polling interval in milliseconds (default: 5 seconds)
     * @returns Final video status when completed
     */
    waitForUploadCompletion(accessToken: string, token: string, maxWaitTime?: number, // 5 minutes
    pollInterval?: number): Promise<VideoStatusResponse>;
    /**
     * Upload video from URL
     * @param accessToken OA access token
     * @param videoUrl URL of the video to upload
     * @returns Token for tracking upload progress
     */
    uploadVideoFromUrl(accessToken: string, videoUrl: string): Promise<VideoUploadResponse>;
    /**
     * Get video information by video ID
     * @param accessToken OA access token
     * @param videoId Video ID
     * @returns Video information
     */
    getVideoInfo(accessToken: string, videoId: string): Promise<any>;
    /**
     * Upload video and wait for completion - Combined method
     * @param accessToken OA access token
     * @param file Video file (Buffer or File)
     * @param filename Original filename (optional, will be generated if not provided)
     * @param maxWaitTime Maximum wait time in milliseconds (default: 5 minutes)
     * @param pollInterval Polling interval in milliseconds (default: 5 seconds)
     * @returns Final video status when completed
     */
    uploadVideoAndWaitForCompletion(accessToken: string, file: Buffer | File, filename?: string, maxWaitTime?: number, // 5 minutes
    pollInterval?: number): Promise<VideoStatusResponse>;
    /**
     * Validate video file
     */
    private validateVideoFile;
    private getMimeTypeFromFilename;
    private extractFilenameFromUrl;
    private sleep;
    private handleVideoUploadError;
}

/**
 * Service xử lý các API tin nhắn tư vấn của Zalo Official Account
 *
 * Tin nhắn tư vấn (CS - Customer Service) là loại tin nhắn đặc biệt
 * cho phép OA gửi tin nhắn chủ động đến người dùng trong khung thời gian nhất định
 *
 * ĐIỀU KIỆN GỬI TIN TƯ VẤN:
 *
 * 1. THỜI GIAN GỬI:
 *    - Chỉ được gửi trong vòng 48 giờ kể từ khi người dùng tương tác cuối cùng với OA
 *    - Tương tác bao gồm: gửi tin nhắn, nhấn button, gọi điện, truy cập website từ OA
 *
 * 2. NỘI DUNG TIN NHẮN:
 *    - Phải liên quan đến tư vấn, hỗ trợ khách hàng
 *    - Bao gồm: trả lời câu hỏi, hướng dẫn sử dụng, hỗ trợ kỹ thuật
 *    - Không được chứa nội dung quảng cáo trực tiếp
 *
 * 3. TẦN SUẤT GỬI:
 *    - Không giới hạn số lượng tin nhắn tư vấn trong ngày
 *    - Tuy nhiên cần tuân thủ nguyên tắc không spam
 *
 * 4. NGƯỜI DÙNG:
 *    - Người dùng phải đã follow OA
 *    - Người dùng không được block OA
 *    - Người dùng phải có tương tác gần đây với OA
 */
declare class ConsultationService {
    private readonly client;
    private readonly endpoint;
    constructor(client: ZaloClient);
    /**
     * Gửi tin nhắn tư vấn văn bản
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param message Nội dung tin nhắn văn bản
     * @returns Thông tin tin nhắn đã gửi
     */
    sendTextMessage(accessToken: string, recipient: MessageRecipient, message: TextMessage): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tư vấn trích dẫn (quote message)
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param text Nội dung tin nhắn trả lời (tối đa 2000 ký tự)
     * @param quoteMessageId ID của tin nhắn muốn trích dẫn
     * @returns Thông tin tin nhắn đã gửi với quota information
     */
    sendQuoteMessage(accessToken: string, recipient: MessageRecipient, text: string, quoteMessageId: string): Promise<SendMessageResponse>;
    /**
     * Tạo ConsultationQuoteMessage object
     * @param text Nội dung tin nhắn trả lời
     * @param quoteMessageId ID của tin nhắn muốn trích dẫn
     * @param quoteContent Nội dung của tin nhắn được trích dẫn (optional)
     * @returns ConsultationQuoteMessage object
     */
    createQuoteMessage(text: string, quoteMessageId: string, quoteContent?: string): ConsultationQuoteMessage;
    /**
     * Gửi tin nhắn tư vấn hình ảnh bằng URL
     *
     * API Specification:
     * - URL: https://openapi.zalo.me/v3.0/oa/message/cs
     * - Method: POST
     * - Content-Type: application/json
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param imageUrl URL trực tiếp đến hình ảnh (jpg, png, tối đa 1MB, tỷ lệ tối ưu 16:9)
     * @param text Tiêu đề ảnh (optional, tối đa 2000 ký tự)
     * @returns Thông tin tin nhắn đã gửi với quota information
     */
    sendImageMessage(accessToken: string, userId: string, imageUrl: string, text?: string): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tư vấn hình ảnh bằng attachment_id (ảnh đã upload trước)
     *
     * API Specification:
     * - URL: https://openapi.zalo.me/v3.0/oa/message/cs
     * - Method: POST
     * - Content-Type: application/json
     * - Lưu ý: Chỉ sử dụng attachment_id HOẶC url, không được dùng cả hai
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param attachmentId ID của ảnh đã upload trước đó (từ API upload ảnh)
     * @param text Tiêu đề ảnh (optional, tối đa 2000 ký tự)
     * @returns Thông tin tin nhắn đã gửi với quota information
     */
    sendImageByAttachmentId(accessToken: string, userId: string, attachmentId: string, text?: string): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tư vấn hình ảnh (hỗ trợ cả URL và attachment_id)
     *
     * API Specification:
     * - URL: https://openapi.zalo.me/v3.0/oa/message/cs
     * - Method: POST
     * - Content-Type: application/json
     * - Lưu ý: Chỉ sử dụng MỘT trong hai: imageUrl HOẶC attachmentId
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param options Tùy chọn gửi ảnh
     * @param options.imageUrl URL trực tiếp đến hình ảnh (jpg, png, tối đa 1MB)
     * @param options.attachmentId ID của ảnh đã upload trước đó
     * @param options.text Tiêu đề ảnh (optional, tối đa 2000 ký tự)
     * @returns Thông tin tin nhắn đã gửi với quota information
     */
    sendImage(accessToken: string, userId: string, options: {
        imageUrl?: string;
        attachmentId?: string;
        text?: string;
    }): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tư vấn ảnh GIF động
     *
     * API Specification:
     * - URL: https://openapi.zalo.me/v3.0/oa/message/cs
     * - Method: POST
     * - Content-Type: application/json
     * - Lưu ý: width và height là BẮT BUỘC cho media_type = "gif"
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param gifUrl URL trực tiếp đến ảnh GIF (tối đa 1MB)
     * @param width Chiều rộng của ảnh GIF (bắt buộc, > 0)
     * @param height Chiều cao của ảnh GIF (bắt buộc, > 0)
     * @param text Tiêu đề ảnh (optional, tối đa 2000 ký tự)
     * @returns Thông tin tin nhắn đã gửi với quota information
     */
    sendGifMessage(accessToken: string, userId: string, gifUrl: string, width: number, height: number, text?: string): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tư vấn đính kèm file
     * Endpoint: https://openapi.zalo.me/v3.0/oa/message/cs
     * Method: POST
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param fileToken Token của file đã upload (từ API upload file)
     * @returns Thông tin tin nhắn đã gửi
     *
     * Note: Cần sử dụng API upload file trước để lấy token
     */
    sendFileMessage(accessToken: string, userId: string, fileToken: string): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tư vấn theo mẫu yêu cầu thông tin người dùng
     * Endpoint: https://openapi.zalo.me/v3.0/oa/message/cs
     * Method: POST
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param title Tiêu đề hiển thị của template (tối đa 100 ký tự)
     * @param subtitle Tiêu đề phụ của template (tối đa 500 ký tự)
     * @param imageUrl Đường dẫn đến ảnh
     * @returns Thông tin tin nhắn đã gửi
     */
    sendRequestUserInfoMessage(accessToken: string, userId: string, title: string, subtitle: string, imageUrl: string): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tư vấn kèm Sticker
     * Endpoint: https://openapi.zalo.me/v3.0/oa/message/cs
     * Method: POST
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param stickerAttachmentId ID của sticker (lấy từ https://stickers.zaloapp.com/)
     * @returns Thông tin tin nhắn đã gửi
     *
     * Note: Sticker ID lấy từ nguồn https://stickers.zaloapp.com/
     * Xem video hướng dẫn: https://vimeo.com/649330161
     */
    sendStickerMessage(accessToken: string, userId: string, stickerAttachmentId: string): Promise<SendMessageResponse>;
    /**
     * Gửi chuỗi tin nhắn tư vấn với delay tùy chỉnh
     * Hỗ trợ tất cả các loại tin nhắn: text, image, gif, file, sticker, request_user_info
     *
     * @param request Thông tin request gửi chuỗi tin nhắn
     * @returns Kết quả gửi từng tin nhắn
     */
    sendMessageSequence(request: SendMessageSequenceRequest): Promise<SendMessageSequenceResponse>;
    /**
     * Gửi chuỗi tin nhắn tư vấn tới nhiều users với callback tracking
     *
     * @param request Thông tin request gửi chuỗi tin nhắn tới nhiều users
     * @returns Kết quả gửi tin nhắn cho tất cả users
     */
    sendMessageSequenceToMultipleUsers(request: SendMessageSequenceToMultipleUsersRequest): Promise<SendMessageSequenceToMultipleUsersResponse>;
    /**
     * Gửi tin nhắn tư vấn tùy chỉnh tới nhiều users với callback tracking
     * Mỗi user có thể có bộ tin nhắn riêng biệt
     *
     * @param request Thông tin request gửi tin nhắn tùy chỉnh tới nhiều users
     * @returns Kết quả gửi tin nhắn cho tất cả users
     */
    sendCustomMessageSequenceToMultipleUsers(request: SendCustomMessageSequenceToMultipleUsersRequest): Promise<SendCustomMessageSequenceToMultipleUsersResponse>;
    /**
     * Gửi tin nhắn template tổng quát với elements và buttons
     * Endpoint: https://openapi.zalo.me/v3.0/oa/message/cs
     * Method: POST
     *
     * @param accessToken Access token của Official Account
     * @param userId ID người nhận (user_id)
     * @param templateType Loại template (ví dụ: "request_user_info")
     * @param elements Mảng elements (tối đa 5 phần tử)
     * @param buttons Mảng buttons (tối đa 5 phần tử, optional)
     * @returns Thông tin tin nhắn đã gửi
     */
    sendTemplateMessage(accessToken: string, userId: string, templateType: string, elements: TemplateElement[], buttons?: TemplateButton[]): Promise<SendMessageResponse>;
    /**
     * Validate template element theo quy tắc API
     * @param element Template element cần validate
     * @param index Index của element trong mảng (để báo lỗi)
     */
    private validateTemplateElement;
    /**
     * Validate template button theo quy tắc API
     * @param button Template button cần validate
     * @param index Index của button trong mảng (để báo lỗi)
     */
    private validateTemplateButton;
    /**
     * Validate template action (cho default_action)
     * @param action Template action cần validate
     * @param context Context để báo lỗi
     */
    private validateTemplateAction;
    /**
     * Validate button payload theo type
     * @param button Template button cần validate
     * @param index Index của button trong mảng (để báo lỗi)
     */
    private validateButtonPayload;
    /**
     * Helper function để tạo template element
     * @param title Tiêu đề (bắt buộc, ≤ 100 ký tự)
     * @param subtitle Tiêu đề phụ (≤ 500 ký tự)
     * @param imageUrl URL ảnh (optional)
     * @param defaultAction Hành động khi click (optional)
     * @returns TemplateElement object
     */
    createTemplateElement(title: string, subtitle?: string, imageUrl?: string, defaultAction?: TemplateAction): TemplateElement;
    /**
     * Helper function để tạo template button với URL
     * @param title Tiêu đề button (≤ 100 ký tự)
     * @param url URL để mở
     * @returns TemplateButton object
     */
    createUrlButton(title: string, url: string): TemplateButton;
    /**
     * Helper function để tạo template button với query (hiển thị)
     * @param title Tiêu đề button (≤ 100 ký tự)
     * @param payload Dữ liệu callback (≤ 1000 ký tự)
     * @returns TemplateButton object
     */
    createQueryShowButton(title: string, payload: string): TemplateButton;
    /**
     * Helper function để tạo template button với query (ẩn)
     * @param title Tiêu đề button (≤ 100 ký tự)
     * @param payload Dữ liệu callback (≤ 1000 ký tự)
     * @returns TemplateButton object
     */
    createQueryHideButton(title: string, payload: string): TemplateButton;
    /**
     * Helper function để tạo template button với SMS
     * @param title Tiêu đề button (≤ 100 ký tự)
     * @param phoneCode Số điện thoại
     * @param content Nội dung SMS (≤ 160 ký tự)
     * @returns TemplateButton object
     */
    createSmsButton(title: string, phoneCode: string, content?: string): TemplateButton;
    /**
     * Helper function để tạo template button với Phone
     * @param title Tiêu đề button (≤ 100 ký tự)
     * @param phoneCode Số điện thoại
     * @returns TemplateButton object
     */
    createPhoneButton(title: string, phoneCode: string): TemplateButton;
    /**
     * Helper function để tạo template action với URL
     * @param url URL để mở
     * @returns TemplateAction object
     */
    createUrlAction(url: string): TemplateAction;
    /**
     * Helper function để tạo template action với query (hiển thị)
     * @param payload Dữ liệu callback (≤ 1000 ký tự)
     * @returns TemplateAction object
     */
    createQueryShowAction(payload: string): TemplateAction;
    /**
     * Helper function để tạo template action với query (ẩn)
     * @param payload Dữ liệu callback (≤ 1000 ký tự)
     * @returns TemplateAction object
     */
    createQueryHideAction(payload: string): TemplateAction;
    /**
     * Helper function để tạo template action với SMS
     * @param phoneCode Số điện thoại
     * @param content Nội dung SMS (≤ 160 ký tự)
     * @returns TemplateAction object
     */
    createSmsAction(phoneCode: string, content?: string): TemplateAction;
    /**
     * Helper function để tạo template action với Phone
     * @param phoneCode Số điện thoại
     * @returns TemplateAction object
     */
    createPhoneAction(phoneCode: string): TemplateAction;
    /**
     * Utility method để sleep/delay
     * @param ms Thời gian delay tính bằng milliseconds
     */
    private sleep;
}

/**
 * Service xử lý các API tin nhắn giao dịch của Zalo Official Account
 *
 * ⚠️ **DEPRECATED**: Transaction Message API (v2.0) đã bị deprecated bởi Zalo
 *
 * **Khuyến nghị**: Sử dụng **ZNS Service** thay thế:
 * - `ZNSService.sendMessageByUid()` - Gửi tin nhắn template theo UID
 * - `ZNSService.sendMessage()` - Gửi tin nhắn theo số điện thoại
 *
 * Tham khảo: https://developers.zalo.me/docs/zalo-notification-service/
 *
 * ===
 *
 * ĐIỀU KIỆN GỬI TIN GIAO DỊCH:
 *
 * 1. THỜI GIAN GỬI:
 *    - Chỉ được gửi trong vòng 24 giờ kể từ khi người dùng tương tác cuối cùng với OA
 *    - Tương tác bao gồm: gửi tin nhắn, nhấn button, gọi điện, truy cập website từ OA
 *
 * 2. NỘI DUNG TIN NHẮN:
 *    - Phải liên quan trực tiếp đến giao dịch thực tế
 *    - Bao gồm: xác nhận đơn hàng, thông báo thanh toán, cập nhật trạng thái giao hàng
 *    - Không được chứa nội dung quảng cáo, khuyến mãi
 *
 * 3. TẦN SUẤT GỬI:
 *    - Tối đa 3 tin nhắn giao dịch/ngày cho mỗi người dùng
 *    - Phải có khoảng cách ít nhất 1 giờ giữa các tin nhắn
 *
 * 4. ĐỊNH DẠNG:
 *    - Phải sử dụng template được Zalo phê duyệt trước
 *    - Template phải tuân thủ format chuẩn của tin giao dịch
 *
 * 5. NGƯỜI DÙNG:
 *    - Người dùng phải đã follow OA
 *    - Người dùng không được block OA
 *    - Người dùng phải có tương tác gần đây với OA
 *
 * 6. OFFICIAL ACCOUNT:
 *    - OA phải được xác minh (verified)
 *    - OA phải có quyền gửi tin giao dịch được Zalo cấp phép
 *    - OA không được vi phạm chính sách của Zalo
 *
 * LỖI THƯỜNG GẶP:
 * - 1004: Người dùng chưa follow OA hoặc đã unfollow
 * - 1005: Vượt quá thời gian 24 giờ từ lần tương tác cuối
 * - 1006: Vượt quá giới hạn 3 tin/ngày
 * - 1007: Template chưa được phê duyệt hoặc không hợp lệ
 * - 1008: Nội dung tin nhắn vi phạm chính sách
 *
 * @deprecated Sử dụng ZNSService thay vì TransactionService
 */
declare class TransactionService {
    private readonly client;
    private readonly transactionApiUrl;
    constructor(client: ZaloClient);
    /**
     * Gửi tin nhắn giao dịch
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param message Nội dung tin nhắn giao dịch
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendTransactionMessage(accessToken: string, recipient: MessageRecipient, message: TransactionMessage): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn xác nhận đơn hàng
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param orderInfo Thông tin đơn hàng
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendOrderConfirmation(accessToken: string, recipient: MessageRecipient, orderInfo: {
        orderId: string;
        orderDate: string;
        totalAmount: number;
        items: Array<{
            name: string;
            quantity: number;
            price: number;
        }>;
        customerInfo: {
            name: string;
            phone: string;
            address: string;
        };
    }): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn thông báo thanh toán
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param paymentInfo Thông tin thanh toán
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendPaymentNotification(accessToken: string, recipient: MessageRecipient, paymentInfo: {
        orderId: string;
        amount: number;
        paymentMethod: string;
        paymentDate: string;
        status: "success" | "failed" | "pending";
    }): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn cập nhật trạng thái giao hàng
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param shippingInfo Thông tin giao hàng
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendShippingUpdate(accessToken: string, recipient: MessageRecipient, shippingInfo: {
        orderId: string;
        trackingNumber: string;
        status: "preparing" | "shipped" | "in_transit" | "delivered";
        estimatedDelivery?: string;
        carrier?: string;
    }): Promise<SendMessageResponse>;
    /**
     * Validate transaction message format
     * @param message Transaction message to validate
     */
    private validateTransactionMessage;
}

/**
 * Service xử lý các API tin nhắn truyền thông/quảng cáo của Zalo Official Account
 *
 * ⚠️ **DEPRECATED**: Promotion Message API (v3.0) đã bị deprecated bởi Zalo
 *
 * **Khuyến nghị**: Sử dụng **ZNS Service** thay thế:
 * - `ZNSService.sendMessageByUid()` - Gửi tin nhắn template theo UID
 * - `ZNSService.sendMessage()` - Gửi tin nhắn theo số điện thoại
 *
 * Tham khảo: https://developers.zalo.me/docs/zalo-notification-service/
 *
 * ===
 *
 * ĐIỀU KIỆN GỬI TIN TRUYỀN THÔNG:
 *
 * 1. THỜI GIAN GỬI:
 *    - Chỉ được gửi trong khung giờ từ 8:00 - 22:00 hàng ngày
 *    - Không được gửi vào các ngày lễ, tết theo quy định
 *
 * 2. NỘI DUNG TIN NHẮN:
 *    - Phải là nội dung quảng cáo, khuyến mãi, tin tức
 *    - Không được chứa nội dung vi phạm pháp luật
 *    - Phải tuân thủ quy định về quảng cáo của Việt Nam
 *
 * 3. TẦN SUẤT GỬI:
 *    - Tối đa 1 tin nhắn truyền thông/ngày cho mỗi người dùng
 *    - Người dùng có thể từ chối nhận tin truyền thông
 *
 * 4. ĐỊNH DẠNG:
 *    - Phải sử dụng template được Zalo phê duyệt trước
 *    - Template phải tuân thủ format chuẩn của tin truyền thông
 *
 * 5. NGƯỜI DÙNG:
 *    - Người dùng phải đã follow OA
 *    - Người dùng không được block OA
 *    - Người dùng không được từ chối nhận tin truyền thông
 *
 * 6. OFFICIAL ACCOUNT:
 *    - OA phải được xác minh (verified)
 *    - OA phải có quyền gửi tin truyền thông được Zalo cấp phép
 *    - OA không được vi phạm chính sách về quảng cáo
 *
 * LỖI THƯỜNG GẶP:
 * - 2001: Ngoài khung giờ cho phép (8:00-22:00)
 * - 2002: Người dùng đã từ chối nhận tin truyền thông
 * - 2003: Vượt quá giới hạn 1 tin/ngày
 * - 2004: Template chưa được phê duyệt
 * - 2005: Nội dung vi phạm chính sách quảng cáo
 * - 2006: OA chưa có quyền gửi tin truyền thông
 *
 * @deprecated Sử dụng ZNSService thay vì PromotionService
 */
declare class PromotionService {
    private readonly client;
    private readonly promotionApiUrl;
    constructor(client: ZaloClient);
    /**
     * Convert input button to proper typed button for API
     * @param button Input button with flexible typing
     * @returns Properly typed button for API
     */
    private convertToTypedButton;
    /**
     * Gửi tin nhắn truyền thông/quảng cáo
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param message Nội dung tin nhắn truyền thông
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendPromotionMessage(accessToken: string, recipient: MessageRecipient, message: PromotionMessage): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn khuyến mãi sản phẩm
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param promotionInfo Thông tin khuyến mãi
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendProductPromotion(accessToken: string, recipient: MessageRecipient, promotionInfo: {
        title: string;
        description: string;
        imageUrl?: string;
        attachmentId?: string;
        originalPrice: number;
        discountPrice: number;
        discountPercent: number;
        validUntil: string;
        productUrl: string;
    }): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn thông báo sự kiện
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param eventInfo Thông tin sự kiện
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendEventNotification(accessToken: string, recipient: MessageRecipient, eventInfo: {
        title: string;
        description: string;
        imageUrl?: string;
        attachmentId?: string;
        eventDate: string;
        location: string;
        registrationUrl: string;
    }): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn newsletter
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param newsletterInfo Thông tin newsletter
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendNewsletter(accessToken: string, recipient: MessageRecipient, newsletterInfo: {
        title: string;
        summary: string;
        imageUrl?: string;
        attachmentId?: string;
        articles: Array<{
            title: string;
            url: string;
        }>;
        unsubscribeUrl: string;
    }): Promise<SendMessageResponse>;
    /**
     * Tạo promotion message theo format chuẩn v3.0 (như trong docs)
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param promotionData Dữ liệu promotion
     * @returns Thông tin tin nhắn đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendCustomPromotion(accessToken: string, recipient: MessageRecipient, promotionData: {
        banner: BannerConfig;
        headerContent: string;
        textContent: string;
        tableData: Array<{
            key: string;
            value: string;
        }>;
        footerText?: string;
        buttons: PromotionButtonInput[];
        language?: "VI" | "EN";
    }): Promise<SendMessageResponse>;
    /**
     * Validate banner configuration
     * @param banner Banner configuration to validate
     */
    private validateBannerConfig;
    /**
     * Validate promotion message format
     * @param message Promotion message to validate
     */
    private validatePromotionMessage;
    /**
     * Gửi promotion message đến nhiều user_ids với callback tracking
     * @param accessToken Access token của Official Account
     * @param userIds Danh sách user IDs cần gửi
     * @param promotionData Dữ liệu promotion
     * @param options Tùy chọn gửi và tracking
     * @returns Kết quả gửi đến tất cả users
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendCustomPromotionToMultipleUsers(accessToken: string, userIds: string[], promotionData: {
        banner: BannerConfig;
        headerContent: string;
        textContent: string;
        tableData: Array<{
            key: string;
            value: string;
        }>;
        footerText?: string;
        buttons: PromotionButtonInput[];
        language?: "VI" | "EN";
    }, options?: {
        mode?: "sequential" | "parallel";
        delayMs?: number;
        onProgress?: (progress: MultiplePromotionProgress) => void;
        onUserComplete?: (result: PromotionUserResult) => void;
        continueOnError?: boolean;
    }): Promise<MultiplePromotionResult>;
    /**
     * Gửi promotion message đến nhiều user_ids với promotionData riêng cho từng user
     * @param accessToken Access token của Official Account
     * @param userPromotions Danh sách user và promotionData tương ứng
     * @param options Tùy chọn gửi và tracking
     * @returns Kết quả gửi đến tất cả users
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendPersonalizedPromotionToMultipleUsers(accessToken: string, userPromotions: Array<{
        userId: string;
        promotionData: {
            banner: BannerConfig;
            headerContent: string;
            textContent: string;
            tableData: Array<{
                key: string;
                value: string;
            }>;
            footerText?: string;
            buttons: PromotionButtonInput[];
            language?: "VI" | "EN";
        };
    }>, options?: {
        mode?: "sequential" | "parallel";
        delayMs?: number;
        onProgress?: (progress: MultiplePromotionProgress) => void;
        onUserComplete?: (result: PromotionUserResult) => void;
        continueOnError?: boolean;
    }): Promise<MultiplePromotionResult>;
    /**
     * Validate sending time (8:00 - 22:00)
     */
    private validateSendingTime;
}

/**
 * Service xử lý các API tin nhắn tổng quát của Zalo Official Account
 *
 * Bao gồm các loại tin nhắn:
 * - Tin nhắn phản hồi (Response Message)
 * - Tin nhắn ẩn danh (Anonymous Message)
 * - Tin nhắn tự động (Auto Message)
 * - Tin nhắn hệ thống (System Message)
 *
 * ĐIỀU KIỆN GỬI TIN NHẮN TỔNG QUÁT:
 *
 * 1. TIN NHẮN PHẢN HỒI:
 *    - Chỉ được gửi trong vòng 24 giờ kể từ khi người dùng gửi tin nhắn đến OA
 *    - Không giới hạn số lượng tin nhắn phản hồi
 *    - Có thể chứa bất kỳ nội dung nào (tư vấn, quảng cáo, thông tin)
 *
 * 2. TIN NHẮN ẨN DANH:
 *    - Gửi tin nhắn mà không hiển thị thông tin OA
 *    - Chỉ dành cho các trường hợp đặc biệt
 *    - Cần được Zalo phê duyệt trước khi sử dụng
 *
 * 3. TIN NHẮN TỰ ĐỘNG:
 *    - Tin nhắn được gửi tự động dựa trên trigger
 *    - Bao gồm: tin chào mừng, tin cảm ơn, tin nhắc nhở
 *
 * 4. TIN NHẮN HỆ THỐNG:
 *    - Tin nhắn thông báo từ hệ thống
 *    - Bao gồm: thông báo bảo trì, cập nhật chính sách
 */
declare class GeneralMessageService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Gửi tin nhắn phản hồi hình ảnh
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param message Nội dung tin nhắn hình ảnh
     * @returns Thông tin tin nhắn đã gửi
     */
    sendResponseImageMessage(accessToken: string, recipient: MessageRecipient, message: ImageMessage): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn ẩn danh
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param message Nội dung tin nhắn ẩn danh
     * @returns Thông tin tin nhắn đã gửi
     */
    sendAnonymousMessage(accessToken: string, recipient: MessageRecipient, message: AnonymousTextMessage): Promise<SendMessageResponse>;
    /**
     * Gửi tin nhắn tổng quát
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận
     * @param message Nội dung tin nhắn
     * @param messagingType Loại tin nhắn (response, update, message_tag)
     * @returns Thông tin tin nhắn đã gửi
     */
    sendMessage(accessToken: string, recipient: MessageRecipient, message: Message, messagingType?: "consultation" | "transaction" | "promotion" | "response" | "update" | "message_tag"): Promise<SendMessageResponse>;
}

/**
 * Interface cho thông tin quota tin nhắn
 */
interface MessageQuotaInfo {
    quota: number;
    used: number;
    remaining: number;
    reset_time: number;
}
/**
 * Interface cho tin nhắn trong cuộc hội thoại - matches Zalo API docs exactly
 */
interface ConversationMessage {
    /** ID của tin nhắn */
    message_id: string;
    /** Thuộc tính cho biết tin nhắn được gửi từ OA hoặc người dùng. 0: tin nhắn từ OA đến user, 1: tin nhắn từ user đến OA */
    src: number;
    /** Thời gian tin nhắn được gửi (định dạng timestamp) */
    time: number;
    /** Loại tin nhắn: text, voice, photo, GIF, link, links, sticker, location */
    type: string;
    /** Nội dung tin nhắn */
    message: string;
    /** Thuộc tính trả về 1 danh sách các đường dẫn được đính kèm trong tin nhắn (chỉ có khi type là link hoặc links) */
    links?: string[];
    /** URL của ảnh hoặc GIF (chỉ có khi type là GIF hoặc photo) */
    thumb?: string;
    /** Đường dẫn đến audio/ảnh/GIF/nhãn dán */
    url?: string;
    /** Mô tả của ảnh (chỉ có khi type là photo) */
    description?: string;
    /** ID của đối tượng gửi tin nhắn (có thể là user_id hoặc oa_id) */
    from_id: string;
    /** ID của đối tượng nhận tin nhắn (có thể là user_id hoặc oa_id) */
    to_id: string;
    /** Tên hiển thị của đối tượng gửi tin nhắn */
    from_display_name: string;
    /** Đường dẫn đến ảnh đại diện của đối tượng gửi tin nhắn */
    from_avatar: string;
    /** Tên hiển thị của đối tượng nhận tin nhắn */
    to_display_name: string;
    /** Đường dẫn đến ảnh đại diện của đối tượng nhận tin nhắn */
    to_avatar: string;
    /** Thuộc tính trả về giá trị longitude và latitude của người dùng (chỉ có khi type là location) */
    location?: string;
}
/**
 * Interface cho cuộc hội thoại
 */
interface Conversation {
    conversation_id: string;
    user_id: string;
    last_message: ConversationMessage;
    unread_count: number;
    updated_time: number;
}
/**
 * Interface cho kết quả upload file - matches Zalo API docs exactly
 */
interface UploadFileResult {
    /** Token của file, sử dụng cho API gửi thông báo đính kèm file */
    token: string;
}
/**
 * Interface cho kết quả upload image - matches Zalo API docs exactly
 */
interface UploadImageResult {
    /** ID của ảnh được upload */
    attachment_id: string;
}
/**
 * Service xử lý các API quản lý tin nhắn của Zalo Official Account
 *
 * Bao gồm các chức năng:
 * - Kiểm tra hạn mức gửi tin nhắn
 * - Lấy danh sách tin nhắn trong cuộc hội thoại với user cụ thể
 * - Lấy thông tin tin nhắn gần nhất
 * - Upload file và hình ảnh
 *
 * ĐIỀU KIỆN SỮCDNG:
 *
 * 1. KIỂM TRA HẠN MỨC:
 *    - Cần quyền truy cập thông tin quota từ Zalo
 *    - Quota được reset hàng ngày vào 00:00 GMT+7
 *
 * 2. LẤY TIN NHẮN:
 *    - API hỗ trợ lấy thông tin tin nhắn giữa OA và một người dùng cụ thể
 *    - Mỗi request lấy tối đa được 10 tin nhắn
 *    - Tin nhắn gần nhất có thứ tự là 0
 *
 * 3. UPLOAD FILE:
 *    - Kích thước tối đa 5MB
 *    - Hỗ trợ các định dạng: PDF, DOC, DOCX
 *    - Quota: 5000 request/tháng
 *
 * 4. UPLOAD HÌNH ẢNH:
 *    - Dung lượng tối đa 1MB
 *    - Hỗ trợ các định dạng: JPG và PNG
 *    - Ảnh sẽ được lưu trên server tối đa 7 ngày
 *    - Quota: 5000 request/tháng
 *
 * 5. UPLOAD ẢNH GIF:
 *    - Kích thước tối đa 5MB
 *    - Chỉ hỗ trợ định dạng GIF
 *    - Ảnh GIF được lưu trên server tối đa 7 ngày
 *    - Quota: 5000 request/tháng
 */
declare class MessageManagementService {
    private readonly client;
    private readonly endpoints;
    constructor(client: ZaloClient);
    /**
     * Kiểm tra hạn mức gửi tin nhắn đến user cụ thể
     * @param accessToken Access token của Official Account
     * @param userId ID của người dùng Zalo
     * @returns Thông tin hạn mức gửi tin nhắn
     */
    checkMessageQuota(accessToken: string, userId: string): Promise<MessageQuotaInfo>;
    /**
     * Lấy danh sách tin nhắn trong cuộc hội thoại với user
     *
     * API hỗ trợ lấy thông tin tin nhắn giữa OA và một người dùng cụ thể.
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người dùng Zalo cần lấy danh sách hội thoại
     * @param offset Thứ tự của tin nhắn đầu tiên trong danh sách trả về (tin nhắn gần nhất có thứ tự là 0)
     * @param count Số lượng tin nhắn cần lấy (mỗi request lấy tối đa 10 tin nhắn)
     * @returns Danh sách tin nhắn
     *
     * @example
     * ```typescript
     * const messages = await messageService.getConversationMessages(
     *   accessToken,
     *   '2512523625412515',
     *   0,
     *   5
     * );
     * ```
     */
    getConversationMessages(accessToken: string, userId: string, offset?: number, count?: number): Promise<ConversationMessage[]>;
    /**
     * Lấy danh sách tin nhắn trong cuộc hội thoại với user (sử dụng POST method)
     *
     * API hỗ trợ lấy thông tin tin nhắn giữa OA và một người dùng cụ thể sử dụng phương thức POST.
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người dùng Zalo cần lấy danh sách hội thoại
     * @param offset Thứ tự của tin nhắn đầu tiên trong danh sách trả về (tin nhắn gần nhất có thứ tự là 0)
     * @param count Số lượng tin nhắn cần lấy (mỗi request lấy tối đa 10 tin nhắn)
     * @returns Danh sách tin nhắn
     *
     * @example
     * ```typescript
     * const messages = await messageService.postConversationMessages(
     *   accessToken,
     *   '2512523625412515',
     *   0,
     *   5
     * );
     * ```
     */
    postConversationMessages(accessToken: string, userId: string, offset?: number, count?: number): Promise<ConversationMessage[]>;
    /**
     * Lấy thông tin tin nhắn gần nhất
     *
     * API hỗ trợ lấy thông tin tối đa 10 tin nhắn gần nhất giữa OA và người dùng.
     *
     * @param accessToken Access token của Official Account
     * @param offset Thứ tự của tin nhắn đầu tiên trong danh sách trả về (tin nhắn gần nhất có thứ tự là 0)
     * @param count Số lượng tin nhắn muốn lấy (mỗi request lấy tối đa 10 tin nhắn)
     * @returns Danh sách tin nhắn gần nhất
     *
     * @example
     * ```typescript
     * const recentMessages = await messageService.getRecentConversations(
     *   accessToken,
     *   0,
     *   5
     * );
     * ```
     */
    getRecentConversations(accessToken: string, offset?: number, count?: number): Promise<ConversationMessage[]>;
    /**
     * Upload file để sử dụng trong tin nhắn
     * @param accessToken Access token của Official Account
     * @param fileData Dữ liệu file (base64 hoặc buffer)
     * @param fileName Tên file
     * @returns Token của file đã upload
     *
     * Lưu ý:
     * - Chỉ hỗ trợ file PDF/DOC/DOCX
     * - Dung lượng file không vượt quá 5MB
     * - File sẽ được lưu trên server tối đa 7 ngày
     * - Quota: 5000 request/tháng
     */
    uploadFile(accessToken: string, fileData: string | Buffer, fileName: string): Promise<UploadFileResult>;
    /**
     * Upload hình ảnh để sử dụng trong tin nhắn
     * @param accessToken Access token của Official Account
     * @param imageData Dữ liệu hình ảnh (base64 hoặc buffer)
     * @param fileName Tên file hình ảnh
     * @returns ID của ảnh đã upload
     *
     * Lưu ý:
     * - Hỗ trợ các định dạng: JPG và PNG
     * - Dung lượng tối đa: 1MB
     * - Ảnh sẽ được lưu trên server tối đa 7 ngày
     * - Quota: 5000 request/tháng
     */
    uploadImage(accessToken: string, imageData: string | Buffer, fileName: string): Promise<UploadImageResult>;
    /**
     * Upload ảnh GIF để sử dụng trong tin nhắn
     * @param accessToken Access token của Official Account
     * @param gifData Dữ liệu ảnh GIF (base64 hoặc buffer)
     * @param fileName Tên file ảnh GIF
     * @returns Thông tin ảnh GIF đã upload
     *
     * Lưu ý:
     * - Dung lượng tối đa: 5MB
     * - Ảnh GIF sẽ được lưu trên server tối đa 7 ngày
     * - Quota: 5000 request/tháng
     * - Định dạng hỗ trợ: GIF
     */
    uploadGif(accessToken: string, gifData: string | Buffer, fileName: string): Promise<UploadImageResult>;
}

/**
 * Broadcast Service for Zalo Official Account
 *
 * ⚠️ **DEPRECATED**: Broadcast Message API (v2.0) đã bị deprecated bởi Zalo
 *
 * **Khuyến nghị**: Sử dụng **ZNS Service** thay thế:
 * - `ZNSService.sendMessageByUid()` - Gửi tin nhắn template theo UID
 * - `ZNSService.sendMessage()` - Gửi tin nhắn theo số điện thoại
 * - `ZNSService.sendHashPhoneMessage()` - Gửi tin nhắn theo hash phone
 *
 * Tham khảo: https://developers.zalo.me/docs/zalo-notification-service/
 *
 * ===
 *
 * Gửi tin Truyền thông Broadcast đến nhiều người dùng dựa trên tiêu chí targeting
 * API: https://openapi.zalo.me/v2.0/oa/message
 *
 * TÍNH NĂNG:
 * 1. GỬI TIN BROADCAST:
 *    - Gửi tin nhắn đến nhiều người dùng cùng lúc
 *    - Targeting theo tuổi, giới tính, địa điểm, platform
 *    - Chỉ hỗ trợ template type "media" với article attachment
 *
 * 2. TARGETING CRITERIA:
 *    - Ages: Nhóm tuổi (0-12, 13-17, 18-24, 25-34, 35-44, 45-54, 55-64, 65+)
 *    - Gender: Giới tính (All, Male, Female)
 *    - Cities: Tỉnh thành cụ thể (63 tỉnh thành Việt Nam)
 *    - Locations: Miền (Bắc, Trung, Nam)
 *    - Platform: Hệ điều hành (iOS, Android, Windows Phone)
 *
 * 3. GIỚI HẠN:
 *    - Chỉ gửi được article attachment
 *    - Cần có quyền broadcast từ Zalo
 *    - Tuân thủ chính sách spam và nội dung
 *
 * LỖI THƯỜNG GẶP:
 * - 3001: Không có quyền gửi broadcast
 * - 3002: Article attachment không tồn tại
 * - 3003: Targeting criteria không hợp lệ
 * - 3004: Vượt quá giới hạn broadcast
 * - 3005: Nội dung vi phạm chính sách
 *
 * @deprecated Sử dụng ZNSService thay vì BroadcastService
 */

declare class BroadcastService {
    private readonly client;
    private readonly broadcastApiUrl;
    constructor(client: ZaloClient);
    /**
     * Gửi tin nhắn broadcast đến nhiều người dùng
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin targeting người nhận
     * @param attachmentId ID của article attachment
     * @returns Thông tin tin nhắn broadcast đã gửi
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendBroadcastMessage(accessToken: string, recipient: BroadcastRecipient, attachmentId: string): Promise<BroadcastResponse>;
    /**
     * Tạo broadcast target với các tiêu chí targeting
     * @param criteria Các tiêu chí targeting
     * @returns BroadcastTarget object
     */
    createBroadcastTarget(criteria: {
        ages?: string[];
        gender?: "ALL" | "MALE" | "FEMALE";
        cities?: string[];
        locations?: ("NORTH" | "CENTRAL" | "SOUTH")[];
        platforms?: ("IOS" | "ANDROID" | "WINDOWS_PHONE")[];
    }): BroadcastTarget;
    /**
     * Validate broadcast request parameters
     * @param recipient Broadcast recipient
     * @param attachmentId Article attachment ID
     */
    private validateBroadcastRequest;
    /**
     * Get available city codes for targeting
     * @returns Object mapping city names to codes
     */
    getCityCodes(): typeof BROADCAST_CITY_CODES;
    /**
     * Get available age group codes for targeting
     * @returns Object mapping age groups to codes
     */
    getAgeGroupCodes(): typeof BROADCAST_AGE_CODES;
    /**
     * Get available gender codes for targeting
     * @returns Object mapping genders to codes
     */
    getGenderCodes(): typeof BROADCAST_GENDER_CODES;
    /**
     * Get available location codes for targeting
     * @returns Object mapping locations to codes
     */
    getLocationCodes(): typeof BROADCAST_LOCATION_CODES;
    /**
     * Get available platform codes for targeting
     * @returns Object mapping platforms to codes
     */
    getPlatformCodes(): typeof BROADCAST_PLATFORM_CODES;
    /**
     * Gửi nhiều tin nhắn broadcast với nhiều attachment IDs
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin targeting người nhận
     * @param attachmentIds Danh sách các article attachment IDs
     * @param options Tùy chọn gửi (delay giữa các tin, parallel/sequential)
     * @returns Danh sách kết quả gửi broadcast
     * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
     */
    sendMultipleBroadcastMessages(accessToken: string, recipient: BroadcastRecipient, attachmentIds: string[], options?: {
        delay?: number;
        mode?: 'parallel' | 'sequential';
        onProgress?: (progress: MultipleBroadcastProgress) => void;
    }): Promise<MultipleBroadcastResult>;
    /**
     * Sleep utility for delays
     * @param ms Milliseconds to sleep
     */
    private sleep;
}

/**
 * Service xử lý các API mua sản phẩm/dịch vụ OA của Zalo
 *
 * ĐIỀU KIỆN SỬ DỤNG PURCHASE API:
 *
 * 1. QUYỀN TRUY CẬP:
 *    - Ứng dụng cần được cấp quyền quản lý "Mua sản phẩm dịch vụ OA"
 *    - OA phải được xác minh và có quyền bán sản phẩm/dịch vụ
 *
 * 2. THỜI GIAN TẠO ĐỚN:
 *    - Order chỉ được tạo từ 00:01 đến 23:54 hàng ngày
 *    - Không thể tạo đơn hàng trong khung giờ bảo trì (23:55 - 00:00)
 *
 * 3. SẢN PHẨM/DỊCH VỤ:
 *    - Sản phẩm phải có sẵn và được phép bán
 *    - Đối tượng thụ hưởng (beneficiary) phải phù hợp với sản phẩm
 *    - Có thể sử dụng product_id HOẶC redeem_code, không được dùng cả hai
 *
 * 4. THANH TOÁN:
 *    - Tài khoản ZCA phải có đủ số dư để thanh toán
 *    - Voucher code (nếu có) phải hợp lệ và chưa hết hạn
 *
 * 5. XÁC THỰC:
 *    - Mỗi đơn hàng sẽ có verified_token (OTT) để xác nhận thanh toán
 *    - OTT có hiệu lực trong vòng 5 phút kể từ khi tạo đơn
 *
 * LỖI THƯỜNG GẶP:
 * - 1001: Beneficiary không hợp lệ
 * - 1002: Product ID không tồn tại hoặc không khả dụng
 * - 1003: Redeem code không hợp lệ hoặc đã được sử dụng
 * - 1004: Voucher code không hợp lệ hoặc đã hết hạn
 * - 1005: Sản phẩm không khả dụng cho đối tượng thụ hưởng
 * - 1006: Số dư tài khoản không đủ
 * - 1007: Ngoài thời gian cho phép tạo đơn hàng
 * - 1008: Đơn hàng trùng lặp
 * - 1009: Không có quyền truy cập
 */
declare class PurchaseService {
    private readonly client;
    private readonly purchaseApiUrl;
    constructor(client: ZaloClient);
    /**
     * Tạo đơn hàng mua sản phẩm/dịch vụ OA
     * @param accessToken Access token của Official Account
     * @param request Thông tin đơn hàng cần tạo
     * @returns Thông tin đơn hàng đã tạo
     */
    createOrder(accessToken: string, request: CreateOrderRequest): Promise<OrderData>;
    /**
     * Tạo đơn hàng với product_id
     * @param accessToken Access token của Official Account
     * @param beneficiary Đối tượng thụ hưởng (OA hoặc APP)
     * @param productId ID sản phẩm
     * @param voucherCode Mã giảm giá (tùy chọn)
     * @returns Thông tin đơn hàng đã tạo
     */
    createOrderWithProduct(accessToken: string, beneficiary: "OA" | "APP", productId: number, voucherCode?: string): Promise<OrderData>;
    /**
     * Tạo đơn hàng với redeem_code (mã quà tặng)
     * @param accessToken Access token của Official Account
     * @param beneficiary Đối tượng thụ hưởng (OA hoặc APP)
     * @param redeemCode Mã quà tặng
     * @param voucherCode Mã giảm giá (tùy chọn)
     * @returns Thông tin đơn hàng đã tạo
     */
    createOrderWithRedeemCode(accessToken: string, beneficiary: "OA" | "APP", redeemCode: string, voucherCode?: string): Promise<OrderData>;
    /**
     * Validate create order request
     * @param request Request to validate
     */
    private validateCreateOrderRequest;
    /**
     * Kiểm tra thời gian có thể tạo đơn hàng
     * @returns true nếu trong thời gian cho phép tạo đơn hàng
     */
    isOrderCreationTimeValid(): boolean;
    /**
     * Tính toán thời gian hết hạn của OTT (One Time Token)
     * @param createdTime Thời gian tạo đơn hàng (milliseconds)
     * @returns Thời gian hết hạn OTT (milliseconds)
     */
    calculateOTTExpiration(createdTime: number): number;
    /**
     * Kiểm tra OTT còn hiệu lực hay không
     * @param createdTime Thời gian tạo đơn hàng (milliseconds)
     * @returns true nếu OTT còn hiệu lực
     */
    isOTTValid(createdTime: number): boolean;
    /**
     * Xác nhận thanh toán đơn hàng
     * @param accessToken Access token của Official Account
     * @param request Thông tin xác nhận đơn hàng
     * @returns Thông tin đơn hàng đã được xác nhận thanh toán
     */
    confirmOrder(accessToken: string, request: ConfirmOrderRequest): Promise<ConfirmedOrderData>;
    /**
     * Xác nhận thanh toán đơn hàng với order ID và verified token
     * @param accessToken Access token của Official Account
     * @param orderId ID đơn hàng
     * @param verifiedToken OTT token để xác nhận
     * @returns Thông tin đơn hàng đã được xác nhận thanh toán
     */
    confirmOrderById(accessToken: string, orderId: string, verifiedToken: string): Promise<ConfirmedOrderData>;
    /**
     * Lấy thông tin sản phẩm theo ID
     * @param productId ID sản phẩm
     * @returns Thông tin sản phẩm hoặc undefined nếu không tìm thấy
     */
    getProductInfo(productId: number): ProductInfo | undefined;
    /**
     * Lấy danh sách tất cả sản phẩm có sẵn
     * @returns Danh sách thông tin sản phẩm
     */
    getAllProducts(): ProductInfo[];
    /**
     * Lấy danh sách sản phẩm theo loại
     * @param category Loại sản phẩm
     * @returns Danh sách sản phẩm thuộc loại đó
     */
    getProductsByCategory(category: 'subscription' | 'gmf' | 'quota'): ProductInfo[];
    /**
     * Lấy danh sách sản phẩm theo đối tượng thụ hưởng
     * @param beneficiary Đối tượng thụ hưởng
     * @returns Danh sách sản phẩm phù hợp với đối tượng thụ hưởng
     */
    getProductsByBeneficiary(beneficiary: "OA" | "APP"): ProductInfo[];
    /**
     * Kiểm tra sản phẩm có phù hợp với đối tượng thụ hưởng không
     * @param productId ID sản phẩm
     * @param beneficiary Đối tượng thụ hưởng
     * @returns true nếu sản phẩm phù hợp với đối tượng thụ hưởng
     */
    isProductCompatibleWithBeneficiary(productId: number, beneficiary: "OA" | "APP"): boolean;
    /**
     * Tạo đơn hàng gói OA Subscription
     * @param accessToken Access token của Official Account
     * @param subscriptionType Loại gói subscription
     * @param voucherCode Mã giảm giá (tùy chọn)
     * @returns Thông tin đơn hàng đã tạo
     */
    createOASubscriptionOrder(accessToken: string, subscriptionType: 'advanced_6m' | 'advanced_12m' | 'premium_6m' | 'premium_12m', voucherCode?: string): Promise<OrderData>;
    /**
     * Tạo đơn hàng gói GMF (Group Message Framework)
     * @param accessToken Access token của Official Account
     * @param memberLimit Giới hạn số thành viên
     * @param voucherCode Mã giảm giá (tùy chọn)
     * @returns Thông tin đơn hàng đã tạo
     */
    createGMFOrder(accessToken: string, memberLimit: 10 | 50 | 100 | 1000, voucherCode?: string): Promise<OrderData>;
    /**
     * Tạo đơn hàng gói quota tin nhắn giao dịch
     * @param accessToken Access token của Official Account
     * @param beneficiary Đối tượng thụ hưởng (OA hoặc APP)
     * @param quotaSize Kích thước gói quota
     * @param voucherCode Mã giảm giá (tùy chọn)
     * @returns Thông tin đơn hàng đã tạo
     */
    createTransactionQuotaOrder(accessToken: string, beneficiary: "OA" | "APP", quotaSize: '5k' | '50k' | '500k', voucherCode?: string): Promise<OrderData>;
    /**
     * Tạo nhiều đơn hàng cùng lúc (batch operation)
     * @param accessToken Access token của Official Account
     * @param orders Danh sách đơn hàng cần tạo
     * @returns Danh sách kết quả tạo đơn hàng
     */
    createMultipleOrders(accessToken: string, orders: CreateOrderRequest[]): Promise<Array<{
        success: boolean;
        data?: OrderData;
        error?: string;
    }>>;
    /**
     * Tạo combo đơn hàng OA Premium + GMF
     * @param accessToken Access token của Official Account
     * @param premiumDuration Thời hạn gói Premium
     * @param gmfMemberLimit Giới hạn thành viên GMF
     * @param voucherCode Mã giảm giá (tùy chọn)
     * @returns Danh sách kết quả tạo đơn hàng
     */
    createOAPremiumGMFCombo(accessToken: string, premiumDuration: '6m' | '12m', gmfMemberLimit: 10 | 50 | 100 | 1000, voucherCode?: string): Promise<{
        premium: {
            success: boolean;
            data?: OrderData;
            error?: string;
        };
        gmf: {
            success: boolean;
            data?: OrderData;
            error?: string;
        };
    }>;
    /**
     * Gợi ý sản phẩm dựa trên nhu cầu
     * @param requirements Yêu cầu của khách hàng
     * @returns Danh sách sản phẩm được gợi ý
     */
    recommendProducts(requirements: {
        beneficiary: "OA" | "APP";
        budget?: 'low' | 'medium' | 'high';
        features?: ('subscription' | 'group_messaging' | 'transaction_quota')[];
        duration?: 'short' | 'long';
    }): ProductInfo[];
    /**
     * Validate confirm order request
     * @param request Request to validate
     */
    private validateConfirmOrderRequest;
}

/**
 * Attachment cho gửi ảnh ẩn danh
 */
interface AnonymousImageAttachment {
    type: "template";
    payload: {
        template_type: "media";
        elements: Array<{
            media_type: "image";
            url?: string;
            attachment_id?: string;
        }>;
    };
}
/**
 * Attachment cho gửi file ẩn danh
 */
interface AnonymousFileAttachment {
    type: "file";
    payload: {
        token: string;
    };
}
/**
 * Attachment cho gửi sticker ẩn danh
 */
interface AnonymousStickerAttachment {
    type: "template";
    payload: {
        template_type: "media";
        elements: Array<{
            media_type: "sticker";
            attachment_id: string;
        }>;
    };
}
/**
 * Request gửi tin nhắn văn bản đến người dùng ẩn danh
 */
interface AnonymousTextMessageRequest {
    recipient: AnonymousMessageRecipient;
    message: {
        text: string;
    };
}
/**
 * Request gửi tin nhắn ảnh đến người dùng ẩn danh
 */
interface AnonymousImageMessageRequest {
    recipient: AnonymousMessageRecipient;
    message: {
        attachment: AnonymousImageAttachment;
    };
}
/**
 * Request gửi tin nhắn file đến người dùng ẩn danh
 */
interface AnonymousFileMessageRequest {
    recipient: AnonymousMessageRecipient;
    message: {
        attachment: AnonymousFileAttachment;
    };
}
/**
 * Request gửi tin nhắn sticker đến người dùng ẩn danh
 */
interface AnonymousStickerMessageRequest {
    recipient: AnonymousMessageRecipient;
    message: {
        attachment: AnonymousStickerAttachment;
    };
}
/**
 * Service xử lý các API gửi tin nhắn đến người dùng ẩn danh của Zalo Official Account
 *
 * Bao gồm các chức năng:
 * - Gửi tin nhắn văn bản đến người dùng ẩn danh
 * - Gửi tin nhắn ảnh đến người dùng ẩn danh
 * - Gửi tin nhắn file đến người dùng ẩn danh
 * - Gửi tin nhắn sticker đến người dùng ẩn danh
 *
 * ĐIỀU KIỆN SỬ DỤNG:
 *
 * 1. QUYỀN TRUY CẬP:
 *    - Cần quyền: Gửi tin và thông báo qua OA
 *
 * 2. THÔNG TIN NGƯỜI DÙNG ẨN DANH:
 *    - anonymous_id: ID đại diện cho người dùng ẩn danh
 *    - conversation_id: ID của cuộc hội thoại
 *    - Các thông tin này nhận được từ webhook khi người dùng ẩn danh gửi tin nhắn
 *
 * 3. GIỚI HẠN:
 *    - Tin nhắn văn bản: Tối đa 2000 ký tự
 *    - Tin nhắn ảnh: Hỗ trợ định dạng JPG, PNG, dung lượng tối đa 1MB
 *    - Tin nhắn sticker: Sử dụng sticker_id từ nguồn https://stickers.zaloapp.com/
 *    - Tin nhắn file: Sử dụng token từ API upload file
 */
declare class AnonymousMessageService {
    private readonly client;
    private readonly endpoint;
    constructor(client: ZaloClient);
    /**
     * Gửi tin nhắn văn bản đến người dùng ẩn danh
     *
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận ẩn danh
     * @param text Nội dung văn bản (tối đa 2000 ký tự)
     * @returns Thông tin tin nhắn đã gửi
     *
     * @example
     * ```typescript
     * const result = await anonymousService.sendTextMessage(
     *   accessToken,
     *   { anonymous_id: 'xxx', conversation_id: 'yyy' },
     *   'hello, world!'
     * );
     * ```
     */
    sendTextMessage(accessToken: string, recipient: AnonymousMessageRecipient, text: string): Promise<AnonymousMessageResponse>;
    /**
     * Gửi tin nhắn ảnh đến người dùng ẩn danh
     *
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận ẩn danh
     * @param imageUrl URL của ảnh (định dạng JPG/PNG, tối đa 1MB) HOẶC attachment_id từ upload
     * @returns Thông tin tin nhắn đã gửi
     *
     * @example
     * ```typescript
     * // Gửi bằng URL
     * const result = await anonymousService.sendImageMessage(
     *   accessToken,
     *   { anonymous_id: 'xxx', conversation_id: 'yyy' },
     *   'https://example.com/image.jpg'
     * );
     *
     * // Gửi bằng attachment_id
     * const result2 = await anonymousService.sendImageMessage(
     *   accessToken,
     *   { anonymous_id: 'xxx', conversation_id: 'yyy' },
     *   undefined,
     *   'uploaded_attachment_id'
     * );
     * ```
     */
    sendImageMessage(accessToken: string, recipient: AnonymousMessageRecipient, imageUrl?: string, attachmentId?: string): Promise<AnonymousMessageResponse>;
    /**
     * Gửi tin nhắn file đến người dùng ẩn danh
     *
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận ẩn danh
     * @param fileToken Token từ API upload file
     * @returns Thông tin tin nhắn đã gửi
     *
     * @example
     * ```typescript
     * // Đầu tiên upload file
     * const uploadResult = await messageService.uploadFile(accessToken, fileData, fileName);
     *
     * // Sau đó gửi tin nhắn file
     * const result = await anonymousService.sendFileMessage(
     *   accessToken,
     *   { anonymous_id: 'xxx', conversation_id: 'yyy' },
     *   uploadResult.token
     * );
     * ```
     */
    sendFileMessage(accessToken: string, recipient: AnonymousMessageRecipient, fileToken: string): Promise<AnonymousMessageResponse>;
    /**
     * Gửi tin nhắn sticker đến người dùng ẩn danh
     *
     * @param accessToken Access token của Official Account
     * @param recipient Thông tin người nhận ẩn danh
     * @param stickerId ID của sticker (lấy từ https://stickers.zaloapp.com/)
     * @returns Thông tin tin nhắn đã gửi
     *
     * @example
     * ```typescript
     * const result = await anonymousService.sendStickerMessage(
     *   accessToken,
     *   { anonymous_id: 'xxx', conversation_id: 'yyy' },
     *   'sticker_id_from_zalo'
     * );
     * ```
     */
    sendStickerMessage(accessToken: string, recipient: AnonymousMessageRecipient, stickerId: string): Promise<AnonymousMessageResponse>;
}

/**
 * Request thả biểu tượng cảm xúc vào tin nhắn
 */
interface MessageReactionRequest {
    recipient: MessageRecipient;
    sender_action: SenderAction;
}
/**
 * Service xử lý API thả biểu tượng cảm xúc vào tin nhắn của Zalo Official Account
 *
 * Bao gồm chức năng:
 * - Thả biểu tượng cảm xúc vào tin nhắn
 * - Thu hồi biểu tượng cảm xúc
 *
 * ĐIỀU KIỆN SỬ DỤNG:
 *
 * 1. QUYỀN TRUY CẬP:
 *    - Cần quyền: Gửi tin nhắn / Thả biểu tượng cảm xúc vào tin nhắn
 *
 * 2. GIỚI HẠN:
 *    - Một message_id chỉ được thả tối đa 50 biểu tượng cảm xúc
 *    - API này KHÔNG bị tính vào quota chủ động hàng tháng
 *
 * 3. CÁC BIỂU TƯỢNG CẢM XÚC HỖ TRỢ:
 *    - :> (Thích - Like)
 *    - --b (Haha)
 *    - :-(( (Buồn - Sad)
 *    - /-strong (Mạnh mẽ - Strong)
 *    - /-heart (Yêu thích - Love)
 *    - :-h (Ngạc nhiên - Wow)
 *    - :o (Wow)
 *    - /-remove (Thu hồi biểu tượng cảm xúc)
 */
declare class MessageReactionService {
    private readonly client;
    private readonly endpoint;
    constructor(client: ZaloClient);
    /**
     * Thả biểu tượng cảm xúc vào tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thả cảm xúc
     * @param reactIcon Biểu tượng cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     *
     * @example
     * ```typescript
     * // Thả icon tim
     * const result = await reactionService.reactToMessage(
     *   accessToken,
     *   '186729651760683225',
     *   '789f573aecf8a9a4f0eb',
     *   '/-heart'
     * );
     *
     * // Thu hồi biểu tượng cảm xúc
     * const removeResult = await reactionService.reactToMessage(
     *   accessToken,
     *   '186729651760683225',
     *   '789f573aecf8a9a4f0eb',
     *   '/-remove'
     * );
     * ```
     */
    reactToMessage(accessToken: string, userId: string, messageId: string, reactIcon: ReactIcon): Promise<MessageReactionResponse>;
    /**
     * Thả icon "Thích" (Like) vào tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thả cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     */
    likeMessage(accessToken: string, userId: string, messageId: string): Promise<MessageReactionResponse>;
    /**
     * Thả icon "Haha" vào tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thả cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     */
    hahaMessage(accessToken: string, userId: string, messageId: string): Promise<MessageReactionResponse>;
    /**
     * Thả icon "Buồn" (Sad) vào tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thả cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     */
    sadMessage(accessToken: string, userId: string, messageId: string): Promise<MessageReactionResponse>;
    /**
     * Thả icon "Mạnh mẽ" (Strong) vào tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thả cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     */
    strongMessage(accessToken: string, userId: string, messageId: string): Promise<MessageReactionResponse>;
    /**
     * Thả icon "Yêu thích" (Love) vào tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thả cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     */
    loveMessage(accessToken: string, userId: string, messageId: string): Promise<MessageReactionResponse>;
    /**
     * Thả icon "Ngạc nhiên" (Wow) vào tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thả cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     */
    wowMessage(accessToken: string, userId: string, messageId: string): Promise<MessageReactionResponse>;
    /**
     * Thu hồi biểu tượng cảm xúc khỏi tin nhắn
     *
     * @param accessToken Access token của Official Account
     * @param userId ID của người nhận
     * @param messageId message_id của tin nhắn cần thu hồi cảm xúc
     * @returns Thông tin tin nhắn phản hồi
     */
    removeReaction(accessToken: string, userId: string, messageId: string): Promise<MessageReactionResponse>;
}
/**
 * Enum các loại biểu tượng cảm xúc hỗ trợ
 */
declare enum ReactionIcon {
    /** Thích (Like) */
    LIKE = ":>",
    /** Haha */
    HAHA = "--b",
    /** Buồn (Sad) */
    SAD = ":-((",
    /** Mạnh mẽ (Strong) */
    STRONG = "/-strong",
    /** Yêu thích (Love) */
    HEART = "/-heart",
    /** Ngạc nhiên (Wow) */
    WOW_H = ":-h",
    /** Wow */
    WOW_O = ":o",
    /** Thu hồi biểu tượng cảm xúc */
    REMOVE = "/-remove"
}

/**
 * Request gửi tin nhắn miniapp
 */
interface MiniAppSendMessageRequest {
    recipient: MessageRecipient;
    message: MiniAppMessageRequest;
}
/**
 * Service xử lý API gửi tin nhắn miniapp của Zalo Official Account
 *
 * Bao gồm chức năng:
 * - Gửi tin nhắn miniapp với template tùy chỉnh
 *
 * ĐIỀU KIỆN SỬ DỤNG:
 *
 * 1. QUYỀN TRUY CẬP:
 *    - Cần quyền: Gửi tin và thông báo qua OA
 *
 * 2. THÔNG TIN BẮT BUỘC:
 *    - access_token: Token của OA
 *    - miniapp_message_token: Token được sinh ra từ miniapp, đại diện cho miniapp
 *    - user_id: Phải khớp với user đã tương tác với Miniapp
 *    - template_type: Token của template miniapp
 *    - template_data: Các thuộc tính của mẫu tin (được quy định theo từng template_type)
 *
 * 3. QUOTA (HẠN MỨC):
 *    API này có thể trả về thông tin quota trong response:
 *    - reply: Tin Tư vấn miễn phí trong hạn mức 8 tin 48 giờ
 *    - sub_quota: Tin Tư vấn miễn phí theo gói dịch vụ OA
 *    - purchase_quota: Tin trong hạn mức gói tính năng lẻ
 *    - reward_quota: Tin trong hạn mức Redeem code
 *
 *    Các loại quota không có thông tin chi tiết:
 *    - Không có quota info: Tin không tính quota (ví dụ: tin trong 48h không còn hạn mức)
 */
declare class MiniAppMessageService {
    private readonly client;
    private readonly endpoint;
    constructor(client: ZaloClient);
    /**
     * Gửi tin nhắn miniapp đến người dùng
     *
     * @param accessToken Access token của Official Account
     * @param miniappMessageToken Token được sinh ra từ miniapp
     * @param userId ID của người nhận (phải khớp với user đã tương tác với Miniapp)
     * @param templateType Token của template miniapp
     * @param templateData Các thuộc tính của mẫu tin (tuỳ theo từng template_type)
     * @returns Thông tin tin nhắn đã gửi (bao gồm quota info nếu có)
     *
     * @example
     * ```typescript
     * const result = await miniappService.sendMessage(
     *   accessToken,
     *   'miniapp_message_token',
     *   '4153814862349165539',
     *   'FB0001',
     *   {
     *     customer_name: 'Tùng Nguyễn',
     *     queue_number: '12',
     *     note: 'Khách hàng thanh toán bằng thẻ',
     *     buttons: [
     *       {
     *         title: 'Chi tiết đơn hàng',
     *         image_icon: 'https://stc-zmp.zadn.vn/oa/basket.png',
     *         url: 'https://zalo.me/s/194839900003483517/'
     *       },
     *       {
     *         title: 'Đánh giá',
     *         image_icon: 'https://stc-zmp.zadn.vn/oa/star.png',
     *         url: 'https://zalo.me/s/194839900003483517/'
     *       }
     *     ]
     *   }
     * );
     *
     * // Kiểm tra quota
     * if (result.quota) {
     *   console.log('Quota type:', result.quota.quota_type);
     *   console.log('Remaining:', result.quota.remain);
     * }
     * ```
     */
    sendMessage(accessToken: string, miniappMessageToken: string, userId: string, templateType: string, templateData: Record<string, any>): Promise<MiniAppMessageResponse>;
    /**
     * Gửi tin nhắn miniapp với template queue notification
     * (Template thông báo số thứ tự trong hàng đợi)
     *
     * @param accessToken Access token của Official Account
     * @param miniappMessageToken Token từ miniapp
     * @param userId ID của người nhận
     * @param customerName Tên khách hàng
     * @param queueNumber Số thứ tự trong hàng đợi
     * @param note Ghi chú thêm
     * @param buttons Danh sách các button kèm theo
     * @returns Thông tin tin nhắn đã gửi
     */
    sendQueueNotification(accessToken: string, miniappMessageToken: string, userId: string, customerName: string, queueNumber: string, note: string, buttons?: Array<{
        title: string;
        image_icon: string;
        url: string;
    }>): Promise<MiniAppMessageResponse>;
}
/**
 * Enum các loại quota (hạn mức) khi gửi tin nhắn miniapp
 */
declare enum MiniAppQuotaType {
    /** Tin Tư vấn miễn phí trong hạn mức 8 tin 48 giờ */
    REPLY = "reply",
    /** Tin Tư vấn miễn phí theo gói dịch vụ OA */
    SUB_QUOTA = "sub_quota",
    /** Tin trong hạn mức gói tính năng lẻ */
    PURCHASE_QUOTA = "purchase_quota",
    /** Tin trong hạn mức Redeem code */
    REWARD_QUOTA = "reward_quota"
}
/**
 * Enum các loại owner sở hữu Quota Package
 */
declare enum QuotaOwnerType {
    /** Thực thể sở hữu là OA */
    OA = "OA",
    /** Thực thể sở hữu là App */
    APP = "App"
}

/**
 * Main Zalo SDK class providing access to all Zalo APIs
 */
declare class ZaloSDK {
    private readonly client;
    private readonly logger;
    readonly auth: AuthService;
    readonly oa: OAService;
    readonly user: UserService;
    readonly zns: ZNSService;
    readonly groupMessage: GroupMessageService;
    readonly groupManagement: GroupManagementService;
    readonly article: ArticleService;
    readonly videoUpload: VideoUploadService;
    readonly consultation: ConsultationService;
    readonly transaction: TransactionService;
    readonly promotion: PromotionService;
    readonly generalMessage: GeneralMessageService;
    readonly messageManagement: MessageManagementService;
    readonly broadcast: BroadcastService;
    readonly purchase: PurchaseService;
    readonly anonymousMessage: AnonymousMessageService;
    readonly messageReaction: MessageReactionService;
    readonly miniappMessage: MiniAppMessageService;
    readonly config: Required<ZaloSDKConfig>;
    constructor(config: ZaloSDKConfig);
    /**
     * Quick method to get OA access token from authorization code
     */
    getOAAccessToken(code: string, redirectUri: string): Promise<AccessToken>;
    /**
     * Quick method to get Social access token from authorization code
     */
    getSocialAccessToken(code: string, redirectUri: string, codeVerifier?: string): Promise<AccessToken>;
    /**
     * Quick method to refresh OA access token
     */
    refreshOAAccessToken(refreshToken: string): Promise<AccessToken>;
    /**
     * Quick method to refresh Social access token
     */
    refreshSocialAccessToken(refreshToken: string): Promise<AccessToken>;
    /**
     * Quick method to get OA information
     */
    getOAInfo(accessToken: string): Promise<OAInfo>;
    /**
     * Quick method to get message quota
     */
    getMessageQuota(accessToken: string): Promise<MessageQuota>;
    /**
     * Quick method to get Social user information
     */
    getSocialUserInfo(accessToken: string, fields?: string): Promise<SocialUserInfo$1>;
    /**
     * Quick method to send consultation text message
     */
    sendConsultationText(accessToken: string, userId: string, text: string): Promise<SendMessageResponse>;
    /**
     * Quick method to get user information
     */
    getUserInfo(accessToken: string, userId: string): Promise<UserInfo>;
    /**
     * Quick method to get user list
     */
    getUserList(accessToken: string, request: UserListRequest): Promise<UserListResponse>;
    /**
     * Quick method to get all followers
     */
    getFollowers(accessToken: string): Promise<UserListResponse>;
    /**
     * Quick method to register webhook handlers
     */
    /**
     * Quick method to process webhook
     */
    /**
     * Create OA authorization URL with PKCE support
     * @deprecated Use auth.createOAAuthUrl() directly for full control over PKCE and state
     */
    createOAAuthUrl(redirectUri: string, state?: string): string;
    /**
     * Create OA authorization URL with full PKCE support
     * Returns both URL and state for enhanced security
     */
    createSecureOAAuthUrl(redirectUri: string, state?: string, enablePKCE?: boolean): OAAuthResult;
    /**
     * Create Social authorization URL
     */
    createSocialAuthUrl(redirectUri: string, state?: string): string;
    /**
     * Generate PKCE for Social API
     */
    generatePKCE(): PKCEConfig;
    /**
     * Validate access token
     */
    validateAccessToken(accessToken: string, scope?: "oa" | "social"): Promise<boolean>;
    /**
     * Get SDK version
     */
    getVersion(): string;
    /**
     * Get SDK configuration (without sensitive data)
     */
    getConfig(): Omit<Required<ZaloSDKConfig>, "appSecret">;
    /**
     * Enable or disable debug logging
     */
    setDebug(enabled: boolean): void;
    /**
     * Test SDK connectivity
     */
    testConnection(): Promise<boolean>;
    /**
     * Make a custom API request (advanced usage)
     */
    customRequest<T = any>(method: "GET" | "POST" | "PUT" | "DELETE", endpoint: string, accessToken: string, data?: any, params?: Record<string, any>): Promise<T>;
    /**
     * Upload file to Zalo API
     */
    uploadFile<T = any>(endpoint: string, accessToken: string, file: Buffer | NodeJS.ReadableStream, filename: string, additionalFields?: Record<string, any>): Promise<T>;
    /**
     * Dispose SDK resources
     */
    dispose(): void;
}

export { ARTICLE_CONSTRAINTS, type AccessToken, type AddGroupAdminEvent, type AddUserToTagEvent, type AdvancedUserFilters, type AllGroupMembersResponse, type AllGroupMembersWithDetailsResponse, type AnonymousFileAttachment, type AnonymousFileMessage, type AnonymousFileMessageRequest, type AnonymousImageAttachment, type AnonymousImageMessage, type AnonymousImageMessageRequest, type AnonymousMessage, type AnonymousMessageRecipient, type AnonymousMessageResponse, AnonymousMessageService, type AnonymousMessageWithAttachments, type AnonymousSendFileEvent, type AnonymousSendImageEvent, type AnonymousSendStickerEvent, type AnonymousSendTextEvent, type AnonymousStickerAttachment, type AnonymousStickerMessage, type AnonymousStickerMessageRequest, type AnonymousTextMessage, type AnonymousTextMessageRequest, type AnonymousUserSendMessageEvent, type AnonymousWebhookEvent, type ArticleBodyItem, type ArticleCite, type ArticleCover, type ArticleDetail, type ArticleDetailNormal, type ArticleDetailVideo, type ArticleListData, type ArticleListItem, type ArticleListRequest, type ArticleListResponse, type ArticleNormalRequest, type ArticleProcessResponse, type ArticleRemoveRequest, type ArticleRemoveResponse, type ArticleRequest, type ArticleResponse, ArticleService, ArticleStatus, ArticleType, type ArticleUpdateNormalRequest, type ArticleUpdateRequest, type ArticleUpdateResponse, type ArticleUpdateVideoRequest, type ArticleValidationError, type ArticleVerifyResponse, type ArticleVideoRequest, type Attachment, type AttachmentPayload, AttachmentType, type AudioPayload, type AuthCodeParams, AuthMethod, AuthScope, AuthService, type AuthUrls, type AuthorizationRequest, BROADCAST_AGE_CODES, BROADCAST_CITY_CODES, BROADCAST_GENDER_CODES, BROADCAST_LOCATION_CODES, BROADCAST_PLATFORM_CODES, type BannerConfig, BaseClient, type BaseMessage, type BaseWebhookEvent, type BeneficiaryType, BodyItemType, type BroadcastMessage, type BroadcastMessageAttachment, type BroadcastMessageElement, type BroadcastMessagePayload, type BroadcastMessageResult, type BroadcastRecipient, type BroadcastRequest, type BroadcastResponse, type BroadcastResponseData, BroadcastService, type BroadcastTarget, type BulkDetailItemResult, type BulkDetailOptions, type BulkDetailProgress, type BulkDetailResult, type BulkRemoveItemResult, type BulkRemoveOptions, type BulkRemoveProgress, type BulkRemoveResult, type BulkUserOperation, type BusinessCardMessage, type BusinessCardPayload, type CallInfo, type CallWebhookEvent, type ChangeOADailyQuotaEvent, type ChangeOATemplateTagsEvent, type ChangeTemplateQualityEvent, type ChangeTemplateQuotaEvent, type ChangeTemplateStatusEvent, CommentStatus, type ConfirmOrderRequest, type ConfirmOrderResponse, type ConfirmedOrderData, ConsoleLogger, type ConsultationFileMessage, type ConsultationImageMessage, type ConsultationQuoteMessage, type ConsultationRequestInfoMessage, ConsultationService, type ConsultationStickerMessage, type ConsultationTemplatePayload, type ConsultationTextMessage, type Conversation, type ConversationMessage, CoverType, type CreateGroupEvent, type CreateLabelRequest, type CreateOrderRequest, type CreateOrderResponse, type CreateOrderWithProductRequest, type CreateOrderWithRedeemRequest, type CreateUserInfoFieldRequest, type CursorPagination, type DeleteTagRequest, type DeleteUserInfoRequest, type EnhancedGroupMember, type EnhancedLinkPayload, type ExtendedMessage, type ExtensionPurchasedEvent, type ExtensionSubscriptionInfo, type FeedbackMessage, type FieldOption, type FileMessage, type FilePayload, type FileUpload, GMFProductType, GeneralMessageService, type GenericPromotionButton, type GetAllMembersProgress, type GetAllMembersWithDetailsProgress, type GetTagsResponse, type GetUserCustomInfoRequest, type GifPayload, type GroupAcceptPendingMembersRequest, type GroupAcceptPendingMembersResponse, type GroupActivity, type GroupActivityList, type GroupAdminActionRequest, type GroupApiResponse, type GroupAvatarUpdateRequest, type GroupBroadcast, type GroupConversationMessage, type GroupCreateRequest, type GroupCreateResponse, type GroupCreateResult, type GroupDeleteRequest, type GroupDeleteResponse, type GroupDetailResponse, type GroupFileMessage, type GroupImageMessage, type GroupInfo$1 as GroupInfo, type GroupInvitation, type GroupJoinRequest, type GroupJoinRequestList, type GroupInfo as GroupManagementInfo, GroupManagementService, type GroupWebhookEvent as GroupManagementWebhookEvent, type GroupMember, type GroupMemberActionRequest, type GroupMemberActionResponse, type GroupMemberInviteRequest, type GroupMemberList, type GroupMembersResponse, type GroupMentionMessage, type GroupMessage, type GroupMessageItem, type GroupMessageResult, type GroupMessageSchedule, GroupMessageService, type GroupMessageTemplate, type GroupPendingMember, type GroupPendingMembersResponse, type GroupPermission, type GroupQuota, type GroupQuotaAsset, type GroupQuotaMessageRequest, type GroupQuotaMessageResponse, type GroupRecentChat, type GroupRemoveMembersRequest, type GroupRemoveMembersResponse, type GroupSendMessageEvent, type GroupSettings, type GroupStatistics, type GroupStickerMessage, type GroupTextMessage, type GroupUpdateRequest, type GroupUpdateResponse, type GroupUserJoinEvent, type GroupUserLeaveEvent, type GroupWebhookEvent$1 as GroupWebhookEvent, type GroupsOfOAResponse, type HttpMethod, type ImageMessage, type ImagePayload, type JourneyAcknowledgedEvent, type JourneyTimeoutEvent, type LegacyWebhookEvent, type LinkPayload, type LocationCoordinates, type LocationPayload, type Logger, type Message, type MessageAttachment, type MessageEvent, type MessageItem, MessageManagementService, type MessageQuota, type MessageQuotaInfo, ReactionIcon as MessageReactionIconEnum, type ReactionMessage$1 as MessageReactionMessage, type MessageReactionRequest, type MessageReactionResponse, MessageReactionService, type MessageRecipient, MessageStatus, type TemplateMessage$1 as MessageTemplateMessage, type MessageWebhookEvent, type MiniAppMessage, type MiniAppMessageButton, type MiniAppMessageRequest, type MiniAppMessageResponse, MiniAppMessageService, MiniAppQuotaType, type MiniAppSendMessageRequest, type MultipleBroadcastProgress, type MultipleBroadcastResult, type MultipleGroupsProgressInfo, type MultiplePromotionProgress, type MultiplePromotionResult, type OAAuthResult, type OACallUserEvent, type OAInfo, type OAMessageWebhookEvent, type OAReactedMessageEvent, type OASendAnonymousFileEvent, type OASendAnonymousImageEvent, type OASendAnonymousStickerEvent, type OASendAnonymousTextEvent, type OASendConsentEvent, type OASendFileEvent, type OASendGifEvent, type OASendGroupAudioEvent, type OASendGroupBusinessCardEvent, type OASendGroupFileEvent, type OASendGroupGifEvent, type OASendGroupImageEvent, type OASendGroupLinkEvent, type OASendGroupLocationEvent, type OASendGroupStickerEvent, type OASendGroupTextEvent, type OASendGroupVideoEvent, type OASendImageEvent, type OASendListEvent, type OASendMessageEvent, type OASendStickerEvent, type OASendTemplateEvent, type OASendTextEvent, OAService, type OASettings, type OAStatistics, type OAVerificationRequest, OA_PRODUCTS, type OAuthConfig, type OAuthParams, type OffsetPagination, type OpenPhoneButton, type OpenPhonePayload, type OpenPhonePayloadInput, type OpenSmsButton, type OpenSmsPayload, type OpenSmsPayloadInput, type OpenUrlButton, type OpenUrlPayload, type OrderData, type PKCEConfig, PRODUCT_INFO, type PaginatedResponse, type PaginationParams, type PermissionRevokedData, type PermissionRevokedEvent, type PhonePayload, type ProductInfo, type PromotionBannerElement, type PromotionButton, type PromotionButtonInput, type PromotionButtonInputOpenPhone, type PromotionButtonInputOpenSms, type PromotionButtonInputOpenUrl, type PromotionButtonInputQueryHide, type PromotionButtonInputQueryShow, type PromotionElement, type PromotionHeaderElement, type PromotionMessage, PromotionService, type PromotionTableElement, type PromotionTextElement, type PromotionUserResult, PurchaseErrorCode, PurchaseService, type PurchaseValidationError, type QueryHideButton, type QueryPayload, type QueryShowButton, type QuotaAsset, type QuotaChange, type QuotaMessageRequest, type QuotaMessageResponse, QuotaOwnerType, QuotaType, type ReactIcon, type ReactRequestJoinGroupEvent, type ReactionMessage, type RefreshTokenParams, type RefreshTokenRequest, type RefreshTokenResponse, type RejectRequestJoinGroupEvent, type RemoveGroupAdminEvent, type RemoveTagFromUserRequest, type RequestConfig, type SeenMessage, type SendCustomMessageSequenceToMultipleUsersRequest, type SendCustomMessageSequenceToMultipleUsersResponse, type SendMessageListToMultipleGroupsRequest, type SendMessageListToMultipleGroupsResponse, type SendMessageRequest, type SendMessageResponse, type SendMessageSequenceRequest, type SendMessageSequenceResponse, type SendMessageSequenceToMultipleUsersRequest, type SendMessageSequenceToMultipleUsersResponse, type SenderAction, type ShopHasOrderEvent, type ShopWebhookEvent, type SmsPayload, type SocialAlbum, type SocialApiError, type SocialConversation, type SocialEvent, type SocialFeed, type SocialFriend, type SocialFriendsList, type SocialGroup, type SocialMessage, type SocialPhoto, type SocialPost, type SocialProfile, type SocialUserInfo$1 as SocialUserInfo, type SocialVideo, type StickerMessage, type StickerPayload, type SystemWebhookEvent, type TagLevelChange, type TagUserRequest, type TemplateAction, type TemplateActionType, type TemplateButton, type TemplateButtonType, type TemplateElement, type TemplateMessage, type TemplatePayload, type TemplateSource, type TemplateStatusChange, type TemplateStatusEvent, type TemplateType, type TextMessage, type TokenRequest, type TokenValidation, type TransactionMessage, TransactionService, type UpdateCustomInfoRequest, type UpdateGroupInfoEvent, type UpdateOAProfileRequest, type UpdateUserCustomInfoRequest, type UpdateUserInfoData, type UpdateUserInfoEvent, type UpdateUserInfoFieldRequest, type UpdateUserRequest, type UploadFileResponse, type UploadFileResult, type UploadImageResult, type UrlPayload, type UserActionWebhookEvent, type UserActivity, type UserAnalytics, type UserBehavior, type UserCallOAEvent, type UserClickButtonEvent, type UserClickChatNowEvent, type UserCustomField, type UserCustomFieldValue, type UserCustomInfo, type UserCustomInfoResponse, type UserCustomMessage, type UserExport, type UserFeedbackEvent, type UserFollowEvent, type UserGroupMessageEvent, type UserImport, type UserInfo, type UserInfoField, UserInfoFieldType, type UserInteraction, type UserJoinGroupEvent, type UserJourney, type UserLabel, type UserLabelRequest, type UserListRequest, type UserListResponse, type UserList as UserManagementList, type UserProfile as UserManagementProfile, type UserMessageWebhookEvent, type UserNote, type UserOutGroupEvent, type UserPreference, type UserProgressInfo, type UserReactedMessageEvent, type UserReceivedMessageEvent, type UserReplyConsentEvent, type UserRequestJoinGroupEvent, type UserSearch, type UserSearchResult, type UserSeenMessageEvent, type UserSegment, type UserSendAudioEvent, type UserSendBusinessCardEvent, type UserSendFileEvent, type UserSendGifEvent, type UserSendGroupAudioEvent, type UserSendGroupBusinessCardEvent, type UserSendGroupFileEvent, type UserSendGroupGifEvent, type UserSendGroupImageEvent, type UserSendGroupLinkEvent, type UserSendGroupLocationEvent, type UserSendGroupStickerEvent, type UserSendGroupTextEvent, type UserSendGroupVideoEvent, type UserSendImageEvent, type UserSendLinkEvent, type UserSendLocationEvent, type UserSendStickerEvent, type UserSendTextEvent, type UserSendVideoEvent, UserService, type UserSubmitInfoEvent, type UserSubmittedInfo, type UserTag, type UserTagList, type UserUnfollowEvent, VIDEO_CONSTRAINTS, type VideoFileConstraints, type VideoPayload, type VideoStatusResponse, type VideoUploadResponse, VideoUploadService, VideoUploadStatus, type WebhookBaseMessage, type WebhookCustomer, type WebhookEvent, type WebhookEventIdentifiers, WebhookEventName, type WebhookFollower, type WebhookHandler, type WebhookHandlers, type WebhookMessageWithAttachments, type WebhookPayload, type WebhookRecipient, type WebhookSender, type WebhookUser, type WebhookVerification, type WidgetFailedToSyncUserExternalIdEvent, type WidgetInteractionAcceptedEvent, type WidgetInteractionData, type WidgetSyncFailureData, type WidgetWebhookEvent, type ZNSAllowedContentTypes, type ZNSAnalytics, type ZNSAttachment, type ZNSBatchMessageStatusRequest, type ZNSBatchMessageStatusResponse, type ZNSButtonItem, ZNSButtonType, type ZNSButtonsComponent, type ZNSComponent, type ZNSCreateTemplateRequest, type ZNSCustomerRatingResponse, type ZNSDeliveryMessage, type ZNSDevModeMessage, type ZNSError, type ZNSHashPhoneMessage, type ZNSImageUsageType, type ZNSImagesComponent, type ZNSJourneyMessage, type ZNSLogoComponent, type ZNSMessage, type ZNSMessageStatusEvent, type ZNSMessageStatusInfo, type ZNSOAQualityInfo, type ZNSOTPComponent, type ZNSParagraphComponent, ZNSParamType, type ZNSPaymentComponent, type ZNSQualityHistoryList, type ZNSQuotaInfo, type ZNSRatingComponent, type ZNSRatingItem, type ZNSRsaMessage, type ZNSSendResult, ZNSService, type ZNSStatusInfo, type ZNSTableComponent, type ZNSTableRow, type ZNSTemplate, type ZNSTemplateComponent, type ZNSTemplateCreateEditResponse, type ZNSTemplateDetails, type ZNSTemplateLayout, type ZNSTemplateList, type ZNSTemplateParam, type ZNSTemplateSampleData, type ZNSTemplateStatus, ZNSTemplateTag, ZNSTemplateType, type ZNSTitleComponent, type ZNSUidMessage, type ZNSUidSendResult, type ZNSUpdateTemplateRequest, type ZNSUploadImageResult, type ZNSUserReceivedMessageEvent, ZNSValidation, type ZNSValidationComponent, type ZNSValidationResult, type ZNSVoucherComponent, type ZNSWebhookEvent, ZNS_BUTTON_TYPES, ZNS_PARAM_TYPES, ZNS_TEMPLATE_TAGS, ZNS_TEMPLATE_TAG_COMPATIBILITY, ZNS_TEMPLATE_TYPES, type ZaloApiResponse, ZaloClient, type ZaloErrorResponse, type ZaloResponse, ZaloSDK, type ZaloSDKConfig, ZaloSDKError, ZaloSDK as default, getMessageDirection, getWebhookEventAppId, getWebhookEventIdentifiers, getWebhookEventOaId, getWebhookEventRecipientId, getWebhookEventSenderId, hasAttachments, isAnonymousEvent, isBusinessCardEvent, isCallEvent, isConsentEvent, isExtensionEvent, isFeedbackEvent, isFollowEvent, isFromGroup, isFromPersonal, isGroupEvent, isGroupMessageEvent, isJourneyEvent, isMessageEvent, isOAMessageEvent, isOASendFileEvent, isOASendGifEvent, isOASendGroupFileEvent, isOASendGroupGifEvent, isOASendGroupImageEvent, isOASendGroupStickerEvent, isOASendGroupTextEvent, isOASendImageEvent, isOASendStickerEvent, isOASendTextEvent, isOAToGroupMessageEvent, isOAToUserMessageEvent, isPermissionEvent, isProductOrderRequest, isQuotaTemplateEvent, isReactionEvent, isRedeemOrderRequest, isShopEvent, isSystemEvent, isTemplateEvent, isUpdateUserInfoEvent, isUserActionEvent, isUserGroupMessageEvent, isUserMessageEvent, isUserSendGroupAudioEvent, isUserSendGroupFileEvent, isUserSendGroupImageEvent, isUserSendGroupTextEvent, isUserSendGroupVideoEvent, isWidgetEvent, isZNSEvent };
