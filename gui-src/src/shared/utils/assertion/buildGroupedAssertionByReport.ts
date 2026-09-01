import { ValidationSubReportDTO } from "@/shared/types/validation/types";
import { AssertionGroup } from "../../../app/validation-portal/components/validation-report/types";
import { processXSDAssertions } from "./processXSDAssertions";

/**
 * Builds a grouped assertion report by report.
 * @param reports a list of reports
 * @param result the result to filter assertions by (e.g., "PASSED", "FAILED", "UNDEFINED")
 */
export function buildGroupedAssertionByReport(reports: ValidationSubReportDTO[], result: string): AssertionGroup[] {
  const groups: AssertionGroup[] = [];

  const visit = (nodes?: ValidationSubReportDTO[] | null, parentPath: string[] = []) => {
    if (!nodes?.length) {
      return;
    }

    nodes.forEach((report, index) => {
      const fallbackName = parentPath.length === 0 ? `Report ${index + 1}` : `Subreport ${index + 1}`;
      const currentName = report.name?.trim() || fallbackName;
      const path = [...parentPath, currentName];
      const assertions = (report.assertionReports ?? []).filter((assertion) => assertion.result === result);

      if (assertions.length > 0) {
        groups.push({
          reportName: path.join(" / "),
          assertions,
        });
      }

      visit(report.subReports, path);
    });
  };

  visit(reports);

  // Process XSD assertions and merge with regular groups
  const xsdGroups = processXSDAssertions(reports, result);
  xsdGroups.forEach((xsdGroup) => {
    const existingGroup = groups.find((g) => g.reportName === xsdGroup.reportName);
    if (existingGroup) {
      // Merge XSD assertions into existing group
      existingGroup.assertions.push(...xsdGroup.assertions);
    } else {
      // Add new XSD group
      groups.push(xsdGroup);
    }
  });

  return groups;
}
