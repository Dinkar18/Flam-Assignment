package com.studyassistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Standardized API error payload.
 */
public record ErrorResponse(
    ErrorBody error
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorBody(code, message, null));
    }

    public static ErrorResponse of(String code, String message, List<String> details) {
        return new ErrorResponse(new ErrorBody(code, message, details));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorBody(
        String code,
        String message,
        List<String> details
    ) {}
}
