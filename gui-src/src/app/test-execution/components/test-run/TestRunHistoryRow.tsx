import { useRef } from "react";
import { Badge, Tooltip } from "@gazelle/gazelle-component-ui";
import { Route } from "next";
import { TestReportSummary, TestReportSummaryResult, TestRunExecution } from "@test-execution/types/TestRunExecution";
import { useTranslation } from "react-i18next";
import useDateFormat from "@hooks/useDateFormat";
import { useRouter } from "next/navigation";

interface TestRunHistoryProps {
  entry: TestRunExecution;
  testRunExecutionHref: Route;
  tooltipText: string;
}

type HistoryResult = TestReportSummary["result"];

const RESULT_VARIANT_MAP: Record<HistoryResult, "success" | "failed" | "default"> = {
  PASSED: "success",
  FAILED: "failed",
  UNDEFINED: "default",
};

function TestRunHistoryRow({ entry, testRunExecutionHref, tooltipText }: Readonly<TestRunHistoryProps>) {
  const linkRef = useRef<HTMLTableRowElement>(null);
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);
  const { push } = useRouter();

  let lastReportDate;
  let lastTestReportSummaryResult: TestReportSummaryResult = "UNDEFINED";

  if (entry.testReportSummaries !== undefined && entry.testReportSummaries !== null && entry.testReportSummaries.length > 0) {
    const lastTestReportSummary = entry.testReportSummaries.at(-1);

    if (lastTestReportSummary !== undefined && lastTestReportSummary !== null) {
      const dateTime = lastTestReportSummary.dateTime;
      if (dateTime !== undefined && dateTime !== null) {
        lastReportDate = formatDate(dateTime);
      }

      if (lastTestReportSummary.result !== undefined && lastTestReportSummary.result !== null) {
        lastTestReportSummaryResult = lastTestReportSummary.result;
      }
    }
  }

  return (
    <tr
      className="hover:bg-lightpurple/20 transition-colors duration-150 flex-wrap cursor-pointer"
      onClick={() => push(testRunExecutionHref)}
      ref={linkRef}
    >
      <td className="px-4 py-3 whitespace-nowrap">
        <div className="flex flex-row gap-1 items-center">{entry.id}</div>
        <Tooltip id={`history-link-${entry.testId}`} position="bottom" triggerRef={linkRef}>
          {tooltipText}
        </Tooltip>
      </td>

      <td className="px-4 py-3 whitespace-nowrap">{lastReportDate ?? t("gzl.texec.history.execution_in_progress")}</td>

      <td className="px-4 py-3 whitespace-nowrap">
        <Badge id={`result-${entry.testId}`} variant={RESULT_VARIANT_MAP[lastTestReportSummaryResult]}>
          {lastTestReportSummaryResult}
        </Badge>
      </td>
    </tr>
  );
}

export default TestRunHistoryRow;
