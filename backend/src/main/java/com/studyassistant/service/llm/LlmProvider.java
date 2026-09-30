package com.studyassistant.service.llm;

import com.studyassistant.exception.LlmException;

/**
 * Generic contract for any LLM provider (Gemini, Groq, Anthropic, local models, etc.).
 */
public interface LlmProvider {

    /**
     * Sends the prompt to the upstream provider and returns the raw JSON string.
     *
     * @param prompt full prompt with instructions
     * @param requestId correlation ID for distributed tracing and logging
     * @return raw JSON string returned by the provider
     * @throws LlmException on provider failure, timeout, or invalid HTTP status
     */
    String generateRawStudySet(String prompt, String requestId) throws LlmException;

    /**
     * Unique identifier for this provider (e.g., "gemini", "groq", "anthropic").
     */
    String getProviderName();

    /**
     * Checks if this provider handles the requested provider name.
     */
    default boolean supports(String providerName) {
        return getProviderName().equalsIgnoreCase(providerName);
    }
}
