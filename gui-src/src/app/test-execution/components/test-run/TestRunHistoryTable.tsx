"use client";
import { useSession } from "next-auth/react";
import { Skeleton } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { Route } from "next";
import TestRunHistoryRow from "@test-execution/components/test-run/TestRunHistoryRow";
import { TestRunExecution } from "@test-execution/types/TestRunExecution";
import { useTestSession } from "@test-execution/context/TestSessionContext";
import { useGetData } from "@hooks/SWR/useGetData";

interface TestRunHistoryTableProps {
  testId: string;
}

export default function TestRunHistoryTable({ testId }: Readonly<TestRunHistoryTableProps>) {
  const { data: session } = useSession();
  const { t } = useTranslation();

  const { testSessionId } = useTestSession();
  const {
    data: historyData,
    isLoading,
    error,
  } = useGetData<TestRunExecution>({
    searchParameters: {
      testSessionId: testSessionId ?? "",
      testId: testId,
      owner: session?.user.gazelleId ?? "",
    },
    field: "id",
    sortOrder: "desc",
    offset: 0,
    limit: 10,
    baseUrl: "/gazelle/test-execution/api",
    apiFolder: "test-run-executions",
    paramMap: { sortBy: "_sort" },
  });

  if (!session) {
    return <p>{t("gzl.texec.history.login_to_view_history")}</p>;
  }

  if (isLoading) {
    return <Skeleton className="h-32" />;
  }

  if (error) {
    return <p className="text-red">{t("gzl.user.interface.error_loading_history")}</p>;
  }

  if (!historyData || historyData.length === 0) {
    return <p>{t("gzl.texec.history.no_history_available")}</p>;
  }

  return (
    <>
      <p>{t("gzl.texec.history.description")}</p>

      <div className="overflow-x-auto border border-lightpurple rounded-lg flex flex-col">
        <table className="min-w-full divide-y divide-gray-200">
          <thead>
            <tr>
              <th className="px-4 py-3 text-left font-semibold">{t("gzl.texec.history.id")}</th>
              <th />
              <th className="px-4 py-3 text-left font-semibold">{t("gzl.texec.history.last_execution_result")}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-grey">
            {historyData.map((entry: TestRunExecution) => {
              const testRunExecutionHref = `/test-execution/test-run-execution?executionId=${entry.id}` as Route;
              return (
                <TestRunHistoryRow
                  key={entry.id}
                  entry={entry}
                  testRunExecutionHref={testRunExecutionHref}
                  tooltipText={t("gzl.texec.view_test_run_tooltip")}
                />
              );
            })}
          </tbody>
        </table>
      </div>
    </>
  );
}
