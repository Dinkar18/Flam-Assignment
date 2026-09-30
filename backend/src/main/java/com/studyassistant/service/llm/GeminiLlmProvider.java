package com.studyassistant.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.config.LlmConfig;
import com.studyassistant.constant.LlmConstants;
import com.studyassistant.exception.LlmProviderException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini provider implementation.
 */
@Component
public class GeminiLlmProvider extends AbstractHttpLlmClient {

    public GeminiLlmProvider(WebClient.Builder webClientBuilder, LlmConfig llmConfig, ObjectMapper objectMapper) {
        super(webClientBuilder, LlmConstants.GEMINI_BASE_URL, llmConfig, objectMapper);
    }

    @Override
    public String getProviderName() {
        return LlmConstants.PROVIDER_GEMINI;
    }

    @Override
    protected String getDefaultModel() {
        return LlmConstants.DEFAULT_GEMINI_MODEL;
    }

    @Override
    protected String resolveEndpointUri() {
        return String.format(LlmConstants.GEMINI_GENERATE_CONTENT_ENDPOINT, getEffectiveModel(), llmConfig.getApiKey());
    }

    @Override
    protected Object buildRequestBody(String prompt) {
        return Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            ),
            "generationConfig", Map.of(
                "responseMimeType", LlmConstants.APPLICATION_JSON_MIME,
                "temperature", LlmConstants.DEFAULT_TEMPERATURE
            )
        );
    }

    @Override
    protected String extractContentFromJson(JsonNode rootNode) {
        JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");
        if (!textNode.isMissingNode() && !textNode.asText().isBlank()) {
            return textNode.asText();
        }
        throw new LlmProviderException("Gemini response missing candidate parts");
    }
}
