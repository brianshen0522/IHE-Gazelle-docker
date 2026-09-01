"use client";
import { useSession } from "next-auth/react";
import { Skeleton } from "@gazelle/gazelle-component-ui";
import { useGetData } from "@shared/hooks/SWR/useGetData";
import { ValidationHistory } from "@validation-portal/types/ValidationProfile";
import { useTranslation } from "react-i18next";
import useDateFormat from "@/shared/hooks/useDateFormat";
import HistoryRow from "./HistoryRow";
import { Route } from "next";
import Link from "next/link";

interface ProfileHistoryTableProps {
  profileId: string;
}

export default function ProfileHistoryTable({ profileId }: Readonly<ProfileHistoryTableProps>) {
  const { data: session } = useSession();
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);
  const {
    data: historyData,
    isLoading,
    error,
  } = useGetData<ValidationHistory>({
    searchParameters: {
      profileId: profileId,
      owner: session?.user.gazelleId ?? "",
    },
    field: "date",
    sortOrder: "desc",
    offset: 0,
    limit: 10,
    baseUrl: "/gazelle/validation-portal/api",
    apiFolder: "history",
  });

  if (!session) {
    return <p>{t("gzl.validation_portal.login_to_view_history")}</p>;
  }

  if (isLoading) {
    return <Skeleton className="h-32" />;
  }

  if (error) {
    return <p className="text-red">{t("gzl.user.interface.error_loading_history")}</p>;
  }

  if (!historyData || historyData.length === 0) {
    return <p>{t("gzl.validation_portal.no_history_available")}</p>;
  }

  const allReportsHref = `/reports?type=VALIDATION_REPORT&validation_profile_id=${encodeURIComponent(profileId)}` as Route;

  return (
    <>
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <p>{t("gzl.validation_portal.history_desc")}</p>
        <Link
          href={allReportsHref}
          className="inline-flex min-w-max items-center justify-center whitespace-nowrap rounded-md border border-blue px-3 py-1.5 text-sm font-medium text-blue hover:bg-lightgrey"
        >
          {t("gzl.validation_portal.access_all_reports")}
        </Link>
      </div>
      <div className="overflow-x-auto border border-lightpurple rounded-lg">
        <table className="min-w-full divide-y divide-gray-200">
          <thead>
            <tr>
              <th className="px-4 py-3 text-left font-semibold">{t("gzl.validation_portal.history.execution_date")}</th>
              <th className="px-4 py-3 text-left font-semibold">{t("gzl.validation_portal.history.result")}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-grey">
            {historyData.map((entry) => {
              const reportHref = `/validation-portal/reports/${entry.reportId}` as Route;
              return (
                <HistoryRow
                  key={entry.reportId}
                  entry={entry}
                  reportHref={reportHref}
                  formatDate={formatDate}
                  tooltipText={t("gzl.validation_portal.view_report_tooltip")}
                  linkTitle={t("gzl.validation_portal.open_report")}
                />
              );
            })}
          </tbody>
        </table>
      </div>
    </>
  );
}
