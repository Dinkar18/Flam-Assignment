import React from 'react';

export default function ProgressIndicator({ currentIndex, total, masteredCount = 0 }) {
  const percentage = Math.round(((currentIndex + 1) / total) * 100);

  return (
    <div className="w-full space-y-2 text-xs font-sans">
      <div className="flex items-center justify-between text-[#86868b]">
        <div className="flex items-center gap-2">
          <span className="font-mono text-[11px] text-[#f5f5f7] font-semibold">
            Progress {currentIndex + 1}/{total}
          </span>
          <span className="text-white/20">•</span>
          <span className="font-mono text-[11px] text-[#86868b]">
            {percentage}%
          </span>
        </div>

        {masteredCount > 0 && (
          <span className="text-white font-medium font-mono text-[11px] px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-300 border border-emerald-500/20 flex items-center gap-1">
            <span className="w-1 h-1 rounded-full bg-emerald-400" />
            {masteredCount} Mastered
          </span>
        )}
      </div>

      {/* Progress Track */}
      <div className="w-full h-1.5 rounded-full overflow-hidden bg-black/80 border border-white/10 p-[1px]">
        <div
          className="h-full rounded-full transition-all duration-500 ease-out bg-gradient-to-r from-white/70 via-white to-white shadow-[0_0_8px_rgba(255,255,255,0.4)]"
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
}

