/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import TestLogEntry from "../TestLogEntry";
import { LogEntry } from "@/app/test-execution/types/TestRunExecution";
import { TestStep as TestModelStep } from "@/app/test-execution/types/TestModel";

vi.mock("@/shared/services/maestro/utils/logFormatters", () => ({
  formatTimestamp: (dateTime: any) => {
    if (!dateTime) return "";
    return "12:00:00";
  },
  formatToString: (value: any) => {
    if (value === null || value === undefined) return "";
    return String(value);
  },
  isStructuredLog: (log: any) => {
    return typeof log === "object" && log !== null && "type" in log;
  },
}));

vi.mock("@/shared/services/maestro/utils/logStyles", () => ({
  ICON_MAP: {
    CHECK: () => React.createElement("svg", { "data-testid": "check-icon" }),
    WARNING: () => React.createElement("svg", { "data-testid": "warning-icon" }),
    ERROR: () => React.createElement("svg", { "data-testid": "error-icon" }),
  },
  getIconColor: (icon: any) => {
    if (icon === "CHECK") return "text-green";
    if (icon === "WARNING") return "text-yellow";
    if (icon === "ERROR") return "text-red";
    return "";
  },
  getBorderColor: (result: any) => {
    if (result === "PASSED") return "border-green";
    if (result === "FAILED") return "border-red";
    return "border-gray";
  },
  LogIconType: {},
}));

vi.mock("@/shared/components/report/ResultBadge", () => ({
  ResultBadge: (props: any) => React.createElement("div", { "data-testid": "result-badge" }, props.result),
  isValidResult: (result: any) => ["PASSED", "FAILED", "RUNNING"].includes(result),
}));

describe("TestLogEntry", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Plain log rendering", () => {
    it("should render plain text log", () => {
      const log: LogEntry = "PASSED";
      render(<TestLogEntry log={log} />);
      expect(screen.getByText("PASSED")).toBeInTheDocument();
    });

    it("should handle empty string log", () => {
      const log: LogEntry = { type: "EMPTY", message: "" };
      const { container } = render(<TestLogEntry log={log} />);
      expect(container).toBeInTheDocument();
    });

    it("should handle multiline plain log", () => {
      const log: LogEntry = { type: "MULTILINE", message: "Line 1\nLine 2\nLine 3" };
      const { container } = render(<TestLogEntry log={log} />);
      expect(container.textContent).toContain("Line 1");
    });
  });

  describe("Structured log rendering", () => {
    it("should render structured log with timestamp and message", () => {
      const log = {
        type: "INFO",
        dateTime: "2026-04-20T10:00:00Z",
        message: "Test message",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByText("[12:00:00]")).toBeInTheDocument();
      expect(screen.getByText("Test message")).toBeInTheDocument();
    });

    it("should render step run started log with step name", () => {
      const testSteps: TestModelStep[] = [
        {
          name: "Validation Step",
          type: "ACTION",
          description: "",
        },
        {
          name: "Second Step",
          type: "ASSERTION",
          description: "",
        },
      ];
      const log = {
        type: "STEP_RUN_STARTED",
        stepIndex: 0,
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} testSteps={testSteps} />);
      expect(screen.getByText("Validation Step started", { exact: false })).toBeInTheDocument();
    });

    it("should render step run finished log with step name and result", () => {
      const testSteps: TestModelStep[] = [
        {
          name: "Test Step",
          type: "ACTION",
          description: "",
        },
      ];
      const log = {
        type: "STEP_RUN_FINISHED",
        stepIndex: 0,
        dateTime: "2026-04-20T10:00:00Z",
        result: "PASSED",
      };
      render(<TestLogEntry log={log} testSteps={testSteps} />);
      expect(screen.getByText("Test Step finished", { exact: false })).toBeInTheDocument();
      expect(screen.getByTestId("result-badge")).toHaveTextContent("PASSED");
    });

    it("should render step run running log with step name", () => {
      const testSteps: TestModelStep[] = [
        {
          name: "Running Step",
          type: "ACTION",
          description: "",
        },
      ];
      const log = {
        type: "STEP_RUN_RUNNING",
        stepIndex: 0,
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} testSteps={testSteps} />);
      expect(screen.getByText("Running Step running", { exact: false })).toBeInTheDocument();
    });

    it("should fallback to testId when stepIndex not available", () => {
      const log = {
        type: "STEP_RUN_STARTED",
        testId: "test-123",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByText("test-123 started", { exact: false })).toBeInTheDocument();
    });

    it("should fallback to name when provided", () => {
      const log = {
        type: "STEP_RUN_FINISHED",
        name: "Custom Name",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByText("Custom Name finished", { exact: false })).toBeInTheDocument();
    });

    it("should match step by index correctly", () => {
      const testSteps: TestModelStep[] = [
        {
          name: "First",
          type: "ACTION",
          description: "",
        },
        {
          name: "Second",
          type: "ACTION",
          description: "",
        },
        {
          name: "Third",
          type: "ACTION",
          description: "",
        },
      ];
      const log = {
        type: "STEP_RUN_STARTED",
        stepIndex: 2,
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} testSteps={testSteps} />);
      expect(screen.getByText("Third started", { exact: false })).toBeInTheDocument();
    });

    it("should render icon when provided", () => {
      const log = {
        type: "INFO",
        icon: "CHECK",
        message: "Success",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByTestId("check-icon")).toBeInTheDocument();
    });

    it("should apply border color based on result", () => {
      const log = {
        type: "INFO",
        result: "PASSED",
        message: "Test passed",
        dateTime: "2026-04-20T10:00:00Z",
      };
      const { container } = render(<TestLogEntry log={log} />);
      const logContainer = container.querySelector(".border-green");
      expect(logContainer).toBeInTheDocument();
    });

    it("should render result badge for valid results", () => {
      const log = {
        type: "INFO",
        result: "FAILED",
        message: "Test failed",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByTestId("result-badge")).toHaveTextContent("FAILED");
    });

    it("should not render result badge for invalid results", () => {
      const log = {
        type: "INFO",
        result: "INVALID_RESULT",
        message: "Message",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.queryByTestId("result-badge")).not.toBeInTheDocument();
    });

    it("should handle missing dateTime", () => {
      const log = {
        type: "INFO",
        message: "Message without timestamp",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByText("Message without timestamp")).toBeInTheDocument();
      expect(screen.queryByText(/\[\d{2}:\d{2}:\d{2}\]/)).not.toBeInTheDocument();
    });

    it("should display message as fallback for display text", () => {
      const log = {
        type: "CUSTOM_TYPE",
        message: "Fallback message",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByText("Fallback message")).toBeInTheDocument();
    });

    it("should display type when no message or name available", () => {
      const log = {
        type: "SYSTEM_EVENT",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} />);
      expect(screen.getByText("SYSTEM_EVENT")).toBeInTheDocument();
    });

    it("should handle stepIndex out of bounds", () => {
      const testSteps: TestModelStep[] = [
        {
          name: "Only Step",
          type: "ACTION",
          description: "",
        },
      ];
      const log = {
        type: "STEP_RUN_STARTED",
        stepIndex: 5,
        testId: "fallback-id",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} testSteps={testSteps} />);
      expect(screen.getByText("fallback-id started", { exact: false })).toBeInTheDocument();
    });

    it("should handle empty testSteps array", () => {
      const log = {
        type: "STEP_RUN_FINISHED",
        stepIndex: 0,
        name: "Named Step",
        dateTime: "2026-04-20T10:00:00Z",
      };
      render(<TestLogEntry log={log} testSteps={[]} />);
      expect(screen.getByText("Named Step finished", { exact: false })).toBeInTheDocument();
    });
  });

  describe("Edge cases", () => {
    it("should handle null log gracefully", () => {
      const log = null;
      const { container } = render(<TestLogEntry log={log!} />);
      expect(container).toBeInTheDocument();
    });

    it("should handle undefined log gracefully", () => {
      const log = undefined;
      const { container } = render(<TestLogEntry log={log!} />);
      expect(container).toBeInTheDocument();
    });

    it("should handle log with all fields", () => {
      const testSteps: TestModelStep[] = [
        {
          name: "Complete Step",
          type: "ACTION",
          description: "",
        },
      ];
      const log = {
        type: "STEP_RUN_FINISHED",
        stepIndex: 0,
        name: "Explicit Name",
        testId: "test-456",
        dateTime: "2026-04-20T10:00:00Z",
        result: "PASSED",
        message: "Step completed",
        icon: "CHECK",
      };
      render(<TestLogEntry log={log} testSteps={testSteps} />);
      expect(screen.getByText("Complete Step finished", { exact: false })).toBeInTheDocument();
      expect(screen.getByTestId("check-icon")).toBeInTheDocument();
      expect(screen.getByTestId("result-badge")).toHaveTextContent("PASSED");
    });
  });
});
