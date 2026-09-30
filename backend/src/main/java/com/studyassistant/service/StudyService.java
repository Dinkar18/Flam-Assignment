package com.studyassistant.service;

import com.studyassistant.config.LlmConfig;
import com.studyassistant.dto.StudyRequest;
import com.studyassistant.dto.StudyResponse;
import com.studyassistant.exception.InvalidLlmResponseException;
import com.studyassistant.exception.LlmException;
import com.studyassistant.exception.LlmGenerationException;
import com.studyassistant.prompt.StudyPromptBuilder;
import com.studyassistant.service.llm.LlmService;
import com.studyassistant.validation.StudyResponseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Orchestrates LLM study generation, prompt construction, bounded retries, and strict validation.
 */
@Service
public class StudyService {

    private static final Logger log = LoggerFactory.getLogger(StudyService.class);

    private final StudyPromptBuilder promptBuilder;
    private final LlmService llmService;
    private final StudyResponseValidator validator;
    private final LlmConfig llmConfig;

    public StudyService(StudyPromptBuilder promptBuilder,
                        LlmService llmService,
                        StudyResponseValidator validator,
                        LlmConfig llmConfig) {
        this.promptBuilder = promptBuilder;
        this.llmService = llmService;
        this.validator = validator;
        this.llmConfig = llmConfig;
    }

    /**
     * Generates a trusted study set with automatic bounded retry.
     *
     * @param request user input
     * @return fully validated StudyResponse
     * @throws LlmGenerationException if generation fails after max attempts
     */
    public StudyResponse generateStudySet(StudyRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String prompt = promptBuilder.buildPrompt(request);
        int maxAttempts = Math.max(1, llmConfig.getMaxAttempts());

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long startTime = System.currentTimeMillis();
            try {
                log.info("Starting study generation [requestId={}, promptVersion={}, attempt={}/{}, provider={}]",
                    requestId, StudyPromptBuilder.PROMPT_VERSION, attempt, maxAttempts, llmService.getProviderName());

                String rawLlmResponse = llmService.generateRawStudySet(prompt, requestId);
                StudyResponse validatedResponse = validator.validate(rawLlmResponse, request.questionCount(), "Study Session");

                long latency = System.currentTimeMillis() - startTime;
                log.info("Study generation successful [requestId={}, promptVersion={}, attempt={}, latency={}ms, validation=PASSED, questionsCount={}]",
                    requestId, StudyPromptBuilder.PROMPT_VERSION, attempt, latency, validatedResponse.questions().size());

                return validatedResponse;
            } catch (InvalidLlmResponseException e) {
                long latency = System.currentTimeMillis() - startTime;
                lastException = e;
                log.warn("Study response validation failed [requestId={}, promptVersion={}, attempt={}, latency={}ms, validation=FAILED, reason={}]",
                    requestId, StudyPromptBuilder.PROMPT_VERSION, attempt, latency, e.getErrorCode());
            } catch (LlmException e) {
                long latency = System.currentTimeMillis() - startTime;
                lastException = e;
                log.warn("LLM provider call failed [requestId={}, promptVersion={}, attempt={}, latency={}ms, validation=FAILED, reason={}]",
                    requestId, StudyPromptBuilder.PROMPT_VERSION, attempt, latency, e.getErrorCode());
            } catch (Exception e) {
                long latency = System.currentTimeMillis() - startTime;
                lastException = e;
                log.error("Unexpected error during study generation [requestId={}, attempt={}, latency={}ms]: {}",
                    requestId, attempt, latency, e.getMessage(), e);
            }

            if (attempt < maxAttempts) {
                log.info("Initiating retry attempt {}/{} [requestId={}]", attempt + 1, maxAttempts, requestId);
            }
        }

        log.error("Study generation exhausted all attempts [requestId={}, totalAttempts={}]", requestId, maxAttempts);
        String causeMessage = (lastException != null && lastException.getMessage() != null)
            ? lastException.getMessage()
            : "Please try again or adjust your study notes.";

        throw new LlmGenerationException(
            "Failed to generate study set: " + causeMessage,
            lastException
        );
    }
}
