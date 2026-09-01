import { UnexpectedError } from "@maestro/types/report/UnexpectedError";
import { StepResult, TestResult } from "@maestro/types/report/Result";
import { Property } from "@maestro/types/report/Property";

export interface TestReport {
  reportVersion?: string;
  uuid: string;
  dateTime: string;
  result: TestResult;
  testSuiteName: string
  testCounters: {
    total: number;
    passed: number;
    failed: number;
    undefined: number;
    unexpectedErrors: number;
  };
  note?: string;
  urlToTestSuiteResult?: string;
  subReportsReferences?: TestReportReference[];
  unexpectedErrors?: UnexpectedError[];
  systemsUnderTest?: SystemUnderTest[];
  testRunReports: TestRunReport[];
  testService?: {
    serviceIdentification: {
      name: string;
      version: string;
    }
    disclaimer: string;
  }
}

export interface TestReportReference {
  testSuiteName: string;
  testReportId: string;
  result: TestResult
}

export interface TestRunReport {
  runId: string;
  dateTime: string;
  result: TestResult;
  test: Test;
  stepRunReports: StepRunReport[];
  urlToTestRun: string;
  unexpectedErrors: UnexpectedError[];
  inputs: Property[];
  outputs: Property[];
}

export interface StepRunReport {
  stepName: string;
  type: string;
  result: StepResult;
  outputs: Property[];
  unexpectedErrors: UnexpectedError[];
}

export interface Test {
  id: string;
  name: string;
  description: string;
  version: string;
}

export interface SystemUnderTest {
  systemIdentification: {
    name: string;
    version: string;
  },
  macAddresses? : string[];
  ipAddresses? : string[];
  hostNames : string[];
}