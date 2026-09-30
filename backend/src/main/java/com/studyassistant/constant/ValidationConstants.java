package com.studyassistant.constant;

import java.util.Set;

/**
 * Domain validation constraints and bounds used across DTOs and Validators.
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Prevent instantiation
    }

    // Question count limits
    public static final int MIN_QUESTION_COUNT = 3;
    public static final int MAX_QUESTION_COUNT = 10;
    public static final int DEFAULT_QUESTION_COUNT = 5;

    // Input text limits
    public static final int MAX_TOPIC_LENGTH = 150;
    public static final int MAX_NOTES_LENGTH = 5000;
    public static final int MAX_PROMPT_LENGTH = 5000;

    // LLM generated text validation limits
    public static final int MAX_QUESTION_LENGTH = 500;
    public static final int MAX_ANSWER_LENGTH = 2500;

    // Allowed question difficulties
    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_MEDIUM = "medium";
    public static final String DIFFICULTY_HARD = "hard";
    public static final Set<String> ALLOWED_DIFFICULTIES = Set.of(
        DIFFICULTY_EASY,
        DIFFICULTY_MEDIUM,
        DIFFICULTY_HARD
    );
}
