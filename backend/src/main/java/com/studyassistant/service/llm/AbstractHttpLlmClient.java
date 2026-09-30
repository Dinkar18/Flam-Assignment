package com.studyassistant.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.config.LlmConfig;
import com.studyassistant.exception.LlmProviderException;
import com.studyassistant.exception.LlmTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Generic Template Method base class for all HTTP-based LLM providers.
 * Handles WebClient configuration, timeouts, JSON parsing, and unified error mapping.
 */
public abstract class AbstractHttpLlmClient implements LlmProvider {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final WebClient webClient;
    protected final LlmConfig llmConfig;
    protected final ObjectMapper objectMapper;

    protected AbstractHttpLlmClient(WebClient.Builder webClientBuilder, String baseUrl, LlmConfig llmConfig, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.llmConfig = llmConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateRawStudySet(String prompt, String requestId) {
        validateApiKey();

        String uri = resolveEndpointUri();
        Object requestBody = buildRequestBody(prompt);
        String model = getEffectiveModel();

        log.info("Dispatching LLM request to [{}] [requestId={}, model={}]", getProviderName(), requestId, model);

        try {
            String rawResponse = webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(buildHeaders())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(llmConfig.getTimeoutMs()))
                .block();

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new LlmProviderException("Received empty response from " + getProviderName());
            }

            JsonNode rootNode = objectMapper.readTree(rawResponse);
            return extractContentFromJson(rootNode);

        } catch (WebClientResponseException e) {
            log.error("{} API HTTP error [requestId={}, statusCode={}]: {}", getProviderName(), requestId, e.getStatusCode(), e.getMessage());
            throw new LlmProviderException(getProviderName() + " API returned HTTP status " + e.getStatusCode().value() + " (" + e.getStatusText() + ")", e);
        } catch (Exception e) {
            if (e.getCause() instanceof TimeoutException || e instanceof TimeoutException) {
                log.error("{} API call timed out [requestId={}]", getProviderName(), requestId);
                throw new LlmTimeoutException(getProviderName() + " call timed out after " + llmConfig.getTimeoutMs() + "ms", e);
            }
            if (e instanceof LlmProviderException lpe) {
                throw lpe;
            }
            log.error("{} API invocation failure [requestId={}]: {}", getProviderName(), requestId, e.getMessage());
            throw new LlmProviderException("Failed to invoke " + getProviderName() + " API: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the configured model or falls back to the provider's default model.
     */
    protected String getEffectiveModel() {
        return (llmConfig.getModel() != null && !llmConfig.getModel().isBlank())
            ? llmConfig.getModel()
            : getDefaultModel();
    }

    /**
     * Ensures API key is present before dispatching network calls.
     */
    protected void validateApiKey() {
        String apiKey = llmConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmProviderException("API key for " + getProviderName() + " is not configured. Please set the appropriate environment variable.");
        }
    }

    /**
     * Optional custom HTTP headers hook (e.g. Bearer token).
     */
    protected Consumer<HttpHeaders> buildHeaders() {
        return headers -> {};
    }

    /**
     * Default model name for this provider when not specified in configuration.
     */
    protected abstract String getDefaultModel();

    /**
     * Endpoint path/URI to invoke.
     */
    protected abstract String resolveEndpointUri();

    /**
     * Constructs provider-specific JSON request body.
     */
    protected abstract Object buildRequestBody(String prompt);

    /**
     * Extracts text content from the parsed JSON response tree.
     */
    protected abstract String extractContentFromJson(JsonNode rootNode);
}
