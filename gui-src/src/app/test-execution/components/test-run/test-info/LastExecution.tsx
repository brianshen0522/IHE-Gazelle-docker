import { ResultBadge } from "@/shared/components/report/ResultBadge";
import { FileCheck } from "lucide-react";
import Link from "next/link";
import { TestRunExecution } from "../../../types/TestRunExecution";
import { useTranslation } from "react-i18next";
import { Route } from "next";
import { useGetTestRunExecutions } from "../../../hooks/SWR/useGetTestRunExecutions";

interface LastExecutionProps {
  testRun: TestRunExecution;
  reportUrl?: string;
}

const LastExecution = ({ testRun, reportUrl }: Readonly<LastExecutionProps>) => {
  const { t } = useTranslation();
  const { data: lastTestExecution } = useGetTestRunExecutions(testRun.id || "");

  const currentTestRun = lastTestExecution || testRun;

  const getTestResult = () => {
    if (currentTestRun?.testReport) {
      return currentTestRun.testReport.result;
    }

    // Fallback to last test report summary if available
    if (currentTestRun?.testReportSummaries && currentTestRun.testReportSummaries.length > 0) {
      const lastSummary = currentTestRun.testReportSummaries.at(-1);
      return lastSummary?.result;
    }

    return null;
  };

  const testResult = getTestResult();

  return (
    <div className="flex flex-col md:flex-row items-center gap-2">
      <div className="flex items-center gap-2 font-semibold">
        {t("gzl.user.interface.last_execution_status")}:<span>{testResult && <ResultBadge result={testResult} />}</span>
      </div>
      <div className="flex items-center gap-2">
        {reportUrl && (
          <Link
            href={reportUrl as Route}
            target="_blank"
            rel="noopener noreferrer"
            className="text-blue hover:text-visited_link underline flex items-center gap-1"
          >
            <FileCheck />
            {t("gzl.user.interface.access_test_report")}
          </Link>
        )}
      </div>
    </div>
  );
};

export default LastExecution;
