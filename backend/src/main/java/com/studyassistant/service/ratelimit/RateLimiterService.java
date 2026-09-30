package com.studyassistant.service.ratelimit;

import com.studyassistant.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe IP Token Bucket Rate Limiter Service using Bucket4j.
 * Manages token buckets per client IP with greedy refill.
 */
@Service
public class RateLimiterService {

    private final RateLimitConfig config;
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    public RateLimiterService(RateLimitConfig config) {
        this.config = config;
    }

    /**
     * Attempts to consume 1 token for the specified client key (e.g. IP address).
     *
     * @param key client identifier (IP address)
     * @return ConsumptionProbe containing remaining tokens and nanos to wait, or null if rate limiting is disabled
     */
    public ConsumptionProbe tryConsume(String key) {
        if (!config.isEnabled()) {
            return null;
        }
        Bucket bucket = bucketCache.computeIfAbsent(key, this::createNewBucket);
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createNewBucket(String key) {
        int capacity = Math.max(1, config.getCapacity());
        int durationMinutes = Math.max(1, config.getDurationMinutes());

        Bandwidth limit = Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(capacity, Duration.ofMinutes(durationMinutes))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
