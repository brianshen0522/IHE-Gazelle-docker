import { TestReport } from "@maestro/types/report/TestReport";
import { useTranslation } from "react-i18next";
import { type StatItem, StatSummary } from "@gazelle/gazelle-component-ui";

interface TestStatsSummaryProps {
  testReport: TestReport;
}

const TestStatsSummary = ({ testReport }: TestStatsSummaryProps) => {
  const { t } = useTranslation();

  const totalTests = testReport.testCounters.total;

  const items: StatItem[] = [
    {
      id: "passed",
      count: testReport.testCounters.passed,
      label: t("gzl.texec.passed"),
      color: "#5fb336",
      percent: totalTests > 0 ? (testReport.testCounters.passed / totalTests) * 100 : 0,
    },
    {
      id: "failed",
      count: testReport.testCounters.failed,
      label: t("gzl.texec.failed"),
      color: "#cc3333",
      percent: totalTests > 0 ? (testReport.testCounters.failed / totalTests) * 100 : 0,
    },
    {
      id: "undefined",
      count: testReport.testCounters.undefined,
      label: t("gzl.texec.undefined"),
      color: "#6f6f6f",
      percent: totalTests > 0 ? (testReport.testCounters.undefined / totalTests) * 100 : 0,
    },
  ];

  return <StatSummary title={t("gzl.texec.number_of_tests")} totalCount={totalTests} items={items} />;
};

export default TestStatsSummary;
