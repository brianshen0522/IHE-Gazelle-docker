"use client";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "next/navigation";
import { CustomInternalError, CustomNotFoundError, ScrollTop, Skeleton } from "@gazelle/gazelle-component-ui";
import { TestReport as TestReportType } from "@maestro/types/report/TestReport";
import { TestReportView } from "@test-execution/components/test-report/maestroReport/TestReportView";
import { useDatahouseItem } from "@hooks/useDatahouseItem";

export function TestExecutionReport({ reportId }: Readonly<{ reportId: string }>) {
  const { t } = useTranslation();
  const searchParams = useSearchParams();
  const readAccessKeySearchParam = searchParams.get("readAccessKey");

  const { item, isLoading, error } = useDatahouseItem({
    itemId: reportId,
    readAccessKey: readAccessKeySearchParam ?? undefined,
    enabled: !!reportId,
  });

  if (isLoading) {
    return <Skeleton className="h-screen" />;
  }

  if (error) {
    return <CustomInternalError message={t("gzl.texec.error.fetching_report")} />;
  }

  if (!item) {
    return <CustomNotFoundError />;
  }

  // Type guard: Only handle TEST_REPORT (maestro reports)
  if (item.type !== "TEST_REPORT") {
    return <CustomNotFoundError message={t("gzl.texec.error.not_test_report")} />;
  }

  const acl = item.accessControlList;
  const report: TestReportType = item.content as unknown as TestReportType;
  const references = item.references;

  return (
    <>
      <TestReportView report={report} reportId={reportId} acl={acl} references={references} />
      <ScrollTop />
    </>
  );
}
