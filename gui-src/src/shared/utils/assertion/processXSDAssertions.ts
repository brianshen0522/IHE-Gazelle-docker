import { AssertionReportDTO, ValidationSubReportDTO } from "@/shared/types/validation/types";
import { AssertionGroup } from "../../../app/validation-portal/components/validation-report/types";

/**
 * Process XSD assertions from reports and sub-reports
 * @param reports Array of reports
 * @param result Filter by result (e.g., "PASSED", "FAILED", "UNDEFINED")
 */
export function processXSDAssertions(reports: ValidationSubReportDTO[], result: string): AssertionGroup[] {
  const xsdGroups: AssertionGroup[] = [];

  reports.forEach((report) => {
    if (report.name === "XSD Validation" || report.standards?.includes("XSD")) {
      // Process main report assertions
      const mainAssertions =
        report.assertionReports
          ?.filter((assertion) => assertion.result === result)
          .map((assertion) => ({
            ...assertion,
            source: report.name,
          })) || [];

      // Process subReports assertions if present
      const subAssertions: AssertionReportDTO[] = [];
      if (report.subReports) {
        report.subReports.forEach((subReport) => {
          if (subReport.assertionReports) {
            const filtered = subReport.assertionReports
              .filter((assertion) => assertion.result === result)
              .map((assertion) => ({
                ...assertion,
                source: `${report.name} - ${subReport.name}`,
              }));
            subAssertions.push(...filtered);
          }
        });
      }

      // If we have any assertions, add them to the groups
      if (mainAssertions.length > 0 || subAssertions.length > 0) {
        xsdGroups.push({
          reportName: report.name,
          assertions: [...mainAssertions, ...subAssertions],
        });
      }
    }
  });

  return xsdGroups;
}
