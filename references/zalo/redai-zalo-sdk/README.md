# RedAI Zalo SDK

A comprehensive TypeScript/JavaScript SDK for Zalo APIs, providing easy-to-use interfaces for:

- **Official Account (OA) API** - Manage your Zalo Official Account
- **Consultation Service** - Send customer support messages within 48-hour window
- **Broadcast Service** - Send mass communication messages with targeting criteria
- **Zalo Notification Service (ZNS)** - Send template-based notification messages
- **Social API** - Access user social information and authentication
- **Group Message Framework (GMF)** - Send messages to Zalo groups
- **User Management** - Comprehensive user profile and tag management
- **Content Management** - Upload and manage media content (images, files, articles)
- **Enhanced Article Management** - Get all articles with auto-pagination and progress tracking
- **Video Upload** - Upload and manage video content with processing
- **Tag Management** - User tagging and segmentation
- **Webhook handling** - Process Zalo webhook events

## Features

- 🚀 **TypeScript Support** - Full type safety and IntelliSense
- 🔐 **OAuth 2.0 Flow** - Complete authentication handling
- 🔄 **Auto Retry** - Built-in retry mechanism for failed requests
- 📝 **Comprehensive Logging** - Debug and monitor API calls
- 🛡️ **Error Handling** - Detailed error information and handling
- 📦 **Zero Dependencies** - Only requires axios and form-data
- 🎯 **Promise-based** - Modern async/await support
- 🔄 **Auto-Pagination** - Automatically fetch all articles with progress tracking

## Installation

```bash
npm install @warriorteam/redai-zalo-sdk
```

## 🚨 Version 1.9.0 - Breaking Changes

**ZNS Template API has been completely restructured to match Zalo API 100%**

### What Changed:
- ✅ **Fixed**: All ZNS template interfaces now match official Zalo API docs
- ✅ **Added**: Proper `layout` structure with header/body/footer components
- ✅ **Added**: Complete enum support for template types, tags, buttons, and params
- ✅ **Added**: Validation helpers and constants
- ❌ **Removed**: Non-existent fields like `templateContent`, `timeout`, `previewUrl`
- 🔄 **Changed**: Field names from camelCase to snake_case (e.g., `templateId` → `template_id`)

### Migration Required:
```typescript
// ❌ Before v1.9.0 (BROKEN)
const templateData = {
  templateId: "123",
  templateContent: "Hello" // Field doesn't exist in Zalo API
};

// ✅ After v1.9.0 (CORRECT)
import { ZNSTemplateType, ZNSTemplateTag } from '@warriorteam/redai-zalo-sdk';

const templateData = {
  template_id: "123",
  template_name: "My Template",
  template_type: ZNSTemplateType.CUSTOM,
  tag: ZNSTemplateTag.TRANSACTION,
  layout: {
    body: {
      components: [{ TITLE: { value: "Hello" } }]
    }
  },
  tracking_id: "track_001"
};
```

See [CHANGELOG.md](./CHANGELOG.md) for complete migration guide.

## 📚 Documentation

### 🚀 Getting Started
- **[API Reference](./docs/API_REFERENCE.md)** - Complete API documentation and method reference
- **[Authentication Guide](./docs/AUTHENTICATION.md)** - OAuth 2.0 flows for OA and Social APIs
- **[Architecture Overview](./ARCHITECTURE.md)** - SDK architecture and design patterns
- **[Services Added](./SERVICES_ADDED.md)** - Detailed breakdown of all services and features

### 📨 Messaging & Communication
- **[Message Services](./docs/MESSAGE_SERVICES.md)** - Complete guide for all message types and services
- **[Consultation Service](./docs/CONSULTATION_SERVICE.md)** - Customer service messaging guide
- **[ZNS Service](./docs/ZNS_SERVICE.md)** - Zalo Notification Service for template messages
- **[Group Management](./docs/GROUP_MANAGEMENT.md)** - Group messaging and management

### 👥 User & Content Management
- **[User Management](./docs/USER_MANAGEMENT.md)** - User profiles, followers, and management
- **[Tag Management](./docs/TAG_MANAGEMENT.md)** - User tagging and segmentation system
- **[Article Management](./docs/ARTICLE_MANAGEMENT.md)** - Content and article management

- **[Webhook Types (Source of Truth)](./src/types/webhook.ts)** - Strongly-typed webhook event definitions

### 🔔 Events & Integration
- **[Webhook Events Guide](./docs/WEBHOOK_EVENTS.md)** - Complete guide for all 70+ webhook event types

## Development

### Build from source

```bash
# Clone the repository
git clone https://github.com/redai/redai-zalo-sdk.git
cd redai-zalo-sdk

# Install dependencies
npm install

# Build the SDK
npm run build

# Run examples
npm run dev
```

## Quick Start

### Initialize the SDK

```typescript
import { ZaloSDK } from "@warriorteam/redai-zalo-sdk";

const zalo = new ZaloSDK({
  appId: "your-app-id",
  appSecret: "your-app-secret",
  debug: true, // Enable debug logging
});
```

### Authentication Flow

#### Official Account (OA) Authentication

```typescript
// 1. Create authorization URL
const authUrl = zalo.createOAAuthUrl("https://your-app.com/callback");
console.log("Visit:", authUrl);

// 2. Exchange authorization code for access token
const accessToken = await zalo.getOAAccessToken(
  "authorization-code-from-callback",
  "https://your-app.com/callback"
);

console.log("Access Token:", accessToken.access_token);
```

#### Social API Authentication

```typescript
// 1. Generate PKCE for security (recommended)
const pkce = zalo.generatePKCE();

// 2. Create authorization URL
const authUrl = zalo.createSocialAuthUrl("https://your-app.com/callback");

// 3. Exchange code for access token
const accessToken = await zalo.getSocialAccessToken(
  "authorization-code",
  "https://your-app.com/callback",
  pkce.code_verifier
);
```

### Official Account Operations

```typescript
// Get OA information
const oaInfo = await zalo.getOAInfo(accessToken.access_token);
console.log("OA Name:", oaInfo.name);
console.log("Followers:", oaInfo.num_follower);

// Check message quota
const quota = await zalo.getMessageQuota(accessToken.access_token);
console.log("Daily Quota:", quota.daily_quota);
console.log("Remaining:", quota.remaining_quota);

// Get detailed quota information
const detailedQuota = await zalo.oa.getQuotaSummary(accessToken.access_token);
console.log("Consultation Quota:", detailedQuota.consultation);
console.log("Transaction Quota:", detailedQuota.transaction);
```

### Social API Operations

```typescript
// Get user information
const userInfo = await zalo.getSocialUserInfo(
  accessToken.access_token,
  "id,name,picture,birthday"
);

console.log("User ID:", userInfo.id);
console.log("Name:", userInfo.name);
console.log("Avatar:", userInfo.picture?.data.url);
```

### Token Management

```typescript
// Refresh OA access token
const newOAToken = await zalo.refreshOAAccessToken(refreshToken);

// Refresh Social access token
const newSocialToken = await zalo.refreshSocialAccessToken(refreshToken);

// Validate token
const isValid = await zalo.validateAccessToken(accessToken.access_token, "oa");
```

## Advanced Usage

### Custom API Requests

```typescript
// Make custom API calls
const customData = await zalo.customRequest(
  "POST",
  "/v3.0/oa/message/cs",
  accessToken.access_token,
  {
    recipient: { user_id: "user-id" },
    message: { text: "Hello from SDK!" },
  }
);
```

### File Upload

```typescript
import fs from "fs";

// Upload image
const fileBuffer = fs.readFileSync("image.jpg");
const uploadResult = await zalo.uploadFile(
  "/v2.0/oa/upload/image",
  accessToken.access_token,
  fileBuffer,
  "image.jpg"
);

console.log("Uploaded URL:", uploadResult.data.url);
```

### Error Handling

```typescript
import { ZaloSDKError } from "redai-zalo-sdk";

try {
  const oaInfo = await zalo.getOAInfo(accessToken.access_token);
} catch (error) {
  if (error instanceof ZaloSDKError) {
    console.error("Zalo API Error:", error.message);
    console.error("Error Code:", error.code);
    console.error("Details:", error.details);
  } else {
    console.error("Unexpected error:", error);
  }
}
```

## Configuration Options

```typescript
const zalo = new ZaloSDK({
  appId: "your-app-id",
  appSecret: "your-app-secret",

  // Optional configurations
  timeout: 30000, // Request timeout in ms (default: 30000)
  debug: false, // Enable debug logging (default: false)
  apiBaseUrl: "https://openapi.zalo.me", // Custom API base URL

  // Retry configuration
  retry: {
    attempts: 3, // Number of retry attempts (default: 3)
    delay: 1000, // Delay between retries in ms (default: 1000)
  },
});
```

## API Reference

### ZaloSDK Class

#### Constructor

- `new ZaloSDK(config: ZaloSDKConfig)`

#### Authentication Methods

- `createOAAuthUrl(redirectUri: string, state?: string): string`
- `createSocialAuthUrl(redirectUri: string, state?: string): string`
- `getOAAccessToken(code: string, redirectUri: string): Promise<AccessToken>`
- `getSocialAccessToken(code: string, redirectUri: string, codeVerifier?: string): Promise<AccessToken>`
- `refreshOAAccessToken(refreshToken: string): Promise<AccessToken>`
- `refreshSocialAccessToken(refreshToken: string): Promise<AccessToken>`

#### OA Methods

- `getOAInfo(accessToken: string): Promise<OAInfo>`
- `getMessageQuota(accessToken: string): Promise<MessageQuota>`

#### Social Methods

- `getSocialUserInfo(accessToken: string, fields?: string): Promise<SocialUserInfo>`

#### Utility Methods

- `validateAccessToken(accessToken: string, scope?: 'oa' | 'social'): Promise<boolean>`
- `generatePKCE(): PKCEConfig`
- `testConnection(): Promise<boolean>`

### Services

#### AuthService

- Complete OAuth 2.0 flow handling
- PKCE support for Social API
- Token validation and refresh

#### OAService

- Official Account information management
- Quota monitoring and management
- Profile updates

## Error Handling

The SDK throws `ZaloSDKError` for all Zalo API related errors:

```typescript
class ZaloSDKError extends Error {
  code: number; // Zalo API error code
  details?: any; // Additional error details
}
```

Common error codes:

- `-216`: Invalid access token
- `-201`: Invalid parameters
- `-223`: Quota exceeded

## TypeScript Support

The SDK is written in TypeScript and provides comprehensive type definitions:

```typescript
import {
  ZaloSDK,
  ZaloSDKConfig,
  AccessToken,
  OAInfo,
  SocialUserInfo,
  ZaloSDKError,
} from "redai-zalo-sdk";
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:

- Create an issue on GitHub
- Contact RedAI team

### Broadcast Service (Mass Communication)

```typescript
// Create targeting criteria
const target = zalo.broadcast.createBroadcastTarget({
  gender: "FEMALE",           // Target female users
  ages: ["18-24", "25-34"],   // Age 18-34
  cities: ["Hồ Chí Minh", "Hà Nội"], // Ho Chi Minh City and Hanoi
  platforms: ["ANDROID", "IOS"]      // Android and iOS users
});

// Send broadcast message
const result = await zalo.broadcast.sendBroadcastMessage(
  accessToken,
  { target },
  "article-attachment-id"
);

console.log("Broadcast sent:", result.data.message_id);

// Simple city targeting
const hcmTarget = zalo.broadcast.createBroadcastTarget({
  cities: ["Hồ Chí Minh"]
});

await zalo.broadcast.sendBroadcastMessage(
  accessToken,
  { target: hcmTarget },
  "article-id"
);

// Send multiple broadcast messages
const attachmentIds = ["article-1", "article-2", "article-3"];

const multipleResult = await zalo.broadcast.sendMultipleBroadcastMessages(
  accessToken,
  { target: hcmTarget },
  attachmentIds,
  {
    mode: 'sequential',
    delay: 2000, // 2 seconds between messages
    onProgress: (progress) => {
      console.log(`Progress: ${progress.completed}/${progress.total}`);
    }
  }
);

console.log(`Sent ${multipleResult.successfulMessages}/${multipleResult.totalMessages} messages`);
```

### Consultation Service (Customer Support)

```typescript
// Send consultation text message
await zalo.consultation.sendTextMessage(accessToken,
  { user_id: "user-id" },
  {
    type: "text",
    text: "Xin chào! Tôi có thể hỗ trợ gì cho bạn?"
  }
);

// Send consultation image with guide
await zalo.consultation.sendImageMessage(accessToken,
  { user_id: "user-id" },
  {
    type: "image",
    attachment: {
      type: "image",
      payload: {
        url: "https://example.com/support-guide.jpg"
      }
    }
  }
);

// Send file attachment for support
await zalo.consultation.sendFileMessage(accessToken,
  { user_id: "user-id" },
  {
    type: "file",
    url: "https://example.com/manual.pdf",
    filename: "User Manual.pdf",
    attachment: {
      type: "file",
      payload: {
        url: "https://example.com/manual.pdf"
      }
    }
  }
);
```

### ZNS (Zalo Notification Service)

```typescript
// Send ZNS notification
await zalo.zns.sendMessage(accessToken, {
  phone: "0123456789",
  template_id: "your-template-id",
  template_data: {
    customer_name: "John Doe",
    order_id: "12345",
  },
});

// Get ZNS quota
const quota = await zalo.zns.getQuotaInfo(accessToken);

// Manage ZNS templates
const templates = await zalo.zns.getTemplateList(accessToken);
const template = await zalo.zns.createTemplate(accessToken, {
  templateName: "Order Confirmation",
  templateContent:
    "Hello {{customer_name}}, your order {{order_id}} is confirmed.",
  templateType: 3, // Confirmation type
});
```

### Group Messaging

```typescript
// Send text message to group
await zalo.groupMessage.sendTextMessage(accessToken, groupId, {
  type: "text",
  text: "Hello everyone!",
});

// Send image to group
await zalo.groupMessage.sendImageMessage(accessToken, groupId, {
  type: "image",
  imageUrl: "https://example.com/image.jpg",
  caption: "Check this out!",
});

// Get group information
const groupInfo = await zalo.groupMessage.getGroupInfo(accessToken, groupId);
const members = await zalo.groupMessage.getGroupMembers(accessToken, groupId);
```

### User Management

```typescript
// Get user profile
const profile = await zalo.userManagement.getUserProfile(accessToken, userId);

// Tag management
await zalo.tag.createTag(accessToken, "VIP Customer");
await zalo.tag.tagUser(accessToken, userId, ["VIP Customer", "Premium"]);
const userTags = await zalo.tag.getUserTags(accessToken, userId);

// Get followers list
const followers = await zalo.userManagement.getFollowersList(accessToken);
```

### Content Management

```typescript
// Upload image
const imageResult = await zalo.content.uploadImage(accessToken, imageBuffer);

// Create article
const article = await zalo.content.createArticle(accessToken, {
  title: "Welcome to our service",
  body: "Article content here...",
  cover_photo_id: imageResult.data.attachment_id,
});

// Share article to user
await zalo.content.shareArticle(accessToken, userId, article.data.id);
```

### Video Upload

```typescript
// Upload video
const uploadResult = await zalo.videoUpload.uploadVideo(
  accessToken,
  videoBuffer,
  "video.mp4"
);

// Wait for processing completion
const finalResult = await zalo.videoUpload.waitForUploadCompletion(
  accessToken,
  uploadResult.data.token
);

if (finalResult.status === "ready") {
  // Send video message
  await zalo.videoUpload.sendVideoMessage(
    accessToken,
    userId,
    finalResult.attachment_id,
    "Check out this video!"
  );
}
```

### Social API

```typescript
// Get user social information
const userInfo = await zalo.social.getUserInfo(accessToken);

// Get friends list
const friends = await zalo.social.getFriendsList(accessToken);

// Post to timeline
await zalo.social.postToTimeline(accessToken, "Hello from Zalo SDK!");
```

## 🔔 Webhook Events

The SDK provides comprehensive webhook event handling with **70+ event types**:

```typescript
import { WebhookHandlers } from "@warriorteam/redai-zalo-sdk";

const handlers: WebhookHandlers = {
  // User message events
  user_send_text: async (event) => {
    console.log("User sent:", event.message.text);
  },

  // Group events
  user_send_group_audio: async (event) => {
    console.log("Audio in group:", event.message.attachments[0].payload.url);
  },

  // ZNS events
  change_template_status: async (event) => {
    console.log("Template status:", event.status);
  },

  // Widget events
  widget_interaction_accepted: async (event) => {
    console.log("Widget accepted:", event.data.user_external_id);
  },

  // System events
  permission_revoked: async (event) => {
    console.log("Permission revoked by:", event.data.action_by);
  },
};
```

### Event Categories:

- **User Message Events** (11 types): text, image, video, audio, file, sticker, gif, location, link, business card, reaction
- **OA Message Events** (11 types): All message types + anonymous + template
- **Group Events** (22 types): Create, join, admin, leave + all message types for both user and OA
- **ZNS Events** (8 types): Quota, template, journey, delivery, status changes
- **Widget Events** (2 types): Interaction accepted, sync failures
- **System Events** (4 types): Permission, extension, user info, tags
- **Call Events** (2 types): OA call user, user call OA
- **Anonymous Events** (4 types): Anonymous chat support
- **Shop Events** (1 type): Order management

### 🎯 Message Classification Helpers

The SDK provides helper functions to easily classify webhook message events:

```typescript
import {
  isUserMessageEvent,
  isGroupMessageEvent,
  isOAToUserMessageEvent,
  isOAToGroupMessageEvent,
  getMessageDirection,
} from "@warriorteam/redai-zalo-sdk";

function handleWebhook(event: WebhookEvent) {
  if (isUserMessageEvent(event)) {
    console.log("Message from individual user");
  } else if (isGroupMessageEvent(event)) {
    console.log("Message from group");
  } else if (isOAToUserMessageEvent(event)) {
    console.log("OA sent message to user");
  } else if (isOAToGroupMessageEvent(event)) {
    console.log("OA sent message to group");
  }

  // Or use the unified helper
  const { direction, target, description } = getMessageDirection(event);
  console.log(`${direction} message to ${target}: ${description}`);
}
```

For complete webhook documentation, see:
- **[Webhook Events Guide](./docs/WEBHOOK_EVENTS.md)** - Complete guide for all 70+ webhook event types
- **[Message Classification Helpers](./docs/WEBHOOK_MESSAGE_HELPERS.md)** - Helper functions for message classification

## Changelog

### v1.0.0

- Initial release
- Official Account API support
- ZNS (Zalo Notification Service) support
- Group Message Framework (GMF) support
- Social API support
- User Management features
- Content and Video Upload capabilities
- Tag Management system
- Comprehensive webhook handling
- Social API support
- Authentication flow
- TypeScript support
