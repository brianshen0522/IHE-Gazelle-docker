import {
  TestRunExecution,
  TestStepStatus,
  TestRunStatus,
  TestReport,
  TestReportSummaryResult
} from "../types/TestRunExecution";
import {
  WebSocketMessage,
  StepRunStarted,
  StepRunFinished,
  ExecutionFinished,
  TestReportMessage,
  ErrorMessage,
} from "@maestro/types/message/WebSocketMessage";

/**
 * Message handler that processes WebSocket messages and updates test execution state
 */
export class TestExecutionMessageHandler {
  /**
   * Process a WebSocket message and return state updates
   */
  static handleMessage(message: WebSocketMessage, currentExecution: TestRunExecution | null): Partial<TestRunExecution> | null {
    switch (message.type) {
      case "STEP_RUN_STARTED":
        return this.handleStepRunStarted(message, currentExecution);

      case "STEP_RUN_FINISHED":
        return this.handleStepRunFinished(message, currentExecution);

      case "EXECUTION_FINISHED":
        return this.handleExecutionFinished(message);

      case "TEST_REPORT":
        return this.handleTestReport(message);

      case "error":
        return this.handleError(message, currentExecution);

      case "log":
      case "message":
        // These are handled by logging logic, no state update needed
        return null;

      default:
        // Unknown message type - log it but don't update state
        console.warn("Unhandled message type:", message.type);
        return null;
    }
  }

  /**
   * Extract log message from WebSocket message
   */
  static extractLogMessage(message: WebSocketMessage): string | Record<string, unknown> | null {
    switch (message.type) {
      case "STEP_RUN_STARTED":
      case "STEP_RUN_FINISHED":
        return { ...message, dateTime: message.dateTime || new Date().toISOString() };

      case "EXECUTION_FINISHED":
        return { type: message.type, dateTime: message.testReport.dateTime };

      case "TEST_REPORT": {
        try {
          const testReport = JSON.parse(message.content) as TestReport;
          return {
            type: "TEST_REPORT",
            result: testReport.result?.toUpperCase(),
            reportId: message.id,
            dateTime: new Date().toISOString(),
          };
        } catch {
          return "Received test report";
        }
      }

      case "error": {
        const errorMessage = message.data?.message;
        return `Error: ${errorMessage || "Unknown error"}`;
      }

      case "log":
      case "message": {
        const data = message.data;
        return data?.message || JSON.stringify(data);
      }

      default:
        return { ...message, dateTime: new Date().toISOString() };
    }
  }

  private static handleStepRunStarted(message: StepRunStarted, currentExecution: TestRunExecution | null): Partial<TestRunExecution> | null {
    if (!currentExecution) return null;

    // Track that a step is running
    return {
      liveStepResults: [
        ...(currentExecution.liveStepResults || []),
        {
          stepName: "",
          type: "",
          dateTime: message.dateTime || new Date().toISOString(),
          result: "RUNNING",
        },
      ],
    };
  }

  private static handleStepRunFinished(message: StepRunFinished, currentExecution: TestRunExecution | null): Partial<TestRunExecution> | null {
    if (!currentExecution) return null;

    const result = message.result;

    let stepStatus: TestStepStatus = "UNKNOWN";
    if (result === "PASSED") stepStatus = "PASSED";
    else if (result === "FAILED") stepStatus = "FAILED";
    else if (result === "UNDEFINED") stepStatus = "UNDEFINED";
    else if (result === "DONE") stepStatus = "COMPLETED";

    // Update the last RUNNING step to finished
    const liveStepResults = [...(currentExecution.liveStepResults || [])];
    const runningIndex = liveStepResults.findIndex((step) => step.result === "RUNNING");

    if (runningIndex >= 0) {
      liveStepResults[runningIndex] = {
        ...liveStepResults[runningIndex],
        dateTime: message.dateTime || new Date().toISOString(),
        result: stepStatus,
      };
    }

    return { liveStepResults };
  }

  private static handleExecutionFinished(message: ExecutionFinished): Partial<TestRunExecution> {
    const testReport = message.testReport;
    const location = message.location;

    let finalStatus: TestRunStatus = "PASSED";
    if (testReport) {
      const result = testReport.result?.toUpperCase();
      if (result === "FAILED")
        finalStatus = "FAILED";
      if (result === "UNDEFINED")
        finalStatus = "UNDEFINED";
    }

    // Create a new test report summary for the execution history
    const newReportSummary = location && testReport ? {
      id: location,
      location: location,
      result: testReport.result?.toUpperCase() as TestReportSummaryResult || "UNDEFINED" as TestReportSummaryResult,
      dateTime: testReport.dateTime || new Date().toISOString(),
    } : undefined;

    return {
      status: finalStatus,
      completedAt: new Date().toISOString(),
      testReport: testReport as unknown as TestReport,
      testReportUrl: location,
      testReportSummaries: newReportSummary ? [newReportSummary] : undefined,
      liveStepResults: undefined, // Clear live data after final report
    };
  }

  private static handleTestReport(message: TestReportMessage): Partial<TestRunExecution> | null {
    try {
      const testReport = JSON.parse(message.content) as TestReport;

      const testReportResult = testReport.result?.toUpperCase();
      let finalStatus: TestRunStatus = "PASSED";
      if (testReportResult === "FAILED" || testReportResult === "UNDEFINED") {
        finalStatus = "FAILED";
      }

      return {
        status: finalStatus,
        completedAt: testReport.dateTime || new Date().toISOString(),
        testReport: testReport as unknown as TestReport,
        testReportUrl: message.id,
        liveStepResults: undefined, // Clear live data after final report
      };
    } catch (error) {
      console.error("Error parsing TEST_REPORT content:", error);
      return null;
    }
  }

  private static handleError(message: ErrorMessage, currentExecution: TestRunExecution | null): Partial<TestRunExecution> | null {
    if (!currentExecution) return null;

    const stepIndex = message.stepIndex;
    if (stepIndex === undefined) return null;

    const updatedResults = [...(currentExecution.results || [])];
    if (updatedResults[stepIndex]) {
      updatedResults[stepIndex].status = "FAILED";
    }

    return { results: updatedResults };
  }
}
