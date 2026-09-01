"use client";
import ValidationReportSummary from "@/app/validation-portal/components/validation-report/summary/ValidationReportSummary";
import ResizableSplit from "@test-execution/components/ResizableSplit"; //TODO: clean code in this component + move to UI lib
import ValidationReportAssertions from "@/app/validation-portal/components/validation-report/assertion/ValidationReportAssertions";
import ValidationReportFiles from "@/app/validation-portal/components/validation-report/content/ValidationReportFiles";
import { ValidationReportDTO } from "@/shared/types/validation/types";
import { ValidationServiceMetadata } from "@/app/validation-portal/components/validation-report/metadata/ValidationServiceMetadata";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { ValidationOverview } from "@/app/validation-portal/components/validation-report/overview/ValidationOverview";
import { useParams } from "next/navigation";

type ValidationReportProps = {
  validationReport: ValidationReportDTO;
  acl: AccessControlList;
  /**
   * Optional itemId - if not provided, will be retrieved from URL params
   * Used when displaying reports in modals (test-execution) vs full pages (validation-portal)
   */
  itemId?: string;
  readAccessKey?: string;
};

function ValidationReport({ validationReport, acl, itemId: itemIdProp, readAccessKey }: Readonly<ValidationReportProps>) {
  const params = useParams<{ reportId: string }>();

  // Use provided itemId or fall back to URL params
  const itemId = itemIdProp || params.reportId;

  return (
    <div className="flex flex-col gap-4 mx-5 mb-3">
      <ValidationReportSummary validationReport={validationReport} acl={acl} readAccessKey={readAccessKey} />
      <ValidationServiceMetadata validationReport={validationReport} />
      <ValidationOverview validationReport={validationReport} />
      <div className="max-h-screen">
        <ResizableSplit
          initialLeftWidth={48}
          leftComponent={<ValidationReportAssertions validationReport={validationReport} />}
          rightComponent={
            <ValidationReportFiles
              itemId={itemId}
              validationItems={validationReport.inputs ?? validationReport.validationItems!}
              inputFileNames={validationReport.inputFileNames}
              readAccessKey={readAccessKey}
            />
          }
        />
      </div>
    </div>
  );
}

export default ValidationReport;
