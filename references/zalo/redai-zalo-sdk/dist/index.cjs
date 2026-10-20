var __create = Object.create;
var __defProp = Object.defineProperty;
var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
var __getOwnPropNames = Object.getOwnPropertyNames;
var __getProtoOf = Object.getPrototypeOf;
var __hasOwnProp = Object.prototype.hasOwnProperty;
var __export = (target, all) => {
  for (var name in all)
    __defProp(target, name, { get: all[name], enumerable: true });
};
var __copyProps = (to, from, except, desc) => {
  if (from && typeof from === "object" || typeof from === "function") {
    for (let key of __getOwnPropNames(from))
      if (!__hasOwnProp.call(to, key) && key !== except)
        __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
  }
  return to;
};
var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
  // If the importer is in node compatibility mode or this is not an ESM
  // file that has been converted to a CommonJS file using a Babel-
  // compatible transform (i.e. "__esModule" has not been set), then set
  // "default" to the CommonJS "module.exports" for node compatibility.
  isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
  mod
));
var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);

// src/index.ts
var index_exports = {};
__export(index_exports, {
  ARTICLE_CONSTRAINTS: () => ARTICLE_CONSTRAINTS,
  AnonymousMessageService: () => AnonymousMessageService,
  ArticleService: () => ArticleService,
  ArticleStatus: () => ArticleStatus,
  ArticleType: () => ArticleType,
  AttachmentType: () => AttachmentType,
  AuthMethod: () => AuthMethod,
  AuthScope: () => AuthScope,
  AuthService: () => AuthService,
  BROADCAST_AGE_CODES: () => BROADCAST_AGE_CODES,
  BROADCAST_CITY_CODES: () => BROADCAST_CITY_CODES,
  BROADCAST_GENDER_CODES: () => BROADCAST_GENDER_CODES,
  BROADCAST_LOCATION_CODES: () => BROADCAST_LOCATION_CODES,
  BROADCAST_PLATFORM_CODES: () => BROADCAST_PLATFORM_CODES,
  BaseClient: () => BaseClient,
  BodyItemType: () => BodyItemType,
  BroadcastService: () => BroadcastService,
  CommentStatus: () => CommentStatus,
  ConsoleLogger: () => ConsoleLogger,
  ConsultationService: () => ConsultationService,
  CoverType: () => CoverType,
  GMFProductType: () => GMFProductType,
  GeneralMessageService: () => GeneralMessageService,
  GroupManagementService: () => GroupManagementService,
  GroupMessageService: () => GroupMessageService,
  MessageManagementService: () => MessageManagementService,
  MessageReactionIconEnum: () => ReactionIcon,
  MessageReactionService: () => MessageReactionService,
  MiniAppMessageService: () => MiniAppMessageService,
  MiniAppQuotaType: () => MiniAppQuotaType,
  OAService: () => OAService,
  OA_PRODUCTS: () => OA_PRODUCTS,
  PRODUCT_INFO: () => PRODUCT_INFO,
  PromotionService: () => PromotionService,
  PurchaseErrorCode: () => PurchaseErrorCode,
  PurchaseService: () => PurchaseService,
  QuotaOwnerType: () => QuotaOwnerType,
  QuotaType: () => QuotaType,
  TransactionService: () => TransactionService,
  UserInfoFieldType: () => UserInfoFieldType,
  UserService: () => UserService,
  VIDEO_CONSTRAINTS: () => VIDEO_CONSTRAINTS,
  VideoUploadService: () => VideoUploadService,
  VideoUploadStatus: () => VideoUploadStatus,
  WebhookEventName: () => WebhookEventName,
  ZNSButtonType: () => ZNSButtonType,
  ZNSParamType: () => ZNSParamType,
  ZNSService: () => ZNSService,
  ZNSTemplateTag: () => ZNSTemplateTag,
  ZNSTemplateType: () => ZNSTemplateType,
  ZNSValidation: () => ZNSValidation,
  ZNS_BUTTON_TYPES: () => ZNS_BUTTON_TYPES,
  ZNS_PARAM_TYPES: () => ZNS_PARAM_TYPES,
  ZNS_TEMPLATE_TAGS: () => ZNS_TEMPLATE_TAGS,
  ZNS_TEMPLATE_TAG_COMPATIBILITY: () => ZNS_TEMPLATE_TAG_COMPATIBILITY,
  ZNS_TEMPLATE_TYPES: () => ZNS_TEMPLATE_TYPES,
  ZaloClient: () => ZaloClient,
  ZaloSDK: () => ZaloSDK,
  ZaloSDKError: () => ZaloSDKError,
  default: () => ZaloSDK,
  getMessageDirection: () => getMessageDirection,
  getWebhookEventAppId: () => getWebhookEventAppId,
  getWebhookEventIdentifiers: () => getWebhookEventIdentifiers,
  getWebhookEventOaId: () => getWebhookEventOaId,
  getWebhookEventRecipientId: () => getWebhookEventRecipientId,
  getWebhookEventSenderId: () => getWebhookEventSenderId,
  hasAttachments: () => hasAttachments,
  isAnonymousEvent: () => isAnonymousEvent,
  isBusinessCardEvent: () => isBusinessCardEvent,
  isCallEvent: () => isCallEvent,
  isConsentEvent: () => isConsentEvent,
  isExtensionEvent: () => isExtensionEvent,
  isFeedbackEvent: () => isFeedbackEvent,
  isFollowEvent: () => isFollowEvent,
  isFromGroup: () => isFromGroup,
  isFromPersonal: () => isFromPersonal,
  isGroupEvent: () => isGroupEvent,
  isGroupMessageEvent: () => isGroupMessageEvent,
  isJourneyEvent: () => isJourneyEvent,
  isMessageEvent: () => isMessageEvent,
  isOAMessageEvent: () => isOAMessageEvent,
  isOASendFileEvent: () => isOASendFileEvent,
  isOASendGifEvent: () => isOASendGifEvent,
  isOASendGroupFileEvent: () => isOASendGroupFileEvent,
  isOASendGroupGifEvent: () => isOASendGroupGifEvent,
  isOASendGroupImageEvent: () => isOASendGroupImageEvent,
  isOASendGroupStickerEvent: () => isOASendGroupStickerEvent,
  isOASendGroupTextEvent: () => isOASendGroupTextEvent,
  isOASendImageEvent: () => isOASendImageEvent,
  isOASendStickerEvent: () => isOASendStickerEvent,
  isOASendTextEvent: () => isOASendTextEvent,
  isOAToGroupMessageEvent: () => isOAToGroupMessageEvent,
  isOAToUserMessageEvent: () => isOAToUserMessageEvent,
  isPermissionEvent: () => isPermissionEvent,
  isProductOrderRequest: () => isProductOrderRequest,
  isQuotaTemplateEvent: () => isQuotaTemplateEvent,
  isReactionEvent: () => isReactionEvent,
  isRedeemOrderRequest: () => isRedeemOrderRequest,
  isShopEvent: () => isShopEvent,
  isSystemEvent: () => isSystemEvent,
  isTemplateEvent: () => isTemplateEvent,
  isUpdateUserInfoEvent: () => isUpdateUserInfoEvent,
  isUserActionEvent: () => isUserActionEvent,
  isUserGroupMessageEvent: () => isUserGroupMessageEvent,
  isUserMessageEvent: () => isUserMessageEvent,
  isUserSendGroupAudioEvent: () => isUserSendGroupAudioEvent,
  isUserSendGroupFileEvent: () => isUserSendGroupFileEvent,
  isUserSendGroupImageEvent: () => isUserSendGroupImageEvent,
  isUserSendGroupTextEvent: () => isUserSendGroupTextEvent,
  isUserSendGroupVideoEvent: () => isUserSendGroupVideoEvent,
  isWidgetEvent: () => isWidgetEvent,
  isZNSEvent: () => isZNSEvent
});
module.exports = __toCommonJS(index_exports);

// src/types/common.ts
var ZaloSDKError = class extends Error {
  constructor(message, code = -1, details) {
    super(message);
    this.name = "ZaloSDKError";
    this.code = code;
    this.details = details;
  }
};
var ConsoleLogger = class {
  constructor(enableDebug = false) {
    this.enableDebug = enableDebug;
  }
  debug(message, ...args) {
    if (this.enableDebug) {
      console.debug(`[ZALO-SDK:DEBUG] ${message}`, ...args);
    }
  }
  info(message, ...args) {
    console.info(`[ZALO-SDK:INFO] ${message}`, ...args);
  }
  warn(message, ...args) {
    console.warn(`[ZALO-SDK:WARN] ${message}`, ...args);
  }
  error(message, ...args) {
    console.error(`[ZALO-SDK:ERROR] ${message}`, ...args);
  }
};

// src/types/auth.ts
var AuthScope = /* @__PURE__ */ ((AuthScope2) => {
  AuthScope2["OA"] = "oa";
  AuthScope2["SOCIAL"] = "social";
  AuthScope2["ZNS"] = "zns";
  return AuthScope2;
})(AuthScope || {});
var AuthMethod = /* @__PURE__ */ ((AuthMethod2) => {
  AuthMethod2["AUTHORIZATION_CODE"] = "authorization_code";
  AuthMethod2["REFRESH_TOKEN"] = "refresh_token";
  return AuthMethod2;
})(AuthMethod || {});

// src/types/oa.ts
var GMFProductType = /* @__PURE__ */ ((GMFProductType2) => {
  GMFProductType2["GMF10"] = "gmf10";
  GMFProductType2["GMF50"] = "gmf50";
  GMFProductType2["GMF100"] = "gmf100";
  return GMFProductType2;
})(GMFProductType || {});
var QuotaType = /* @__PURE__ */ ((QuotaType3) => {
  QuotaType3["SUB_QUOTA"] = "sub_quota";
  QuotaType3["PURCHASE_QUOTA"] = "purchase_quota";
  QuotaType3["REWARD_QUOTA"] = "reward_quota";
  return QuotaType3;
})(QuotaType || {});

// src/types/user.ts
var UserInfoFieldType = /* @__PURE__ */ ((UserInfoFieldType2) => {
  UserInfoFieldType2["TEXT"] = "text";
  UserInfoFieldType2["NUMBER"] = "number";
  UserInfoFieldType2["DATE"] = "date";
  UserInfoFieldType2["SELECT"] = "select";
  return UserInfoFieldType2;
})(UserInfoFieldType || {});

// src/types/zns.ts
var ZNSTemplateType = /* @__PURE__ */ ((ZNSTemplateType2) => {
  ZNSTemplateType2[ZNSTemplateType2["CUSTOM"] = 1] = "CUSTOM";
  ZNSTemplateType2[ZNSTemplateType2["AUTHENTICATION"] = 2] = "AUTHENTICATION";
  ZNSTemplateType2[ZNSTemplateType2["PAYMENT_REQUEST"] = 3] = "PAYMENT_REQUEST";
  ZNSTemplateType2[ZNSTemplateType2["VOUCHER"] = 4] = "VOUCHER";
  ZNSTemplateType2[ZNSTemplateType2["SERVICE_RATING"] = 5] = "SERVICE_RATING";
  return ZNSTemplateType2;
})(ZNSTemplateType || {});
var ZNSTemplateTag = /* @__PURE__ */ ((ZNSTemplateTag2) => {
  ZNSTemplateTag2["TRANSACTION"] = "1";
  ZNSTemplateTag2["CUSTOMER_CARE"] = "2";
  ZNSTemplateTag2["PROMOTION"] = "3";
  return ZNSTemplateTag2;
})(ZNSTemplateTag || {});
var ZNSButtonType = /* @__PURE__ */ ((ZNSButtonType2) => {
  ZNSButtonType2[ZNSButtonType2["ENTERPRISE_PAGE"] = 1] = "ENTERPRISE_PAGE";
  ZNSButtonType2[ZNSButtonType2["PHONE"] = 2] = "PHONE";
  ZNSButtonType2[ZNSButtonType2["OA_PAGE"] = 3] = "OA_PAGE";
  ZNSButtonType2[ZNSButtonType2["ZALO_MINI_APP"] = 4] = "ZALO_MINI_APP";
  ZNSButtonType2[ZNSButtonType2["DOWNLOAD_APP"] = 5] = "DOWNLOAD_APP";
  ZNSButtonType2[ZNSButtonType2["PRODUCT_DISTRIBUTION"] = 6] = "PRODUCT_DISTRIBUTION";
  ZNSButtonType2[ZNSButtonType2["OTHER_WEB_APP"] = 7] = "OTHER_WEB_APP";
  ZNSButtonType2[ZNSButtonType2["OTHER_APP"] = 8] = "OTHER_APP";
  ZNSButtonType2[ZNSButtonType2["OA_ARTICLE"] = 9] = "OA_ARTICLE";
  ZNSButtonType2[ZNSButtonType2["COPY"] = 10] = "COPY";
  ZNSButtonType2[ZNSButtonType2["PAY_NOW"] = 11] = "PAY_NOW";
  ZNSButtonType2[ZNSButtonType2["VIEW_DETAILS"] = 12] = "VIEW_DETAILS";
  return ZNSButtonType2;
})(ZNSButtonType || {});
var ZNSParamType = /* @__PURE__ */ ((ZNSParamType2) => {
  ZNSParamType2["CUSTOMER_NAME"] = "1";
  ZNSParamType2["PHONE_NUMBER"] = "2";
  ZNSParamType2["ADDRESS"] = "3";
  ZNSParamType2["CODE"] = "4";
  ZNSParamType2["CUSTOM_LABEL"] = "5";
  ZNSParamType2["TRANSACTION_STATUS"] = "6";
  ZNSParamType2["CONTACT_INFO"] = "7";
  ZNSParamType2["GENDER_TITLE"] = "8";
  ZNSParamType2["PRODUCT_BRAND"] = "9";
  ZNSParamType2["QUANTITY_AMOUNT"] = "10";
  ZNSParamType2["TIME"] = "11";
  ZNSParamType2["OTP"] = "12";
  ZNSParamType2["URL"] = "13";
  ZNSParamType2["CURRENCY"] = "14";
  ZNSParamType2["BANK_TRANSFER_NOTE"] = "15";
  return ZNSParamType2;
})(ZNSParamType || {});

// src/types/article.ts
var ARTICLE_CONSTRAINTS = {
  TITLE_MAX_LENGTH: 150,
  AUTHOR_MAX_LENGTH: 50,
  DESCRIPTION_MAX_LENGTH: 300,
  IMAGE_MAX_SIZE: 1024 * 1024,
  // 1MB
  VIDEO_MAX_SIZE: 50 * 1024 * 1024,
  // 50MB
  LIST_MAX_LIMIT: 10
  // Zalo API maximum limit is 10
};
var VIDEO_CONSTRAINTS = {
  maxSize: 50 * 1024 * 1024,
  // 50MB
  allowedExtensions: [".mp4", ".avi"],
  allowedMimeTypes: ["video/mp4", "video/avi", "video/x-msvideo"]
};
var ArticleStatus = /* @__PURE__ */ ((ArticleStatus2) => {
  ArticleStatus2["SHOW"] = "show";
  ArticleStatus2["HIDE"] = "hide";
  return ArticleStatus2;
})(ArticleStatus || {});
var CommentStatus = /* @__PURE__ */ ((CommentStatus2) => {
  CommentStatus2["SHOW"] = "show";
  CommentStatus2["HIDE"] = "hide";
  return CommentStatus2;
})(CommentStatus || {});
var ArticleType = /* @__PURE__ */ ((ArticleType2) => {
  ArticleType2["NORMAL"] = "normal";
  ArticleType2["VIDEO"] = "video";
  return ArticleType2;
})(ArticleType || {});
var CoverType = /* @__PURE__ */ ((CoverType2) => {
  CoverType2["PHOTO"] = "photo";
  CoverType2["VIDEO"] = "video";
  return CoverType2;
})(CoverType || {});
var BodyItemType = /* @__PURE__ */ ((BodyItemType2) => {
  BodyItemType2["TEXT"] = "text";
  BodyItemType2["IMAGE"] = "image";
  BodyItemType2["VIDEO"] = "video";
  BodyItemType2["PRODUCT"] = "product";
  return BodyItemType2;
})(BodyItemType || {});
var VideoUploadStatus = /* @__PURE__ */ ((VideoUploadStatus2) => {
  VideoUploadStatus2[VideoUploadStatus2["UNKNOWN"] = 0] = "UNKNOWN";
  VideoUploadStatus2[VideoUploadStatus2["SUCCESS"] = 1] = "SUCCESS";
  VideoUploadStatus2[VideoUploadStatus2["LOCKED"] = 2] = "LOCKED";
  VideoUploadStatus2[VideoUploadStatus2["PROCESSING"] = 3] = "PROCESSING";
  VideoUploadStatus2[VideoUploadStatus2["FAILED"] = 4] = "FAILED";
  VideoUploadStatus2[VideoUploadStatus2["DELETED"] = 5] = "DELETED";
  return VideoUploadStatus2;
})(VideoUploadStatus || {});

// src/types/broadcast.ts
var BROADCAST_CITY_CODES = {
  "\u0110\u1ED3ng Th\xE1p": "0",
  "B\xECnh Ph\u01B0\u1EDBc": "1",
  "Ninh B\xECnh": "2",
  "B\u1EA1c Li\xEAu": "3",
  "H\u1ED3 Ch\xED Minh": "4",
  "V\u0129nh Long": "5",
  "L\xE2m \u0110\u1ED3ng": "6",
  "Y\xEAn B\xE1i": "7",
  "H\xE0 Nam": "8",
  "H\xE0 N\u1ED9i": "9",
  "H\u1EA3i D\u01B0\u01A1ng": "10",
  "H\u1EADu Giang": "11",
  "An Giang": "12",
  "Tr\xE0 Vinh": "13",
  "Ti\u1EC1n Giang": "14",
  "T\xE2y Ninh": "15",
  "\u0110\u1ED3ng Nai": "16",
  "\u0110\u1EAFk L\u1EAFk": "17",
  "B\xECnh \u0110\u1ECBnh": "18",
  "Kon Tum": "19",
  "\u0110\xE0 N\u1EB5ng": "20",
  "B\u1EAFc Giang": "21",
  "B\u1EAFc K\u1EA1n": "22",
  "\u0110i\u1EC7n Bi\xEAn": "23",
  "H\xF2a B\xECnh": "24",
  "Th\xE1i B\xECnh": "25",
  "V\u0129nh Ph\xFAc": "26",
  "H\xE0 Giang": "27",
  "Ki\xEAn Giang": "28",
  "B\xECnh D\u01B0\u01A1ng": "29",
  "B\xECnh Thu\u1EADn": "30",
  "\u0110\u1EAFk N\xF4ng": "31",
  "Kh\xE1nh H\xF2a": "32",
  "Gia Lai": "33",
  "Qu\u1EA3ng Nam": "34",
  "Qu\u1EA3ng Tr\u1ECB": "35",
  "H\xE0 T\u0129nh": "36",
  "H\u01B0ng Y\xEAn": "37",
  "Qu\u1EA3ng Ninh": "38",
  "Thanh H\xF3a": "39",
  "Ph\xFA Th\u1ECD": "40",
  "Lai Ch\xE2u": "41",
  "Th\xE1i Nguy\xEAn": "42",
  "Cao B\u1EB1ng": "43",
  "C\xE0 Mau": "44",
  "C\u1EA7n Th\u01A1": "45",
  "S\xF3c Tr\u0103ng": "46",
  "B\u1EBFn Tre": "47",
  "Long An": "48",
  "B\xE0 R\u1ECBa V\u0169ng T\xE0u": "49",
  "Ninh Thu\u1EADn": "50",
  "Ph\xFA Y\xEAn": "51",
  "Qu\xE3ng Ng\xE3i": "52",
  "Th\u1EEBa Thi\xEAn Hu\u1EBF": "53",
  "Qu\u1EA3ng B\xECnh": "54",
  "Ngh\u1EC7 An": "55",
  "Nam \u0110\u1ECBnh": "56",
  "H\u1EA3i Ph\xF2ng": "57",
  "L\u1EA1ng S\u01A1n": "58",
  "L\xE0o Cai": "59",
  "S\u01A1n La": "60",
  "B\u1EAFc Ninh": "61",
  "Tuy\xEAn Quang": "62",
  "Kh\xF4ng Thu\u1ED9c Vi\u1EC7t Nam": "63"
};
var BROADCAST_AGE_CODES = {
  "0-12": "0",
  "13-17": "1",
  "18-24": "2",
  "25-34": "3",
  "35-44": "4",
  "45-54": "5",
  "55-64": "6",
  "65+": "7"
};
var BROADCAST_GENDER_CODES = {
  "ALL": "0",
  "MALE": "1",
  "FEMALE": "2"
};
var BROADCAST_LOCATION_CODES = {
  "NORTH": "0",
  "CENTRAL": "1",
  "SOUTH": "2"
};
var BROADCAST_PLATFORM_CODES = {
  "IOS": "1",
  "ANDROID": "2",
  "WINDOWS_PHONE": "3"
};

// src/types/purchase.ts
function isProductOrderRequest(request) {
  return "product_id" in request;
}
function isRedeemOrderRequest(request) {
  return "redeem_code" in request;
}
var OA_PRODUCTS = {
  // Sản phẩm OA Subscription
  OA_ADVANCED_6M: 866836109767958100,
  OA_ADVANCED_12M: 1302963828004138500,
  OA_PREMIUM_6M: 757295129578622300,
  OA_PREMIUM_12M: 3071996459978069e3,
  // Sản phẩm Quota khởi tạo GMF
  GMF_10_MEMBERS: 739448264820568800,
  GMF_50_MEMBERS: 2405469629611791400,
  GMF_100_MEMBERS: 2275350247265190700,
  GMF_1000_MEMBERS: 36785572333921e5,
  // Sản phẩm Gói quota tin nhắn OA
  TRANSACTION_5K: 4609774892111012e3,
  TRANSACTION_50K: 2038298593847789600,
  TRANSACTION_500K: 2544831667685732e3
};
var PRODUCT_INFO = {
  // OA Subscription
  [OA_PRODUCTS.OA_ADVANCED_6M]: {
    id: OA_PRODUCTS.OA_ADVANCED_6M,
    name: "G\xF3i OA N\xE2ng cao 6 th\xE1ng",
    beneficiary: ["OA"],
    category: "subscription"
  },
  [OA_PRODUCTS.OA_ADVANCED_12M]: {
    id: OA_PRODUCTS.OA_ADVANCED_12M,
    name: "G\xF3i OA N\xE2ng cao 12 th\xE1ng",
    beneficiary: ["OA"],
    category: "subscription"
  },
  [OA_PRODUCTS.OA_PREMIUM_6M]: {
    id: OA_PRODUCTS.OA_PREMIUM_6M,
    name: "G\xF3i OA Premium 6 th\xE1ng",
    beneficiary: ["OA"],
    category: "subscription"
  },
  [OA_PRODUCTS.OA_PREMIUM_12M]: {
    id: OA_PRODUCTS.OA_PREMIUM_12M,
    name: "G\xF3i OA Premium 12 th\xE1ng",
    beneficiary: ["OA"],
    category: "subscription"
  },
  // GMF Products
  [OA_PRODUCTS.GMF_10_MEMBERS]: {
    id: OA_PRODUCTS.GMF_10_MEMBERS,
    name: "GMF t\u1ED1i \u0111a 10 th\xE0nh vi\xEAn",
    beneficiary: ["OA"],
    category: "gmf"
  },
  [OA_PRODUCTS.GMF_50_MEMBERS]: {
    id: OA_PRODUCTS.GMF_50_MEMBERS,
    name: "GMF t\u1ED1i \u0111a 50 th\xE0nh vi\xEAn",
    beneficiary: ["OA"],
    category: "gmf"
  },
  [OA_PRODUCTS.GMF_100_MEMBERS]: {
    id: OA_PRODUCTS.GMF_100_MEMBERS,
    name: "GMF t\u1ED1i \u0111a 100 th\xE0nh vi\xEAn",
    beneficiary: ["OA"],
    category: "gmf"
  },
  [OA_PRODUCTS.GMF_1000_MEMBERS]: {
    id: OA_PRODUCTS.GMF_1000_MEMBERS,
    name: "GMF t\u1ED1i \u0111a 1000 th\xE0nh vi\xEAn",
    beneficiary: ["OA"],
    category: "gmf"
  },
  // Transaction Quota
  [OA_PRODUCTS.TRANSACTION_5K]: {
    id: OA_PRODUCTS.TRANSACTION_5K,
    name: "G\xF3i 5k tin Giao d\u1ECBch (1 th\xE1ng)",
    beneficiary: ["APP", "OA"],
    category: "quota"
  },
  [OA_PRODUCTS.TRANSACTION_50K]: {
    id: OA_PRODUCTS.TRANSACTION_50K,
    name: "G\xF3i 50k tin Giao d\u1ECBch (1 th\xE1ng)",
    beneficiary: ["APP", "OA"],
    category: "quota"
  },
  [OA_PRODUCTS.TRANSACTION_500K]: {
    id: OA_PRODUCTS.TRANSACTION_500K,
    name: "G\xF3i 500k tin Giao d\u1ECBch (1 th\xE1ng)",
    beneficiary: ["APP", "OA"],
    category: "quota"
  }
};
var PurchaseErrorCode = /* @__PURE__ */ ((PurchaseErrorCode2) => {
  PurchaseErrorCode2[PurchaseErrorCode2["INVALID_BENEFICIARY"] = 1001] = "INVALID_BENEFICIARY";
  PurchaseErrorCode2[PurchaseErrorCode2["INVALID_PRODUCT_ID"] = 1002] = "INVALID_PRODUCT_ID";
  PurchaseErrorCode2[PurchaseErrorCode2["INVALID_REDEEM_CODE"] = 1003] = "INVALID_REDEEM_CODE";
  PurchaseErrorCode2[PurchaseErrorCode2["INVALID_VOUCHER_CODE"] = 1004] = "INVALID_VOUCHER_CODE";
  PurchaseErrorCode2[PurchaseErrorCode2["PRODUCT_NOT_AVAILABLE"] = 1005] = "PRODUCT_NOT_AVAILABLE";
  PurchaseErrorCode2[PurchaseErrorCode2["INSUFFICIENT_BALANCE"] = 1006] = "INSUFFICIENT_BALANCE";
  PurchaseErrorCode2[PurchaseErrorCode2["ORDER_TIME_RESTRICTED"] = 1007] = "ORDER_TIME_RESTRICTED";
  PurchaseErrorCode2[PurchaseErrorCode2["DUPLICATE_ORDER"] = 1008] = "DUPLICATE_ORDER";
  PurchaseErrorCode2[PurchaseErrorCode2["PERMISSION_DENIED"] = 1009] = "PERMISSION_DENIED";
  PurchaseErrorCode2[PurchaseErrorCode2["INVALID_ORDER_ID"] = 1010] = "INVALID_ORDER_ID";
  PurchaseErrorCode2[PurchaseErrorCode2["INVALID_VERIFIED_TOKEN"] = 1011] = "INVALID_VERIFIED_TOKEN";
  PurchaseErrorCode2[PurchaseErrorCode2["ORDER_EXPIRED"] = 1012] = "ORDER_EXPIRED";
  PurchaseErrorCode2[PurchaseErrorCode2["ORDER_ALREADY_CONFIRMED"] = 1013] = "ORDER_ALREADY_CONFIRMED";
  PurchaseErrorCode2[PurchaseErrorCode2["SYSTEM_ERROR"] = 9999] = "SYSTEM_ERROR";
  return PurchaseErrorCode2;
})(PurchaseErrorCode || {});

// src/constants/zns.constants.ts
var ZNS_TEMPLATE_TYPES = {
  [1 /* CUSTOM */]: {
    value: 1 /* CUSTOM */,
    name: "ZNS t\xF9y ch\u1EC9nh",
    description: "Template t\xF9y ch\u1EC9nh cho c\xE1c m\u1EE5c \u0111\xEDch kh\xE1c nhau"
  },
  [2 /* AUTHENTICATION */]: {
    value: 2 /* AUTHENTICATION */,
    name: "ZNS x\xE1c th\u1EF1c",
    description: "Template cho vi\u1EC7c x\xE1c th\u1EF1c OTP, m\xE3 PIN"
  },
  [3 /* PAYMENT_REQUEST */]: {
    value: 3 /* PAYMENT_REQUEST */,
    name: "ZNS y\xEAu c\u1EA7u thanh to\xE1n",
    description: "Template y\xEAu c\u1EA7u thanh to\xE1n"
  },
  [4 /* VOUCHER */]: {
    value: 4 /* VOUCHER */,
    name: "ZNS voucher",
    description: "Template g\u1EEDi voucher, khuy\u1EBFn m\xE3i"
  },
  [5 /* SERVICE_RATING */]: {
    value: 5 /* SERVICE_RATING */,
    name: "ZNS \u0111\xE1nh gi\xE1 d\u1ECBch v\u1EE5",
    description: "Template y\xEAu c\u1EA7u \u0111\xE1nh gi\xE1 d\u1ECBch v\u1EE5"
  }
};
var ZNS_TEMPLATE_TAGS = {
  ["1" /* TRANSACTION */]: {
    value: "1" /* TRANSACTION */,
    name: "Transaction",
    description: "Giao d\u1ECBch, thanh to\xE1n"
  },
  ["2" /* CUSTOMER_CARE */]: {
    value: "2" /* CUSTOMER_CARE */,
    name: "Customer Care",
    description: "Ch\u0103m s\xF3c kh\xE1ch h\xE0ng"
  },
  ["3" /* PROMOTION */]: {
    value: "3" /* PROMOTION */,
    name: "Promotion",
    description: "Khuy\u1EBFn m\xE3i, qu\u1EA3ng c\xE1o"
  }
};
var ZNS_BUTTON_TYPES = {
  [1 /* ENTERPRISE_PAGE */]: {
    value: 1 /* ENTERPRISE_PAGE */,
    name: "Trang doanh nghi\u1EC7p",
    description: "\u0110\u1EBFn trang c\u1EE7a doanh nghi\u1EC7p ho\u1EB7c trang tra c\u1EE9u ho\xE1 \u0111\u01A1n \u0111i\u1EC7n t\u1EED"
  },
  [2 /* PHONE */]: {
    value: 2 /* PHONE */,
    name: "Phone",
    description: "G\u1ECDi \u0111i\u1EC7n tho\u1EA1i"
  },
  [3 /* OA_PAGE */]: {
    value: 3 /* OA_PAGE */,
    name: "Trang OA",
    description: "\u0110\u1EBFn trang th\xF4ng tin OA"
  },
  [4 /* ZALO_MINI_APP */]: {
    value: 4 /* ZALO_MINI_APP */,
    name: "Zalo Mini App",
    description: "\u0110\u1EBFn \u1EE9ng d\u1EE5ng Zalo Mini App c\u1EE7a doanh nghi\u1EC7p"
  },
  [5 /* DOWNLOAD_APP */]: {
    value: 5 /* DOWNLOAD_APP */,
    name: "T\u1EA3i App",
    description: "\u0110\u1EBFn trang t\u1EA3i \u1EE9ng d\u1EE5ng"
  },
  [6 /* PRODUCT_DISTRIBUTION */]: {
    value: 6 /* PRODUCT_DISTRIBUTION */,
    name: "Ph\xE2n ph\u1ED1i s\u1EA3n ph\u1EA9m",
    description: "\u0110\u1EBFn trang ph\xE2n ph\u1ED1i s\u1EA3n ph\u1EA9m"
  },
  [7 /* OTHER_WEB_APP */]: {
    value: 7 /* OTHER_WEB_APP */,
    name: "Web/App kh\xE1c",
    description: "\u0110\u1EBFn trang web/Zalo Mini App kh\xE1c"
  },
  [8 /* OTHER_APP */]: {
    value: 8 /* OTHER_APP */,
    name: "App kh\xE1c",
    description: "\u0110\u1EBFn \u1EE9ng d\u1EE5ng kh\xE1c"
  },
  [9 /* OA_ARTICLE */]: {
    value: 9 /* OA_ARTICLE */,
    name: "B\xE0i vi\u1EBFt OA",
    description: "\u0110\u1EBFn b\xE0i vi\u1EBFt c\u1EE7a OA"
  },
  [10 /* COPY */]: {
    value: 10 /* COPY */,
    name: "Copy",
    description: "Sao ch\xE9p n\u1ED9i dung"
  },
  [11 /* PAY_NOW */]: {
    value: 11 /* PAY_NOW */,
    name: "Thanh to\xE1n ngay",
    description: "Thanh to\xE1n ngay"
  },
  [12 /* VIEW_DETAILS */]: {
    value: 12 /* VIEW_DETAILS */,
    name: "Xem chi ti\u1EBFt",
    description: "Xem chi ti\u1EBFt"
  }
};
var ZNS_PARAM_TYPES = {
  ["1" /* CUSTOMER_NAME */]: {
    value: "1" /* CUSTOMER_NAME */,
    name: "T\xEAn kh\xE1ch h\xE0ng",
    maxLength: 30
  },
  ["2" /* PHONE_NUMBER */]: {
    value: "2" /* PHONE_NUMBER */,
    name: "S\u1ED1 \u0111i\u1EC7n tho\u1EA1i",
    maxLength: 15
  },
  ["3" /* ADDRESS */]: {
    value: "3" /* ADDRESS */,
    name: "\u0110\u1ECBa ch\u1EC9",
    maxLength: 200
  },
  ["4" /* CODE */]: {
    value: "4" /* CODE */,
    name: "M\xE3 s\u1ED1",
    maxLength: 30
  },
  ["5" /* CUSTOM_LABEL */]: {
    value: "5" /* CUSTOM_LABEL */,
    name: "Nh\xE3n t\xF9y ch\u1EC9nh",
    maxLength: 30
  },
  ["6" /* TRANSACTION_STATUS */]: {
    value: "6" /* TRANSACTION_STATUS */,
    name: "Tr\u1EA1ng th\xE1i giao d\u1ECBch",
    maxLength: 30
  },
  ["7" /* CONTACT_INFO */]: {
    value: "7" /* CONTACT_INFO */,
    name: "Th\xF4ng tin li\xEAn h\u1EC7",
    maxLength: 50
  },
  ["8" /* GENDER_TITLE */]: {
    value: "8" /* GENDER_TITLE */,
    name: "Gi\u1EDBi t\xEDnh / Danh x\u01B0ng",
    maxLength: 5
  },
  ["9" /* PRODUCT_BRAND */]: {
    value: "9" /* PRODUCT_BRAND */,
    name: "T\xEAn s\u1EA3n ph\u1EA9m / Th\u01B0\u01A1ng hi\u1EC7u",
    maxLength: 200
  },
  ["10" /* QUANTITY_AMOUNT */]: {
    value: "10" /* QUANTITY_AMOUNT */,
    name: "S\u1ED1 l\u01B0\u1EE3ng / S\u1ED1 ti\u1EC1n",
    maxLength: 20
  },
  ["11" /* TIME */]: {
    value: "11" /* TIME */,
    name: "Th\u1EDDi gian",
    maxLength: 20
  },
  ["12" /* OTP */]: {
    value: "12" /* OTP */,
    name: "OTP",
    maxLength: 10
  },
  ["13" /* URL */]: {
    value: "13" /* URL */,
    name: "URL",
    maxLength: 200
  },
  ["14" /* CURRENCY */]: {
    value: "14" /* CURRENCY */,
    name: "Ti\u1EC1n t\u1EC7 (VN\u0110)",
    maxLength: 12
  },
  ["15" /* BANK_TRANSFER_NOTE */]: {
    value: "15" /* BANK_TRANSFER_NOTE */,
    name: "Bank transfer note",
    maxLength: 90
  }
};
var ZNS_TEMPLATE_TAG_COMPATIBILITY = {
  [1 /* CUSTOM */]: [
    "1" /* TRANSACTION */,
    "2" /* CUSTOMER_CARE */,
    "3" /* PROMOTION */
  ],
  [2 /* AUTHENTICATION */]: [
    "1" /* TRANSACTION */
  ],
  [3 /* PAYMENT_REQUEST */]: [
    "1" /* TRANSACTION */
  ],
  [4 /* VOUCHER */]: [
    "1" /* TRANSACTION */,
    "2" /* CUSTOMER_CARE */,
    "3" /* PROMOTION */
  ],
  [5 /* SERVICE_RATING */]: [
    "2" /* CUSTOMER_CARE */
  ]
};
var ZNSValidation = {
  /**
   * Kiểm tra template name hợp lệ
   */
  isValidTemplateName: (name) => {
    return name.length >= 10 && name.length <= 60;
  },
  /**
   * Kiểm tra note hợp lệ
   */
  isValidNote: (note) => {
    return note.length >= 1 && note.length <= 400;
  },
  /**
   * Kiểm tra tag có tương thích với template type không
   */
  isTagCompatibleWithType: (templateType, tag) => {
    const compatibleTags = ZNS_TEMPLATE_TAG_COMPATIBILITY[templateType];
    return compatibleTags ? compatibleTags.includes(tag) : false;
  },
  /**
   * Kiểm tra param value có hợp lệ với type không
   */
  isValidParamValue: (paramType, value) => {
    const typeInfo = ZNS_PARAM_TYPES[paramType];
    return typeInfo ? value.length <= typeInfo.maxLength : false;
  }
};

// src/types/webhook.ts
var WebhookEventName = /* @__PURE__ */ ((WebhookEventName2) => {
  WebhookEventName2["USER_SEND_LOCATION"] = "user_send_location";
  WebhookEventName2["USER_SEND_IMAGE"] = "user_send_image";
  WebhookEventName2["USER_SEND_LINK"] = "user_send_link";
  WebhookEventName2["USER_SEND_TEXT"] = "user_send_text";
  WebhookEventName2["USER_SEND_STICKER"] = "user_send_sticker";
  WebhookEventName2["USER_SEND_GIF"] = "user_send_gif";
  WebhookEventName2["USER_SEND_AUDIO"] = "user_send_audio";
  WebhookEventName2["USER_SEND_VIDEO"] = "user_send_video";
  WebhookEventName2["USER_SEND_FILE"] = "user_send_file";
  WebhookEventName2["USER_RECEIVED_MESSAGE"] = "user_received_message";
  WebhookEventName2["USER_SEEN_MESSAGE"] = "user_seen_message";
  WebhookEventName2["FOLLOW"] = "follow";
  WebhookEventName2["UNFOLLOW"] = "unfollow";
  WebhookEventName2["USER_SUBMIT_INFO"] = "user_submit_info";
  WebhookEventName2["OA_SEND_TEXT"] = "oa_send_text";
  WebhookEventName2["OA_SEND_IMAGE"] = "oa_send_image";
  WebhookEventName2["OA_SEND_GIF"] = "oa_send_gif";
  WebhookEventName2["OA_SEND_LIST"] = "oa_send_list";
  WebhookEventName2["OA_SEND_FILE"] = "oa_send_file";
  WebhookEventName2["OA_SEND_STICKER"] = "oa_send_sticker";
  WebhookEventName2["USER_CLICK_CHATNOW"] = "user_click_chatnow";
  WebhookEventName2["USER_REACTED_MESSAGE"] = "user_reacted_message";
  WebhookEventName2["USER_REPLY_CONSENT"] = "user_reply_consent";
  WebhookEventName2["OA_REACTED_MESSAGE"] = "oa_reacted_message";
  WebhookEventName2["OA_SEND_CONSENT"] = "oa_send_consent";
  WebhookEventName2["ANONYMOUS_SEND_TEXT"] = "anonymous_send_text";
  WebhookEventName2["ANONYMOUS_SEND_IMAGE"] = "anonymous_send_image";
  WebhookEventName2["ANONYMOUS_SEND_FILE"] = "anonymous_send_file";
  WebhookEventName2["ANONYMOUS_SEND_STICKER"] = "anonymous_send_sticker";
  WebhookEventName2["OA_SEND_ANONYMOUS_TEXT"] = "oa_send_anonymous_text";
  WebhookEventName2["OA_SEND_ANONYMOUS_IMAGE"] = "oa_send_anonymous_image";
  WebhookEventName2["OA_SEND_ANONYMOUS_FILE"] = "oa_send_anonymous_file";
  WebhookEventName2["OA_SEND_ANONYMOUS_STICKER"] = "oa_send_anonymous_sticker";
  WebhookEventName2["OA_CALL_USER"] = "oa_call_user";
  WebhookEventName2["USER_CALL_OA"] = "user_call_oa";
  WebhookEventName2["OA_SEND_TEMPLATE"] = "oa_send_template";
  WebhookEventName2["USER_SEND_BUSINESS_CARD"] = "user_send_business_card";
  WebhookEventName2["USER_FEEDBACK"] = "user_feedback";
  WebhookEventName2["CHANGE_OA_DAILY_QUOTA"] = "change_oa_daily_quota";
  WebhookEventName2["CHANGE_OA_TEMPLATE_TAGS"] = "change_oa_template_tags";
  WebhookEventName2["CHANGE_TEMPLATE_QUALITY"] = "change_template_quality";
  WebhookEventName2["CHANGE_TEMPLATE_QUOTA"] = "change_template_quota";
  WebhookEventName2["CHANGE_TEMPLATE_STATUS"] = "change_template_status";
  WebhookEventName2["EVENT_JOURNEY_TIME_OUT"] = "event_journey_time_out";
  WebhookEventName2["EVENT_JOURNEY_ACKNOWLEDGED"] = "event_journey_acknowledged";
  WebhookEventName2["ZNS_USER_RECEIVED_MESSAGE"] = "user_received_message";
  WebhookEventName2["CREATE_GROUP"] = "create_group";
  WebhookEventName2["USER_JOIN_GROUP"] = "user_join_group";
  WebhookEventName2["USER_REQUEST_JOIN_GROUP"] = "user_request_join_group";
  WebhookEventName2["REACT_REQUEST_JOIN_GROUP"] = "react_request_join_group";
  WebhookEventName2["REJECT_REQUEST_JOIN_GROUP"] = "reject_request_join_group";
  WebhookEventName2["ADD_GROUP_ADMIN"] = "add_group_admin";
  WebhookEventName2["REMOVE_GROUP_ADMIN"] = "remove_group_admin";
  WebhookEventName2["UPDATE_GROUP_INFO"] = "update_group_info";
  WebhookEventName2["USER_OUT_GROUP"] = "user_out_group";
  WebhookEventName2["OA_SEND_GROUP_TEXT"] = "oa_send_group_text";
  WebhookEventName2["OA_SEND_GROUP_IMAGE"] = "oa_send_group_image";
  WebhookEventName2["OA_SEND_GROUP_LINK"] = "oa_send_group_link";
  WebhookEventName2["OA_SEND_GROUP_AUDIO"] = "oa_send_group_audio";
  WebhookEventName2["OA_SEND_GROUP_LOCATION"] = "oa_send_group_location";
  WebhookEventName2["OA_SEND_GROUP_VIDEO"] = "oa_send_group_video";
  WebhookEventName2["OA_SEND_GROUP_BUSINESS_CARD"] = "oa_send_group_business_card";
  WebhookEventName2["OA_SEND_GROUP_STICKER"] = "oa_send_group_sticker";
  WebhookEventName2["OA_SEND_GROUP_GIF"] = "oa_send_group_gif";
  WebhookEventName2["OA_SEND_GROUP_FILE"] = "oa_send_group_file";
  WebhookEventName2["USER_SEND_GROUP_TEXT"] = "user_send_group_text";
  WebhookEventName2["USER_SEND_GROUP_IMAGE"] = "user_send_group_image";
  WebhookEventName2["USER_SEND_GROUP_LINK"] = "user_send_group_link";
  WebhookEventName2["USER_SEND_GROUP_AUDIO"] = "user_send_group_audio";
  WebhookEventName2["USER_SEND_GROUP_LOCATION"] = "user_send_group_location";
  WebhookEventName2["USER_SEND_GROUP_VIDEO"] = "user_send_group_video";
  WebhookEventName2["USER_SEND_GROUP_BUSINESS_CARD"] = "user_send_group_business_card";
  WebhookEventName2["USER_SEND_GROUP_STICKER"] = "user_send_group_sticker";
  WebhookEventName2["USER_SEND_GROUP_GIF"] = "user_send_group_gif";
  WebhookEventName2["USER_SEND_GROUP_FILE"] = "user_send_group_file";
  WebhookEventName2["WIDGET_INTERACTION_ACCEPTED"] = "widget_interaction_accepted";
  WebhookEventName2["WIDGET_FAILED_TO_SYNC_USER_EXTERNAL_ID"] = "widget_failed_to_sync_user_external_id";
  WebhookEventName2["PERMISSION_REVOKED"] = "permission_revoked";
  WebhookEventName2["EXTENSION_PURCHASED"] = "extension_purchased";
  WebhookEventName2["UPDATE_USER_INFO"] = "update_user_info";
  WebhookEventName2["SHOP_HAS_ORDER"] = "shop_has_order";
  WebhookEventName2["ADD_USER_TO_TAG"] = "add_user_to_tag";
  return WebhookEventName2;
})(WebhookEventName || {});
var AttachmentType = /* @__PURE__ */ ((AttachmentType2) => {
  AttachmentType2["LOCATION"] = "location";
  AttachmentType2["IMAGE"] = "image";
  AttachmentType2["LINK"] = "link";
  AttachmentType2["STICKER"] = "sticker";
  AttachmentType2["GIF"] = "gif";
  AttachmentType2["AUDIO"] = "audio";
  AttachmentType2["VIDEO"] = "video";
  AttachmentType2["FILE"] = "file";
  return AttachmentType2;
})(AttachmentType || {});
function isMessageEvent(event) {
  return [
    "user_send_location" /* USER_SEND_LOCATION */,
    "user_send_image" /* USER_SEND_IMAGE */,
    "user_send_link" /* USER_SEND_LINK */,
    "user_send_text" /* USER_SEND_TEXT */,
    "user_send_sticker" /* USER_SEND_STICKER */,
    "user_send_gif" /* USER_SEND_GIF */,
    "user_send_audio" /* USER_SEND_AUDIO */,
    "user_send_video" /* USER_SEND_VIDEO */,
    "user_send_file" /* USER_SEND_FILE */,
    "user_received_message" /* USER_RECEIVED_MESSAGE */,
    "user_seen_message" /* USER_SEEN_MESSAGE */,
    "oa_send_text" /* OA_SEND_TEXT */,
    "oa_send_image" /* OA_SEND_IMAGE */,
    "oa_send_gif" /* OA_SEND_GIF */,
    "oa_send_list" /* OA_SEND_LIST */,
    "oa_send_file" /* OA_SEND_FILE */,
    "oa_send_sticker" /* OA_SEND_STICKER */,
    "oa_send_anonymous_text" /* OA_SEND_ANONYMOUS_TEXT */,
    "oa_send_anonymous_image" /* OA_SEND_ANONYMOUS_IMAGE */,
    "oa_send_anonymous_file" /* OA_SEND_ANONYMOUS_FILE */,
    "oa_send_anonymous_sticker" /* OA_SEND_ANONYMOUS_STICKER */,
    "oa_send_template" /* OA_SEND_TEMPLATE */,
    "anonymous_send_text" /* ANONYMOUS_SEND_TEXT */,
    "anonymous_send_image" /* ANONYMOUS_SEND_IMAGE */,
    "anonymous_send_file" /* ANONYMOUS_SEND_FILE */,
    "anonymous_send_sticker" /* ANONYMOUS_SEND_STICKER */,
    "user_send_business_card" /* USER_SEND_BUSINESS_CARD */,
    "oa_send_group_text" /* OA_SEND_GROUP_TEXT */,
    "oa_send_group_image" /* OA_SEND_GROUP_IMAGE */,
    "oa_send_group_link" /* OA_SEND_GROUP_LINK */,
    "oa_send_group_audio" /* OA_SEND_GROUP_AUDIO */,
    "oa_send_group_location" /* OA_SEND_GROUP_LOCATION */,
    "oa_send_group_video" /* OA_SEND_GROUP_VIDEO */,
    "oa_send_group_business_card" /* OA_SEND_GROUP_BUSINESS_CARD */,
    "oa_send_group_sticker" /* OA_SEND_GROUP_STICKER */,
    "oa_send_group_gif" /* OA_SEND_GROUP_GIF */,
    "oa_send_group_file" /* OA_SEND_GROUP_FILE */,
    "user_send_group_text" /* USER_SEND_GROUP_TEXT */,
    "user_send_group_link" /* USER_SEND_GROUP_LINK */,
    "user_send_group_audio" /* USER_SEND_GROUP_AUDIO */,
    "user_send_group_location" /* USER_SEND_GROUP_LOCATION */,
    "user_send_group_video" /* USER_SEND_GROUP_VIDEO */,
    "user_send_group_business_card" /* USER_SEND_GROUP_BUSINESS_CARD */,
    "user_send_group_sticker" /* USER_SEND_GROUP_STICKER */,
    "user_send_group_gif" /* USER_SEND_GROUP_GIF */,
    "user_send_group_file" /* USER_SEND_GROUP_FILE */
  ].includes(event.event_name);
}
function isFollowEvent(event) {
  return ["follow" /* FOLLOW */, "unfollow" /* UNFOLLOW */].includes(
    event.event_name
  );
}
function isUserActionEvent(event) {
  return [
    "follow" /* FOLLOW */,
    "unfollow" /* UNFOLLOW */,
    "user_submit_info" /* USER_SUBMIT_INFO */,
    "user_click_chatnow" /* USER_CLICK_CHATNOW */,
    "user_reacted_message" /* USER_REACTED_MESSAGE */,
    "user_reply_consent" /* USER_REPLY_CONSENT */,
    "user_send_business_card" /* USER_SEND_BUSINESS_CARD */,
    "user_feedback" /* USER_FEEDBACK */
  ].includes(event.event_name);
}
function isShopEvent(event) {
  return event.event_name === "shop_has_order" /* SHOP_HAS_ORDER */;
}
function isOAMessageEvent(event) {
  return [
    "oa_send_text" /* OA_SEND_TEXT */,
    "oa_send_image" /* OA_SEND_IMAGE */,
    "oa_send_gif" /* OA_SEND_GIF */,
    "oa_send_list" /* OA_SEND_LIST */,
    "oa_send_file" /* OA_SEND_FILE */,
    "oa_send_sticker" /* OA_SEND_STICKER */,
    "oa_send_anonymous_text" /* OA_SEND_ANONYMOUS_TEXT */,
    "oa_send_anonymous_image" /* OA_SEND_ANONYMOUS_IMAGE */,
    "oa_send_anonymous_file" /* OA_SEND_ANONYMOUS_FILE */,
    "oa_send_anonymous_sticker" /* OA_SEND_ANONYMOUS_STICKER */,
    "oa_send_template" /* OA_SEND_TEMPLATE */
  ].includes(event.event_name);
}
function isAnonymousEvent(event) {
  return [
    "anonymous_send_text" /* ANONYMOUS_SEND_TEXT */,
    "anonymous_send_image" /* ANONYMOUS_SEND_IMAGE */,
    "anonymous_send_file" /* ANONYMOUS_SEND_FILE */,
    "anonymous_send_sticker" /* ANONYMOUS_SEND_STICKER */
  ].includes(event.event_name);
}
function isCallEvent(event) {
  return [
    "oa_call_user" /* OA_CALL_USER */,
    "user_call_oa" /* USER_CALL_OA */
  ].includes(event.event_name);
}
function isTemplateEvent(event) {
  return event.event_name === "oa_send_template" /* OA_SEND_TEMPLATE */;
}
function isBusinessCardEvent(event) {
  return event.event_name === "user_send_business_card" /* USER_SEND_BUSINESS_CARD */;
}
function isFeedbackEvent(event) {
  return event.event_name === "user_feedback" /* USER_FEEDBACK */;
}
function isZNSEvent(event) {
  return [
    "change_oa_daily_quota" /* CHANGE_OA_DAILY_QUOTA */,
    "change_oa_template_tags" /* CHANGE_OA_TEMPLATE_TAGS */,
    "change_template_quality" /* CHANGE_TEMPLATE_QUALITY */,
    "change_template_quota" /* CHANGE_TEMPLATE_QUOTA */,
    "change_template_status" /* CHANGE_TEMPLATE_STATUS */,
    "event_journey_time_out" /* EVENT_JOURNEY_TIME_OUT */,
    "event_journey_acknowledged" /* EVENT_JOURNEY_ACKNOWLEDGED */,
    "user_received_message" /* ZNS_USER_RECEIVED_MESSAGE */
  ].includes(event.event_name);
}
function isGroupEvent(event) {
  return [
    "create_group" /* CREATE_GROUP */,
    "user_join_group" /* USER_JOIN_GROUP */,
    "user_request_join_group" /* USER_REQUEST_JOIN_GROUP */,
    "react_request_join_group" /* REACT_REQUEST_JOIN_GROUP */,
    "reject_request_join_group" /* REJECT_REQUEST_JOIN_GROUP */,
    "add_group_admin" /* ADD_GROUP_ADMIN */,
    "remove_group_admin" /* REMOVE_GROUP_ADMIN */,
    "update_group_info" /* UPDATE_GROUP_INFO */,
    "user_out_group" /* USER_OUT_GROUP */,
    "oa_send_group_text" /* OA_SEND_GROUP_TEXT */,
    "oa_send_group_image" /* OA_SEND_GROUP_IMAGE */,
    "oa_send_group_link" /* OA_SEND_GROUP_LINK */,
    "oa_send_group_audio" /* OA_SEND_GROUP_AUDIO */,
    "oa_send_group_location" /* OA_SEND_GROUP_LOCATION */,
    "oa_send_group_video" /* OA_SEND_GROUP_VIDEO */,
    "oa_send_group_business_card" /* OA_SEND_GROUP_BUSINESS_CARD */,
    "oa_send_group_sticker" /* OA_SEND_GROUP_STICKER */,
    "oa_send_group_gif" /* OA_SEND_GROUP_GIF */,
    "oa_send_group_file" /* OA_SEND_GROUP_FILE */,
    "user_send_group_text" /* USER_SEND_GROUP_TEXT */,
    "user_send_group_link" /* USER_SEND_GROUP_LINK */,
    "user_send_group_audio" /* USER_SEND_GROUP_AUDIO */,
    "user_send_group_location" /* USER_SEND_GROUP_LOCATION */,
    "user_send_group_video" /* USER_SEND_GROUP_VIDEO */,
    "user_send_group_business_card" /* USER_SEND_GROUP_BUSINESS_CARD */,
    "user_send_group_sticker" /* USER_SEND_GROUP_STICKER */,
    "user_send_group_gif" /* USER_SEND_GROUP_GIF */,
    "user_send_group_file" /* USER_SEND_GROUP_FILE */
  ].includes(event.event_name);
}
function isJourneyEvent(event) {
  return [
    "event_journey_time_out" /* EVENT_JOURNEY_TIME_OUT */,
    "event_journey_acknowledged" /* EVENT_JOURNEY_ACKNOWLEDGED */
  ].includes(event.event_name);
}
function isQuotaTemplateEvent(event) {
  return [
    "change_oa_daily_quota" /* CHANGE_OA_DAILY_QUOTA */,
    "change_oa_template_tags" /* CHANGE_OA_TEMPLATE_TAGS */,
    "change_template_quality" /* CHANGE_TEMPLATE_QUALITY */,
    "change_template_quota" /* CHANGE_TEMPLATE_QUOTA */,
    "change_template_status" /* CHANGE_TEMPLATE_STATUS */
  ].includes(event.event_name);
}
function isWidgetEvent(event) {
  return [
    "widget_interaction_accepted" /* WIDGET_INTERACTION_ACCEPTED */,
    "widget_failed_to_sync_user_external_id" /* WIDGET_FAILED_TO_SYNC_USER_EXTERNAL_ID */
  ].includes(event.event_name);
}
function isSystemEvent(event) {
  return [
    "add_user_to_tag" /* ADD_USER_TO_TAG */,
    "permission_revoked" /* PERMISSION_REVOKED */,
    "extension_purchased" /* EXTENSION_PURCHASED */,
    "update_user_info" /* UPDATE_USER_INFO */
  ].includes(event.event_name);
}
function isPermissionEvent(event) {
  return event.event_name === "permission_revoked" /* PERMISSION_REVOKED */;
}
function isExtensionEvent(event) {
  return event.event_name === "extension_purchased" /* EXTENSION_PURCHASED */;
}
function isUpdateUserInfoEvent(event) {
  return event.event_name === "update_user_info" /* UPDATE_USER_INFO */;
}
function isReactionEvent(event) {
  return [
    "user_reacted_message" /* USER_REACTED_MESSAGE */,
    "oa_reacted_message" /* OA_REACTED_MESSAGE */
  ].includes(event.event_name);
}
function isConsentEvent(event) {
  return [
    "oa_send_consent" /* OA_SEND_CONSENT */,
    "user_reply_consent" /* USER_REPLY_CONSENT */
  ].includes(event.event_name);
}
function hasAttachments(message) {
  return "attachments" in message && Array.isArray(message.attachments);
}
function isUserMessageEvent(event) {
  return [
    "user_send_location" /* USER_SEND_LOCATION */,
    "user_send_image" /* USER_SEND_IMAGE */,
    "user_send_link" /* USER_SEND_LINK */,
    "user_send_text" /* USER_SEND_TEXT */,
    "user_send_sticker" /* USER_SEND_STICKER */,
    "user_send_gif" /* USER_SEND_GIF */,
    "user_send_audio" /* USER_SEND_AUDIO */,
    "user_send_video" /* USER_SEND_VIDEO */,
    "user_send_file" /* USER_SEND_FILE */,
    "user_received_message" /* USER_RECEIVED_MESSAGE */,
    "user_seen_message" /* USER_SEEN_MESSAGE */,
    "user_send_business_card" /* USER_SEND_BUSINESS_CARD */
  ].includes(event.event_name);
}
function isGroupMessageEvent(event) {
  return [
    "user_send_group_text" /* USER_SEND_GROUP_TEXT */,
    "user_send_group_link" /* USER_SEND_GROUP_LINK */,
    "user_send_group_audio" /* USER_SEND_GROUP_AUDIO */,
    "user_send_group_location" /* USER_SEND_GROUP_LOCATION */,
    "user_send_group_video" /* USER_SEND_GROUP_VIDEO */,
    "user_send_group_business_card" /* USER_SEND_GROUP_BUSINESS_CARD */,
    "user_send_group_sticker" /* USER_SEND_GROUP_STICKER */,
    "user_send_group_gif" /* USER_SEND_GROUP_GIF */,
    "user_send_group_file" /* USER_SEND_GROUP_FILE */
  ].includes(event.event_name);
}
function isOAToUserMessageEvent(event) {
  return [
    "oa_send_text" /* OA_SEND_TEXT */,
    "oa_send_image" /* OA_SEND_IMAGE */,
    "oa_send_gif" /* OA_SEND_GIF */,
    "oa_send_list" /* OA_SEND_LIST */,
    "oa_send_file" /* OA_SEND_FILE */,
    "oa_send_sticker" /* OA_SEND_STICKER */,
    "oa_send_template" /* OA_SEND_TEMPLATE */
  ].includes(event.event_name);
}
function isOAToGroupMessageEvent(event) {
  return [
    "oa_send_group_text" /* OA_SEND_GROUP_TEXT */,
    "oa_send_group_image" /* OA_SEND_GROUP_IMAGE */,
    "oa_send_group_link" /* OA_SEND_GROUP_LINK */,
    "oa_send_group_audio" /* OA_SEND_GROUP_AUDIO */,
    "oa_send_group_location" /* OA_SEND_GROUP_LOCATION */,
    "oa_send_group_video" /* OA_SEND_GROUP_VIDEO */,
    "oa_send_group_business_card" /* OA_SEND_GROUP_BUSINESS_CARD */,
    "oa_send_group_sticker" /* OA_SEND_GROUP_STICKER */,
    "oa_send_group_gif" /* OA_SEND_GROUP_GIF */,
    "oa_send_group_file" /* OA_SEND_GROUP_FILE */
  ].includes(event.event_name);
}
function getMessageDirection(event) {
  if (isUserMessageEvent(event)) {
    return {
      direction: "incoming",
      target: "user",
      description: "Tin nh\u1EAFn t\u1EEB ng\u01B0\u1EDDi d\xF9ng c\xE1 nh\xE2n g\u1EEDi t\u1EDBi OA"
    };
  }
  if (isGroupMessageEvent(event)) {
    return {
      direction: "incoming",
      target: "group",
      description: "Tin nh\u1EAFn t\u1EEB ng\u01B0\u1EDDi d\xF9ng trong group"
    };
  }
  if (isOAToUserMessageEvent(event)) {
    return {
      direction: "outgoing",
      target: "user",
      description: "Tin nh\u1EAFn t\u1EEB OA g\u1EEDi t\u1EDBi ng\u01B0\u1EDDi d\xF9ng c\xE1 nh\xE2n"
    };
  }
  if (isOAToGroupMessageEvent(event)) {
    return {
      direction: "outgoing",
      target: "group",
      description: "Tin nh\u1EAFn t\u1EEB OA g\u1EEDi t\u1EDBi group"
    };
  }
  return {
    direction: "unknown",
    target: "unknown",
    description: "Lo\u1EA1i s\u1EF1 ki\u1EC7n kh\xF4ng x\xE1c \u0111\u1ECBnh"
  };
}
function readWebhookString(value) {
  if (typeof value !== "string") {
    return null;
  }
  const normalizedValue = value.trim();
  return normalizedValue.length > 0 ? normalizedValue : null;
}
function readWebhookObjectId(value) {
  if (!value || typeof value !== "object") {
    return null;
  }
  return readWebhookString(value.id);
}
function hasWebhookEventPrefix(event, prefix) {
  return event.event_name.startsWith(prefix);
}
function getWebhookEventAppId(event) {
  const directAppId = readWebhookString(event.app_id);
  if (directAppId) {
    return directAppId;
  }
  if ("message" in event && event.message && typeof event.message === "object") {
    const messageSource = event.message.source;
    if (messageSource && typeof messageSource === "object") {
      return readWebhookString(
        messageSource.app_id
      );
    }
  }
  return null;
}
function getWebhookEventOaId(event) {
  const directOaId = readWebhookString(event.oa_id);
  if (directOaId) {
    return directOaId;
  }
  if (hasWebhookEventPrefix(event, "oa_")) {
    return readWebhookObjectId(event.sender);
  }
  if (hasWebhookEventPrefix(event, "user_") || hasWebhookEventPrefix(event, "anonymous_")) {
    return readWebhookObjectId(event.recipient);
  }
  return null;
}
function getWebhookEventSenderId(event) {
  const directSenderId = readWebhookObjectId(
    event.sender
  );
  if (directSenderId) {
    return directSenderId;
  }
  const fallbackSenderId = readWebhookString(event.user_id) ?? readWebhookObjectId(event.follower) ?? readWebhookObjectId(event.customer) ?? readWebhookObjectId(event.user);
  if (fallbackSenderId) {
    return fallbackSenderId;
  }
  if (hasWebhookEventPrefix(event, "oa_")) {
    return getWebhookEventOaId(event);
  }
  return null;
}
function getWebhookEventRecipientId(event) {
  const directRecipientId = readWebhookObjectId(
    event.recipient
  );
  if (directRecipientId) {
    return directRecipientId;
  }
  if (hasWebhookEventPrefix(event, "oa_")) {
    return readWebhookString(event.user_id);
  }
  return getWebhookEventOaId(event);
}
function getWebhookEventIdentifiers(event) {
  return {
    oaId: getWebhookEventOaId(event),
    appId: getWebhookEventAppId(event),
    senderId: getWebhookEventSenderId(event),
    recipientId: getWebhookEventRecipientId(event)
  };
}

// src/utils/type-guards.ts
function isUserSendGroupTextEvent(event) {
  return event.event_name === "user_send_group_text" /* USER_SEND_GROUP_TEXT */;
}
function isUserSendGroupImageEvent(event) {
  return event.event_name === "user_send_group_image" /* USER_SEND_GROUP_IMAGE */;
}
function isUserSendGroupVideoEvent(event) {
  return event.event_name === "user_send_group_video" /* USER_SEND_GROUP_VIDEO */;
}
function isUserSendGroupAudioEvent(event) {
  return event.event_name === "user_send_group_audio" /* USER_SEND_GROUP_AUDIO */;
}
function isUserSendGroupFileEvent(event) {
  return event.event_name === "user_send_group_file" /* USER_SEND_GROUP_FILE */;
}
function isUserGroupMessageEvent(event) {
  return [
    "user_send_group_text" /* USER_SEND_GROUP_TEXT */,
    "user_send_group_image" /* USER_SEND_GROUP_IMAGE */,
    "user_send_group_video" /* USER_SEND_GROUP_VIDEO */,
    "user_send_group_audio" /* USER_SEND_GROUP_AUDIO */,
    "user_send_group_file" /* USER_SEND_GROUP_FILE */
  ].includes(event.event_name);
}
function isOASendTextEvent(event) {
  return event.event_name === "oa_send_text" /* OA_SEND_TEXT */;
}
function isOASendImageEvent(event) {
  return event.event_name === "oa_send_image" /* OA_SEND_IMAGE */;
}
function isOASendFileEvent(event) {
  return event.event_name === "oa_send_file" /* OA_SEND_FILE */;
}
function isOASendStickerEvent(event) {
  return event.event_name === "oa_send_sticker" /* OA_SEND_STICKER */;
}
function isOASendGifEvent(event) {
  return event.event_name === "oa_send_gif" /* OA_SEND_GIF */;
}
function isOASendGroupTextEvent(event) {
  return event.event_name === "oa_send_group_text" /* OA_SEND_GROUP_TEXT */;
}
function isOASendGroupImageEvent(event) {
  return event.event_name === "oa_send_group_image" /* OA_SEND_GROUP_IMAGE */;
}
function isOASendGroupFileEvent(event) {
  return event.event_name === "oa_send_group_file" /* OA_SEND_GROUP_FILE */;
}
function isOASendGroupStickerEvent(event) {
  return event.event_name === "oa_send_group_sticker" /* OA_SEND_GROUP_STICKER */;
}
function isOASendGroupGifEvent(event) {
  return event.event_name === "oa_send_group_gif" /* OA_SEND_GROUP_GIF */;
}
function isFromGroup(event) {
  return "group_id" in event || event.event_name.includes("group");
}
function isFromPersonal(event) {
  return !isFromGroup(event);
}

// src/clients/base-client.ts
var import_axios = __toESM(require("axios"));
var import_form_data = __toESM(require("form-data"));
var BaseClient = class {
  constructor(config) {
    this.config = {
      appId: config.appId,
      appSecret: config.appSecret,
      timeout: config.timeout || 3e4,
      debug: config.debug || false,
      apiBaseUrl: config.apiBaseUrl || "https://openapi.zalo.me",
      retry: {
        attempts: config.retry?.attempts || 3,
        delay: config.retry?.delay || 1e3,
        ...config.retry
      }
    };
    this.logger = new ConsoleLogger(this.config.debug);
    this.axios = import_axios.default.create({
      baseURL: this.config.apiBaseUrl,
      timeout: this.config.timeout,
      headers: {
        "Content-Type": "application/json",
        "User-Agent": "RedAI-Zalo-SDK/1.0.0"
      }
    });
    this.setupInterceptors();
  }
  /**
   * Setup axios interceptors for logging and error handling
   */
  setupInterceptors() {
    this.axios.interceptors.request.use(
      (config) => {
        this.logger.debug(
          `Making ${config.method?.toUpperCase()} request to ${config.url}`,
          {
            headers: this.sanitizeHeaders(config.headers || {}),
            params: config.params
          }
        );
        return config;
      },
      (error) => {
        this.logger.error("Request setup error:", error.message);
        return Promise.reject(error);
      }
    );
    this.axios.interceptors.response.use(
      (response) => {
        this.logger.debug(`Received response from ${response.config.url}`, {
          status: response.status,
          data: response.data
        });
        return response;
      },
      (error) => {
        this.handleAxiosError(error);
        return Promise.reject(error);
      }
    );
  }
  /**
   * Sanitize headers for logging (remove sensitive information)
   */
  sanitizeHeaders(headers) {
    const sanitized = { ...headers };
    if (sanitized.access_token) {
      sanitized.access_token = "***REDACTED***";
    }
    if (sanitized.secret_key) {
      sanitized.secret_key = "***REDACTED***";
    }
    return sanitized;
  }
  /**
   * Handle axios errors and log them
   */
  handleAxiosError(error) {
    if (error.response) {
      this.logger.error(`HTTP ${error.response.status} error:`, {
        url: error.config?.url,
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data
      });
    } else if (error.request) {
      this.logger.error("No response received:", {
        url: error.config?.url,
        message: error.message
      });
    } else {
      this.logger.error("Request setup error:", error.message);
    }
  }
  /**
   * Make a GET request
   */
  async get(url, accessToken, params) {
    return this.request({
      method: "GET",
      url,
      headers: accessToken ? { access_token: accessToken } : {},
      params
    });
  }
  /**
   * Make a POST request
   */
  async post(url, accessToken, data, params) {
    return this.request({
      method: "POST",
      url,
      headers: accessToken ? { access_token: accessToken } : {},
      data,
      params
    });
  }
  /**
   * Make a PUT request
   */
  async put(url, accessToken, data, params) {
    return this.request({
      method: "PUT",
      url,
      headers: accessToken ? { access_token: accessToken } : {},
      data,
      params
    });
  }
  /**
   * Make a DELETE request
   */
  async delete(url, accessToken, params) {
    return this.request({
      method: "DELETE",
      url,
      headers: accessToken ? { access_token: accessToken } : {},
      params
    });
  }
  /**
   * Upload file using FormData
   */
  async uploadFile(url, accessToken, file, filename, additionalFields) {
    const formData = new import_form_data.default();
    formData.append("file", file, filename);
    if (additionalFields) {
      Object.entries(additionalFields).forEach(([key, value]) => {
        formData.append(key, value);
      });
    }
    return this.request({
      method: "POST",
      url,
      headers: {
        access_token: accessToken,
        ...formData.getHeaders()
        // Let form-data package set Content-Type with proper boundary
      },
      data: formData
    });
  }
  /**
   * Make a generic HTTP request with retry logic
   */
  async request(config) {
    let lastError;
    for (let attempt = 1; attempt <= (this.config.retry?.attempts || 3); attempt++) {
      try {
        const axiosConfig = {
          method: config.method,
          url: config.url,
          headers: config.headers,
          params: config.params,
          data: config.data,
          timeout: config.timeout || this.config.timeout
        };
        const response = await this.axios.request(
          axiosConfig
        );
        this.validateZaloResponse(response.data);
        return response.data;
      } catch (error) {
        lastError = error;
        if (attempt < (this.config.retry?.attempts || 3) && this.shouldRetry(error)) {
          this.logger.warn(
            `Request failed, retrying (${attempt}/${this.config.retry?.attempts || 3})...`
          );
          await this.delay((this.config.retry?.delay || 1e3) * attempt);
          continue;
        }
        break;
      }
    }
    throw this.createSDKError(lastError);
  }
  /**
   * Validate Zalo API response for errors
   */
  validateZaloResponse(data) {
    if (data && typeof data === "object") {
      if ("error" in data && data.error !== 0) {
        const errorMessage = data.error_description || data.message || "Unknown Zalo API error";
        throw new ZaloSDKError(errorMessage, data.error, data);
      }
      if ("result" in data && data.result && typeof data.result === "object" && "error" in data.result && data.result.error !== 0) {
        const errorMessage = data.result.error_description || data.result.message || "Unknown Zalo API error";
        throw new ZaloSDKError(errorMessage, data.result.error, data.result);
      }
    }
  }
  /**
   * Determine if request should be retried
   */
  shouldRetry(error) {
    return !error.response || error.response.status >= 500 && error.response.status < 600;
  }
  /**
   * Create SDK error from axios error
   */
  createSDKError(error) {
    if (error instanceof ZaloSDKError) {
      return error;
    }
    if (error instanceof import_axios.AxiosError) {
      const response = error.response;
      if (response?.data) {
        const errorCode = response.data.error || response.status;
        const errorMessage = response.data.error_description || response.data.message || error.message;
        return new ZaloSDKError(errorMessage, errorCode, response.data);
      }
    }
    return new ZaloSDKError(error.message, -1, error);
  }
  /**
   * Delay execution for specified milliseconds
   */
  delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
};

// src/clients/zalo-client.ts
var ZaloClient = class extends BaseClient {
  constructor(config) {
    super(config);
  }
  /**
   * Make authenticated GET request to Zalo API
   */
  async apiGet(endpoint, accessToken, params) {
    return this.get(endpoint, accessToken, params);
  }
  /**
   * Make GET request with custom headers
   */
  async apiGetWithHeaders(endpoint, headers) {
    return this.request({
      method: "GET",
      url: endpoint,
      headers
    });
  }
  /**
   * Make authenticated POST request to Zalo API
   */
  async apiPost(endpoint, accessToken, data, params) {
    return this.post(endpoint, accessToken, data, params);
  }
  /**
   * Make authenticated POST request to Zalo API with custom headers
   */
  async apiPostWithHeaders(endpoint, accessToken, data, customHeaders) {
    return this.request({
      method: "POST",
      url: endpoint,
      headers: {
        access_token: accessToken,
        ...customHeaders
      },
      data
    });
  }
  /**
   * Make authenticated PUT request to Zalo API
   */
  async apiPut(endpoint, accessToken, data, params) {
    return this.put(endpoint, accessToken, data, params);
  }
  /**
   * Make authenticated DELETE request to Zalo API
   */
  async apiDelete(endpoint, accessToken, params) {
    return this.delete(endpoint, accessToken, params);
  }
  /**
   * Upload file to Zalo API
   */
  async apiUploadFile(endpoint, accessToken, file, filename, additionalFields) {
    return this.uploadFile(
      endpoint,
      accessToken,
      file,
      filename,
      additionalFields
    );
  }
  /**
   * Make POST request with FormData (for file uploads)
   */
  async apiPostFormData(endpoint, accessToken, formData) {
    return this.request({
      method: "POST",
      url: endpoint,
      headers: {
        access_token: accessToken
        // Don't set Content-Type header, let the browser set it with boundary
      },
      data: formData
    });
  }
  /**
   * Make request to OAuth endpoints (without access token)
   */
  async oauthRequest(method, endpoint, data, headers) {
    const config = {
      method,
      url: endpoint,
      headers,
      data
    };
    if (method === "GET") {
      config.data = void 0;
      return this.get(endpoint, void 0, data);
    } else {
      return this.post(endpoint, void 0, data);
    }
  }
  /**
   * Make request to ZNS API (different base URL)
   */
  async znsRequest(method, endpoint, accessToken, data, params) {
    const znsBaseUrl = "https://business.openapi.zalo.me";
    const fullUrl = `${znsBaseUrl}${endpoint}`;
    switch (method) {
      case "GET":
        return this.get(fullUrl, accessToken, params);
      case "POST":
        return this.post(fullUrl, accessToken, data, params);
      case "PUT":
        return this.put(fullUrl, accessToken, data, params);
      case "DELETE":
        return this.delete(fullUrl, accessToken, params);
      default:
        throw new Error(`Unsupported HTTP method: ${method}`);
    }
  }
  /**
   * Make request to OAuth endpoints with custom base URL
   */
  async oauthRequestWithUrl(method, url, data, headers) {
    return this.request({
      method,
      url,
      headers: headers || {},
      data: method === "POST" ? data : void 0,
      params: method === "GET" ? data : void 0
    });
  }
};

// src/services/auth.service.ts
var import_crypto = require("crypto");
var AuthService = class {
  constructor(client, appId, appSecret) {
    this.client = client;
    this.appId = appId;
    this.appSecret = appSecret;
    // Zalo OAuth & Graph endpoints - organized by functionality
    this.endpoints = {
      auth: {
        oaPermission: "https://oauth.zaloapp.com/v4/oa/permission",
        socialPermission: "https://oauth.zaloapp.com/v4/permission",
        oaToken: "https://oauth.zaloapp.com/v4/oa/access_token",
        socialToken: "https://oauth.zaloapp.com/v4/access_token",
        refreshOaToken: "https://oauth.zaloapp.com/v4/oa/access_token",
        refreshSocialToken: "https://openapi.zalo.me/v2.0/oauth/access_token"
      },
      social: {
        me: "https://graph.zalo.me/v2.0/me"
      }
    };
  }
  /**
   * Generate PKCE code verifier and challenge for Social API
   */
  generatePKCE() {
    const codeVerifier = (0, import_crypto.randomBytes)(32).toString("base64url");
    const codeChallenge = (0, import_crypto.createHash)("sha256").update(codeVerifier).digest("base64url");
    return {
      code_verifier: codeVerifier,
      code_challenge: codeChallenge,
      code_challenge_method: "S256"
    };
  }
  /**
   * Create OAuth authorization URL for Official Account with PKCE support
   *
   * @param redirectUri - The redirect URI after authorization
   * @param state - Optional state parameter for security. If not provided, auto-generates with 'zalo_oa_' prefix
   * @param usePkce - Whether to use PKCE for enhanced security. If true and pkce not provided, will auto-generate
   * @param pkce - Optional PKCE configuration for enhanced security. If usePkce=true and this is not provided, will be auto-generated
   * @returns Object containing the authorization URL, state, and PKCE config (if used)
   */
  createOAAuthUrl(redirectUri, state, pkce, usePkce = false) {
    const finalState = state || `zalo_oa_${(0, import_crypto.randomBytes)(16).toString("hex")}`;
    let finalPkce = pkce;
    if (usePkce && !pkce) {
      finalPkce = this.generatePKCE();
    }
    const params = new URLSearchParams({
      app_id: this.appId,
      redirect_uri: redirectUri,
      state: finalState
    });
    if (usePkce && finalPkce) {
      params.append("code_challenge", finalPkce.code_challenge);
      params.append("code_challenge_method", finalPkce.code_challenge_method);
    }
    const url = `${this.endpoints.auth.oaPermission}?${params.toString()}`;
    return {
      url,
      state: finalState,
      pkce: usePkce ? finalPkce : void 0
    };
  }
  /**
   * Create OAuth authorization URL for Social API
   */
  createSocialAuthUrl(redirectUri, state, pkce) {
    const params = new URLSearchParams({
      app_id: this.appId,
      redirect_uri: redirectUri,
      state: state || "social_auth"
    });
    if (pkce) {
      params.append("code_challenge", pkce.code_challenge);
      params.append("code_challenge_method", pkce.code_challenge_method);
    }
    return `${this.endpoints.auth.socialPermission}?${params.toString()}`;
  }
  /**
   * Exchange authorization code for Official Account access token
   * Now supports PKCE code_verifier for enhanced security
   */
  async getOAAccessToken(params) {
    try {
      const url = this.endpoints.auth.oaToken;
      const formData = new URLSearchParams();
      formData.append("code", params.code);
      formData.append("app_id", params.app_id);
      formData.append("grant_type", "authorization_code");
      if (params.code_verifier) {
        formData.append("code_verifier", params.code_verifier);
      }
      let result;
      try {
        result = await this.client.oauthRequestWithUrl(
          "POST",
          url,
          formData.toString(),
          {
            "Content-Type": "application/x-www-form-urlencoded",
            "secret_key": params.app_secret
          }
        );
      } catch (error) {
        if (error instanceof ZaloSDKError && error.details?.access_token) {
          result = error.details;
        } else {
          throw error;
        }
      }
      if (result.access_token) {
        return {
          access_token: result.access_token,
          expires_in: parseInt(result.expires_in) || result.expires_in,
          refresh_token: result.refresh_token
        };
      }
      if (result.data?.access_token) {
        return {
          access_token: result.data.access_token,
          expires_in: parseInt(result.data.expires_in) || result.data.expires_in,
          refresh_token: result.data.refresh_token
        };
      }
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.error_description || result.message || "Failed to get OA access token",
          result.error,
          result
        );
      }
      throw new ZaloSDKError("No access token received from Zalo API", -1, result);
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get OA access token: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Exchange authorization code for Social API access token
   */
  async getSocialAccessToken(params) {
    try {
      const url = this.endpoints.auth.socialToken;
      const formData = new URLSearchParams();
      formData.append("code", params.code);
      formData.append("app_id", params.app_id);
      formData.append("grant_type", "authorization_code");
      if (params.code_verifier) {
        formData.append("code_verifier", params.code_verifier);
      }
      let result;
      try {
        result = await this.client.oauthRequestWithUrl(
          "POST",
          url,
          formData.toString(),
          {
            "Content-Type": "application/x-www-form-urlencoded",
            secret_key: params.app_secret
          }
        );
      } catch (error) {
        if (error instanceof ZaloSDKError && error.details?.access_token) {
          result = error.details;
        } else {
          throw error;
        }
      }
      if (result.access_token) {
        return {
          access_token: result.access_token,
          expires_in: result.expires_in,
          refresh_token: result.refresh_token
        };
      }
      if (result.data?.access_token) {
        return {
          access_token: result.data.access_token,
          expires_in: result.data.expires_in,
          refresh_token: result.data.refresh_token
        };
      }
      if (result.error && result.error !== 0) {
        throw new ZaloSDKError(
          result.error_description || result.message || "Failed to get Social access token",
          result.error,
          result
        );
      }
      throw new ZaloSDKError("No access token received from Zalo API", -1, result);
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get Social access token: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Refresh Official Account access token
   */
  async refreshOAAccessToken(params) {
    try {
      const url = this.endpoints.auth.refreshOaToken;
      const formData = new URLSearchParams();
      formData.append("refresh_token", params.refresh_token);
      formData.append("app_id", params.app_id);
      formData.append("grant_type", "refresh_token");
      const result = await this.client.oauthRequestWithUrl(
        "POST",
        url,
        formData.toString(),
        {
          "Content-Type": "application/x-www-form-urlencoded",
          secret_key: params.app_secret
        }
      );
      if (!result.access_token) {
        throw new ZaloSDKError(
          "No access token received when refreshing OA token",
          -1
        );
      }
      return {
        access_token: result.access_token,
        refresh_token: result.refresh_token,
        expires_in: parseInt(result.expires_in)
      };
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to refresh OA access token: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Refresh Social API access token
   */
  async refreshSocialAccessToken(params) {
    try {
      const endpoint = this.endpoints.auth.refreshSocialToken;
      const requestParams = {
        app_id: params.app_id,
        grant_type: "refresh_token",
        refresh_token: params.refresh_token
      };
      const result = await this.client.apiGet(endpoint, "", requestParams);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.error_description || result.message || "Failed to refresh Social access token",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError(
          "No data received when refreshing Social token",
          -1
        );
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to refresh Social access token: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Get Social user information
   */
  async getSocialUserInfo(accessToken, fields = "id,name,picture") {
    try {
      const url = this.endpoints.social.me;
      const params = { fields };
      const result = await this.client.oauthRequestWithUrl("GET", url, params, {
        access_token: accessToken
      });
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get Social user info",
          result.error,
          result
        );
      }
      if (!result.id) {
        throw new ZaloSDKError("No user data received from Social API", -1);
      }
      return {
        id: result.id,
        name: result.name,
        picture: result.picture,
        is_sensitive: result.is_sensitive
      };
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get Social user info: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Validate access token by attempting to get user info
   */
  async validateAccessToken(accessToken, scope = "social" /* SOCIAL */) {
    try {
      if (scope === "social" /* SOCIAL */) {
        const userInfo = await this.getSocialUserInfo(accessToken, "id");
        return {
          valid: true,
          user_info: userInfo
        };
      } else {
        return { valid: true };
      }
    } catch (error) {
      return { valid: false };
    }
  }
  /**
   * Get all authentication URLs with optional PKCE support
   */
  getAuthUrls(redirectUri, usePkce = false, pkce) {
    const oaAuthResult = this.createOAAuthUrl(
      redirectUri,
      void 0,
      pkce,
      usePkce
    );
    return {
      oa_auth_url: oaAuthResult.url,
      social_auth_url: this.createSocialAuthUrl(redirectUri, void 0, pkce),
      token_url: this.endpoints.auth.socialToken,
      refresh_url: this.endpoints.auth.refreshOaToken
    };
  }
};

// src/services/oa.service.ts
var OAService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      // OA information
      info: "https://openapi.zalo.me/v2.0/oa/getoa",
      // Quota endpoints
      quota: {
        message: "https://openapi.zalo.me/v3.0/oa/quota/message"
      },
      // Profile management
      profile: {
        update: "https://openapi.zalo.me/v2.0/oa/update"
      },
      // Statistics
      statistics: {
        overview: "https://openapi.zalo.me/v2.0/oa/statistics"
      }
    };
  }
  /**
   * Get Official Account information
   */
  async getOAInfo(accessToken) {
    try {
      const endpoint = this.endpoints.info;
      const result = await this.client.apiGet(endpoint, accessToken);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get OA information",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No OA data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(`Failed to get OA info: ${error.message}`, -1, error);
    }
  }
  /**
   * Get detailed quota message information
   */
  async getDetailedQuotaMessage(accessToken, request) {
    try {
      const endpoint = this.endpoints.quota.message;
      const result = await this.client.apiPost(endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get detailed quota message",
          result.error,
          result
        );
      }
      return result.data || [];
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(`Failed to get detailed quota message: ${error.message}`, -1, error);
    }
  }
  /**
   * Get quota for specific product type
   */
  async getQuotaByProductType(accessToken, productType, quotaType) {
    const request = {
      quota_owner: "OA",
      product_type: productType,
      quota_type: quotaType
    };
    return this.getDetailedQuotaMessage(accessToken, request);
  }
  /**
   * Get available GMF quota
   */
  async getGMFQuota(accessToken, gmfType = "gmf10" /* GMF10 */) {
    return this.getQuotaByProductType(accessToken, gmfType);
  }
  /**
   * Get consultation message quota
   */
  async getConsultationQuota(accessToken) {
    return this.getQuotaByProductType(accessToken, "cs");
  }
  /**
   * Get transaction message quota
   */
  async getTransactionQuota(accessToken) {
    return this.getQuotaByProductType(accessToken, "transaction");
  }
  /**
   * Update OA profile information
   */
  async updateOAProfile(accessToken, profileData) {
    try {
      const endpoint = this.endpoints.profile.update;
      const result = await this.client.apiPost(endpoint, accessToken, profileData);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to update OA profile",
          result.error,
          result
        );
      }
      return true;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(`Failed to update OA profile: ${error.message}`, -1, error);
    }
  }
  /**
   * Get OA statistics (if available)
   */
  async getOAStatistics(accessToken) {
    try {
      const endpoint = this.endpoints.statistics.overview;
      const result = await this.client.apiGet(endpoint, accessToken);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get OA statistics",
          result.error,
          result
        );
      }
      return result.data || {
        total_followers: 0,
        new_followers_today: 0,
        messages_sent_today: 0,
        messages_received_today: 0
      };
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(`Failed to get OA statistics: ${error.message}`, -1, error);
    }
  }
  /**
   * Lấy thông tin quota tổng quan cho tin nhắn (tổng số asset và số còn khả dụng)
   * Lưu ý: Zalo hiện cung cấp API chi tiết qua endpoint quota/message theo asset.
   * Hàm này tổng hợp đơn giản để trả về cấu trúc MessageQuota phục vụ SDK.
   */
  async getMessageQuota(accessToken) {
    try {
      const request = {
        quota_owner: "OA"
      };
      const assets = await this.getDetailedQuotaMessage(accessToken, request);
      const totalAssets = assets.length;
      const availableAssets = assets.filter((a) => a.status === "available").length;
      const quota = {
        // Sử dụng tổng số asset làm daily_quota (ước lượng theo asset)
        daily_quota: totalAssets,
        // Số còn lại là số asset đang ở trạng thái available
        remaining_quota: availableAssets,
        // Quota type tổng hợp vì có thể gồm nhiều loại
        quota_type: "mixed",
        // Không có reset_time từ API asset -> trả 0
        reset_time: 0
      };
      return quota;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get message quota: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Check if OA has sufficient quota for message type
   */
  async hasQuotaForMessageType(accessToken, messageType, requiredCount = 1) {
    try {
      let productType;
      if (messageType === "promotion") {
        return true;
      } else {
        productType = messageType;
      }
      const quotaAssets = await this.getQuotaByProductType(accessToken, productType);
      const availableAssets = quotaAssets.filter((asset) => asset.status === "available");
      if (availableAssets.length === 0) {
        return false;
      }
      return true;
    } catch (error) {
      return true;
    }
  }
  /**
   * Get quota summary for all message types
   */
  async getQuotaSummary(accessToken) {
    try {
      const [consultation, transaction, gmf10, gmf50, gmf100] = await Promise.all([
        this.getConsultationQuota(accessToken),
        this.getTransactionQuota(accessToken),
        this.getGMFQuota(accessToken, "gmf10" /* GMF10 */),
        this.getGMFQuota(accessToken, "gmf50" /* GMF50 */),
        this.getGMFQuota(accessToken, "gmf100" /* GMF100 */)
      ]);
      return {
        consultation,
        transaction,
        gmf10,
        gmf50,
        gmf100
      };
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(`Failed to get quota summary: ${error.message}`, -1, error);
    }
  }
  /**
   * Validate OA access token by getting OA info
   */
  async validateOAToken(accessToken) {
    try {
      await this.getOAInfo(accessToken);
      return true;
    } catch (error) {
      return false;
    }
  }
};

// src/services/user.service.ts
var UserService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      // User management endpoints
      user: {
        getList: "https://openapi.zalo.me/v3.0/oa/user/getlist",
        postList: "https://openapi.zalo.me/v3.0/oa/user/getlist",
        getDetail: "https://openapi.zalo.me/v3.0/oa/user/detail",
        update: "https://openapi.zalo.me/v3.0/oa/user/update",
        delete: "https://openapi.zalo.me/v2.0/oa/deletefollowerinfo",
        getCustomInfo: "https://openapi.zalo.me/v3.0/oa/user/detail/custominfo",
        updateCustomInfo: "https://openapi.zalo.me/v3.0/oa/user/update/custominfo"
      },
      // Tag management endpoints
      tag: {
        addToUser: "https://openapi.zalo.me/v2.0/oa/tag/tagfollower",
        removeFromUser: "https://openapi.zalo.me/v2.0/oa/tag/rmfollowerfromtag",
        getList: "https://openapi.zalo.me/v2.0/oa/tag/gettagsofoa",
        delete: "https://openapi.zalo.me/v2.0/oa/tag/rmtag"
      }
    };
  }
  /**
   * Truy xuất chi tiết người dùng
   * Endpoint: https://openapi.zalo.me/v3.0/oa/user/detail
   * Method: GET with data parameter as JSON string
   *
   * @param accessToken OA access token
   * @param userId User ID to get details for
   * @returns Promise<UserInfo> - Detailed user information
   */
  async getUserInfo(accessToken, userId) {
    try {
      const dataObject = { user_id: userId };
      const params = {
        data: JSON.stringify(dataObject)
      };
      const result = await this.client.apiGet(
        this.endpoints.user.getDetail,
        accessToken,
        params
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get user information",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No user data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      this.handleError(error, "Failed to get user info");
    }
  }
  /**
  * Truy xuất chi tiết người dùng (bản dùng POST)
  * Endpoint: https://openapi.zalo.me/v3.0/oa/user/detail
  * Method: POST với body JSON
  *
  * @param accessToken OA access token
  * @param userId ID người dùng cần lấy thông tin
  * @returns Promise<UserInfo> - Thông tin chi tiết người dùng
  */
  async postUserInfo(accessToken, userId) {
    try {
      const body = {
        user_id: userId
      };
      const result = await this.client.apiPost(
        this.endpoints.user.getDetail,
        accessToken,
        body
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get user information (POST)",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No user data received from POST request", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      this.handleError(error, "Failed to get user info (POST)");
    }
  }
  /**
   * Truy xuất danh sách người dùng
   * Endpoint: https://openapi.zalo.me/v3.0/oa/user/getlist
   * Method: GET with data parameter as JSON string
   *
   * @param accessToken OA access token
   * @param request User list request parameters
   * @returns Promise<UserListResponse> - List of users with pagination info
   */
  async getUserList(accessToken, request) {
    try {
      const dataObject = {
        offset: request.offset,
        count: Math.min(request.count, 50),
        // Max 50 users per request
        ...request.tag_name && { tag_name: request.tag_name },
        ...request.last_interaction_period && {
          last_interaction_period: request.last_interaction_period
        },
        ...request.is_follower !== void 0 && {
          is_follower: request.is_follower
        }
      };
      const params = {
        data: JSON.stringify(dataObject)
      };
      const result = await this.client.apiGet(
        this.endpoints.user.getList,
        accessToken,
        params
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get user list",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No user list data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      this.handleError(error, "Failed to get user list");
    }
  }
  /**
   * Truy xuất danh sách người dùng (POST method)
   * Endpoint: https://openapi.zalo.me/v3.0/oa/user/getlist
   * Method: POST with data in request body
   *
   * @param accessToken OA access token
   * @param request User list request parameters
   * @returns Promise<UserListResponse> - List of users with pagination info
   */
  async postUserList(accessToken, request) {
    try {
      const dataObject = {
        offset: request.offset,
        count: Math.min(request.count, 50),
        // Max 50 users per request
        ...request.tag_name && { tag_name: request.tag_name },
        ...request.last_interaction_period && {
          last_interaction_period: request.last_interaction_period
        },
        ...request.is_follower !== void 0 && {
          is_follower: request.is_follower
        }
      };
      const result = await this.client.apiPost(
        this.endpoints.user.postList,
        accessToken,
        dataObject
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get user list",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No user list data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      this.handleError(error, "Failed to get user list");
    }
  }
  /**
   * Get all users with automatic pagination
   * Lấy tất cả users với phân trang tự động, đảm bảo lấy 100%
   *
   * @param accessToken Access token của OA
   * @param filters Bộ lọc tùy chọn
   * @returns Promise<UserInfo[]> - Danh sách đầy đủ tất cả users
   */
  async getAllUsers(accessToken, filters) {
    console.log("[getAllUsers] B\u1EAFt \u0111\u1EA7u l\u1EA5y danh s\xE1ch users v\u1EDBi filters:", filters);
    const users = [];
    let offset = 0;
    const count = 50;
    let totalUsers = 0;
    let fetchedUsers = 0;
    let failedUsers = 0;
    try {
      while (true) {
        console.log(`[getAllUsers] \u0110ang l\u1EA5y batch t\u1EA1i offset=${offset}, count=${count}`);
        const userListResponse = await this.postUserList(accessToken, {
          offset,
          count,
          ...filters
        });
        console.log(`[getAllUsers] Nh\u1EADn \u0111\u01B0\u1EE3c ${userListResponse.users.length} user IDs t\u1EEB postUserList`);
        if (offset === 0) {
          totalUsers = userListResponse.total;
          console.log(`[getAllUsers] \u2713 Total users to fetch: ${totalUsers}`);
          if (totalUsers > 1e4) {
            console.warn(
              `[getAllUsers] \u26A0 Warning: Total users (${totalUsers}) exceeds Zalo API limit (10000). Only first 10000 users can be fetched due to max offset = 9951.`
            );
          }
        }
        const userIds = userListResponse.users.map((u) => u.user_id);
        console.log(`[getAllUsers] B\u1EAFt \u0111\u1EA7u l\u1EA5y chi ti\u1EBFt cho ${userIds.length} users...`);
        const userInfos = [];
        for (let i = 0; i < userIds.length; i++) {
          const userId = userIds[i];
          console.log(`[getAllUsers] \u251C\u2500 [${i + 1}/${userIds.length}] \u0110ang l\u1EA5y info cho user: ${userId}`);
          try {
            const userInfo = await this.postUserInfo(accessToken, userId);
            userInfos.push(userInfo);
            console.log(`[getAllUsers] \u2502  \u2713 Th\xE0nh c\xF4ng: ${userInfo.display_name || "N/A"}`);
          } catch (error) {
            failedUsers++;
            console.warn(`[getAllUsers] \u2502  \u26A0 Skip user ${userId} do l\u1ED7i: ${error.message}`);
          }
          if (i < userIds.length - 1) {
            await new Promise((resolve) => setTimeout(resolve, 100));
          }
        }
        console.log(`[getAllUsers] \u2713 Ho\xE0n th\xE0nh l\u1EA5y chi ti\u1EBFt ${userInfos.length} users trong batch n\xE0y`);
        users.push(...userInfos);
        fetchedUsers += userInfos.length;
        console.log(`[getAllUsers] \u{1F4CA} Progress: \u0110\xE3 l\u1EA5y ${fetchedUsers}/${totalUsers} users (${Math.round(fetchedUsers / totalUsers * 100)}%)`);
        if (userListResponse.users.length === 0 || fetchedUsers >= totalUsers || userListResponse.users.length < count) {
          console.log(`[getAllUsers] \u{1F6D1} \u0110i\u1EC1u ki\u1EC7n d\u1EEBng:`, {
            noMoreUsers: userListResponse.users.length === 0,
            reachedTotal: fetchedUsers >= totalUsers,
            lastPage: userListResponse.users.length < count
          });
          break;
        }
        offset += count;
        console.log(`[getAllUsers] \u27A1 Chuy\u1EC3n sang batch ti\u1EBFp theo, offset=${offset}`);
        if (offset > 9951) {
          console.warn(
            `[getAllUsers] \u26A0 Reached Zalo maximum offset limit (9951), stopping pagination`
          );
          break;
        }
      }
      console.log(`[getAllUsers] \u2705 HO\xC0N TH\xC0NH: Successfully fetched ${users.length}/${totalUsers} users` + (failedUsers > 0 ? ` (${failedUsers} failed/skipped)` : ""));
      return users;
    } catch (error) {
      console.error("[getAllUsers] \u274C L\u1ED6I:", {
        message: error.message,
        code: error.code || error.error,
        offset,
        fetchedSoFar: fetchedUsers,
        totalTarget: totalUsers
      });
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      this.handleError(error, "Failed to get all users");
    }
  }
  /**
   * Get all user IDs only (without detailed info) - Faster version
   * Chỉ lấy danh sách user ID, không lấy thông tin chi tiết - Nhanh hơn
   *
   * @param accessToken Access token của OA
   * @param filters Bộ lọc tùy chọn
   * @returns Promise<string[]> - Danh sách user IDs
   */
  async getAllUserIds(accessToken, filters) {
    const userIds = [];
    let offset = 0;
    const count = 50;
    let totalUsers = 0;
    try {
      while (true) {
        const userListResponse = await this.postUserList(accessToken, {
          offset,
          count,
          ...filters
        });
        if (offset === 0) {
          totalUsers = userListResponse.total;
          console.log(`Total user IDs to fetch: ${totalUsers}`);
          if (totalUsers > 1e4) {
            console.warn(
              `Warning: Total users (${totalUsers}) exceeds Zalo API limit (10000). Only first 10000 users can be fetched due to max offset = 9951.`
            );
          }
        }
        const currentUserIds = userListResponse.users.map((u) => u.user_id);
        userIds.push(...currentUserIds);
        console.log(
          `Fetched ${userIds.length}/${Math.min(totalUsers, 1e4)} user IDs`
        );
        if (userListResponse.users.length === 0 || userIds.length >= Math.min(totalUsers, 1e4) || userListResponse.users.length < count) {
          break;
        }
        offset += count;
        if (offset > 9951) {
          console.warn(
            "Reached Zalo maximum offset limit (9951), stopping pagination"
          );
          break;
        }
      }
      console.log(`Successfully fetched ${userIds.length} user IDs`);
      return userIds;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      this.handleError(error, "Failed to get all user IDs");
    }
  }
  /**
   * Get all users with advanced filtering based on UserInfo fields
   * Lấy tất cả users với bộ lọc nâng cao dựa trên các trường của UserInfo
   *
   * @param accessToken Access token của OA
   * @param filters Bộ lọc nâng cao
   * @returns Promise<UserInfo[]> - Danh sách users đã được lọc
   */
  async getAllUsersWithAdvancedFilters(accessToken, filters = {}) {
    try {
      const hasOnlyUserIdsFilter = filters.userIds && filters.userIds.length > 0 && Object.keys(filters).length === 1;
      if (hasOnlyUserIdsFilter) {
        console.log(`Fetching ${filters.userIds.length} specific users by IDs`);
        const userInfoPromises = filters.userIds.map(
          (userId) => this.getUserInfo(accessToken, userId)
        );
        return await Promise.all(userInfoPromises);
      }
      const basicFilters = {
        ...filters.tag_name && { tag_name: filters.tag_name },
        ...filters.last_interaction_period && {
          last_interaction_period: filters.last_interaction_period
        },
        ...filters.is_follower !== void 0 && {
          is_follower: filters.is_follower
        }
      };
      console.log("Fetching users with basic filters:", basicFilters);
      const allUsers = await this.getAllUsers(accessToken, basicFilters);
      const filteredUsers = allUsers.filter((user) => {
        if (filters.userIds && filters.userIds.length > 0) {
          if (!filters.userIds.includes(user.user_id)) {
            return false;
          }
        }
        if (filters.display_name_contains && !user.display_name?.toLowerCase().includes(filters.display_name_contains.toLowerCase())) {
          return false;
        }
        if (filters.user_alias_contains && !user.user_alias?.toLowerCase().includes(filters.user_alias_contains.toLowerCase())) {
          return false;
        }
        if (filters.is_sensitive !== void 0 && user.is_sensitive !== filters.is_sensitive) {
          return false;
        }
        if (filters.last_interaction_date_range) {
          const userDate = this.parseDate(user.user_last_interaction_date);
          const fromDate = this.parseDate(
            filters.last_interaction_date_range.from
          );
          const toDate = this.parseDate(filters.last_interaction_date_range.to);
          if (userDate < fromDate || userDate > toDate) {
            return false;
          }
        }
        if (filters.tag_names && filters.tag_names.length > 0) {
          const userTags = user.tags_and_notes_info.tag_names || [];
          const hasAnyTag = filters.tag_names.some(
            (tag) => userTags.includes(tag)
          );
          if (!hasAnyTag) {
            return false;
          }
        }
        if (filters.require_all_tags && filters.require_all_tags.length > 0) {
          const userTags = user.tags_and_notes_info.tag_names || [];
          const hasAllTags = filters.require_all_tags.every(
            (tag) => userTags.includes(tag)
          );
          if (!hasAllTags) {
            return false;
          }
        }
        if (filters.exclude_tags && filters.exclude_tags.length > 0) {
          const userTags = user.tags_and_notes_info.tag_names || [];
          const hasExcludedTag = filters.exclude_tags.some(
            (tag) => userTags.includes(tag)
          );
          if (hasExcludedTag) {
            return false;
          }
        }
        if (filters.notes_contains) {
          const userNotes = user.tags_and_notes_info.notes || [];
          const hasNote = userNotes.some(
            (note) => note.toLowerCase().includes(filters.notes_contains.toLowerCase())
          );
          if (!hasNote) {
            return false;
          }
        }
        if (user.shared_info) {
          if (filters.city && user.shared_info.city?.toLowerCase() !== filters.city.toLowerCase()) {
            return false;
          }
          if (filters.district && user.shared_info.district?.toLowerCase() !== filters.district.toLowerCase()) {
            return false;
          }
          if (filters.phone_contains && !user.shared_info.phone?.includes(filters.phone_contains)) {
            return false;
          }
          if (filters.name_contains && !user.shared_info.name?.toLowerCase().includes(filters.name_contains.toLowerCase())) {
            return false;
          }
          if (filters.address_contains && !user.shared_info.address?.toLowerCase().includes(filters.address_contains.toLowerCase())) {
            return false;
          }
          if (filters.dob_range && user.shared_info.user_dob) {
            const userDob = this.parseDate(user.shared_info.user_dob);
            const fromDob = this.parseDate(filters.dob_range.from);
            const toDob = this.parseDate(filters.dob_range.to);
            if (userDob < fromDob || userDob > toDob) {
              return false;
            }
          }
          if (filters.age_range && user.shared_info.user_dob) {
            const userAge = this.calculateAge(user.shared_info.user_dob);
            if (userAge < filters.age_range.min || userAge > filters.age_range.max) {
              return false;
            }
          }
        } else {
          if (filters.city || filters.district || filters.phone_contains || filters.name_contains || filters.address_contains || filters.dob_range || filters.age_range) {
            return false;
          }
        }
        if (filters.external_id_contains && !user.user_external_id?.includes(filters.external_id_contains)) {
          return false;
        }
        if (filters.has_shared_info !== void 0 && !!user.shared_info !== filters.has_shared_info) {
          return false;
        }
        if (filters.has_phone !== void 0 && !!user.shared_info?.phone !== filters.has_phone) {
          return false;
        }
        if (filters.has_address !== void 0 && !!user.shared_info?.address !== filters.has_address) {
          return false;
        }
        return true;
      });
      console.log(
        `Applied advanced filters: ${allUsers.length} -> ${filteredUsers.length} users`
      );
      return filteredUsers;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      this.handleError(error, "Failed to get users with advanced filters");
    }
  }
  /**
   * Parse date from dd/MM/yyyy format to Date object
   * Helper method cho các filter date
   */
  parseDate(dateString) {
    if (!dateString) return /* @__PURE__ */ new Date(0);
    const [day, month, year] = dateString.split("/").map(Number);
    return new Date(year, month - 1, day);
  }
  /**
   * Calculate age from date of birth
   * Helper method cho age filter
   */
  calculateAge(dobString) {
    if (!dobString) return 0;
    const dob = this.parseDate(dobString);
    const today = /* @__PURE__ */ new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    if (monthDiff < 0 || monthDiff === 0 && today.getDate() < dob.getDate()) {
      age--;
    }
    return age;
  }
  /**
   * Cập nhật chi tiết người dùng
   * Endpoint: https://openapi.zalo.me/v3.0/oa/user/update
   * Method: POST
   *
   * @param accessToken OA access token
   * @param request Update user request with user_id and optional fields
   * @returns Promise<boolean> - true if successful
   */
  async updateUser(accessToken, request) {
    try {
      const result = await this.client.apiPost(
        this.endpoints.user.update,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to update user",
          result.error,
          result
        );
      }
      return true;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to update user: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async deleteUserInfo(accessToken, userId) {
    try {
      const request = { user_id: userId };
      const result = await this.client.apiPost(
        this.endpoints.user.delete,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to delete user info",
          result.error,
          result
        );
      }
      return true;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to delete user info: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async getUserCustomInfo(accessToken, request) {
    try {
      const dataObject = {
        user_id: request.user_id,
        ...request.fields_to_export && {
          fields_to_export: request.fields_to_export
        }
      };
      const params = {
        data: JSON.stringify(dataObject)
      };
      const result = await this.client.apiGet(
        this.endpoints.user.getCustomInfo,
        accessToken,
        params
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get user custom info",
          result.error,
          result
        );
      }
      return result;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get user custom info: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async updateUserCustomInfo(accessToken, request) {
    try {
      const result = await this.client.apiPost(
        this.endpoints.user.updateCustomInfo,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to update user custom info",
          result.error,
          result
        );
      }
      return true;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to update user custom info: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Lấy danh sách nhãn
   * Endpoint: https://openapi.zalo.me/v2.0/oa/tag/gettagsofoa
   * Method: GET
   *
   * @param accessToken OA access token
   * @returns Promise<string[]> - Array of tag names
   */
  async getLabels(accessToken) {
    try {
      const result = await this.client.apiGet(
        this.endpoints.tag.getList,
        accessToken
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get labels",
          result.error,
          result
        );
      }
      return result.data || [];
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get labels: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async addTagToUser(accessToken, request) {
    try {
      const result = await this.client.apiPost(
        this.endpoints.tag.addToUser,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to add tag to user",
          result.error,
          result
        );
      }
      return true;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to add tag to user: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gỡ nhãn người dùng
   * Endpoint: https://openapi.zalo.me/v2.0/oa/tag/rmfollowerfromtag
   *
   * @param accessToken OA access token
   * @param request Remove tag request with user_id and tag_name
   * @returns Promise<boolean> - true if successful
   */
  async removeTagFromUser(accessToken, request) {
    try {
      const result = await this.client.apiPost(
        this.endpoints.tag.removeFromUser,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to remove tag from user",
          result.error,
          result
        );
      }
      return true;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to remove tag from user: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Xóa nhãn
   * Endpoint: https://openapi.zalo.me/v2.0/oa/tag/rmtag
   * Method: POST
   *
   * @param accessToken OA access token
   * @param tagName Tag name to delete
   * @returns Promise<boolean> - true if successful
   */
  async deleteLabel(accessToken, tagName) {
    try {
      const request = { tag_name: tagName };
      const result = await this.client.apiPost(
        this.endpoints.tag.delete,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to delete label",
          result.error,
          result
        );
      }
      return true;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to delete label: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Get users by tag
   */
  async getUsersByTag(accessToken, tagName, offset = 0, count = 50) {
    return this.getUserList(accessToken, {
      offset,
      count,
      tag_name: tagName
    });
  }
  /**
   * Get followers only
   */
  async getFollowers(accessToken, offset = 0, count = 50) {
    return this.getUserList(accessToken, {
      offset,
      count,
      is_follower: true
    });
  }
  /**
   * Get users by interaction period
   */
  async getUsersByInteraction(accessToken, period, offset = 0, count = 50) {
    return this.getUserList(accessToken, {
      offset,
      count,
      last_interaction_period: period
    });
  }
  /**
   * Bulk tag operations
   */
  async bulkAddTag(accessToken, userIds, tagName) {
    const results = {
      success: [],
      failed: []
    };
    for (const userId of userIds) {
      try {
        await this.addTagToUser(accessToken, {
          user_id: userId,
          tag_name: tagName
        });
        results.success.push(userId);
      } catch (error) {
        results.failed.push(userId);
      }
    }
    return results;
  }
  /**
   * Bulk remove tag operations
   */
  async bulkRemoveTag(accessToken, userIds, tagName) {
    const results = {
      success: [],
      failed: []
    };
    for (const userId of userIds) {
      try {
        await this.removeTagFromUser(accessToken, {
          user_id: userId,
          tag_name: tagName
        });
        results.success.push(userId);
      } catch (error) {
        results.failed.push(userId);
      }
    }
    return results;
  }
  handleError(error, message) {
    const errorMessage = error instanceof Error ? error.message : "Unknown error";
    throw new ZaloSDKError(`${message}: ${errorMessage}`, -1, error);
  }
};

// src/services/zns.service.ts
var ZNSService = class {
  constructor(client) {
    this.client = client;
    // Zalo ZNS endpoints - organized by functionality
    this.endpoints = {
      message: {
        sendTemplate: "https://business.openapi.zalo.me/message/template",
        sendHashPhone: "https://business.openapi.zalo.me/message/template/hash_phone",
        sendDev: "https://business.openapi.zalo.me/message/template/dev",
        sendRsa: "https://business.openapi.zalo.me/message/template/rsa",
        sendJourney: "https://business.openapi.zalo.me/message/template/journey",
        sendByUid: "https://openapi.zalo.me/v3.0/oa/message/template",
        status: "https://business.openapi.zalo.me/message/status",
        quota: "https://business.openapi.zalo.me/message/quota",
        templateTag: "https://business.openapi.zalo.me/message/template-tag"
      },
      template: {
        list: "https://business.openapi.zalo.me/template/all",
        detail: "https://business.openapi.zalo.me/template/info/v2",
        create: "https://business.openapi.zalo.me/template/create",
        edit: "https://business.openapi.zalo.me/template/edit",
        uploadImage: "https://business.openapi.zalo.me/upload/image",
        quality: "https://business.openapi.zalo.me/template/quality",
        sampleData: "https://business.openapi.zalo.me/template/sample-data"
      },
      rating: {
        get: "https://business.openapi.zalo.me/rating/get"
      },
      quality: {
        oa: "https://business.openapi.zalo.me/quality"
      }
    };
  }
  /**
   * Send ZNS message
   * @param accessToken OA access token
   * @param message ZNS message data
   * @returns Send result
   */
  async sendMessage(accessToken, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.sendTemplate,
        accessToken,
        message
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to send ZNS message");
    }
  }
  /**
   * Send ZNS message with phone hash
   * @param accessToken OA access token
   * @param message ZNS message with hashed phone
   * @returns Send result
   */
  async sendHashPhoneMessage(accessToken, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.sendHashPhone,
        accessToken,
        message
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to send ZNS hash phone message");
    }
  }
  /**
   * Send ZNS message in development mode
   * @param accessToken OA access token
   * @param message ZNS dev mode message
   * @returns Send result
   */
  async sendDevModeMessage(accessToken, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.sendDev,
        accessToken,
        message
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to send ZNS dev mode message");
    }
  }
  /**
   * Send ZNS message with RSA encryption
   * @param accessToken OA access token
   * @param message ZNS RSA message
   * @returns Send result
   */
  async sendRsaMessage(accessToken, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.sendRsa,
        accessToken,
        message
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to send ZNS RSA message");
    }
  }
  /**
   * Send ZNS journey message
   * @param accessToken OA access token
   * @param message ZNS journey message
   * @returns Send result
   */
  async sendJourneyMessage(accessToken, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.sendJourney,
        accessToken,
        message
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to send ZNS journey message");
    }
  }
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
  async sendMessageByUid(accessToken, message) {
    try {
      if (!message.user_id) {
        throw new ZaloSDKError("user_id l\xE0 b\u1EAFt bu\u1ED9c", 400);
      }
      if (!message.template_id) {
        throw new ZaloSDKError("template_id l\xE0 b\u1EAFt bu\u1ED9c", 400);
      }
      if (!message.template_data || typeof message.template_data !== "object") {
        throw new ZaloSDKError("template_data ph\u1EA3i l\xE0 object", 400);
      }
      const response = await this.client.apiPost(
        this.endpoints.message.sendByUid,
        accessToken,
        message
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to send ZNS message by UID");
    }
  }
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
  async getMessageStatus(accessToken, messageId) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.message.status,
        accessToken,
        { message_id: messageId }
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get ZNS message status");
    }
  }
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
  async getBatchMessageStatus(accessToken, messageIds) {
    try {
      if (!Array.isArray(messageIds) || messageIds.length === 0) {
        throw new ZaloSDKError("messageIds ph\u1EA3i l\xE0 array v\xE0 kh\xF4ng \u0111\u01B0\u1EE3c r\u1ED7ng", 400);
      }
      if (messageIds.length > 100) {
        throw new ZaloSDKError("Kh\xF4ng \u0111\u01B0\u1EE3c ki\u1EC3m tra qu\xE1 100 message c\xF9ng l\xFAc", 400);
      }
      const uniqueMessageIds = [...new Set(messageIds)];
      const statusPromises = uniqueMessageIds.map(async (messageId) => {
        try {
          const status = await this.getMessageStatus(accessToken, messageId);
          return {
            messageId,
            success: true,
            data: status.data
          };
        } catch (error) {
          return {
            messageId,
            success: false,
            error: error instanceof ZaloSDKError ? error.message : "Unknown error"
          };
        }
      });
      const results = await Promise.all(statusPromises);
      const responseData = {};
      let hasErrors = false;
      results.forEach((result) => {
        if (result.success && result.data) {
          responseData[result.messageId] = result.data;
        } else {
          hasErrors = true;
          responseData[result.messageId] = {
            delivery_time: "",
            message: result.error || "Failed to get status",
            status: -1
          };
        }
      });
      return {
        error: hasErrors ? 1 : 0,
        message: hasErrors ? "Some messages failed to get status" : "Success",
        data: responseData
      };
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get batch ZNS message status");
    }
  }
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
  async getQuotaInfo(accessToken) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.message.quota,
        accessToken
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get ZNS quota info");
    }
  }
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
  async getAllowedContentTypes(accessToken) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.message.templateTag,
        accessToken
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(
        error,
        "Failed to get ZNS allowed content types"
      );
    }
  }
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
  async getTemplateList(accessToken, offset = 0, limit = 10, status) {
    try {
      if (limit > 100) {
        throw new ZaloSDKError("Limit kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 100", 400);
      }
      const params = { offset, limit };
      if (status !== void 0) {
        params.status = status;
      }
      const response = await this.client.apiGet(
        this.endpoints.template.list,
        accessToken,
        params
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get ZNS template list");
    }
  }
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
  async getTemplateDetails(accessToken, templateId) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.template.detail,
        accessToken,
        { template_id: templateId }
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get ZNS template details");
    }
  }
  /**
   * Custom: Lấy tất cả template kèm thông tin chi tiết cho 1 accessToken
   * - Tự động phân trang để lấy đủ tổng số template
   * - Lấy chi tiết song song theo batch để tối ưu thời gian
   */
  async getAllTemplatesWithDetails(accessToken, status) {
    try {
      const limitPerPage = 100;
      let offset = 0;
      const templateIds = [];
      let total = 0;
      while (true) {
        const listResp = await this.getTemplateList(accessToken, offset, limitPerPage, status);
        const items = listResp.data || [];
        if (listResp.metadata && typeof listResp.metadata.total === "number") {
          total = listResp.metadata.total;
        }
        for (const item of items) {
          if (!templateIds.includes(item.templateId)) {
            templateIds.push(item.templateId);
          }
        }
        if (items.length < limitPerPage || templateIds.length >= total) {
          break;
        }
        offset += limitPerPage;
      }
      if (templateIds.length === 0) {
        return { error: 0, message: "Success", data: [], metadata: { total } };
      }
      const batchSize = 10;
      const detailResults = [];
      let hasErrors = false;
      for (let i = 0; i < templateIds.length; i += batchSize) {
        const batchIds = templateIds.slice(i, i + batchSize);
        const settled = await Promise.allSettled(
          batchIds.map((id) => this.getTemplateDetails(accessToken, id))
        );
        for (const res of settled) {
          if (res.status === "fulfilled" && res.value && res.value.data) {
            detailResults.push(res.value.data);
          } else {
            hasErrors = true;
          }
        }
      }
      return {
        error: hasErrors ? 1 : 0,
        message: hasErrors ? "Some template details failed to fetch" : "Success",
        data: detailResults,
        metadata: { total }
      };
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get all templates with details");
    }
  }
  /**
   * Custom: Lấy tất cả template chi tiết cho nhiều accessToken (nhiều OA)
   * Đầu ra nhóm theo từng accessToken
   */
  async getAllTemplatesWithDetailsByTokens(accessTokens, status) {
    try {
      const uniqueTokens = Array.from(new Set(accessTokens.filter((t) => t && t.trim().length > 0)));
      if (uniqueTokens.length === 0) {
        return { error: 0, message: "Success", data: [] };
      }
      const settled = await Promise.allSettled(
        uniqueTokens.map((token) => this.getAllTemplatesWithDetails(token, status))
      );
      const groups = [];
      let hasErrors = false;
      settled.forEach((res, index) => {
        const token = uniqueTokens[index];
        if (res.status === "fulfilled") {
          const payload = res.value;
          groups.push({
            accessToken: token,
            templates: payload.data,
            total: payload.metadata.total
          });
          if (payload.error !== 0) {
            hasErrors = true;
          }
        } else {
          hasErrors = true;
          groups.push({ accessToken: token, templates: [], total: 0 });
        }
      });
      return {
        error: hasErrors ? 1 : 0,
        message: hasErrors ? "Some OA groups failed to fetch" : "Success",
        data: groups
      };
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get templates with details by tokens");
    }
  }
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
  async getTemplateSampleData(accessToken, templateId) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.template.sampleData,
        accessToken,
        { template_id: templateId }
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(
        error,
        "Failed to get ZNS template sample data"
      );
    }
  }
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
  async getCustomerRating(accessToken, templateId, fromTime, toTime, offset, limit) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.rating.get,
        accessToken,
        {
          template_id: templateId,
          from_time: fromTime,
          to_time: toTime,
          offset,
          limit
        }
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get customer rating");
    }
  }
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
  async getOAQuality(accessToken) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.quality.oa,
        accessToken
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to get OA quality information");
    }
  }
  /**
   * Create ZNS template
   * @param accessToken OA access token
   * @param templateData Template creation data theo chuẩn Zalo API
   * @returns Created template response
   *
   * API: POST https://business.openapi.zalo.me/template/create
   */
  async createTemplate(accessToken, templateData) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.template.create,
        accessToken,
        templateData
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to create ZNS template");
    }
  }
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
  async updateTemplate(accessToken, templateData) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.template.edit,
        accessToken,
        templateData
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to edit ZNS template");
    }
  }
  /**
   * Upload image for ZNS template
   * @param accessToken OA access token
   * @param imageFile Image file (Buffer or ReadableStream)
   * @param filename Filename with extension
   * @returns Upload result with media_id
   *
   * API: POST https://business.openapi.zalo.me/upload/image
   */
  async uploadImage(accessToken, imageFile, filename = "image.jpg") {
    try {
      const response = await this.client.apiUploadFile(
        this.endpoints.template.uploadImage,
        accessToken,
        imageFile,
        filename
      );
      return response;
    } catch (error) {
      throw this.handleZNSError(error, "Failed to upload ZNS image");
    }
  }
  /**
   * Hàm validate file ảnh / logo ZNS độc lập để Backend / AI Agent gọi kiểm tra trước khi upload
   * Trả về kết quả đánh giá (isValid, errors, warnings) thay vì throw exception
   * @param filename Tên file kèm đuôi (.jpg, .jpeg, .png)
   * @param fileSize Dung lượng file theo byte
   * @param imageType Loại ảnh ("LOGO" hoặc "IMAGE")
   * @param dimensions Kích thước ảnh (chiều rộng x chiều cao, optional)
   */
  validateZnsImage(filename, fileSize, imageType, dimensions) {
    const errors = [];
    const warnings = [];
    const allowedExtensions = [".jpg", ".jpeg", ".png"];
    const fileExtension = filename.toLowerCase().substring(filename.lastIndexOf("."));
    if (!allowedExtensions.includes(fileExtension)) {
      errors.push(`\u0110\u1ECBnh d\u1EA1ng file '${fileExtension || "kh\xF4ng x\xE1c \u0111\u1ECBnh"}' kh\xF4ng h\u1EE3p l\u1EC7. Zalo ZNS ch\u1EC9 ch\u1EA5p nh\u1EADn JPG ho\u1EB7c PNG.`);
    }
    if (imageType === "LOGO" && fileExtension !== ".png") {
      errors.push("Logo b\u1EAFt bu\u1ED9c ph\u1EA3i c\xF3 \u0111\u1ECBnh d\u1EA1ng PNG (\u0111\u1EC3 h\u1ED7 tr\u1EE3 n\u1EC1n trong su\u1ED1t theo quy chu\u1EA9n Zalo ZNS cho Light & Dark Mode).");
    }
    const maxSize = 500 * 1024;
    if (fileSize > maxSize) {
      errors.push(`Dung l\u01B0\u1EE3ng file ${(fileSize / 1024).toFixed(1)} KB v\u01B0\u1EE3t qu\xE1 gi\u1EDBi h\u1EA1n t\u1ED1i \u0111a 500KB c\u1EE7a Zalo ZNS.`);
    } else if (fileSize > 400 * 1024) {
      warnings.push(`Dung l\u01B0\u1EE3ng file ${(fileSize / 1024).toFixed(1)} KB kh\xE1 l\u1EDBn (g\u1EA7n ng\u01B0\u1EE1ng 500KB), khuy\u1EBFn ngh\u1ECB n\xE9n d\u01B0\u1EDBi 300KB \u0111\u1EC3 tin nh\u1EAFn load nhanh.`);
    }
    if (dimensions?.width && dimensions?.height) {
      const { width, height } = dimensions;
      if (imageType === "LOGO") {
        if (width !== 400 || height !== 96) {
          errors.push(`K\xEDch th\u01B0\u1EDBc logo (${width}x${height} px) kh\xF4ng \u0111\xFAng chu\u1EA9n Zalo ZNS (y\xEAu c\u1EA7u ch\xEDnh x\xE1c 400x96 px).`);
        }
      } else if (imageType === "IMAGE") {
        const ratio = width / height;
        const isRatio16x9 = Math.abs(ratio - 16 / 9) <= 0.02;
        if (!isRatio16x9) {
          errors.push(`T\u1EC9 l\u1EC7 h\xECnh \u1EA3nh (${width}x${height} px, t\u1EC9 l\u1EC7 ${ratio.toFixed(2)}) kh\xF4ng \u0111\u1EA1t t\u1EC9 l\u1EC7 chu\u1EA9n 16:9 c\u1EE7a Zalo ZNS.`);
        }
      }
    }
    return {
      isValid: errors.length === 0,
      errors,
      warnings: warnings.length > 0 ? warnings : void 0
    };
  }
  /**
   * Comprehensive validation for ZNS template request
   * Trả về kết quả đánh giá (isValid, errors, warnings) thay vì throw exception để Backend và AI Agent phân tích
   */
  validateZNSTemplateRequest(templateData) {
    const errors = [];
    const warnings = [];
    try {
      this.validateBasicFields(templateData);
    } catch (e) {
      errors.push(e.message || String(e));
    }
    try {
      this.validateTemplateTypeTagCompatibility(templateData.template_type, templateData.tag);
    } catch (e) {
      errors.push(e.message || String(e));
    }
    try {
      this.validateLayoutStructure(templateData.template_type, templateData.layout);
    } catch (e) {
      errors.push(e.message || String(e));
    }
    if (templateData.params && templateData.params.length > 0) {
      try {
        this.validateParams(templateData.params, templateData.layout);
      } catch (e) {
        errors.push(e.message || String(e));
      }
    }
    try {
      this.validateTagParametersRequirement(templateData.tag, templateData.params || []);
    } catch (e) {
      errors.push(e.message || String(e));
    }
    if (templateData.tag === "3") {
      try {
        this.validatePromotionButtons(templateData.layout, templateData.note);
      } catch (e) {
        errors.push(e.message || String(e));
      }
    }
    if (templateData.note) {
      if (templateData.note.length < 1 || templateData.note.length > 400) {
        errors.push("note ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 400 k\xFD t\u1EF1");
      }
    }
    return {
      isValid: errors.length === 0,
      errors,
      warnings: warnings.length > 0 ? warnings : void 0
    };
  }
  /**
   * Validate basic required fields
   */
  validateBasicFields(templateData) {
    if (!templateData.template_name) {
      throw new ZaloSDKError("template_name l\xE0 b\u1EAFt bu\u1ED9c", 400);
    }
    if (templateData.template_name.length < 10 || templateData.template_name.length > 60) {
      throw new ZaloSDKError("template_name ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 10 \u0111\u1EBFn 60 k\xFD t\u1EF1", 400);
    }
    if (!templateData.template_type) {
      throw new ZaloSDKError("template_type l\xE0 b\u1EAFt bu\u1ED9c", 400);
    }
    if (![1, 2, 3, 4, 5].includes(templateData.template_type)) {
      throw new ZaloSDKError("template_type ph\u1EA3i l\xE0 1, 2, 3, 4 ho\u1EB7c 5", 400);
    }
    if (!templateData.tag) {
      throw new ZaloSDKError("tag l\xE0 b\u1EAFt bu\u1ED9c", 400);
    }
    if (!["1", "2", "3"].includes(templateData.tag)) {
      throw new ZaloSDKError("tag ph\u1EA3i l\xE0 '1', '2' ho\u1EB7c '3'", 400);
    }
    if (!templateData.layout) {
      throw new ZaloSDKError("layout l\xE0 b\u1EAFt bu\u1ED9c", 400);
    }
    if (!Array.isArray(templateData.layout) && typeof templateData.layout !== "object") {
      throw new ZaloSDKError("layout ph\u1EA3i l\xE0 array ho\u1EB7c object", 400);
    }
    if (!templateData.tracking_id) {
      throw new ZaloSDKError("tracking_id l\xE0 b\u1EAFt bu\u1ED9c", 400);
    }
  }
  /**
   * Validate template_type and tag compatibility
   */
  validateTemplateTypeTagCompatibility(templateType, tag) {
    const compatibility = {
      1: ["1", "2", "3"],
      // ZNS tùy chỉnh
      2: ["1"],
      // ZNS xác thực
      3: ["1"],
      // ZNS yêu cầu thanh toán
      4: ["1", "2", "3"],
      // ZNS voucher
      5: ["2"]
      // ZNS đánh giá dịch vụ
    };
    if (!compatibility[templateType]?.includes(tag)) {
      throw new ZaloSDKError(`template_type ${templateType} kh\xF4ng t\u01B0\u01A1ng th\xEDch v\u1EDBi tag ${tag}`, 400);
    }
  }
  /**
   * Validate layout structure based on template type
   */
  validateLayoutStructure(templateType, layout) {
    let headerComponents = [];
    let bodyComponents = [];
    let footerComponents = [];
    let allComponents = [];
    if (Array.isArray(layout)) {
      headerComponents = layout.filter((item) => this.isHeaderComponent(item));
      bodyComponents = layout.filter((item) => this.isBodyComponent(item));
      footerComponents = layout.filter((item) => this.isFooterComponent(item));
      allComponents = layout;
    } else if (layout && typeof layout === "object") {
      if (layout.header?.components) {
        headerComponents = layout.header.components;
      }
      if (layout.body?.components) {
        bodyComponents = layout.body.components;
      }
      if (layout.footer?.components) {
        footerComponents = layout.footer.components;
      }
      allComponents = [...headerComponents, ...bodyComponents, ...footerComponents];
    } else {
      throw new ZaloSDKError("layout ph\u1EA3i l\xE0 array ho\u1EB7c object c\xF3 c\u1EA5u tr\xFAc header/body/footer", 400);
    }
    switch (templateType) {
      case 1:
        this.validateCustomTemplateLayout(headerComponents, bodyComponents, footerComponents);
        break;
      case 2:
        this.validateAuthTemplateLayout(headerComponents, bodyComponents, footerComponents);
        break;
      case 3:
        this.validatePaymentTemplateLayout(headerComponents, bodyComponents, footerComponents);
        break;
      case 4:
        this.validateVoucherTemplateLayout(headerComponents, bodyComponents, footerComponents);
        break;
      case 5:
        this.validateRatingTemplateLayout(headerComponents, bodyComponents, footerComponents);
        break;
    }
    allComponents.forEach((component) => this.validateComponent(component));
  }
  /**
   * Check if component belongs to header
   */
  isHeaderComponent(component) {
    return "LOGO" in component || "IMAGES" in component;
  }
  /**
   * Check if component belongs to body
   */
  isBodyComponent(component) {
    return "TITLE" in component || "PARAGRAPH" in component || "TABLE" in component || "OTP" in component || "PAYMENT" in component || "VOUCHER" in component || "RATING" in component;
  }
  /**
   * Check if component belongs to footer
   */
  isFooterComponent(component) {
    return "BUTTONS" in component;
  }
  /**
   * Validate custom template layout (type 1)
   */
  validateCustomTemplateLayout(header, body, footer) {
    if (header.length !== 1) {
      throw new ZaloSDKError("ZNS t\xF9y ch\u1EC9nh ph\u1EA3i c\xF3 \u0111\xFAng 1 component trong header (Logo ho\u1EB7c Image)", 400);
    }
    const titles = body.filter((c) => "TITLE" in c);
    const paragraphs = body.filter((c) => "PARAGRAPH" in c);
    const tables = body.filter((c) => "TABLE" in c);
    if (titles.length !== 1) {
      throw new ZaloSDKError("ZNS t\xF9y ch\u1EC9nh ph\u1EA3i c\xF3 \u0111\xFAng 1 component TITLE", 400);
    }
    if (paragraphs.length > 4) {
      throw new ZaloSDKError("ZNS t\xF9y ch\u1EC9nh kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 4 component PARAGRAPH", 400);
    }
    if (tables.length > 1) {
      throw new ZaloSDKError("ZNS t\xF9y ch\u1EC9nh kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 1 component TABLE", 400);
    }
    if (footer.length > 2) {
      throw new ZaloSDKError("ZNS t\xF9y ch\u1EC9nh kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 2 button", 400);
    }
    const hasImage = header.some((c) => "IMAGES" in c);
    if (hasImage && footer.length === 0) {
      throw new ZaloSDKError("ZNS c\xF3 component Image ph\u1EA3i c\xF3 \xEDt nh\u1EA5t 1 button", 400);
    }
  }
  /**
   * Validate auth template layout (type 2)
   */
  validateAuthTemplateLayout(header, body, footer) {
    if (header.length !== 1 || !("LOGO" in header[0])) {
      throw new ZaloSDKError("ZNS x\xE1c th\u1EF1c ph\u1EA3i c\xF3 \u0111\xFAng 1 component LOGO trong header", 400);
    }
    const otps = body.filter((c) => "OTP" in c);
    const paragraphs = body.filter((c) => "PARAGRAPH" in c);
    if (otps.length !== 1) {
      throw new ZaloSDKError("ZNS x\xE1c th\u1EF1c ph\u1EA3i c\xF3 \u0111\xFAng 1 component OTP", 400);
    }
    if (paragraphs.length !== 1) {
      throw new ZaloSDKError("ZNS x\xE1c th\u1EF1c ph\u1EA3i c\xF3 \u0111\xFAng 1 component PARAGRAPH", 400);
    }
    if (footer.length > 0) {
      throw new ZaloSDKError("ZNS x\xE1c th\u1EF1c kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 component trong footer", 400);
    }
  }
  /**
   * Validate payment template layout (type 3)
   */
  validatePaymentTemplateLayout(header, body, footer) {
    if (header.length !== 1 || !("LOGO" in header[0])) {
      throw new ZaloSDKError("ZNS y\xEAu c\u1EA7u thanh to\xE1n ph\u1EA3i c\xF3 \u0111\xFAng 1 component LOGO trong header", 400);
    }
    const titles = body.filter((c) => "TITLE" in c);
    const paragraphs = body.filter((c) => "PARAGRAPH" in c);
    const tables = body.filter((c) => "TABLE" in c);
    const payments = body.filter((c) => "PAYMENT" in c);
    if (titles.length !== 1) {
      throw new ZaloSDKError("ZNS y\xEAu c\u1EA7u thanh to\xE1n ph\u1EA3i c\xF3 \u0111\xFAng 1 component TITLE", 400);
    }
    if (paragraphs.length > 4) {
      throw new ZaloSDKError("ZNS y\xEAu c\u1EA7u thanh to\xE1n kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 4 component PARAGRAPH", 400);
    }
    if (tables.length > 1) {
      throw new ZaloSDKError("ZNS y\xEAu c\u1EA7u thanh to\xE1n kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 1 component TABLE", 400);
    }
    if (payments.length !== 1) {
      throw new ZaloSDKError("ZNS y\xEAu c\u1EA7u thanh to\xE1n ph\u1EA3i c\xF3 \u0111\xFAng 1 component PAYMENT", 400);
    }
    if (footer.length > 2) {
      throw new ZaloSDKError("ZNS y\xEAu c\u1EA7u thanh to\xE1n kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 2 button", 400);
    }
  }
  /**
   * Validate voucher template layout (type 4)
   */
  validateVoucherTemplateLayout(header, body, footer) {
    if (header.length !== 1) {
      throw new ZaloSDKError("ZNS voucher ph\u1EA3i c\xF3 \u0111\xFAng 1 component trong header (Logo ho\u1EB7c Image)", 400);
    }
    const titles = body.filter((c) => "TITLE" in c);
    const paragraphs = body.filter((c) => "PARAGRAPH" in c);
    const tables = body.filter((c) => "TABLE" in c);
    const vouchers = body.filter((c) => "VOUCHER" in c);
    if (titles.length !== 1) {
      throw new ZaloSDKError("ZNS voucher ph\u1EA3i c\xF3 \u0111\xFAng 1 component TITLE", 400);
    }
    if (paragraphs.length > 4) {
      throw new ZaloSDKError("ZNS voucher kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 4 component PARAGRAPH", 400);
    }
    if (tables.length > 1) {
      throw new ZaloSDKError("ZNS voucher kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 1 component TABLE", 400);
    }
    if (vouchers.length !== 1) {
      throw new ZaloSDKError("ZNS voucher ph\u1EA3i c\xF3 \u0111\xFAng 1 component VOUCHER", 400);
    }
    if (footer.length > 2) {
      throw new ZaloSDKError("ZNS voucher kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 2 button", 400);
    }
    const hasImage = header.some((c) => "IMAGES" in c);
    if (hasImage && footer.length === 0) {
      throw new ZaloSDKError("ZNS voucher c\xF3 component Image ph\u1EA3i c\xF3 \xEDt nh\u1EA5t 1 button", 400);
    }
  }
  /**
   * Validate rating template layout (type 5)
   */
  validateRatingTemplateLayout(header, body, footer) {
    if (header.length !== 1 || !("LOGO" in header[0])) {
      throw new ZaloSDKError("ZNS \u0111\xE1nh gi\xE1 d\u1ECBch v\u1EE5 ph\u1EA3i c\xF3 \u0111\xFAng 1 component LOGO trong header", 400);
    }
    const titles = body.filter((c) => "TITLE" in c);
    const paragraphs = body.filter((c) => "PARAGRAPH" in c);
    const ratings = body.filter((c) => "RATING" in c);
    if (titles.length !== 1) {
      throw new ZaloSDKError("ZNS \u0111\xE1nh gi\xE1 d\u1ECBch v\u1EE5 ph\u1EA3i c\xF3 \u0111\xFAng 1 component TITLE", 400);
    }
    if (paragraphs.length > 1) {
      throw new ZaloSDKError("ZNS \u0111\xE1nh gi\xE1 d\u1ECBch v\u1EE5 kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 1 component PARAGRAPH", 400);
    }
    if (ratings.length !== 1) {
      throw new ZaloSDKError("ZNS \u0111\xE1nh gi\xE1 d\u1ECBch v\u1EE5 ph\u1EA3i c\xF3 \u0111\xFAng 1 component RATING", 400);
    }
    if (footer.length > 2) {
      throw new ZaloSDKError("ZNS \u0111\xE1nh gi\xE1 d\u1ECBch v\u1EE5 kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 2 button", 400);
    }
  }
  /**
   * Validate individual component content
   */
  validateComponent(component) {
    if ("TITLE" in component) {
      this.validateTitleComponent(component.TITLE);
    }
    if ("PARAGRAPH" in component) {
      this.validateParagraphComponent(component.PARAGRAPH);
    }
    if ("OTP" in component) {
      this.validateOTPComponent(component.OTP);
    }
    if ("TABLE" in component) {
      this.validateTableComponent(component.TABLE);
    }
    if ("LOGO" in component) {
      this.validateLogoComponent(component.LOGO);
    }
    if ("IMAGES" in component) {
      this.validateImagesComponent(component.IMAGES);
    }
    if ("BUTTONS" in component) {
      this.validateButtonsComponent(component.BUTTONS);
    }
    if ("PAYMENT" in component) {
      this.validatePaymentComponent(component.PAYMENT);
    }
    if ("VOUCHER" in component) {
      this.validateVoucherComponent(component.VOUCHER);
    }
    if ("RATING" in component) {
      this.validateRatingComponent(component.RATING);
    }
  }
  /**
   * Validate TITLE component
   */
  validateTitleComponent(title) {
    if (!title.value || typeof title.value !== "string") {
      throw new ZaloSDKError("TITLE component ph\u1EA3i c\xF3 field value ki\u1EC3u string", 400);
    }
    if (title.value.length < 9 || title.value.length > 65) {
      throw new ZaloSDKError("TITLE value ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 9 \u0111\u1EBFn 65 k\xFD t\u1EF1", 400);
    }
    const paramCount = (title.value.match(/\{\{[^}]+\}\}/g) || []).length;
    if (paramCount > 4) {
      throw new ZaloSDKError("TITLE component kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 4 params", 400);
    }
  }
  /**
   * Validate PARAGRAPH component
   */
  validateParagraphComponent(paragraph) {
    if (!paragraph.value || typeof paragraph.value !== "string") {
      throw new ZaloSDKError("PARAGRAPH component ph\u1EA3i c\xF3 field value ki\u1EC3u string", 400);
    }
    if (paragraph.value.length < 9 || paragraph.value.length > 400) {
      throw new ZaloSDKError("PARAGRAPH value ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 9 \u0111\u1EBFn 400 k\xFD t\u1EF1", 400);
    }
    const paramCount = (paragraph.value.match(/\{\{[^}]+\}\}/g) || []).length;
    if (paramCount > 10) {
      throw new ZaloSDKError("PARAGRAPH component kh\xF4ng \u0111\u01B0\u1EE3c c\xF3 qu\xE1 10 params", 400);
    }
  }
  /**
   * Validate OTP component
   */
  validateOTPComponent(otp) {
    if (!otp.value || typeof otp.value !== "string") {
      throw new ZaloSDKError("OTP component ph\u1EA3i c\xF3 field value ki\u1EC3u string", 400);
    }
    if (otp.value.length < 1 || otp.value.length > 10) {
      throw new ZaloSDKError("OTP value ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 10 k\xFD t\u1EF1", 400);
    }
  }
  /**
   * Validate TABLE component
   */
  validateTableComponent(table) {
    if (!table.rows || !Array.isArray(table.rows)) {
      throw new ZaloSDKError("TABLE component ph\u1EA3i c\xF3 field rows ki\u1EC3u array", 400);
    }
    if (table.rows.length < 2 || table.rows.length > 8) {
      throw new ZaloSDKError("TABLE rows ph\u1EA3i c\xF3 t\u1EEB 2 \u0111\u1EBFn 8 objects", 400);
    }
    table.rows.forEach((row, index) => {
      if (!row.title || typeof row.title !== "string") {
        throw new ZaloSDKError(`TABLE row ${index + 1} ph\u1EA3i c\xF3 field title ki\u1EC3u string`, 400);
      }
      if (row.title.length < 3 || row.title.length > 36) {
        throw new ZaloSDKError(`TABLE row ${index + 1} title ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 3 \u0111\u1EBFn 36 k\xFD t\u1EF1`, 400);
      }
      if (!row.value || typeof row.value !== "string") {
        throw new ZaloSDKError(`TABLE row ${index + 1} ph\u1EA3i c\xF3 field value ki\u1EC3u string`, 400);
      }
      if (row.value.length < 3 || row.value.length > 90) {
        throw new ZaloSDKError(`TABLE row ${index + 1} value ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 3 \u0111\u1EBFn 90 k\xFD t\u1EF1`, 400);
      }
      if (row.row_type !== void 0 && ![0, 1, 2, 3, 4, 5].includes(row.row_type)) {
        throw new ZaloSDKError(`TABLE row ${index + 1} row_type ph\u1EA3i l\xE0 0, 1, 2, 3, 4 ho\u1EB7c 5`, 400);
      }
    });
  }
  /**
   * Validate LOGO component
   */
  validateLogoComponent(logo) {
    if (!logo.light || !logo.dark) {
      throw new ZaloSDKError("LOGO component ph\u1EA3i c\xF3 c\u1EA3 light v\xE0 dark attachment", 400);
    }
    this.validateAttachment(logo.light, "LOGO light");
    this.validateAttachment(logo.dark, "LOGO dark");
  }
  /**
   * Validate IMAGES component
   */
  validateImagesComponent(images) {
    if (!images.items || !Array.isArray(images.items)) {
      throw new ZaloSDKError("IMAGES component ph\u1EA3i c\xF3 field items ki\u1EC3u array", 400);
    }
    if (images.items.length < 1 || images.items.length > 3) {
      throw new ZaloSDKError("IMAGES items ph\u1EA3i c\xF3 t\u1EEB 1 \u0111\u1EBFn 3 attachments", 400);
    }
    images.items.forEach((item, index) => {
      this.validateAttachment(item, `IMAGES item ${index + 1}`);
    });
  }
  /**
   * Validate BUTTONS component
   */
  validateButtonsComponent(buttons) {
    if (!buttons.items || !Array.isArray(buttons.items)) {
      throw new ZaloSDKError("BUTTONS component ph\u1EA3i c\xF3 field items ki\u1EC3u array", 400);
    }
    if (buttons.items.length < 1 || buttons.items.length > 2) {
      throw new ZaloSDKError("BUTTONS items ph\u1EA3i c\xF3 t\u1EEB 1 \u0111\u1EBFn 2 objects", 400);
    }
    buttons.items.forEach((button, index) => {
      if (!button.type || ![1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].includes(button.type)) {
        throw new ZaloSDKError(`BUTTON ${index + 1} type ph\u1EA3i l\xE0 1-12`, 400);
      }
      if (!button.title || typeof button.title !== "string") {
        throw new ZaloSDKError(`BUTTON ${index + 1} ph\u1EA3i c\xF3 field title ki\u1EC3u string`, 400);
      }
      if (button.title.length < 5 || button.title.length > 30) {
        throw new ZaloSDKError(`BUTTON ${index + 1} title ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 5 \u0111\u1EBFn 30 k\xFD t\u1EF1`, 400);
      }
      if (!button.content || typeof button.content !== "string") {
        throw new ZaloSDKError(`BUTTON ${index + 1} ph\u1EA3i c\xF3 field content ki\u1EC3u string`, 400);
      }
    });
  }
  /**
   * Validate PAYMENT component
   */
  validatePaymentComponent(payment) {
    if (!payment.bank_code || typeof payment.bank_code !== "string") {
      throw new ZaloSDKError("PAYMENT component ph\u1EA3i c\xF3 field bank_code ki\u1EC3u string", 400);
    }
    if (!payment.account_name || typeof payment.account_name !== "string") {
      throw new ZaloSDKError("PAYMENT component ph\u1EA3i c\xF3 field account_name ki\u1EC3u string", 400);
    }
    if (payment.account_name.length < 1 || payment.account_name.length > 100) {
      throw new ZaloSDKError("PAYMENT account_name ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 100 k\xFD t\u1EF1", 400);
    }
    if (!payment.bank_account || typeof payment.bank_account !== "string") {
      throw new ZaloSDKError("PAYMENT component ph\u1EA3i c\xF3 field bank_account ki\u1EC3u string", 400);
    }
    if (payment.bank_account.length < 1 || payment.bank_account.length > 100) {
      throw new ZaloSDKError("PAYMENT bank_account ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 100 k\xFD t\u1EF1", 400);
    }
    if (!payment.amount) {
      throw new ZaloSDKError("PAYMENT component ph\u1EA3i c\xF3 field amount", 400);
    }
    if (!payment.amount.includes("{{")) {
      const numericAmount = Number(payment.amount.replace(/[^0-9]/g, ""));
      if (!isNaN(numericAmount) && numericAmount > 0) {
        if (numericAmount < 2e3 || numericAmount > 5e8) {
          throw new ZaloSDKError("PAYMENT amount c\u1ED1 \u0111\u1ECBnh ph\u1EA3i n\u1EB1m trong kho\u1EA3ng t\u1EEB 2,000 VN\u0110 \u0111\u1EBFn 500,000,000 VN\u0110", 400);
        }
      }
    }
    if (payment.note && (payment.note.length < 1 || payment.note.length > 90)) {
      throw new ZaloSDKError("PAYMENT note ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 90 k\xFD t\u1EF1", 400);
    }
  }
  /**
   * Validate VOUCHER component
   */
  validateVoucherComponent(voucher) {
    if (!voucher.name || typeof voucher.name !== "string") {
      throw new ZaloSDKError("VOUCHER component ph\u1EA3i c\xF3 field name ki\u1EC3u string", 400);
    }
    if (voucher.name.length < 1 || voucher.name.length > 30) {
      throw new ZaloSDKError("VOUCHER name ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 30 k\xFD t\u1EF1", 400);
    }
    if (!voucher.condition || typeof voucher.condition !== "string") {
      throw new ZaloSDKError("VOUCHER component ph\u1EA3i c\xF3 field condition ki\u1EC3u string", 400);
    }
    if (voucher.condition.length < 1 || voucher.condition.length > 40) {
      throw new ZaloSDKError("VOUCHER condition ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 40 k\xFD t\u1EF1", 400);
    }
    if (!voucher.end_date || typeof voucher.end_date !== "string") {
      throw new ZaloSDKError("VOUCHER component ph\u1EA3i c\xF3 field end_date ki\u1EC3u string", 400);
    }
    if (!voucher.voucher_code || typeof voucher.voucher_code !== "string") {
      throw new ZaloSDKError("VOUCHER component ph\u1EA3i c\xF3 field voucher_code ki\u1EC3u string", 400);
    }
    if (voucher.voucher_code.length < 1 || voucher.voucher_code.length > 25) {
      throw new ZaloSDKError("VOUCHER voucher_code ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 25 k\xFD t\u1EF1", 400);
    }
  }
  /**
   * Validate RATING component
   */
  validateRatingComponent(rating) {
    if (!rating.items || !Array.isArray(rating.items)) {
      throw new ZaloSDKError("RATING component ph\u1EA3i c\xF3 field items ki\u1EC3u array", 400);
    }
    if (rating.items.length !== 5) {
      throw new ZaloSDKError("RATING items ph\u1EA3i c\xF3 \u0111\xFAng 5 objects", 400);
    }
    rating.items.forEach((item, index) => {
      const starValue = index + 1;
      if (item.star !== starValue) {
        throw new ZaloSDKError(`RATING item ${index + 1} ph\u1EA3i c\xF3 star = ${starValue}`, 400);
      }
      if (!item.title || typeof item.title !== "string") {
        throw new ZaloSDKError(`RATING item ${index + 1} ph\u1EA3i c\xF3 field title ki\u1EC3u string`, 400);
      }
      if (item.title.length < 1 || item.title.length > 50) {
        throw new ZaloSDKError(`RATING item ${index + 1} title ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 50 k\xFD t\u1EF1`, 400);
      }
      if (item.question && (item.question.length < 1 || item.question.length > 100)) {
        throw new ZaloSDKError(`RATING item ${index + 1} question ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 100 k\xFD t\u1EF1`, 400);
      }
      if (item.answers && Array.isArray(item.answers)) {
        if (item.answers.length < 1 || item.answers.length > 5) {
          throw new ZaloSDKError(`RATING item ${index + 1} answers ph\u1EA3i c\xF3 t\u1EEB 1 \u0111\u1EBFn 5 c\xE2u tr\u1EA3 l\u1EDDi`, 400);
        }
        item.answers.forEach((answer, answerIndex) => {
          if (!answer || answer.length < 1 || answer.length > 50) {
            throw new ZaloSDKError(`RATING item ${index + 1} answer ${answerIndex + 1} ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 50 k\xFD t\u1EF1`, 400);
          }
        });
      }
      if (!item.thanks || typeof item.thanks !== "string") {
        throw new ZaloSDKError(`RATING item ${index + 1} ph\u1EA3i c\xF3 field thanks ki\u1EC3u string`, 400);
      }
      if (item.thanks.length < 1 || item.thanks.length > 100) {
        throw new ZaloSDKError(`RATING item ${index + 1} thanks ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 100 k\xFD t\u1EF1`, 400);
      }
      if (!item.description || typeof item.description !== "string") {
        throw new ZaloSDKError(`RATING item ${index + 1} ph\u1EA3i c\xF3 field description ki\u1EC3u string`, 400);
      }
      if (item.description.length < 1 || item.description.length > 200) {
        throw new ZaloSDKError(`RATING item ${index + 1} description ph\u1EA3i c\xF3 \u0111\u1ED9 d\xE0i t\u1EEB 1 \u0111\u1EBFn 200 k\xFD t\u1EF1`, 400);
      }
    });
  }
  /**
   * Validate attachment object
   */
  validateAttachment(attachment, context) {
    if (!attachment.type || !attachment.media_id) {
      throw new ZaloSDKError(`${context} ph\u1EA3i c\xF3 type v\xE0 media_id`, 400);
    }
    if (attachment.type !== "IMAGE") {
      throw new ZaloSDKError(`${context} type ph\u1EA3i l\xE0 "IMAGE"`, 400);
    }
  }
  /**
   * Validate params array
   */
  validateParams(params, layout) {
    const paramTypeConstraints = {
      "1": 30,
      // Tên khách hàng
      "2": 15,
      // Số điện thoại
      "3": 200,
      // Địa chỉ
      "4": 30,
      // Mã số
      "5": 30,
      // Nhãn tùy chỉnh
      "6": 30,
      // Trạng thái giao dịch
      "7": 50,
      // Thông tin liên hệ
      "8": 5,
      // Giới tính / Danh xưng
      "9": 200,
      // Tên sản phẩm / Thương hiệu
      "10": 20,
      // Số lượng / Số tiền
      "11": 20,
      // Thời gian
      "12": 10,
      // OTP
      "13": 200,
      // URL
      "14": 12,
      // Tiền tệ (VNĐ)
      "15": 90
      // Bank transfer note
    };
    const layoutString = JSON.stringify(layout);
    const usedParams = /* @__PURE__ */ new Set();
    const paramMatches = layoutString.match(/\{\{([^}]+)\}\}/g);
    if (paramMatches) {
      paramMatches.forEach((match) => {
        const paramName = match.replace(/[{}]/g, "");
        usedParams.add(paramName);
      });
    }
    params.forEach((param, index) => {
      if (!param.name || typeof param.name !== "string") {
        throw new ZaloSDKError(`Param ${index + 1} ph\u1EA3i c\xF3 field name ki\u1EC3u string`, 400);
      }
      if (!param.type || typeof param.type !== "string") {
        throw new ZaloSDKError(`Param ${index + 1} ph\u1EA3i c\xF3 field type ki\u1EC3u string`, 400);
      }
      if (!Object.keys(paramTypeConstraints).includes(param.type)) {
        throw new ZaloSDKError(`Param ${index + 1} type ph\u1EA3i l\xE0 1-15`, 400);
      }
      if (!param.sample_value || typeof param.sample_value !== "string") {
        throw new ZaloSDKError(`Param ${index + 1} ph\u1EA3i c\xF3 field sample_value ki\u1EC3u string`, 400);
      }
      const maxLength = paramTypeConstraints[param.type];
      if (param.sample_value.length > maxLength) {
        throw new ZaloSDKError(`Param ${index + 1} sample_value kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 ${maxLength} k\xFD t\u1EF1 cho type ${param.type}`, 400);
      }
      usedParams.delete(param.name);
    });
    if (usedParams.size > 0) {
      throw new ZaloSDKError(`C\xE1c param sau \u0111\u01B0\u1EE3c s\u1EED d\u1EE5ng trong layout nh\u01B0ng ch\u01B0a \u0111\u01B0\u1EE3c \u0111\u1ECBnh ngh\u0129a: ${Array.from(usedParams).join(", ")}`, 400);
    }
  }
  /**
   * Validate yêu cầu tham số định danh theo Tag (theo quy định kiểm duyệt của Zalo ZNS):
   * - Tag 1 (GIAO DỊCH) & Tag 2 (CHĂM SÓC KHÁCH HÀNG):
   *   BẮT BUỘC có tên khách hàng (type "1") và ít nhất 1 tham số định danh giao dịch (type "4" - Mã số,
   *   "6" - Trạng thái GD, "10" - SL/Số tiền, "11" - Thời gian, "14" - Tiền tệ).
   *   Trường hợp không có tên khách hàng do bất khả kháng: Bắt buộc có tổng cộng ít nhất 3 tham số định danh.
   */
  validateTagParametersRequirement(tag, params) {
    if (tag === "1" || tag === "2") {
      const hasCustomerName = params.some((p) => p.type === "1");
      const identifierParamTypes = ["4", "6", "10", "11", "14", "15"];
      const identifierParamsCount = params.filter((p) => identifierParamTypes.includes(p.type)).length;
      if (hasCustomerName) {
        if (identifierParamsCount < 1) {
          throw new ZaloSDKError(
            `Theo quy \u0111\u1ECBnh x\xE9t duy\u1EC7t Zalo ZNS cho Tag ${tag === "1" ? "1 (Giao d\u1ECBch)" : "2 (Ch\u0103m s\xF3c kh\xE1ch h\xE0ng)"}: Template b\u1EAFt bu\u1ED9c ph\u1EA3i c\xF3 \xEDt nh\u1EA5t 1 tham s\u1ED1 \u0111\u1ECBnh danh/x\xE1c \u0111\u1ECBnh giao d\u1ECBch (nh\u01B0 m\xE3 \u0111\u01A1n h\xE0ng, ng\xE0y GD, s\u1ED1 ti\u1EC1n, tr\u1EA1ng th\xE1i...).`,
            400
          );
        }
      } else {
        if (identifierParamsCount < 3) {
          throw new ZaloSDKError(
            `Theo quy \u0111\u1ECBnh x\xE9t duy\u1EC7t Zalo ZNS cho Tag ${tag === "1" ? "1 (Giao d\u1ECBch)" : "2 (Ch\u0103m s\xF3c kh\xE1ch h\xE0ng)"}: Template n\u1EBFu kh\xF4ng c\xF3 tham s\u1ED1 T\xEAn kh\xE1ch h\xE0ng (type 1) th\xEC b\u1EAFt bu\u1ED9c ph\u1EA3i c\xF3 \xEDt nh\u1EA5t 3 tham s\u1ED1 \u0111\u1ECBnh danh giao d\u1ECBch ph\xF9 h\u1EE3p.`,
            400
          );
        }
      }
    }
  }
  /**
   * Validate quy định nút bấm (CTA) cho Tag 3 (HẬU MÃI / PROMOTION):
   * - Nếu có nút gọi điện thoại (type 2 - PHONE): Phải là đầu số tổng đài 1800 hoặc 1900.
   *   Trường hợp dùng số khác thì bắt buộc phải có giải trình trong trường note.
   */
  validatePromotionButtons(layout, note) {
    let buttonsList = [];
    if (Array.isArray(layout)) {
      layout.forEach((c) => {
        if ("BUTTONS" in c && c.BUTTONS?.items) {
          buttonsList.push(...c.BUTTONS.items);
        }
      });
    } else if (layout && typeof layout === "object" && layout.footer?.components) {
      layout.footer.components.forEach((c) => {
        if ("BUTTONS" in c && c.BUTTONS?.items) {
          buttonsList.push(...c.BUTTONS.items);
        }
      });
    }
    const phoneButtons = buttonsList.filter((b) => b.type === 2);
    for (const pb of phoneButtons) {
      const cleanPhone = (pb.content || "").replace(/[\s.-]/g, "");
      const isTollFree = cleanPhone.startsWith("1800") || cleanPhone.startsWith("1900");
      if (!isTollFree) {
        if (!note || !note.trim()) {
          throw new ZaloSDKError(
            `\u0110\u1ED1i v\u1EDBi Tag 3 (H\u1EADu m\xE3i), n\xFAt g\u1ECDi Hotline '${pb.content}' kh\xF4ng thu\u1ED9c \u0111\u1EA7u s\u1ED1 t\u1ED5ng \u0111\xE0i 1800/1900. Theo quy chu\u1EA9n Zalo ZNS, b\u1EA1n b\u1EAFt bu\u1ED9c ph\u1EA3i cung c\u1EA5p link/th\xF4ng tin ch\u1EE9ng minh s\u1ED1 hotline ch\xEDnh th\u1EE9c c\u1EE7a doanh nghi\u1EC7p v\xE0o tr\u01B0\u1EDDng 'note'.`,
            400
          );
        }
      }
    }
  }
  handleZNSError(error, defaultMessage) {
    if (error.response?.data) {
      const errorData = error.response.data;
      return new ZaloSDKError(
        `${defaultMessage}: ${errorData.message || errorData.error || "Unknown error"}`,
        errorData.error || -1,
        errorData
      );
    }
    return new ZaloSDKError(`${defaultMessage}: ${error.message || "Unknown error"}`, -1, error);
  }
};

// src/services/group-message.service.ts
var GroupMessageService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      // Group message endpoints
      message: {
        group: "https://openapi.zalo.me/v3.0/oa/group/message"
      },
      // Group management endpoints
      group: {
        getInfo: "https://openapi.zalo.me/v3.0/oa/group/getinfo",
        getMembers: "https://openapi.zalo.me/v3.0/oa/group/getmembers"
      }
    };
  }
  /**
   * Send text message to group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param message Text message content
   * @returns Send result
   */
  async sendTextMessage(accessToken, groupId, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.group,
        accessToken,
        {
          recipient: {
            group_id: groupId
          },
          message: {
            text: message.text
          }
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupMessageError(
        error,
        "Failed to send text message to group"
      );
    }
  }
  /**
   * Send image message to group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param message Image message content
   * @returns Send result
   */
  async sendImageMessage(accessToken, groupId, message) {
    try {
      if (!message.imageUrl && !message.attachmentId) {
        throw new Error("Either imageUrl or attachmentId must be provided");
      }
      if (message.caption && message.caption.length > 2e3) {
        throw new Error("Caption cannot exceed 2000 characters");
      }
      const attachmentPayload = {
        type: "template",
        payload: {
          template_type: "media",
          elements: [
            {
              media_type: "image",
              ...message.attachmentId ? { attachment_id: message.attachmentId } : { url: message.imageUrl }
            }
          ]
        }
      };
      const messagePayload = {
        attachment: attachmentPayload
      };
      if (message.caption) {
        messagePayload.text = message.caption;
      }
      const response = await this.client.apiPost(
        this.endpoints.message.group,
        accessToken,
        {
          recipient: {
            group_id: groupId
          },
          message: messagePayload
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupMessageError(
        error,
        "Failed to send image message to group"
      );
    }
  }
  /**
   * Send file message to group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param message File message content
   * @returns Send result
   */
  async sendFileMessage(accessToken, groupId, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.group,
        accessToken,
        {
          recipient: {
            group_id: groupId
          },
          message: {
            attachment: {
              type: "file",
              payload: {
                token: message.fileToken
              }
            }
          }
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupMessageError(
        error,
        "Failed to send file message to group"
      );
    }
  }
  /**
   * Send sticker message to group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param message Sticker message content
   * @returns Send result
   */
  async sendStickerMessage(accessToken, groupId, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.group,
        accessToken,
        {
          recipient: {
            group_id: groupId
          },
          message: {
            attachment: {
              type: "template",
              payload: {
                template_type: "media",
                elements: [
                  {
                    media_type: "sticker",
                    attachment_id: message.stickerId
                  }
                ]
              }
            }
          }
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupMessageError(
        error,
        "Failed to send sticker message to group"
      );
    }
  }
  /**
   * Send mention message to group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param message Mention message content
   * @returns Send result
   */
  async sendMentionMessage(accessToken, groupId, message) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.message.group,
        accessToken,
        {
          recipient: {
            group_id: groupId
          },
          message: {
            text: message.text,
            mention: message.mentions
          }
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupMessageError(
        error,
        "Failed to send mention message to group"
      );
    }
  }
  /**
   * Get group information
   * @param accessToken OA access token
   * @param groupId Group ID
   * @returns Group information
   */
  async getGroupInfo(accessToken, groupId) {
    try {
      const response = await this.client.apiGet(
        this.endpoints.group.getInfo,
        accessToken,
        {
          group_id: groupId
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupMessageError(
        error,
        "Failed to get group information"
      );
    }
  }
  /**
   * Get group members
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param offset Offset for pagination
   * @param count Number of members to retrieve
   * @returns Group members list
   */
  async getGroupMembers(accessToken, groupId, offset = 0, count = 50) {
    try {
      const response = await this.client.apiGet(this.endpoints.group.getMembers, accessToken, {
        group_id: groupId,
        offset,
        count
      });
      return response;
    } catch (error) {
      throw this.handleGroupMessageError(error, "Failed to get group members");
    }
  }
  /**
   * Gửi danh sách tin nhắn tới 1 group với delay tùy chỉnh
   * Hỗ trợ tất cả các loại tin nhắn: text, image, file, sticker, mention
   *
   * @param request Thông tin request gửi danh sách tin nhắn
   * @returns Kết quả gửi từng tin nhắn
   */
  async sendMessageListToGroup(request) {
    const startTime = Date.now();
    const messageResults = [];
    let successfulMessages = 0;
    let failedMessages = 0;
    try {
      if (!request.accessToken || request.accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.groupId || request.groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.messages || request.messages.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      for (let i = 0; i < request.messages.length; i++) {
        const message = request.messages[i];
        const messageStartTime = Date.now();
        if (request.onProgress) {
          request.onProgress({
            groupId: request.groupId,
            messageIndex: i,
            totalMessages: request.messages.length,
            messageType: message.type,
            status: "started",
            startTime: messageStartTime
          });
        }
        const messageResult = {
          messageIndex: i,
          messageType: message.type,
          success: false,
          startTime: messageStartTime,
          endTime: 0,
          duration: 0
        };
        try {
          let result;
          switch (message.type) {
            case "text":
              if (!message.text) {
                throw new Error("Text kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng cho text message");
              }
              result = await this.sendTextMessage(
                request.accessToken,
                request.groupId,
                { type: "text", text: message.text }
              );
              break;
            case "image":
              if (!message.imageUrl && !message.attachmentId) {
                throw new Error("imageUrl ho\u1EB7c attachmentId l\xE0 b\u1EAFt bu\u1ED9c cho image message");
              }
              result = await this.sendImageMessage(
                request.accessToken,
                request.groupId,
                {
                  type: "image",
                  imageUrl: message.imageUrl,
                  attachmentId: message.attachmentId,
                  caption: message.caption
                }
              );
              break;
            case "file":
              if (!message.fileToken) {
                throw new Error("fileToken l\xE0 b\u1EAFt bu\u1ED9c cho file message");
              }
              result = await this.sendFileMessage(
                request.accessToken,
                request.groupId,
                {
                  type: "file",
                  fileToken: message.fileToken,
                  fileName: message.fileName
                }
              );
              break;
            case "sticker":
              if (!message.stickerId) {
                throw new Error("stickerId l\xE0 b\u1EAFt bu\u1ED9c cho sticker message");
              }
              result = await this.sendStickerMessage(
                request.accessToken,
                request.groupId,
                {
                  type: "sticker",
                  stickerId: message.stickerId
                }
              );
              break;
            case "mention":
              if (!message.text || !message.mentions) {
                throw new Error("text v\xE0 mentions l\xE0 b\u1EAFt bu\u1ED9c cho mention message");
              }
              result = await this.sendMentionMessage(
                request.accessToken,
                request.groupId,
                {
                  type: "mention",
                  text: message.text,
                  mentions: message.mentions
                }
              );
              break;
            default:
              throw new Error(`Lo\u1EA1i tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c h\u1ED7 tr\u1EE3: ${message.type}`);
          }
          const messageEndTime = Date.now();
          messageResult.success = true;
          messageResult.result = result;
          messageResult.endTime = messageEndTime;
          messageResult.duration = messageEndTime - messageStartTime;
          successfulMessages++;
          if (request.onProgress) {
            request.onProgress({
              groupId: request.groupId,
              messageIndex: i,
              totalMessages: request.messages.length,
              messageType: message.type,
              status: "completed",
              result,
              startTime: messageStartTime,
              endTime: messageEndTime
            });
          }
        } catch (error) {
          const messageEndTime = Date.now();
          const errorMessage = error instanceof Error ? error.message : String(error);
          messageResult.success = false;
          messageResult.error = errorMessage;
          messageResult.endTime = messageEndTime;
          messageResult.duration = messageEndTime - messageStartTime;
          failedMessages++;
          if (request.onProgress) {
            request.onProgress({
              groupId: request.groupId,
              messageIndex: i,
              totalMessages: request.messages.length,
              messageType: message.type,
              status: "failed",
              error: errorMessage,
              startTime: messageStartTime,
              endTime: messageEndTime
            });
          }
        }
        messageResults.push(messageResult);
        if (i < request.messages.length - 1) {
          const delayTime = message.delay ?? request.defaultDelay ?? 0;
          if (delayTime > 0) {
            await this.sleep(delayTime);
          }
        }
      }
      const totalDuration = Date.now() - startTime;
      return {
        groupId: request.groupId,
        totalMessages: request.messages.length,
        successfulMessages,
        failedMessages,
        messageResults,
        totalDuration
      };
    } catch (error) {
      const totalDuration = Date.now() - startTime;
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send message list to group: ${error.message}`,
        -1,
        {
          groupId: request.groupId,
          totalMessages: request.messages?.length || 0,
          successfulMessages,
          failedMessages,
          messageResults,
          totalDuration
        }
      );
    }
  }
  handleGroupMessageError(error, defaultMessage) {
    let errorDetails = "";
    if (error && typeof error === "object" && "response" in error && error.response && typeof error.response === "object" && "data" in error.response) {
      const errorData = error.response.data;
      const errorCode = errorData.error || error.response.status;
      const errorMessage = errorData.message || errorData.error_description || "Unknown error";
      errorDetails = `Error ${errorCode}: ${errorMessage}`;
      if (errorData.error_name) {
        errorDetails += ` (${errorData.error_name})`;
      }
      console.error(`[GroupMessageService] ${defaultMessage}`, {
        errorCode,
        errorMessage,
        errorData,
        url: error.config?.url,
        method: error.config?.method,
        requestData: error.config?.data
      });
      return new Error(`${defaultMessage}: ${errorDetails}`);
    }
    const errorObj = error;
    console.error(`[GroupMessageService] ${defaultMessage}`, {
      message: errorObj.message,
      stack: errorObj.stack,
      url: error.config?.url,
      method: error.config?.method
    });
    return new Error(`${defaultMessage}: ${errorObj.message || "Unknown error"}`);
  }
  /**
   * Gửi danh sách tin nhắn tới nhiều groups với callback tracking
   *
   * @param request Thông tin request gửi danh sách tin nhắn tới nhiều groups
   * @returns Kết quả gửi tin nhắn cho tất cả groups
   */
  async sendMessageListToMultipleGroups(request) {
    const startTime = Date.now();
    const groupResults = [];
    let successfulGroups = 0;
    let failedGroups = 0;
    let totalSuccessfulMessages = 0;
    let totalFailedMessages = 0;
    try {
      if (!request.accessToken || request.accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.groupIds || request.groupIds.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch group IDs kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.messages || request.messages.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const uniqueGroupIds = [...new Set(request.groupIds.filter((id) => id && id.trim().length > 0))];
      if (uniqueGroupIds.length === 0) {
        throw new ZaloSDKError("Kh\xF4ng c\xF3 group ID h\u1EE3p l\u1EC7 n\xE0o", -1);
      }
      for (let i = 0; i < uniqueGroupIds.length; i++) {
        const groupId = uniqueGroupIds[i];
        const groupStartTime = Date.now();
        if (request.onProgress) {
          request.onProgress({
            groupId,
            groupIndex: i,
            totalGroups: uniqueGroupIds.length,
            status: "started",
            startTime: groupStartTime
          });
        }
        const groupResult = {
          groupId,
          groupIndex: i,
          success: false,
          startTime: groupStartTime,
          endTime: 0,
          duration: 0
        };
        try {
          const messageListResult = await this.sendMessageListToGroup({
            accessToken: request.accessToken,
            groupId,
            messages: request.messages,
            defaultDelay: request.defaultDelay
            // Không truyền onProgress để tránh callback lồng nhau
          });
          const groupEndTime = Date.now();
          groupResult.success = true;
          groupResult.messageListResult = messageListResult;
          groupResult.endTime = groupEndTime;
          groupResult.duration = groupEndTime - groupStartTime;
          successfulGroups++;
          totalSuccessfulMessages += messageListResult.successfulMessages;
          totalFailedMessages += messageListResult.failedMessages;
          if (request.onProgress) {
            request.onProgress({
              groupId,
              groupIndex: i,
              totalGroups: uniqueGroupIds.length,
              status: "completed",
              result: messageListResult,
              startTime: groupStartTime,
              endTime: groupEndTime
            });
          }
        } catch (error) {
          const groupEndTime = Date.now();
          const errorMessage = error instanceof Error ? error.message : String(error);
          groupResult.success = false;
          groupResult.error = errorMessage;
          groupResult.endTime = groupEndTime;
          groupResult.duration = groupEndTime - groupStartTime;
          failedGroups++;
          totalFailedMessages += request.messages.length;
          if (request.onProgress) {
            request.onProgress({
              groupId,
              groupIndex: i,
              totalGroups: uniqueGroupIds.length,
              status: "failed",
              error: errorMessage,
              startTime: groupStartTime,
              endTime: groupEndTime
            });
          }
        }
        groupResults.push(groupResult);
        if (i < uniqueGroupIds.length - 1 && request.delayBetweenGroups && request.delayBetweenGroups > 0) {
          await this.sleep(request.delayBetweenGroups);
        }
      }
      const totalDuration = Date.now() - startTime;
      const totalMessages = uniqueGroupIds.length * request.messages.length;
      return {
        totalGroups: uniqueGroupIds.length,
        successfulGroups,
        failedGroups,
        groupResults,
        totalDuration,
        messageStats: {
          totalSuccessfulMessages,
          totalFailedMessages,
          totalMessages
        }
      };
    } catch (error) {
      const totalDuration = Date.now() - startTime;
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send message list to multiple groups: ${error.message}`,
        -1,
        {
          totalGroups: request.groupIds?.length || 0,
          successfulGroups,
          failedGroups,
          groupResults,
          totalDuration,
          messageStats: {
            totalSuccessfulMessages,
            totalFailedMessages,
            totalMessages: (request.groupIds?.length || 0) * (request.messages?.length || 0)
          }
        }
      );
    }
  }
  /**
   * Gửi tin nhắn cá nhân hóa tới nhiều groups - mỗi group có bộ tin nhắn riêng
   *
   * @param request Thông tin request gửi tin nhắn cá nhân hóa tới nhiều groups
   * @returns Kết quả gửi tin nhắn cho tất cả groups
   */
  async sendPersonalizedMessageToMultipleGroups(request) {
    const startTime = Date.now();
    const groupResults = [];
    let successfulGroups = 0;
    let failedGroups = 0;
    let totalSuccessfulMessages = 0;
    let totalFailedMessages = 0;
    let totalMessages = 0;
    try {
      if (!request.accessToken || request.accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.personalizedMessages || request.personalizedMessages.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch tin nh\u1EAFn c\xE1 nh\xE2n h\xF3a kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      for (let i = 0; i < request.personalizedMessages.length; i++) {
        const personalizedMessage = request.personalizedMessages[i];
        if (!personalizedMessage.groupId || personalizedMessage.groupId.trim().length === 0) {
          throw new ZaloSDKError(`Group ID t\u1EA1i index ${i} kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        if (!personalizedMessage.messages || personalizedMessage.messages.length === 0) {
          throw new ZaloSDKError(`Danh s\xE1ch tin nh\u1EAFn cho group ${personalizedMessage.groupId} kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
      }
      const uniquePersonalizedMessages = request.personalizedMessages.filter(
        (message, index, self) => self.findIndex((m) => m.groupId === message.groupId) === index && message.groupId.trim().length > 0
      );
      if (uniquePersonalizedMessages.length === 0) {
        throw new ZaloSDKError("Kh\xF4ng c\xF3 group message h\u1EE3p l\u1EC7 n\xE0o", -1);
      }
      totalMessages = uniquePersonalizedMessages.reduce((sum, pm) => sum + pm.messages.length, 0);
      for (let i = 0; i < uniquePersonalizedMessages.length; i++) {
        const personalizedMessage = uniquePersonalizedMessages[i];
        const groupStartTime = Date.now();
        if (request.onProgress) {
          request.onProgress({
            groupId: personalizedMessage.groupId,
            groupIndex: i,
            totalGroups: uniquePersonalizedMessages.length,
            status: "started",
            startTime: groupStartTime
          });
        }
        const groupResult = {
          groupId: personalizedMessage.groupId,
          groupIndex: i,
          success: false,
          startTime: groupStartTime,
          endTime: 0,
          duration: 0
        };
        try {
          const messageListResult = await this.sendMessageListToGroup({
            accessToken: request.accessToken,
            groupId: personalizedMessage.groupId,
            messages: personalizedMessage.messages,
            defaultDelay: request.defaultDelay
            // Không truyền onProgress để tránh callback lồng nhau
          });
          const groupEndTime = Date.now();
          groupResult.success = true;
          groupResult.messageListResult = messageListResult;
          groupResult.endTime = groupEndTime;
          groupResult.duration = groupEndTime - groupStartTime;
          successfulGroups++;
          totalSuccessfulMessages += messageListResult.successfulMessages;
          totalFailedMessages += messageListResult.failedMessages;
          if (request.onProgress) {
            request.onProgress({
              groupId: personalizedMessage.groupId,
              groupIndex: i,
              totalGroups: uniquePersonalizedMessages.length,
              status: "completed",
              result: messageListResult,
              startTime: groupStartTime,
              endTime: groupEndTime
            });
          }
        } catch (error) {
          const groupEndTime = Date.now();
          const errorMessage = error instanceof Error ? error.message : String(error);
          groupResult.success = false;
          groupResult.error = errorMessage;
          groupResult.endTime = groupEndTime;
          groupResult.duration = groupEndTime - groupStartTime;
          failedGroups++;
          totalFailedMessages += personalizedMessage.messages.length;
          if (request.onProgress) {
            request.onProgress({
              groupId: personalizedMessage.groupId,
              groupIndex: i,
              totalGroups: uniquePersonalizedMessages.length,
              status: "failed",
              error: errorMessage,
              startTime: groupStartTime,
              endTime: groupEndTime
            });
          }
        }
        groupResults.push(groupResult);
        if (i < uniquePersonalizedMessages.length - 1 && request.delayBetweenGroups && request.delayBetweenGroups > 0) {
          await this.sleep(request.delayBetweenGroups);
        }
      }
      const totalDuration = Date.now() - startTime;
      return {
        totalGroups: uniquePersonalizedMessages.length,
        successfulGroups,
        failedGroups,
        groupResults,
        totalDuration,
        messageStats: {
          totalSuccessfulMessages,
          totalFailedMessages,
          totalMessages
        }
      };
    } catch (error) {
      const totalDuration = Date.now() - startTime;
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send personalized messages to multiple groups: ${error.message}`,
        -1,
        {
          totalGroups: request.personalizedMessages?.length || 0,
          successfulGroups,
          failedGroups,
          groupResults,
          totalDuration,
          messageStats: {
            totalSuccessfulMessages,
            totalFailedMessages,
            totalMessages
          }
        }
      );
    }
  }
  /**
   * Utility method để sleep/delay
   * @param ms Thời gian delay tính bằng milliseconds
   */
  sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
};

// src/services/group-management.service.ts
var GroupManagementService = class _GroupManagementService {
  constructor(client, userService) {
    this.client = client;
    this.userService = userService;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      group: {
        create: "https://openapi.zalo.me/v3.0/oa/group/creategroupwithoa",
        get: "https://openapi.zalo.me/v3.0/oa/group/getgroup",
        updateInfo: "https://openapi.zalo.me/v3.0/oa/group/updateinfo",
        updateAsset: "https://openapi.zalo.me/v3.0/oa/group/updateasset",
        invite: "https://openapi.zalo.me/v3.0/oa/group/invite",
        listPendingInvite: "https://openapi.zalo.me/v3.0/oa/group/listpendinginvite",
        acceptPendingInvite: "https://openapi.zalo.me/v3.0/oa/group/acceptpendinginvite",
        rejectPendingInvite: "https://openapi.zalo.me/v3.0/oa/group/rejectpendinginvite",
        removeMembers: "https://openapi.zalo.me/v3.0/oa/group/removemembers",
        getGroupsOfOA: "https://openapi.zalo.me/v3.0/oa/group/getgroupsofoa",
        recent: "https://openapi.zalo.me/v3.0/oa/group/listrecentchat",
        conversation: "https://openapi.zalo.me/v3.0/oa/group/conversation",
        addAdmins: "https://openapi.zalo.me/v3.0/oa/group/addadmins",
        removeAdmins: "https://openapi.zalo.me/v3.0/oa/group/removeadmins",
        conversationByGroupId: (groupId) => `https://openapi.zalo.me/v3.0/oa/group/${groupId}/conversation`,
        members: "https://openapi.zalo.me/v3.0/oa/group/listmember",
        delete: "https://openapi.zalo.me/v3.0/oa/group/delete",
        quota: "https://openapi.zalo.me/v3.0/oa/quota/group"
      }
    };
  }
  /**
   * Create GroupManagementService with UserService for enhanced member details
   * @param client ZaloClient instance
   * @param userService UserService instance for fetching detailed user info
   * @returns GroupManagementService with enhanced capabilities
   */
  static withUserService(client, userService) {
    return new _GroupManagementService(client, userService);
  }
  /**
   * Create basic GroupManagementService without UserService
   * @param client ZaloClient instance
   * @returns GroupManagementService with basic capabilities only
   */
  static basic(client) {
    return new _GroupManagementService(client);
  }
  /**
   * Create new group chat with asset_id
   * @param accessToken OA access token
   * @param groupData Group information to create
   * @returns Created group information
   *
   * API: POST https://openapi.zalo.me/v3.0/oa/group/creategroupwithoa
   */
  async createGroup(accessToken, groupData) {
    try {
      if (!groupData.group_name || groupData.group_name.trim().length === 0) {
        throw new ZaloSDKError("Group name cannot be empty", -1);
      }
      if (groupData.group_name.length > 100) {
        throw new ZaloSDKError("Group name cannot exceed 100 characters", -1);
      }
      if (!groupData.asset_id || groupData.asset_id.trim().length === 0) {
        throw new ZaloSDKError("Asset ID cannot be empty", -1);
      }
      if (groupData.member_user_ids.length > 99) {
        throw new ZaloSDKError(
          "Initial member count cannot exceed 99 people",
          -1
        );
      }
      if (groupData.member_user_ids.length === 0) {
        throw new ZaloSDKError("Member list cannot be empty", -1);
      }
      const requestData = {
        group_name: groupData.group_name.trim(),
        ...groupData.group_description && {
          group_description: groupData.group_description.trim()
        },
        asset_id: groupData.asset_id,
        member_user_ids: groupData.member_user_ids
      };
      const response = await this.client.apiPost(
        this.endpoints.group.create,
        accessToken,
        requestData
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(error, "Failed to create group");
    }
  }
  /**
   * Helper method to extract group data from create response
   * @param response Full API response
   * @returns Group data only
   */
  extractGroupData(response) {
    return response.data;
  }
  /**
   * Helper method to extract group info from update response
   * @param response Full API response
   * @returns Group info only
   */
  extractGroupInfo(response) {
    return response.data.group_info;
  }
  /**
   * Helper method to extract group settings from update response
   * @param response Full API response
   * @returns Group settings only
   */
  extractGroupSettings(response) {
    return response.data.group_setting;
  }
  /**
   * Helper method to extract asset info from update response
   * @param response Full API response
   * @returns Asset info only
   */
  extractAssetInfo(response) {
    return response.data.asset_info;
  }
  async updateGroupAsset(accessToken, groupIdOrUpdateData, assetId) {
    try {
      let requestData;
      if (typeof groupIdOrUpdateData === "string") {
        if (!assetId) {
          throw new ZaloSDKError("Asset ID is required when using separate parameters", -1);
        }
        requestData = {
          group_id: groupIdOrUpdateData,
          asset_id: assetId
        };
      } else {
        requestData = groupIdOrUpdateData;
      }
      if (!requestData.group_id || requestData.group_id.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (!requestData.asset_id || requestData.asset_id.trim().length === 0) {
        throw new ZaloSDKError("Asset ID cannot be empty", -1);
      }
      const response = await this.client.apiPost(
        this.endpoints.group.updateAsset,
        accessToken,
        requestData
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to update group asset"
      );
    }
  }
  /**
   * Get detailed group information
   * @param accessToken OA access token
   * @param groupId Group ID
   * @returns Detailed group information including group_info, asset_info and group_setting
   *
   * API: GET https://openapi.zalo.me/v3.0/oa/group/getgroup
   */
  async getGroupInfo(accessToken, groupId) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.group.get,
        accessToken,
        {
          group_id: groupId.trim()
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get group information"
      );
    }
  }
  /**
   * Update group information
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param updateData Information to update
   * @returns Update result with full group information
   *
   * API: POST https://openapi.zalo.me/v3.0/oa/group/updateinfo
   */
  async updateGroupInfo(accessToken, groupId, updateData) {
    try {
      if (updateData.group_name && updateData.group_name.length > 100) {
        throw new ZaloSDKError("Group name cannot exceed 100 characters", -1);
      }
      if (updateData.group_description && updateData.group_description.length > 500) {
        throw new ZaloSDKError(
          "Group description cannot exceed 500 characters",
          -1
        );
      }
      const requestData = {
        group_id: groupId,
        ...updateData.group_name && {
          group_name: updateData.group_name.trim()
        },
        ...updateData.group_avatar && {
          group_avatar: updateData.group_avatar
        },
        ...updateData.group_description && {
          group_description: updateData.group_description.trim()
        },
        ...updateData.lock_send_msg !== void 0 && {
          lock_send_msg: updateData.lock_send_msg
        },
        ...updateData.join_appr !== void 0 && {
          join_appr: updateData.join_appr
        },
        ...updateData.enable_msg_history !== void 0 && {
          enable_msg_history: updateData.enable_msg_history
        },
        ...updateData.enable_link_join !== void 0 && {
          enable_link_join: updateData.enable_link_join
        }
      };
      const response = await this.client.apiPost(
        this.endpoints.group.updateInfo,
        accessToken,
        requestData
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to update group information"
      );
    }
  }
  /**
   * Update group avatar
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param avatarData New avatar information
   * @returns Update result
   * @deprecated Use updateGroupInfo() with group_avatar field instead
   */
  async updateGroupAvatar(accessToken, groupId, avatarData) {
    try {
      const response = await this.client.apiPost(
        this.endpoints.group.updateInfo,
        accessToken,
        {
          group_id: groupId,
          ...avatarData
        }
      );
      return { success: response.error === 0 };
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to update group avatar"
      );
    }
  }
  async inviteMembers(accessToken, groupId, inviteDataOrUserIds) {
    try {
      let memberUserIds;
      if (Array.isArray(inviteDataOrUserIds)) {
        memberUserIds = inviteDataOrUserIds;
      } else {
        memberUserIds = inviteDataOrUserIds.member_user_ids;
      }
      if (memberUserIds.length === 0) {
        throw new ZaloSDKError("Member list cannot be empty", -1);
      }
      if (memberUserIds.length > 50) {
        throw new ZaloSDKError("Cannot invite more than 50 people at once", -1);
      }
      const requestData = {
        group_id: groupId,
        member_user_ids: memberUserIds
      };
      const response = await this.client.apiPost(
        this.endpoints.group.invite,
        accessToken,
        requestData
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(error, "Failed to invite members");
    }
  }
  /**
   * Get list of pending members
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param offset Offset for pagination (default: 0)
   * @param count Maximum number to return (default: 20, max: 50)
   * @returns List of pending members
   */
  async getPendingMembers(accessToken, groupId, offset = 0, count = 5) {
    try {
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (offset < 0) {
        throw new ZaloSDKError("Offset must be >= 0", -1);
      }
      if (count <= 0 || count > 50) {
        throw new ZaloSDKError("Count must be from 1 to 50", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.group.listPendingInvite,
        accessToken,
        {
          group_id: groupId.trim(),
          offset,
          count
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get pending members"
      );
    }
  }
  /**
   * Accept pending members to group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param memberUserIds List of user IDs to accept
   * @returns Accept result
   */
  async acceptPendingMembers(accessToken, groupId, memberUserIds) {
    try {
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (!memberUserIds || memberUserIds.length === 0) {
        throw new ZaloSDKError("Member user IDs list cannot be empty", -1);
      }
      if (memberUserIds.length > 100) {
        throw new ZaloSDKError(
          "Cannot accept more than 100 members at once",
          -1
        );
      }
      const validUserIds = memberUserIds.filter(
        (id) => id && id.trim().length > 0
      );
      if (validUserIds.length === 0) {
        throw new ZaloSDKError("No valid user IDs found", -1);
      }
      const requestBody = {
        group_id: groupId.trim(),
        member_user_ids: validUserIds.map((id) => id.trim())
      };
      const response = await this.client.apiPost(
        this.endpoints.group.acceptPendingInvite,
        accessToken,
        requestBody
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to accept pending members"
      );
    }
  }
  /**
   * Reject pending members from group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param memberUserIds List of user IDs to reject
   * @returns Reject result
   */
  async rejectPendingMembers(accessToken, groupId, memberUserIds) {
    try {
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (!memberUserIds || memberUserIds.length === 0) {
        throw new ZaloSDKError("Member user IDs list cannot be empty", -1);
      }
      if (memberUserIds.length > 100) {
        throw new ZaloSDKError(
          "Cannot reject more than 100 members at once",
          -1
        );
      }
      const validUserIds = memberUserIds.filter(
        (id) => id && id.trim().length > 0
      );
      if (validUserIds.length === 0) {
        throw new ZaloSDKError("No valid user IDs found", -1);
      }
      const requestBody = {
        group_id: groupId.trim(),
        member_user_ids: validUserIds.map((id) => id.trim())
      };
      const response = await this.client.apiPost(
        this.endpoints.group.rejectPendingInvite,
        accessToken,
        requestBody
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to reject pending members"
      );
    }
  }
  /**
   * Remove members from group
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param memberUserIds List of user IDs to remove
   * @returns Remove result
   */
  async removeMembers(accessToken, groupId, memberUserIds) {
    try {
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (!memberUserIds || memberUserIds.length === 0) {
        throw new ZaloSDKError("Member user IDs list cannot be empty", -1);
      }
      if (memberUserIds.length > 100) {
        throw new ZaloSDKError(
          "Cannot remove more than 100 members at once",
          -1
        );
      }
      const validUserIds = memberUserIds.filter(
        (id) => id && id.trim().length > 0
      );
      if (validUserIds.length === 0) {
        throw new ZaloSDKError("No valid user IDs found", -1);
      }
      const requestBody = {
        group_id: groupId.trim(),
        member_user_ids: validUserIds.map((id) => id.trim())
      };
      const response = await this.client.apiPost(
        this.endpoints.group.removeMembers,
        accessToken,
        requestBody
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(error, "Failed to remove members");
    }
  }
  async addAdmins(accessToken, groupId, adminDataOrUserIds) {
    try {
      let memberUserIds;
      if (Array.isArray(adminDataOrUserIds)) {
        memberUserIds = adminDataOrUserIds;
      } else {
        memberUserIds = adminDataOrUserIds.member_user_ids;
      }
      if (!memberUserIds || memberUserIds.length === 0) {
        throw new ZaloSDKError("Member user IDs list cannot be empty", -1);
      }
      const requestData = {
        group_id: groupId,
        member_user_ids: memberUserIds
      };
      const response = await this.client.apiPost(this.endpoints.group.addAdmins, accessToken, requestData);
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(error, "Failed to add admins");
    }
  }
  async removeAdmins(accessToken, groupId, adminDataOrUserIds) {
    try {
      let memberUserIds;
      if (Array.isArray(adminDataOrUserIds)) {
        memberUserIds = adminDataOrUserIds;
      } else {
        memberUserIds = adminDataOrUserIds.member_user_ids;
      }
      if (!memberUserIds || memberUserIds.length === 0) {
        throw new ZaloSDKError("Member user IDs list cannot be empty", -1);
      }
      const requestData = {
        group_id: groupId,
        member_user_ids: memberUserIds
      };
      const response = await this.client.apiPost(this.endpoints.group.removeAdmins, accessToken, requestData);
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(error, "Failed to remove admins");
    }
  }
  /**
   * Delete group chat (Disband group)
   * @param accessToken OA access token
   * @param groupId Group ID to delete
   * @returns Delete result
   */
  async deleteGroup(accessToken, groupId) {
    try {
      const requestData = {
        group_id: groupId
      };
      const response = await this.client.apiPost(this.endpoints.group.delete, accessToken, requestData);
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(error, "Failed to delete group");
    }
  }
  /**
   * Get list of OA groups
   * @param accessToken OA access token
   * @param offset Offset for pagination (default: 0)
   * @param count Maximum number to return (default: 5, max: 50)
   * @returns List of OA groups
   *
   * API: GET https://openapi.zalo.me/v3.0/oa/group/getgroupsofoa
   */
  async getGroupsOfOA(accessToken, offset = 0, count = 5) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (offset < 0) {
        throw new ZaloSDKError("Offset must be >= 0", -1);
      }
      if (count <= 0 || count > 50) {
        throw new ZaloSDKError("Count must be from 1 to 50", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.group.getGroupsOfOA,
        accessToken,
        {
          offset,
          count
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get groups of OA"
      );
    }
  }
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
  async getAllGroupsOfOA(accessToken, maxGroups, progressCallback) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (maxGroups !== void 0 && maxGroups <= 0) {
        throw new ZaloSDKError("Max groups must be greater than 0", -1);
      }
      const allGroups = [];
      let offset = 0;
      const pageSize = 50;
      let totalCount;
      let pagesFetched = 0;
      let hasMore = true;
      const updateProgress = (currentCount, isComplete = false) => {
        if (progressCallback) {
          const percentage = totalCount ? Math.round(currentCount / totalCount * 100) : void 0;
          progressCallback({
            currentCount,
            totalCount,
            percentage,
            isComplete
          });
        }
      };
      updateProgress(0);
      while (hasMore) {
        if (maxGroups && allGroups.length >= maxGroups) {
          break;
        }
        const remainingLimit = maxGroups ? maxGroups - allGroups.length : pageSize;
        const currentPageSize = Math.min(pageSize, remainingLimit);
        const response = await this.getGroupsOfOA(
          accessToken,
          offset,
          currentPageSize
        );
        if (response.error !== 0) {
          throw new ZaloSDKError(
            `Failed to fetch groups at offset ${offset}: ${response.message}`,
            response.error
          );
        }
        if (!response.data || !response.data.groups) {
          break;
        }
        if (totalCount === void 0 && response.data.total !== void 0) {
          totalCount = response.data.total;
          if (maxGroups && maxGroups < totalCount) {
            totalCount = maxGroups;
          }
        }
        allGroups.push(...response.data.groups);
        pagesFetched++;
        updateProgress(allGroups.length);
        const receivedCount = response.data.groups.length;
        hasMore = receivedCount === currentPageSize && (!maxGroups || allGroups.length < maxGroups) && (response.data.total === void 0 || allGroups.length < response.data.total);
        offset += receivedCount;
        if (pagesFetched > 100) {
          console.warn(`getAllGroupsOfOA: Reached maximum page limit (${pagesFetched}) for OA`);
          break;
        }
      }
      updateProgress(allGroups.length, true);
      const finalGroups = maxGroups ? allGroups.slice(0, maxGroups) : allGroups;
      return {
        error: 0,
        message: "Success",
        data: {
          total_groups: totalCount || allGroups.length,
          groups: finalGroups,
          pages_fetched: pagesFetched,
          is_complete: !maxGroups || allGroups.length <= maxGroups
        }
      };
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get all groups of OA"
      );
    }
  }
  /**
   * Get ALL groups of OA with simple interface (no progress tracking)
   * @param accessToken OA access token
   * @param maxGroups Maximum number of groups to retrieve (optional)
   * @returns Array of all OA groups
   *
   * Simplified version of getAllGroupsOfOA that returns just the groups array
   */
  async getAllGroupsOfOASimple(accessToken, maxGroups) {
    const response = await this.getAllGroupsOfOA(accessToken, maxGroups);
    return response.data.groups;
  }
  /**
   * Get group quota information and asset_id
   * @param accessToken OA access token
   * @param productType Product type (optional)
   * @param quotaType Quota type (optional)
   * @returns Group quota information including asset_id
   *
   * API: POST https://openapi.zalo.me/v3.0/oa/quota/group
   */
  async getGroupQuota(accessToken, productType, quotaType) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      const quotaRequest = {
        quota_owner: "OA",
        ...productType && { product_type: productType },
        ...quotaType && { quota_type: quotaType }
      };
      const response = await this.client.apiPost(
        this.endpoints.group.quota,
        accessToken,
        quotaRequest
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(error, "Failed to get group quota");
    }
  }
  /**
   * Get asset_id for creating GMF group
   * @param accessToken OA access token
   * @returns Asset_id for group creation
   */
  async getAssetId(accessToken) {
    try {
      const quotaRequest = {
        quota_owner: "OA"
      };
      const response = await this.client.apiPost(
        this.endpoints.group.quota,
        accessToken,
        quotaRequest
      );
      if (response.error !== 0) {
        throw new ZaloSDKError(
          response.message || "Failed to get asset_id",
          response.error
        );
      }
      if (response.data && response.data.length > 0) {
        const activeAsset = response.data.find(
          (asset) => asset.status === "available"
        );
        if (activeAsset) {
          return activeAsset.asset_id;
        }
      }
      throw new ZaloSDKError(
        "No valid asset_id found for group creation. Please check your GMF package.",
        -1
      );
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get asset_id for group creation"
      );
    }
  }
  /**
   * Get list of asset_ids available for creating GMF groups
   * @param accessToken OA access token
   * @returns List of asset_ids and quota information
   */
  async getAssetIds(accessToken) {
    try {
      const quotaRequest = {
        quota_owner: "OA"
      };
      const response = await this.client.apiPost(
        this.endpoints.group.quota,
        accessToken,
        quotaRequest
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get asset_ids list"
      );
    }
  }
  /**
   * Get list of recent chats
   * @param accessToken OA access token
   * @param offset Offset for pagination (default: 0)
   * @param count Maximum number to return (default: 5, max: 50)
   * @returns List of recent chats
   *
   * API: GET https://openapi.zalo.me/v3.0/oa/group/listrecentchat
   */
  async getRecentChats(accessToken, offset = 0, count = 5) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (offset < 0) {
        throw new ZaloSDKError("Offset must be >= 0", -1);
      }
      if (count <= 0 || count > 50) {
        throw new ZaloSDKError("Count must be from 1 to 50", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.group.recent,
        accessToken,
        {
          offset,
          count
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get recent chats"
      );
    }
  }
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
  async getGroupConversation(accessToken, groupId, offset = 0, count = 5) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (offset < 0) {
        throw new ZaloSDKError("Offset must be >= 0", -1);
      }
      if (count <= 0) {
        throw new ZaloSDKError("Count must be greater than 0", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.group.conversation,
        accessToken,
        {
          group_id: groupId.trim(),
          offset,
          count
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get group conversation"
      );
    }
  }
  /**
   * Get group members list from Zalo API
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param offset Offset for pagination (default: 0)
   * @param count Maximum number to return (default: 5, max: 50)
   * @returns Group members list
   */
  async getGroupMembers(accessToken, groupId, offset = 0, count = 5) {
    try {
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (offset < 0) {
        throw new ZaloSDKError("Offset must be >= 0", -1);
      }
      if (count <= 0 || count > 50) {
        throw new ZaloSDKError("Count must be from 1 to 50", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.group.members,
        accessToken,
        {
          group_id: groupId.trim(),
          offset,
          count
        }
      );
      return response;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get group members"
      );
    }
  }
  /**
   * Find a specific group member by user ID
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param userId User ID to search for
   * @returns GroupMemberInfo if found, null otherwise
   * 
   * Note: This method iterates through pages of members to find the user.
   */
  async getGroupMemberByUserId(accessToken, groupId, userId) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (!userId || userId.trim().length === 0) {
        throw new ZaloSDKError("User ID cannot be empty", -1);
      }
      const targetUserId = userId.trim();
      let offset = 0;
      const pageSize = 50;
      let hasMore = true;
      let pagesFetched = 0;
      while (hasMore) {
        const response = await this.getGroupMembers(
          accessToken,
          groupId,
          offset,
          pageSize
        );
        if (response.error !== 0 || !response.data || !response.data.members) {
          throw new ZaloSDKError(
            `Failed to fetch members at offset ${offset}: ${response.message}`,
            response.error
          );
        }
        const foundMember = response.data.members.find(
          (member) => member.user_id === targetUserId
        );
        if (foundMember) {
          return foundMember;
        }
        const membersCount = response.data.members.length;
        hasMore = membersCount === pageSize && (response.data.total === void 0 || offset + membersCount < response.data.total);
        offset += membersCount;
        pagesFetched++;
        if (pagesFetched > 1e3) {
          break;
        }
      }
      return null;
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        `Failed to find member with user ID ${userId}`
      );
    }
  }
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
  async getAllGroupMembers(accessToken, groupId, maxMembers, progressCallback) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      if (maxMembers !== void 0 && maxMembers <= 0) {
        throw new ZaloSDKError("Max members must be greater than 0", -1);
      }
      const allMembers = [];
      let offset = 0;
      const pageSize = 50;
      let totalCount;
      let pagesFetched = 0;
      let hasMore = true;
      const updateProgress = (currentCount, isComplete = false) => {
        if (progressCallback) {
          const percentage = totalCount ? Math.round(currentCount / totalCount * 100) : void 0;
          progressCallback({
            currentCount,
            totalCount,
            percentage,
            isComplete
          });
        }
      };
      updateProgress(0);
      while (hasMore) {
        if (maxMembers && allMembers.length >= maxMembers) {
          break;
        }
        const remainingLimit = maxMembers ? maxMembers - allMembers.length : pageSize;
        const currentPageSize = Math.min(pageSize, remainingLimit);
        const response = await this.getGroupMembers(
          accessToken,
          groupId,
          offset,
          currentPageSize
        );
        if (response.error !== 0) {
          throw new ZaloSDKError(
            `Failed to fetch members at offset ${offset}: ${response.message}`,
            response.error
          );
        }
        if (!response.data || !response.data.members) {
          break;
        }
        if (totalCount === void 0 && response.data.total !== void 0) {
          totalCount = response.data.total;
          if (maxMembers && maxMembers < totalCount) {
            totalCount = maxMembers;
          }
        }
        allMembers.push(...response.data.members);
        pagesFetched++;
        updateProgress(allMembers.length);
        const receivedCount = response.data.members.length;
        hasMore = receivedCount === currentPageSize && (!maxMembers || allMembers.length < maxMembers) && (response.data.total === void 0 || allMembers.length < response.data.total);
        offset += receivedCount;
        if (pagesFetched > 1e3) {
          console.warn(`getAllGroupMembers: Reached maximum page limit (${pagesFetched}) for group ${groupId}`);
          break;
        }
      }
      updateProgress(allMembers.length, true);
      const finalMembers = maxMembers ? allMembers.slice(0, maxMembers) : allMembers;
      return {
        error: 0,
        message: "Success",
        data: {
          total_members: totalCount || allMembers.length,
          members: finalMembers,
          pages_fetched: pagesFetched,
          is_complete: !maxMembers || allMembers.length <= maxMembers
        }
      };
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get all group members"
      );
    }
  }
  /**
   * Get ALL members of a group with simple interface (no progress tracking)
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param maxMembers Maximum number of members to retrieve (optional)
   * @returns Array of all group members
   *
   * Simplified version of getAllGroupMembers that returns just the members array
   */
  async getAllGroupMembersSimple(accessToken, groupId, maxMembers) {
    const response = await this.getAllGroupMembers(accessToken, groupId, maxMembers);
    return response.data.members;
  }
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
  async getAllGroupMembersWithDetails(accessToken, groupId, maxMembers, progressCallback, options) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (!groupId || groupId.trim().length === 0) {
        throw new ZaloSDKError("Group ID cannot be empty", -1);
      }
      const opts = {
        detailBatchSize: 10,
        detailTimeout: 5e3,
        continueWithoutUserService: true,
        maxConcurrentRequests: 5,
        ...options
      };
      if (!this.userService && !opts.continueWithoutUserService) {
        throw new ZaloSDKError(
          "UserService is required for fetching detailed member information. Please provide UserService in constructor or set continueWithoutUserService to true.",
          -1
        );
      }
      const updateProgress = (currentCount, totalCount, phase, phaseDetails, isComplete = false) => {
        if (progressCallback) {
          const percentage = totalCount ? Math.round(currentCount / totalCount * 100) : void 0;
          progressCallback({
            currentCount,
            totalCount,
            percentage,
            isComplete,
            phase,
            phase_details: phaseDetails
          });
        }
      };
      updateProgress(0, void 0, "fetching_members");
      const basicMembersResponse = await this.getAllGroupMembers(
        accessToken,
        groupId,
        maxMembers,
        (basicProgress) => {
          updateProgress(
            basicProgress.currentCount,
            basicProgress.totalCount,
            "fetching_members"
          );
        }
      );
      const basicMembers = basicMembersResponse.data.members;
      const totalMembers = basicMembers.length;
      const enhancedMembers = basicMembers.map((member) => ({
        basic_info: member,
        detailed_info: null,
        has_detailed_info: false
      }));
      let successfulDetails = 0;
      let failedDetails = 0;
      if (this.userService && totalMembers > 0) {
        updateProgress(0, totalMembers, "fetching_details", {
          details_fetched: 0,
          details_failed: 0,
          current_batch: 0,
          total_batches: Math.ceil(totalMembers / opts.detailBatchSize)
        });
        for (let i = 0; i < totalMembers; i += opts.detailBatchSize) {
          const batch = enhancedMembers.slice(i, i + opts.detailBatchSize);
          const currentBatch = Math.floor(i / opts.detailBatchSize) + 1;
          const totalBatches = Math.ceil(totalMembers / opts.detailBatchSize);
          const batchPromises = batch.map(async (enhancedMember) => {
            const userId = enhancedMember.basic_info.user_id || enhancedMember.basic_info.oa_id;
            if (!userId) {
              enhancedMember.detail_fetch_error = "No user_id or oa_id available";
              failedDetails++;
              return;
            }
            try {
              const timeoutPromise = new Promise((_, reject) => {
                setTimeout(() => reject(new Error("Timeout")), opts.detailTimeout);
              });
              const userDetailPromise = this.userService.getUserInfo(accessToken, userId);
              const userDetail = await Promise.race([userDetailPromise, timeoutPromise]);
              enhancedMember.detailed_info = userDetail;
              enhancedMember.has_detailed_info = true;
              successfulDetails++;
            } catch (error) {
              const errorMessage = error instanceof Error ? error.message : "Unknown error";
              enhancedMember.detail_fetch_error = `Failed to fetch user details: ${errorMessage}`;
              failedDetails++;
            }
            const processedCount = successfulDetails + failedDetails;
            updateProgress(processedCount, totalMembers, "fetching_details", {
              details_fetched: successfulDetails,
              details_failed: failedDetails,
              current_batch: currentBatch,
              total_batches: totalBatches
            });
          });
          const chunks = [];
          for (let j = 0; j < batchPromises.length; j += opts.maxConcurrentRequests) {
            chunks.push(batchPromises.slice(j, j + opts.maxConcurrentRequests));
          }
          for (const chunk of chunks) {
            await Promise.all(chunk);
          }
        }
      } else if (!this.userService) {
        failedDetails = totalMembers;
        enhancedMembers.forEach((member) => {
          member.detail_fetch_error = "UserService not available";
        });
      }
      updateProgress(totalMembers, totalMembers, "complete", {
        details_fetched: successfulDetails,
        details_failed: failedDetails
      }, true);
      const successRate = totalMembers > 0 ? Math.round(successfulDetails / totalMembers * 100) : 0;
      const finalMembers = maxMembers ? enhancedMembers.slice(0, maxMembers) : enhancedMembers;
      return {
        error: 0,
        message: "Success",
        data: {
          total_members: basicMembersResponse.data.total_members,
          members: finalMembers,
          pages_fetched: basicMembersResponse.data.pages_fetched,
          is_complete: basicMembersResponse.data.is_complete,
          detail_fetch_stats: {
            total_processed: totalMembers,
            successful_details: successfulDetails,
            failed_details: failedDetails,
            success_rate: successRate
          }
        }
      };
    } catch (error) {
      throw this.handleGroupManagementError(
        error,
        "Failed to get all group members with details"
      );
    }
  }
  /**
   * Get ALL members of a group with detailed info - Simple interface
   * @param accessToken OA access token
   * @param groupId Group ID
   * @param maxMembers Maximum number of members to retrieve (optional)
   * @returns Array of enhanced group members with detailed info
   *
   * Simplified version that returns just the enhanced members array
   */
  async getAllGroupMembersWithDetailsSimple(accessToken, groupId, maxMembers) {
    const response = await this.getAllGroupMembersWithDetails(accessToken, groupId, maxMembers);
    return response.data.members;
  }
  handleGroupManagementError(error, defaultMessage) {
    if (error instanceof ZaloSDKError) {
      return error;
    }
    if (error.response?.data) {
      const errorData = error.response.data;
      return new ZaloSDKError(
        `${defaultMessage}: ${errorData.message || errorData.error || "Unknown error"}`,
        errorData.error || -1,
        errorData
      );
    }
    return new ZaloSDKError(
      `${defaultMessage}: ${error.message || "Unknown error"}`,
      -1,
      error
    );
  }
};

// src/services/article.service.ts
var ArticleService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      article: {
        create: "https://openapi.zalo.me/v2.0/article/create",
        update: "https://openapi.zalo.me/v2.0/article/update",
        remove: "https://openapi.zalo.me/v2.0/article/remove",
        verify: "https://openapi.zalo.me/v2.0/article/verify",
        detail: "https://openapi.zalo.me/v2.0/article/getdetail",
        list: "https://openapi.zalo.me/v2.0/article/getslice"
      }
    };
  }
  /**
   * Create normal article with rich content
   * @param accessToken OA access token
   * @param request Normal article information
   * @returns Token for tracking creation progress
   */
  async createNormalArticle(accessToken, request) {
    try {
      this.validateNormalArticleRequest(request);
      let processedTrackingLink = "https://v2.redai.vn/auth";
      if (request.tracking_link && this.isValidTrackingLink(request.tracking_link)) {
        processedTrackingLink = request.tracking_link;
      }
      const data = {
        type: request.type,
        title: request.title,
        author: request.author,
        cover: request.cover,
        description: request.description,
        body: request.body,
        related_medias: request.related_medias || [],
        tracking_link: processedTrackingLink,
        status: request.status || "hide" /* HIDE */,
        comment: request.comment || "show" /* SHOW */
      };
      const response = await this.client.apiPost(
        this.endpoints.article.create,
        accessToken,
        data
      );
      if (!response.data || !response.data.token) {
        throw new ZaloSDKError("No token received from article creation", -1, response);
      }
      return response;
    } catch (error) {
      throw this.handleArticleError(error, "Failed to create normal article");
    }
  }
  /**
   * Create video article
   * @param accessToken OA access token
   * @param request Video article information
   * @returns Token for tracking creation progress
   */
  async createVideoArticle(accessToken, request) {
    try {
      this.validateVideoArticleRequest(request);
      const data = {
        type: request.type,
        title: request.title,
        description: request.description,
        video_id: request.video_id,
        avatar: request.avatar,
        status: request.status || "hide" /* HIDE */,
        comment: request.comment || "show" /* SHOW */
      };
      const response = await this.client.apiPost(
        this.endpoints.article.create,
        accessToken,
        data
      );
      if (!response.data || !response.data.token) {
        throw new ZaloSDKError("No token received from article creation", -1, response);
      }
      return response;
    } catch (error) {
      throw this.handleArticleError(error, "Failed to create video article");
    }
  }
  /**
   * Create article (automatically detect type)
   * @param accessToken OA access token
   * @param request Article information
   * @returns Token for tracking creation progress
   */
  async createArticle(accessToken, request) {
    if (request.type === "normal" /* NORMAL */) {
      return this.createNormalArticle(accessToken, request);
    } else if (request.type === "video" /* VIDEO */) {
      return this.createVideoArticle(accessToken, request);
    } else {
      throw new ZaloSDKError(
        'Invalid article type. Only "normal" and "video" are supported',
        -1
      );
    }
  }
  /**
   * Check article creation progress
   * @param accessToken OA access token
   * @param token Token from article creation response
   * @returns Progress information
   */
  async checkArticleProcess(accessToken, token) {
    try {
      if (!token || token.trim() === "") {
        throw new ZaloSDKError("Token cannot be empty", -1);
      }
      const response = await this.verifyArticle(accessToken, token);
      const result = {
        article_id: response?.id,
        status: response?.id ? "success" : "processing",
        created_time: Date.now(),
        updated_time: Date.now()
      };
      return result;
    } catch (error) {
      if (error instanceof ZaloSDKError && error.message.includes("token was expired")) {
        return {
          status: "success",
          error_message: "Token expired - article processing completed",
          created_time: Date.now(),
          updated_time: Date.now()
        };
      }
      if (error instanceof ZaloSDKError && error.message.includes("API")) {
        return {
          status: "processing",
          created_time: Date.now(),
          updated_time: Date.now()
        };
      }
      if (error instanceof TypeError) {
        return {
          status: "processing",
          error_message: "Article is being processed",
          created_time: Date.now(),
          updated_time: Date.now()
        };
      }
      throw this.handleArticleError(error, "Failed to check article process");
    }
  }
  /**
   * Verify article and get article ID
   * @param accessToken OA access token
   * @param token Token from article creation or update response
   * @returns Article ID
   */
  async verifyArticle(accessToken, token) {
    try {
      if (!token || token.trim() === "") {
        throw new ZaloSDKError("Token cannot be empty", -1);
      }
      const data = {
        token
      };
      const response = await this.client.apiPost(
        this.endpoints.article.verify,
        accessToken,
        data
      );
      return response.data;
    } catch (error) {
      throw this.handleArticleError(error, "Failed to verify article");
    }
  }
  /**
   * Get article details
   * @param accessToken OA access token
   * @param articleId Article ID
   * @returns Article details
   */
  async getArticleDetail(accessToken, articleId) {
    try {
      if (!articleId || articleId.trim() === "") {
        throw new ZaloSDKError("Article ID cannot be empty", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.article.detail,
        accessToken,
        { id: articleId }
      );
      return response.data;
    } catch (error) {
      throw this.handleArticleError(error, "Failed to get article detail");
    }
  }
  /**
   * Get article list
   * @param accessToken OA access token
   * @param request List parameters
   * @returns Article list
   */
  async getArticleList(accessToken, request) {
    try {
      this.validateArticleListRequest(request);
      const response = await this.client.apiGet(
        this.endpoints.article.list,
        accessToken,
        {
          offset: request.offset,
          limit: request.limit,
          type: request.type
        }
      );
      return response;
    } catch (error) {
      throw this.handleArticleError(error, "Failed to get article list");
    }
  }
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
  async getAllArticles(accessToken, type = "normal", options = {}) {
    try {
      const {
        batchSize = 10,
        // Zalo API maximum limit is 10
        maxArticles = 1e3,
        onProgress
      } = options;
      if (batchSize <= 0 || batchSize > ARTICLE_CONSTRAINTS.LIST_MAX_LIMIT) {
        throw new ZaloSDKError(
          `Batch size must be between 1 and ${ARTICLE_CONSTRAINTS.LIST_MAX_LIMIT}`,
          -1
        );
      }
      if (maxArticles < 0) {
        throw new ZaloSDKError("Max articles must be >= 0 (0 = no limit)", -1);
      }
      if (!["normal", "video"].includes(type)) {
        throw new ZaloSDKError('Type must be "normal" or "video"', -1);
      }
      const allArticles = [];
      let offset = 0;
      let currentBatch = 0;
      let hasMore = true;
      while (hasMore) {
        currentBatch++;
        let currentLimit = batchSize;
        if (maxArticles > 0) {
          const remaining = maxArticles - allArticles.length;
          if (remaining <= 0) break;
          currentLimit = Math.min(batchSize, remaining);
        }
        const response = await this.getArticleList(accessToken, {
          offset,
          limit: currentLimit,
          type
        });
        if ("data" in response && response.data?.medias && response.data.medias.length > 0) {
          allArticles.push(...response.data.medias);
          offset += response.data.medias.length;
          hasMore = response.data.medias.length === currentLimit;
          if (maxArticles > 0 && allArticles.length >= maxArticles) {
            hasMore = false;
          }
        } else if ("medias" in response && response.medias && response.medias.length > 0) {
          allArticles.push(...response.medias);
          offset += response.medias.length;
          hasMore = response.medias.length === currentLimit;
          if (maxArticles > 0 && allArticles.length >= maxArticles) {
            hasMore = false;
          }
        } else {
          hasMore = false;
        }
        if (onProgress) {
          onProgress({
            currentBatch,
            totalFetched: allArticles.length,
            hasMore
          });
        }
        if (currentBatch > 100) {
          console.warn("Reached maximum batch limit (100), stopping fetch");
          break;
        }
      }
      return {
        articles: allArticles,
        totalFetched: allArticles.length,
        totalBatches: currentBatch,
        hasMore: hasMore && (maxArticles === 0 || allArticles.length < maxArticles)
      };
    } catch (error) {
      throw this.handleArticleError(error, "Failed to get all articles");
    }
  }
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
  async getAllArticlesCombined(accessToken, options = {}) {
    try {
      const {
        batchSize = 10,
        // Zalo API maximum limit is 10
        maxArticlesPerType = 500,
        onProgress
      } = options;
      const normalResult = await this.getAllArticles(accessToken, "normal", {
        batchSize,
        maxArticles: maxArticlesPerType,
        onProgress: onProgress ? (progress) => onProgress({
          type: "normal",
          ...progress
        }) : void 0
      });
      const videoResult = await this.getAllArticles(accessToken, "video", {
        batchSize,
        maxArticles: maxArticlesPerType,
        onProgress: onProgress ? (progress) => onProgress({
          type: "video",
          ...progress
        }) : void 0
      });
      const allArticles = [...normalResult.articles, ...videoResult.articles];
      return {
        articles: allArticles,
        breakdown: {
          normal: normalResult,
          video: videoResult
        },
        totalFetched: allArticles.length,
        totalBatches: normalResult.totalBatches + videoResult.totalBatches
      };
    } catch (error) {
      throw this.handleArticleError(error, "Failed to get all articles combined");
    }
  }
  /**
   * Remove article
   * @param accessToken OA access token
   * @param articleId Article ID to remove
   * @returns Removal result
   */
  async removeArticle(accessToken, articleId) {
    try {
      if (!articleId || articleId.trim() === "") {
        throw new ZaloSDKError("Article ID cannot be empty", -1);
      }
      const data = {
        id: articleId
      };
      const response = await this.client.apiPost(
        this.endpoints.article.remove,
        accessToken,
        data
      );
      return response;
    } catch (error) {
      if (error instanceof ZaloSDKError && error.message.includes("No data received")) {
        return {
          message: "Article has been deleted or does not exist"
        };
      }
      throw this.handleArticleError(error, "Failed to remove article");
    }
  }
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
  async removeBulkArticles(accessToken, articleIds, options, progressCallback) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (!articleIds || articleIds.length === 0) {
        throw new ZaloSDKError("Article IDs array cannot be empty", -1);
      }
      const validArticleIds = articleIds.filter((id) => id && id.trim().length > 0);
      if (validArticleIds.length === 0) {
        throw new ZaloSDKError("No valid article IDs found", -1);
      }
      const opts = {
        batch_size: 10,
        max_concurrency: 3,
        continue_on_error: true,
        batch_delay: 100,
        request_timeout: 1e4,
        ...options
      };
      const results = [];
      const errorSummary = {};
      const startTime = Date.now();
      let successfulCount = 0;
      let failedCount = 0;
      const updateProgress = (currentCount, isComplete = false) => {
        if (progressCallback) {
          const percentage = Math.round(currentCount / validArticleIds.length * 100);
          const currentBatch = Math.ceil(currentCount / opts.batch_size);
          const totalBatches = Math.ceil(validArticleIds.length / opts.batch_size);
          progressCallback({
            current_count: currentCount,
            total_count: validArticleIds.length,
            percentage,
            successful_count: successfulCount,
            failed_count: failedCount,
            is_complete: isComplete,
            current_batch: currentBatch,
            total_batches: totalBatches
          });
        }
      };
      updateProgress(0);
      for (let i = 0; i < validArticleIds.length; i += opts.batch_size) {
        const batch = validArticleIds.slice(i, i + opts.batch_size);
        const batchPromises = batch.map(async (articleId) => {
          const itemStartTime = Date.now();
          try {
            const timeoutPromise = new Promise((_, reject) => {
              setTimeout(() => reject(new Error("Request timeout")), opts.request_timeout);
            });
            const removePromise = this.removeArticle(accessToken, articleId);
            const response = await Promise.race([removePromise, timeoutPromise]);
            const processingTime = Date.now() - itemStartTime;
            successfulCount++;
            const result = {
              article_id: articleId,
              success: true,
              message: response.message,
              processing_time: processingTime
            };
            results.push(result);
            return result;
          } catch (error) {
            const processingTime = Date.now() - itemStartTime;
            failedCount++;
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            const errorType = error instanceof ZaloSDKError ? "ZaloSDKError" : "UnknownError";
            errorSummary[errorType] = (errorSummary[errorType] || 0) + 1;
            const result = {
              article_id: articleId,
              success: false,
              error: errorMessage,
              processing_time: processingTime
            };
            results.push(result);
            if (!opts.continue_on_error) {
              throw new ZaloSDKError(
                `Bulk removal stopped due to error on article ${articleId}: ${errorMessage}`,
                -1
              );
            }
            return result;
          }
        });
        const chunks = [];
        for (let j = 0; j < batchPromises.length; j += opts.max_concurrency) {
          chunks.push(batchPromises.slice(j, j + opts.max_concurrency));
        }
        for (const chunk of chunks) {
          await Promise.all(chunk);
        }
        updateProgress(Math.min(i + opts.batch_size, validArticleIds.length));
        if (i + opts.batch_size < validArticleIds.length && opts.batch_delay > 0) {
          await new Promise((resolve) => setTimeout(resolve, opts.batch_delay));
        }
      }
      updateProgress(validArticleIds.length, true);
      const totalTime = Date.now() - startTime;
      const successRate = validArticleIds.length > 0 ? Math.round(successfulCount / validArticleIds.length * 100) : 0;
      return {
        total_processed: validArticleIds.length,
        successful_count: successfulCount,
        failed_count: failedCount,
        success_rate: successRate,
        total_time: totalTime,
        results: results.sort(
          (a, b) => validArticleIds.indexOf(a.article_id) - validArticleIds.indexOf(b.article_id)
        ),
        error_summary: Object.keys(errorSummary).length > 0 ? errorSummary : void 0
      };
    } catch (error) {
      throw this.handleArticleError(error, "Failed to remove articles in bulk");
    }
  }
  /**
   * Remove multiple articles in bulk with simple interface (no progress tracking)
   * @param accessToken OA access token
   * @param articleIds Array of article IDs to remove
   * @param options Optional settings for bulk removal
   * @returns Simple summary of bulk removal operation
   *
   * Simplified version of removeBulkArticles that returns just the summary statistics
   */
  async removeBulkArticlesSimple(accessToken, articleIds, options) {
    const result = await this.removeBulkArticles(accessToken, articleIds, options);
    const failedArticles = result.results.filter((r) => !r.success).map((r) => r.article_id);
    return {
      total_processed: result.total_processed,
      successful_count: result.successful_count,
      failed_count: result.failed_count,
      success_rate: result.success_rate,
      failed_articles: failedArticles.length > 0 ? failedArticles : void 0
    };
  }
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
  async getAllArticlesWithDetails(accessToken, type = "both", options, progressCallback) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      const opts = {
        detail_batch_size: 5,
        max_concurrency: 3,
        continue_on_error: true,
        batch_delay: 200,
        request_timeout: 15e3,
        max_articles: 0,
        // 0 = unlimited
        list_options: {
          batch_size: 10,
          // Zalo API maximum limit is 10
          max_articles: 0
          // 0 = unlimited
        },
        ...options
      };
      const results = [];
      const articlesWithDetails = [];
      const errorSummary = {};
      const startTime = Date.now();
      let successfulCount = 0;
      let failedCount = 0;
      const updateProgress = (currentCount, totalCount, phase, isComplete = false) => {
        if (progressCallback) {
          const percentage = totalCount > 0 ? Math.round(currentCount / totalCount * 100) : 0;
          const currentBatch = phase === "fetching_details" ? Math.ceil(currentCount / opts.detail_batch_size) : void 0;
          const totalBatches = phase === "fetching_details" ? Math.ceil(totalCount / opts.detail_batch_size) : void 0;
          progressCallback({
            current_count: currentCount,
            total_count: totalCount,
            percentage,
            successful_count: successfulCount,
            failed_count: failedCount,
            is_complete: isComplete,
            phase,
            current_batch: currentBatch,
            total_batches: totalBatches
          });
        }
      };
      updateProgress(0, 0, "fetching_list");
      const listStartTime = Date.now();
      let articlesList = [];
      let listFetchStats = { total_pages_fetched: 0, list_fetch_time: 0 };
      const listMaxArticles = opts.list_options.max_articles ?? 500;
      if (type === "both") {
        const combinedResult = await this.getAllArticlesCombined(accessToken, {
          batchSize: opts.list_options.batch_size,
          maxArticlesPerType: listMaxArticles,
          onProgress: (progress) => {
            updateProgress(progress.totalFetched, progress.totalFetched, "fetching_list");
          }
        });
        articlesList = combinedResult.articles;
        listFetchStats.total_pages_fetched = combinedResult.totalBatches;
      } else {
        const singleResult = await this.getAllArticles(accessToken, type, {
          batchSize: opts.list_options.batch_size,
          maxArticles: listMaxArticles,
          onProgress: (progress) => {
            updateProgress(progress.totalFetched, progress.totalFetched, "fetching_list");
          }
        });
        articlesList = singleResult.articles;
        listFetchStats.total_pages_fetched = singleResult.totalBatches;
      }
      listFetchStats.list_fetch_time = Date.now() - listStartTime;
      if (opts.max_articles > 0 && articlesList.length > opts.max_articles) {
        articlesList = articlesList.slice(0, opts.max_articles);
      }
      const totalArticles = articlesList.length;
      if (totalArticles === 0) {
        updateProgress(0, 0, "complete", true);
        return {
          total_processed: 0,
          successful_count: 0,
          failed_count: 0,
          success_rate: 0,
          total_time: Date.now() - startTime,
          results: [],
          articles_with_details: [],
          list_fetch_stats: listFetchStats
        };
      }
      updateProgress(0, totalArticles, "fetching_details");
      for (let i = 0; i < totalArticles; i += opts.detail_batch_size) {
        const batch = articlesList.slice(i, i + opts.detail_batch_size);
        const batchPromises = batch.map(async (article) => {
          const itemStartTime = Date.now();
          try {
            const timeoutPromise = new Promise((_, reject) => {
              setTimeout(() => reject(new Error("Request timeout")), opts.request_timeout);
            });
            const detailPromise = this.getArticleDetail(accessToken, article.id);
            const detail = await Promise.race([detailPromise, timeoutPromise]);
            const processingTime = Date.now() - itemStartTime;
            successfulCount++;
            const result = {
              article_id: article.id,
              success: true,
              detail,
              processing_time: processingTime
            };
            results.push(result);
            articlesWithDetails.push(detail);
            return result;
          } catch (error) {
            const processingTime = Date.now() - itemStartTime;
            failedCount++;
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            const errorType = error instanceof ZaloSDKError ? "ZaloSDKError" : "UnknownError";
            errorSummary[errorType] = (errorSummary[errorType] || 0) + 1;
            const result = {
              article_id: article.id,
              success: false,
              error: errorMessage,
              processing_time: processingTime
            };
            results.push(result);
            if (!opts.continue_on_error) {
              throw new ZaloSDKError(
                `Bulk details fetch stopped due to error on article ${article.id}: ${errorMessage}`,
                -1
              );
            }
            return result;
          }
        });
        const chunks = [];
        for (let j = 0; j < batchPromises.length; j += opts.max_concurrency) {
          chunks.push(batchPromises.slice(j, j + opts.max_concurrency));
        }
        for (const chunk of chunks) {
          await Promise.all(chunk);
        }
        updateProgress(Math.min(i + opts.detail_batch_size, totalArticles), totalArticles, "fetching_details");
        if (i + opts.detail_batch_size < totalArticles && opts.batch_delay > 0) {
          await new Promise((resolve) => setTimeout(resolve, opts.batch_delay));
        }
      }
      updateProgress(totalArticles, totalArticles, "complete", true);
      const totalTime = Date.now() - startTime;
      const successRate = totalArticles > 0 ? Math.round(successfulCount / totalArticles * 100) : 0;
      return {
        total_processed: totalArticles,
        successful_count: successfulCount,
        failed_count: failedCount,
        success_rate: successRate,
        total_time: totalTime,
        results: results.sort(
          (a, b) => articlesList.findIndex((art) => art.id === a.article_id) - articlesList.findIndex((art) => art.id === b.article_id)
        ),
        articles_with_details: articlesWithDetails,
        error_summary: Object.keys(errorSummary).length > 0 ? errorSummary : void 0,
        list_fetch_stats: listFetchStats
      };
    } catch (error) {
      throw this.handleArticleError(error, "Failed to get all articles with details");
    }
  }
  /**
   * Get all articles with full details - Simple interface (no progress tracking)
   * @param accessToken OA access token
   * @param type Article type ("normal", "video", or "both")
   * @param options Optional settings for details fetch
   * @returns Array of articles with full details
   *
   * Simplified version that returns just the articles with details array
   */
  async getAllArticlesWithDetailsSimple(accessToken, type = "both", options) {
    const result = await this.getAllArticlesWithDetails(accessToken, type, options);
    return result.articles_with_details;
  }
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
  async createArticleAndWaitForCompletion(accessToken, request, options, progressCallback) {
    try {
      if (!accessToken || accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token cannot be empty", -1);
      }
      if (!request) {
        throw new ZaloSDKError("Article request cannot be empty", -1);
      }
      const opts = {
        timeout: 6e4,
        // 1 minute
        checkInterval: 2e3,
        // 2 seconds
        returnDetails: false,
        ...options
      };
      const startTime = Date.now();
      let token;
      let articleId;
      const updateProgress = (phase, message, additionalData) => {
        if (progressCallback) {
          progressCallback({
            phase,
            message,
            token: additionalData?.token || token,
            article_id: additionalData?.article_id || articleId,
            elapsed_time: Date.now() - startTime
          });
        }
      };
      updateProgress("creating", "Creating article...");
      const createResponse = await this.createArticle(accessToken, request);
      token = createResponse.data?.token;
      if (!token) {
        throw new ZaloSDKError("No token received from article creation", -1);
      }
      updateProgress("processing", "Article created, waiting for processing...", { token });
      const maxAttempts = Math.ceil(opts.timeout / opts.checkInterval);
      let attempts = 0;
      let isComplete = false;
      while (attempts < maxAttempts && !isComplete) {
        attempts++;
        try {
          const elapsedTime = Date.now() - startTime;
          if (elapsedTime >= opts.timeout) {
            throw new ZaloSDKError(
              `Article creation timeout after ${Math.round(elapsedTime / 1e3)} seconds. Token: ${token}`,
              -1
            );
          }
          updateProgress("processing", `Checking progress... (attempt ${attempts}/${maxAttempts})`);
          const verifyResult = await this.verifyArticle(accessToken, token);
          if (verifyResult && verifyResult.id) {
            articleId = verifyResult.id;
            isComplete = true;
            updateProgress("verifying", "Article processing completed!", { article_id: articleId });
            break;
          }
        } catch (error) {
          const elapsedTime = Date.now() - startTime;
          if (error instanceof ZaloSDKError) {
            if (error.message.includes("token was expired")) {
              updateProgress("verifying", "Token expired, attempting to find article...");
              try {
                const recentArticles = await this.getArticleList(accessToken, {
                  offset: 0,
                  limit: 10,
                  type: request.type
                });
                if ("data" in recentArticles && recentArticles.data?.medias) {
                  const recentArticle = recentArticles.data.medias.find((article) => {
                    const articleTime = new Date(article.create_date).getTime();
                    const timeDiff = Math.abs(articleTime - startTime);
                    return timeDiff < 5 * 60 * 1e3 && article.title === request.title;
                  });
                  if (recentArticle) {
                    articleId = recentArticle.id;
                    isComplete = true;
                    updateProgress("complete", "Article found after token expiration!", { article_id: articleId });
                    break;
                  }
                }
              } catch (searchError) {
              }
            }
            if (elapsedTime < opts.timeout) {
              updateProgress("processing", `Still processing... (${Math.round(elapsedTime / 1e3)}s elapsed)`);
              await new Promise((resolve) => setTimeout(resolve, opts.checkInterval));
              continue;
            }
          }
          if (elapsedTime >= opts.timeout) {
            throw new ZaloSDKError(
              `Article creation timeout after ${Math.round(elapsedTime / 1e3)} seconds. Last error: ${error instanceof Error ? error.message : "Unknown error"}. Token: ${token}`,
              -1
            );
          }
          updateProgress("processing", `Retrying after error... (${error instanceof Error ? error.message : "Unknown error"})`);
          await new Promise((resolve) => setTimeout(resolve, opts.checkInterval));
        }
      }
      if (!articleId) {
        throw new ZaloSDKError(
          `Failed to get article ID after ${attempts} attempts. Token: ${token}`,
          -1
        );
      }
      const totalTime = Date.now() - startTime;
      updateProgress("complete", `Article created successfully! ID: ${articleId}`, { article_id: articleId });
      let articleDetails;
      if (opts.returnDetails) {
        try {
          updateProgress("complete", "Fetching article details...");
          articleDetails = await this.getArticleDetail(accessToken, articleId);
        } catch (error) {
          console.warn("Failed to fetch article details:", error);
        }
      }
      return {
        article_id: articleId,
        token,
        total_time: totalTime,
        article_details: articleDetails
      };
    } catch (error) {
      throw this.handleArticleError(error, "Failed to create article and wait for completion");
    }
  }
  /**
   * Create article and wait for completion - Simple interface
   * @param accessToken OA access token
   * @param request Article information
   * @param timeoutMs Optional timeout in milliseconds (default: 60000)
   * @returns Article ID when creation is complete
   *
   * Simplified version that returns just the article ID
   */
  async createArticleAndGetId(accessToken, request, timeoutMs = 6e4) {
    const result = await this.createArticleAndWaitForCompletion(
      accessToken,
      request,
      { timeout: timeoutMs }
    );
    return result.article_id;
  }
  handleArticleError(error, defaultMessage) {
    if (error instanceof ZaloSDKError) {
      return error;
    }
    if (error.response?.data) {
      const errorData = error.response.data;
      return new ZaloSDKError(
        `${defaultMessage}: ${errorData.message || errorData.error || "Unknown error"}`,
        errorData.error || -1,
        errorData
      );
    }
    return new ZaloSDKError(
      `${defaultMessage}: ${error.message || "Unknown error"}`,
      -1,
      error
    );
  }
  /**
   * Update normal article
   * @param accessToken OA access token
   * @param request Update information
   * @returns Token for tracking update progress
   */
  async updateNormalArticle(accessToken, request) {
    try {
      this.validateUpdateNormalArticleRequest(request);
      let processedTrackingLink = "https://v2.redai.vn/auth";
      if (request.tracking_link && this.isValidTrackingLink(request.tracking_link)) {
        processedTrackingLink = request.tracking_link;
      }
      const data = {
        id: request.id,
        type: request.type,
        title: request.title,
        author: request.author,
        cover: request.cover,
        description: request.description,
        body: request.body,
        related_medias: request.related_medias || [],
        tracking_link: processedTrackingLink,
        status: request.status || "hide" /* HIDE */,
        comment: request.comment || "show" /* SHOW */
      };
      const response = await this.client.apiPost(
        this.endpoints.article.update,
        accessToken,
        data
      );
      return response;
    } catch (error) {
      throw this.handleArticleError(error, "Failed to update normal article");
    }
  }
  /**
   * Update video article
   * @param accessToken OA access token
   * @param request Update information
   * @returns Token for tracking update progress
   */
  async updateVideoArticle(accessToken, request) {
    try {
      this.validateUpdateVideoArticleRequest(request);
      const data = {
        id: request.id,
        type: request.type,
        title: request.title,
        description: request.description,
        video_id: request.video_id,
        avatar: request.avatar,
        status: request.status || "hide" /* HIDE */,
        comment: request.comment || "show" /* SHOW */
      };
      const response = await this.client.apiPost(
        this.endpoints.article.update,
        accessToken,
        data
      );
      return response;
    } catch (error) {
      throw this.handleArticleError(error, "Failed to update video article");
    }
  }
  /**
   * Update article (automatically detect type)
   * @param accessToken OA access token
   * @param request Update information
   * @returns Token for tracking update progress
   */
  async updateArticle(accessToken, request) {
    if (request.type === "normal" /* NORMAL */) {
      return this.updateNormalArticle(accessToken, request);
    } else if (request.type === "video" /* VIDEO */) {
      return this.updateVideoArticle(accessToken, request);
    } else {
      throw new ZaloSDKError(
        'Invalid article type. Only "normal" and "video" are supported',
        -1
      );
    }
  }
  /**
   * Update article status only
   * @param accessToken OA access token
   * @param articleId Article ID to update
   * @param status New status ('show' or 'hide')
   * @param comment Optional comment status ('show' or 'hide')
   * @returns Token for tracking update progress
   */
  async updateArticleStatus(accessToken, articleId, status, comment) {
    try {
      if (!articleId || articleId.trim() === "") {
        throw new ZaloSDKError("Article ID cannot be empty", -1);
      }
      if (!Object.values(ArticleStatus).includes(status)) {
        throw new ZaloSDKError(
          `Invalid status. Must be one of: ${Object.values(ArticleStatus).join(", ")}`,
          -1
        );
      }
      if (comment && !Object.values(CommentStatus).includes(comment)) {
        throw new ZaloSDKError(
          `Invalid comment status. Must be one of: ${Object.values(CommentStatus).join(", ")}`,
          -1
        );
      }
      const articleDetail = await this.getArticleDetail(accessToken, articleId);
      let updateRequest;
      if (articleDetail.type === "normal" /* NORMAL */) {
        const normalDetail = articleDetail;
        updateRequest = {
          id: articleId,
          type: "normal" /* NORMAL */,
          title: normalDetail.title,
          author: normalDetail.author,
          cover: normalDetail.cover,
          description: normalDetail.description,
          body: normalDetail.body,
          related_medias: normalDetail.related_medias,
          tracking_link: normalDetail.tracking_link,
          status,
          comment: comment || normalDetail.comment
        };
      } else if (articleDetail.type === "video" /* VIDEO */) {
        const videoDetail = articleDetail;
        updateRequest = {
          id: articleId,
          type: "video" /* VIDEO */,
          title: videoDetail.title,
          description: videoDetail.description,
          video_id: videoDetail.video_id,
          avatar: videoDetail.avatar,
          status,
          comment: comment || videoDetail.comment
        };
      } else {
        throw new ZaloSDKError(
          `Unsupported article type: ${articleDetail.type}`,
          -1
        );
      }
      return await this.updateArticle(accessToken, updateRequest);
    } catch (error) {
      throw this.handleArticleError(error, "Failed to update article status");
    }
  }
  // ==================== VALIDATION METHODS ====================
  /**
   * Validate article request (universal method)
   * @param request Article creation request
   */
  validateArticleRequest(request) {
    if (!request) {
      throw new ZaloSDKError("Request cannot be empty", -1);
    }
    if (request.type === "normal" /* NORMAL */) {
      this.validateNormalArticleRequest(request);
    } else if (request.type === "video" /* VIDEO */) {
      this.validateVideoArticleRequest(request);
    } else {
      throw new ZaloSDKError(
        'Invalid article type. Only "normal" and "video" are supported',
        -1
      );
    }
  }
  /**
   * Validate normal article creation request
   */
  validateNormalArticleRequest(request) {
    if (!request.title || request.title.trim() === "") {
      throw new ZaloSDKError("Title cannot be empty", -1);
    }
    if (request.title.length > ARTICLE_CONSTRAINTS.TITLE_MAX_LENGTH) {
      throw new ZaloSDKError(
        `Title cannot exceed ${ARTICLE_CONSTRAINTS.TITLE_MAX_LENGTH} characters`,
        -1
      );
    }
    if (!request.author || request.author.trim() === "") {
      throw new ZaloSDKError("Author cannot be empty", -1);
    }
    if (request.author.length > ARTICLE_CONSTRAINTS.AUTHOR_MAX_LENGTH) {
      throw new ZaloSDKError(
        `Author name cannot exceed ${ARTICLE_CONSTRAINTS.AUTHOR_MAX_LENGTH} characters`,
        -1
      );
    }
    if (!request.description || request.description.trim() === "") {
      throw new ZaloSDKError("Description cannot be empty", -1);
    }
    if (request.description.length > ARTICLE_CONSTRAINTS.DESCRIPTION_MAX_LENGTH) {
      throw new ZaloSDKError(
        `Description cannot exceed ${ARTICLE_CONSTRAINTS.DESCRIPTION_MAX_LENGTH} characters`,
        -1
      );
    }
    if (!request.cover) {
      throw new ZaloSDKError("Cover information cannot be empty", -1);
    }
    if (!request.body || request.body.length === 0) {
      throw new ZaloSDKError("Article body cannot be empty", -1);
    }
    this.validateCover(request.cover);
    request.body.forEach((item, index) => {
      this.validateBodyItem(item, index);
    });
    if (request.status && !["show", "hide"].includes(request.status)) {
      throw new ZaloSDKError('Status must be "show" or "hide"', -1);
    }
    if (request.comment && !["show", "hide"].includes(request.comment)) {
      throw new ZaloSDKError('Comment status must be "show" or "hide"', -1);
    }
  }
  /**
   * Validate video article creation request
   */
  validateVideoArticleRequest(request) {
    if (!request.title || request.title.trim() === "") {
      throw new ZaloSDKError("Title cannot be empty", -1);
    }
    if (request.title.length > ARTICLE_CONSTRAINTS.TITLE_MAX_LENGTH) {
      throw new ZaloSDKError(
        `Title cannot exceed ${ARTICLE_CONSTRAINTS.TITLE_MAX_LENGTH} characters`,
        -1
      );
    }
    if (!request.description || request.description.trim() === "") {
      throw new ZaloSDKError("Description cannot be empty", -1);
    }
    if (request.description.length > ARTICLE_CONSTRAINTS.DESCRIPTION_MAX_LENGTH) {
      throw new ZaloSDKError(
        `Description cannot exceed ${ARTICLE_CONSTRAINTS.DESCRIPTION_MAX_LENGTH} characters`,
        -1
      );
    }
    if (!request.video_id || request.video_id.trim() === "") {
      throw new ZaloSDKError("Video ID cannot be empty", -1);
    }
    if (!request.avatar || request.avatar.trim() === "") {
      throw new ZaloSDKError("Avatar URL cannot be empty", -1);
    }
    if (request.status && !["show", "hide"].includes(request.status)) {
      throw new ZaloSDKError('Status must be "show" or "hide"', -1);
    }
    if (request.comment && !["show", "hide"].includes(request.comment)) {
      throw new ZaloSDKError('Comment status must be "show" or "hide"', -1);
    }
  }
  /**
   * Validate article list request
   */
  validateArticleListRequest(request) {
    if (request.offset < 0) {
      throw new ZaloSDKError("Offset must be >= 0", -1);
    }
    if (request.limit <= 0) {
      throw new ZaloSDKError("Limit must be > 0", -1);
    }
    if (request.limit > ARTICLE_CONSTRAINTS.LIST_MAX_LIMIT) {
      throw new ZaloSDKError(
        `Limit cannot exceed ${ARTICLE_CONSTRAINTS.LIST_MAX_LIMIT}`,
        -1
      );
    }
    if (!["normal", "video"].includes(request.type)) {
      throw new ZaloSDKError('Type must be "normal" or "video"', -1);
    }
  }
  /**
   * Validate normal article update request
   */
  validateUpdateNormalArticleRequest(request) {
    if (!request.id || request.id.trim() === "") {
      throw new ZaloSDKError("Article ID cannot be empty", -1);
    }
    this.validateNormalArticleRequest(request);
  }
  /**
   * Validate video article update request
   */
  validateUpdateVideoArticleRequest(request) {
    if (!request.id || request.id.trim() === "") {
      throw new ZaloSDKError("Article ID cannot be empty", -1);
    }
    this.validateVideoArticleRequest(request);
  }
  /**
   * Validate cover information
   */
  validateCover(cover) {
    if (!cover.cover_type) {
      throw new ZaloSDKError("Cover type cannot be empty", -1);
    }
    if (!["photo", "video"].includes(cover.cover_type)) {
      throw new ZaloSDKError('Cover type must be "photo" or "video"', -1);
    }
    if (cover.cover_type === "photo" /* PHOTO */ && (!cover.photo_url || cover.photo_url.trim() === "")) {
      throw new ZaloSDKError(
        'Photo URL cannot be empty when cover_type is "photo"',
        -1
      );
    }
    if (cover.cover_type === "video" /* VIDEO */) {
      if (!cover.video_id || cover.video_id.trim() === "") {
        throw new ZaloSDKError(
          'Video ID cannot be empty when cover_type is "video"',
          -1
        );
      }
      if (cover.cover_view && !["horizontal", "vertical", "square"].includes(cover.cover_view)) {
        throw new ZaloSDKError(
          'cover_view must be "horizontal", "vertical" or "square"',
          -1
        );
      }
    }
    if (!["show", "hide"].includes(cover.status)) {
      throw new ZaloSDKError('Cover status must be "show" or "hide"', -1);
    }
  }
  /**
   * Validate body item
   */
  validateBodyItem(item, index) {
    if (!item.type) {
      throw new ZaloSDKError(
        `Content type at position ${index + 1} cannot be empty`,
        -1
      );
    }
    if (!["text", "image", "video", "product"].includes(item.type)) {
      throw new ZaloSDKError(
        `Content type at position ${index + 1} must be "text", "image", "video" or "product"`,
        -1
      );
    }
    switch (item.type) {
      case "text" /* TEXT */:
        if (!item.content || item.content.trim() === "") {
          throw new ZaloSDKError(
            `Text content at position ${index + 1} cannot be empty`,
            -1
          );
        }
        break;
      case "image" /* IMAGE */:
        if (!item.url || item.url.trim() === "") {
          throw new ZaloSDKError(
            `Image URL at position ${index + 1} cannot be empty`,
            -1
          );
        }
        break;
      case "video" /* VIDEO */:
        if (!item.video_id && !item.url) {
          throw new ZaloSDKError(
            `Video at position ${index + 1} must have video_id or url`,
            -1
          );
        }
        break;
      case "product" /* PRODUCT */:
        if (!item.id || item.id.trim() === "") {
          throw new ZaloSDKError(
            `Product ID at position ${index + 1} cannot be empty`,
            -1
          );
        }
        break;
    }
  }
  isValidTrackingLink(url) {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  }
};

// src/services/video-upload.service.ts
var import_axios2 = __toESM(require("axios"));
var import_form_data2 = __toESM(require("form-data"));
var VideoUploadService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      video: {
        upload: "https://openapi.zalo.me/v2.0/article/upload_video/preparevideo",
        verify: "https://openapi.zalo.me/v2.0/article/upload_video/verify",
        info: "https://openapi.zalo.me/v2.0/article/video/info"
      }
    };
  }
  /**
   * Upload video file for article
   * @param accessToken OA access token
   * @param file Video file (Buffer or File)
   * @param filename Original filename (optional, will be generated if not provided)
   * @returns Token for tracking upload progress
   */
  async uploadVideo(accessToken, file, filename) {
    try {
      this.validateVideoFile(file, filename);
      const actualFilename = Buffer.isBuffer(file) ? filename || "video.mp4" : file.name || filename || "video.mp4";
      let bufferToUpload;
      if (Buffer.isBuffer(file)) {
        bufferToUpload = file;
      } else {
        const arrayBuffer = await file.arrayBuffer();
        bufferToUpload = Buffer.from(arrayBuffer);
      }
      const formData = new import_form_data2.default();
      formData.append("file", bufferToUpload, {
        filename: actualFilename,
        contentType: this.getMimeTypeFromFilename(actualFilename)
      });
      const response = await import_axios2.default.post(this.endpoints.video.upload, formData, {
        headers: {
          ...formData.getHeaders(),
          "access_token": accessToken
        },
        timeout: 3e5,
        // 5 minutes timeout
        maxContentLength: Infinity,
        maxBodyLength: Infinity
      });
      const responseData = response.data;
      if (responseData.error && responseData.error !== 0) {
        throw new ZaloSDKError(
          `Zalo API error: ${responseData.message || "Unknown error"}`,
          responseData.error
        );
      }
      return responseData.data;
    } catch (error) {
      throw this.handleVideoUploadError(error, "Failed to upload video");
    }
  }
  /**
   * Check video upload status
   * @param accessToken OA access token
   * @param token Token from video upload response
   * @returns Video status information
   */
  async checkVideoStatus(accessToken, token) {
    try {
      if (!token || token.trim() === "") {
        throw new ZaloSDKError("Token cannot be empty", -1);
      }
      const response = await this.client.apiGetWithHeaders(
        this.endpoints.video.verify,
        {
          access_token: accessToken,
          token
        }
      );
      return response.data;
    } catch (error) {
      throw this.handleVideoUploadError(error, "Failed to check video status");
    }
  }
  /**
   * Wait for video upload completion with polling
   * @param accessToken OA access token
   * @param token Token from video upload response
   * @param maxWaitTime Maximum wait time in milliseconds (default: 5 minutes)
   * @param pollInterval Polling interval in milliseconds (default: 5 seconds)
   * @returns Final video status when completed
   */
  async waitForUploadCompletion(accessToken, token, maxWaitTime = 5 * 60 * 1e3, pollInterval = 5 * 1e3) {
    const startTime = Date.now();
    while (Date.now() - startTime < maxWaitTime) {
      try {
        const status = await this.checkVideoStatus(accessToken, token);
        if (status.status === 1 /* SUCCESS */ && status.video_id) {
          return status;
        }
        if (status.status === 4 /* FAILED */) {
          throw new ZaloSDKError(
            `Video processing failed: ${status.status_message}`,
            status.convert_error_code
          );
        }
        if (status.status === 2 /* LOCKED */) {
          throw new ZaloSDKError(
            `Video has been locked: ${status.status_message}`,
            -1
          );
        }
        if (status.status === 5 /* DELETED */) {
          throw new ZaloSDKError(
            `Video has been deleted: ${status.status_message}`,
            -1
          );
        }
        if (status.status === 3 /* PROCESSING */ || status.status === 0 /* UNKNOWN */) {
          await this.sleep(pollInterval);
          continue;
        }
        throw new ZaloSDKError(
          `Unknown video processing status: ${status.status}`,
          -1
        );
      } catch (error) {
        if (error instanceof ZaloSDKError) {
          throw error;
        }
        await this.sleep(pollInterval);
      }
    }
    throw new ZaloSDKError(
      `Video processing timeout after ${maxWaitTime / 1e3} seconds`,
      -1
    );
  }
  /**
   * Upload video from URL
   * @param accessToken OA access token
   * @param videoUrl URL of the video to upload
   * @returns Token for tracking upload progress
   */
  async uploadVideoFromUrl(accessToken, videoUrl) {
    try {
      if (!videoUrl || videoUrl.trim() === "") {
        throw new ZaloSDKError("Video URL cannot be empty", -1);
      }
      try {
        new URL(videoUrl);
      } catch {
        throw new ZaloSDKError("Invalid video URL format", -1);
      }
      const response = await fetch(videoUrl);
      if (!response.ok) {
        throw new ZaloSDKError(
          `Failed to download video from URL: ${response.statusText}`,
          -1
        );
      }
      const videoBuffer = Buffer.from(await response.arrayBuffer());
      const filename = this.extractFilenameFromUrl(videoUrl) || "video.mp4";
      return await this.uploadVideo(accessToken, videoBuffer, filename);
    } catch (error) {
      throw this.handleVideoUploadError(
        error,
        "Failed to upload video from URL"
      );
    }
  }
  /**
   * Get video information by video ID
   * @param accessToken OA access token
   * @param videoId Video ID
   * @returns Video information
   */
  async getVideoInfo(accessToken, videoId) {
    try {
      if (!videoId || videoId.trim() === "") {
        throw new ZaloSDKError("Video ID cannot be empty", -1);
      }
      const response = await this.client.apiGet(
        this.endpoints.video.info,
        accessToken,
        { video_id: videoId }
      );
      return response;
    } catch (error) {
      throw this.handleVideoUploadError(error, "Failed to get video info");
    }
  }
  /**
   * Upload video and wait for completion - Combined method
   * @param accessToken OA access token
   * @param file Video file (Buffer or File)
   * @param filename Original filename (optional, will be generated if not provided)
   * @param maxWaitTime Maximum wait time in milliseconds (default: 5 minutes)
   * @param pollInterval Polling interval in milliseconds (default: 5 seconds)
   * @returns Final video status when completed
   */
  async uploadVideoAndWaitForCompletion(accessToken, file, filename, maxWaitTime = 5 * 60 * 1e3, pollInterval = 5 * 1e3) {
    try {
      const uploadResult = await this.uploadVideo(accessToken, file, filename);
      return await this.waitForUploadCompletion(
        accessToken,
        uploadResult.token,
        maxWaitTime,
        pollInterval
      );
    } catch (error) {
      throw this.handleVideoUploadError(
        error,
        "Failed to upload video and wait for completion"
      );
    }
  }
  // ==================== VALIDATION METHODS ====================
  /**
   * Validate video file
   */
  validateVideoFile(file, filename) {
    if (!file) {
      throw new ZaloSDKError("Video file cannot be empty", -1);
    }
    let fileSize;
    let fileName;
    let mimeType;
    if (Buffer.isBuffer(file)) {
      fileSize = file.length;
      fileName = filename || "video.mp4";
      mimeType = this.getMimeTypeFromFilename(fileName);
    } else {
      const fileObj = file;
      fileSize = fileObj.size;
      fileName = fileObj.name || filename || "video.mp4";
      mimeType = fileObj.type || this.getMimeTypeFromFilename(fileName);
    }
    if (fileSize > VIDEO_CONSTRAINTS.maxSize) {
      throw new ZaloSDKError(
        `Video file size exceeds ${VIDEO_CONSTRAINTS.maxSize / (1024 * 1024)}MB limit`,
        -1
      );
    }
    const fileExtension = fileName.toLowerCase().substring(fileName.lastIndexOf("."));
    if (!VIDEO_CONSTRAINTS.allowedExtensions.includes(fileExtension)) {
      throw new ZaloSDKError(
        `Unsupported video format. Only ${VIDEO_CONSTRAINTS.allowedExtensions.join(
          ", "
        )} are supported`,
        -1
      );
    }
    if (!VIDEO_CONSTRAINTS.allowedMimeTypes.includes(mimeType)) {
      throw new ZaloSDKError(
        `Unsupported video MIME type. Only ${VIDEO_CONSTRAINTS.allowedMimeTypes.join(
          ", "
        )} are supported`,
        -1
      );
    }
  }
  // ==================== HELPER METHODS ====================
  getMimeTypeFromFilename(filename) {
    const extension = filename.toLowerCase().substring(filename.lastIndexOf("."));
    switch (extension) {
      case ".mp4":
        return "video/mp4";
      case ".avi":
        return "video/x-msvideo";
      default:
        return "video/mp4";
    }
  }
  extractFilenameFromUrl(url) {
    try {
      const urlObj = new URL(url);
      const pathname = urlObj.pathname;
      const filename = pathname.substring(pathname.lastIndexOf("/") + 1);
      return filename || null;
    } catch {
      return null;
    }
  }
  sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
  handleVideoUploadError(error, defaultMessage) {
    if (error instanceof ZaloSDKError) {
      return error;
    }
    if (error.response?.data) {
      const errorData = error.response.data;
      return new ZaloSDKError(
        `${defaultMessage}: ${errorData.message || errorData.error || "Unknown error"}`,
        errorData.error || -1,
        errorData
      );
    }
    return new ZaloSDKError(
      `${defaultMessage}: ${error.message || "Unknown error"}`,
      -1,
      error
    );
  }
};

// src/services/consultation.service.ts
var ConsultationService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoint for consultation messages
    this.endpoint = "https://openapi.zalo.me/v3.0/oa/message/cs";
  }
  /**
   * Gửi tin nhắn tư vấn văn bản
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param message Nội dung tin nhắn văn bản
   * @returns Thông tin tin nhắn đã gửi
   */
  async sendTextMessage(accessToken, recipient, message) {
    try {
      if (!message.text || message.text.trim().length === 0) {
        throw new ZaloSDKError("N\u1ED9i dung tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (message.text.length > 2e3) {
        throw new ZaloSDKError(
          "N\u1ED9i dung tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 2000 k\xFD t\u1EF1",
          -1
        );
      }
      const request = {
        recipient: {
          user_id: recipient.user_id
        },
        message: {
          text: message.text
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation text message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation text message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn tư vấn trích dẫn (quote message)
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param text Nội dung tin nhắn trả lời (tối đa 2000 ký tự)
   * @param quoteMessageId ID của tin nhắn muốn trích dẫn
   * @returns Thông tin tin nhắn đã gửi với quota information
   */
  async sendQuoteMessage(accessToken, recipient, text, quoteMessageId) {
    try {
      if (!text || text.trim().length === 0) {
        throw new ZaloSDKError("N\u1ED9i dung tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (text.length > 2e3) {
        throw new ZaloSDKError(
          "N\u1ED9i dung tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 2000 k\xFD t\u1EF1",
          -1
        );
      }
      if (!quoteMessageId || quoteMessageId.trim().length === 0) {
        throw new ZaloSDKError("Quote message ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!recipient.user_id || recipient.user_id.trim().length === 0) {
        throw new ZaloSDKError("User ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const request = {
        recipient: {
          user_id: recipient.user_id
        },
        message: {
          text: text.trim(),
          quote_message_id: quoteMessageId.trim()
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation quote message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation quote message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Tạo ConsultationQuoteMessage object
   * @param text Nội dung tin nhắn trả lời
   * @param quoteMessageId ID của tin nhắn muốn trích dẫn
   * @param quoteContent Nội dung của tin nhắn được trích dẫn (optional)
   * @returns ConsultationQuoteMessage object
   */
  createQuoteMessage(text, quoteMessageId, quoteContent) {
    return {
      type: "consultation_quote",
      text: text.trim(),
      quote: {
        message_id: quoteMessageId.trim(),
        content: quoteContent || ""
      }
    };
  }
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
  async sendImageMessage(accessToken, userId, imageUrl, text) {
    try {
      if (!imageUrl || imageUrl.trim().length === 0) {
        throw new ZaloSDKError("URL h\xECnh \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (text && text.length > 2e3) {
        throw new ZaloSDKError(
          "Ti\xEAu \u0111\u1EC1 \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 2000 k\xFD t\u1EF1",
          -1
        );
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          ...text && text.trim().length > 0 && { text: text.trim() },
          attachment: {
            type: "template",
            payload: {
              template_type: "media",
              elements: [
                {
                  media_type: "image",
                  url: imageUrl.trim()
                }
              ]
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation image message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation image message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendImageByAttachmentId(accessToken, userId, attachmentId, text) {
    try {
      if (!attachmentId || attachmentId.trim().length === 0) {
        throw new ZaloSDKError("Attachment ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (text && text.length > 2e3) {
        throw new ZaloSDKError(
          "Ti\xEAu \u0111\u1EC1 \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 2000 k\xFD t\u1EF1",
          -1
        );
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          ...text && text.trim().length > 0 && { text: text.trim() },
          attachment: {
            type: "template",
            payload: {
              template_type: "media",
              elements: [
                {
                  media_type: "image",
                  attachment_id: attachmentId.trim()
                }
              ]
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation image message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation image message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendImage(accessToken, userId, options) {
    try {
      if (!options.imageUrl && !options.attachmentId) {
        throw new ZaloSDKError(
          "Ph\u1EA3i cung c\u1EA5p imageUrl ho\u1EB7c attachmentId",
          -1
        );
      }
      if (options.imageUrl && options.attachmentId) {
        throw new ZaloSDKError(
          "Ch\u1EC9 \u0111\u01B0\u1EE3c s\u1EED d\u1EE5ng imageUrl HO\u1EB6C attachmentId, kh\xF4ng \u0111\u01B0\u1EE3c c\u1EA3 hai",
          -1
        );
      }
      if (options.imageUrl && options.imageUrl.trim().length === 0) {
        throw new ZaloSDKError("URL h\xECnh \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (options.attachmentId && options.attachmentId.trim().length === 0) {
        throw new ZaloSDKError("Attachment ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (options.text && options.text.length > 2e3) {
        throw new ZaloSDKError(
          "Ti\xEAu \u0111\u1EC1 \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 2000 k\xFD t\u1EF1",
          -1
        );
      }
      const element = {
        media_type: "image"
      };
      if (options.imageUrl) {
        element.url = options.imageUrl.trim();
      } else if (options.attachmentId) {
        element.attachment_id = options.attachmentId.trim();
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          ...options.text && options.text.trim().length > 0 && {
            text: options.text.trim()
          },
          attachment: {
            type: "template",
            payload: {
              template_type: "media",
              elements: [element]
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation image message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation image message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendGifMessage(accessToken, userId, gifUrl, width, height, text) {
    try {
      if (!gifUrl || gifUrl.trim().length === 0) {
        throw new ZaloSDKError("URL GIF kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (width <= 0 || height <= 0) {
        throw new ZaloSDKError("Chi\u1EC1u r\u1ED9ng v\xE0 chi\u1EC1u cao ph\u1EA3i l\u1EDBn h\u01A1n 0", -1);
      }
      if (text && text.length > 2e3) {
        throw new ZaloSDKError(
          "Ti\xEAu \u0111\u1EC1 \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 2000 k\xFD t\u1EF1",
          -1
        );
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          ...text && text.trim().length > 0 && { text: text.trim() },
          attachment: {
            type: "template",
            payload: {
              template_type: "media",
              elements: [
                {
                  media_type: "gif",
                  url: gifUrl.trim(),
                  width,
                  height
                }
              ]
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation gif message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation gif message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendFileMessage(accessToken, userId, fileToken) {
    try {
      if (!fileToken || fileToken.trim().length === 0) {
        throw new ZaloSDKError("File token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          attachment: {
            type: "file",
            payload: {
              token: fileToken
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation file message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation file message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendRequestUserInfoMessage(accessToken, userId, title, subtitle, imageUrl) {
    try {
      if (!title || title.trim().length === 0) {
        throw new ZaloSDKError("Ti\xEAu \u0111\u1EC1 kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (title.length > 100) {
        throw new ZaloSDKError("Ti\xEAu \u0111\u1EC1 kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 100 k\xFD t\u1EF1", -1);
      }
      if (!subtitle || subtitle.trim().length === 0) {
        throw new ZaloSDKError("Ti\xEAu \u0111\u1EC1 ph\u1EE5 kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (subtitle.length > 500) {
        throw new ZaloSDKError("Ti\xEAu \u0111\u1EC1 ph\u1EE5 kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 500 k\xFD t\u1EF1", -1);
      }
      if (!imageUrl || imageUrl.trim().length === 0) {
        throw new ZaloSDKError("URL h\xECnh \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          attachment: {
            type: "template",
            payload: {
              template_type: "request_user_info",
              elements: [
                {
                  title,
                  subtitle,
                  image_url: imageUrl
                }
              ]
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send request user info message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send request user info message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendStickerMessage(accessToken, userId, stickerAttachmentId) {
    try {
      if (!stickerAttachmentId || stickerAttachmentId.trim().length === 0) {
        throw new ZaloSDKError("Sticker attachment ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          attachment: {
            type: "template",
            payload: {
              template_type: "media",
              elements: [
                {
                  media_type: "sticker",
                  attachment_id: stickerAttachmentId
                }
              ]
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send consultation sticker message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send consultation sticker message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi chuỗi tin nhắn tư vấn với delay tùy chỉnh
   * Hỗ trợ tất cả các loại tin nhắn: text, image, gif, file, sticker, request_user_info
   *
   * @param request Thông tin request gửi chuỗi tin nhắn
   * @returns Kết quả gửi từng tin nhắn
   */
  async sendMessageSequence(request) {
    const startTime = Date.now();
    const results = [];
    let successCount = 0;
    let failureCount = 0;
    try {
      if (!request.accessToken || request.accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.userId || request.userId.trim().length === 0) {
        throw new ZaloSDKError("User ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.messages || request.messages.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      for (let i = 0; i < request.messages.length; i++) {
        const message = request.messages[i];
        const messageResult = {
          index: i,
          type: message.type,
          success: false,
          timestamp: Date.now()
        };
        try {
          let response;
          switch (message.type) {
            case "text":
              if (!message.text) {
                throw new Error("Text kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng cho text message");
              }
              response = await this.sendTextMessage(
                request.accessToken,
                { user_id: request.userId },
                { type: "text", text: message.text }
              );
              break;
            case "image":
              if (message.imageUrl) {
                response = await this.sendImageMessage(
                  request.accessToken,
                  request.userId,
                  message.imageUrl,
                  message.text
                );
              } else if (message.attachmentId) {
                response = await this.sendImageByAttachmentId(
                  request.accessToken,
                  request.userId,
                  message.attachmentId,
                  message.text
                );
              } else {
                throw new Error("imageUrl ho\u1EB7c attachmentId l\xE0 b\u1EAFt bu\u1ED9c cho image message");
              }
              break;
            case "gif":
              if (!message.gifUrl || !message.width || !message.height) {
                throw new Error("gifUrl, width v\xE0 height l\xE0 b\u1EAFt bu\u1ED9c cho gif message");
              }
              response = await this.sendGifMessage(
                request.accessToken,
                request.userId,
                message.gifUrl,
                message.width,
                message.height,
                message.text
              );
              break;
            case "file":
              if (!message.fileToken) {
                throw new Error("fileToken l\xE0 b\u1EAFt bu\u1ED9c cho file message");
              }
              response = await this.sendFileMessage(
                request.accessToken,
                request.userId,
                message.fileToken
              );
              break;
            case "sticker":
              if (!message.stickerAttachmentId) {
                throw new Error("stickerAttachmentId l\xE0 b\u1EAFt bu\u1ED9c cho sticker message");
              }
              response = await this.sendStickerMessage(
                request.accessToken,
                request.userId,
                message.stickerAttachmentId
              );
              break;
            case "request_user_info":
              if (!message.title || !message.subtitle || !message.imageUrl) {
                throw new Error("title, subtitle v\xE0 imageUrl l\xE0 b\u1EAFt bu\u1ED9c cho request_user_info message");
              }
              response = await this.sendRequestUserInfoMessage(
                request.accessToken,
                request.userId,
                message.title,
                message.subtitle,
                message.imageUrl
              );
              break;
            default:
              throw new Error(`Lo\u1EA1i tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c h\u1ED7 tr\u1EE3: ${message.type}`);
          }
          messageResult.success = true;
          messageResult.response = response;
          successCount++;
        } catch (error) {
          messageResult.success = false;
          messageResult.error = error instanceof Error ? error.message : String(error);
          failureCount++;
        }
        results.push(messageResult);
        if (i < request.messages.length - 1) {
          const delayTime = message.delay ?? request.defaultDelay ?? 0;
          if (delayTime > 0) {
            await this.sleep(delayTime);
          }
        }
      }
      const totalDuration = Date.now() - startTime;
      return {
        successCount,
        failureCount,
        results,
        totalDuration
      };
    } catch (error) {
      const totalDuration = Date.now() - startTime;
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send message sequence: ${error.message}`,
        -1,
        {
          successCount,
          failureCount,
          results,
          totalDuration
        }
      );
    }
  }
  /**
   * Gửi chuỗi tin nhắn tư vấn tới nhiều users với callback tracking
   *
   * @param request Thông tin request gửi chuỗi tin nhắn tới nhiều users
   * @returns Kết quả gửi tin nhắn cho tất cả users
   */
  async sendMessageSequenceToMultipleUsers(request) {
    const startTime = Date.now();
    const userResults = [];
    let successfulUsers = 0;
    let failedUsers = 0;
    let totalSuccessfulMessages = 0;
    let totalFailedMessages = 0;
    try {
      if (!request.accessToken || request.accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.userIds || request.userIds.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch user IDs kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.messages || request.messages.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const uniqueUserIds = [...new Set(request.userIds.filter((id) => id && id.trim().length > 0))];
      if (uniqueUserIds.length === 0) {
        throw new ZaloSDKError("Kh\xF4ng c\xF3 user ID h\u1EE3p l\u1EC7 n\xE0o", -1);
      }
      for (let i = 0; i < uniqueUserIds.length; i++) {
        const userId = uniqueUserIds[i];
        const userStartTime = Date.now();
        if (request.onProgress) {
          request.onProgress({
            userId,
            userIndex: i,
            totalUsers: uniqueUserIds.length,
            status: "started",
            startTime: userStartTime
          });
        }
        const userResult = {
          userId,
          userIndex: i,
          success: false,
          startTime: userStartTime,
          endTime: 0,
          duration: 0
        };
        try {
          const messageSequenceResult = await this.sendMessageSequence({
            accessToken: request.accessToken,
            userId,
            messages: request.messages,
            defaultDelay: request.defaultDelay
          });
          const userEndTime = Date.now();
          userResult.success = true;
          userResult.messageSequenceResult = messageSequenceResult;
          userResult.endTime = userEndTime;
          userResult.duration = userEndTime - userStartTime;
          successfulUsers++;
          totalSuccessfulMessages += messageSequenceResult.successCount;
          totalFailedMessages += messageSequenceResult.failureCount;
          if (request.onProgress) {
            request.onProgress({
              userId,
              userIndex: i,
              totalUsers: uniqueUserIds.length,
              status: "completed",
              result: messageSequenceResult,
              startTime: userStartTime,
              endTime: userEndTime
            });
          }
        } catch (error) {
          const userEndTime = Date.now();
          const errorMessage = error instanceof Error ? error.message : String(error);
          userResult.success = false;
          userResult.error = errorMessage;
          userResult.endTime = userEndTime;
          userResult.duration = userEndTime - userStartTime;
          failedUsers++;
          totalFailedMessages += request.messages.length;
          if (request.onProgress) {
            request.onProgress({
              userId,
              userIndex: i,
              totalUsers: uniqueUserIds.length,
              status: "failed",
              error: errorMessage,
              startTime: userStartTime,
              endTime: userEndTime
            });
          }
        }
        userResults.push(userResult);
        if (i < uniqueUserIds.length - 1 && request.delayBetweenUsers && request.delayBetweenUsers > 0) {
          await this.sleep(request.delayBetweenUsers);
        }
      }
      const totalDuration = Date.now() - startTime;
      const totalMessages = uniqueUserIds.length * request.messages.length;
      return {
        totalUsers: uniqueUserIds.length,
        successfulUsers,
        failedUsers,
        userResults,
        totalDuration,
        messageStats: {
          totalSuccessfulMessages,
          totalFailedMessages,
          totalMessages
        }
      };
    } catch (error) {
      const totalDuration = Date.now() - startTime;
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send message sequence to multiple users: ${error.message}`,
        -1,
        {
          totalUsers: request.userIds?.length || 0,
          successfulUsers,
          failedUsers,
          userResults,
          totalDuration,
          messageStats: {
            totalSuccessfulMessages,
            totalFailedMessages,
            totalMessages: (request.userIds?.length || 0) * (request.messages?.length || 0)
          }
        }
      );
    }
  }
  /**
   * Gửi tin nhắn tư vấn tùy chỉnh tới nhiều users với callback tracking
   * Mỗi user có thể có bộ tin nhắn riêng biệt
   *
   * @param request Thông tin request gửi tin nhắn tùy chỉnh tới nhiều users
   * @returns Kết quả gửi tin nhắn cho tất cả users
   */
  async sendCustomMessageSequenceToMultipleUsers(request) {
    const startTime = Date.now();
    const userResults = [];
    let successfulUsers = 0;
    let failedUsers = 0;
    let totalSuccessfulMessages = 0;
    let totalFailedMessages = 0;
    try {
      if (!request.accessToken || request.accessToken.trim().length === 0) {
        throw new ZaloSDKError("Access token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!request.userCustomMessages || request.userCustomMessages.length === 0) {
        throw new ZaloSDKError("Danh s\xE1ch user custom messages kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const validUserCustomMessages = [];
      const seenUserIds = /* @__PURE__ */ new Set();
      for (let i = 0; i < request.userCustomMessages.length; i++) {
        const userCustomMessage = request.userCustomMessages[i];
        if (!userCustomMessage.userId || userCustomMessage.userId.trim().length === 0) {
          throw new ZaloSDKError(`User custom message ${i + 1}: userId kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        if (seenUserIds.has(userCustomMessage.userId)) {
          throw new ZaloSDKError(`User ID "${userCustomMessage.userId}" b\u1ECB tr\xF9ng l\u1EB7p`, -1);
        }
        seenUserIds.add(userCustomMessage.userId);
        if (!userCustomMessage.messages || userCustomMessage.messages.length === 0) {
          throw new ZaloSDKError(`User custom message ${i + 1}: danh s\xE1ch tin nh\u1EAFn kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        validUserCustomMessages.push(userCustomMessage);
      }
      for (let i = 0; i < validUserCustomMessages.length; i++) {
        const userCustomMessage = validUserCustomMessages[i];
        const userId = userCustomMessage.userId;
        const userStartTime = Date.now();
        if (request.onProgress) {
          request.onProgress({
            userId,
            userIndex: i,
            totalUsers: validUserCustomMessages.length,
            status: "started",
            startTime: userStartTime
          });
        }
        const userResult = {
          userId,
          userIndex: i,
          success: false,
          startTime: userStartTime,
          endTime: 0,
          duration: 0
        };
        try {
          const messageSequenceResult = await this.sendMessageSequence({
            accessToken: request.accessToken,
            userId,
            messages: userCustomMessage.messages,
            defaultDelay: request.defaultDelay
          });
          const userEndTime = Date.now();
          userResult.success = true;
          userResult.messageSequenceResult = messageSequenceResult;
          userResult.endTime = userEndTime;
          userResult.duration = userEndTime - userStartTime;
          successfulUsers++;
          totalSuccessfulMessages += messageSequenceResult.successCount;
          totalFailedMessages += messageSequenceResult.failureCount;
          if (request.onProgress) {
            request.onProgress({
              userId,
              userIndex: i,
              totalUsers: validUserCustomMessages.length,
              status: "completed",
              result: messageSequenceResult,
              startTime: userStartTime,
              endTime: userEndTime
            });
          }
        } catch (error) {
          const userEndTime = Date.now();
          const errorMessage = error instanceof Error ? error.message : String(error);
          userResult.success = false;
          userResult.error = errorMessage;
          userResult.endTime = userEndTime;
          userResult.duration = userEndTime - userStartTime;
          failedUsers++;
          totalFailedMessages += userCustomMessage.messages.length;
          if (request.onProgress) {
            request.onProgress({
              userId,
              userIndex: i,
              totalUsers: validUserCustomMessages.length,
              status: "failed",
              error: errorMessage,
              startTime: userStartTime,
              endTime: userEndTime
            });
          }
        }
        userResults.push(userResult);
        if (i < validUserCustomMessages.length - 1 && request.delayBetweenUsers && request.delayBetweenUsers > 0) {
          await this.sleep(request.delayBetweenUsers);
        }
      }
      const totalDuration = Date.now() - startTime;
      const totalMessages = validUserCustomMessages.reduce((total, userCustomMessage) => {
        return total + userCustomMessage.messages.length;
      }, 0);
      return {
        totalUsers: validUserCustomMessages.length,
        successfulUsers,
        failedUsers,
        userResults,
        totalDuration,
        messageStats: {
          totalSuccessfulMessages,
          totalFailedMessages,
          totalMessages
        }
      };
    } catch (error) {
      const totalDuration = Date.now() - startTime;
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      const totalMessages = request.userCustomMessages?.reduce((total, userCustomMessage) => {
        return total + (userCustomMessage.messages?.length || 0);
      }, 0) || 0;
      throw new ZaloSDKError(
        `Failed to send custom message sequence to multiple users: ${error.message}`,
        -1,
        {
          totalUsers: request.userCustomMessages?.length || 0,
          successfulUsers,
          failedUsers,
          userResults,
          totalDuration,
          messageStats: {
            totalSuccessfulMessages,
            totalFailedMessages,
            totalMessages
          }
        }
      );
    }
  }
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
  async sendTemplateMessage(accessToken, userId, templateType, elements, buttons) {
    try {
      if (!templateType || templateType.trim().length === 0) {
        throw new ZaloSDKError("Template type kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!elements || elements.length === 0) {
        throw new ZaloSDKError("Elements kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (elements.length > 5) {
        throw new ZaloSDKError("Elements kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 5 ph\u1EA7n t\u1EED", -1);
      }
      if (buttons && buttons.length > 5) {
        throw new ZaloSDKError("Buttons kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 5 ph\u1EA7n t\u1EED", -1);
      }
      elements.forEach((element, index) => {
        this.validateTemplateElement(element, index);
      });
      if (buttons) {
        buttons.forEach((button, index) => {
          this.validateTemplateButton(button, index);
        });
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          attachment: {
            type: "template",
            payload: {
              template_type: templateType,
              elements,
              ...buttons && buttons.length > 0 && { buttons }
            }
          }
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send template message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send template message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Validate template element theo quy tắc API
   * @param element Template element cần validate
   * @param index Index của element trong mảng (để báo lỗi)
   */
  validateTemplateElement(element, index) {
    if (!element.title || element.title.trim().length === 0) {
      throw new ZaloSDKError(`Element ${index + 1}: title kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
    }
    if (element.title.length > 100) {
      throw new ZaloSDKError(`Element ${index + 1}: title kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 100 k\xFD t\u1EF1`, -1);
    }
    if (index === 0) {
      if (!element.subtitle || element.subtitle.trim().length === 0) {
        throw new ZaloSDKError("Element \u0111\u1EA7u ti\xEAn ph\u1EA3i c\xF3 subtitle", -1);
      }
    }
    if (element.subtitle && element.subtitle.length > 500) {
      throw new ZaloSDKError(`Element ${index + 1}: subtitle kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 500 k\xFD t\u1EF1`, -1);
    }
    if (element.default_action) {
      this.validateTemplateAction(element.default_action, `Element ${index + 1} default_action`);
    }
  }
  /**
   * Validate template button theo quy tắc API
   * @param button Template button cần validate
   * @param index Index của button trong mảng (để báo lỗi)
   */
  validateTemplateButton(button, index) {
    if (!button.title || button.title.trim().length === 0) {
      throw new ZaloSDKError(`Button ${index + 1}: title kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
    }
    if (button.title.length > 100) {
      throw new ZaloSDKError(`Button ${index + 1}: title kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 100 k\xFD t\u1EF1`, -1);
    }
    if (!button.type || button.type.trim().length === 0) {
      throw new ZaloSDKError(`Button ${index + 1}: type kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
    }
    this.validateButtonPayload(button, index);
  }
  /**
   * Validate template action (cho default_action)
   * @param action Template action cần validate
   * @param context Context để báo lỗi
   */
  validateTemplateAction(action, context) {
    if (!action.type || action.type.trim().length === 0) {
      throw new ZaloSDKError(`${context}: type kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
    }
    const validActionTypes = ["oa.open.url", "oa.query.show", "oa.query.hide", "oa.open.sms", "oa.open.phone"];
    if (!validActionTypes.includes(action.type)) {
      throw new ZaloSDKError(`${context}: type ph\u1EA3i l\xE0 m\u1ED9t trong ${validActionTypes.join(", ")}`, -1);
    }
    switch (action.type) {
      case "oa.open.url":
        if (!action.url || action.url.trim().length === 0) {
          throw new ZaloSDKError(`${context}: url kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng cho type oa.open.url`, -1);
        }
        break;
      case "oa.query.show":
      case "oa.query.hide":
        if (typeof action.payload !== "string") {
          throw new ZaloSDKError(`${context}: payload ph\u1EA3i l\xE0 string cho type ${action.type}`, -1);
        }
        if (!action.payload || action.payload.trim().length === 0) {
          throw new ZaloSDKError(`${context}: payload kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng cho type ${action.type}`, -1);
        }
        if (action.payload.length > 1e3) {
          throw new ZaloSDKError(`${context}: payload kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 1000 k\xFD t\u1EF1`, -1);
        }
        break;
      case "oa.open.sms":
        if (!action.payload || typeof action.payload !== "object") {
          throw new ZaloSDKError(`${context}: payload ph\u1EA3i l\xE0 object cho type oa.open.sms`, -1);
        }
        const smsPayload = action.payload;
        if (!smsPayload.phone_code || smsPayload.phone_code.trim().length === 0) {
          throw new ZaloSDKError(`${context}: payload.phone_code kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        if (smsPayload.content && smsPayload.content.length > 160) {
          throw new ZaloSDKError(`${context}: payload.content kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 160 k\xFD t\u1EF1`, -1);
        }
        break;
      case "oa.open.phone":
        if (!action.payload || typeof action.payload !== "object") {
          throw new ZaloSDKError(`${context}: payload ph\u1EA3i l\xE0 object cho type oa.open.phone`, -1);
        }
        const phonePayload = action.payload;
        if (!phonePayload.phone_code || phonePayload.phone_code.trim().length === 0) {
          throw new ZaloSDKError(`${context}: payload.phone_code kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        break;
    }
  }
  /**
   * Validate button payload theo type
   * @param button Template button cần validate
   * @param index Index của button trong mảng (để báo lỗi)
   */
  validateButtonPayload(button, index) {
    const context = `Button ${index + 1}`;
    switch (button.type) {
      case "oa.open.url":
        if (!button.payload || typeof button.payload !== "object") {
          throw new ZaloSDKError(`${context}: payload ph\u1EA3i l\xE0 object cho type oa.open.url`, -1);
        }
        const urlPayload = button.payload;
        if (!urlPayload.url || urlPayload.url.trim().length === 0) {
          throw new ZaloSDKError(`${context}: payload.url kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        break;
      case "oa.query.show":
      case "oa.query.hide":
        if (typeof button.payload !== "string") {
          throw new ZaloSDKError(`${context}: payload ph\u1EA3i l\xE0 string cho type ${button.type}`, -1);
        }
        if (!button.payload || button.payload.trim().length === 0) {
          throw new ZaloSDKError(`${context}: payload kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        if (button.payload.length > 1e3) {
          throw new ZaloSDKError(`${context}: payload kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 1000 k\xFD t\u1EF1`, -1);
        }
        break;
      case "oa.open.sms":
        if (!button.payload || typeof button.payload !== "object") {
          throw new ZaloSDKError(`${context}: payload ph\u1EA3i l\xE0 object cho type oa.open.sms`, -1);
        }
        const smsPayload = button.payload;
        if (!smsPayload.phone_code || smsPayload.phone_code.trim().length === 0) {
          throw new ZaloSDKError(`${context}: payload.phone_code kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        if (smsPayload.content && smsPayload.content.length > 160) {
          throw new ZaloSDKError(`${context}: payload.content kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 160 k\xFD t\u1EF1`, -1);
        }
        break;
      case "oa.open.phone":
        if (!button.payload || typeof button.payload !== "object") {
          throw new ZaloSDKError(`${context}: payload ph\u1EA3i l\xE0 object cho type oa.open.phone`, -1);
        }
        const phonePayload = button.payload;
        if (!phonePayload.phone_code || phonePayload.phone_code.trim().length === 0) {
          throw new ZaloSDKError(`${context}: payload.phone_code kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng`, -1);
        }
        break;
      default:
        const validButtonTypes = ["oa.open.url", "oa.query.show", "oa.query.hide", "oa.open.sms", "oa.open.phone"];
        throw new ZaloSDKError(`${context}: type ph\u1EA3i l\xE0 m\u1ED9t trong ${validButtonTypes.join(", ")}`, -1);
    }
  }
  /**
   * Helper function để tạo template element
   * @param title Tiêu đề (bắt buộc, ≤ 100 ký tự)
   * @param subtitle Tiêu đề phụ (≤ 500 ký tự)
   * @param imageUrl URL ảnh (optional)
   * @param defaultAction Hành động khi click (optional)
   * @returns TemplateElement object
   */
  createTemplateElement(title, subtitle, imageUrl, defaultAction) {
    return {
      title,
      ...subtitle && { subtitle },
      ...imageUrl && { image_url: imageUrl },
      ...defaultAction && { default_action: defaultAction }
    };
  }
  /**
   * Helper function để tạo template button với URL
   * @param title Tiêu đề button (≤ 100 ký tự)
   * @param url URL để mở
   * @returns TemplateButton object
   */
  createUrlButton(title, url) {
    return {
      title,
      type: "oa.open.url",
      payload: { url }
    };
  }
  /**
   * Helper function để tạo template button với query (hiển thị)
   * @param title Tiêu đề button (≤ 100 ký tự)
   * @param payload Dữ liệu callback (≤ 1000 ký tự)
   * @returns TemplateButton object
   */
  createQueryShowButton(title, payload) {
    return {
      title,
      type: "oa.query.show",
      payload
    };
  }
  /**
   * Helper function để tạo template button với query (ẩn)
   * @param title Tiêu đề button (≤ 100 ký tự)
   * @param payload Dữ liệu callback (≤ 1000 ký tự)
   * @returns TemplateButton object
   */
  createQueryHideButton(title, payload) {
    return {
      title,
      type: "oa.query.hide",
      payload
    };
  }
  /**
   * Helper function để tạo template button với SMS
   * @param title Tiêu đề button (≤ 100 ký tự)
   * @param phoneCode Số điện thoại
   * @param content Nội dung SMS (≤ 160 ký tự)
   * @returns TemplateButton object
   */
  createSmsButton(title, phoneCode, content) {
    return {
      title,
      type: "oa.open.sms",
      payload: {
        phone_code: phoneCode,
        ...content && { content }
      }
    };
  }
  /**
   * Helper function để tạo template button với Phone
   * @param title Tiêu đề button (≤ 100 ký tự)
   * @param phoneCode Số điện thoại
   * @returns TemplateButton object
   */
  createPhoneButton(title, phoneCode) {
    return {
      title,
      type: "oa.open.phone",
      payload: { phone_code: phoneCode }
    };
  }
  /**
   * Helper function để tạo template action với URL
   * @param url URL để mở
   * @returns TemplateAction object
   */
  createUrlAction(url) {
    return {
      type: "oa.open.url",
      url
    };
  }
  /**
   * Helper function để tạo template action với query (hiển thị)
   * @param payload Dữ liệu callback (≤ 1000 ký tự)
   * @returns TemplateAction object
   */
  createQueryShowAction(payload) {
    return {
      type: "oa.query.show",
      payload
    };
  }
  /**
   * Helper function để tạo template action với query (ẩn)
   * @param payload Dữ liệu callback (≤ 1000 ký tự)
   * @returns TemplateAction object
   */
  createQueryHideAction(payload) {
    return {
      type: "oa.query.hide",
      payload
    };
  }
  /**
   * Helper function để tạo template action với SMS
   * @param phoneCode Số điện thoại
   * @param content Nội dung SMS (≤ 160 ký tự)
   * @returns TemplateAction object
   */
  createSmsAction(phoneCode, content) {
    return {
      type: "oa.open.sms",
      payload: {
        phone_code: phoneCode,
        ...content && { content }
      }
    };
  }
  /**
   * Helper function để tạo template action với Phone
   * @param phoneCode Số điện thoại
   * @returns TemplateAction object
   */
  createPhoneAction(phoneCode) {
    return {
      type: "oa.open.phone",
      payload: { phone_code: phoneCode }
    };
  }
  /**
   * Utility method để sleep/delay
   * @param ms Thời gian delay tính bằng milliseconds
   */
  sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
};

// src/services/transaction.service.ts
var TransactionService = class {
  constructor(client) {
    this.client = client;
    this.transactionApiUrl = "https://openapi.zalo.me/v2.0/oa/message/transaction";
  }
  /**
   * Gửi tin nhắn giao dịch
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param message Nội dung tin nhắn giao dịch
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendTransactionMessage(accessToken, recipient, message) {
    try {
      this.validateTransactionMessage(message);
      const request = {
        recipient,
        message
      };
      const result = await this.client.apiPost(this.transactionApiUrl, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send transaction message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send transaction message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn xác nhận đơn hàng
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param orderInfo Thông tin đơn hàng
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendOrderConfirmation(accessToken, recipient, orderInfo) {
    try {
      const message = {
        type: "transaction",
        attachment: {
          type: "template",
          payload: {
            template_type: "transaction",
            elements: [
              {
                title: `X\xE1c nh\u1EADn \u0111\u01A1n h\xE0ng #${orderInfo.orderId}`,
                subtitle: `Ng\xE0y \u0111\u1EB7t: ${orderInfo.orderDate}`,
                default_action: {
                  type: "web_url",
                  url: `https://example.com/orders/${orderInfo.orderId}`
                },
                buttons: [
                  {
                    type: "web_url",
                    title: "Xem chi ti\u1EBFt",
                    url: `https://example.com/orders/${orderInfo.orderId}`
                  }
                ]
              }
            ]
          }
        }
      };
      return this.sendTransactionMessage(accessToken, recipient, message);
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send order confirmation: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn thông báo thanh toán
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param paymentInfo Thông tin thanh toán
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendPaymentNotification(accessToken, recipient, paymentInfo) {
    try {
      const statusText = {
        success: "Th\xE0nh c\xF4ng",
        failed: "Th\u1EA5t b\u1EA1i",
        pending: "\u0110ang x\u1EED l\xFD"
      };
      const message = {
        type: "transaction",
        attachment: {
          type: "template",
          payload: {
            template_type: "transaction",
            elements: [
              {
                title: `Th\xF4ng b\xE1o thanh to\xE1n - ${statusText[paymentInfo.status]}`,
                subtitle: `\u0110\u01A1n h\xE0ng #${paymentInfo.orderId} - ${paymentInfo.amount.toLocaleString("vi-VN")} VN\u0110`,
                default_action: {
                  type: "web_url",
                  url: `https://example.com/payments/${paymentInfo.orderId}`
                },
                buttons: [
                  {
                    type: "web_url",
                    title: "Xem chi ti\u1EBFt",
                    url: `https://example.com/payments/${paymentInfo.orderId}`
                  }
                ]
              }
            ]
          }
        }
      };
      return this.sendTransactionMessage(accessToken, recipient, message);
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send payment notification: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn cập nhật trạng thái giao hàng
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param shippingInfo Thông tin giao hàng
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendShippingUpdate(accessToken, recipient, shippingInfo) {
    try {
      const statusText = {
        preparing: "\u0110ang chu\u1EA9n b\u1ECB h\xE0ng",
        shipped: "\u0110\xE3 giao cho \u0111\u01A1n v\u1ECB v\u1EADn chuy\u1EC3n",
        in_transit: "\u0110ang v\u1EADn chuy\u1EC3n",
        delivered: "\u0110\xE3 giao h\xE0ng th\xE0nh c\xF4ng"
      };
      const message = {
        type: "transaction",
        attachment: {
          type: "template",
          payload: {
            template_type: "transaction",
            elements: [
              {
                title: `C\u1EADp nh\u1EADt giao h\xE0ng - ${statusText[shippingInfo.status]}`,
                subtitle: `\u0110\u01A1n h\xE0ng #${shippingInfo.orderId} - M\xE3 v\u1EADn \u0111\u01A1n: ${shippingInfo.trackingNumber}`,
                default_action: {
                  type: "web_url",
                  url: `https://example.com/tracking/${shippingInfo.trackingNumber}`
                },
                buttons: [
                  {
                    type: "web_url",
                    title: "Theo d\xF5i \u0111\u01A1n h\xE0ng",
                    url: `https://example.com/tracking/${shippingInfo.trackingNumber}`
                  }
                ]
              }
            ]
          }
        }
      };
      return this.sendTransactionMessage(accessToken, recipient, message);
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send shipping update: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Validate transaction message format
   * @param message Transaction message to validate
   */
  validateTransactionMessage(message) {
    if (!message.attachment) {
      throw new ZaloSDKError("Transaction message must have attachment", -1);
    }
    if (!message.attachment.payload) {
      throw new ZaloSDKError(
        "Transaction message attachment must have payload",
        -1
      );
    }
    if (message.attachment.payload.template_type !== "transaction") {
      throw new ZaloSDKError(
        "Transaction message must use transaction template type",
        -1
      );
    }
  }
};

// src/services/promotion.service.ts
var PromotionService = class {
  constructor(client) {
    this.client = client;
    this.promotionApiUrl = "https://openapi.zalo.me/v3.0/oa/message/promotion";
  }
  /**
   * Convert input button to proper typed button for API
   * @param button Input button with flexible typing
   * @returns Properly typed button for API
   */
  convertToTypedButton(button) {
    const baseButton = {
      title: button.title,
      image_icon: button.imageIcon || "",
      type: button.type
    };
    switch (button.type) {
      case "oa.open.url":
        return {
          ...baseButton,
          payload: { url: button.payload.url }
        };
      case "oa.query.show":
      case "oa.query.hide":
        return {
          ...baseButton,
          payload: button.payload
          // Already string type
        };
      case "oa.open.sms":
        return {
          ...baseButton,
          payload: {
            content: button.payload.content,
            phone_code: button.payload.phone_code || button.payload.phoneCode || ""
          }
        };
      case "oa.open.phone":
        return {
          ...baseButton,
          payload: typeof button.payload === "string" ? { phone_code: button.payload } : { phone_code: button.payload.phone_code || button.payload.phoneCode || "" }
        };
      default:
        return {
          ...baseButton,
          payload: {}
        };
    }
  }
  /**
   * Gửi tin nhắn truyền thông/quảng cáo
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param message Nội dung tin nhắn truyền thông
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendPromotionMessage(accessToken, recipient, message) {
    try {
      this.validatePromotionMessage(message);
      this.validateSendingTime();
      const request = {
        recipient,
        message
      };
      const result = await this.client.apiPost(this.promotionApiUrl, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send promotion message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send promotion message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn khuyến mãi sản phẩm
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param promotionInfo Thông tin khuyến mãi
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendProductPromotion(accessToken, recipient, promotionInfo) {
    try {
      const message = {
        type: "promotion",
        attachment: {
          type: "template",
          payload: {
            template_type: "promotion",
            language: "VI",
            elements: [
              // Banner element
              {
                type: "banner",
                ...promotionInfo.attachmentId ? { attachment_id: promotionInfo.attachmentId } : { image_url: promotionInfo.imageUrl }
              },
              // Header element
              {
                type: "header",
                content: `\u{1F4A5}\u{1F4A5} ${promotionInfo.title} \u{1F4A5}\u{1F4A5}`,
                align: "center"
              },
              // Description text
              {
                type: "text",
                content: promotionInfo.description,
                align: "left"
              },
              // Price table
              {
                type: "table",
                content: [
                  {
                    key: "Gi\xE1 g\u1ED1c",
                    value: `${promotionInfo.originalPrice.toLocaleString("vi-VN")}\u0111`
                  },
                  {
                    key: "Gi\xE1 khuy\u1EBFn m\xE3i",
                    value: `${promotionInfo.discountPrice.toLocaleString("vi-VN")}\u0111`
                  },
                  {
                    key: "Gi\u1EA3m gi\xE1",
                    value: `${promotionInfo.discountPercent}%`
                  },
                  {
                    key: "H\u1EA1n s\u1EED d\u1EE5ng",
                    value: promotionInfo.validUntil
                  }
                ]
              },
              // Footer text
              {
                type: "text",
                content: "\u{1F6D2} Nhanh tay \u0111\u1EB7t h\xE0ng \u0111\u1EC3 kh\xF4ng b\u1ECF l\u1EE1 \u01B0u \u0111\xE3i!",
                align: "center"
              }
            ],
            buttons: [
              {
                title: "Mua ngay",
                image_icon: "",
                type: "oa.open.url",
                payload: {
                  url: promotionInfo.productUrl
                }
              },
              {
                title: "Xem th\xEAm s\u1EA3n ph\u1EA9m",
                image_icon: "",
                type: "oa.query.hide",
                payload: "#xemthem"
              }
            ]
          }
        }
      };
      return this.sendPromotionMessage(accessToken, recipient, message);
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send product promotion: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn thông báo sự kiện
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param eventInfo Thông tin sự kiện
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendEventNotification(accessToken, recipient, eventInfo) {
    try {
      const message = {
        type: "promotion",
        attachment: {
          type: "template",
          payload: {
            template_type: "promotion",
            language: "VI",
            elements: [
              // Banner element
              {
                type: "banner",
                ...eventInfo.attachmentId ? { attachment_id: eventInfo.attachmentId } : { image_url: eventInfo.imageUrl }
              },
              // Header element
              {
                type: "header",
                content: `\u{1F389} ${eventInfo.title} \u{1F389}`,
                align: "center"
              },
              // Description text
              {
                type: "text",
                content: eventInfo.description,
                align: "left"
              },
              // Event details table
              {
                type: "table",
                content: [
                  {
                    key: "Th\u1EDDi gian",
                    value: eventInfo.eventDate
                  },
                  {
                    key: "\u0110\u1ECBa \u0111i\u1EC3m",
                    value: eventInfo.location
                  }
                ]
              },
              // Call to action text
              {
                type: "text",
                content: "\u{1F3AB} \u0110\u0103ng k\xFD ngay \u0111\u1EC3 kh\xF4ng b\u1ECF l\u1EE1 s\u1EF1 ki\u1EC7n!",
                align: "center"
              }
            ],
            buttons: [
              {
                title: "\u0110\u0103ng k\xFD tham gia",
                image_icon: "",
                type: "oa.open.url",
                payload: {
                  url: eventInfo.registrationUrl
                }
              },
              {
                title: "Chia s\u1EBB s\u1EF1 ki\u1EC7n",
                image_icon: "",
                type: "oa.query.hide",
                payload: "#chiase"
              }
            ]
          }
        }
      };
      return this.sendPromotionMessage(accessToken, recipient, message);
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send event notification: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn newsletter
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param newsletterInfo Thông tin newsletter
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendNewsletter(accessToken, recipient, newsletterInfo) {
    try {
      const message = {
        type: "promotion",
        attachment: {
          type: "template",
          payload: {
            template_type: "promotion",
            language: "VI",
            elements: [
              // Banner element
              {
                type: "banner",
                ...newsletterInfo.attachmentId ? { attachment_id: newsletterInfo.attachmentId } : { image_url: newsletterInfo.imageUrl }
              },
              // Header element
              {
                type: "header",
                content: `\u{1F4F0} ${newsletterInfo.title}`,
                align: "center"
              },
              // Summary text
              {
                type: "text",
                content: newsletterInfo.summary,
                align: "left"
              },
              // Articles table
              {
                type: "table",
                content: newsletterInfo.articles.slice(0, 5).map((article, index) => ({
                  key: `B\xE0i vi\u1EBFt ${index + 1}`,
                  value: article.title
                }))
              },
              // Footer text
              {
                type: "text",
                content: "\u{1F4D6} \u0110\u1ECDc ngay \u0111\u1EC3 c\u1EADp nh\u1EADt th\xF4ng tin m\u1EDBi nh\u1EA5t!",
                align: "center"
              }
            ],
            buttons: [
              {
                title: "\u0110\u1ECDc to\xE0n b\u1ED9",
                image_icon: "",
                type: "oa.open.url",
                payload: {
                  url: newsletterInfo.articles[0]?.url || "#"
                }
              },
              {
                title: "H\u1EE7y \u0111\u0103ng k\xFD",
                image_icon: "",
                type: "oa.open.url",
                payload: {
                  url: newsletterInfo.unsubscribeUrl
                }
              }
            ]
          }
        }
      };
      return this.sendPromotionMessage(accessToken, recipient, message);
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send newsletter: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Tạo promotion message theo format chuẩn v3.0 (như trong docs)
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param promotionData Dữ liệu promotion
   * @returns Thông tin tin nhắn đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendCustomPromotion(accessToken, recipient, promotionData) {
    try {
      this.validateBannerConfig(promotionData.banner);
      const elements = [
        // Banner element
        {
          type: "banner",
          ...promotionData.banner
        },
        // Header element
        {
          type: "header",
          content: promotionData.headerContent
        },
        // Text element
        {
          type: "text",
          align: "left",
          content: promotionData.textContent
        }
      ];
      if (promotionData.tableData && promotionData.tableData.length > 0) {
        elements.push({
          type: "table",
          content: promotionData.tableData
        });
      }
      if (promotionData.footerText) {
        elements.push({
          type: "text",
          align: "center",
          content: promotionData.footerText
        });
      }
      const message = {
        type: "promotion",
        attachment: {
          type: "template",
          payload: {
            template_type: "promotion",
            language: promotionData.language || "VI",
            elements,
            buttons: promotionData.buttons.map((btn) => this.convertToTypedButton(btn))
          }
        }
      };
      return this.sendPromotionMessage(accessToken, recipient, message);
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send custom promotion: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Validate banner configuration
   * @param banner Banner configuration to validate
   */
  validateBannerConfig(banner) {
    if (!banner.image_url && !banner.attachment_id) {
      throw new ZaloSDKError(
        "Banner must have either image_url or attachment_id",
        -1
      );
    }
    if (banner.image_url && banner.attachment_id) {
      throw new ZaloSDKError(
        "Banner cannot have both image_url and attachment_id. Use only one.",
        -1
      );
    }
  }
  /**
   * Validate promotion message format
   * @param message Promotion message to validate
   */
  validatePromotionMessage(message) {
    if (!message.attachment) {
      throw new ZaloSDKError("Promotion message must have attachment", -1);
    }
    if (!message.attachment.payload) {
      throw new ZaloSDKError(
        "Promotion message attachment must have payload",
        -1
      );
    }
    if (message.attachment.payload.template_type !== "promotion") {
      throw new ZaloSDKError(
        "Promotion message must use promotion template type",
        -1
      );
    }
    if (!message.attachment.payload.elements || message.attachment.payload.elements.length === 0) {
      throw new ZaloSDKError(
        "Promotion message must have at least one element",
        -1
      );
    }
  }
  /**
   * Gửi promotion message đến nhiều user_ids với callback tracking
   * @param accessToken Access token của Official Account
   * @param userIds Danh sách user IDs cần gửi
   * @param promotionData Dữ liệu promotion
   * @param options Tùy chọn gửi và tracking
   * @returns Kết quả gửi đến tất cả users
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendCustomPromotionToMultipleUsers(accessToken, userIds, promotionData, options) {
    try {
      if (!userIds || userIds.length === 0) {
        throw new ZaloSDKError("User IDs array cannot be empty", -1);
      }
      const uniqueUserIds = [...new Set(userIds)];
      this.validateSendingTime();
      const startTime = Date.now();
      const results = [];
      const mode = options?.mode || "sequential";
      const delayMs = options?.delayMs || 1e3;
      const continueOnError = options?.continueOnError !== false;
      if (options?.onProgress) {
        options.onProgress({
          total: uniqueUserIds.length,
          completed: 0,
          successful: 0,
          failed: 0,
          currentUserId: null,
          startTime,
          estimatedTimeRemaining: null
        });
      }
      if (mode === "parallel") {
        const promises = uniqueUserIds.map(async (userId) => {
          try {
            const result = await this.sendCustomPromotion(
              accessToken,
              { user_id: userId },
              promotionData
            );
            const userResult = {
              userId,
              success: true,
              result,
              error: null,
              timestamp: /* @__PURE__ */ new Date()
            };
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            return userResult;
          } catch (error) {
            const userResult = {
              userId,
              success: false,
              result: null,
              error: error instanceof ZaloSDKError ? error : new ZaloSDKError(
                `Failed to send to user ${userId}: ${error.message}`,
                -1,
                error
              ),
              timestamp: /* @__PURE__ */ new Date()
            };
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            if (!continueOnError) {
              throw userResult.error;
            }
            return userResult;
          }
        });
        const parallelResults = await Promise.all(promises);
        results.push(...parallelResults);
      } else {
        for (let i = 0; i < uniqueUserIds.length; i++) {
          const userId = uniqueUserIds[i];
          try {
            if (options?.onProgress) {
              const completed = i;
              const successful2 = results.filter((r) => r.success).length;
              const failed2 = results.filter((r) => !r.success).length;
              const elapsed = Date.now() - startTime;
              const avgTimePerUser = elapsed / Math.max(completed, 1);
              const remaining = uniqueUserIds.length - completed;
              const estimatedTimeRemaining = remaining * avgTimePerUser;
              options.onProgress({
                total: uniqueUserIds.length,
                completed,
                successful: successful2,
                failed: failed2,
                currentUserId: userId,
                startTime,
                estimatedTimeRemaining
              });
            }
            const result = await this.sendCustomPromotion(
              accessToken,
              { user_id: userId },
              promotionData
            );
            const userResult = {
              userId,
              success: true,
              result,
              error: null,
              timestamp: /* @__PURE__ */ new Date()
            };
            results.push(userResult);
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            if (i < uniqueUserIds.length - 1 && delayMs > 0) {
              await new Promise((resolve) => setTimeout(resolve, delayMs));
            }
          } catch (error) {
            const userResult = {
              userId,
              success: false,
              result: null,
              error: error instanceof ZaloSDKError ? error : new ZaloSDKError(
                `Failed to send to user ${userId}: ${error.message}`,
                -1,
                error
              ),
              timestamp: /* @__PURE__ */ new Date()
            };
            results.push(userResult);
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            if (!continueOnError) {
              throw userResult.error;
            }
          }
        }
      }
      const endTime = Date.now();
      const successful = results.filter((r) => r.success);
      const failed = results.filter((r) => !r.success);
      if (options?.onProgress) {
        options.onProgress({
          total: uniqueUserIds.length,
          completed: uniqueUserIds.length,
          successful: successful.length,
          failed: failed.length,
          currentUserId: null,
          startTime,
          estimatedTimeRemaining: 0
        });
      }
      return {
        total: uniqueUserIds.length,
        successful: successful.length,
        failed: failed.length,
        results,
        executionTime: endTime - startTime,
        mode,
        startTime: new Date(startTime),
        endTime: new Date(endTime),
        successRate: successful.length / uniqueUserIds.length * 100
      };
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send promotion to multiple users: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi promotion message đến nhiều user_ids với promotionData riêng cho từng user
   * @param accessToken Access token của Official Account
   * @param userPromotions Danh sách user và promotionData tương ứng
   * @param options Tùy chọn gửi và tracking
   * @returns Kết quả gửi đến tất cả users
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendPersonalizedPromotionToMultipleUsers(accessToken, userPromotions, options) {
    try {
      if (!userPromotions || userPromotions.length === 0) {
        throw new ZaloSDKError("User promotions array cannot be empty", -1);
      }
      const userIds = userPromotions.map((up) => up.userId);
      const uniqueUserIds = [...new Set(userIds)];
      if (uniqueUserIds.length !== userPromotions.length) {
        throw new ZaloSDKError("Duplicate user IDs found in user promotions", -1);
      }
      this.validateSendingTime();
      const startTime = Date.now();
      const results = [];
      const mode = options?.mode || "sequential";
      const delayMs = options?.delayMs || 1e3;
      const continueOnError = options?.continueOnError !== false;
      if (options?.onProgress) {
        options.onProgress({
          total: uniqueUserIds.length,
          completed: 0,
          successful: 0,
          failed: 0,
          currentUserId: null,
          startTime,
          estimatedTimeRemaining: null
        });
      }
      if (mode === "parallel") {
        const promises = userPromotions.map(async (userPromotion) => {
          try {
            const result = await this.sendCustomPromotion(
              accessToken,
              { user_id: userPromotion.userId },
              userPromotion.promotionData
            );
            const userResult = {
              userId: userPromotion.userId,
              success: true,
              result,
              error: null,
              timestamp: /* @__PURE__ */ new Date()
            };
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            return userResult;
          } catch (error) {
            const userResult = {
              userId: userPromotion.userId,
              success: false,
              result: null,
              error: error instanceof ZaloSDKError ? error : new ZaloSDKError(
                `Failed to send to user ${userPromotion.userId}: ${error.message}`,
                -1,
                error
              ),
              timestamp: /* @__PURE__ */ new Date()
            };
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            if (!continueOnError) {
              throw userResult.error;
            }
            return userResult;
          }
        });
        const parallelResults = await Promise.all(promises);
        results.push(...parallelResults);
      } else {
        for (let i = 0; i < userPromotions.length; i++) {
          const userPromotion = userPromotions[i];
          try {
            if (options?.onProgress) {
              const completed = i;
              const successful2 = results.filter((r) => r.success).length;
              const failed2 = results.filter((r) => !r.success).length;
              const elapsed = Date.now() - startTime;
              const avgTimePerUser = elapsed / Math.max(completed, 1);
              const remaining = userPromotions.length - completed;
              const estimatedTimeRemaining = remaining * avgTimePerUser;
              options.onProgress({
                total: userPromotions.length,
                completed,
                successful: successful2,
                failed: failed2,
                currentUserId: userPromotion.userId,
                startTime,
                estimatedTimeRemaining
              });
            }
            const result = await this.sendCustomPromotion(
              accessToken,
              { user_id: userPromotion.userId },
              userPromotion.promotionData
            );
            const userResult = {
              userId: userPromotion.userId,
              success: true,
              result,
              error: null,
              timestamp: /* @__PURE__ */ new Date()
            };
            results.push(userResult);
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            if (i < userPromotions.length - 1 && delayMs > 0) {
              await new Promise((resolve) => setTimeout(resolve, delayMs));
            }
          } catch (error) {
            const userResult = {
              userId: userPromotion.userId,
              success: false,
              result: null,
              error: error instanceof ZaloSDKError ? error : new ZaloSDKError(
                `Failed to send to user ${userPromotion.userId}: ${error.message}`,
                -1,
                error
              ),
              timestamp: /* @__PURE__ */ new Date()
            };
            results.push(userResult);
            if (options?.onUserComplete) {
              options.onUserComplete(userResult);
            }
            if (!continueOnError) {
              throw userResult.error;
            }
          }
        }
      }
      const endTime = Date.now();
      const successful = results.filter((r) => r.success);
      const failed = results.filter((r) => !r.success);
      if (options?.onProgress) {
        options.onProgress({
          total: userPromotions.length,
          completed: userPromotions.length,
          successful: successful.length,
          failed: failed.length,
          currentUserId: null,
          startTime,
          estimatedTimeRemaining: 0
        });
      }
      return {
        total: userPromotions.length,
        successful: successful.length,
        failed: failed.length,
        results,
        executionTime: endTime - startTime,
        mode,
        startTime: new Date(startTime),
        endTime: new Date(endTime),
        successRate: successful.length / userPromotions.length * 100
      };
    } catch (error) {
      throw new ZaloSDKError(
        `Failed to send personalized promotion to multiple users: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Validate sending time (8:00 - 22:00)
   */
  validateSendingTime() {
    const now = /* @__PURE__ */ new Date();
    const hour = now.getHours();
    if (hour < 8 || hour >= 22) {
      throw new ZaloSDKError(
        "Tin nh\u1EAFn truy\u1EC1n th\xF4ng ch\u1EC9 \u0111\u01B0\u1EE3c g\u1EEDi trong khung gi\u1EDD t\u1EEB 8:00 - 22:00",
        2001
      );
    }
  }
};

// src/services/general-message.service.ts
var GeneralMessageService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      // Message endpoints
      message: {
        send: "https://openapi.zalo.me/v3.0/oa/message/cs",
        sendAnonymous: "https://openapi.zalo.me/v3.0/oa/message/anonymous"
      }
    };
  }
  /**
   * Gửi tin nhắn phản hồi hình ảnh
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param message Nội dung tin nhắn hình ảnh
   * @returns Thông tin tin nhắn đã gửi
   */
  async sendResponseImageMessage(accessToken, recipient, message) {
    try {
      if (!message.attachment?.payload?.url) {
        throw new ZaloSDKError("URL h\xECnh \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const endpoint = this.endpoints.message.send;
      const request = {
        recipient,
        message,
        messaging_type: "response"
      };
      const result = await this.client.apiPost(endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send response image message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send response image message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn ẩn danh
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param message Nội dung tin nhắn ẩn danh
   * @returns Thông tin tin nhắn đã gửi
   */
  async sendAnonymousMessage(accessToken, recipient, message) {
    try {
      if (!message.text || message.text.trim().length === 0) {
        throw new ZaloSDKError(
          "N\u1ED9i dung tin nh\u1EAFn \u1EA9n danh kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng",
          -1
        );
      }
      const endpoint = this.endpoints.message.sendAnonymous;
      const request = {
        recipient,
        message
      };
      const result = await this.client.apiPost(endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send anonymous message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send anonymous message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Gửi tin nhắn tổng quát
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin người nhận
   * @param message Nội dung tin nhắn
   * @param messagingType Loại tin nhắn (response, update, message_tag)
   * @returns Thông tin tin nhắn đã gửi
   */
  async sendMessage(accessToken, recipient, message, messagingType = "response") {
    try {
      const endpoint = this.endpoints.message.send;
      const request = {
        recipient,
        message,
        messaging_type: messagingType
      };
      const result = await this.client.apiPost(endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send message: ${error.message}`,
        -1,
        error
      );
    }
  }
};

// src/services/message-management.service.ts
var MessageManagementService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoints - organized by functionality
    this.endpoints = {
      // Message management endpoints
      message: {
        quota: "https://openapi.zalo.me/v2.0/oa/quota",
        conversation: "https://openapi.zalo.me/v2.0/oa/conversation",
        recentConversations: "https://openapi.zalo.me/v2.0/oa/listrecentchat"
      },
      // Upload endpoints
      upload: {
        file: "https://openapi.zalo.me/v2.0/oa/upload/file",
        image: "https://openapi.zalo.me/v2.0/oa/upload/image",
        gif: "https://openapi.zalo.me/v2.0/oa/upload/gif"
      }
    };
  }
  /**
   * Kiểm tra hạn mức gửi tin nhắn đến user cụ thể
   * @param accessToken Access token của Official Account
   * @param userId ID của người dùng Zalo
   * @returns Thông tin hạn mức gửi tin nhắn
   */
  async checkMessageQuota(accessToken, userId) {
    try {
      const result = await this.client.apiGet(
        this.endpoints.message.quota,
        accessToken,
        { user_id: userId }
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to check message quota",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No quota data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to check message quota: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async getConversationMessages(accessToken, userId, offset = 0, count = 10) {
    try {
      if (count > 10) {
        throw new ZaloSDKError("M\u1ED7i request l\u1EA5y t\u1ED1i \u0111a \u0111\u01B0\u1EE3c 10 tin nh\u1EAFn", -1);
      }
      const params = {
        data: JSON.stringify({
          user_id: userId,
          offset,
          count
        })
      };
      const result = await this.client.apiGet(
        this.endpoints.message.conversation,
        accessToken,
        params
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get conversation messages",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No conversation data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get conversation messages: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async postConversationMessages(accessToken, userId, offset = 0, count = 10) {
    try {
      if (count > 10) {
        throw new ZaloSDKError("M\u1ED7i request l\u1EA5y t\u1ED1i \u0111a \u0111\u01B0\u1EE3c 10 tin nh\u1EAFn", -1);
      }
      const body = {
        user_id: userId,
        offset,
        count
      };
      const result = await this.client.apiPost(
        this.endpoints.message.conversation,
        accessToken,
        body
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get conversation messages",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No conversation data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get conversation messages: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async getRecentConversations(accessToken, offset = 0, count = 10) {
    try {
      if (count > 10) {
        throw new ZaloSDKError("M\u1ED7i request l\u1EA5y t\u1ED1i \u0111a 10 tin nh\u1EAFn", -1);
      }
      const params = {
        data: JSON.stringify({
          offset,
          count
        })
      };
      const result = await this.client.apiGet(
        this.endpoints.message.recentConversations,
        accessToken,
        params
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to get recent messages",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No recent messages data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to get recent messages: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async uploadFile(accessToken, fileData, fileName) {
    try {
      const allowedExtensions = [".pdf", ".doc", ".docx"];
      const fileExt = fileName.toLowerCase().substring(fileName.lastIndexOf("."));
      if (!allowedExtensions.includes(fileExt)) {
        throw new ZaloSDKError("Ch\u1EC9 h\u1ED7 tr\u1EE3 file PDF, DOC, DOCX", -1);
      }
      const maxSize = 5 * 1024 * 1024;
      let fileSize;
      if (typeof fileData === "string") {
        fileSize = Buffer.from(fileData, "base64").length;
      } else {
        fileSize = fileData.length;
      }
      if (fileSize > maxSize) {
        throw new ZaloSDKError("K\xEDch th\u01B0\u1EDBc file kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 5MB", -1);
      }
      let buffer;
      if (typeof fileData === "string") {
        buffer = Buffer.from(fileData, "base64");
      } else {
        buffer = fileData;
      }
      const result = await this.client.apiUploadFile(
        this.endpoints.upload.file,
        accessToken,
        buffer,
        fileName
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to upload file",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No upload result received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to upload file: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async uploadImage(accessToken, imageData, fileName) {
    try {
      const maxSize = 1 * 1024 * 1024;
      let imageSize;
      if (typeof imageData === "string") {
        imageSize = Buffer.from(imageData, "base64").length;
      } else {
        imageSize = imageData.length;
      }
      if (imageSize > maxSize) {
        throw new ZaloSDKError(
          "K\xEDch th\u01B0\u1EDBc h\xECnh \u1EA3nh kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 1MB",
          -1
        );
      }
      let buffer;
      if (typeof imageData === "string") {
        buffer = Buffer.from(imageData, "base64");
      } else {
        buffer = imageData;
      }
      const result = await this.client.apiUploadFile(
        this.endpoints.upload.image,
        accessToken,
        buffer,
        fileName
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to upload image",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No upload result received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to upload image: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async uploadGif(accessToken, gifData, fileName) {
    try {
      const maxSize = 5 * 1024 * 1024;
      let gifSize;
      if (typeof gifData === "string") {
        gifSize = Buffer.from(gifData, "base64").length;
      } else {
        gifSize = gifData.length;
      }
      if (gifSize > maxSize) {
        throw new ZaloSDKError(
          "K\xEDch th\u01B0\u1EDBc \u1EA3nh GIF kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 5MB",
          -1
        );
      }
      if (!fileName.toLowerCase().endsWith(".gif")) {
        throw new ZaloSDKError("File ph\u1EA3i c\xF3 \u0111\u1ECBnh d\u1EA1ng GIF", -1);
      }
      let buffer;
      if (typeof gifData === "string") {
        buffer = Buffer.from(gifData, "base64");
      } else {
        buffer = gifData;
      }
      const result = await this.client.apiUploadFile(
        this.endpoints.upload.gif,
        accessToken,
        buffer,
        fileName
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to upload GIF",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No upload result received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to upload GIF: ${error.message}`,
        -1,
        error
      );
    }
  }
};

// src/services/broadcast.service.ts
var BroadcastService = class {
  constructor(client) {
    this.client = client;
    // Zalo API endpoint for broadcast messages
    this.broadcastApiUrl = "https://openapi.zalo.me/v2.0/oa/message";
  }
  /**
   * Gửi tin nhắn broadcast đến nhiều người dùng
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin targeting người nhận
   * @param attachmentId ID của article attachment
   * @returns Thông tin tin nhắn broadcast đã gửi
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendBroadcastMessage(accessToken, recipient, attachmentId) {
    try {
      this.validateBroadcastRequest(recipient, attachmentId);
      const message = {
        attachment: {
          type: "template",
          payload: {
            template_type: "media",
            elements: [
              {
                media_type: "article",
                attachment_id: attachmentId
              }
            ]
          }
        }
      };
      const request = {
        recipient,
        message
      };
      const result = await this.client.apiPost(
        this.broadcastApiUrl,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send broadcast message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No broadcast response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send broadcast message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Tạo broadcast target với các tiêu chí targeting
   * @param criteria Các tiêu chí targeting
   * @returns BroadcastTarget object
   */
  createBroadcastTarget(criteria) {
    const target = {};
    if (criteria.ages && criteria.ages.length > 0) {
      const ageCodes = criteria.ages.map((age) => {
        const code = BROADCAST_AGE_CODES[age];
        if (!code) {
          throw new ZaloSDKError(`Invalid age group: ${age}`, -1);
        }
        return code;
      });
      target.ages = ageCodes.join(",");
    }
    if (criteria.gender) {
      const genderCode = BROADCAST_GENDER_CODES[criteria.gender];
      if (!genderCode) {
        throw new ZaloSDKError(`Invalid gender: ${criteria.gender}`, -1);
      }
      target.gender = genderCode;
    }
    if (criteria.cities && criteria.cities.length > 0) {
      const cityCodes = criteria.cities.map((city) => {
        const code = BROADCAST_CITY_CODES[city];
        if (!code) {
          throw new ZaloSDKError(`Invalid city: ${city}`, -1);
        }
        return code;
      });
      target.cities = cityCodes.join(",");
    }
    if (criteria.locations && criteria.locations.length > 0) {
      const locationCodes = criteria.locations.map((location) => {
        const code = BROADCAST_LOCATION_CODES[location];
        if (!code) {
          throw new ZaloSDKError(`Invalid location: ${location}`, -1);
        }
        return code;
      });
      target.locations = locationCodes.join(",");
    }
    if (criteria.platforms && criteria.platforms.length > 0) {
      const platformCodes = criteria.platforms.map((platform) => {
        const code = BROADCAST_PLATFORM_CODES[platform];
        if (!code) {
          throw new ZaloSDKError(`Invalid platform: ${platform}`, -1);
        }
        return code;
      });
      target.platform = platformCodes.join(",");
    }
    return target;
  }
  /**
   * Validate broadcast request parameters
   * @param recipient Broadcast recipient
   * @param attachmentId Article attachment ID
   */
  validateBroadcastRequest(recipient, attachmentId) {
    if (!recipient || !recipient.target) {
      throw new ZaloSDKError("Recipient target is required for broadcast", -1);
    }
    if (!attachmentId || attachmentId.trim().length === 0) {
      throw new ZaloSDKError("Article attachment ID is required", -1);
    }
    const target = recipient.target;
    const hasTargeting = target.ages || target.gender || target.cities || target.locations || target.platform;
    if (!hasTargeting) {
      throw new ZaloSDKError(
        "At least one targeting criteria is required (ages, gender, cities, locations, or platform)",
        -1
      );
    }
  }
  /**
   * Get available city codes for targeting
   * @returns Object mapping city names to codes
   */
  getCityCodes() {
    return BROADCAST_CITY_CODES;
  }
  /**
   * Get available age group codes for targeting
   * @returns Object mapping age groups to codes
   */
  getAgeGroupCodes() {
    return BROADCAST_AGE_CODES;
  }
  /**
   * Get available gender codes for targeting
   * @returns Object mapping genders to codes
   */
  getGenderCodes() {
    return BROADCAST_GENDER_CODES;
  }
  /**
   * Get available location codes for targeting
   * @returns Object mapping locations to codes
   */
  getLocationCodes() {
    return BROADCAST_LOCATION_CODES;
  }
  /**
   * Get available platform codes for targeting
   * @returns Object mapping platforms to codes
   */
  getPlatformCodes() {
    return BROADCAST_PLATFORM_CODES;
  }
  /**
   * Gửi nhiều tin nhắn broadcast với nhiều attachment IDs
   * @param accessToken Access token của Official Account
   * @param recipient Thông tin targeting người nhận
   * @param attachmentIds Danh sách các article attachment IDs
   * @param options Tùy chọn gửi (delay giữa các tin, parallel/sequential)
   * @returns Danh sách kết quả gửi broadcast
   * @deprecated Sử dụng ZNSService.sendMessageByUid() thay vì method này
   */
  async sendMultipleBroadcastMessages(accessToken, recipient, attachmentIds, options) {
    try {
      if (!attachmentIds || attachmentIds.length === 0) {
        throw new ZaloSDKError("At least one attachment ID is required", -1);
      }
      const uniqueAttachmentIds = [...new Set(attachmentIds)];
      const startTime = Date.now();
      const results = [];
      const mode = options?.mode || "sequential";
      const delay = options?.delay || 0;
      if (mode === "parallel") {
        const promises = uniqueAttachmentIds.map(async (attachmentId, index) => {
          try {
            const result = await this.sendBroadcastMessage(
              accessToken,
              recipient,
              attachmentId
            );
            const messageResult = {
              attachmentId,
              success: true,
              messageId: result.data.message_id,
              error: null,
              sentAt: /* @__PURE__ */ new Date()
            };
            if (options?.onProgress) {
              options.onProgress({
                total: uniqueAttachmentIds.length,
                completed: index + 1,
                successful: results.filter((r) => r.success).length + 1,
                failed: results.filter((r) => !r.success).length,
                currentAttachmentId: attachmentId,
                isCompleted: index === uniqueAttachmentIds.length - 1
              });
            }
            return messageResult;
          } catch (error) {
            const messageResult = {
              attachmentId,
              success: false,
              messageId: null,
              error: error instanceof ZaloSDKError ? error : new ZaloSDKError(error.message, -1),
              sentAt: /* @__PURE__ */ new Date()
            };
            if (options?.onProgress) {
              options.onProgress({
                total: uniqueAttachmentIds.length,
                completed: index + 1,
                successful: results.filter((r) => r.success).length,
                failed: results.filter((r) => !r.success).length + 1,
                currentAttachmentId: attachmentId,
                isCompleted: index === uniqueAttachmentIds.length - 1
              });
            }
            return messageResult;
          }
        });
        const parallelResults = await Promise.all(promises);
        results.push(...parallelResults);
      } else {
        for (let i = 0; i < uniqueAttachmentIds.length; i++) {
          const attachmentId = uniqueAttachmentIds[i];
          try {
            const result = await this.sendBroadcastMessage(
              accessToken,
              recipient,
              attachmentId
            );
            const messageResult = {
              attachmentId,
              success: true,
              messageId: result.data.message_id,
              error: null,
              sentAt: /* @__PURE__ */ new Date()
            };
            results.push(messageResult);
            if (options?.onProgress) {
              options.onProgress({
                total: uniqueAttachmentIds.length,
                completed: i + 1,
                successful: results.filter((r) => r.success).length,
                failed: results.filter((r) => !r.success).length,
                currentAttachmentId: attachmentId,
                isCompleted: i === uniqueAttachmentIds.length - 1
              });
            }
          } catch (error) {
            const messageResult = {
              attachmentId,
              success: false,
              messageId: null,
              error: error instanceof ZaloSDKError ? error : new ZaloSDKError(error.message, -1),
              sentAt: /* @__PURE__ */ new Date()
            };
            results.push(messageResult);
            if (options?.onProgress) {
              options.onProgress({
                total: uniqueAttachmentIds.length,
                completed: i + 1,
                successful: results.filter((r) => r.success).length,
                failed: results.filter((r) => !r.success).length,
                currentAttachmentId: attachmentId,
                isCompleted: i === uniqueAttachmentIds.length - 1
              });
            }
          }
          if (delay > 0 && i < uniqueAttachmentIds.length - 1) {
            await this.sleep(delay);
          }
        }
      }
      const totalDuration = Date.now() - startTime;
      const successful = results.filter((r) => r.success).length;
      const failed = results.filter((r) => !r.success).length;
      return {
        totalMessages: uniqueAttachmentIds.length,
        successfulMessages: successful,
        failedMessages: failed,
        results,
        totalDuration,
        mode,
        targetCriteria: recipient.target
      };
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send multiple broadcast messages: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Sleep utility for delays
   * @param ms Milliseconds to sleep
   */
  sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
};

// src/services/purchase.service.ts
var PurchaseService = class {
  constructor(client) {
    this.client = client;
    this.purchaseApiUrl = "https://openapi.zalo.me/v3.0/oa/purchase";
  }
  /**
   * Tạo đơn hàng mua sản phẩm/dịch vụ OA
   * @param accessToken Access token của Official Account
   * @param request Thông tin đơn hàng cần tạo
   * @returns Thông tin đơn hàng đã tạo
   */
  async createOrder(accessToken, request) {
    try {
      this.validateCreateOrderRequest(request);
      const result = await this.client.apiPost(
        `${this.purchaseApiUrl}/create_order`,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to create order",
          result.error,
          result
        );
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to create order: ${error.message}`,
        9999 /* SYSTEM_ERROR */,
        error
      );
    }
  }
  /**
   * Tạo đơn hàng với product_id
   * @param accessToken Access token của Official Account
   * @param beneficiary Đối tượng thụ hưởng (OA hoặc APP)
   * @param productId ID sản phẩm
   * @param voucherCode Mã giảm giá (tùy chọn)
   * @returns Thông tin đơn hàng đã tạo
   */
  async createOrderWithProduct(accessToken, beneficiary, productId, voucherCode) {
    const request = {
      beneficiary,
      product_id: productId,
      ...voucherCode && { voucher_code: voucherCode }
    };
    return this.createOrder(accessToken, request);
  }
  /**
   * Tạo đơn hàng với redeem_code (mã quà tặng)
   * @param accessToken Access token của Official Account
   * @param beneficiary Đối tượng thụ hưởng (OA hoặc APP)
   * @param redeemCode Mã quà tặng
   * @param voucherCode Mã giảm giá (tùy chọn)
   * @returns Thông tin đơn hàng đã tạo
   */
  async createOrderWithRedeemCode(accessToken, beneficiary, redeemCode, voucherCode) {
    const request = {
      beneficiary,
      redeem_code: redeemCode,
      ...voucherCode && { voucher_code: voucherCode }
    };
    return this.createOrder(accessToken, request);
  }
  /**
   * Validate create order request
   * @param request Request to validate
   */
  validateCreateOrderRequest(request) {
    if (!request.beneficiary || !["OA", "APP"].includes(request.beneficiary)) {
      throw new ZaloSDKError(
        "Beneficiary must be either 'OA' or 'APP'",
        1001 /* INVALID_BENEFICIARY */
      );
    }
    const hasProductId = isProductOrderRequest(request);
    const hasRedeemCode = isRedeemOrderRequest(request);
    if (!hasProductId && !hasRedeemCode) {
      throw new ZaloSDKError(
        "Either product_id or redeem_code must be provided",
        1002 /* INVALID_PRODUCT_ID */
      );
    }
    if (hasProductId && hasRedeemCode) {
      throw new ZaloSDKError(
        "Cannot use both product_id and redeem_code in the same request",
        1002 /* INVALID_PRODUCT_ID */
      );
    }
    if (hasProductId && (!request.product_id || request.product_id <= 0)) {
      throw new ZaloSDKError(
        "Product ID must be a positive number",
        1002 /* INVALID_PRODUCT_ID */
      );
    }
    if (hasRedeemCode && (!request.redeem_code || request.redeem_code.trim() === "")) {
      throw new ZaloSDKError(
        "Redeem code cannot be empty",
        1003 /* INVALID_REDEEM_CODE */
      );
    }
    if (request.voucher_code && request.voucher_code.trim() === "") {
      throw new ZaloSDKError(
        "Voucher code cannot be empty if provided",
        1004 /* INVALID_VOUCHER_CODE */
      );
    }
  }
  /**
   * Kiểm tra thời gian có thể tạo đơn hàng
   * @returns true nếu trong thời gian cho phép tạo đơn hàng
   */
  isOrderCreationTimeValid() {
    const now = /* @__PURE__ */ new Date();
    const hours = now.getHours();
    const minutes = now.getMinutes();
    if (hours === 0 && minutes === 0) {
      return false;
    }
    if (hours === 23 && minutes >= 55) {
      return false;
    }
    return true;
  }
  /**
   * Tính toán thời gian hết hạn của OTT (One Time Token)
   * @param createdTime Thời gian tạo đơn hàng (milliseconds)
   * @returns Thời gian hết hạn OTT (milliseconds)
   */
  calculateOTTExpiration(createdTime) {
    return createdTime + 5 * 60 * 1e3;
  }
  /**
   * Kiểm tra OTT còn hiệu lực hay không
   * @param createdTime Thời gian tạo đơn hàng (milliseconds)
   * @returns true nếu OTT còn hiệu lực
   */
  isOTTValid(createdTime) {
    const now = Date.now();
    const expirationTime = this.calculateOTTExpiration(createdTime);
    return now < expirationTime;
  }
  /**
   * Xác nhận thanh toán đơn hàng
   * @param accessToken Access token của Official Account
   * @param request Thông tin xác nhận đơn hàng
   * @returns Thông tin đơn hàng đã được xác nhận thanh toán
   */
  async confirmOrder(accessToken, request) {
    try {
      this.validateConfirmOrderRequest(request);
      const result = await this.client.apiPost(
        `${this.purchaseApiUrl}/confirm_order`,
        accessToken,
        request
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to confirm order",
          result.error,
          result
        );
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to confirm order: ${error.message}`,
        9999 /* SYSTEM_ERROR */,
        error
      );
    }
  }
  /**
   * Xác nhận thanh toán đơn hàng với order ID và verified token
   * @param accessToken Access token của Official Account
   * @param orderId ID đơn hàng
   * @param verifiedToken OTT token để xác nhận
   * @returns Thông tin đơn hàng đã được xác nhận thanh toán
   */
  async confirmOrderById(accessToken, orderId, verifiedToken) {
    const request = {
      order_id: orderId,
      verified_token: verifiedToken
    };
    return this.confirmOrder(accessToken, request);
  }
  /**
   * Lấy thông tin sản phẩm theo ID
   * @param productId ID sản phẩm
   * @returns Thông tin sản phẩm hoặc undefined nếu không tìm thấy
   */
  getProductInfo(productId) {
    return PRODUCT_INFO[productId];
  }
  /**
   * Lấy danh sách tất cả sản phẩm có sẵn
   * @returns Danh sách thông tin sản phẩm
   */
  getAllProducts() {
    return Object.values(PRODUCT_INFO);
  }
  /**
   * Lấy danh sách sản phẩm theo loại
   * @param category Loại sản phẩm
   * @returns Danh sách sản phẩm thuộc loại đó
   */
  getProductsByCategory(category) {
    return Object.values(PRODUCT_INFO).filter((product) => product.category === category);
  }
  /**
   * Lấy danh sách sản phẩm theo đối tượng thụ hưởng
   * @param beneficiary Đối tượng thụ hưởng
   * @returns Danh sách sản phẩm phù hợp với đối tượng thụ hưởng
   */
  getProductsByBeneficiary(beneficiary) {
    return Object.values(PRODUCT_INFO).filter(
      (product) => product.beneficiary.includes(beneficiary)
    );
  }
  /**
   * Kiểm tra sản phẩm có phù hợp với đối tượng thụ hưởng không
   * @param productId ID sản phẩm
   * @param beneficiary Đối tượng thụ hưởng
   * @returns true nếu sản phẩm phù hợp với đối tượng thụ hưởng
   */
  isProductCompatibleWithBeneficiary(productId, beneficiary) {
    const product = this.getProductInfo(productId);
    if (!product) {
      return false;
    }
    return product.beneficiary.includes(beneficiary);
  }
  // ==================== SPECIALIZED PRODUCT APIS ====================
  /**
   * Tạo đơn hàng gói OA Subscription
   * @param accessToken Access token của Official Account
   * @param subscriptionType Loại gói subscription
   * @param voucherCode Mã giảm giá (tùy chọn)
   * @returns Thông tin đơn hàng đã tạo
   */
  async createOASubscriptionOrder(accessToken, subscriptionType, voucherCode) {
    const productIdMap = {
      "advanced_6m": OA_PRODUCTS.OA_ADVANCED_6M,
      "advanced_12m": OA_PRODUCTS.OA_ADVANCED_12M,
      "premium_6m": OA_PRODUCTS.OA_PREMIUM_6M,
      "premium_12m": OA_PRODUCTS.OA_PREMIUM_12M
    };
    const productId = productIdMap[subscriptionType];
    return this.createOrderWithProduct(accessToken, "OA", productId, voucherCode);
  }
  /**
   * Tạo đơn hàng gói GMF (Group Message Framework)
   * @param accessToken Access token của Official Account
   * @param memberLimit Giới hạn số thành viên
   * @param voucherCode Mã giảm giá (tùy chọn)
   * @returns Thông tin đơn hàng đã tạo
   */
  async createGMFOrder(accessToken, memberLimit, voucherCode) {
    const productIdMap = {
      10: OA_PRODUCTS.GMF_10_MEMBERS,
      50: OA_PRODUCTS.GMF_50_MEMBERS,
      100: OA_PRODUCTS.GMF_100_MEMBERS,
      1e3: OA_PRODUCTS.GMF_1000_MEMBERS
    };
    const productId = productIdMap[memberLimit];
    return this.createOrderWithProduct(accessToken, "OA", productId, voucherCode);
  }
  /**
   * Tạo đơn hàng gói quota tin nhắn giao dịch
   * @param accessToken Access token của Official Account
   * @param beneficiary Đối tượng thụ hưởng (OA hoặc APP)
   * @param quotaSize Kích thước gói quota
   * @param voucherCode Mã giảm giá (tùy chọn)
   * @returns Thông tin đơn hàng đã tạo
   */
  async createTransactionQuotaOrder(accessToken, beneficiary, quotaSize, voucherCode) {
    const productIdMap = {
      "5k": OA_PRODUCTS.TRANSACTION_5K,
      "50k": OA_PRODUCTS.TRANSACTION_50K,
      "500k": OA_PRODUCTS.TRANSACTION_500K
    };
    const productId = productIdMap[quotaSize];
    if (!this.isProductCompatibleWithBeneficiary(productId, beneficiary)) {
      throw new ZaloSDKError(
        `Product ${quotaSize} transaction quota is not compatible with beneficiary ${beneficiary}`,
        1005 /* PRODUCT_NOT_AVAILABLE */
      );
    }
    return this.createOrderWithProduct(accessToken, beneficiary, productId, voucherCode);
  }
  // ==================== BULK ORDER OPERATIONS ====================
  /**
   * Tạo nhiều đơn hàng cùng lúc (batch operation)
   * @param accessToken Access token của Official Account
   * @param orders Danh sách đơn hàng cần tạo
   * @returns Danh sách kết quả tạo đơn hàng
   */
  async createMultipleOrders(accessToken, orders) {
    const results = [];
    for (const order of orders) {
      try {
        const orderData = await this.createOrder(accessToken, order);
        results.push({ success: true, data: orderData });
      } catch (error) {
        results.push({
          success: false,
          error: error instanceof ZaloSDKError ? error.message : error.message
        });
      }
    }
    return results;
  }
  /**
   * Tạo combo đơn hàng OA Premium + GMF
   * @param accessToken Access token của Official Account
   * @param premiumDuration Thời hạn gói Premium
   * @param gmfMemberLimit Giới hạn thành viên GMF
   * @param voucherCode Mã giảm giá (tùy chọn)
   * @returns Danh sách kết quả tạo đơn hàng
   */
  async createOAPremiumGMFCombo(accessToken, premiumDuration, gmfMemberLimit, voucherCode) {
    const premiumResult = await this.createOASubscriptionOrder(
      accessToken,
      premiumDuration === "6m" ? "premium_6m" : "premium_12m",
      voucherCode
    ).then((data) => ({ success: true, data })).catch((error) => ({
      success: false,
      error: error instanceof ZaloSDKError ? error.message : error.message
    }));
    const gmfResult = await this.createGMFOrder(
      accessToken,
      gmfMemberLimit,
      voucherCode
    ).then((data) => ({ success: true, data })).catch((error) => ({
      success: false,
      error: error instanceof ZaloSDKError ? error.message : error.message
    }));
    return {
      premium: premiumResult,
      gmf: gmfResult
    };
  }
  // ==================== PRODUCT RECOMMENDATION ====================
  /**
   * Gợi ý sản phẩm dựa trên nhu cầu
   * @param requirements Yêu cầu của khách hàng
   * @returns Danh sách sản phẩm được gợi ý
   */
  recommendProducts(requirements) {
    let recommendations = [];
    const compatibleProducts = this.getProductsByBeneficiary(requirements.beneficiary);
    if (requirements.features?.includes("subscription")) {
      const subscriptionProducts = compatibleProducts.filter((p) => p.category === "subscription");
      if (requirements.duration === "long") {
        recommendations.push(...subscriptionProducts.filter((p) => p.name.includes("12 th\xE1ng")));
      } else {
        recommendations.push(...subscriptionProducts.filter((p) => p.name.includes("6 th\xE1ng")));
      }
    }
    if (requirements.features?.includes("group_messaging")) {
      const gmfProducts = compatibleProducts.filter((p) => p.category === "gmf");
      if (requirements.budget === "low") {
        recommendations.push(...gmfProducts.filter((p) => p.name.includes("10 th\xE0nh vi\xEAn")));
      } else if (requirements.budget === "high") {
        recommendations.push(...gmfProducts.filter((p) => p.name.includes("1000 th\xE0nh vi\xEAn")));
      } else {
        recommendations.push(...gmfProducts.filter(
          (p) => p.name.includes("50 th\xE0nh vi\xEAn") || p.name.includes("100 th\xE0nh vi\xEAn")
        ));
      }
    }
    if (requirements.features?.includes("transaction_quota")) {
      const quotaProducts = compatibleProducts.filter((p) => p.category === "quota");
      if (requirements.budget === "low") {
        recommendations.push(...quotaProducts.filter((p) => p.name.includes("5k tin")));
      } else if (requirements.budget === "high") {
        recommendations.push(...quotaProducts.filter((p) => p.name.includes("500k tin")));
      } else {
        recommendations.push(...quotaProducts.filter((p) => p.name.includes("50k tin")));
      }
    }
    return recommendations.filter(
      (product, index, self) => index === self.findIndex((p) => p.id === product.id)
    );
  }
  /**
   * Validate confirm order request
   * @param request Request to validate
   */
  validateConfirmOrderRequest(request) {
    if (!request.order_id || request.order_id.trim() === "") {
      throw new ZaloSDKError(
        "Order ID cannot be empty",
        1010 /* INVALID_ORDER_ID */
      );
    }
    if (!request.verified_token || request.verified_token.trim() === "") {
      throw new ZaloSDKError(
        "Verified token cannot be empty",
        1011 /* INVALID_VERIFIED_TOKEN */
      );
    }
  }
};

// src/services/anonymous-message.service.ts
var AnonymousMessageService = class {
  constructor(client) {
    this.client = client;
    this.endpoint = "https://openapi.zalo.me/v2.0/oa/message";
  }
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
  async sendTextMessage(accessToken, recipient, text) {
    try {
      if (text.length > 2e3) {
        throw new ZaloSDKError(
          "N\u1ED9i dung v\u0103n b\u1EA3n kh\xF4ng \u0111\u01B0\u1EE3c v\u01B0\u1EE3t qu\xE1 2000 k\xFD t\u1EF1",
          -1
        );
      }
      if (text.trim().length === 0) {
        throw new ZaloSDKError("N\u1ED9i dung v\u0103n b\u1EA3n kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const request = {
        recipient,
        message: { text }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send anonymous text message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send anonymous text message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendImageMessage(accessToken, recipient, imageUrl, attachmentId) {
    try {
      if (!imageUrl && !attachmentId) {
        throw new ZaloSDKError(
          "Ph\u1EA3i cung c\u1EA5p imageUrl ho\u1EB7c attachmentId",
          -1
        );
      }
      const attachment = {
        type: "template",
        payload: {
          template_type: "media",
          elements: [
            {
              media_type: "image",
              ...imageUrl && { url: imageUrl },
              ...attachmentId && { attachment_id: attachmentId }
            }
          ]
        }
      };
      const request = {
        recipient,
        message: { attachment }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send anonymous image message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send anonymous image message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendFileMessage(accessToken, recipient, fileToken) {
    try {
      if (!fileToken || fileToken.trim().length === 0) {
        throw new ZaloSDKError("File token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const attachment = {
        type: "file",
        payload: {
          token: fileToken
        }
      };
      const request = {
        recipient,
        message: { attachment }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send anonymous file message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send anonymous file message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendStickerMessage(accessToken, recipient, stickerId) {
    try {
      if (!stickerId || stickerId.trim().length === 0) {
        throw new ZaloSDKError("Sticker ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const attachment = {
        type: "template",
        payload: {
          template_type: "media",
          elements: [
            {
              media_type: "sticker",
              attachment_id: stickerId
            }
          ]
        }
      };
      const request = {
        recipient,
        message: { attachment }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send anonymous sticker message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send anonymous sticker message: ${error.message}`,
        -1,
        error
      );
    }
  }
};

// src/services/message-reaction.service.ts
var MessageReactionService = class {
  constructor(client) {
    this.client = client;
    this.endpoint = "https://openapi.zalo.me/v2.0/oa/message";
  }
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
  async reactToMessage(accessToken, userId, messageId, reactIcon) {
    try {
      if (!userId || userId.trim().length === 0) {
        throw new ZaloSDKError("User ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!messageId || messageId.trim().length === 0) {
        throw new ZaloSDKError("Message ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const validIcons = [
        ":>",
        "--b",
        ":-((",
        "/-strong",
        "/-heart",
        ":-h",
        ":o",
        "/-remove"
      ];
      if (!validIcons.includes(reactIcon)) {
        throw new ZaloSDKError(
          `Bi\u1EC3u t\u01B0\u1EE3ng c\u1EA3m x\xFAc kh\xF4ng h\u1EE3p l\u1EC7. C\xE1c gi\xE1 tr\u1ECB h\u1EE3p l\u1EC7: ${validIcons.join(
            ", "
          )}`,
          -1
        );
      }
      const request = {
        recipient: {
          user_id: userId
        },
        sender_action: {
          react_icon: reactIcon,
          react_message_id: messageId
        }
      };
      const result = await this.client.apiPost(this.endpoint, accessToken, request);
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to react to message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to react to message: ${error.message}`,
        -1,
        error
      );
    }
  }
  /**
   * Thả icon "Thích" (Like) vào tin nhắn
   *
   * @param accessToken Access token của Official Account
   * @param userId ID của người nhận
   * @param messageId message_id của tin nhắn cần thả cảm xúc
   * @returns Thông tin tin nhắn phản hồi
   */
  async likeMessage(accessToken, userId, messageId) {
    return this.reactToMessage(accessToken, userId, messageId, ":>");
  }
  /**
   * Thả icon "Haha" vào tin nhắn
   *
   * @param accessToken Access token của Official Account
   * @param userId ID của người nhận
   * @param messageId message_id của tin nhắn cần thả cảm xúc
   * @returns Thông tin tin nhắn phản hồi
   */
  async hahaMessage(accessToken, userId, messageId) {
    return this.reactToMessage(accessToken, userId, messageId, "--b");
  }
  /**
   * Thả icon "Buồn" (Sad) vào tin nhắn
   *
   * @param accessToken Access token của Official Account
   * @param userId ID của người nhận
   * @param messageId message_id của tin nhắn cần thả cảm xúc
   * @returns Thông tin tin nhắn phản hồi
   */
  async sadMessage(accessToken, userId, messageId) {
    return this.reactToMessage(accessToken, userId, messageId, ":-((");
  }
  /**
   * Thả icon "Mạnh mẽ" (Strong) vào tin nhắn
   *
   * @param accessToken Access token của Official Account
   * @param userId ID của người nhận
   * @param messageId message_id của tin nhắn cần thả cảm xúc
   * @returns Thông tin tin nhắn phản hồi
   */
  async strongMessage(accessToken, userId, messageId) {
    return this.reactToMessage(accessToken, userId, messageId, "/-strong");
  }
  /**
   * Thả icon "Yêu thích" (Love) vào tin nhắn
   *
   * @param accessToken Access token của Official Account
   * @param userId ID của người nhận
   * @param messageId message_id của tin nhắn cần thả cảm xúc
   * @returns Thông tin tin nhắn phản hồi
   */
  async loveMessage(accessToken, userId, messageId) {
    return this.reactToMessage(accessToken, userId, messageId, "/-heart");
  }
  /**
   * Thả icon "Ngạc nhiên" (Wow) vào tin nhắn
   *
   * @param accessToken Access token của Official Account
   * @param userId ID của người nhận
   * @param messageId message_id của tin nhắn cần thả cảm xúc
   * @returns Thông tin tin nhắn phản hồi
   */
  async wowMessage(accessToken, userId, messageId) {
    return this.reactToMessage(accessToken, userId, messageId, ":-h");
  }
  /**
   * Thu hồi biểu tượng cảm xúc khỏi tin nhắn
   *
   * @param accessToken Access token của Official Account
   * @param userId ID của người nhận
   * @param messageId message_id của tin nhắn cần thu hồi cảm xúc
   * @returns Thông tin tin nhắn phản hồi
   */
  async removeReaction(accessToken, userId, messageId) {
    return this.reactToMessage(accessToken, userId, messageId, "/-remove");
  }
};
var ReactionIcon = /* @__PURE__ */ ((ReactionIcon2) => {
  ReactionIcon2["LIKE"] = ":>";
  ReactionIcon2["HAHA"] = "--b";
  ReactionIcon2["SAD"] = ":-((";
  ReactionIcon2["STRONG"] = "/-strong";
  ReactionIcon2["HEART"] = "/-heart";
  ReactionIcon2["WOW_H"] = ":-h";
  ReactionIcon2["WOW_O"] = ":o";
  ReactionIcon2["REMOVE"] = "/-remove";
  return ReactionIcon2;
})(ReactionIcon || {});

// src/services/miniapp-message.service.ts
var MiniAppMessageService = class {
  constructor(client) {
    this.client = client;
    this.endpoint = "https://openapi.zalo.me/v3.0/oa/message/cs/miniapp";
  }
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
  async sendMessage(accessToken, miniappMessageToken, userId, templateType, templateData) {
    try {
      if (!miniappMessageToken || miniappMessageToken.trim().length === 0) {
        throw new ZaloSDKError(
          "Miniapp message token kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng",
          -1
        );
      }
      if (!userId || userId.trim().length === 0) {
        throw new ZaloSDKError("User ID kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!templateType || templateType.trim().length === 0) {
        throw new ZaloSDKError("Template type kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      if (!templateData || Object.keys(templateData).length === 0) {
        throw new ZaloSDKError("Template data kh\xF4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng", -1);
      }
      const request = {
        recipient: {
          user_id: userId
        },
        message: {
          template_type: templateType,
          template_data: templateData
        }
      };
      const result = await this.client.apiPostWithHeaders(
        this.endpoint,
        accessToken,
        request,
        {
          miniapp_messsage_token: miniappMessageToken
        }
      );
      if (result.error !== 0) {
        throw new ZaloSDKError(
          result.message || "Failed to send miniapp message",
          result.error,
          result
        );
      }
      if (!result.data) {
        throw new ZaloSDKError("No response data received", -1);
      }
      return result.data;
    } catch (error) {
      if (error instanceof ZaloSDKError) {
        throw error;
      }
      throw new ZaloSDKError(
        `Failed to send miniapp message: ${error.message}`,
        -1,
        error
      );
    }
  }
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
  async sendQueueNotification(accessToken, miniappMessageToken, userId, customerName, queueNumber, note, buttons) {
    const templateData = {
      customer_name: customerName,
      queue_number: queueNumber,
      note
    };
    if (buttons && buttons.length > 0) {
      templateData.buttons = buttons;
    }
    return this.sendMessage(
      accessToken,
      miniappMessageToken,
      userId,
      "FB0001",
      templateData
    );
  }
};
var MiniAppQuotaType = /* @__PURE__ */ ((MiniAppQuotaType2) => {
  MiniAppQuotaType2["REPLY"] = "reply";
  MiniAppQuotaType2["SUB_QUOTA"] = "sub_quota";
  MiniAppQuotaType2["PURCHASE_QUOTA"] = "purchase_quota";
  MiniAppQuotaType2["REWARD_QUOTA"] = "reward_quota";
  return MiniAppQuotaType2;
})(MiniAppQuotaType || {});
var QuotaOwnerType = /* @__PURE__ */ ((QuotaOwnerType2) => {
  QuotaOwnerType2["OA"] = "OA";
  QuotaOwnerType2["APP"] = "App";
  return QuotaOwnerType2;
})(QuotaOwnerType || {});

// src/zalo-sdk.ts
var ZaloSDK = class {
  constructor(config) {
    if (!config.appId) {
      throw new ZaloSDKError("appId is required in SDK configuration");
    }
    if (!config.appSecret) {
      throw new ZaloSDKError("appSecret is required in SDK configuration");
    }
    this.config = {
      appId: config.appId,
      appSecret: config.appSecret,
      timeout: config.timeout || 3e4,
      debug: config.debug || false,
      apiBaseUrl: config.apiBaseUrl || "https://openapi.zalo.me",
      retry: {
        attempts: config.retry?.attempts || 3,
        delay: config.retry?.delay || 1e3,
        ...config.retry
      }
    };
    this.logger = new ConsoleLogger(this.config.debug);
    this.client = new ZaloClient(this.config);
    this.auth = new AuthService(
      this.client,
      this.config.appId,
      this.config.appSecret
    );
    this.oa = new OAService(this.client);
    this.user = new UserService(this.client);
    this.zns = new ZNSService(this.client);
    this.groupMessage = new GroupMessageService(this.client);
    this.groupManagement = new GroupManagementService(this.client);
    this.article = new ArticleService(this.client);
    this.videoUpload = new VideoUploadService(this.client);
    this.consultation = new ConsultationService(this.client);
    this.transaction = new TransactionService(this.client);
    this.promotion = new PromotionService(this.client);
    this.generalMessage = new GeneralMessageService(this.client);
    this.messageManagement = new MessageManagementService(this.client);
    this.broadcast = new BroadcastService(this.client);
    this.purchase = new PurchaseService(this.client);
    this.anonymousMessage = new AnonymousMessageService(this.client);
    this.messageReaction = new MessageReactionService(this.client);
    this.miniappMessage = new MiniAppMessageService(this.client);
    this.logger.info("Zalo SDK initialized", {
      appId: this.config.appId,
      debug: this.config.debug,
      timeout: this.config.timeout
    });
  }
  /**
   * Quick method to get OA access token from authorization code
   */
  async getOAAccessToken(code, redirectUri) {
    const params = {
      app_id: this.config.appId,
      app_secret: this.config.appSecret,
      code,
      redirect_uri: redirectUri
    };
    return this.auth.getOAAccessToken(params);
  }
  /**
   * Quick method to get Social access token from authorization code
   */
  async getSocialAccessToken(code, redirectUri, codeVerifier) {
    const params = {
      app_id: this.config.appId,
      app_secret: this.config.appSecret,
      code,
      redirect_uri: redirectUri,
      code_verifier: codeVerifier || ""
    };
    return this.auth.getSocialAccessToken(params);
  }
  /**
   * Quick method to refresh OA access token
   */
  async refreshOAAccessToken(refreshToken) {
    const params = {
      app_id: this.config.appId,
      app_secret: this.config.appSecret,
      refresh_token: refreshToken
    };
    return this.auth.refreshOAAccessToken(params);
  }
  /**
   * Quick method to refresh Social access token
   */
  async refreshSocialAccessToken(refreshToken) {
    const params = {
      app_id: this.config.appId,
      app_secret: this.config.appSecret,
      refresh_token: refreshToken
    };
    return this.auth.refreshSocialAccessToken(params);
  }
  /**
   * Quick method to get OA information
   */
  async getOAInfo(accessToken) {
    return this.oa.getOAInfo(accessToken);
  }
  /**
   * Quick method to get message quota
   */
  async getMessageQuota(accessToken) {
    return this.oa.getMessageQuota(accessToken);
  }
  /**
   * Quick method to get Social user information
   */
  async getSocialUserInfo(accessToken, fields) {
    return this.auth.getSocialUserInfo(accessToken, fields);
  }
  /**
   * Quick method to send consultation text message
   */
  async sendConsultationText(accessToken, userId, text) {
    return this.consultation.sendTextMessage(
      accessToken,
      { user_id: userId },
      { type: "text", text }
    );
  }
  /**
   * Quick method to get user information
   */
  async getUserInfo(accessToken, userId) {
    return this.user.getUserInfo(accessToken, userId);
  }
  /**
   * Quick method to get user list
   */
  async getUserList(accessToken, request) {
    return this.user.getUserList(accessToken, request);
  }
  /**
   * Quick method to get all followers
   */
  async getFollowers(accessToken) {
    return this.user.getFollowers(accessToken);
  }
  /**
   * Quick method to register webhook handlers
   */
  // public registerWebhookHandlers(handlers: WebhookHandlers): void {
  //   this.webhook.registerHandlers(handlers);
  // }
  /**
   * Quick method to process webhook
   */
  // public async processWebhook(
  //   payload: string,
  //   signature?: string,
  //   timestamp?: string
  // ): Promise<void> {
  //   return this.webhook.processWebhook(payload, signature, timestamp);
  // }
  /**
   * Create OA authorization URL with PKCE support
   * @deprecated Use auth.createOAAuthUrl() directly for full control over PKCE and state
   */
  createOAAuthUrl(redirectUri, state) {
    const result = this.auth.createOAAuthUrl(redirectUri, state, void 0, false);
    return result.url;
  }
  /**
   * Create OA authorization URL with full PKCE support
   * Returns both URL and state for enhanced security
   */
  createSecureOAAuthUrl(redirectUri, state, enablePKCE = true) {
    const result = this.auth.createOAAuthUrl(redirectUri, state, void 0, enablePKCE);
    return result;
  }
  /**
   * Create Social authorization URL
   */
  createSocialAuthUrl(redirectUri, state) {
    return this.auth.createSocialAuthUrl(redirectUri, state);
  }
  /**
   * Generate PKCE for Social API
   */
  generatePKCE() {
    return this.auth.generatePKCE();
  }
  /**
   * Validate access token
   */
  async validateAccessToken(accessToken, scope = "social") {
    try {
      if (scope === "oa") {
        return this.oa.validateOAToken(accessToken);
      } else {
        const validation = await this.auth.validateAccessToken(accessToken);
        return validation.valid;
      }
    } catch (error) {
      this.logger.warn(`Token validation failed: ${error.message}`);
      return false;
    }
  }
  /**
   * Get SDK version
   */
  getVersion() {
    return "1.0.0";
  }
  /**
   * Get SDK configuration (without sensitive data)
   */
  getConfig() {
    const { appSecret, ...safeConfig } = this.config;
    return safeConfig;
  }
  /**
   * Enable or disable debug logging
   */
  setDebug(enabled) {
    this.config.debug = enabled;
    if (enabled !== this.config.debug) {
      this.logger.info(`Debug mode ${enabled ? "enabled" : "disabled"}`);
    }
  }
  /**
   * Test SDK connectivity
   */
  async testConnection() {
    try {
      const testUrl = `${this.config.apiBaseUrl}/v2.0/oa/getoa`;
      await this.client.apiGet(testUrl, "test-token");
      return true;
    } catch (error) {
      if (error.message.includes("access_token") || error.message.includes("401")) {
        return true;
      }
      this.logger.error(`Connection test failed: ${error.message}`);
      return false;
    }
  }
  /**
   * Make a custom API request (advanced usage)
   */
  async customRequest(method, endpoint, accessToken, data, params) {
    switch (method) {
      case "GET":
        return this.client.apiGet(endpoint, accessToken, params);
      case "POST":
        return this.client.apiPost(endpoint, accessToken, data, params);
      case "PUT":
        return this.client.apiPut(endpoint, accessToken, data, params);
      case "DELETE":
        return this.client.apiDelete(endpoint, accessToken, params);
      default:
        throw new ZaloSDKError(`Unsupported HTTP method: ${method}`);
    }
  }
  /**
   * Upload file to Zalo API
   */
  async uploadFile(endpoint, accessToken, file, filename, additionalFields) {
    return this.client.apiUploadFile(
      endpoint,
      accessToken,
      file,
      filename,
      additionalFields
    );
  }
  /**
   * Dispose SDK resources
   */
  dispose() {
    this.logger.info("Zalo SDK disposed");
  }
};
// Annotate the CommonJS export names for ESM import in node:
0 && (module.exports = {
  ARTICLE_CONSTRAINTS,
  AnonymousMessageService,
  ArticleService,
  ArticleStatus,
  ArticleType,
  AttachmentType,
  AuthMethod,
  AuthScope,
  AuthService,
  BROADCAST_AGE_CODES,
  BROADCAST_CITY_CODES,
  BROADCAST_GENDER_CODES,
  BROADCAST_LOCATION_CODES,
  BROADCAST_PLATFORM_CODES,
  BaseClient,
  BodyItemType,
  BroadcastService,
  CommentStatus,
  ConsoleLogger,
  ConsultationService,
  CoverType,
  GMFProductType,
  GeneralMessageService,
  GroupManagementService,
  GroupMessageService,
  MessageManagementService,
  MessageReactionIconEnum,
  MessageReactionService,
  MiniAppMessageService,
  MiniAppQuotaType,
  OAService,
  OA_PRODUCTS,
  PRODUCT_INFO,
  PromotionService,
  PurchaseErrorCode,
  PurchaseService,
  QuotaOwnerType,
  QuotaType,
  TransactionService,
  UserInfoFieldType,
  UserService,
  VIDEO_CONSTRAINTS,
  VideoUploadService,
  VideoUploadStatus,
  WebhookEventName,
  ZNSButtonType,
  ZNSParamType,
  ZNSService,
  ZNSTemplateTag,
  ZNSTemplateType,
  ZNSValidation,
  ZNS_BUTTON_TYPES,
  ZNS_PARAM_TYPES,
  ZNS_TEMPLATE_TAGS,
  ZNS_TEMPLATE_TAG_COMPATIBILITY,
  ZNS_TEMPLATE_TYPES,
  ZaloClient,
  ZaloSDK,
  ZaloSDKError,
  getMessageDirection,
  getWebhookEventAppId,
  getWebhookEventIdentifiers,
  getWebhookEventOaId,
  getWebhookEventRecipientId,
  getWebhookEventSenderId,
  hasAttachments,
  isAnonymousEvent,
  isBusinessCardEvent,
  isCallEvent,
  isConsentEvent,
  isExtensionEvent,
  isFeedbackEvent,
  isFollowEvent,
  isFromGroup,
  isFromPersonal,
  isGroupEvent,
  isGroupMessageEvent,
  isJourneyEvent,
  isMessageEvent,
  isOAMessageEvent,
  isOASendFileEvent,
  isOASendGifEvent,
  isOASendGroupFileEvent,
  isOASendGroupGifEvent,
  isOASendGroupImageEvent,
  isOASendGroupStickerEvent,
  isOASendGroupTextEvent,
  isOASendImageEvent,
  isOASendStickerEvent,
  isOASendTextEvent,
  isOAToGroupMessageEvent,
  isOAToUserMessageEvent,
  isPermissionEvent,
  isProductOrderRequest,
  isQuotaTemplateEvent,
  isReactionEvent,
  isRedeemOrderRequest,
  isShopEvent,
  isSystemEvent,
  isTemplateEvent,
  isUpdateUserInfoEvent,
  isUserActionEvent,
  isUserGroupMessageEvent,
  isUserMessageEvent,
  isUserSendGroupAudioEvent,
  isUserSendGroupFileEvent,
  isUserSendGroupImageEvent,
  isUserSendGroupTextEvent,
  isUserSendGroupVideoEvent,
  isWidgetEvent,
  isZNSEvent
});
//# sourceMappingURL=index.cjs.map