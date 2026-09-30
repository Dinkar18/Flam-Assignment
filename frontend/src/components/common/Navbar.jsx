import React from 'react';
import { BookOpen } from 'lucide-react';

export default function Navbar({ onReset }) {
  return (
    <header className="apple-glass sticky top-0 z-50 transition-all duration-200">
      <div className="max-w-3xl lg:max-w-4xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
        {/* Apple Minimalist Brand & Title with Glowing Book Icon */}
        <button
          onClick={onReset}
          className="flex items-center gap-3 text-left focus:outline-none rounded-full py-1 px-1.5 transition-all duration-200 cursor-pointer hover:opacity-90 group"
          title="Return to Home"
        >
          {/* Glowing Book Icon Capsule */}
          <div className="relative flex items-center justify-center">
            <div className="absolute inset-0 rounded-full bg-white/20 blur-md group-hover:bg-white/35 transition-all" />
            <div className="relative w-8 h-8 rounded-full flex items-center justify-center bg-gradient-to-b from-[#2c2c2e] via-[#1c1c1e] to-[#000000] border border-white/30 shadow-[0_0_14px_rgba(255,255,255,0.2)] group-hover:border-white/50 group-hover:shadow-[0_0_22px_rgba(255,255,255,0.35)] transition-all">
              <BookOpen className="w-4 h-4 text-white" />
            </div>
          </div>

          <div className="flex flex-col">
            <span className="text-sm font-semibold tracking-tight text-white group-hover:text-white transition-colors">
              Study Assistant
            </span>
            <span className="text-[10px] font-mono text-[#86868b] tracking-wider -mt-0.5">
              Active Recall AI
            </span>
          </div>
        </button>

        {/* Apple-style right action with subtle glow */}
        <button
          onClick={onReset}
          className="text-xs text-[#f5f5f7] hover:text-white px-3.5 py-1.5 rounded-full border border-white/15 hover:border-white/35 bg-white/[0.06] hover:bg-white/[0.12] hover:shadow-[0_0_15px_rgba(255,255,255,0.15)] transition-all duration-200 cursor-pointer font-medium active:scale-[0.98]"
        >
          + New Session
        </button>
      </div>
    </header>
  );
}

