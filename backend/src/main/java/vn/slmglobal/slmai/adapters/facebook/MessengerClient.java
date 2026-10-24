package vn.slmglobal.slmai.adapters.facebook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
     * Tên Facebook của khách qua User Profile API (GET /{psid}?fields=name). Chỉ gọi được sau khi khách đã nhắn
     * cho Trang; trả null khi không lấy được.
     */
    public String fetchUserName(String psid) {
        if (!properties.hasPageAccessToken()) {
            throw new MessengerApiException("Chưa cấu hình FB_PAGE_ACCESS_TOKEN");
        }
        JsonNode profile = restClient.get()
                .uri(b -> b.path("/{version}/{psid}")
                        .queryParam("fields", "name,first_name,last_name")
                        .queryParam("access_token", properties.pageAccessToken())
                        .build(properties.graphApiVersion(), psid))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                    throw new MessengerApiException("User Profile API lỗi HTTP " + resp.getStatusCode().value()
                            + ": " + MessengerApiException.describeGraphError(resp.getBody().readAllBytes()));
                })
                .body(JsonNode.class);
        if (profile == null) {
            return null;
        }
        String name = profile.path("name").asText("").strip();
        if (name.isEmpty()) {
            name = (profile.path("last_name").asText("") + " " + profile.path("first_name").asText("")).strip();
        }
        return name.isEmpty() ? null : name;
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
