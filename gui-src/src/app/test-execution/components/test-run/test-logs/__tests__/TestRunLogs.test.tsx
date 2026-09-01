/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import TestRunLogs from "../TestRunLogs";
import { LogEntry } from "@/app/test-execution/types/TestRunExecution";
import { TestStep as TestModelStep } from "@/app/test-execution/types/TestModel";

vi.mock("lucide-react", () => ({
  Terminal: () => <svg data-testid="terminal-icon" />,
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  CollapsableSubCard: (props: { children: React.ReactNode; title: string; className?: string }) => (
    <div data-testid="collapsable-sub-card" className={props.className}>
      <div data-testid="card-title">{props.title}</div>
      {props.children}
    </div>
  ),
}));

vi.mock("../TestLogEntry", () => ({
  default: (props: { log: LogEntry; testSteps?: TestModelStep[] }) => (
    <div data-testid="test-log-entry" data-has-steps={!!props.testSteps}>
      {typeof props.log === "string" ? props.log : (props.log as any).type || "log"}
    </div>
  ),
}));

describe("TestRunLogs", () => {
  const mockScrollIntoView = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    Element.prototype.scrollIntoView = mockScrollIntoView;
  });

  describe("Empty logs", () => {
    it("should render empty state when no logs provided", () => {
      render(<TestRunLogs isRunning={false} />);
      expect(screen.getByTestId("terminal-icon")).toBeInTheDocument();
      expect(screen.getByText("No logs available")).toBeInTheDocument();
    });

    it("should render empty state when logs array is empty", () => {
      render(<TestRunLogs logs={[]} isRunning={false} />);
      expect(screen.getByTestId("terminal-icon")).toBeInTheDocument();
      expect(screen.getByText("No logs available")).toBeInTheDocument();
    });

    it("should not render CollapsableSubCard when no logs", () => {
      render(<TestRunLogs logs={[]} isRunning={false} />);
      expect(screen.queryByTestId("collapsable-sub-card")).not.toBeInTheDocument();
    });
  });

  describe("Log rendering", () => {
    it("should render logs in CollapsableSubCard", () => {
      const logs: LogEntry[] = ["PASSED", "FAILED"];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      expect(screen.getByTestId("collapsable-sub-card")).toBeInTheDocument();
      expect(screen.getByTestId("card-title")).toHaveTextContent("Execution logs");
    });

    it("should render all log entries", () => {
      const logs: LogEntry[] = ["PASSED", "FAILED", "RUNNING"];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      const logEntries = screen.getAllByTestId("test-log-entry");
      expect(logEntries).toHaveLength(3);
    });

    it("should pass testSteps to each log entry", () => {
      const logs: LogEntry[] = [{ type: "STEP_RUN_STARTED", stepIndex: 0 } as any];
      const testSteps: TestModelStep[] = [
        {
          name: "Step 1",
          type: "ACTION",
          description: "",
        },
      ];
      render(<TestRunLogs logs={logs} isRunning={false} testSteps={testSteps} />);
      const logEntry = screen.getByTestId("test-log-entry");
      expect(logEntry).toHaveAttribute("data-has-steps", "true");
    });

    it("should render mixed log types", () => {
      const logs: LogEntry[] = ["PASSED", { type: "STEP_RUN_STARTED", stepIndex: 0 } as any, { type: "INFO", message: "Info log" } as any];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      const logEntries = screen.getAllByTestId("test-log-entry");
      expect(logEntries).toHaveLength(3);
    });
  });

  describe("Running indicator", () => {
    it("should display running indicator when isRunning is true", () => {
      const logs: LogEntry[] = ["RUNNING"];
      render(<TestRunLogs logs={logs} isRunning={true} />);
      expect(screen.getByText("Running")).toBeInTheDocument();
    });

    it("should not display running indicator when isRunning is false", () => {
      const logs: LogEntry[] = ["PASSED"];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      expect(screen.queryByText("Running")).not.toBeInTheDocument();
    });

    it("should have pulse animation on running indicator", () => {
      const logs: LogEntry[] = ["RUNNING"];
      const { container } = render(<TestRunLogs logs={logs} isRunning={true} />);
      const pulseElement = container.querySelector(".animate-pulse");
      expect(pulseElement).toBeInTheDocument();
    });
  });

  describe("Auto-scroll functionality", () => {
    it("should auto-scroll when logs change", async () => {
      const { rerender } = render(<TestRunLogs logs={["PASSED"]} isRunning={true} />);

      rerender(<TestRunLogs logs={["PASSED", "FAILED"]} isRunning={true} />);

      await waitFor(() => {
        expect(mockScrollIntoView).toHaveBeenCalled();
      });
    });

    it("should scroll with smooth behavior", async () => {
      const { rerender } = render(<TestRunLogs logs={["PASSED"]} isRunning={true} />);

      rerender(<TestRunLogs logs={["PASSED", "RUNNING"]} isRunning={true} />);

      await waitFor(() => {
        expect(mockScrollIntoView).toHaveBeenCalledWith({ behavior: "smooth" });
      });
    });

    it("should handle initial render without scrolling error", () => {
      render(<TestRunLogs logs={["PASSED"]} isRunning={false} />);
      // No error should be thrown
    });
  });

  describe("Key generation", () => {
    it("should use dateTime for structured logs in key", () => {
      const logs: LogEntry[] = [
        { type: "INFO", dateTime: "2026-04-20T10:00:00Z", message: "Log 1" } as any,
        { type: "INFO", dateTime: "2026-04-20T10:01:00Z", message: "Log 2" } as any,
      ];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      const logEntries = screen.getAllByTestId("test-log-entry");
      expect(logEntries).toHaveLength(2);
    });

    it("should use index for plain string logs in key", () => {
      const logs: LogEntry[] = ["PASSED", "FAILED", "RUNNING"];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      const logEntries = screen.getAllByTestId("test-log-entry");
      expect(logEntries).toHaveLength(3);
    });

    it("should handle duplicate dateTime values", () => {
      const logs: LogEntry[] = [
        { type: "INFO", dateTime: "2026-04-20T10:00:00Z", message: "Log 1" } as any,
        { type: "INFO", dateTime: "2026-04-20T10:00:00Z", message: "Log 2" } as any,
      ];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      const logEntries = screen.getAllByTestId("test-log-entry");
      expect(logEntries).toHaveLength(2);
    });
  });

  describe("Styling and layout", () => {
    it("should apply correct container classes", () => {
      const logs: LogEntry[] = ["PASSED"];
      const { container } = render(<TestRunLogs logs={logs} isRunning={false} />);
      const logContainer = container.querySelector(".max-h-64.overflow-y-auto.p-4.text-sm.space-y-1");
      expect(logContainer).toBeInTheDocument();
    });

    it("should apply background color to CollapsableSubCard", () => {
      const logs: LogEntry[] = ["PASSED"];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      const card = screen.getByTestId("collapsable-sub-card");
      expect(card).toHaveClass("bg-lightgrey", "rounded-md");
    });

    it("should apply running indicator styles", () => {
      const logs: LogEntry[] = ["RUNNING"];
      const { container } = render(<TestRunLogs logs={logs} isRunning={true} />);
      const indicator = container.querySelector(".text-blue");
      expect(indicator).toBeInTheDocument();
    });
  });

  describe("Edge cases", () => {
    it("should handle large number of logs", () => {
      const logs: LogEntry[] = Array.from({ length: 1000 }, (_, i) => ({ type: `LOG_${i}`, message: `Log ${i}` }));
      render(<TestRunLogs logs={logs} isRunning={false} />);
      const logEntries = screen.getAllByTestId("test-log-entry");
      expect(logEntries).toHaveLength(1000);
    });

    it("should handle logs with null dateTime", () => {
      const logs: LogEntry[] = [{ type: "INFO", dateTime: null, message: "Log" } as any];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      expect(screen.getByTestId("test-log-entry")).toBeInTheDocument();
    });

    it("should handle logs with missing type", () => {
      const logs: LogEntry[] = [{ message: "Log without type" } as any];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      expect(screen.getByTestId("test-log-entry")).toBeInTheDocument();
    });

    it("should not crash with undefined testSteps", () => {
      const logs: LogEntry[] = [{ type: "STEP_RUN_STARTED", stepIndex: 0 } as any];
      render(<TestRunLogs logs={logs} isRunning={false} />);
      expect(screen.getByTestId("test-log-entry")).toBeInTheDocument();
    });

    it("should handle empty testSteps array", () => {
      const logs: LogEntry[] = [{ type: "STEP_RUN_STARTED", stepIndex: 0 } as any];
      render(<TestRunLogs logs={logs} isRunning={false} testSteps={[]} />);
      expect(screen.getByTestId("test-log-entry")).toBeInTheDocument();
    });
  });

  describe("Props handling", () => {
    it("should use default empty array for logs when undefined", () => {
      render(<TestRunLogs isRunning={false} />);
      expect(screen.getByText("No logs available")).toBeInTheDocument();
    });

    it("should maintain testSteps reference across renders", () => {
      const logs: LogEntry[] = ["PASSED"];
      const testSteps: TestModelStep[] = [
        {
          name: "Step",
          type: "ACTION",
          description: "",
        },
      ];
      const { rerender } = render(<TestRunLogs logs={logs} isRunning={false} testSteps={testSteps} />);

      rerender(<TestRunLogs logs={[...logs, "FAILED"]} isRunning={false} testSteps={testSteps} />);

      const logEntries = screen.getAllByTestId("test-log-entry");
      expect(logEntries).toHaveLength(2);
    });
  });
});
