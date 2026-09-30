package com.studyassistant.service.llm;

import com.studyassistant.exception.LlmException;

/**
 * Common contract for LLM providers.
 */
public interface LlmService {

    /**
     * Sends the prompt to the upstream LLM provider and returns the raw response string.
     *
     * @param prompt full formatted prompt
     * @param requestId correlation ID for logging and tracing
     * @return raw response string from the provider
     * @throws LlmException on provider failure, rate limit, or timeout
     */
    String generateRawStudySet(String prompt, String requestId) throws LlmException;

    /**
     * Identifies the provider name for observability.
     */
    String getProviderName();
}
