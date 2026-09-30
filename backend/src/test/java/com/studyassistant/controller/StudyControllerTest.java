package com.studyassistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.constant.ApiConstants;
import com.studyassistant.constant.ErrorCodes;
import com.studyassistant.constant.LlmConstants;
import com.studyassistant.dto.Question;
import com.studyassistant.dto.StudyRequest;
import com.studyassistant.dto.StudyResponse;
import com.studyassistant.exception.LlmGenerationException;
import com.studyassistant.service.StudyService;
import com.studyassistant.service.llm.LlmService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudyController.class)
class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyService studyService;

    @MockBean
    private LlmService llmService;

    @MockBean
    private com.studyassistant.service.ratelimit.RateLimiterService rateLimiterService;

    @Test
    @DisplayName("POST /api/study should return 200 with validated study set on valid request")
    void shouldReturn200OnValidRequest() throws Exception {
        StudyRequest request = new StudyRequest("Java Collections: HashMap stores key-values", 3);
        StudyResponse response = new StudyResponse(
            "Java Collections",
            LlmConstants.PROMPT_VERSION,
            List.of(
                new Question("q1", "What is HashMap?", "Key-value store", "A high-yield map.", "easy", List.of("Key-value store", "List", "Set", "Queue")),
                new Question("q2", "What is HashSet?", "Unique elements store", "A high-yield set.", "medium", List.of("Unique elements store", "Map", "Array", "Stack")),
                new Question("q3", "What is TreeMap?", "Sorted map", "A high-yield tree.", "hard", List.of("Sorted map", "Unordered map", "Graph", "Tree"))
            )
        );

        when(studyService.generateStudySet(any(StudyRequest.class))).thenReturn(response);

        mockMvc.perform(post(ApiConstants.API_BASE + ApiConstants.STUDY_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Java Collections"))
            .andExpect(jsonPath("$.promptVersion").value(LlmConstants.PROMPT_VERSION))
            .andExpect(jsonPath("$.questions.length()").value(3))
            .andExpect(jsonPath("$.questions[0].id").value("q1"));
    }

    @Test
    @DisplayName("POST /api/study should return 400 when prompt is blank")
    void shouldReturn400OnBlankInput() throws Exception {
        StudyRequest invalidRequest = new StudyRequest("", 5);

        mockMvc.perform(post(ApiConstants.API_BASE + ApiConstants.STUDY_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.INVALID_INPUT))
            .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    @DisplayName("POST /api/study should return 400 when questionCount is out of bounds (<3 or >10)")
    void shouldReturn400OnInvalidQuestionCount() throws Exception {
        StudyRequest invalidCountRequest = new StudyRequest("Valid Prompt Content", 15);

        mockMvc.perform(post(ApiConstants.API_BASE + ApiConstants.STUDY_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCountRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.INVALID_INPUT));
    }

    @Test
    @DisplayName("POST /api/study should return 502 Bad Gateway when LLM generation fails")
    void shouldReturn502OnGenerationFailure() throws Exception {
        StudyRequest request = new StudyRequest("Java programming fundamentals", 3);

        when(studyService.generateStudySet(any(StudyRequest.class)))
            .thenThrow(new LlmGenerationException("We couldn't generate the study set. Please try again."));

        mockMvc.perform(post(ApiConstants.API_BASE + ApiConstants.STUDY_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.GENERATION_FAILED))
            .andExpect(jsonPath("$.error.message").value("We couldn't generate the study set. Please try again."));
    }

    @Test
    @DisplayName("GET /api/health should return 200 with service status")
    void shouldReturnHealthStatus() throws Exception {
        when(llmService.getProviderName()).thenReturn(LlmConstants.PROVIDER_GEMINI);

        mockMvc.perform(get(ApiConstants.API_BASE + ApiConstants.HEALTH_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(ApiConstants.STATUS_UP))
            .andExpect(jsonPath("$.service").value(ApiConstants.SERVICE_NAME))
            .andExpect(jsonPath("$.llmProvider").value(LlmConstants.PROVIDER_GEMINI));
    }
}
