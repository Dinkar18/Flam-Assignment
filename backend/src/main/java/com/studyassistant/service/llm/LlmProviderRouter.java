package com.studyassistant.service.llm;

import com.studyassistant.config.LlmConfig;
import com.studyassistant.constant.LlmConstants;
import com.studyassistant.exception.LlmProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extensible Provider Registry and Router.
 * Automatically discovers all LlmProvider components and dispatches requests dynamically.
 */
@Service
@Primary
public class LlmProviderRouter implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmProviderRouter.class);

    private final Map<String, LlmProvider> providerMap;
    private final LlmConfig llmConfig;

    public LlmProviderRouter(List<LlmProvider> providers, LlmConfig llmConfig) {
        this.llmConfig = llmConfig;
        this.providerMap = providers.stream()
            .collect(Collectors.toMap(
                p -> p.getProviderName().toLowerCase(),
                Function.identity(),
                (existing, replacement) -> existing
            ));

        log.info("Initialized LLM Provider Registry with available providers: {}", providerMap.keySet());
    }

    @Override
    public String generateRawStudySet(String prompt, String requestId) {
        LlmProvider activeProvider = resolveActiveProvider();
        return activeProvider.generateRawStudySet(prompt, requestId);
    }

    @Override
    public String getProviderName() {
        return resolveActiveProvider().getProviderName();
    }

    /**
     * Resolves the configured provider or provides a clear error message.
     */
    private LlmProvider resolveActiveProvider() {
        String configured = llmConfig.getProvider();
        if (configured == null || configured.isBlank()) {
            configured = LlmConstants.DEFAULT_PROVIDER;
        }

        String key = configured.trim().toLowerCase();
        LlmProvider provider = providerMap.get(key);

        if (provider == null) {
            throw new LlmProviderException(
                String.format("Unsupported LLM provider '%s'. Available registered providers: %s", configured, providerMap.keySet())
            );
        }

        return provider;
    }
}
