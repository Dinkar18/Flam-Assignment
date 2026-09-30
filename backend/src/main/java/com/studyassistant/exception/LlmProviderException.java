package com.studyassistant.exception;

/**
 * Thrown when an LLM provider returns an HTTP error or unreachable network response.
 */
public class LlmProviderException extends LlmException {
    public LlmProviderException(String message) {
        super("PROVIDER_ERROR", message);
    }

    public LlmProviderException(String message, Throwable cause) {
        super("PROVIDER_ERROR", message, cause);
    }
}
