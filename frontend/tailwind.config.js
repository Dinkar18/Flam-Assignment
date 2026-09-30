/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        apple: {
          bg: '#000000',
          surface: '#161617',
          surfaceHover: '#1c1c1e',
          card: '#1d1d1f',
          elevated: '#2c2c2e',
          border: 'rgba(255, 255, 255, 0.08)',
          borderHover: 'rgba(255, 255, 255, 0.16)',
          borderSubtle: '#424245',
          text: '#f5f5f7',
          muted: '#86868b',
          dim: '#6e6e73',
          link: '#2997ff',
        }
      },
      fontFamily: {
        sans: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"SF Pro Text"',
          '"SF Pro Display"',
          '"SF Pro"',
          '"Helvetica Neue"',
          'Helvetica',
          'Arial',
          'sans-serif'
        ],
        mono: [
          '"SF Mono"',
          'Menlo',
          'Monaco',
          'Consolas',
          '"Courier New"',
          'monospace'
        ],
      },
      boxShadow: {
        'apple-card': '0 0 0 1px rgba(255, 255, 255, 0.08), 0 20px 40px -15px rgba(0, 0, 0, 0.7)',
        'apple-btn': '0 1px 3px 0 rgba(0, 0, 0, 0.3)',
        'glow-white-sm': '0 0 16px rgba(255, 255, 255, 0.18)',
        'glow-white': '0 0 28px rgba(255, 255, 255, 0.22), 0 0 0 1px rgba(255, 255, 255, 0.35)',
        'glow-white-lg': '0 0 50px rgba(255, 255, 255, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.5)',
      },
      transitionTimingFunction: {
        'apple-spring': 'cubic-bezier(0.25, 1, 0.5, 1)',
      },
      animation: {
        'apple-fade': 'appleFade 240ms cubic-bezier(0.25, 1, 0.5, 1)',
        'apple-slide': 'appleSlide 280ms cubic-bezier(0.25, 1, 0.5, 1)',
        'apple-reveal': 'appleReveal 260ms cubic-bezier(0.25, 1, 0.5, 1)',
        'glow-pulse': 'glowPulse 3.5s ease-in-out infinite',
        'border-spin': 'borderSpin 4s linear infinite',
      },
      keyframes: {
        appleFade: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        appleSlide: {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        appleReveal: {
          '0%': { opacity: '0', transform: 'translateY(-6px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        glowPulse: {
          '0%, 100%': {
            boxShadow: '0 0 18px rgba(255, 255, 255, 0.12), 0 0 0 1px rgba(255, 255, 255, 0.25) inset',
            borderColor: 'rgba(255, 255, 255, 0.25)'
          },
          '50%': {
            boxShadow: '0 0 36px rgba(255, 255, 255, 0.28), 0 0 0 1px rgba(255, 255, 255, 0.55) inset',
            borderColor: 'rgba(255, 255, 255, 0.5)'
          }
        },
        borderSpin: {
          '100%': {
            transform: 'rotate(360deg)',
          }
        }
      }
    },
  },
  plugins: [],
}
