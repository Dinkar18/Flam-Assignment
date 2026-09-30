import React, { useState, useEffect } from 'react';
import { CheckCircle2, Circle, Eye, EyeOff, Lightbulb } from 'lucide-react';
import { useKeyboardShortcuts } from '../../../hooks/useKeyboardShortcuts';
import { DIFFICULTY_CONFIG } from '../../../constants';

export default function StudyCard({
  question,
  index,
  total,
  onNext,
  onPrev,
  isMastered,
  onToggleMastered
}) {
  const [isRevealed, setIsRevealed] = useState(false);

  useEffect(() => {
    setIsRevealed(false);
  }, [question?.id]);

  useKeyboardShortcuts({
    Space: () => setIsRevealed((prev) => !prev),
    ArrowRight: () => onNext(),
    ArrowLeft: () => onPrev(),
    m: () => onToggleMastered(question?.id),
    M: () => onToggleMastered(question?.id),
  }, [question?.id, onNext, onPrev, onToggleMastered]);

  const diff = DIFFICULTY_CONFIG[question.difficulty?.toLowerCase()] || DIFFICULTY_CONFIG.medium;

  return (
    <div className="w-full space-y-5 animate-apple-fade font-sans">
      {/* Primary Card */}
      <div className="rounded-3xl border p-7 sm:p-9 apple-card-glow space-y-6 relative overflow-hidden">
        {/* Top Metadata Row */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-mono font-medium border ${diff.border}`}>
              <span className={`w-1.5 h-1.5 rounded-full ${diff.dot}`} />
              {diff.label}
            </span>

            <span className="text-[11px] font-mono text-[#86868b] px-2 py-0.5 rounded-full bg-white/[0.04] border border-white/5">
              Card {index + 1} of {total}
            </span>
          </div>

          <button
            type="button"
            onClick={() => onToggleMastered(question.id)}
            className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-full border transition-all duration-200 cursor-pointer text-xs font-semibold ${
              isMastered
                ? 'bg-white text-black border-transparent shadow-[0_0_12px_rgba(255,255,255,0.3)]'
                : 'bg-black/50 text-[#86868b] border-white/10 hover:text-[#f5f5f7] hover:border-white/25 hover:bg-white/[0.04]'
            }`}
          >
            {isMastered ? (
              <CheckCircle2 className="w-3.5 h-3.5" />
            ) : (
              <Circle className="w-3.5 h-3.5" />
            )}
            <span>{isMastered ? 'Mastered' : 'Mark Mastered'}</span>
          </button>
        </div>

        {/* Question Text */}
        <div className="py-2">
          <h3 className="text-2xl sm:text-3xl font-semibold text-[#f5f5f7] tracking-tight leading-snug font-sans">
            {question.question}
          </h3>
        </div>

        {/* Answer Reveal Area */}
        {!isRevealed ? (
          <div className="space-y-2.5">
            <button
              type="button"
              onClick={() => setIsRevealed(true)}
              className="w-full py-4 px-5 rounded-2xl border border-dashed transition-all duration-200 flex items-center justify-center gap-2.5 cursor-pointer text-sm font-semibold text-[#f5f5f7] bg-black/60 border-white/20 hover:border-white/50 hover:bg-white/[0.06] hover:shadow-[0_0_25px_rgba(255,255,255,0.12)] group"
            >
              <Eye className="w-4 h-4 text-[#a1a1a6] group-hover:text-white transition-colors" />
              <span>Reveal Answer</span>
            </button>
            <div className="text-center">
              <span className="text-[11px] font-mono text-[#6e6e73]">
                Tip: Press <kbd className="px-1.5 py-0.5 rounded bg-white/10 text-[10px] text-[#a1a1a6] border border-white/10">Space</kbd> on keyboard to toggle answer
              </span>
            </div>
          </div>
        ) : (
          <div className="space-y-3 pt-2 border-t border-white/10 animate-apple-reveal">
            <div className="flex items-center justify-between text-xs text-[#86868b]">
              <span className="font-semibold text-white flex items-center gap-1.5">
                <Lightbulb className="w-4 h-4 text-emerald-400" />
                Core Answer & Key Takeaway
              </span>
              <button
                type="button"
                onClick={() => setIsRevealed(false)}
                className="hover:underline cursor-pointer text-[#86868b] hover:text-[#f5f5f7] flex items-center gap-1 text-xs"
              >
                <EyeOff className="w-3.5 h-3.5" />
                <span>Hide Answer</span>
              </button>
            </div>

            <div className="p-5 rounded-2xl border leading-relaxed bg-black/80 border-white/15 shadow-inner space-y-3">
              <div className="text-base sm:text-lg font-semibold text-white tracking-tight">
                {question.answer}
              </div>
              {question.explanation && question.explanation.trim() !== question.answer.trim() && (
                <div className="text-xs sm:text-sm text-[#a1a1a6] pt-3 border-t border-white/10 leading-relaxed font-sans">
                  {question.explanation}
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Navigation Controls */}
      <div className="flex items-center justify-between gap-3 text-xs">
        <button
          type="button"
          onClick={onPrev}
          disabled={index === 0}
          className="py-2.5 px-5 rounded-full border transition-all duration-200 flex items-center gap-1.5 cursor-pointer disabled:opacity-30 disabled:cursor-not-allowed bg-[#161617] border-white/10 text-[#a1a1a6] hover:bg-[#2c2c2e] hover:text-[#f5f5f7] hover:border-white/20"
        >
          <span>&larr; Previous</span>
        </button>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={onNext}
            className="py-2.5 px-6 rounded-full font-semibold transition-all duration-200 flex items-center gap-1.5 cursor-pointer text-black bg-white hover:bg-[#f5f5f7] border-transparent shadow-[0_0_16px_rgba(255,255,255,0.25)] hover:shadow-[0_0_25px_rgba(255,255,255,0.4)] active:scale-[0.98]"
          >
            <span>{index === total - 1 ? 'Finish Review' : 'Next'}</span>
            <span>&rarr;</span>
          </button>
        </div>
      </div>
    </div>
  );
}
