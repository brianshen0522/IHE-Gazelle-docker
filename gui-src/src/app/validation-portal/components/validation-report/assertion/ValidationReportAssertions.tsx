import { useEffect } from "react";
import { AssertionReportDTO, ValidationSubReportDTO, SubjectLocation, ValidationReportDTO } from "@/shared/types/validation/types";
import Assertion from "@/app/validation-portal/components/validation-report/assertion/Assertion";
import { useReportAssertions } from "@validation-portal/context/selectedAssertionContext";
import AssertionBuilder from "./AssertionBuilder";
import { useTranslation } from "react-i18next";
import { buildGroupedAssertionByReport } from "@shared/utils/assertion/buildGroupedAssertionByReport";

function getFailedAssertionsWithLocation(reports?: ValidationSubReportDTO[] | null): AssertionReportDTO[] {
  if (!reports) return [];

  return reports.flatMap((report) => {
    const failedAssertionsOnReport = (report.assertionReports ?? [])
      .filter(
        (assertion): assertion is AssertionReportDTO & { subjectLocations: SubjectLocation[] } =>
          assertion.result === "FAILED" && !!assertion.subjectLocations,
      )
      .map((assertion) => ({
        ...assertion,
        requirementIDs: assertion.requirementIDs ?? [],
        unexpectedErrors: assertion.unexpectedErrors ?? [],
      }));

    const fromSubReports = getFailedAssertionsWithLocation(report.subReports);

    return [...failedAssertionsOnReport, ...fromSubReports];
  });
}

/**
 * Displays assertions grouped by report.
 * Each group contains a report name and a list of assertions.
 * The assertions are displayed using the Assertion component.
 * @param assertionsByReport
 */
function displayAssertionsGroupedByReport(
  assertionsByReport: {
    reportName: string;
    assertions: AssertionReportDTO[];
  }[],
) {
  return assertionsByReport.map((group) => (
    <div key={`report-${group.reportName}`}>
      <h3 className="font-semibold mb-1">{group.reportName?.toUpperCase()}</h3>
      <div className="flex flex-col gap-2">
        {group.assertions.map((assertion, index) => (
          <Assertion key={`assertion-${group.reportName}-${index}-${assertion.assertionID ?? ""}`} assertion={assertion} />
        ))}
      </div>
    </div>
  ));
}

function ValidationReportAssertions({ validationReport }: Readonly<{ validationReport: ValidationReportDTO }>) {
  const { t } = useTranslation();
  const { setAssertionsWithLocation } = useReportAssertions();

  useEffect(() => {
    setAssertionsWithLocation(getFailedAssertionsWithLocation(validationReport.reports));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [validationReport.reports]);

  return (
    <div className="flex flex-col gap-4">
      <AssertionBuilder
        validationReport={validationReport}
        buildGroupedAssertionByReport={buildGroupedAssertionByReport}
        displayAssertionsGroupedByReport={displayAssertionsGroupedByReport}
        type="UNDEFINED"
        title={t("gzl.texec.undefined_assertions")}
        description={t("gzl.texec.the_following_assertions_could_not_be_evaluated")}
      />
      <AssertionBuilder
        validationReport={validationReport}
        buildGroupedAssertionByReport={buildGroupedAssertionByReport}
        displayAssertionsGroupedByReport={displayAssertionsGroupedByReport}
        type="FAILED"
        title={t("gzl.texec.reported_issues")}
        description={t("gzl.texec.the_following_reported_by_the_validation_service_are_listed_below")}
        allowGroupBySeverity={true}
      />
      <AssertionBuilder
        validationReport={validationReport}
        buildGroupedAssertionByReport={buildGroupedAssertionByReport}
        type="PASSED"
        title={t("gzl.texec.successful_assertions")}
        description={t("gzl.texec.the_following_assertions_completed_successfully")}
        initiallyExpanded={false}
      />
    </div>
  );
}

export default ValidationReportAssertions;
