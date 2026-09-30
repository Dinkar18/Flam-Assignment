package com.studyassistant.dto;

import java.util.List;

/**
 * Immutable DTO representing a single validated study question.
 */
public record Question(
    String id,
    String question,
    String answer,
    String explanation,
    String difficulty,
    List<String> options
) {}


