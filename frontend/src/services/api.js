import { API_ROUTES } from '../constants';

/**
 * API service for communicating with the Spring Boot backend.
 * Implements AbortController cancellation and sequence-based stale request protection.
 */

let activeAbortController = null;
let currentRequestId = 0;

export async function generateStudySet({ prompt, questionCount }) {
  // Cancel any existing in-flight request
  if (activeAbortController) {
    activeAbortController.abort('New study request initiated');
  }

  const controller = new AbortController();
  activeAbortController = controller;
  const requestId = ++currentRequestId;

  try {
    const payload = {
      prompt: (prompt || '').trim(),
      questionCount: Number(questionCount) || 5,
    };

    const response = await fetch(API_ROUTES.STUDY, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
      signal: controller.signal,
    });

    // If a newer request was dispatched while this was in-flight, discard this response
    if (requestId !== currentRequestId) {
      throw new Error('STALE_REQUEST');
    }

    const data = await response.json();

    if (!response.ok) {
      const errorCode = data?.error?.code || 'UNKNOWN_ERROR';
      const errorMessage = data?.error?.message || 'Failed to generate study set.';
      const details = data?.error?.details || [];
      const error = new Error(errorMessage);
      error.code = errorCode;
      error.details = details;
      throw error;
    }

    return data;
  } catch (err) {
    if (err.name === 'AbortError' || err.message === 'STALE_REQUEST') {
      const abortErr = new Error('Request was canceled or superseded.');
      abortErr.isAborted = true;
      throw abortErr;
    }
    throw err;
  } finally {
    if (activeAbortController === controller) {
      activeAbortController = null;
    }
  }
}

export function cancelActiveRequest() {
  if (activeAbortController) {
    activeAbortController.abort('User canceled request');
    activeAbortController = null;
  }
}

export async function checkBackendHealth() {
  try {
    const res = await fetch(API_ROUTES.HEALTH);
    if (res.ok) {
      return await res.json();
    }
    return { status: 'DOWN' };
  } catch {
    return { status: 'UNREACHABLE' };
  }
}
