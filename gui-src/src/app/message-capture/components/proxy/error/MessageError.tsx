import { MsgOverviewProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const MessageError = ({ selectedRow }: MsgOverviewProps) => {
  const { t } = useTranslation();
  const { content = {} } = selectedRow || {};
  const errorType = content.unexpectedErrors?.rootType || "";
  const errorMessage = content.unexpectedErrors?.list?.[0]?.message || "";
  const errorCauseMsg = content.unexpectedErrors?.list?.[0]?.cause?.message || "";
  const errorCause = content.unexpectedErrors?.list?.[0]?.cause?.cause || "";
  const connectionError = content?.list?.[0]?.message || selectedRow?.list?.[0]?.message || "";
  const parseErrorMessage = (message: string | Error) => {
    if (typeof message !== "string") {
      message = message.message;
    }

    const regex = /[a-zA-Z$#~!%_0-9/.]{1,200}:\s+/g;
    return message.replaceAll(regex, "").trim().substring(0, 150);
  };
  const sameMsgAndCauseMsg = parseErrorMessage(errorCauseMsg).startsWith(parseErrorMessage(errorMessage));

  return (
    <div className="flex flex-col text-red border border-red rounded-lg p-2 break-all">
      {sameMsgAndCauseMsg && errorType === "DECODER_ERROR" && <span>{t("gzl.message.capture.error_while_decoding_message")} </span>}
      {errorMessage && <span>{parseErrorMessage(errorMessage)}</span>}
      {!errorCauseMsg && !sameMsgAndCauseMsg && <span>{parseErrorMessage(errorCauseMsg)}</span>}
      {errorCause && <span>{parseErrorMessage(errorCause)}</span>}
      {connectionError && <span>{parseErrorMessage(connectionError)}</span>}
    </div>
  );
};

export default MessageError;
