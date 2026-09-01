import { ValidationSubReportDTO, ValidationReportDTO } from "@/shared/types/validation/types";
import { CollapsableSubCard, IssueCounter } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import UnexpectedErrorsDisplay from "@validation-portal/components/validation-report/UnexpectedErrorDisplay";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { hasUnexpectedErrorsInReportTree } from "@/shared/utils/validation/reportTree";

function ReportOverviewCard({
  report,
  reportIndexPath,
  title,
}: Readonly<{ report: ValidationSubReportDTO; reportIndexPath: number[]; title: string }>) {
  const { t } = useTranslation();
  const hasUnexpectedErrors = hasUnexpectedErrorsInReportTree([report]);
  const hasDirectUnexpectedErrors = (report.unexpectedErrors?.length ?? 0) > 0;

  const cardTitle = (
    <div className="flex flex-row w-full justify-between items-center mr-3">
      <h4 id={`validation-report-${reportIndexPath.join("-")}`}>{title}</h4>
      <ResultBadge result={report.subReportResult} />
    </div>
  );

  return (
    <CollapsableSubCard title={cardTitle} defaultExpanded={hasUnexpectedErrors} className={reportIndexPath.length > 1 ? "m-1" : undefined}>
      <div className="flex flex-col gap-2 p-2">
        <IssueCounter {...report.subCounters} />
        {hasDirectUnexpectedErrors && <UnexpectedErrorsDisplay errors={report.unexpectedErrors ?? []} />}
        {report.subReports?.map((subReport, subReportIndex) => (
          <ReportOverviewCard
            key={`subreport-${reportIndexPath.join("-")}-${subReportIndex}-${subReport.name ?? ""}`}
            report={subReport}
            reportIndexPath={[...reportIndexPath, subReportIndex]}
            title={subReport.name ? subReport.name : t("gzl.texec.validation_subreport") + ` ${subReportIndex + 1}`}
          />
        ))}
      </div>
    </CollapsableSubCard>
  );
}

export function ValidationOverview({ validationReport }: Readonly<{ validationReport: ValidationReportDTO }>) {
  const { t } = useTranslation();

  const containsAtLeastOneUnexpectedError = hasUnexpectedErrorsInReportTree(validationReport.reports);

  if (!validationReport.reports?.length) return <></>;

  const title = <h3 id="validation-steps">{t("gzl.texec.validation_overview")}</h3>;
  return (
    <CollapsableSubCard title={title} defaultExpanded={containsAtLeastOneUnexpectedError}>
      <div className="flex flex-col gap-2 px-3 py-2">
        {validationReport.reports.map((report, reportIndex) => {
          return (
            <ReportOverviewCard
              key={`report-${reportIndex}-${report.name ?? ""}`}
              report={report}
              reportIndexPath={[reportIndex]}
              title={report.name ? report.name : t("gzl.texec.validation_report") + ` ${reportIndex + 1}`}
            />
          );
        })}
      </div>
    </CollapsableSubCard>
  );
}
