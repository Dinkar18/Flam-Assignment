package com.studyassistant.exception;

import com.studyassistant.constant.ErrorCodes;
import com.studyassistant.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler providing sanitized, standardized JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .toList();

        log.warn("Client validation error: {}", details);

        ErrorResponse response = ErrorResponse.of(
            ErrorCodes.INVALID_INPUT,
            "Please correct the errors in your submission.",
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(LlmTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(LlmTimeoutException ex) {
        log.warn("LLM timeout exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            ErrorCodes.LLM_TIMEOUT,
            "The AI service took too long to respond. Please try again."
        );
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    @ExceptionHandler(LlmGenerationException.class)
    public ResponseEntity<ErrorResponse> handleGenerationException(LlmGenerationException ex) {
        log.error("Study set generation failed after retries: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            ErrorCodes.GENERATION_FAILED,
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(LlmProviderException.class)
    public ResponseEntity<ErrorResponse> handleProviderException(LlmProviderException ex) {
        log.error("Upstream LLM provider failure: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            ErrorCodes.PROVIDER_ERROR,
            "The study assistant service encountered an upstream provider error. Please try again."
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(LlmException.class)
    public ResponseEntity<ErrorResponse> handleLlmGenericException(LlmException ex) {
        log.error("LLM general exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            ex.getErrorCode(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.debug("Static resource not found: {}", ex.getResourcePath());
        ErrorResponse response = ErrorResponse.of(
            "NOT_FOUND",
            "Resource not found: " + ex.getResourcePath()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleCatchAll(Exception ex) {
        log.error("Unhandled server exception: {}", ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.of(
            ErrorCodes.INTERNAL_SERVER_ERROR,
            "An unexpected internal error occurred. Please try again."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
