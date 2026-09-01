import { useTranslation } from "react-i18next";
import { ValidationReportDTO } from "@/shared/types/validation/types";
import { StatSummary, StatItem } from "@gazelle/gazelle-component-ui";

interface ValidationStatSummaryProps {
  validationReport: ValidationReportDTO;
}

function computePercentOfCategorizedIssues(numberOfIssue: number, totalIssues: number): number {
  const result = totalIssues > 0 ? (numberOfIssue / totalIssues) * 100 : 0;
  return Number.isNaN(result) ? 0 : result;
}

const ValidationStatSummary = ({ validationReport }: ValidationStatSummaryProps) => {
  const { t } = useTranslation();

  function computeNumberOfIssues(): number {
    const counters = validationReport.counters;
    return (
      (counters?.numberOfFailedWithErrors ?? 0) +
      (counters?.numberOfFailedWithWarnings ?? 0) +
      (counters?.numberOfFailedWithInfos ?? 0) +
      (counters?.numberOfUndefined ?? 0)
    );
  }

  const totalIssues = computeNumberOfIssues();

  const items: StatItem[] = [
    {
      id: "errors",
      count: validationReport.counters?.numberOfFailedWithErrors ?? 0,
      label: t("gzl.texec.errors"),
      color: "#cc3333",
      percent: computePercentOfCategorizedIssues(validationReport.counters?.numberOfFailedWithErrors ?? 0, totalIssues),
    },
    {
      id: "warnings",
      count: validationReport.counters?.numberOfFailedWithWarnings ?? 0,
      label: t("gzl.texec.warnings"),
      color: "#EE7601",
      percent: computePercentOfCategorizedIssues(validationReport.counters?.numberOfFailedWithWarnings ?? 0, totalIssues),
    },
    {
      id: "infos",
      count: validationReport.counters?.numberOfFailedWithInfos ?? 0,
      label: t("gzl.texec.infos"),
      color: "#45327d",
      percent: computePercentOfCategorizedIssues(validationReport.counters?.numberOfFailedWithInfos ?? 0, totalIssues),
    },
    {
      id: "undefined",
      count: validationReport.counters?.numberOfUndefined ?? 0,
      label: t("gzl.texec.undefined"),
      color: "#6f6f6f",
      percent: computePercentOfCategorizedIssues(validationReport.counters?.numberOfUndefined ?? 0, totalIssues),
    },
  ];

  return <StatSummary title={t("gzl.texec.number_of_issues")} totalCount={totalIssues} items={items} />;
};

export default ValidationStatSummary;
