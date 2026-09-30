package com.studyassistant.prompt;

import com.studyassistant.constant.LlmConstants;
import com.studyassistant.dto.StudyRequest;
import org.springframework.stereotype.Component;

/**
 * Builds versioned, controlled prompts for LLM study set generation from free-form user input.
 */
@Component
public class StudyPromptBuilder {

    public static final String PROMPT_VERSION = LlmConstants.PROMPT_VERSION;

    /**
     * Constructs the full system + user prompt ensuring strict structured output instructions.
     *
     * @param request the validated user study request
     * @return controlled prompt string
     */
    public String buildPrompt(StudyRequest request) {
        String content = request.getEffectiveContent().trim();
        int count = request.questionCount();

        return """
            You are a pedagogical AI Study Assistant.
            Your task is to analyze the user's study input and generate a structured, interactive study set (flashcards and quiz questions).

            PROMPT_VERSION: %s

            USER STUDY INPUT:
            %s

            REQUESTED QUESTION COUNT:
            %d

            CORE INSTRUCTIONS:
            1. Title: Derive a concise, relevant title for this study session (e.g. "Java HashMap Internals", "JavaScript vs Java Differences", "How to Cook Maggi").
            2. Questions: Generate EXACTLY %d educational questions that test key concepts, definitions, facts, or procedures from the input.
            3. Answer vs Explanation (Crucial Distinction):
               - "answer": A concise, direct phrase (1-5 words) representing the exact correct answer. Must match one of the items in "options".
               - "explanation": A detailed, high-yield educational breakdown (1-3 sentences) explaining WHY the answer is correct, contrasting it with common misconceptions, and providing key takeaways for in-depth flashcard study.
            4. Multiple-Choice Options: Each question must contain exactly 4 distinct options (1 correct answer + 3 plausible distractors).
            5. Difficulty: Assign each question one difficulty: "easy", "medium", or "hard".
            6. Free-form Flexibility: If the input is comprehensive notes, ground directly in the provided material. If the input is a brief topic or prompt, use your knowledge base to generate substantive, accurate questions about that subject.
            7. Return ONLY a valid JSON object matching the exact schema below. Do not include markdown code fences (no ```json), commentary, or extra text.

            SCHEMA:
            {
              "title": "Topic Title",
              "questions": [
                {
                  "id": "q1",
                  "question": "Which language can be executed directly in a web browser without prior compilation?",
                  "answer": "JavaScript",
                  "explanation": "JavaScript engines (e.g., V8 in Chrome, SpiderMonkey in Firefox) are built directly into web browsers to parse and execute scripts at runtime. In contrast, Java requires pre-compilation into bytecode for the Java Virtual Machine (JVM).",
                  "difficulty": "easy",
                  "options": [
                    "JavaScript",
                    "Java",
                    "C++",
                    "Python"
                  ]
                }
              ]
            }
            """.formatted(
                PROMPT_VERSION,
                content,
                count,
                count
            );
    }
}

