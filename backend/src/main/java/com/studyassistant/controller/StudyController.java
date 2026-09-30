package com.studyassistant.controller;

import com.studyassistant.constant.ApiConstants;
import com.studyassistant.dto.StudyRequest;
import com.studyassistant.dto.StudyResponse;
import com.studyassistant.service.StudyService;
import com.studyassistant.service.llm.LlmService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST endpoint exposing study session generation and health checks.
 */
@RestController
@RequestMapping(ApiConstants.API_BASE)
public class StudyController {

    private static final Logger log = LoggerFactory.getLogger(StudyController.class);

    private final StudyService studyService;
    private final LlmService llmService;

    public StudyController(StudyService studyService, LlmService llmService) {
        this.studyService = studyService;
        this.llmService = llmService;
    }

    /**
     * Generates a validated study set from topic and notes.
     */
    @PostMapping(ApiConstants.STUDY_ENDPOINT)
    public ResponseEntity<StudyResponse> generateStudySet(@Valid @RequestBody StudyRequest request) {
        log.info("Received study generation request: promptLength={}, questionCount={}", request.prompt().length(), request.questionCount());
        StudyResponse response = studyService.generateStudySet(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check and configuration status endpoint.
     */
    @GetMapping(ApiConstants.HEALTH_ENDPOINT)
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            ApiConstants.STATUS_KEY, ApiConstants.STATUS_UP,
            ApiConstants.SERVICE_KEY, ApiConstants.SERVICE_NAME,
            ApiConstants.LLM_PROVIDER_KEY, llmService.getProviderName()
        ));
    }
}
