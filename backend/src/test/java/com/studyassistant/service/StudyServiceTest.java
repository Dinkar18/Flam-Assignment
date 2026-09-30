package com.studyassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.config.LlmConfig;
import com.studyassistant.constant.ErrorCodes;
import com.studyassistant.constant.LlmConstants;
import com.studyassistant.dto.StudyRequest;
import com.studyassistant.dto.StudyResponse;
import com.studyassistant.exception.LlmGenerationException;
import com.studyassistant.prompt.StudyPromptBuilder;
import com.studyassistant.service.llm.LlmService;
import com.studyassistant.validation.StudyResponseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock
    private LlmService llmService;

    private StudyService studyService;

    @BeforeEach
    void setUp() {
        StudyPromptBuilder promptBuilder = new StudyPromptBuilder();
        StudyResponseValidator validator = new StudyResponseValidator(new ObjectMapper());
        LlmConfig config = new LlmConfig();
        config.setMaxAttempts(2);

        when(llmService.getProviderName()).thenReturn(LlmConstants.PROVIDER_GEMINI);
        studyService = new StudyService(promptBuilder, llmService, validator, config);
    }

    @Test
    @DisplayName("Should succeed on the first attempt when LLM returns valid output")
    void shouldSucceedOnFirstAttempt() {
        String validJson = """
            {
              "title": "React Hooks",
              "questions": [
                { "id": "q1", "question": "What is useState?", "answer": "State hook", "difficulty": "easy", "options": ["State hook", "Effect hook", "Memo hook", "Ref hook"] },
                { "id": "q2", "question": "What is useEffect?", "answer": "Side effect hook", "difficulty": "medium", "options": ["Side effect hook", "State hook", "Memo hook", "Ref hook"] },
                { "id": "q3", "question": "What is useMemo?", "answer": "Memoization hook", "difficulty": "hard", "options": ["Memoization hook", "State hook", "Effect hook", "Ref hook"] }
              ]
            }
            """;

        when(llmService.generateRawStudySet(anyString(), anyString())).thenReturn(validJson);

        StudyRequest request = new StudyRequest("React Hooks: useState manages state, useEffect handles side effects", 3);
        StudyResponse response = studyService.generateStudySet(request);

        assertThat(response).isNotNull();
        assertThat(response.questions()).hasSize(3);
        verify(llmService, times(1)).generateRawStudySet(anyString(), anyString());
    }

    @Test
    @DisplayName("Should retry and succeed on second attempt when first attempt yields invalid JSON")
    void shouldRetryAndSucceedOnSecondAttempt() {
        String brokenJson = "Not valid JSON at all";
        String validJson = """
            {
              "title": "React Hooks",
              "questions": [
                { "id": "q1", "question": "Q1", "answer": "A1", "difficulty": "easy", "options": ["A1", "A2", "A3", "A4"] },
                { "id": "q2", "question": "Q2", "answer": "A2", "difficulty": "medium", "options": ["A1", "A2", "A3", "A4"] },
                { "id": "q3", "question": "Q3", "answer": "A3", "difficulty": "hard", "options": ["A1", "A2", "A3", "A4"] }
              ]
            }
            """;

        when(llmService.generateRawStudySet(anyString(), anyString()))
            .thenReturn(brokenJson)
            .thenReturn(validJson);

        StudyRequest request = new StudyRequest("React Hooks: notes content", 3);
        StudyResponse response = studyService.generateStudySet(request);

        assertThat(response).isNotNull();
        assertThat(response.questions()).hasSize(3);
        verify(llmService, times(2)).generateRawStudySet(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw LlmGenerationException when both attempts fail")
    void shouldFailWhenAllAttemptsExhausted() {
        String brokenJson = "Broken JSON";

        when(llmService.generateRawStudySet(anyString(), anyString())).thenReturn(brokenJson);

        StudyRequest request = new StudyRequest("React Hooks: notes content", 3);

        assertThatThrownBy(() -> studyService.generateStudySet(request))
            .isInstanceOf(LlmGenerationException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GENERATION_FAILED)
            .hasMessageContaining("Failed to generate study set");

        verify(llmService, times(2)).generateRawStudySet(anyString(), anyString());
    }
}
