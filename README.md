# AI Study Assistant

A full-stack AI Study Assistant built for the Flam Software Engineering Internship assignment that transforms unstructured study notes, lecture excerpts, and custom topics into interactive learning experiences (Active Recall Flashcards and Diagnostic Assessment Quizzes).

Built with **React 18 (Feature-Driven Architecture) + Tailwind CSS** on the frontend and **Java 17 / Spring Boot 3** on the backend.

---

## 🎯 Assignment & Problem Statement

This project fulfills the **Study Assistant** assignment specifications:
- **Unified Free-form Text Input**: Takes arbitrary study notes, lecture excerpts, topic prompts, or questions (e.g. *"Explain Java HashMap collision handling"*, *"Photosynthesis overview"*, *"How to cook Maggi"*).
- **Not a Chatbot**: Converts raw LLM output into structured, validated JSON data rendered as interactive components (Flashcards with progressive sleeve reveal, multi-choice diagnostic quizzes with instant feedback, and score reviews).
- **Backend API Proxy**: API keys (Groq, Gemini) are strictly stored on the backend and never exposed to the client.
- **Resilient AI Failure Handling**: Comprehensive multi-stage validation, bounded retry, and stale request cancellation.
- **Apple Dark Minimalist UX**: SF Pro typography, squircle containers, translucent glass navigation, and keyboard shortcuts (`Space`, `Arrows`, `M`).

---

## 🏗 System Architecture

```mermaid
graph TB
    subgraph Client["Frontend Client (React 18 + Vite)"]
        UI["Feature-Driven UI<br/>(StudyForm / StudyCard / Quiz / Results)"]
        Storage["useLocalStorage<br/>(State Persistence)"]
        API_CLIENT["api.js<br/>(AbortController + Stale-Request Guard)"]
    end

    subgraph Security["Edge Security & Filters"]
        CORS["WebCorsConfig<br/>(CORS_ALLOWED_ORIGINS)"]
        RL["RateLimitingFilter<br/>(Bucket4j 10 req/min)"]
    end

    subgraph Backend["Spring Boot 3 Backend Proxy"]
        Controller["StudyController<br/>(POST /api/study)"]
        ValidatorDTO["Jakarta Validation<br/>(@Valid StudyRequest)"]
        Service["StudyService<br/>(Bounded Retry Orchestrator)"]
        PromptEngine["StudyPromptBuilder<br/>(Versioned Few-Shot Prompt)"]
        Router["LlmProviderRouter<br/>(Strategy Pattern)"]
        Validator["StudyResponseValidator<br/>(AST Multi-Stage Parser)"]
    end

    subgraph External["External AI Providers"]
        Groq["Groq API<br/>(llama-3.3-70b-versatile)"]
        Gemini["Google Gemini API<br/>(gemini-2.5-flash)"]
    end

    UI --> API_CLIENT
    Storage <--> UI
    API_CLIENT -- "HTTP POST (JSON)" --> CORS
    CORS --> RL
    RL --> Controller
    Controller --> ValidatorDTO
    ValidatorDTO --> Service
    Service --> PromptEngine
    Service --> Router
    Router --> Groq
    Router --> Gemini
    Groq -- "Raw JSON String" --> Service
    Gemini -- "Raw JSON String" --> Service
    Service --> Validator
    Validator -- "Trusted StudyResponse DTO" --> Controller
    Controller -- "HTTP 200 JSON" --> API_CLIENT
    API_CLIENT --> UI
```

---

## 🔄 User Request & End-to-End Processing Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as Student / User
    participant Form as StudyForm (React)
    participant API as api.js (Client Service)
    participant Filter as RateLimitingFilter (Bucket4j)
    participant Controller as StudyController
    participant Service as StudyService
    participant LLM as LlmService (Groq / Gemini)
    participant Validator as StudyResponseValidator
    participant Session as StudySession UI

    User->>Form: Enters prompt / notes & selects question count
    Form->>API: generateStudySet({ prompt, questionCount })
    Note over API: Aborts previous in-flight request<br/>assigns unique Request ID

    API->>Filter: POST /api/study
    alt Rate Limit Exceeded (>10 req/min)
        Filter-->>API: HTTP 429 Too Many Requests
        API-->>Form: Display Rate Limit Error
    else Token Available
        Filter->>Controller: Forward Request
    end

    Controller->>Service: generateStudySet(StudyRequest)
    
    loop Bounded Retry Loop (Attempt 1 to 2)
        Service->>LLM: generateRawStudySet(prompt, requestId)
        LLM-->>Service: Raw JSON string response
        Service->>Validator: validate(rawResponse, count, fallbackTitle)
        
        alt Validation Passed (Valid AST + Schema Rules)
            Validator-->>Service: Validated StudyResponse DTO
        else Validation Failed (Malformed JSON / Schema Mismatch)
            Validator-->>Service: InvalidLlmResponseException
            Note over Service: Logs diagnostic telemetry<br/>Retries attempt 2/2
        end
    end

    alt Success
        Service-->>Controller: Return Trusted DTO
        Controller-->>API: HTTP 200 OK (JSON)
        API-->>Session: Initialize Flashcards & Quiz Modes
        Session-->>User: Render Interactive Recall Session
    else All Attempts Exhausted
        Service-->>Controller: throw LlmGenerationException
        Controller-->>API: HTTP 502 Bad Gateway (Structured Error)
        API-->>Form: Render Error State with Itemized Diagnostics
    end
```

---

## 🛡 Handling Bad AI Output & Resilience Pipeline

Handling model unreliability is central to this application:

```mermaid
flowchart TD
    Raw["Raw LLM Output String"] --> Sanitize["1. Sanitize Markdown Code Fences (```json)"]
    Sanitize --> AST["2. Jackson AST Parse (readTree)"]
    
    AST -->|Syntax Error| Retry{"Attempt < MaxAttempts?"}
    
    AST -->|Valid AST| Structural["3. Structural Validation<br/>- Root is Object<br/>- 'questions' is Array<br/>- questions.size == expectedCount"]
    
    Structural -->|Structure Invalid| Retry
    
    Structural -->|Structure OK| Integrity["4. Field & Business Integrity<br/>- Non-blank ID & Question Text<br/>- ID uniqueness check<br/>- Allowed Difficulty ('easy' | 'medium' | 'hard')<br/>- Explanation fallback<br/>- Distractor uniqueness & Answer in options"]
    
    Integrity -->|Violations Found| Retry
    
    Integrity -->|All Rules Passed| DTO["5. Construct Trusted StudyResponse DTO"]
    
    Retry -->|Yes| NextAttempt["Log Telemetry & Trigger Immediate Retry"]
    Retry -->|No| Fail["Throw LlmGenerationException (HTTP 502)"]
```

### Resilience Guarantees:
1. **Multi-Stage Deterministic Validation (`StudyResponseValidator.java`)**:
   - **JSON Syntactic Check**: Parses JSON AST, handles markdown code-fence encapsulation (` ```json `), and extracts the JSON object from surrounding text. Invalid JSON is rejected and retried rather than silently accepted.
   - **Schema & Field Integrity**: Verifies question array bounds ($3 \le count \le 10$), required fields (`id`, `question`, `answer`, `difficulty`, `options`).
   - **Duplicate Detection**: Enforces uniqueness across questions.
   - **Options Consistency**: Ensures multiple-choice options contain the correct answer and unique distractors.
2. **Bounded Automatic Retry (`StudyService.java`)**:
   - If the LLM generates invalid JSON or fails schema validation, the backend logs diagnostic telemetry and performs a bounded retry before failing gracefully.
3. **Stale Response Protection (`api.js`)**:
   - Uses `AbortController` and an incremental request sequence tracker. If a user triggers a new generation or cancels while a previous LLM request is in flight, the older response is aborted and prevented from overwriting newer state.
4. **Token Bucket Rate Limiting (`RateLimitingFilter.java`)**:
   - In-memory Bucket4j IP rate limiter enforcing 10 generation requests per minute per client IP. Emits standard `X-Rate-Limit-Remaining` and `Retry-After` headers.

---

## ⚠️ Known Limitations

- Generated study content can contain factual inaccuracies because it depends on the selected LLM.
- The backend validates JSON structure and application-level rules but does not guarantee factual correctness.
- Study sessions are stored locally in the browser and are not synchronized across devices.
- The current application uses a fixed question schema rather than arbitrary AI-generated content blocks.
- Rate limiting is in-memory and intended as lightweight protection rather than distributed production infrastructure.

---

## 📱 Frontend Feature-Driven Navigation & State

```mermaid
stateDiagram-v2
    [*] --> Form: User Lands on App

    Form --> Loading: Submit Study Notes / Topic
    Loading --> Form: Cancel Request (AbortController)
    Loading --> Error: Generation Failed / Network Disconnected
    Loading --> Studying: Study Set Validated (HTTP 200)

    Error --> Loading: Retry Same Request
    Error --> Form: Edit Study Notes

    state Studying {
        [*] --> Flashcards
        Flashcards --> Quiz: Switch Tab
        Quiz --> Flashcards: Switch Tab
        Flashcards --> Flashcards: Next / Prev / Spacebar / Mark Mastered (M)
        Quiz --> Quiz: Select Option / Submit Answer / Immediate Feedback
    }

    Studying --> Results: Complete Diagnostic Quiz
    Studying --> Form: Navbar / New Session (Confirmation Modal)

    Results --> Studying: Retake Quiz / Review Flashcards
    Results --> Form: + New Session
```

---

## 📂 Project Structure

```
Flam/
├── backend/                              # Spring Boot 3 Backend
│   ├── src/main/java/com/studyassistant/
│   │   ├── config/                       # CORS & Rate Limit configurations
│   │   ├── constant/                     # Centralized constants (zero magic strings)
│   │   ├── controller/                   # REST API controllers (/api/study, /api/health)
│   │   ├── dto/                          # Immutable Records (StudyRequest, StudyResponse, Question)
│   │   ├── exception/                    # Custom exceptions & GlobalExceptionHandler
│   │   ├── filter/                       # Bucket4j Rate Limiting Filter
│   │   ├── prompt/                       # Versioned Few-shot prompt builder
│   │   ├── service/                      # StudyService orchestrator
│   │   │   ├── llm/                      # Strategy Pattern (Gemini, Groq, Router)
│   │   │   └── ratelimit/                # Token bucket service
│   │   └── validation/                   # Multi-stage deterministic JSON validator
│   └── src/test/java/                    # Unit & Integration Tests (100% Pass)
│
├── frontend/                             # React 18 + Vite Frontend
│   └── src/
│       ├── components/common/            # Navbar, ConfirmationDialog, LoadingState, ErrorState, ProgressIndicator
│       ├── constants/                    # Centralized routes, limits, storage keys
│       ├── features/                     # Feature-Driven Architecture
│       │   ├── prompt-entry/             # StudyForm & Presets
│       │   ├── flashcards/               # Active Recall Cards & Keyboard Shortcuts
│       │   ├── quiz/                     # Diagnostic assessment & feedback
│       │   ├── study-session/            # Session orchestration & Tab switcher
│       │   └── results/                  # Radial Mastery Gauge & Diagnostic breakdown
│       ├── hooks/                        # useLocalStorage, useKeyboardShortcuts
│       └── services/                     # api.js (Fetch, AbortController, Stale Request Guard)
```

---

## 🚀 Quick Start & Running Locally

### Prerequisites
- **Java 17+** (`java -version`)
- **Node.js 18+** & **npm**

### 1. Configure & Run Backend (Port 8081)

Create a `.env` file in the project root (or copy `.env.example`):
```env
LLM_PROVIDER=gemini
LLM_API_KEY=your_api_key_here
```

Run the backend server:
```bash
cd backend
./gradlew bootRun
```

### 2. Run Frontend (Port 5173)

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.

---

## 🧪 Running Automated Tests

```bash
cd backend
./gradlew test
```

---

## 🤖 AI Usage Note

In accordance with the assignment guidelines:
- **Tools Used**: Antigravity / Gemini for rapid architecture scaffolding, Spring Boot configuration, and UI iteration.
- **Engineering Decisions**:
  - Structured prompt design with few-shot JSON formatting constraints (`study-v1`).
  - Strategy + Template Method pattern for LLM clients (`AbstractHttpLlmClient.java`).
  - Separation of UI state between Flashcard and Quiz modes with synchronized index and persistent quiz results.
  - Bucket4j Token Bucket rate limiter to protect backend against scrapers and credit exhaustion.

---

## ⏱ Time Spent

- **Architecture & Backend Pipeline**: ~2.5 hours (Spring Boot 3, LLM providers, multi-stage validator, tests).
- **Frontend Core & State Synchronization**: ~2.5 hours (React components, state synchronization, stale request protection).
- **Design System, Dark Theme & Polish**: ~2.0 hours (Tailwind CSS, animations, responsive design, keyboard navigation).
- **Testing & Documentation**: ~0.5 hours.
- **Total Time**: ~7.5 hours (within the 8-hour target).
