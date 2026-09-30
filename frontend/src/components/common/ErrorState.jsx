import React from 'react';

export default function ErrorState({ error, onRetry, onEdit }) {
  const errorCode = error?.code || 'GENERATION_FAILED';
  const errorMessage = error?.message || 'We could not generate the study set. Please try again.';
  const details = error?.details || [];

  return (
    <div className="w-full max-w-lg mx-auto p-8 rounded-3xl border text-center space-y-6 bg-[#161617] border-white/[0.08] shadow-apple-card animate-apple-fade font-sans">
      <div className="w-12 h-12 mx-auto rounded-full flex items-center justify-center bg-[#2c2c2e] border border-white/10">
        <span className="text-base text-[#f5f5f7] font-mono font-bold">!</span>
      </div>

      <div className="space-y-1">
        <div className="inline-block px-3 py-0.5 rounded-full font-mono text-[10px] uppercase font-semibold text-[#86868b] bg-[#000000] border border-white/10">
          {errorCode}
        </div>
        <h3 className="text-2xl font-semibold tracking-tight text-[#f5f5f7]">
          Unable to Generate Session
        </h3>
        <p className="text-xs sm:text-sm text-[#86868b] leading-relaxed max-w-md mx-auto">
          {errorMessage}
        </p>
      </div>

      {details.length > 0 && (
        <div className="p-4 rounded-2xl border text-left space-y-1 text-xs bg-[#000000] border-white/10">
          <div className="font-medium text-[#f5f5f7] font-mono text-[11px]">Validation Issues:</div>
          <ul className="list-disc list-inside space-y-0.5 text-[#86868b]">
            {details.map((det, idx) => (
              <li key={idx}>{det}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="flex flex-col sm:flex-row items-center justify-center gap-2.5 pt-1 text-xs">
        {onRetry && (
          <button
            type="button"
            onClick={onRetry}
            className="w-full sm:w-auto px-6 py-3 rounded-full font-medium flex items-center justify-center gap-1.5 cursor-pointer text-[#1d1d1f] bg-[#f5f5f7] hover:bg-white border-transparent shadow-apple-btn transition-all duration-200 active:scale-[0.98]"
          >
            <span>Try Again</span>
          </button>
        )}
        <button
          type="button"
          onClick={onEdit}
          className="w-full sm:w-auto px-6 py-3 rounded-full font-medium flex items-center justify-center gap-1.5 cursor-pointer bg-[#2c2c2e] hover:bg-[#3a3a3c] border border-white/10 text-[#f5f5f7] transition-all duration-200"
        >
          <span>Edit Notes</span>
        </button>
      </div>
    </div>
  );
}
