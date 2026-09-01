import { MaestroReport, MaestroStepReport } from "@test-execution/services/maestro/maestroReport";
import { TestEngine, TestRunStatus, TestStepStatus } from "./TestRunExecution";

export interface TestRun {
  id: string;
  userId: string;
  engine: TestEngine | undefined;
  status: TestRunStatus;
  steps: TestStepElement[];
  report?: MaestroReport;
  timestamp: number;
}

export type TestStepElement = {
  testStepIndex: number;
  id: string;
  status: TestStepStatus;
  report?: MaestroStepReport;
  location?: string;
};


export type UploadedInput = {
  id: string;
  value: string;
}