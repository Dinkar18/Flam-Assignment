import { useEffect } from 'react';

/**
 * Custom hook to manage global keyboard shortcuts with proper event lifecycle and unmounting.
 */
export function useKeyboardShortcuts(shortcuts = {}, deps = []) {
  useEffect(() => {
    const handleKeyDown = (event) => {
      // Ignore if user is currently typing in an input or textarea
      if (['INPUT', 'TEXTAREA'].includes(event.target.tagName)) {
        return;
      }

      const handler = shortcuts[event.code] || shortcuts[event.key];
      if (handler) {
        event.preventDefault();
        handler(event);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, deps); // eslint-disable-line react-hooks/exhaustive-deps
}
