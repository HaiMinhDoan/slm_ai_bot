package vn.slmglobal.slmai.adapters.facebook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gọi Send API của Messenger: POST /{version}/me/messages (mục 54.3).
 * Chỉ là lớp gọi API; kiểm tra cửa sổ 24 giờ, Policy và ghi tin_nhan nằm ở tầng hội thoại.
 */
@Component
public class MessengerClient {

    /** Messenger giới hạn 2000 ký tự cho một tin văn bản */
    public static final int MAX_TEXT_LENGTH = 2000;

    private final RestClient restClient;
    private final FacebookProperties properties;

    public MessengerClient(RestClient.Builder builder, FacebookProperties properties) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.graphApiBaseUrl())
                .messageConverters(converters -> converters.add(0, graphJsonConverter()))
                .build();
    }

    /** Graph API trả JSON nhưng nhiều endpoint ghi Content-Type là text/javascript */
    private static MappingJackson2HttpMessageConverter graphJsonConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, new MediaType("text", "javascript")));
        return converter;
    }

    public record SendResult(String messageId, String recipientId) {
    }

    /** Tin đính kèm đã gửi; {@code attachmentId} dùng lại được cho lần gửi sau (tải lên với is_reusable) */
    public record AttachmentResult(String messageId, String attachmentId) {
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * Gửi tin văn bản.
     *
     * @param humanAgentTag true khi người thật trả lời ngoài cửa sổ 24 giờ (tag HUMAN_AGENT, tối đa 7 ngày);
     *                      AI không bao giờ dùng tag này
     */
    public SendResult sendText(String psid, String text, boolean humanAgentTag) {
        if (!properties.hasPageAccessToken()) {
            throw new MessengerApiException("Chưa cấu hình FB_PAGE_ACCESS_TOKEN");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Tin nhắn rỗng");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Tin Messenger tối đa " + MAX_TEXT_LENGTH + " ký tự");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("recipient", Map.of("id", psid));
        if (humanAgentTag) {
            body.put("messaging_type", "MESSAGE_TAG");
            body.put("tag", "HUMAN_AGENT");
        } else {
            body.put("messaging_type", "RESPONSE");
        }
        body.put("message", Map.of("text", text));

        JsonNode response = restClient.post()
                .uri(b -> b.path("/{version}/me/messages")
                        .queryParam("access_token", properties.pageAccessToken())
                        .build(properties.graphApiVersion()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                    throw new MessengerApiException("Send API lỗi HTTP " + resp.getStatusCode().value() + ": "
                            + MessengerApiException.describeGraphError(resp.getBody().readAllBytes()));
                })
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("message_id")) {
            throw new MessengerApiException("Send API không trả message_id");
        }
        return new SendResult(response.path("message_id").asText(), response.path("recipient_id").asText(null));
    }

    /**
     * Tải lên và gửi một tệp đính kèm (ảnh / video) trong cửa sổ 24 giờ: multipart {@code filedata} kèm
     * {@code is_reusable}, Meta trả {@code attachment_id} để gửi lại tệp này cho khách khác không phải tải lên nữa.
     *
     * @param type {@code image} hoặc {@code video}
     */
    public AttachmentResult sendAttachment(String psid, String type, byte[] content, String mimeType,
                                           String fileName) {
        requireToken();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("recipient", json(Map.of("id", psid)));
        body.add("messaging_type", "RESPONSE");
        body.add("message", json(Map.of("attachment", Map.of("type", type, "payload", Map.of("is_reusable", true)))));
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(mimeType));
        fileHeaders.setContentDispositionFormData("filedata", fileName);
        body.add("filedata", new HttpEntity<>(new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        }, fileHeaders));
        JsonNode response = restClient.post()
                .uri(b -> b.path("/{version}/me/messages")
                        .queryParam("access_token", properties.pageAccessToken())
                        .build(properties.graphApiVersion()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                    throw new MessengerApiException("Send API (tệp) lỗi HTTP " + resp.getStatusCode().value() + ": "
                            + MessengerApiException.describeGraphError(resp.getBody().readAllBytes()));
                })
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("message_id")) {
            throw new MessengerApiException("Send API không trả message_id");
        }
        return new AttachmentResult(response.path("message_id").asText(), response.path("attachment_id").asText(null));
    }

    /** Gửi lại một tệp đã tải lên theo attachment_id */
    public SendResult sendAttachmentById(String psid, String type, String attachmentId) {
        requireToken();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("recipient", Map.of("id", psid));
        body.put("messaging_type", "RESPONSE");
        body.put("message", Map.of("attachment", Map.of("type", type, "payload",
                Map.of("attachment_id", attachmentId))));
        JsonNode response = restClient.post()
                .uri(b -> b.path("/{version}/me/messages")
                        .queryParam("access_token", properties.pageAccessToken())
                        .build(properties.graphApiVersion()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                    throw new MessengerApiException("Send API (tệp đã tải) lỗi HTTP " + resp.getStatusCode().value()
                            + ": " + MessengerApiException.describeGraphError(resp.getBody().readAllBytes()));
                })
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("message_id")) {
            throw new MessengerApiException("Send API không trả message_id");
        }
        return new SendResult(response.path("message_id").asText(), response.path("recipient_id").asText(null));
    }

    private void requireToken() {
        if (!properties.hasPageAccessToken()) {
            throw new MessengerApiException("Chưa cấu hình FB_PAGE_ACCESS_TOKEN");
        }
    }

    private static String json(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Tên Facebook của khách, lấy từ người tham gia cuộc trò chuyện giữa Trang và khách
     * (GET /me/conversations?platform=messenger&user_id={psid}&fields=participants; cần pages_messaging,
     * pages_manage_metadata, pages_read_engagement). Không dùng User Profile API (GET /{psid}) vì API đó cần tính năng
     * Business Asset User Profile Access mà app chưa có: Meta trả 400 code=100 subcode=33 cho mọi khách.
     * Trả null khi không lấy được.
     */
    public String fetchUserName(String psid) {
        if (!properties.hasPageAccessToken()) {
            throw new MessengerApiException("Chưa cấu hình FB_PAGE_ACCESS_TOKEN");
        }
        JsonNode response = restClient.get()
                .uri(b -> b.path("/{version}/me/conversations")
                        .queryParam("platform", "messenger")
                        .queryParam("user_id", psid)
                        .queryParam("fields", "participants")
                        .queryParam("access_token", properties.pageAccessToken())
                        .build(properties.graphApiVersion()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                    throw new MessengerApiException("Conversations API lỗi HTTP " + resp.getStatusCode().value()
                            + ": " + MessengerApiException.describeGraphError(resp.getBody().readAllBytes()));
                })
                .body(JsonNode.class);
        if (response == null) {
            return null;
        }
        // Người tham gia gồm khách và chính Trang: chỉ lấy người có id là PSID của khách
        for (JsonNode conversation : response.path("data")) {
            for (JsonNode participant : conversation.path("participants").path("data")) {
                String name = participant.path("name").asText("").strip();
                if (psid.equals(participant.path("id").asText()) && !name.isEmpty()) {
                    return name;
                }
            }
        }
        return null;
    }

    /** Hành động người gửi: {@code mark_seen}, {@code typing_on}, {@code typing_off}. */
    public void senderAction(String psid, String action) {
        if (!properties.hasPageAccessToken()) {
            throw new MessengerApiException("Chưa cấu hình FB_PAGE_ACCESS_TOKEN");
        }
        restClient.post()
                .uri(b -> b.path("/{version}/me/messages")
                        .queryParam("access_token", properties.pageAccessToken())
                        .build(properties.graphApiVersion()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("recipient", Map.of("id", psid), "sender_action", action))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                    throw new MessengerApiException("Send API (sender_action) lỗi HTTP " + resp.getStatusCode().value()
                            + ": " + MessengerApiException.describeGraphError(resp.getBody().readAllBytes()));
                })
                .toBodilessEntity();
    }
}
