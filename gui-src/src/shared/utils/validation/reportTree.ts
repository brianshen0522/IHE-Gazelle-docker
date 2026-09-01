import { ValidationSubReportDTO } from "@/shared/types/validation/types";

export interface ReportTreeNode {
  report: ValidationSubReportDTO;
  depth: number;
  path: string[];
}

export function flattenReportTree(reports?: ValidationSubReportDTO[] | null, parentPath: string[] = [], depth = 0): ReportTreeNode[] {
  if (!reports?.length) {
    return [];
  }

  return reports.flatMap((report, index) => {
    const fallbackName = depth === 0 ? `report-${index + 1}` : `subreport-${index + 1}`;
    const currentName = report.name?.trim() || fallbackName;
    const path = [...parentPath, currentName];

    return [{ report, depth, path }, ...flattenReportTree(report.subReports, path, depth + 1)];
  });
}

export function hasUnexpectedErrorsInReportTree(reports?: ValidationSubReportDTO[] | null): boolean {
  return flattenReportTree(reports).some(
    ({ report }) =>
      (report.subCounters?.numberOfUnexpectedErrors ?? 0) > 0 ||
      (report.unexpectedErrors?.length ?? 0) > 0 ||
      (report.assertionReports?.some((assertion) => (assertion.unexpectedErrors?.length ?? 0) > 0) ?? false),
  );
}
