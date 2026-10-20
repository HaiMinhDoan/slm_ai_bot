package vn.slmglobal.slmai.adapters.facebook;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class FacebookSignatureVerifierTest {

    private static FacebookProperties props(String secret) {
        return new FacebookProperties("1", secret, "vt", "PAGE", "token", "v26.0", "https://graph.facebook.com");
    }

    private final FacebookSignatureVerifier verifier = new FacebookSignatureVerifier(props("test-secret"));
    private final byte[] body = "{\"object\":\"page\"}".getBytes(StandardCharsets.UTF_8);

    private String header(byte[] b) {
        return "sha256=" + HexFormat.of().formatHex(verifier.sign(b));
    }

    @Test
    void acceptsCorrectSignature() {
        assertThat(verifier.isValid(body, header(body))).isTrue();
    }

    @Test
    void rejectsSignatureOfOtherBody() {
        byte[] other = "{\"object\":\"user\"}".getBytes(StandardCharsets.UTF_8);
        assertThat(verifier.isValid(body, header(other))).isFalse();
    }

    @Test
    void rejectsMissingOrMalformedHeader() {
        assertThat(verifier.isValid(body, null)).isFalse();
        assertThat(verifier.isValid(body, "sha1=abc")).isFalse();
        assertThat(verifier.isValid(body, "sha256=not-hex")).isFalse();
    }

    @Test
    void rejectsEverythingWhenSecretNotConfigured() {
        FacebookSignatureVerifier noSecret = new FacebookSignatureVerifier(props(""));
        assertThat(noSecret.isValid(body, header(body))).isFalse();
    }
}
