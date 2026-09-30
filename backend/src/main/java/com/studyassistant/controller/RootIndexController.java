package com.studyassistant.controller;

import com.studyassistant.constant.ApiConstants;
import com.studyassistant.service.llm.LlmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root index endpoint providing service discovery and status.
 */
@RestController
public class RootIndexController {

    private final LlmService llmService;

    public RootIndexController(LlmService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootIndex() {
        return ResponseEntity.ok(Map.of(
            "service", ApiConstants.SERVICE_NAME,
            "status", ApiConstants.STATUS_UP,
            "llmProvider", llmService.getProviderName(),
            "endpoints", Map.of(
                "health", ApiConstants.API_BASE + ApiConstants.HEALTH_ENDPOINT,
                "study", ApiConstants.API_BASE + ApiConstants.STUDY_ENDPOINT
            )
        ));
    }
}
