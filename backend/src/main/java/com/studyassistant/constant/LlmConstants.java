package com.studyassistant.constant;

/**
 * Constants for LLM provider identifiers, models, default endpoints, and prompt metadata.
 */
public final class LlmConstants {

    private LlmConstants() {
    }

    // Prompt Version
    public static final String PROMPT_VERSION = "study-v5-enhanced";

    // Provider Names
    public static final String PROVIDER_GROQ = "groq";
    public static final String PROVIDER_GEMINI = "gemini";
    public static final String DEFAULT_PROVIDER = "gemini";

    // Default Models
    public static final String DEFAULT_GROQ_MODEL = "openai/gpt-oss-120b";
    public static final String DEFAULT_GEMINI_MODEL = "gemini-1.5-flash";

    // Provider Base URLs
    public static final String GROQ_BASE_URL = "https://api.groq.com";
    public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com";

    // Endpoints
    public static final String GROQ_CHAT_COMPLETIONS_ENDPOINT = "/openai/v1/chat/completions";
    public static final String GEMINI_GENERATE_CONTENT_ENDPOINT = "/v1beta/models/%s:generateContent?key=%s";

    // Generation Parameters
    public static final double DEFAULT_TEMPERATURE = 0.2;
    public static final String JSON_OBJECT_TYPE = "json_object";
    public static final String APPLICATION_JSON_MIME = "application/json";
}
