import React from "react";
import { Badge, CopyURL } from "@gazelle/gazelle-component-ui";
import { useSearchParams } from "next/navigation";
import { useGetValidationLogs } from "@message-capture/hooks/swr-requests/useGetValidationLogs";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { removeDonePrefix } from "@/app/message-capture/utils/labelFormatter";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { logsResults } from "./Types";

const ValidationLogs = () => {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const { data: logs, isError: isLogsError, isLoading: isLogsLoading } = useGetValidationLogs(id as string);
  const formatDate = useDateFormat(false);
  const { t } = useTranslation();

  const variant = (status: string) => {
    switch (status) {
      case "DONE_PASSED":
        return "success";
      case "DONE_FAILED":
        return "failed";
      case "DONE_UNDEFINED":
        return "warning";
      default:
        return "default";
    }
  };

  if (isLogsLoading) return <div>{t("gzl.user.interface.loading")}...</div>;
  if (isLogsError) return <div>{t("gzl.message.capture.error_loading_validation_logs")}</div>;

  return (
    <div className="flex flex-col justify-start gap-8 p-2">
      {logs?.length > 0 ? (
        <div className="flex flex-col gap-2 2xl:w-1/2">
          <h2 className="font-semibold bg-lightgrey rounded-md p-2">{t("gzl.message.capture.validation_history")}</h2>
          <div className="overflow-auto">
            <table className="table-auto w-full rounded-lg">
              <thead className="bg-lightgrey hidden md:table-header-group">
                <tr>
                  <th>{t("gzl.user.interface.status")}</th>
                  <th>{t("gzl.message.capture.report")}</th>
                  <th>{t("gzl.message.capture.permanent_link")}</th>
                </tr>
              </thead>
              <tbody>
                {logs.map(
                  (
                    item: logsResults<{
                      status: string;
                      validationDate: number;
                      guiReportLocation: string;
                    }>,
                    index: number
                  ) => (
                    <tr key={item.validationDate} className="md:table-row flex flex-col md:border-b border-lightgrey">
                      <td
                        className="before:p-2 before:content-[attr(data-label)] before:font-semibold md:before:content-none before:flex before:bg-lightgrey p-2"
                        data-label="Status"
                      >
                        {index === 0 && (
                          <Badge id={item.validationDate} variant={variant(item.status)}>
                            {removeDonePrefix(item.status)}
                          </Badge>
                        )}
                        {index !== 0 && (
                          <div className="bg-lightgrey flex text-center justify-center p-1 rounded min-w-24">{removeDonePrefix(item.status)}</div>
                        )}
                      </td>
                      <td
                        className="before:p-2 before:content-[attr(data-label)] before:font-semibold md:before:content-none before:flex before:bg-lightgrey pb-8 md:pb-0"
                        data-label="Report"
                      >
                        <a
                          href={item?.guiReportLocation}
                          target="_blank"
                          rel="noopener noreferrer"
                          title="View validation report"
                          className="flex flex-col sm:flex-row md:items-center gap-2 hover:text-visited_link hover:underline"
                        >
                          View validation report {formatDate(item.validationDate)}
                        </a>
                      </td>
                      <td
                        className="before:p-2 before:content-[attr(data-label)] before:font-semibold md:before:content-none before:flex before:bg-lightgrey pb-8 md:pb-0"
                        data-label="Copy url"
                      >
                        <CopyURL
                          currentURL={item.guiReportLocation}
                          title={t("gzl.message.capture.copy_evs_validation_report_link")}
                          onCopySuccess={() => toast.success("URL copied to clipboard")}
                          onCopyError={() => toast.error("Failed to copy URL")}
                        />
                      </td>
                    </tr>
                  )
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div>{t("gzl.message.capture.no_validation_logs_available")}</div>
      )}
    </div>
  );
};
export default ValidationLogs;
