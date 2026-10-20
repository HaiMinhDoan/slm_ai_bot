package vn.slmglobal.slmai.adapters.facebook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Webhook Messenger / Page (mục 54.3). Đường dẫn khai báo ở Meta App: https://&lt;tên-miền&gt;/api/v1/webhooks/facebook
 * Không qua đăng nhập (WebConfig loại trừ /api/v1/webhooks/**): tự xác thực bằng chữ ký của Meta.
 */
@Slf4j
@Tag(name = "Webhook Facebook")
@RestController
@RequestMapping("/api/v1/webhooks/facebook")
@RequiredArgsConstructor
public class FacebookWebhookController {

    private final FacebookProperties properties;
    private final FacebookSignatureVerifier signatureVerifier;
    private final FacebookWebhookIngestService ingestService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Meta gọi để xác minh webhook khi đăng ký (hub.challenge)")
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verify(@RequestParam(name = "hub.mode", required = false) String mode,
                                         @RequestParam(name = "hub.verify_token", required = false) String token,
                                         @RequestParam(name = "hub.challenge", required = false) String challenge) {
        String expected = properties.verifyToken();
        boolean ok = "subscribe".equals(mode) && expected != null && !expected.isBlank() && token != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            log.warn("Từ chối xác minh webhook Facebook (mode={})", mode);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("forbidden");
        }
        return ResponseEntity.ok(challenge == null ? "" : challenge);
    }

    @Operation(summary = "Meta gửi sự kiện tin nhắn, echo, postback, bình luận")
    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> receive(@RequestBody byte[] body,
                                          @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature) {
        if (!signatureVerifier.isValid(body, signature)) {
            log.warn("Webhook Facebook sai chữ ký hoặc chưa cấu hình FB_APP_SECRET ({} byte)", body.length);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("invalid signature");
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("invalid json");
        }
        int stored = ingestService.ingest(root);
        log.debug("Webhook Facebook: ghi {} sự kiện mới", stored);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
