import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/common/Navbar';
import ConfirmationDialog from './components/common/ConfirmationDialog';
import LoadingState from './components/common/LoadingState';
import ErrorState from './components/common/ErrorState';
import StudyForm from './features/prompt-entry/components/StudyForm';
import StudySession from './features/study-session/components/StudySession';
import Results from './features/results/components/Results';
import { useLocalStorage } from './hooks/useLocalStorage';
import { generateStudySet, cancelActiveRequest } from './services/api';
import { STORAGE_KEYS, VIEWS, STUDY_MODES } from './constants';

export default function App() {
  const [studySet, setStudySet] = useLocalStorage(STORAGE_KEYS.ACTIVE_SET, null);
  const [view, setView] = useState(() => (studySet ? VIEWS.STUDYING : VIEWS.FORM));
  const [lastParams, setLastParams] = useLocalStorage(STORAGE_KEYS.LAST_PARAMS, null);
  const [error, setError] = useState(null);
  const [quizResults, setQuizResults] = useLocalStorage(STORAGE_KEYS.QUIZ_RESULTS, null);
  const [isNavConfirmOpen, setIsNavConfirmOpen] = useState(false);
  const [studySessionKey, setStudySessionKey] = useState(0);
  const [sessionInitialMode, setSessionInitialMode] = useState(STUDY_MODES.FLASHCARDS);
  const [sessionSubsetQuestions, setSessionSubsetQuestions] = useState(null);

  // Sync initial view if studySet was loaded from storage
  useEffect(() => {
    if (studySet && view === VIEWS.FORM) {
      setView(VIEWS.STUDYING);
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleGenerate = useCallback(async (params) => {
    setLastParams(params);
    setError(null);
    setView(VIEWS.LOADING);

    try {
      const data = await generateStudySet(params);
      setStudySet(data);
      setQuizResults(null);
      setSessionInitialMode(STUDY_MODES.FLASHCARDS);
      setSessionSubsetQuestions(null);
      setStudySessionKey(prev => prev + 1);
      setView(VIEWS.STUDYING);
    } catch (err) {
      if (err.isAborted) {
        return;
      }
      setError({
        code: err.code || 'GENERATION_FAILED',
        message: err.message || 'We could not generate the study set. Please try again.',
        details: err.details || [],
      });
      setView(VIEWS.ERROR);
    }
  }, []);

  const handleCancelGeneration = () => {
    cancelActiveRequest();
    setView(studySet ? VIEWS.STUDYING : VIEWS.FORM);
  };

  const handleRetry = () => {
    if (lastParams) {
      handleGenerate(lastParams);
    } else {
      setView(VIEWS.FORM);
    }
  };

  const handleCompleteQuiz = (results) => {
    setQuizResults(results);
    setView(VIEWS.RESULTS);
  };

  const handleRetakeQuiz = () => {
    setQuizResults(null);
    setSessionInitialMode(STUDY_MODES.QUIZ);
    setSessionSubsetQuestions(null);
    setStudySessionKey(prev => prev + 1);
    setView(VIEWS.STUDYING);
  };

  const handleRetestWrongAnswers = () => {
    if (!quizResults || !studySet?.questions) return;
    
    // quizResults is a map of questionId -> result data
    const wrongQuestionIds = Object.entries(quizResults)
      .filter(([id, data]) => !data.isCorrect)
      .map(([id]) => id);
      
    const wrongQuestions = studySet.questions.filter(q => wrongQuestionIds.includes(q.id));
    
    setQuizResults(null);
    setSessionInitialMode(STUDY_MODES.QUIZ);
    setSessionSubsetQuestions(wrongQuestions.length > 0 ? wrongQuestions : null);
    setStudySessionKey(prev => prev + 1);
    setView(VIEWS.STUDYING);
  };

  const handleSwitchToFlashcards = () => {
    setSessionInitialMode(STUDY_MODES.FLASHCARDS);
    setSessionSubsetQuestions(null);
    setView(VIEWS.STUDYING);
  };

  const handleNavbarClick = () => {
    if (view === VIEWS.STUDYING || view === VIEWS.RESULTS) {
      setIsNavConfirmOpen(true);
    } else {
      handleNewTopic();
    }
  };

  const handleNewTopic = () => {
    setStudySet(null);
    setQuizResults(null);
    setError(null);
    setIsNavConfirmOpen(false);
    setView(VIEWS.FORM);
  };

  return (
    <div className="min-h-screen flex flex-col font-sans text-[#f5f5f7] bg-black selection:bg-white selection:text-black relative overflow-hidden">
      {/* Ambient Top Spotlight / Glow Aura */}
      <div className="absolute -top-40 left-1/2 -translate-x-1/2 w-[700px] h-[380px] bg-gradient-to-b from-white/[0.07] via-white/[0.02] to-transparent blur-[120px] pointer-events-none rounded-full" />
      
      {/* Subtle Dot Grid Background */}
      <div className="absolute inset-0 bg-dot-grid opacity-30 pointer-events-none [mask-image:radial-gradient(ellipse_60%_60%_at_50%_40%,#000_60%,transparent_100%)]" />

      <Navbar onReset={handleNavbarClick} />

      <main className="relative z-10 flex-1 max-w-3xl lg:max-w-4xl w-full mx-auto px-4 sm:px-6 md:px-8 py-4 sm:py-6 md:py-8 flex flex-col justify-center">
        {view === VIEWS.FORM && (
          <StudyForm
            onSubmit={handleGenerate}
            isGenerating={false}
          />
        )}

        {view === VIEWS.LOADING && (
          <LoadingState onCancel={handleCancelGeneration} />
        )}

        {view === VIEWS.ERROR && (
          <ErrorState
            error={error}
            onRetry={lastParams ? handleRetry : null}
            onEdit={() => setView(VIEWS.FORM)}
          />
        )}

        {view === VIEWS.STUDYING && studySet && (
          <StudySession
            key={studySessionKey}
            studySet={studySet}
            initialMode={sessionInitialMode}
            subsetQuestions={sessionSubsetQuestions}
            onCompleteQuiz={handleCompleteQuiz}
            onNewTopic={handleNewTopic}
          />
        )}

        {view === VIEWS.RESULTS && (
          <Results
            topic={studySet?.title || 'Study Session'}
            quizResults={quizResults}
            onRetakeQuiz={handleRetakeQuiz}
            onRetestWrong={handleRetestWrongAnswers}
            onSwitchToFlashcards={handleSwitchToFlashcards}
            onNewTopic={handleNewTopic}
          />
        )}
      </main>

      {/* Navbar New Session Confirmation Dialog */}
      <ConfirmationDialog
        isOpen={isNavConfirmOpen}
        title="Start New Session?"
        message="Your active study session and quiz answers will be discarded. Are you sure you want to return to the home screen?"
        confirmLabel="Leave Session"
        cancelLabel="Keep Studying"
        onConfirm={handleNewTopic}
        onCancel={() => setIsNavConfirmOpen(false)}
      />
    </div>
  );
}

