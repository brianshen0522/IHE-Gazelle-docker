import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { useDebounce } from "../useDebounce";

describe("useDebounce", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it("should return the initial value immediately", () => {
    const { result } = renderHook(() => useDebounce("initial", 300));
    expect(result.current).toBe("initial");
  });

  it("should debounce value changes with default delay", () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: "initial" },
    });

    expect(result.current).toBe("initial");

    // Update the value
    rerender({ value: "updated" });
    expect(result.current).toBe("initial");

    // Fast-forward time by 299ms (not enough)
    act(() => {
      vi.advanceTimersByTime(299);
    });
    expect(result.current).toBe("initial");

    // Fast-forward the remaining time
    act(() => {
      vi.advanceTimersByTime(1);
    });
    expect(result.current).toBe("updated");
  });

  it("should debounce value changes with custom delay", () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 500), {
      initialProps: { value: "initial" },
    });

    rerender({ value: "updated" });
    expect(result.current).toBe("initial");

    act(() => {
      vi.advanceTimersByTime(499);
    });
    expect(result.current).toBe("initial");

    act(() => {
      vi.advanceTimersByTime(1);
    });
    expect(result.current).toBe("updated");
  });

  it("should only apply the last value after multiple rapid changes", () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: "initial" },
    });

    // Make multiple rapid changes
    rerender({ value: "change1" });
    act(() => {
      vi.advanceTimersByTime(100);
    });

    rerender({ value: "change2" });
    act(() => {
      vi.advanceTimersByTime(100);
    });

    rerender({ value: "change3" });
    act(() => {
      vi.advanceTimersByTime(100);
    });

    // Still showing initial value
    expect(result.current).toBe("initial");

    // After the full delay from the last change
    act(() => {
      vi.advanceTimersByTime(200);
    });
    expect(result.current).toBe("change3");
  });

  it("should work with different data types", () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: 123 },
    });

    expect(result.current).toBe(123);

    rerender({ value: 456 });
    act(() => {
      vi.advanceTimersByTime(300);
    });
    expect(result.current).toBe(456);
  });

  it("should work with objects", () => {
    const initialObject = { name: "John", age: 30 };
    const updatedObject = { name: "Jane", age: 25 };

    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: initialObject },
    });

    expect(result.current).toBe(initialObject);

    rerender({ value: updatedObject });
    act(() => {
      vi.advanceTimersByTime(300);
    });
    expect(result.current).toBe(updatedObject);
  });

  it("should cleanup timeout on unmount", () => {
    const clearTimeoutSpy = vi.spyOn(globalThis, "clearTimeout");
    const { unmount } = renderHook(() => useDebounce("value", 300));

    unmount();

    expect(clearTimeoutSpy).toHaveBeenCalled();
  });

  it("should handle delay updates correctly", () => {
    const { result, rerender } = renderHook(({ value, delay }) => useDebounce(value, delay), {
      initialProps: { value: "initial", delay: 300 },
    });

    // Change both value and delay
    rerender({ value: "updated", delay: 500 });

    act(() => {
      vi.advanceTimersByTime(300);
    });
    expect(result.current).toBe("initial");

    act(() => {
      vi.advanceTimersByTime(200);
    });
    expect(result.current).toBe("updated");
  });

  it("should reset timer when value changes before delay completes", () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: "initial" },
    });

    rerender({ value: "first" });
    act(() => {
      vi.advanceTimersByTime(200);
    });

    // Change again before the first delay completes
    rerender({ value: "second" });
    act(() => {
      vi.advanceTimersByTime(200);
    });

    // Should still be initial
    expect(result.current).toBe("initial");

    // Complete the second delay
    act(() => {
      vi.advanceTimersByTime(100);
    });
    expect(result.current).toBe("second");
  });
});
