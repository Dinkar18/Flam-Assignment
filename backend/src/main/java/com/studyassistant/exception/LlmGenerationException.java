package com.studyassistant.exception;

/**
 * Thrown when study set generation fails after all retry attempts have been exhausted.
 */
public class LlmGenerationException extends LlmException {
    public LlmGenerationException(String message) {
        super("GENERATION_FAILED", message);
    }

    public LlmGenerationException(String message, Throwable cause) {
        super("GENERATION_FAILED", message, cause);
    }
}
