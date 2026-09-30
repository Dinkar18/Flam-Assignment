import React, { useState, useEffect, useMemo } from 'react';
import { CheckCircle2, CheckCircle } from 'lucide-react';

export default function Quiz({
  questions,
  currentIndex,
  setCurrentIndex,
  quizAnswers,
  setQuizAnswers,
  onCompleteQuiz
}) {
  const currentQuestion = questions[currentIndex];
  const existingAnswer = currentQuestion ? quizAnswers[currentQuestion.id] : null;

  const [selectedOption, setSelectedOption] = useState(existingAnswer ? existingAnswer.selected : null);
  const [isAnswerSubmitted, setIsAnswerSubmitted] = useState(!!existingAnswer);

  useEffect(() => {
    if (existingAnswer) {
      setSelectedOption(existingAnswer.selected);
      setIsAnswerSubmitted(true);
    } else {
      setSelectedOption(null);
      setIsAnswerSubmitted(false);
    }
  }, [currentIndex, existingAnswer]);

  const options = useMemo(() => {
    if (!currentQuestion || !currentQuestion.options) return [];
    
    // Deterministically shuffle options based on question ID so the correct answer is randomized across A/B/C/D
    return [...currentQuestion.options].sort((a, b) => {
      const hashA = (currentQuestion.id + a).split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
      const hashB = (currentQuestion.id + b).split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
      return (hashA % 19) - (hashB % 19);
    });
  }, [currentQuestion]);

  const handleSelectOption = (option) => {
    if (isAnswerSubmitted) return;
    setSelectedOption(option);
  };

  const handleSubmitAnswer = () => {
    if (!selectedOption || isAnswerSubmitted) return;

    const isCorrect = selectedOption.trim().toLowerCase() === currentQuestion.answer.trim().toLowerCase();
    setIsAnswerSubmitted(true);

    setQuizAnswers((prev) => ({
      ...prev,
      [currentQuestion.id]: {
        question: currentQuestion.question,
        selected: selectedOption,
        correctAnswer: currentQuestion.answer,
        explanation: currentQuestion.explanation,
        isCorrect,
        difficulty: currentQuestion.difficulty
      }
    }));
  };

  const handleNext = () => {
    if (currentIndex < questions.length - 1) {
      setCurrentIndex((prev) => prev + 1);
    } else {
      onCompleteQuiz(quizAnswers);
    }
  };

  const handlePrev = () => {
    if (currentIndex > 0) {
      setCurrentIndex((prev) => prev - 1);
    }
  };

  if (!currentQuestion) return null;

  const optionLetters = ['A', 'B', 'C', 'D', 'E'];

  return (
    <div className="w-full space-y-5 animate-apple-fade font-sans">
      {/* Quiz Card */}
      <div className="rounded-3xl border p-7 sm:p-9 apple-card-glow space-y-6 relative overflow-hidden">
        {/* Top Header Row */}
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-mono text-[#86868b] px-2.5 py-0.5 rounded-full bg-white/[0.04] border border-white/5">
            Question {currentIndex + 1} of {questions.length}
          </span>

          <span className="text-[11px] font-mono text-[#a1a1a6] uppercase tracking-wider">
            Diagnostic Assessment
          </span>
        </div>

        {/* Question Text */}
        <div className="py-1">
          <h3 className="text-2xl sm:text-3xl font-semibold text-[#f5f5f7] tracking-tight leading-snug font-sans">
            {currentQuestion.question}
          </h3>
        </div>

        {/* Options List */}
        <div className="space-y-3">
          {options.map((option, idx) => {
            const letter = optionLetters[idx] || `${idx + 1}`;
            const isSelected = selectedOption === option;
            const isCorrectOption = option.trim().toLowerCase() === currentQuestion.answer.trim().toLowerCase();

            let optionStyle = "bg-black/60 hover:bg-white/[0.04] border-white/10 text-[#f5f5f7] hover:border-white/20";

            if (isAnswerSubmitted) {
              if (isCorrectOption) {
                optionStyle = "bg-white text-black font-semibold border-white shadow-[0_0_20px_rgba(255,255,255,0.25)]";
              } else if (isSelected && !isCorrectOption) {
                optionStyle = "bg-[#1c1c1e] border-red-500/30 text-[#86868b] opacity-60";
              } else {
                optionStyle = "bg-black/40 border-white/5 text-[#52525b] opacity-35";
              }
            } else if (isSelected) {
              optionStyle = "bg-white text-black font-semibold border-transparent shadow-[0_0_16px_rgba(255,255,255,0.2)]";
            }

            return (
              <button
                key={idx}
                type="button"
                onClick={() => handleSelectOption(option)}
                disabled={isAnswerSubmitted}
                className={`w-full text-left p-4 sm:p-4.5 rounded-2xl border transition-all duration-200 flex items-start gap-3.5 cursor-pointer disabled:cursor-default ${optionStyle}`}
              >
                <span className={`w-6 h-6 rounded-full text-xs font-mono font-semibold flex items-center justify-center shrink-0 transition-colors ${
                  isAnswerSubmitted && isCorrectOption
                    ? 'bg-black text-white'
                    : isAnswerSubmitted && isSelected && !isCorrectOption
                    ? 'bg-red-500/20 text-red-400'
                    : isSelected
                    ? 'bg-black text-white'
                    : 'bg-white/10 text-[#a1a1a6]'
                }`}>
                  {letter}
                </span>

                <div className="flex-1 text-sm leading-relaxed pt-0.5 font-sans">
                  {option}
                </div>

                {isAnswerSubmitted && isCorrectOption && (
                  <span className="text-xs font-mono font-bold text-black shrink-0 self-center flex items-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    Correct
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* Diff Feedback */}
        {isAnswerSubmitted && (
          <div className="p-5 rounded-2xl border text-sm leading-relaxed animate-apple-reveal bg-black/80 border-white/10 space-y-2.5 shadow-inner">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-sm">
                {selectedOption?.trim().toLowerCase() === currentQuestion.answer.trim().toLowerCase() ? (
                  <span className="text-emerald-400 flex items-center gap-1.5">
                    <CheckCircle className="w-4 h-4" />
                    Correct Response
                  </span>
                ) : (
                  <span className="text-[#86868b]">
                    Correct answer: <strong className="text-white font-semibold ml-1">{currentQuestion.answer}</strong>
                  </span>
                )}
              </span>
            </div>
            {currentQuestion.explanation && (
              <p className="text-xs sm:text-sm text-[#a1a1a6] leading-relaxed pt-2 border-t border-white/10 font-sans">
                {currentQuestion.explanation}
              </p>
            )}
          </div>
        )}

        {/* Actions Bar */}
        <div className="pt-2 flex items-center justify-between gap-3 text-xs">
          <button
            type="button"
            onClick={handlePrev}
            disabled={currentIndex === 0}
            className="py-2.5 px-5 rounded-full border transition-all duration-200 flex items-center gap-1.5 cursor-pointer disabled:opacity-30 disabled:cursor-not-allowed bg-[#161617] border-white/10 text-[#a1a1a6] hover:bg-[#2c2c2e] hover:text-[#f5f5f7]"
          >
            <span>&larr; Previous</span>
          </button>

          {!isAnswerSubmitted ? (
            <button
              type="button"
              onClick={handleSubmitAnswer}
              disabled={!selectedOption}
              className="flex-1 py-3 px-6 rounded-full font-semibold text-sm text-black bg-white hover:bg-[#f5f5f7] border-transparent shadow-[0_0_16px_rgba(255,255,255,0.2)] transition-all duration-200 cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed active:scale-[0.98]"
            >
              Submit Answer
            </button>
          ) : currentIndex < questions.length - 1 ? (
            <button
              type="button"
              onClick={handleNext}
              className="flex-1 py-3 px-6 rounded-full font-semibold text-sm text-black bg-white hover:bg-[#f5f5f7] border-transparent shadow-[0_0_16px_rgba(255,255,255,0.2)] transition-all duration-200 flex items-center justify-center gap-1.5 cursor-pointer active:scale-[0.98]"
            >
              <span>Next Question</span>
              <span>&rarr;</span>
            </button>
          ) : (
            <button
              type="button"
              onClick={handleNext}
              className="flex-1 py-3 px-6 rounded-full font-semibold text-sm text-black bg-white hover:bg-[#f5f5f7] border-transparent shadow-[0_0_16px_rgba(255,255,255,0.2)] transition-all duration-200 flex items-center justify-center gap-1.5 cursor-pointer active:scale-[0.98]"
            >
              <span>Complete Session</span>
              <span>&rarr;</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
