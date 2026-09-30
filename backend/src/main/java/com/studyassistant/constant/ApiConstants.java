package com.studyassistant.constant;

/**
 * REST API routing paths and service metadata constants.
 */
public final class ApiConstants {

    private ApiConstants() {
    }

    public static final String API_BASE = "/api";
    public static final String STUDY_ENDPOINT = "/study";
    public static final String HEALTH_ENDPOINT = "/health";

    public static final String SERVICE_NAME = "ai-study-assistant";
    public static final String STATUS_UP = "UP";
    public static final String STATUS_KEY = "status";
    public static final String SERVICE_KEY = "service";
    public static final String LLM_PROVIDER_KEY = "llmProvider";
}
