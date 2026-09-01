import { TestReportSummary } from "@/app/test-execution/types/TestRunExecution";
import { ResultBadge } from "@/shared/components/report/ResultBadge";
import { Route } from "next";
import Link from "next/link";
import { getReportUrl } from "../utils/getReportUrl";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { useTranslation } from "react-i18next";
import React from "react";
import { useGetTestRunExecutions } from "../../../hooks/SWR/useGetTestRunExecutions";

interface TestExecutionHistoryProps {
  testReportSummaries: TestReportSummary[];
  testSessionId?: string;
  executionId?: string;
}

const TestExecutionHistory = ({ testReportSummaries, testSessionId, executionId }: TestExecutionHistoryProps) => {
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);
  const { data: lastTestExecution } = useGetTestRunExecutions(executionId || "");

  const currentReportSummaries = lastTestExecution?.testReportSummaries || testReportSummaries;

  return (
    <>
      <div className="p-2">{t("gzl.user.interface.test_exec_history_information")}</div>
      <div className="grid grid-cols-[auto_auto] p-2">
        {[...currentReportSummaries].reverse().map((summary) => {
          const reportUrl = getReportUrl({ testReportUrl: summary.location, testSessionId }) as Route;

          return (
            <React.Fragment key={summary.id}>
              <Link
                href={reportUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue hover:text-visited_link underline flex items-center gap-1"
              >
                {formatDate(summary.dateTime)}
              </Link>

              <div className="p-2 flex items-center">
                <ResultBadge result={summary.result} />
              </div>
            </React.Fragment>
          );
        })}
      </div>
    </>
  );
};

export default TestExecutionHistory;
