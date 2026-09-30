import React, { useState } from 'react';
import { Database, Boxes, Network, FileText, SlidersHorizontal, ArrowRight, AlertCircle } from 'lucide-react';

const PRESETS = [
  {
    label: 'Java HashMap Internals',
    tag: 'Backend Architecture',
    Icon: Database,
    description: 'Hash tables, Red-Black tree conversion (bucket ≥ 8), and collision chaining.',
    text: `HashMap is a hash table-based implementation of the Map interface in Java.
- Key characteristics: allows one null key and multiple null values, not synchronized (not thread-safe).
- Default initial capacity is 16, default load factor is 0.75.
- Bucket threshold: When a bucket has 8 or more entries and table capacity is >= 64, the linked list converts into a Red-Black Tree (TreeBin) for O(log n) performance instead of O(n).
- Hash collisions are resolved via chaining.
- hashCode() and equals() contract: if two objects are equal according to equals(), they must produce the identical hashCode().`
  },
  {
    label: 'React Hooks & State',
    tag: 'Frontend Engineering',
    Icon: Boxes,
    description: 'useState state transitions, useEffect lifecycle rules, and dependency arrays.',
    text: `React Hooks allow functional components to manage local state and lifecycle side-effects.
- useState: declares state variable that triggers a re-render when its setter is called with a new reference.
- useEffect: runs side effects after DOM render. An empty dependency array [] runs once on mount; omitted dependency runs after every single render; listed dependencies run when values change.
- Cleanup functions in useEffect run prior to the component unmounting and before the next effect execution.
- Rules of Hooks: Only call hooks at the top level (never in loops, conditions, or nested functions). Only call from React components or custom hooks.`
  },
  {
    label: 'REST & HTTP Semantics',
    tag: 'Distributed Systems',
    Icon: Network,
    description: 'Statelessness, verb idempotency (GET/POST/PUT), and HTTP status codes.',
    text: `REST (Representational State Transfer) is an architectural style for distributed systems.
- Statelessness: Each request from client to server must contain all necessary authentication and context to be fully understood.
- HTTP Verbs: GET (safe, idempotent, retrieves resource), POST (non-idempotent, creates resource), PUT (idempotent, replaces resource), PATCH (modifies resource partially), DELETE (idempotent, removes resource).
- Key Status Codes: 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 502 Bad Gateway.`
  }
];

export default function StudyForm({ onSubmit, isGenerating }) {
  const [prompt, setPrompt] = useState('');
  const [questionCount, setQuestionCount] = useState(5);
  const [error, setError] = useState(null);

  const validate = () => {
    if (!prompt.trim()) {
      setError('Please enter a topic, notes, or study prompt');
      return false;
    }
    if (prompt.trim().length > 5000) {
      setError('Input must not exceed 5000 characters');
      return false;
    }
    if (!questionCount || questionCount < 3 || questionCount > 10) {
      setError('Question count must be between 3 and 10');
      return false;
    }
    setError(null);
    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      onSubmit({ prompt: prompt.trim(), questionCount });
    }
  };

  const applyPreset = (preset) => {
    setPrompt(preset.text);
    setError(null);
  };

  return (
    <div className="w-full space-y-4 sm:space-y-5 animate-apple-fade font-sans">
      {/* Hero Header */}
      <div className="text-left space-y-1 sm:space-y-1.5 pt-1">
        <h1 className="text-2xl sm:text-3xl md:text-4xl font-semibold tracking-tight leading-tight bg-gradient-to-b from-white via-[#f0f0f2] to-[#86868b] bg-clip-text text-transparent">
          Transform Notes into Mastery.
        </h1>
        <p className="text-xs sm:text-sm text-[#86868b] leading-relaxed max-w-xl">
          Paste study material, lecture notes, or any topic to generate interactive flashcards and a diagnostic quiz with instant AI feedback.
        </p>
      </div>

      {/* Sleek Preset Starters */}
      <div className="space-y-2">
        <div className="flex items-center justify-between text-[11px] text-[#86868b] px-0.5 font-mono uppercase tracking-wider font-semibold">
          <span>Featured Topics</span>
          <span className="text-[10px] normal-case text-[#6e6e73]">Click to auto-fill notes</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-2.5">
          {PRESETS.map((p, idx) => {
            const IconComponent = p.Icon;
            return (
              <button
                key={idx}
                type="button"
                onClick={() => applyPreset(p)}
                className="text-left p-3 sm:p-3.5 rounded-2xl border transition-all duration-300 group cursor-pointer apple-card-glow glow-border-interactive flex flex-col justify-between gap-1.5 min-h-[78px]"
              >
                <div className="flex items-center justify-between w-full">
                  <div className="flex items-center gap-2">
                    <div className="w-6 h-6 rounded-lg flex items-center justify-center bg-white/10 text-white border border-white/10 group-hover:bg-white group-hover:text-black transition-colors shrink-0">
                      <IconComponent className="w-3.5 h-3.5" />
                    </div>
                    <span className="text-[9px] font-mono uppercase tracking-wider px-2 py-0.5 rounded-full bg-white/[0.08] text-[#f5f5f7] font-semibold border border-white/10 group-hover:border-white/30 group-hover:bg-white/15 transition-all">
                      {p.tag}
                    </span>
                  </div>
                  <span className="text-[#86868b] group-hover:text-white group-hover:translate-x-0.5 transition-all font-semibold text-xs">
                    &rarr;
                  </span>
                </div>

                <div>
                  <div className="text-xs sm:text-sm font-bold text-white tracking-tight group-hover:text-white transition-colors">
                    {p.label}
                  </div>
                  <p className="text-[11px] text-[#86868b] leading-tight group-hover:text-[#a1a1a6] transition-colors line-clamp-1 mt-0.5">
                    {p.description}
                  </p>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Main Free-form Canvas Card with Glowing Border */}
      <form onSubmit={handleSubmit} className="p-4 sm:p-6 md:p-7 rounded-2xl sm:rounded-3xl border apple-card-glow hover:border-white/30 focus-within:border-white/40 focus-within:shadow-[0_0_35px_rgba(255,255,255,0.12)] transition-all duration-300 space-y-4 sm:space-y-4.5">
        {/* Single Free-form Textarea */}
        <div className="space-y-1.5">
          <div className="flex justify-between items-center text-xs font-medium text-[#a1a1a6]">
            <span className="text-white font-semibold flex items-center gap-1.5 text-xs sm:text-sm">
              <FileText className="w-4 h-4 text-[#a1a1a6]" />
              Study Notes or Custom Prompt
            </span>
            <span className="text-[11px] font-mono text-[#86868b]">
              {prompt.length}/5000
            </span>
          </div>
          <textarea
            id="prompt"
            rows={4}
            value={prompt}
            onChange={(e) => {
              setPrompt(e.target.value);
              if (error) setError(null);
            }}
            placeholder="e.g. Explain Java HashMap internals with collision handling, or paste notes from your textbook / lecture slides..."
            maxLength={5000}
            className="w-full p-3.5 sm:p-4 rounded-xl text-xs sm:text-sm transition-all duration-200 focus:outline-none resize-y leading-relaxed bg-[#000000]/80 border border-[#333336] focus:border-white/60 focus:ring-2 focus:ring-white/20 focus:shadow-[0_0_20px_rgba(255,255,255,0.12)] text-[#f5f5f7] placeholder-[#6e6e73]"
          />
          {error && (
            <p className="text-xs text-red-400 font-medium pt-0.5 flex items-center gap-1.5">
              <AlertCircle className="w-3.5 h-3.5 shrink-0" />
              {error}
            </p>
          )}
        </div>

        {/* Question Count Selector */}
        <div className="space-y-1.5">
          <div className="flex justify-between items-center text-xs font-medium text-[#a1a1a6]">
            <span className="text-[#f5f5f7] flex items-center gap-1.5 font-medium text-[11px] sm:text-xs">
              <SlidersHorizontal className="w-3.5 h-3.5 text-[#a1a1a6]" />
              Target Question Count
            </span>
            <span className="text-[11px] font-mono font-semibold px-2 py-0.5 rounded-md bg-white/10 text-white border border-white/15 shadow-[0_0_10px_rgba(255,255,255,0.1)]">
              {questionCount} Questions
            </span>
          </div>

          {/* Responsive Segmented Pill Grid */}
          <div className="p-1 rounded-xl bg-[#000000]/90 border border-white/10 grid grid-cols-4 sm:grid-cols-8 gap-1 shadow-inner">
            {[3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
              <button
                key={num}
                type="button"
                onClick={() => setQuestionCount(num)}
                className={`py-1.5 sm:py-2 text-xs font-medium rounded-lg transition-all duration-200 cursor-pointer ${
                  questionCount === num
                    ? 'bg-white text-black font-bold shadow-[0_0_14px_rgba(255,255,255,0.35)]'
                    : 'text-[#86868b] hover:text-[#f5f5f7] hover:bg-white/[0.06]'
                }`}
              >
                {num}
              </button>
            ))}
          </div>
        </div>

        {/* Action Button */}
        <div className="pt-1">
          <button
            type="submit"
            disabled={isGenerating}
            className="w-full py-3 sm:py-3.5 px-6 rounded-full font-semibold text-xs sm:text-sm transition-all duration-300 flex items-center justify-center gap-2 text-black bg-gradient-to-r from-white via-[#f5f5f7] to-[#e5e5e7] hover:brightness-105 active:scale-[0.98] cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed shadow-[0_0_25px_rgba(255,255,255,0.25)] hover:shadow-[0_0_40px_rgba(255,255,255,0.45)]"
          >
            <span>Generate Study Session</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </form>
    </div>
  );
}
