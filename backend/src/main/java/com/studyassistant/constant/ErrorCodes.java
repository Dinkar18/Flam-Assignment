package com.studyassistant.constant;

/**
 * Centralized, standardized error code constants across the backend.
 * Eliminates magic string literals and ensures compile-time safety and client contract consistency.
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    // Global & Client Validation Error Codes
    public static final String INVALID_INPUT = "INVALID_INPUT";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    // LLM Provider & Infrastructure Error Codes
    public static final String LLM_TIMEOUT = "LLM_TIMEOUT";
    public static final String GENERATION_FAILED = "GENERATION_FAILED";
    public static final String PROVIDER_ERROR = "PROVIDER_ERROR";

    // LLM Response Structural & Schema Validation Error Codes
    public static final String EMPTY_RESPONSE = "EMPTY_RESPONSE";
    public static final String MALFORMED_JSON = "MALFORMED_JSON";
    public static final String INVALID_ROOT_TYPE = "INVALID_ROOT_TYPE";
    public static final String MISSING_QUESTIONS_ARRAY = "MISSING_QUESTIONS_ARRAY";
    public static final String QUESTION_COUNT_MISMATCH = "QUESTION_COUNT_MISMATCH";
    public static final String INVALID_QUESTION_TYPE = "INVALID_QUESTION_TYPE";
    public static final String MISSING_QUESTION_ID = "MISSING_QUESTION_ID";
    public static final String DUPLICATE_QUESTION_ID = "DUPLICATE_QUESTION_ID";
    public static final String MISSING_QUESTION_TEXT = "MISSING_QUESTION_TEXT";
    public static final String QUESTION_TOO_LONG = "QUESTION_TOO_LONG";
    public static final String DUPLICATE_QUESTION_TEXT = "DUPLICATE_QUESTION_TEXT";
    public static final String MISSING_ANSWER_TEXT = "MISSING_ANSWER_TEXT";
    public static final String ANSWER_TOO_LONG = "ANSWER_TOO_LONG";
    public static final String MISSING_DIFFICULTY = "MISSING_DIFFICULTY";
    public static final String INVALID_DIFFICULTY = "INVALID_DIFFICULTY";
    public static final String MISSING_OPTIONS = "MISSING_OPTIONS";
    public static final String INVALID_OPTIONS = "INVALID_OPTIONS";
    public static final String ANSWER_NOT_IN_OPTIONS = "ANSWER_NOT_IN_OPTIONS";
}
