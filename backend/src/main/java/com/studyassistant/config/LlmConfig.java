package com.studyassistant.config;

import com.studyassistant.constant.LlmConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Generic configuration properties for LLM providers (Groq, Gemini, etc.).
 */
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    private String provider = LlmConstants.PROVIDER_GROQ;
    private String apiKey = "";
    private String model = "";
    private int timeoutMs = 15000;
    private int maxAttempts = 2;

    public String getProvider() {
        if (provider != null && !provider.isBlank()) {
            return provider;
        }
        String sysProvider = System.getProperty("LLM_PROVIDER", System.getenv("LLM_PROVIDER"));
        return (sysProvider != null && !sysProvider.isBlank()) ? sysProvider : LlmConstants.PROVIDER_GROQ;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }

        String fallback = System.getProperty("LLM_API_KEY", System.getenv("LLM_API_KEY"));
        return (fallback != null) ? fallback.trim() : "";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
}
