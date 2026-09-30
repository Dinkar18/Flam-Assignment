import React, { useState } from 'react';
import { RotateCcw, Layers, HelpCircle } from 'lucide-react';
import StudyCard from '../../flashcards/components/StudyCard';
import Quiz from '../../quiz/components/Quiz';
import ProgressIndicator from '../../../components/common/ProgressIndicator';
import ConfirmationDialog from '../../../components/common/ConfirmationDialog';
import { STUDY_MODES } from '../../../constants';

export default function StudySession({
  studySet,
  onCompleteQuiz,
  onNewTopic
}) {
  const [activeTab, setActiveTab] = useState(STUDY_MODES.FLASHCARDS);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [masteredIds, setMasteredIds] = useState(new Set());
  const [quizAnswers, setQuizAnswers] = useState({});
  const [confirmDialog, setConfirmDialog] = useState({ isOpen: false, type: null });

  const questions = studySet?.questions || [];
  const currentQuestion = questions[currentIndex];

  const handleToggleMastered = (id) => {
    setMasteredIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleNextQuestion = () => {
    if (currentIndex < questions.length - 1) {
      setCurrentIndex((prev) => prev + 1);
    } else {
      setCurrentIndex(0);
    }
  };

  const handlePrevQuestion = () => {
    if (currentIndex > 0) {
      setCurrentIndex((prev) => prev - 1);
    }
  };

  const handleConfirmAction = () => {
    if (confirmDialog.type === 'reset') {
      setCurrentIndex(0);
      setMasteredIds(new Set());
      setQuizAnswers({});
    } else if (confirmDialog.type === 'new_topic') {
      onNewTopic();
    }
    setConfirmDialog({ isOpen: false, type: null });
  };

  return (
    <div className="w-full space-y-5 animate-apple-fade font-sans">
      {/* Session Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 pb-1">
        <div className="min-w-0 flex-1">
          <span className="text-[10px] font-mono uppercase tracking-wider text-[#86868b] font-medium block mb-0.5">
            Active Study Session
          </span>
          <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight truncate">
            {studySet.title || 'Study Session'}
          </h2>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto shrink-0 text-xs">
          <button
            type="button"
            onClick={() => setConfirmDialog({ isOpen: true, type: 'reset' })}
            className="px-3.5 py-1.5 rounded-full border transition-all duration-200 flex items-center gap-1.5 cursor-pointer bg-white/[0.04] hover:bg-white/[0.08] border-white/10 text-[#a1a1a6] hover:text-white"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Reset</span>
          </button>

          <button
            type="button"
            onClick={() => setConfirmDialog({ isOpen: true, type: 'new_topic' })}
            className="px-3.5 py-1.5 rounded-full border transition-all duration-200 flex items-center gap-1.5 cursor-pointer bg-white text-black font-semibold border-transparent shadow-xs hover:bg-[#f5f5f7]"
          >
            <span>+ New Topic</span>
          </button>
        </div>
      </div>

      {/* Apple Segmented Tab Switcher */}
      <div className="flex rounded-2xl p-1.5 border text-xs font-medium bg-black/80 border-white/10 shadow-inner">
        <button
          type="button"
          onClick={() => setActiveTab(STUDY_MODES.FLASHCARDS)}
          className={`flex-1 py-2.5 px-4 rounded-xl flex items-center justify-center gap-2 transition-all duration-300 cursor-pointer ${
            activeTab === STUDY_MODES.FLASHCARDS
              ? 'bg-white text-black shadow-[0_0_12px_rgba(255,255,255,0.2)] font-bold'
              : 'text-[#86868b] hover:text-[#f5f5f7] hover:bg-white/[0.04]'
          }`}
        >
          <Layers className="w-4 h-4" />
          <span>Flashcards Mode</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab(STUDY_MODES.QUIZ)}
          className={`flex-1 py-2.5 px-4 rounded-xl flex items-center justify-center gap-2 transition-all duration-300 cursor-pointer ${
            activeTab === STUDY_MODES.QUIZ
              ? 'bg-white text-black shadow-[0_0_12px_rgba(255,255,255,0.2)] font-bold'
              : 'text-[#86868b] hover:text-[#f5f5f7] hover:bg-white/[0.04]'
          }`}
        >
          <HelpCircle className="w-4 h-4" />
          <span>Diagnostic Quiz</span>
        </button>
      </div>

      {/* Progress Track */}
      <ProgressIndicator
        currentIndex={currentIndex}
        total={questions.length}
        masteredCount={masteredIds.size}
      />

      {/* Content */}
      {activeTab === STUDY_MODES.FLASHCARDS ? (
        <StudyCard
          question={currentQuestion}
          index={currentIndex}
          total={questions.length}
          onNext={handleNextQuestion}
          onPrev={handlePrevQuestion}
          isMastered={masteredIds.has(currentQuestion?.id)}
          onToggleMastered={handleToggleMastered}
        />
      ) : (
        <Quiz
          questions={questions}
          currentIndex={currentIndex}
          setCurrentIndex={setCurrentIndex}
          quizAnswers={quizAnswers}
          setQuizAnswers={setQuizAnswers}
          onCompleteQuiz={onCompleteQuiz}
        />
      )}

      {/* Second-chance confirmation dialog */}
      <ConfirmationDialog
        isOpen={confirmDialog.isOpen}
        title={confirmDialog.type === 'reset' ? 'Reset Study Session?' : 'Start New Topic?'}
        message={
          confirmDialog.type === 'reset'
            ? 'This will clear all mastered flashcards and quiz submissions for this session. Are you sure?'
            : 'Your current session progress will be discarded and you will return to the input screen.'
        }
        confirmLabel={confirmDialog.type === 'reset' ? 'Reset Progress' : 'Leave Session'}
        cancelLabel="Keep Studying"
        onConfirm={handleConfirmAction}
        onCancel={() => setConfirmDialog({ isOpen: false, type: null })}
      />
    </div>
  );
}
