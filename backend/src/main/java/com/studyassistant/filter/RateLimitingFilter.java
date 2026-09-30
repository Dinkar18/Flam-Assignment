package com.studyassistant.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.constant.ApiConstants;
import com.studyassistant.constant.ErrorCodes;
import com.studyassistant.dto.ErrorResponse;
import com.studyassistant.service.ratelimit.RateLimiterService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Production-ready OncePerRequestFilter enforcing IP-level token bucket rate limits on study generation.
 * Emits HTTP 429 Too Many Requests with standard JSON error payload and rate-limit headers.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only apply rate limiting to the heavy LLM study generation endpoint
        return !path.startsWith(ApiConstants.API_BASE + ApiConstants.STUDY_ENDPOINT);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        ConsumptionProbe probe = rateLimiterService.tryConsume(clientIp);

        if (probe == null || probe.isConsumed()) {
            if (probe != null) {
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Rate limit exceeded: return HTTP 429 Too Many Requests
        long waitForRefillSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        log.warn("Rate limit exceeded for IP [{}] on [{}] - retry after {}s", clientIp, request.getRequestURI(), waitForRefillSeconds);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefillSeconds));

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCodes.RATE_LIMIT_EXCEEDED,
            "Too many study generation requests. Please wait before trying again.",
            List.of("Rate limit reached. Please retry in " + waitForRefillSeconds + " seconds.")
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
