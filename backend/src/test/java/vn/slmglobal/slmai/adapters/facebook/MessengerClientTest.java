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

    @Test
    void humanAgentTagUsesMessageTag() {
        server.expect(requestTo(startsWith(BASE + "/v26.0/me/messages")))
                .andExpect(content().json("""
                        {"messaging_type":"MESSAGE_TAG","tag":"HUMAN_AGENT"}
                        """, false))
                .andRespond(withSuccess("{\"message_id\":\"m_1\"}", MediaType.APPLICATION_JSON));

        assertThat(client.sendText("PSID_1", "Dạ em gọi lại anh nhé", true).messageId()).isEqualTo("m_1");
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

    @Test
    void rejectsTooLongText() {
        assertThatThrownBy(() -> client.sendText("PSID_1", "a".repeat(MessengerClient.MAX_TEXT_LENGTH + 1), false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
