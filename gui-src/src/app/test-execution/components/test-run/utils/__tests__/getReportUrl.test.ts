import { describe, it, expect } from "vitest";
import { getReportUrl, getTestReportUrl } from "../getReportUrl";
import { TestRunExecution } from "@/app/test-execution/types/TestRunExecution";

describe("getReportUrl", () => {
  describe("when testReportUrl is not provided", () => {
    it("should return null", () => {
      const result = getReportUrl({ testReportUrl: undefined });
      expect(result).toBeNull();
    });
  });

  describe("when testReportUrl contains a valid report ID", () => {
    it("should extract report ID and return test-execution URL", () => {
      const testReportUrl = "http://api.example.com/items/report-123";
      const result = getReportUrl({ testReportUrl });

      expect(result).toBe("/test-execution/reports/report-123");
    });

    it("should include testSessionId as query parameter when provided", () => {
      const testReportUrl = "http://api.example.com/items/report-123";
      const testSessionId = "session-456";
      const result = getReportUrl({ testReportUrl, testSessionId });

      expect(result).toBe("/test-execution/reports/report-123?testSessionId=session-456");
    });

    it("should return validation-portal URL when kindOfReport is VALIDATION_REPORT", () => {
      const testReportUrl = "http://api.example.com/items/validation-report-789";
      const result = getReportUrl({ testReportUrl, kindOfReport: "VALIDATION_REPORT" });

      expect(result).toBe("/validation-portal/reports/validation-report-789");
    });

    it("should return simulation-portal URL when kindOfReport is SIMULATION_REPORT", () => {
      const testReportUrl = "http://api.example.com/items/simulation-report-101";
      const result = getReportUrl({ testReportUrl, kindOfReport: "SIMULATION_REPORT" });

      expect(result).toBe("/simulation-portal/reports/simulation-report-101");
    });

    it("should include testSessionId with validation report", () => {
      const testReportUrl = "http://api.example.com/items/validation-report-789";
      const testSessionId = "session-999";
      const result = getReportUrl({ testReportUrl, testSessionId, kindOfReport: "VALIDATION_REPORT" });

      expect(result).toBe("/validation-portal/reports/validation-report-789?testSessionId=session-999");
    });

    it("should include testSessionId with simulation report", () => {
      const testReportUrl = "http://api.example.com/items/simulation-report-101";
      const testSessionId = "session-888";
      const result = getReportUrl({ testReportUrl, testSessionId, kindOfReport: "SIMULATION_REPORT" });

      expect(result).toBe("/simulation-portal/reports/simulation-report-101?testSessionId=session-888");
    });

    it("should handle report ID with special characters", () => {
      const testReportUrl = "http://api.example.com/items/report-abc-123_xyz";
      const result = getReportUrl({ testReportUrl });

      expect(result).toBe("/test-execution/reports/report-abc-123_xyz");
    });

    it("should handle URLs with additional path segments after report ID", () => {
      const testReportUrl = "http://api.example.com/items/report-123/details";
      const result = getReportUrl({ testReportUrl });

      expect(result).toBe("/test-execution/reports/report-123");
    });
  });

  describe("when testReportUrl does not match expected pattern", () => {
    it("should return the original URL if no match found", () => {
      const testReportUrl = "http://api.example.com/invalid/url";
      const result = getReportUrl({ testReportUrl });

      expect(result).toBe(testReportUrl);
    });

    it("should return the original URL when items path is missing", () => {
      const testReportUrl = "http://api.example.com/reports/123";
      const result = getReportUrl({ testReportUrl });

      expect(result).toBe(testReportUrl);
    });
  });
});

describe("getTestReportUrl", () => {
  it("should return undefined when execution is undefined", () => {
    // @ts-expect-error - Testing undefined input
    const result = getTestReportUrl(undefined);
    expect(result).toBeUndefined();
  });

  it("should return testReportUrl when it exists", () => {
    const execution: TestRunExecution = {
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
      testReportUrl: "http://api.example.com/items/report-primary",
      testReportSummaries: [
        {
          id: "summary-1",
          result: "PASSED",
          dateTime: "2026-01-01T10:00:00.000Z",
          location: "http://api.example.com/items/report-fallback",
        },
      ],
    } as TestRunExecution;

    const result = getTestReportUrl(execution);
    expect(result).toBe("http://api.example.com/items/report-primary");
  });

  it("should return last summary location when testReportUrl is undefined", () => {
    const execution: TestRunExecution = {
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
      testReportSummaries: [
        {
          id: "summary-1",
          result: "PASSED",
          dateTime: "2026-01-01T10:00:00.000Z",
          location: "http://api.example.com/items/report-first",
        },
        {
          id: "summary-2",
          result: "FAILED",
          dateTime: "2026-01-02T11:00:00.000Z",
          location: "http://api.example.com/items/report-last",
        },
      ],
    } as TestRunExecution;

    const result = getTestReportUrl(execution);
    expect(result).toBe("http://api.example.com/items/report-last");
  });

  it("should return undefined when testReportUrl is not set and testReportSummaries is undefined", () => {
    const execution: TestRunExecution = {
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
    } as TestRunExecution;

    const result = getTestReportUrl(execution);
    expect(result).toBeUndefined();
  });

  it("should return undefined when testReportUrl is not set and testReportSummaries is empty", () => {
    const execution: TestRunExecution = {
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
      testReportSummaries: [],
    } as unknown as TestRunExecution;

    const result = getTestReportUrl(execution);
    expect(result).toBeUndefined();
  });

  it("should prefer testReportUrl over summary location even when both exist", () => {
    const execution: TestRunExecution = {
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
      testReportUrl: "http://api.example.com/items/report-preferred",
      testReportSummaries: [
        {
          id: "summary-1",
          result: "PASSED",
          dateTime: "2026-01-01T10:00:00.000Z",
          location: "http://api.example.com/items/report-alternative",
        },
      ],
    } as TestRunExecution;

    const result = getTestReportUrl(execution);
    expect(result).toBe("http://api.example.com/items/report-preferred");
  });
});
