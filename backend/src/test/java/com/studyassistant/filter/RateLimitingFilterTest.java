package com.studyassistant.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.config.RateLimitConfig;
import com.studyassistant.constant.ErrorCodes;
import com.studyassistant.service.ratelimit.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private RateLimiterService rateLimiterService;
    private RateLimitConfig config;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        config.setEnabled(true);
        config.setCapacity(2); // Set capacity to 2 for quick testing
        config.setDurationMinutes(1);

        rateLimiterService = new RateLimiterService(config);
        filter = new RateLimitingFilter(rateLimiterService, new ObjectMapper());
    }

    @Test
    @DisplayName("Should allow requests within capacity and attach remaining header")
    void shouldAllowRequestsWithinCapacity() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/study");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
    }

    @Test
    @DisplayName("Should block requests when capacity is exceeded and return 429 Too Many Requests")
    void shouldBlockWhenCapacityExceeded() throws Exception {
        String clientIp = "192.168.1.200";

        // Request 1: OK
        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/api/study");
        req1.setRemoteAddr(clientIp);
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, new MockFilterChain());
        assertThat(res1.getStatus()).isEqualTo(200);

        // Request 2: OK
        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/study");
        req2.setRemoteAddr(clientIp);
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, new MockFilterChain());
        assertThat(res2.getStatus()).isEqualTo(200);

        // Request 3: 429 Too Many Requests
        MockHttpServletRequest req3 = new MockHttpServletRequest("POST", "/api/study");
        req3.setRemoteAddr(clientIp);
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        filter.doFilter(req3, res3, new MockFilterChain());

        assertThat(res3.getStatus()).isEqualTo(429);
        assertThat(res3.getHeader("X-Rate-Limit-Retry-After-Seconds")).isNotNull();
        assertThat(res3.getContentAsString()).contains(ErrorCodes.RATE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("Should bypass rate limiting on health check endpoint")
    void shouldBypassOnNonStudyEndpoints() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        request.setRemoteAddr("192.168.1.300");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNull();
    }
}
