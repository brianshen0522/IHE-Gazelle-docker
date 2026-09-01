import { JSX } from "react";
import { AssertionReportDTO, ValidationReportDTO } from "@/shared/types/validation/types";

export interface AssertionGroup {
  reportName: string;
  assertions: AssertionReportDTO[];
}

export type AssertionResult = "PASSED" | "FAILED" | "UNDEFINED";

export interface BuildAssertionsProps {
  validationReport: ValidationReportDTO;
  buildGroupedAssertionByReport: (reports: any, result: string) => AssertionGroup[];
  displayAssertionsGroupedByReport?: (groupedAssertions: AssertionGroup[]) => JSX.Element[];
  type: AssertionResult;
  title: string;
  description: string;
  initiallyExpanded?: boolean;
  allowGroupBySeverity?: boolean;
}
