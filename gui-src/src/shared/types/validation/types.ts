import { UnexpectedError } from "@maestro/types/report/UnexpectedError";

export type ValidationResult = "PASSED" | "FAILED" | "UNDEFINED";

export type ValidationReportDTO = {
  modelVersion: string;
  uuid: string;
  dateTime: string;
  disclaimer: string;
  overallResult: ValidationResult;
  validationMethod: ValidationMethod;
  validationItems?: ValidationInput[];
  inputs?: ValidationInput[];
  inputFileNames?: Record<string, string>; // Used by validation-portal
  reports?: ValidationSubReportDTO[] | null;
  counters: Counters;
  additionalMetadata?: Metadata[] | null;
};

export type ValidationMethod = {
  validationServiceName: string;
  validationServiceVersion: string;
  validationProfileID: string;
  validationProfileName?: string;
  validationProfileVersion: string;
};

export type ValidationInput = {
  id: string;
  itemId?: string; // Used by test-execution
  content?: string; // base64 encoded content
  location?: string;
};

export type ValidationSubReportDTO = {
  name: string;
  subReportResult: ValidationResult;
  subReports?: ValidationSubReportDTO[] | null;
  assertionReports?: AssertionReportDTO[] | null;
  subCounters: Counters;
  standards?: string[] | null;
  unexpectedErrors?: UnexpectedError[] | null;
};

export type AssertionReportDTO = {
  assertionID?: string;
  assertionType?: string;
  description?: string;
  subjectLocations?: SubjectLocation[] | null;
  subjectValue?: string;
  formalExpression?: string;
  requirementIDs?: string[] | null;
  severity: string;
  priority?: string;
  result?: ValidationResult;
  unexpectedErrors?: UnexpectedError[] | null;
  source?: string; // Optional element for front to manage source of the assertion when sorted by severity
};

export type SubjectLocation = {
  inputId?: string;
  type?: string;
  value?: string;
};

export function isEqualAssertion(a: AssertionReportDTO | undefined, b: AssertionReportDTO | undefined): boolean {
  if (!a || !b) return false;

  return a.assertionID === b.assertionID && a.description === b.description;
}

export type Counters = {
  numberOfAssertions: number;
  numberOfFailedWithInfos: number;
  numberOfFailedWithWarnings: number;
  numberOfFailedWithErrors: number;
  numberOfUnexpectedErrors: number;
  numberOfUndefined: number;
};

export type Metadata = {
  name: string;
  value: string;
};
