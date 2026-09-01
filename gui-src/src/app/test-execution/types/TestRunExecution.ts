import { AccessControlList } from "@/shared/types/AccessControlListTypes";

export type TestRunStatus = "NOT_STARTED" | "IN_PROGRESS" | "READY_FOR_REVIEW" | "COMPLETED" | "FAILED" | "PASSED" | "SUCCESS" | "RUNNING" | "UNDEFINED";
export type TestStepStatus = "RUNNING" | "TODO" | "PASSED" | "COMPLETED" | "FAILED" | "SKIPPED" | "UNKNOWN" | "UNDEFINED";
export type TestEngine = "MAESTRO" | "ROBOT" | "ITB";
export type TestReportSummaryResult = "FAILED" | "PASSED" | "UNDEFINED";

export type LogEntry = TestStepStatus | Record<string, unknown>;

export interface TestRunExecution {
  id: string;
  inputs: TestInput[];
  testSessionId: string;
  testSuiteId: string;
  testId: string;
  testSpecification: string;
  testName: string;
  testSummary?: string;
  testTags?: string[];
  testRoles?: string[];
  testVersion?: string;
  status: TestRunStatus;
  startedAt: string;
  completedAt?: string;
  duration?: number; // in milliseconds
  executedBy: string;
  results?: TestRunResult[];
  logs?: LogEntry[];
  errorMessage?: string;
  liveStepResults?: StepRunReport[]; // Live step execution state (during test run)
  testReport?: TestReport; // Final test report (set after completion)
  testReportUrl?: string;
  testReportSummaries?: TestReportSummary[];
  accessControlList?: AccessControlList;
  lastUpdateTimestamp?: string;
}

export interface ServiceIdentification {
  version: string;
  name: string;
}

export interface TestService {
  serviceIdentification: ServiceIdentification;
  disclaimer: string;
}

export interface TestCounters {
  total: number;
  passed: number;
  failed: number;
  undefined: number;
  unexpectedErrors: number;
}

export interface Test {
  id: string;
  name: string;
}

export interface TestInput {
  type: string;
  name: string;
  value: string;
  reference?: string;
}

export interface StepOutput {
  type: string;
  name: string;
  fileName?: string;
  mimeType?: string;
  itemType?: string;
  reference?: string;
}

export interface StepRunReport {
  stepName: string;
  type: string;
  dateTime: string;
  result: TestStepStatus;
  outputs?: StepOutput[];
}

export interface TestRunReport {
  dateTime: string;
  result: TestStepStatus;
  test: Test;
  inputs?: TestInput[];
  stepRunReports?: StepRunReport[];
}

export interface TestReport {
  uuid: string;
  result: TestStepStatus;
  dateTime: string;
  testService: TestService;
  testSuiteName: string;
  testCounters: TestCounters;
  testRunReports: TestRunReport[];
}

export interface TestReportSummary {
  id: string;
  result: TestReportSummaryResult;
  dateTime: string;
  location: string;
}

export interface TestRunResult {
  stepId: string;
  stepName: string;
  status: TestStepStatus;
  duration: number;
  message?: string;
}
