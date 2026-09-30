package com.studyassistant.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyassistant.constant.ErrorCodes;
import com.studyassistant.dto.Question;
import com.studyassistant.dto.StudyResponse;
import com.studyassistant.exception.InvalidLlmResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyResponseValidatorTest {

    private StudyResponseValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validator = new StudyResponseValidator(objectMapper);
    }

    @Test
    @DisplayName("Should successfully validate a well-formed JSON study set")
    void shouldValidateValidJson() {
        String json = """
            {
              "title": "Java Collections",
              "questions": [
                {
                  "id": "q1",
                  "question": "What is a HashMap?",
                  "answer": "A HashMap is a hash table based implementation of the Map interface.",
                  "difficulty": "easy",
                  "options": [
                    "A HashMap is a hash table based implementation of the Map interface.",
                    "A List implementation",
                    "A Thread safe queue",
                    "A primitive data type"
                  ]
                },
                {
                  "id": "q2",
                  "question": "What is the time complexity of get() in HashMap on average?",
                  "answer": "O(1)",
                  "difficulty": "medium",
                  "options": ["O(1)", "O(n)", "O(log n)", "O(n^2)"]
                },
                {
                  "id": "q3",
                  "question": "How does HashMap resolve collisions in Java 8+?",
                  "answer": "Using linked lists and converting to Red-Black Trees when bucket size exceeds 8.",
                  "difficulty": "hard",
                  "options": [
                    "Using linked lists and converting to Red-Black Trees when bucket size exceeds 8.",
                    "Linear probing",
                    "Double hashing",
                    "Discarding old values"
                  ]
                }
              ]
            }
            """;

        StudyResponse response = validator.validate(json, 3, "Java Collections");

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Java Collections");
        assertThat(response.questions()).hasSize(3);

        Question q1 = response.questions().get(0);
        assertThat(q1.id()).isEqualTo("q1");
        assertThat(q1.question()).isEqualTo("What is a HashMap?");
        assertThat(q1.difficulty()).isEqualTo("easy");
        assertThat(q1.options()).contains(q1.answer());
    }

    @Test
    @DisplayName("Should successfully strip markdown code fences")
    void shouldStripMarkdownFences() {
        String fencedJson = """
            ```json
            {
              "title": "Spring Boot",
              "questions": [
                {
                  "id": "q1",
                  "question": "What does @SpringBootApplication do?",
                  "answer": "Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan.",
                  "difficulty": "easy",
                  "options": [
                    "Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan.",
                    "Configures database",
                    "Starts Tomcat",
                    "Enables security"
                  ]
                },
                {
                  "id": "q2",
                  "question": "What is dependency injection?",
                  "answer": "A design pattern where IoC container injects objects into dependent classes.",
                  "difficulty": "medium",
                  "options": [
                    "A design pattern where IoC container injects objects into dependent classes.",
                    "A way to build jars",
                    "A routing mechanism",
                    "A database ORM"
                  ]
                },
                {
                  "id": "q3",
                  "question": "What is actuator?",
                  "answer": "Provides production-ready features like metrics and health checks.",
                  "difficulty": "medium",
                  "options": [
                    "Provides production-ready features like metrics and health checks.",
                    "A message broker",
                    "A testing framework",
                    "A template engine"
                  ]
                }
              ]
            }
            ```
            """;

        StudyResponse response = validator.validate(fencedJson, 3, "Spring Boot");
        assertThat(response.questions()).hasSize(3);
    }

    @Test
    @DisplayName("Should throw exception on malformed JSON")
    void shouldFailOnMalformedJson() {
        String brokenJson = "{ title: 'missing quotes' ";

        assertThatThrownBy(() -> validator.validate(brokenJson, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.MALFORMED_JSON);
    }

    @Test
    @DisplayName("Should throw exception when question count does not match expected")
    void shouldFailOnQuestionCountMismatch() {
        String json = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "Q1?", "answer": "A1", "difficulty": "easy" },
                { "id": "q2", "question": "Q2?", "answer": "A2", "difficulty": "easy" }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json, 4, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.QUESTION_COUNT_MISMATCH)
            .hasMessageContaining("Expected exactly 4 questions but received 2");
    }

    @Test
    @DisplayName("Should throw exception when difficulty is not allowed enum")
    void shouldFailOnInvalidDifficulty() {
        String json = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "Q1?", "answer": "A1", "difficulty": "extreme" },
                { "id": "q2", "question": "Q2?", "answer": "A2", "difficulty": "easy" },
                { "id": "q3", "question": "Q3?", "answer": "A3", "difficulty": "medium" }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.INVALID_DIFFICULTY);
    }

    @Test
    @DisplayName("Should throw exception on duplicate question ID")
    void shouldFailOnDuplicateQuestionId() {
        String json = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "Q1?", "answer": "A1", "difficulty": "easy", "options": ["A1", "A2", "A3", "A4"] },
                { "id": "q1", "question": "Q2?", "answer": "A2", "difficulty": "easy", "options": ["A1", "A2", "A3", "A4"] },
                { "id": "q3", "question": "Q3?", "answer": "A3", "difficulty": "hard", "options": ["A1", "A2", "A3", "A4"] }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.DUPLICATE_QUESTION_ID);
    }

    @Test
    @DisplayName("Should throw exception on blank question text or blank answer")
    void shouldFailOnBlankFields() {
        String json = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "   ", "answer": "A1", "difficulty": "easy" },
                { "id": "q2", "question": "Q2?", "answer": "A2", "difficulty": "easy" },
                { "id": "q3", "question": "Q3?", "answer": "A3", "difficulty": "hard" }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.MISSING_QUESTION_TEXT);
    }
    @Test
    @DisplayName("Should throw exception when options are missing")
    void shouldFailOnMissingOptions() {
        String json = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "Q1?", "answer": "A1", "difficulty": "easy" },
                { "id": "q2", "question": "Q2?", "answer": "A2", "difficulty": "easy" },
                { "id": "q3", "question": "Q3?", "answer": "A3", "difficulty": "hard" }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.MISSING_OPTIONS);
    }

    @Test
    @DisplayName("Should throw exception when options array does not have exactly 4 items")
    void shouldFailOnInvalidOptionCount() {
        String json3Options = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "Q1?", "answer": "A1", "difficulty": "easy", "options": ["A1", "A2", "A3"] },
                { "id": "q2", "question": "Q2?", "answer": "A2", "difficulty": "easy", "options": ["A2", "B1", "B2", "B3"] },
                { "id": "q3", "question": "Q3?", "answer": "A3", "difficulty": "hard", "options": ["A3", "C1", "C2", "C3"] }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json3Options, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.INVALID_OPTIONS)
            .hasMessageContaining("must have exactly 4 unique valid options");
            
        String json5Options = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "Q1?", "answer": "A1", "difficulty": "easy", "options": ["A1", "A2", "A3", "A4", "A5"] },
                { "id": "q2", "question": "Q2?", "answer": "A2", "difficulty": "easy", "options": ["A2", "B1", "B2", "B3"] },
                { "id": "q3", "question": "Q3?", "answer": "A3", "difficulty": "hard", "options": ["A3", "C1", "C2", "C3"] }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json5Options, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.INVALID_OPTIONS)
            .hasMessageContaining("must have exactly 4 unique valid options");
    }

    @Test
    @DisplayName("Should throw exception when answer is not in options")
    void shouldFailOnAnswerNotInOptions() {
        String json = """
            {
              "title": "Test",
              "questions": [
                { "id": "q1", "question": "Q1?", "answer": "A1", "difficulty": "easy", "options": ["B1", "B2", "B3", "B4"] },
                { "id": "q2", "question": "Q2?", "answer": "A2", "difficulty": "easy", "options": ["A2", "C1", "C2", "C3"] },
                { "id": "q3", "question": "Q3?", "answer": "A3", "difficulty": "hard", "options": ["A3", "D1", "D2", "D3"] }
              ]
            }
            """;

        assertThatThrownBy(() -> validator.validate(json, 3, "Test"))
            .isInstanceOf(InvalidLlmResponseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.ANSWER_NOT_IN_OPTIONS);
    }
}
