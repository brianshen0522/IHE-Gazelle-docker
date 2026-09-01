/**
 * Backend returns validation profiles wrapped with service information
 */
export interface ValidationProfileResponse {
  validationService: string;
  profile: ValidationProfile;
}

export interface ValidationProfile {
  profileID: string;
  profileName?: string;
  version?: string;
  domain?: string;
  coveredItems?: string[];
  standards?: string[];
  inputs?: SupportedInput[];
  tags?: string[];
}

export interface SupportedInput {
  id: string;
  label?: string;
  required?: boolean;
}

export interface ValidationHistory {
  reportId: string;
  executionDate: string;
  result: string;
}

export interface FavoriteSearch {
  id: string;
  name: string;
  params: Record<string, string>;
  createdAt: string;
}

export interface ValidationReportObject {
  id: string;
  type: "VALIDATION_REPORT";
  content: string; // JSON string containing the detailed validation report
  date: string;
  additionalParameters: Record<string, unknown>;
  references: ValidationReference[];
  accessControlList: AccessControlList;
}

export interface ValidationReference {
  name: string;
  value: string;
  refType: "ATTACHMENT";
  type: "VALIDATION_INPUT";
}

export interface AccessControlList {
  isPublic: boolean;
}

export interface ParsedValidationContent {
  modelVersion: string;
  uuid: string;
  dateTime: string;
  disclaimer: string;
  overallResult: ValidationResult;
  validationMethod: ValidationMethod;
  inputs: ValidationInput[];
  reports: ValidationSubReport[];
  counters: ValidationCounters;
}

export interface ValidationMethod {
  validationServiceName: string;
  validationServiceVersion: string;
  validationProfileID: string;
  validationProfileName: string;
  validationProfileVersion: string;
}

export interface ValidationInput {
  id: string;
  itemId: string;
  location: string;
}

export interface ValidationSubReport {
  name: string;
  subReportResult: ValidationResult;
  assertionReports: AssertionReport[];
  subCounters: ValidationCounters;
}

export interface AssertionReport {
  result: ValidationResult;
  severity: ValidationSeverity;
  priority: ValidationPriority;
  description: string;
  subjectLocations?: SubjectLocation[];
}

export interface SubjectLocation {
  inputId: string;
  type: "line-column" | "XPath";
  value: string;
}

export interface ValidationCounters {
  numberOfAssertions: number;
  numberOfFailedWithInfos: number;
  numberOfFailedWithWarnings: number;
  numberOfFailedWithErrors: number;
  numberOfUnexpectedErrors: number;
  numberOfUndefined: number;
}

export type ValidationResult = "PASSED" | "FAILED";

export type ValidationSeverity = "INFO" | "WARNING" | "ERROR";

export type ValidationPriority = "MANDATORY" | "OPTIONAL";
