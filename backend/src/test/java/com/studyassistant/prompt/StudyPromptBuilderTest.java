package com.studyassistant.prompt;

import com.studyassistant.dto.StudyRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudyPromptBuilderTest {

    private final StudyPromptBuilder promptBuilder = new StudyPromptBuilder();

    @Test
    void shouldBuildControlledPromptFromFreeformInput() {
        StudyRequest request = new StudyRequest("Explain Java HashMap internals with collision handling", 5);

        String prompt = promptBuilder.buildPrompt(request);

        assertNotNull(prompt);
        assertTrue(prompt.contains("PROMPT_VERSION: " + StudyPromptBuilder.PROMPT_VERSION));
        assertTrue(prompt.contains("USER STUDY INPUT:"));
        assertTrue(prompt.contains("Explain Java HashMap internals with collision handling"));
        assertTrue(prompt.contains("REQUESTED QUESTION COUNT:\n5"));
        assertTrue(prompt.contains("explanation"));
    }
}
