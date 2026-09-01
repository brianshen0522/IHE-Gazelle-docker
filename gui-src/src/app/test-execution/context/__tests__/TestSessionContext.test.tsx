import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { TestSessionProvider, useTestSession } from "../TestSessionContext";

describe("TestSessionContext", () => {
  describe("useTestSession", () => {
    it("throws error when used outside provider", () => {
      expect(() => {
        renderHook(() => useTestSession());
      }).toThrow("useTestSession must be used within a TestSessionProvider");
    });

    it("provides initial null values", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      expect(result.current.testSessionId).toBeNull();
      expect(result.current.testId).toBeNull();
    });

    it("updates testSessionId", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      act(() => {
        result.current.setTestSessionId("session-123");
      });

      expect(result.current.testSessionId).toBe("session-123");
    });

    it("updates testId", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      act(() => {
        result.current.setTestId("test-456");
      });

      expect(result.current.testId).toBe("test-456");
    });

    it("updates both values using setTestSessionData", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      act(() => {
        result.current.setTestSessionData({
          testSessionId: "session-abc",
          testId: "test-xyz",
        });
      });

      expect(result.current.testSessionId).toBe("session-abc");
      expect(result.current.testId).toBe("test-xyz");
    });

    it("updates only testSessionId when testId is undefined", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      act(() => {
        result.current.setTestId("initial-test");
        result.current.setTestSessionData({
          testSessionId: "session-123",
          testId: "initial-test",
        });
      });

      expect(result.current.testSessionId).toBe("session-123");
      expect(result.current.testId).toBe("initial-test");
    });

    it("updates only testId when testSessionId is undefined", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      act(() => {
        result.current.setTestSessionId("initial-session");
        result.current.setTestSessionData({
          testId: "test-456",
          testSessionId: "initial-session",
        });
      });

      expect(result.current.testSessionId).toBe("initial-session");
      expect(result.current.testId).toBe("test-456");
    });

    it("clears all values using clearTestSession", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      act(() => {
        result.current.setTestSessionData({
          testSessionId: "session-123",
          testId: "test-456",
        });
      });

      expect(result.current.testSessionId).toBe("session-123");
      expect(result.current.testId).toBe("test-456");

      act(() => {
        result.current.clearTestSession();
      });

      expect(result.current.testSessionId).toBeNull();
      expect(result.current.testId).toBeNull();
    });

    it("accepts null values in setters", () => {
      const { result } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      act(() => {
        result.current.setTestSessionId("session-123");
        result.current.setTestId("test-456");
      });

      act(() => {
        result.current.setTestSessionId(null);
        result.current.setTestId(null);
      });

      expect(result.current.testSessionId).toBeNull();
      expect(result.current.testId).toBeNull();
    });

    it("maintains referential stability for callbacks", () => {
      const { result, rerender } = renderHook(() => useTestSession(), {
        wrapper: TestSessionProvider,
      });

      const initialSetTestSessionData = result.current.setTestSessionData;
      const initialClearTestSession = result.current.clearTestSession;

      act(() => {
        result.current.setTestSessionId("session-123");
      });

      rerender();

      expect(result.current.setTestSessionData).toBe(initialSetTestSessionData);
      expect(result.current.clearTestSession).toBe(initialClearTestSession);
    });
  });
});
