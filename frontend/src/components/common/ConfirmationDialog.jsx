import React, { useEffect } from 'react';

export default function ConfirmationDialog({
  isOpen,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
}) {
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        e.preventDefault();
        onCancel();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onCancel]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-md animate-apple-fade">
      {/* Click outside backdrop */}
      <div className="absolute inset-0" onClick={onCancel} />

      {/* Dialog Card */}
      <div className="relative max-w-sm w-full rounded-3xl border p-6 sm:p-7 bg-[#1c1c1e] border-white/20 apple-card-glow shadow-2xl space-y-4 animate-apple-reveal z-10 font-sans">
        <div className="space-y-2">
          <h3 className="text-lg font-semibold tracking-tight text-white">
            {title}
          </h3>
          <p className="text-xs sm:text-sm text-[#86868b] leading-relaxed">
            {message}
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-2.5 pt-2 text-xs">
          <button
            type="button"
            onClick={onCancel}
            className="px-4 py-2 rounded-full border transition-all duration-200 cursor-pointer bg-white/[0.06] hover:bg-white/[0.12] border-white/10 hover:border-white/25 text-[#f5f5f7] font-medium"
          >
            {cancelLabel}
          </button>

          <button
            type="button"
            onClick={onConfirm}
            className="px-4 py-2 rounded-full transition-all duration-200 cursor-pointer font-semibold shadow-[0_0_15px_rgba(255,255,255,0.2)] bg-white text-black hover:bg-[#f5f5f7] active:scale-[0.98]"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
