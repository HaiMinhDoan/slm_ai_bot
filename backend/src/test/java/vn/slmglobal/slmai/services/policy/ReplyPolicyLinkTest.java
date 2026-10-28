package vn.slmglobal.slmai.services.policy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Link trong tin AI: chỉ link có trong kết quả tool, link trang đã gửi, trang chủ theo miền. */
class ReplyPolicyLinkTest {

    private static final List<String> ALLOWED = List.of("slmsolar.vn", "slmsolar.com.vn",
            "https://slmsolar.vn/products/105");

    @Test
    void allowsToolLinksWithPunctuationAndHomepages() {
        assertThat(ReplyPolicy.linksAllowed("Chị xem tại https://slmsolar.vn/products/105.", ALLOWED)).isTrue();
        assertThat(ReplyPolicy.linksAllowed("Link: https://slmsolar.vn/products/105/ ạ", ALLOWED)).isTrue();
        assertThat(ReplyPolicy.linksAllowed("Website bên em: slmsolar.com.vn", ALLOWED)).isTrue();
        assertThat(ReplyPolicy.linksAllowed("Không có link nào cả", ALLOWED)).isTrue();
    }

    @Test
    void blocksOtherLinks() {
        assertThat(ReplyPolicy.linksAllowed("Xem https://slmsolar.vn/products/999", ALLOWED)).isFalse();
        assertThat(ReplyPolicy.linksAllowed("Xem https://slmsolar.com/products/105", ALLOWED)).isFalse();
        assertThat(ReplyPolicy.linksAllowed("Xem www.example.com nhé", ALLOWED)).isFalse();
        assertThat(ReplyPolicy.linksAllowed("Gửi mail cho em qua abc@gmail.com", ALLOWED)).isFalse();
        assertThat(ReplyPolicy.linksAllowed("Xem https://slmsolar.vn/products/105", List.of())).isFalse();
    }

    @Test
    void extractsLinks() {
        assertThat(ReplyPolicy.links("A https://slmsolar.com.vn/products/97, B slmsolar.vn."))
                .containsExactly("https://slmsolar.com.vn/products/97", "slmsolar.vn");
    }
}
