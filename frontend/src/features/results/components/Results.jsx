import React from 'react';
import { RotateCcw, Layers, ClipboardCheck } from 'lucide-react';

export default function Results({
  topic,
  quizResults,
  onRetakeQuiz,
  onSwitchToFlashcards,
  onNewTopic
}) {
  const answerEntries = Object.values(quizResults || {});
  const total = answerEntries.length || 1;
  const correctCount = answerEntries.filter((a) => a.isCorrect).length;
  const percentage = Math.round((correctCount / total) * 100);

  // SVG Radial Gauge Metrics
  const radius = 48;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (percentage / 100) * circumference;

  return (
    <div className="w-full space-y-6 animate-apple-fade font-sans">
      {/* Apple Keynote Summary Card */}
      <div className="rounded-3xl border p-8 sm:p-10 text-center space-y-6 apple-card-glow relative overflow-hidden">
        {/* Subtle radial aura behind score */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-48 h-48 bg-white/[0.04] blur-3xl pointer-events-none rounded-full" />

        {/* Circular SVG Gauge */}
        <div className="relative flex items-center justify-center pt-2">
          <div className="relative w-36 h-36 flex items-center justify-center">
            <svg className="w-full h-full -rotate-90 transform" viewBox="0 0 120 120">
              {/* Background Track Ring */}
              <circle
                cx="60"
                cy="60"
                r={radius}
                className="text-white/[0.06]"
                strokeWidth="8"
                stroke="currentColor"
                fill="transparent"
              />
              {/* Animated Progress Ring */}
              <circle
                cx="60"
                cy="60"
                r={radius}
                className="text-white transition-all duration-1000 ease-out"
                strokeWidth="8"
                strokeDasharray={circumference}
                strokeDashoffset={strokeDashoffset}
                strokeLinecap="round"
                stroke="currentColor"
                fill="transparent"
              />
            </svg>

            {/* Score in Center */}
            <div className="absolute flex flex-col items-center justify-center">
              <span className="text-3xl sm:text-4xl font-mono font-bold text-white tracking-tight">
                {percentage}%
              </span>
              <span className="text-[10px] uppercase font-mono text-[#86868b] tracking-wider font-semibold">
                Mastery
              </span>
            </div>
          </div>
        </div>

        {/* Topic Title */}
        <div className="space-y-1">
          <h2 className="text-2xl sm:text-3xl font-semibold tracking-tight text-white">
            {percentage >= 80 ? 'Exceptional Performance' : percentage >= 50 ? 'Strong Progress' : 'Needs Reinforcement'}
          </h2>
          <p className="text-xs text-[#86868b] max-w-sm mx-auto truncate font-medium">
            {topic}
          </p>
        </div>

        {/* Statistical KPI Grid */}
        <div className="grid grid-cols-3 gap-3 max-w-sm mx-auto py-1">
          <div className="p-4 rounded-2xl border bg-black/60 border-white/10 shadow-inner">
            <div className="text-xl font-mono font-bold text-white">{percentage}%</div>
            <div className="text-[11px] text-[#86868b] mt-0.5 font-medium">Accuracy</div>
          </div>
          <div className="p-4 rounded-2xl border bg-black/60 border-white/10 shadow-inner">
            <div className="text-xl font-mono font-bold text-emerald-400">{correctCount}</div>
            <div className="text-[11px] text-[#86868b] mt-0.5 font-medium">Correct</div>
          </div>
          <div className="p-4 rounded-2xl border bg-black/60 border-white/10 shadow-inner">
            <div className="text-xl font-mono font-bold text-[#86868b]">{total}</div>
            <div className="text-[11px] text-[#86868b] mt-0.5 font-medium">Total</div>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex flex-wrap items-center justify-center gap-3 pt-2 text-xs">
          <button
            type="button"
            onClick={onRetakeQuiz}
            className="px-6 py-3 rounded-full font-semibold flex items-center gap-1.5 cursor-pointer text-black bg-white hover:bg-[#f5f5f7] border-transparent shadow-[0_0_16px_rgba(255,255,255,0.2)] transition-all duration-200 active:scale-[0.98]"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Retake Quiz</span>
          </button>

          <button
            type="button"
            onClick={onSwitchToFlashcards}
            className="px-5 py-3 rounded-full font-medium flex items-center gap-1.5 cursor-pointer bg-white/[0.08] hover:bg-white/[0.14] border border-white/10 text-white transition-all duration-200"
          >
            <Layers className="w-4 h-4" />
            <span>Review Flashcards</span>
          </button>

          <button
            type="button"
            onClick={onNewTopic}
            className="px-5 py-3 rounded-full font-medium flex items-center gap-1.5 cursor-pointer bg-white/[0.04] hover:bg-white/[0.08] border border-white/10 text-[#a1a1a6] hover:text-white transition-all duration-200"
          >
            <span>+ New Session</span>
          </button>
        </div>
      </div>

      {/* Itemized Breakdown */}
      <div className="space-y-3">
        <h3 className="text-xs font-mono font-medium uppercase tracking-wider text-[#86868b] px-1 flex items-center gap-1.5">
          <ClipboardCheck className="w-4 h-4" />
          Diagnostic Breakdown ({total} Questions)
        </h3>

        <div className="space-y-3">
          {answerEntries.map((ans, idx) => (
            <div
              key={idx}
              className="p-5 rounded-2xl border apple-card-glow space-y-3"
            >
              <div className="flex items-start justify-between gap-3">
                <h4 className="text-sm sm:text-base font-semibold text-white leading-snug">
                  {idx + 1}. {ans.question}
                </h4>

                <div className="shrink-0">
                  {ans.isCorrect ? (
                    <span className="text-[11px] font-mono font-bold px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-300 border border-emerald-500/20 flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                      PASSED
                    </span>
                  ) : (
                    <span className="text-[11px] font-mono font-medium px-2.5 py-0.5 rounded-full bg-red-500/10 text-red-300 border border-red-500/20 flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-red-400" />
                      MISSED
                    </span>
                  )}
                </div>
              </div>

              <div className="pt-2 border-t border-white/[0.06] space-y-2 text-xs">
                {!ans.isCorrect && (
                  <div className="text-[#a1a1a6]">
                    <span className="font-semibold text-red-400 mr-1.5">Your Choice:</span> {ans.selected || 'None'}
                  </div>
                )}
                <div className="text-white font-medium">
                  <span className="font-semibold text-emerald-400 mr-1.5">Correct Answer:</span> {ans.correctAnswer}
                </div>
                {ans.explanation && (
                  <div className="text-[#86868b] leading-relaxed pt-2 border-t border-white/5 font-sans">
                    <span className="text-white font-medium">Concept Takeaway:</span> {ans.explanation}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
