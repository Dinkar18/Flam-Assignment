import React, { useState, useEffect } from 'react';

const STEPS = [
  { label: "Analyzing source notes & knowledge hierarchy...", icon: "psychology" },
  { label: "Synthesizing core concepts & memory anchors...", icon: "auto_awesome" },
  { label: "Generating & validating diagnostic distractors...", icon: "fact_check" },
  { label: "Assembling interactive recall session...", icon: "style" }
];

export default function LoadingState({ onCancel }) {
  const [stepIndex, setStepIndex] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setStepIndex((prev) => (prev < STEPS.length - 1 ? prev + 1 : prev));
    }, 1200);
    return () => clearInterval(timer);
  }, []);

  const progressPercent = Math.min(100, Math.round(((stepIndex + 1) / STEPS.length) * 100));

  return (
    <div className="w-full max-w-lg mx-auto p-7 sm:p-9 rounded-3xl border text-center space-y-7 apple-card-glow animate-apple-fade font-sans relative overflow-hidden bg-black/90 border-white/20 shadow-[0_0_50px_rgba(255,255,255,0.08)]">
      {/* Radiant ambient glow halo */}
      <div className="absolute -top-12 left-1/2 -translate-x-1/2 w-64 h-64 bg-white/[0.06] blur-3xl pointer-events-none rounded-full" />

      {/* Luminous Apple-style Spinner */}
      <div className="relative w-16 h-16 mx-auto flex items-center justify-center">
        <div className="absolute inset-0 rounded-full bg-white/10 blur-md animate-pulse" />
        <div className="w-14 h-14 rounded-full flex items-center justify-center bg-black/80 border border-white/20 shadow-[0_0_25px_rgba(255,255,255,0.2)]">
          <div className="w-7 h-7 border-2 border-white/20 border-t-white rounded-full animate-spin" />
        </div>
      </div>

      {/* Header */}
      <div className="space-y-1.5 relative z-10">
        <h3 className="text-xl sm:text-2xl font-bold tracking-tight text-white">
          Synthesizing Study Set
        </h3>
        <p className="text-xs sm:text-sm text-[#86868b] max-w-xs mx-auto">
          Extracting concepts, generating distractors, and structuring recall cards.
        </p>
      </div>

      {/* Progress Bar */}
      <div className="space-y-1.5 px-1">
        <div className="flex justify-between text-[11px] font-mono text-[#86868b]">
          <span>AI Pipeline</span>
          <span className="text-white font-semibold">{progressPercent}%</span>
        </div>
        <div className="w-full h-1.5 bg-white/10 rounded-full overflow-hidden p-0.5">
          <div
            className="h-full bg-gradient-to-r from-white/70 via-white to-white/90 rounded-full transition-all duration-500 shadow-[0_0_10px_rgba(255,255,255,0.5)]"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>

      {/* Evaluation Status Steps Box */}
      <div className="p-4 sm:p-5 rounded-2xl border text-left space-y-2.5 text-xs bg-black/80 border-white/15 shadow-inner relative z-10">
        {STEPS.map((step, idx) => {
          const isDone = idx < stepIndex;
          const isCurrent = idx === stepIndex;
          return (
            <div
              key={idx}
              className={`flex items-center gap-3 p-2 rounded-xl transition-all duration-300 ${
                isCurrent
                  ? 'bg-white/[0.08] border border-white/20 shadow-[0_0_15px_rgba(255,255,255,0.08)]'
                  : 'border border-transparent'
              }`}
            >
              <div className="shrink-0 flex items-center justify-center w-5 h-5">
                {isDone ? (
                  <span className="w-5 h-5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-bold text-[11px] flex items-center justify-center">
                    ✓
                  </span>
                ) : isCurrent ? (
                  <div className="relative flex items-center justify-center w-4 h-4">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75" />
                    <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-white shadow-[0_0_8px_rgba(255,255,255,0.8)]" />
                  </div>
                ) : (
                  <div className="w-2 h-2 rounded-full bg-[#3f3f46]" />
                )}
              </div>

              <span
                className={`flex-1 leading-snug font-medium transition-colors ${
                  isCurrent
                    ? 'text-white font-semibold'
                    : isDone
                    ? 'text-[#86868b]'
                    : 'text-[#52525b]'
                }`}
              >
                {step.label}
              </span>
            </div>
          );
        })}
      </div>

      {/* Cancel Action */}
      <div className="pt-1">
        <button
          type="button"
          onClick={onCancel}
          className="text-xs text-[#86868b] hover:text-white px-5 py-2.5 rounded-full border border-white/10 hover:border-white/30 bg-white/[0.04] hover:bg-white/[0.08] transition-all duration-200 cursor-pointer font-medium hover:shadow-[0_0_15px_rgba(255,255,255,0.1)] active:scale-[0.98]"
        >
          Cancel Request
        </button>
      </div>
    </div>
  );
}
