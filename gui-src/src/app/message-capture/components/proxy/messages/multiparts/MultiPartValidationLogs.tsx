import { Badge, Subtitle } from "@gazelle/gazelle-component-ui";
import { FileCheck } from "lucide-react";
import { useGetValidationLogs } from "@message-capture/hooks/swr-requests/useGetValidationLogs";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { removeDonePrefix } from "@/app/message-capture/utils/labelFormatter";
import { useTranslation } from "react-i18next";

interface MultiPartValidationLogsProps {
  id: string;
  validationError: string | null;
}

// This component is used to display the validation logs for a multipart message and meant to be temporary
const MultiPartValidationLogs = ({ id, validationError }: MultiPartValidationLogsProps) => {
  const { data } = useGetValidationLogs(id);
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

  const [currentStatus] = data || [];

  return (
    <>
      {validationError && <div className="text-red">{validationError}</div>}
      {currentStatus && (
        <section className="flex flex-col gap-2">
          <Subtitle title={t("gzl.message.capture.part_validation")} />
          <div className="flex flex-col md:flex-row md:items-center gap-2">
            <Badge id={currentStatus.validationDate} variant={variant(currentStatus.status)}>
              {removeDonePrefix(currentStatus.status)}
            </Badge>
            <a
              href={currentStatus?.guiReportLocation}
              target="_blank"
              rel="noopener noreferrer"
              title="View validation report"
              className="flex items-center gap-1 hover:text-visited_link hover:underline"
            >
              <FileCheck size={16} />
              View validation report
            </a>
            <span className="hidden md:block">|</span>
            <h4 className="font-medium">{t("gzl.message.capture.validation_timestamp")}:</h4>
            <div>{formatDate(currentStatus?.validationDate ?? "")}</div>
          </div>
        </section>
      )}
    </>
  );
};

export default MultiPartValidationLogs;
