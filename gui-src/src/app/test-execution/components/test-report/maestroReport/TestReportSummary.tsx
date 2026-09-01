"use client";
import CollapsableCard from "@shared/components/boxes/CollapsableCard";
import { Button, InfoRow, NoticeBanner, useSmallScreen } from "@gazelle/gazelle-component-ui";
import { TestReport } from "@maestro/types/report/TestReport";
import { AclDisplay } from "@/shared/components/ACL/AclDisplay";
import { toast } from "react-toastify";
import { Download, Info } from "lucide-react";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { useParams, useSearchParams } from "next/navigation";
import { useTranslation } from "react-i18next";
import TestStatsSummary from "./TestStatsSummary";
import useDateFormat from "@/shared/hooks/useDateFormat";

function scrollIntoFirstUnexpectedError() {
  const element = document.getElementsByClassName("unexpected-errors");
  if (element && element.length > 0) {
    (element[0] as HTMLElement).scrollIntoView({ behavior: "smooth", block: "center" });
  }
}

export function TestReportSummary({ testReport, acl }: Readonly<{ testReport: TestReport; acl: AccessControlList }>) {
  const { t } = useTranslation();
  const { reportId } = useParams<{ reportId: string }>();
  const searchParams = useSearchParams();
  const readAccessKey = searchParams.get("readAccessKey");
  const formatDate = useDateFormat(false);
  const isSmallScreen = useSmallScreen();

  function handleDownloadTestReport() {
    if (testReport) {
      const testReportDownload = JSON.stringify(testReport);
      const blob = new Blob([testReportDownload], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = testReport.testSuiteName ? testReport.testSuiteName.replaceAll(" ", "_") + ".json" : `test_report.json`;
      link.click();
      URL.revokeObjectURL(url);
    } else {
      toast.error("gzl.texec.no_content_to_download");
    }
  }

  function alertPotentialUnexpectedError() {
    const hasUnexpectedError =
      (testReport.unexpectedErrors && testReport.unexpectedErrors.length > 0) ||
      testReport.testRunReports?.some(
        (testRun) =>
          (testRun.unexpectedErrors && testRun.unexpectedErrors.length > 0) ||
          testRun.stepRunReports?.some((stepReport) => stepReport.unexpectedErrors && stepReport.unexpectedErrors.length > 0),
      );
    if (hasUnexpectedError) {
      return (
        <div className="border-1 border-red rounded-small bg-lightred bg-opacity-40 px-1.5 py-1 mb-3 w-fit inline">
          <p className="text-red inline pr-2">{t("gzl.texec.one_or_more_issues_have_been_encountered_while_executing_the_test")}.</p>
          <button className="text-blue hover:text-visited_link" onClick={scrollIntoFirstUnexpectedError}>
            {t("gzl.texec.read_more_details")}.
          </button>
        </div>
      );
    }
  }

  return (
    <CollapsableCard title={t("gzl.texec.test_summary")}>
      <div className={`flex ${isSmallScreen ? " flex-col" : " flex-row"} gap-3 justify-start mt-3`}>
        <div className="flex flex-col gap-4 basis-7/12">
          <InfoRow label={t("gzl.texec.test")} value={testReport.testSuiteName ?? testReport.uuid} />

          <InfoRow label={t("gzl.texec.creation_time")} value={formatDate(testReport.dateTime)} />

          {testReport.testService?.serviceIdentification && (
            <InfoRow
              label={t("gzl.texec.executed_with")}
              value={
                <>
                  {testReport.testService.serviceIdentification.name}{" "}
                  {testReport.testService.serviceIdentification.version && `(${testReport.testService.serviceIdentification.version})`}
                </>
              }
            />
          )}

          {testReport.subReportsReferences && testReport.subReportsReferences.length > 0 && (
            <InfoRow
              label={t("gzl.texec.attachments")}
              value={testReport.subReportsReferences.map((reportReference) => {
                let href = `/test-execution/reports/${reportReference.testReportId}?previousReportId=${reportId}`;
                if (readAccessKey) href += `&readAccessKey=${readAccessKey}`;

                return (
                  <li key={reportReference.testSuiteName} className="list-item">
                    <a href={href} target="_blank" rel="noopener noreferrer" className="text-blue">
                      {reportReference.testSuiteName} ({reportReference.result})
                    </a>
                  </li>
                );
              })}
            />
          )}

          {testReport.note && testReport.note.length > 0 && (
            <InfoRow label={t("gzl.texec.note")} value={<p className="inline">{testReport.note}</p>} />
          )}

          <AclDisplay acl={acl} itemId={reportId} />

          {alertPotentialUnexpectedError()}

          <NoticeBanner className="flex items-center gap-2">
            <Info className="hidden md:flex" />
            {testReport?.testService?.disclaimer && <i className="ml-2">{testReport.testService.disclaimer}</i>}
          </NoticeBanner>
        </div>

        <div className="flex flex-col gap-2 items-center">
          <TestStatsSummary testReport={testReport} />
          <Button id="download-test-report" type={"button"} className="mx-auto my-2" variant="primary" onClick={handleDownloadTestReport}>
            <Download /> {t("gzl.texec.download_test_report")}
          </Button>
        </div>
      </div>
    </CollapsableCard>
  );
}
