import {
  WebSocketMessage,
  StepRunStarted,
  StepRunFinished,
  ExecutionFinished,
  TestReportMessage,
  ErrorMessage,
  TestRunStarted,
  TestRunFinished,
} from "@maestro/types/message/WebSocketMessage";
import { TestSuiteExecution, ExecutionStatus } from "@maestro/types/execution/TestSuiteExecution";
import { TestReport } from "@maestro/types/report/TestReport";
import { StepResult } from "@maestro/types/report/Result";

// TODO: see if this can be more generic and used by SimulationPortal as well
// Process a WebSocket message and return state updates for TestSuiteExecution
export function handleMessage(message: WebSocketMessage, currentExecution: TestSuiteExecution | null): Partial<TestSuiteExecution> | null {
  switch (message.type) {
    case "TEST_RUN_STARTED":
      return handleTestRunStarted(message, currentExecution);

    case "STEP_RUN_STARTED":
      return handleStepRunStarted(message, currentExecution);

    case "STEP_RUN_FINISHED":
      return handleStepRunFinished(message, currentExecution);

    case "TEST_RUN_FINISHED":
      return handleTestRunFinished(message, currentExecution);

    case "EXECUTION_FINISHED":
      return handleExecutionFinished(message, currentExecution);

    case "TEST_REPORT":
      return handleTestReport(message);

    case "error":
      return handleError(message, currentExecution);

    case "log":
    case "message":
      // These are handled by logging logic, no state update needed
      return null;

    case "INTERACT_WITH_USER":
    case "USER_INTERACTION_COMPLETED":
      // These are handled separately by context/hook
      return null;

    default:
      console.warn("Unhandled message type:", message.type);
      return null;
  }
}

// Extract a log message from a WebSocket message
export function extractLogMessage(message: WebSocketMessage): string | Record<string, unknown> | null {
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
      return message.data?.message || JSON.stringify(message.data);
    }

    default:
      return { ...message, dateTime: new Date().toISOString() };
  }
}

function handleTestRunStarted(message: TestRunStarted, currentExecution: TestSuiteExecution | null): Partial<TestSuiteExecution> | null {
  if (!currentExecution) return null;

  const tests = currentExecution.tests?.map((test) => {
    if (test.testId === message.testId) {
      return {
        ...test,
        executionStatus: ExecutionStatus.RUNNING,
        startDateTime: message.dateTime,
      };
    }
    return test;
  });

  return { tests };
}

function handleStepRunStarted(message: StepRunStarted, currentExecution: TestSuiteExecution | null): Partial<TestSuiteExecution> | null {
  if (!currentExecution) return null;

  const tests = currentExecution.tests?.map((test) => {
    if (test.testId === message.testId) {
      const stepRuns = test.stepRuns.map((step, index) => {
        if (index === message.stepIndex) {
          return {
            ...step,
            executionStatus: ExecutionStatus.RUNNING,
            startDateTime: message.dateTime,
          };
        }
        return step;
      });
      return { ...test, stepRuns };
    }
    return test;
  });

  return { tests };
}

function handleStepRunFinished(message: StepRunFinished, currentExecution: TestSuiteExecution | null): Partial<TestSuiteExecution> | null {
  if (!currentExecution) return null;

  const tests = currentExecution.tests?.map((test) => {
    if (test.testId === message.testId) {
      const stepRuns = test.stepRuns.map((step, index) => {
        if (index === message.stepIndex) {
          return {
            ...step,
            executionStatus: ExecutionStatus.FINISHED,
            finishDateTime: message.dateTime,
            result: message.result,
          };
        }
        return step;
      });
      return { ...test, stepRuns };
    }
    return test;
  });

  return {
    tests,
    unexpectedErrors: message.unexpectedErrors,
  };
}

function handleTestRunFinished(message: TestRunFinished, currentExecution: TestSuiteExecution | null): Partial<TestSuiteExecution> | null {
  if (!currentExecution) return null;

  const tests = currentExecution.tests?.map((test) => {
    if (test.testId === message.testId) {
      return {
        ...test,
        executionStatus: ExecutionStatus.FINISHED,
        finishDateTime: message.dateTime,
        result: message.result,
      };
    }
    return test;
  });

  return {
    tests,
    unexpectedErrors: message.unexpectedErrors,
  };
}

function handleExecutionFinished(message: ExecutionFinished, currentExecution: TestSuiteExecution | null): Partial<TestSuiteExecution> {
  // Update final test if isTestRun (single test execution)
  let tests = currentExecution?.tests;
  if (currentExecution?.tests?.length === 1) {
    const testRunReport = message.testReport.testRunReports?.[0];
    if (testRunReport) {
      tests = [
        {
          ...currentExecution.tests[0],
          executionStatus: ExecutionStatus.FINISHED,
          result: testRunReport.result,
          finishDateTime: testRunReport.dateTime,
        },
      ];
    }
  }

  return {
    executionStatus: ExecutionStatus.FINISHED,
    result: message.testReport.result,
    finishDateTime: message.testReport.dateTime,
    unexpectedErrors: message.testReport.unexpectedErrors,
    tests,
  };
}

function handleTestReport(message: TestReportMessage): Partial<TestSuiteExecution> | null {
  try {
    const testReport = JSON.parse(message.content) as TestReport;

    return {
      executionStatus: ExecutionStatus.FINISHED,
      result: testReport.result,
      finishDateTime: testReport.dateTime || new Date().toISOString(),
    };
  } catch (error) {
    console.error("Error parsing TEST_REPORT content:", error);
    return null;
  }
}

function handleError(message: ErrorMessage, currentExecution: TestSuiteExecution | null): Partial<TestSuiteExecution> | null {
  if (!currentExecution?.tests?.[0] || message.stepIndex === undefined) return null;

  const test = currentExecution.tests[0];
  const stepRuns = [...test.stepRuns];

  if (stepRuns[message.stepIndex]) {
    stepRuns[message.stepIndex] = {
      ...stepRuns[message.stepIndex],
      executionStatus: ExecutionStatus.FINISHED,
      result: "FAILED" as StepResult,
    };
  }

  return {
    tests: [{ ...test, stepRuns }, ...currentExecution.tests.slice(1)],
  };
}
