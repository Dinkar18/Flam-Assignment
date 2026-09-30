package com.studyassistant.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.config.LlmConfig;
import com.studyassistant.constant.LlmConstants;
import com.studyassistant.exception.LlmProviderException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Groq LLM provider implementation for ultra-fast structured inference.
 */
@Component
public class GroqLlmProvider extends AbstractHttpLlmClient {

    public GroqLlmProvider(WebClient.Builder webClientBuilder, LlmConfig llmConfig, ObjectMapper objectMapper) {
        super(webClientBuilder, LlmConstants.GROQ_BASE_URL, llmConfig, objectMapper);
    }

    @Override
    public String getProviderName() {
        return LlmConstants.PROVIDER_GROQ;
    }

    @Override
    protected String getDefaultModel() {
        return LlmConstants.DEFAULT_GROQ_MODEL;
    }

    @Override
    protected String resolveEndpointUri() {
        return LlmConstants.GROQ_CHAT_COMPLETIONS_ENDPOINT;
    }

    @Override
    protected Consumer<HttpHeaders> buildHeaders() {
        return headers -> headers.setBearerAuth(llmConfig.getApiKey());
    }

    @Override
    protected Object buildRequestBody(String prompt) {
        return Map.of(
            "model", getEffectiveModel(),
            "messages", List.of(
                Map.of("role", "system", "content", "You are a pedagogical study assistant that outputs strictly structured JSON."),
                Map.of("role", "user", "content", prompt)
            ),
            "response_format", Map.of("type", LlmConstants.JSON_OBJECT_TYPE),
            "temperature", LlmConstants.DEFAULT_TEMPERATURE
        );
    }

    @Override
    protected String extractContentFromJson(JsonNode rootNode) {
        JsonNode contentNode = rootNode.at("/choices/0/message/content");
        if (!contentNode.isMissingNode() && !contentNode.asText().isBlank()) {
            return contentNode.asText();
        }
        throw new LlmProviderException("Groq response missing message content");
    }
}
