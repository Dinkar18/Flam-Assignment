package com.studyassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for IP-based Token Bucket rate limiting.
 */
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitConfig {

    private boolean enabled = true;
    private int capacity = 10;
    private int durationMinutes = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
