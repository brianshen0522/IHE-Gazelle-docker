"use client";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "next/navigation";
import Unauthorized from "@auth/Unauthorized";
import { CustomNotFoundError, ScrollTop, Skeleton } from "@gazelle/gazelle-component-ui";
import { ValidationReportDTO } from "@/shared/types/validation/types";
import ValidationReport from "@validation-portal/components/validation-report/ValidationReport";
import { useValidationReportItem } from "@validation-portal/hooks/useValidationReportItem";
import { ReportAssertionsProvider } from "@validation-portal/context/selectedAssertionContext";
import ContentHeaderWrapper from "@shared/components/layout/ContentHeader";
import { ResultBadge } from "@/shared/components/report/ResultBadge";
import { canReadResource } from "@/shared/utils/acl/accessControlListPermissions";

export function ValidationReportMain({ reportId }: Readonly<{ reportId: string }>) {
  const { t } = useTranslation();
  const searchParams = useSearchParams();
  const readAccessKeySearchParam = searchParams.get("readAccessKey") as string;

  const { item, isLoading, error } = useValidationReportItem({
    itemId: reportId,
    readAccessKey: readAccessKeySearchParam ?? undefined,
    enabled: !!reportId,
  });

  const validationBreadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.user.interface.validation_portal"), url: "/validation-portal/profiles" },
    { label: t("gzl.texec.validation_report"), url: `/validation-portal/reports/${reportId}` },
  ];

  if (isLoading) {
    return <Skeleton className="h-screen" />;
  }

  if (error) {
    return <Unauthorized />;
  }

  if (!item) {
    return <CustomNotFoundError />;
  }

  // Some environments still expose validation items with the legacy VALIDATION type.
  if (item.type !== "VALIDATION_REPORT" && item.type !== "VALIDATION") {
    return <CustomNotFoundError message={t("gzl.validation.error.not_validation_report")} />;
  }

  const acl = item.accessControlList;
  const report: ValidationReportDTO = item.content as unknown as ValidationReportDTO;

  const secured = !canReadResource(null, acl, readAccessKeySearchParam);

  const title = (
    <div className="flex justify-start items-center gap-4 mb-4">
      {t("gzl.texec.validation_report")}
      <ResultBadge result={report.overallResult} />
    </div>
  );

  return (
    <ContentHeaderWrapper breadcrumbsItems={validationBreadcrumbs} secured={secured} title={title}>
      <ReportAssertionsProvider>
        <ValidationReport validationReport={report} acl={acl} readAccessKey={readAccessKeySearchParam ?? undefined} />
      </ReportAssertionsProvider>
      <ScrollTop />
    </ContentHeaderWrapper>
  );
}
