package vn.slmglobal.slmai.common.security;

import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/** Request bất đồng bộ (SSE) không được làm rò người dùng sang request khác, cũng không ghi 401 vào stream. */
class AuthInterceptorAsyncTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final AuthInterceptor interceptor = new AuthInterceptor(jwtService, mock(TokenBlacklist.class));

    @Test
    void asyncDispatchIsNotReAuthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/notifications/stream");
        request.setDispatcherType(DispatcherType.ASYNC);
        request.addHeader("Authorization", "Bearer token-da-het-han");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(jwtService);
    }

    @Test
    void contextIsClearedWhenAsyncHandlingStarts() {
        RequestContext.set("/api/v1/notifications/stream", "vi",
                new AuthInfo(java.util.UUID.randomUUID(), "a@b.c", "A", java.util.Set.of(RoleCode.STAFF)));

        interceptor.afterConcurrentHandlingStarted(new MockHttpServletRequest(), new MockHttpServletResponse(),
                new Object());

        assertThat(RequestContext.getAuthInfo()).isNull();
    }
}
