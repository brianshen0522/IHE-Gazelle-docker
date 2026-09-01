"use client";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { TestReportSummary } from "@test-execution/components/test-report/maestroReport/TestReportSummary";
import { TestReportSUTs } from "@test-execution/components/test-report/maestroReport/TestReportSUTs";
import { TestReportRuns } from "@test-execution/components/test-report/maestroReport/TestReportRuns";
import { TestReport as TestReportType } from "@maestro/types/report/TestReport";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { DatahouseItemReference } from "@shared/types/datahouse/DatahouseItem";
import ContentHeaderWrapper from "@shared/components/layout/ContentHeaderWrapper";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "next/navigation";

type TestReportProps = {
  report: TestReportType;
  reportId: string;
  acl: AccessControlList;
  references: DatahouseItemReference[];
};

export function TestReportView({ report, reportId, acl, references }: Readonly<TestReportProps>) {
  const { t } = useTranslation();
  const searchParams = useSearchParams();
  const readAccessKeySearchParam = searchParams.get("readAccessKey");
  const hasReadAccessKey = Boolean(readAccessKeySearchParam);
  const previousReportId = searchParams.get("previousReportId");

  const testExecutionBreadcrumbs = [
    { label: t("gzl.home.home"), url: "/home" },
    { label: t("gzl.texec.test_execution"), url: `/test-execution/test-suite` },
  ];

  previousReportId &&
    testExecutionBreadcrumbs.push({
      label: t("gzl.texec.test_report"),
      url: `/test-execution/report/${previousReportId}`,
    });
  testExecutionBreadcrumbs.push({
    label: t("gzl.texec.test_report"),
    url: `/test-execution/report/${reportId}`,
  });

  return (
    <ContentHeaderWrapper
      id="test-report"
      breadcrumbs={testExecutionBreadcrumbs}
      title={
        <div className="flex items-center gap-2 mt-2">
          {report.testSuiteName ?? report.uuid} <ResultBadge result={report.result} />
        </div>
      }
      secured={!hasReadAccessKey && !acl.isPublic}
    >
      <div className="space-y-8 p-4">
        <TestReportSummary testReport={report} acl={acl} />
        <TestReportSUTs testReportSUTs={report.systemsUnderTest!} />
        <TestReportRuns testRunReports={report.testRunReports} references={references} />
      </div>
    </ContentHeaderWrapper>
  );
}
