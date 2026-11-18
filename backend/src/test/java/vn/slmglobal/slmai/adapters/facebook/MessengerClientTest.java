package vn.slmglobal.slmai.adapters.facebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

class MessengerClientTest {

    private static final String BASE = "https://graph.test";
    private MockRestServiceServer server;
    private MessengerClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new MessengerClient(builder,
                new FacebookProperties("1", "s", "vt", "PAGE", "PAGE_TOKEN", "v26.0", BASE));
    }

    @Test
    void sendsTextAsResponseAndReturnsMessageId() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/messages")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(queryParam("access_token", "PAGE_TOKEN"))
                .andExpect(content().json("""
                        {"recipient":{"id":"PSID_1"},"messaging_type":"RESPONSE","message":{"text":"Xin chào"}}
                        """, true))
                .andRespond(withSuccess("{\"recipient_id\":\"PSID_1\",\"message_id\":\"m_abc\"}",
                        MediaType.APPLICATION_JSON));

        MessengerClient.SendResult result = client.sendText("PSID_1", "Xin chào", false);

        assertThat(result.messageId()).isEqualTo("m_abc");
        server.verify();
    }

    /** Graph API thật trả Content-Type text/javascript; trước đây đọc lỗi nên tin đã gửi bị ghi FAILED */
    @Test
    void acceptsGraphJsonServedAsTextJavascript() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/messages")))
                .andRespond(withSuccess("{\"recipient_id\":\"PSID_1\",\"message_id\":\"m_js\"}",
                        MediaType.parseMediaType("text/javascript;charset=UTF-8")));

        assertThat(client.sendText("PSID_1", "Xin chào", false).messageId()).isEqualTo("m_js");
    }

    @Test
    void humanAgentTagUsesMessageTag() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/messages")))
                .andExpect(content().json("""
                        {"messaging_type":"MESSAGE_TAG","tag":"HUMAN_AGENT"}
                        """, false))
                .andRespond(withSuccess("{\"message_id\":\"m_1\"}", MediaType.APPLICATION_JSON));

        assertThat(client.sendText("PSID_1", "Dạ em gọi lại anh nhé", true).messageId()).isEqualTo("m_1");
    }

    /** Ảnh gửi dạng tệp (không gửi link ảnh): multipart filedata, is_reusable để lần sau gửi theo attachment_id */
    @Test
    void uploadsFileAttachmentAndReusesItById() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/messages")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(allOf(
                        containsString("name=\"recipient\""), containsString("{\"id\":\"PSID_1\"}"),
                        containsString("\"is_reusable\":true"), containsString("\"type\":\"image\""),
                        containsString("name=\"filedata\"; filename=\"bao-gia-156.jpg\""),
                        containsString("Content-Type: image/jpeg"), containsString("JPEGDATA"))))
                .andRespond(withSuccess("{\"recipient_id\":\"PSID_1\",\"message_id\":\"m_img\","
                        + "\"attachment_id\":\"att_1\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/messages")))
                .andExpect(content().json("""
                        {"recipient":{"id":"PSID_2"},"messaging_type":"RESPONSE",
                         "message":{"attachment":{"type":"image","payload":{"attachment_id":"att_1"}}}}
                        """, true))
                .andRespond(withSuccess("{\"message_id\":\"m_again\"}", MediaType.APPLICATION_JSON));

        MessengerClient.AttachmentResult first = client.sendAttachment("PSID_1", "image",
                "JPEGDATA".getBytes(), "image/jpeg", "bao-gia-156.jpg");
        assertThat(first.messageId()).isEqualTo("m_img");
        assertThat(first.attachmentId()).isEqualTo("att_1");
        assertThat(client.sendAttachmentById("PSID_2", "image", "att_1").messageId()).isEqualTo("m_again");
        server.verify();
    }

    @Test
    void graphErrorIsReportedWithoutToken() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/messages")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {"error":{"message":"(#10) This message is sent outside of allowed window.",
                                  "type":"OAuthException","code":10,"error_subcode":2018278,"fbtrace_id":"AbC"}}
                        """));

        assertThatThrownBy(() -> client.sendText("PSID_1", "Chào", false))
                .isInstanceOf(MessengerApiException.class)
                .hasMessageContaining("code=10")
                .hasMessageContaining("subcode=2018278")
                .hasMessageNotContaining("PAGE_TOKEN");
    }

    /** User Profile API cần tính năng app chưa có (400 subcode 33); tên lấy từ người tham gia cuộc trò chuyện */
    @Test
    void fetchesNameFromConversationParticipants() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/conversations")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("platform", "messenger"))
                .andExpect(queryParam("user_id", "PSID_1"))
                .andExpect(queryParam("fields", "participants"))
                .andExpect(queryParam("access_token", "PAGE_TOKEN"))
                .andRespond(withSuccess("""
                        {"data":[{"participants":{"data":[
                            {"name":"Solarmax","id":"PAGE"},
                            {"name":" Thùy Giang ","email":"PSID_1@facebook.com","id":"PSID_1"}]},
                          "id":"t_1"}]}
                        """, MediaType.parseMediaType("text/javascript;charset=UTF-8")));

        assertThat(client.fetchUserName("PSID_1")).isEqualTo("Thùy Giang");
        server.verify();
    }

    @Test
    void nameIsNullWhenNoConversationFound() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/conversations")))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.fetchUserName("PSID_1")).isNull();
    }

    @Test
    void rejectsTooLongText() {
        assertThatThrownBy(() -> client.sendText("PSID_1", "a".repeat(MessengerClient.MAX_TEXT_LENGTH + 1), false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
