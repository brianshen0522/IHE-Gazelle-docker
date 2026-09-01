import { StepResult, TestResult } from "@maestro/types/report/Result";
import { UnexpectedError } from "@maestro/types/report/UnexpectedError";

export interface TestSuiteExecution {
  testSuiteRunId: string;
  startDateTime: string;
  finishDateTime?: string;
  result?: TestResult;
  tests?: TestRunExecution[];
  executionStatus: ExecutionStatus;
  unexpectedErrors?: UnexpectedError[];
}

export interface TestRunExecution {
  testId: string;
  startDateTime?: string;
  finishDateTime?: string;
  result?: TestResult;
  stepRuns: StepRunExecution[];
  executionStatus: ExecutionStatus;
  unexpectedErrors?: UnexpectedError[];
}

export interface StepRunExecution {
  stepName: string;
  startDateTime?: string;
  finishDateTime?: string;
  result?: StepResult;
  executionStatus: ExecutionStatus;
  unexpectedErrors?: UnexpectedError[];
}

export enum ExecutionStatus {
  QUEUED = "QUEUED",
  RUNNING = "RUNNING",
  FINISHED = "FINISHED",
}