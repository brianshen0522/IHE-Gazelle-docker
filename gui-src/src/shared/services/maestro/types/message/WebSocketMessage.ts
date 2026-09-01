import { TestSuiteRun, TestRun, SupportedInput, Property } from "@maestro/types/execution/TestSuiteRun";
import { StepResult, TestResult } from "@maestro/types/report/Result";
import { UnexpectedError } from "@maestro/types/report/UnexpectedError";
import { TestReport } from "@maestro/types/report/TestReport";

export type WebSocketMessageType =
  // Client-initiated messages
  | "START_TEST_SUITE_RUN"
  | "START_TEST_RUN"
  | "USER_INTERACTION_COMPLETED"
  // Server-sent execution lifecycle messages
  | "TEST_RUN_STARTED"
  | "STEP_RUN_STARTED"
  | "STEP_RUN_FINISHED"
  | "TEST_RUN_FINISHED"
  | "EXECUTION_FINISHED"
  // User interaction
  | "INTERACT_WITH_USER"
  // Reporting and logging
  | "TEST_REPORT"
  | "error"
  | "log"
  | "message";

export type WebSocketMessage =
  | StartTestSuiteRun
  | StartTestRun
  | UserInteractionCompleted
  | TestRunStarted
  | StepRunStarted
  | StepRunFinished
  | TestRunFinished
  | ExecutionFinished
  | InteractWithUser
  | TestReportMessage
  | ErrorMessage
  | LogMessage
  | InfoMessage;

export interface StartTestSuiteRun {
  type: "START_TEST_SUITE_RUN";
  testSuiteRun: TestSuiteRun;
  persist: boolean;
  authorization?: string;
}

export interface StartTestRun {
  type: "START_TEST_RUN";
  testRun: TestRun;
  persist: boolean;
  authorization?: string;
}

export interface TestRunStarted {
  type: "TEST_RUN_STARTED";
  testId: string;
  dateTime?: string;
}

export interface StepRunStarted {
  type: "STEP_RUN_STARTED";
  testId: string;
  stepIndex: number;
  dateTime?: string;
}

export interface InteractWithUser {
  type: "INTERACT_WITH_USER";
  interactionTitle: string;
  message: string;
  timeout: number;
  supportedInputs?: SupportedInput[];
}

export interface UserInteractionCompleted {
  type: "USER_INTERACTION_COMPLETED";
  inputs?: Property[];
  authorization?: string;
}

export interface StepRunFinished {
  type: "STEP_RUN_FINISHED";
  testId: string;
  stepIndex: number;
  dateTime: string;
  result: StepResult;
  unexpectedErrors: UnexpectedError[];
}

export interface TestRunFinished {
  type: "TEST_RUN_FINISHED";
  testId: string;
  dateTime: string;
  result: TestResult;
  unexpectedErrors: UnexpectedError[];
}

export interface ExecutionFinished {
  type: "EXECUTION_FINISHED";
  testReport: TestReport;
  location?: string;
}

// Reporting and logging message types
export interface TestReportMessage {
  type: "TEST_REPORT";
  id: string;
  content: string; // JSON string of TestReport
}

export interface ErrorMessage {
  type: "error";
  stepIndex?: number;
  data?: {
    message?: string;
  };
}

export interface LogMessage {
  type: "log";
  data?: {
    message?: string;
    [key: string]: unknown;
  };
}

export interface InfoMessage {
  type: "message";
  data?: {
    message?: string;
    [key: string]: unknown;
  };
}
