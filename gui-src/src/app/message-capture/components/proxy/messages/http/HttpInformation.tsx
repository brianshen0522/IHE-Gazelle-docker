import DownloadContentButton from "@message-capture/components/proxy/messages/DownloadContentButton";
import { useTranslation } from "react-i18next";
import { ReferenceType, DataMessageProps } from "@/app/message-capture/components/proxy/Types";

const HttpInformation = ({ data }: DataMessageProps) => {
  const { t } = useTranslation();
  const { content, references } = data ?? { content: {}, references: [] };
  const { HTTP_REWRITE, HTTP_VALIDATION, VALIDATION_FAILED, VALIDATION_REPORT_FOUND } = content?.additionalParameters ?? {};

  const attachmentId = references.find((ref: ReferenceType) => ref.refType === "ATTACHMENT")?.value as string;
  const rewriteEnabled = HTTP_REWRITE === "true";
  const validationEnabled = HTTP_VALIDATION === "true";
  const isValidationFailed = VALIDATION_FAILED === "true";
  const isValidationReport = VALIDATION_REPORT_FOUND === "true";
  const isErrorMessage = validationEnabled && (isValidationFailed === true || isValidationReport === false) && content.type === "HTTP_REQUEST";
  const noValidationFoundMessage = t("gzl.message.capture.no_validation_profile_matches");
  const headerHasBeenRewritten = t("gzl.message.capture.header_has_been_rewritten");

  const downloadContent = content;

  return (
    <div className="flex flex-col gap-2">
      {rewriteEnabled && (
        <p className="text-orange border-2 border-yellow rounded-lg p-2">
          {t("gzl.message.capture.header_host_forwarded_host_forwarded_for_rewritten")}
        </p>
      )}
      {isErrorMessage && (
        <p className="text-red border-2 border-red rounded-lg p-2">
          {t("gzl.message.capture.validation_failed_or_not_performed_incoming_message_not_sent")}
        </p>
      )}
      {validationEnabled && (
        <p className="text-orange border-2 border-orange rounded-lg p-2">{isValidationReport ? headerHasBeenRewritten : noValidationFoundMessage}</p>
      )}
      <div className="flex items-center justify-end">
        <DownloadContentButton content={downloadContent} messageId={data?.id} attachmentId={attachmentId ?? ""} />
      </div>
    </div>
  );
};

export default HttpInformation;
