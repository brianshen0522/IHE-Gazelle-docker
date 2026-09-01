import { Session } from "next-auth";

export interface FileInput {
  id: string;
  content: string;
  name: string;
}

export interface ExecuteValidationParams {
  profileId: string;
  serviceName: string;
  fileInputs: FileInput[];
  session: Session | null;
}

export interface ExecuteValidationResult {
  reportId?: string;
  inlineReport?: unknown;
  error?: string;
}

export interface StepProperty {
  name: string;
  type: "STRING" | "BYTE_ARRAY";
  value: string;
}

export interface MaestroInput {
  name: string;
  type: "BYTE_ARRAY";
  value: string;
}

export interface MaestroOutput {
  itemType?: string;
  name?: string;
  reference?: string;
}

export interface UnexpectedError {
  name: string;
  message: string;
}

export interface StepRunReport {
  outputs?: MaestroOutput[];
  unexpectedErrors?: UnexpectedError[];
}

export interface TestRunReport {
  stepRunReports?: StepRunReport[];
}

export interface MaestroTestStep {
  report?: {
    location?: string;
  };
}

export interface MaestroTest {
  steps?: MaestroTestStep[];
}

export interface MaestroResponse {
  testRunReports?: TestRunReport[];
  test?: MaestroTest;
}
