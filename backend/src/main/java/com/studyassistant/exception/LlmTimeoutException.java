package com.studyassistant.exception;

/**
 * Thrown when an LLM upstream call exceeds the configured timeout limit.
 */
public class LlmTimeoutException extends LlmException {
    public LlmTimeoutException(String message) {
        super("LLM_TIMEOUT", message);
    }

    public LlmTimeoutException(String message, Throwable cause) {
        super("LLM_TIMEOUT", message, cause);
    }
}
