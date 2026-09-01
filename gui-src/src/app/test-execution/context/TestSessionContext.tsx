"use client";
import React, { createContext, useContext, useMemo, useState, useCallback } from "react";

/**
 * Context for sharing test session data across test-suite and test-run-execution components
 */
export interface TestSessionContextProps {
  // Test session identifiers
  testSessionId: string | null;
  setTestSessionId: (testSessionId: string | null) => void;
  // Current test identifier
  testId: string | null;
  setTestId: (testId: string | null) => void;
  // Helper to set all values at once
  setTestSessionData: (data: { testSessionId: string | null; testId: string | null }) => void;
  // Helper to clear all values
  clearTestSession: () => void;
}

export const TestSessionContext = createContext<TestSessionContextProps | null>(null);

/**
 * Provider component for TestSessionContext
 */
export function TestSessionProvider({ children }: Readonly<{ children: React.ReactNode }>) {
  const [testSessionId, setTestSessionId] = useState<string | null>(null);
  const [testId, setTestId] = useState<string | null>(null);

  const setTestSessionData = useCallback((data: { testSessionId: string | null; testId: string | null }) => {
    if (data.testSessionId !== undefined) setTestSessionId(data.testSessionId);
    if (data.testId !== undefined) setTestId(data.testId);
  }, []);

  const clearTestSession = useCallback(() => {
    setTestSessionId(null);
    setTestId(null);
  }, []);

  const value = useMemo(
    () => ({
      testSessionId,
      setTestSessionId,
      testId,
      setTestId,
      setTestSessionData,
      clearTestSession,
    }),
    [testSessionId, testId, setTestSessionData, clearTestSession],
  );

  return <TestSessionContext.Provider value={value}>{children}</TestSessionContext.Provider>;
}

/**
 * Hook to access TestSessionContext
 * @throws Error if used outside TestSessionProvider
 */
export function useTestSession(): TestSessionContextProps {
  const context = useContext(TestSessionContext);
  if (!context) {
    throw new Error("useTestSession must be used within a TestSessionProvider");
  }
  return context;
}
