package com.studyassistant.exception;

/**
 * Thrown when an LLM response fails syntax, structural, or business validation.
 */
public class InvalidLlmResponseException extends LlmException {
    public InvalidLlmResponseException(String failureReason, String message) {
        super(failureReason, message);
    }

    public InvalidLlmResponseException(String failureReason, String message, Throwable cause) {
        super(failureReason, message, cause);
    }
}
