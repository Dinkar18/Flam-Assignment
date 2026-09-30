/**
 * Centralized Frontend Constants.
 * Prevents magic strings, typos, and maintains consistency with backend contracts.
 */

// LocalStorage Keys
export const STORAGE_KEYS = {
  ACTIVE_SET: 'study_assistant_active_set',
  LAST_PARAMS: 'study_assistant_last_params',
  QUIZ_RESULTS: 'study_assistant_quiz_results',
};

// Application Views
export const VIEWS = {
  FORM: 'form',
  LOADING: 'loading',
  STUDYING: 'studying',
  RESULTS: 'results',
  ERROR: 'error',
};

// Study Session Modes
export const STUDY_MODES = {
  FLASHCARDS: 'flashcards',
  QUIZ: 'quiz',
};

// API Endpoints
const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '');

export const API_ROUTES = {
  STUDY: `${API_BASE}/api/study`,
  HEALTH: `${API_BASE}/api/health`,
};

// Question Constraints
export const QUESTION_LIMITS = {
  MIN_COUNT: 3,
  MAX_COUNT: 10,
  DEFAULT_COUNT: 5,
  MAX_INPUT_CHARS: 5000,
};

// Difficulty Badges & Color Tokens
export const DIFFICULTY_CONFIG = {
  easy: {
    label: 'Easy',
    dot: 'bg-emerald-400',
    border: 'border-emerald-500/20 text-emerald-300 bg-emerald-500/10',
  },
  medium: {
    label: 'Medium',
    dot: 'bg-amber-400',
    border: 'border-amber-500/20 text-amber-300 bg-amber-500/10',
  },
  hard: {
    label: 'Hard',
    dot: 'bg-indigo-400',
    border: 'border-indigo-500/20 text-indigo-300 bg-indigo-500/10',
  },
};
