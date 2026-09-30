package com.studyassistant.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.constant.ErrorCodes;
import com.studyassistant.constant.LlmConstants;
import com.studyassistant.constant.ValidationConstants;
import com.studyassistant.dto.Question;
import com.studyassistant.dto.StudyResponse;
import com.studyassistant.exception.InvalidLlmResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic multi-stage validator for untrusted LLM outputs.
 * Pipeline: Clean JSON -> Syntax Parse -> Structural Checks -> Business Rule Verification -> Trusted DTO.
 */
@Component
public class StudyResponseValidator {

    private static final Logger log = LoggerFactory.getLogger(StudyResponseValidator.class);
    private static final Set<String> ALLOWED_DIFFICULTIES = ValidationConstants.ALLOWED_DIFFICULTIES;
    private static final int MAX_QUESTION_LENGTH = ValidationConstants.MAX_QUESTION_LENGTH;
    private static final int MAX_ANSWER_LENGTH = ValidationConstants.MAX_ANSWER_LENGTH;

    private final ObjectMapper objectMapper;

    public StudyResponseValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses and validates raw LLM output against the expected schema and business constraints.
     *
     * @param rawResponse raw string returned by LLM
     * @param expectedCount number of questions requested by user
     * @param defaultTitle fallback title if title is missing
     * @return trusted, validated StudyResponse
     * @throws InvalidLlmResponseException if validation fails
     */
    public StudyResponse validate(String rawResponse, int expectedCount, String defaultTitle) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new InvalidLlmResponseException(ErrorCodes.EMPTY_RESPONSE, "LLM response is null or empty");
        }

        String cleanedJson = sanitizeJsonText(rawResponse);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(cleanedJson);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse LLM response as JSON: {}", e.getMessage());
            throw new InvalidLlmResponseException(ErrorCodes.MALFORMED_JSON, "LLM response is not valid JSON: " + e.getOriginalMessage(), e);
        }

        if (!rootNode.isObject()) {
            throw new InvalidLlmResponseException(ErrorCodes.INVALID_ROOT_TYPE, "Expected root JSON object but got " + rootNode.getNodeType());
        }

        // Validate title
        String title = defaultTitle;
        if (rootNode.hasNonNull("title") && rootNode.get("title").isTextual()) {
            String candidateTitle = rootNode.get("title").asText().trim();
            if (!candidateTitle.isEmpty()) {
                title = candidateTitle;
            }
        }

        // Validate questions array
        JsonNode questionsNode = rootNode.get("questions");
        if (questionsNode == null || !questionsNode.isArray()) {
            throw new InvalidLlmResponseException(ErrorCodes.MISSING_QUESTIONS_ARRAY, "Root object must contain a 'questions' array");
        }

        if (questionsNode.size() != expectedCount) {
            throw new InvalidLlmResponseException(
                ErrorCodes.QUESTION_COUNT_MISMATCH,
                String.format("Expected exactly %d questions but received %d", expectedCount, questionsNode.size())
            );
        }

        List<Question> validatedQuestions = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        Set<String> seenQuestionTexts = new HashSet<>();

        for (int i = 0; i < questionsNode.size(); i++) {
            JsonNode qNode = questionsNode.get(i);
            if (!qNode.isObject()) {
                throw new InvalidLlmResponseException(ErrorCodes.INVALID_QUESTION_TYPE, "Question at index " + i + " must be an object");
            }

            // ID Validation
            if (!qNode.hasNonNull("id") || !qNode.get("id").isTextual() || qNode.get("id").asText().isBlank()) {
                throw new InvalidLlmResponseException(ErrorCodes.MISSING_QUESTION_ID, "Question at index " + i + " has a missing or blank 'id'");
            }
            String id = qNode.get("id").asText().trim();
            if (!seenIds.add(id)) {
                throw new InvalidLlmResponseException(ErrorCodes.DUPLICATE_QUESTION_ID, "Duplicate question ID encountered: " + id);
            }

            // Question Text Validation
            if (!qNode.hasNonNull("question") || !qNode.get("question").isTextual() || qNode.get("question").asText().isBlank()) {
                throw new InvalidLlmResponseException(ErrorCodes.MISSING_QUESTION_TEXT, "Question with ID '" + id + "' has missing or blank 'question' text");
            }
            String questionText = qNode.get("question").asText().trim();
            if (questionText.length() > MAX_QUESTION_LENGTH) {
                throw new InvalidLlmResponseException(ErrorCodes.QUESTION_TOO_LONG, "Question text for ID '" + id + "' exceeds maximum length of " + MAX_QUESTION_LENGTH);
            }
            if (!seenQuestionTexts.add(questionText.toLowerCase(Locale.ROOT))) {
                throw new InvalidLlmResponseException(ErrorCodes.DUPLICATE_QUESTION_TEXT, "Duplicate question text detected for ID: " + id);
            }

            // Answer Validation
            if (!qNode.hasNonNull("answer") || !qNode.get("answer").isTextual() || qNode.get("answer").asText().isBlank()) {
                throw new InvalidLlmResponseException(ErrorCodes.MISSING_ANSWER_TEXT, "Question with ID '" + id + "' has missing or blank 'answer' text");
            }
            String answerText = qNode.get("answer").asText().trim();
            if (answerText.length() > MAX_ANSWER_LENGTH) {
                throw new InvalidLlmResponseException(ErrorCodes.ANSWER_TOO_LONG, "Answer text for ID '" + id + "' exceeds maximum length of " + MAX_ANSWER_LENGTH);
            }

            // Difficulty Validation
            if (!qNode.hasNonNull("difficulty") || !qNode.get("difficulty").isTextual() || qNode.get("difficulty").asText().isBlank()) {
                throw new InvalidLlmResponseException(ErrorCodes.MISSING_DIFFICULTY, "Question with ID '" + id + "' is missing 'difficulty'");
            }
            String difficulty = qNode.get("difficulty").asText().trim().toLowerCase(Locale.ROOT);
            if (!ALLOWED_DIFFICULTIES.contains(difficulty)) {
                throw new InvalidLlmResponseException(ErrorCodes.INVALID_DIFFICULTY, "Invalid difficulty '" + difficulty + "' for question ID '" + id + "'. Allowed: " + ALLOWED_DIFFICULTIES);
            }

            // Explanation Validation (Rich context for flashcards and quiz review)
            String explanation = null;
            if (qNode.hasNonNull("explanation") && qNode.get("explanation").isTextual() && !qNode.get("explanation").asText().isBlank()) {
                explanation = qNode.get("explanation").asText().trim();
            }
            if (explanation == null || explanation.isBlank()) {
                explanation = answerText;
            }

            // Options Validation (For Quiz Mode)
            if (!qNode.has("options") || !qNode.get("options").isArray()) {
                throw new InvalidLlmResponseException(ErrorCodes.MISSING_OPTIONS, "Question with ID '" + id + "' is missing 'options' array");
            }
            
            List<String> options = new ArrayList<>();
            Set<String> uniqueOptions = new HashSet<>();
            for (JsonNode optNode : qNode.get("options")) {
                if (optNode.isTextual() && !optNode.asText().isBlank()) {
                    String optText = optNode.asText().trim();
                    if (uniqueOptions.add(optText.toLowerCase(Locale.ROOT))) {
                        options.add(optText);
                    }
                }
            }

            if (options.size() != 4) {
                throw new InvalidLlmResponseException(ErrorCodes.INVALID_OPTIONS, "Question with ID '" + id + "' must have exactly 4 unique valid options, found: " + options.size());
            }

            // Ensure options contain the correct answer
            boolean containsAnswer = false;
            for (String opt : options) {
                if (opt.equalsIgnoreCase(answerText)) {
                    containsAnswer = true;
                    break;
                }
            }
            
            if (!containsAnswer) {
                throw new InvalidLlmResponseException(ErrorCodes.ANSWER_NOT_IN_OPTIONS, "Question with ID '" + id + "' has options that do not include the correct answer");
            }

            validatedQuestions.add(new Question(id, questionText, answerText, explanation, difficulty, options));
        }

        return new StudyResponse(title, LlmConstants.PROMPT_VERSION, validatedQuestions);
    }

    /**
     * Strips Markdown formatting or surrounding conversational text if present.
     */
    private String sanitizeJsonText(String raw) {
        String trimmed = raw.trim();

        // Strip ```json ... ``` or ``` ... ```
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            trimmed = trimmed.trim();
        }

        // Find the outermost JSON object bounds '{' and '}'
        int startIdx = trimmed.indexOf('{');
        int endIdx = trimmed.lastIndexOf('}');
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            trimmed = trimmed.substring(startIdx, endIdx + 1);
        }

        return trimmed;
    }
}
