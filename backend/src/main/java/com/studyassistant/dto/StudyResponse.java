package com.studyassistant.dto;

import java.util.List;

/**
 * Immutable DTO representing a fully validated and trusted study set response.
 */
public record StudyResponse(
    String title,
    String promptVersion,
    List<Question> questions
) {}
