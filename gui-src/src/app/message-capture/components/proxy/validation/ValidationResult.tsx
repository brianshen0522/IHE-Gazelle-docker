import React from "react";
import { Badge } from "@gazelle/gazelle-component-ui";
import { FileCheck } from "lucide-react";
import { useSearchParams } from "next/navigation";
import { useGetValidationLogs } from "@message-capture/hooks/swr-requests/useGetValidationLogs";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { removeDonePrefix } from "@/app/message-capture/utils/labelFormatter";
import { useTranslation } from "react-i18next";
import { ValidationResultProps } from "./Types";

const ValidationResult = (validationError: ValidationResultProps) => {
  const { t } = useTranslation();
  const searchParams = useSearchParams();
  const formatDate = useDateFormat(false);
  const id = searchParams.get("id");
  const { data: logs, isError: isLogsError, isLoading: isLogsLoading } = useGetValidationLogs(id as string);

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

  const [firstLog] = logs || [];

  if (isLogsLoading) return <div>{t("gzl.message.capture.loading_validation_result")}</div>;
  if (isLogsError) return <div>{t("gzl.message.capture.error_loading_validation_result")}</div>;

  return (
    <>
      {validationError && <div className="text-red">{validationError.validationError}</div>}
      {firstLog && (
        <div className="flex flex-col gap-2">
          <div className="flex items-center gap-2">
            <Badge id={firstLog.validationDate} variant={variant(firstLog.status)}>
              {removeDonePrefix(firstLog.status)}
            </Badge>
            <a
              href={firstLog?.guiReportLocation}
              target="_blank"
              rel="noopener noreferrer"
              title="View validation report"
              className="flex items-center gap-1 hover:text-visited_link hover:underline"
            >
              <FileCheck size={16} />
              View validation report
            </a>
          </div>
          <div className="flex gap-2">
            <p className="font-medium">{t("gzl.message.capture.validation_timestamp")}:</p>
            {formatDate(firstLog?.validationDate ?? "")}
          </div>
        </div>
      )}
    </>
  );
};

export default ValidationResult;
