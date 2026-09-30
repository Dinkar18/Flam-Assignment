package com.studyassistant.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.studyassistant.constant.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Immutable DTO representing a user's free-form study generation request.
 */
public record StudyRequest(
    @NotBlank(message = "Please enter a topic, notes, or study prompt")
    @Size(max = ValidationConstants.MAX_NOTES_LENGTH, message = "Input must not exceed 5000 characters")
    @JsonProperty("prompt")
    String prompt,

    @NotNull(message = "Question count is required")
    @Min(value = ValidationConstants.MIN_QUESTION_COUNT, message = "Question count must be at least 3")
    @Max(value = ValidationConstants.MAX_QUESTION_COUNT, message = "Question count must not exceed 10")
    @JsonProperty("questionCount")
    Integer questionCount
) {
    @JsonCreator
    public StudyRequest(
        @JsonProperty("prompt") String prompt,
        @JsonProperty("questionCount") Integer questionCount
    ) {
        this.prompt = prompt != null ? prompt.trim() : "";
        this.questionCount = questionCount != null ? questionCount : ValidationConstants.DEFAULT_QUESTION_COUNT;
    }

    /**
     * Canonical content accessor for prompt construction.
     */
    public String getEffectiveContent() {
        return prompt;
    }
}
