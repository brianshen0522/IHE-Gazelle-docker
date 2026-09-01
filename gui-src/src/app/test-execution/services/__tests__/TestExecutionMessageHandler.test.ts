/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect } from "vitest";
import { TestExecutionMessageHandler } from "../TestExecutionMessageHandler";

describe("TestExecutionMessageHandler", () => {
  describe("handleMessage", () => {
    it("should return null for unknown message types", () => {
      const message = { type: "UNKNOWN_TYPE" } as any;
      const result = TestExecutionMessageHandler.handleMessage(message, null);
      expect(result).toBeNull();
    });

    it("should add RUNNING step on STEP_RUN_STARTED", () => {
      const message = { type: "STEP_RUN_STARTED" as const, testId: "test-1", stepIndex: 0, dateTime: "2026-04-14T10:00:00Z" };
      const execution = { liveStepResults: [] } as any;

      const result = TestExecutionMessageHandler.handleMessage(message, execution);

      expect(result?.liveStepResults).toHaveLength(1);
      expect(result?.liveStepResults?.[0].result).toBe("RUNNING");
    });

    it("should update RUNNING step on STEP_RUN_FINISHED", () => {
      const message = {
        type: "STEP_RUN_FINISHED" as const,
        testId: "test-1",
        stepIndex: 0,
        result: "PASSED" as const,
        dateTime: "2026-04-14T10:01:00Z",
        unexpectedErrors: [],
      };
      const execution = {
        liveStepResults: [
          {
            stepName: "",
            type: "",
            dateTime: "2026-04-14T10:00:00Z",
            result: "RUNNING",
          },
        ],
      } as any;

      const result = TestExecutionMessageHandler.handleMessage(message, execution);

      expect(result?.liveStepResults?.[0].result).toBe("PASSED");
    });

    it("should clear liveStepResults on TEST_REPORT", () => {
      const testReport = { testId: "test-1", result: "PASSED", dateTime: "2026-04-14T10:05:00Z" };
      const message = { type: "TEST_REPORT" as const, id: "report-1", content: JSON.stringify(testReport) };

      const result = TestExecutionMessageHandler.handleMessage(message, null);

      expect(result?.liveStepResults).toBeUndefined();
      expect(result?.testReport).toEqual(testReport);
    });
  });

  describe("extractLogMessage", () => {
    it("should extract log from step-update", () => {
      const message = { type: "step-update", stepIndex: 0, status: "RUNNING" } as any;
      const log = TestExecutionMessageHandler.extractLogMessage(message);
      expect(log).toMatchObject({
        status: "RUNNING",
        stepIndex: 0,
        type: "step-update",
        dateTime: expect.any(String),
      });
    });

    it("should extract log from execution-start", () => {
      const message = { type: "execution-start" } as any;
      const log = TestExecutionMessageHandler.extractLogMessage(message);
      expect(log).toMatchObject({
        type: "execution-start",
        dateTime: expect.any(String),
      });
    });
  });
});
