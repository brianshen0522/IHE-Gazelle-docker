import DicomContent from "@message-capture/components/proxy/messages/dicom/DicomContent";
import DecoderErrorContent from "@message-capture/components/proxy/error/DecoderErrorContent";
import Hl7Content from "@message-capture/components/proxy/messages/hl7/Hl7Content";
import HttpContent from "@message-capture/components/proxy/messages/http/HttpContent";
import MessageError from "@message-capture/components/proxy/error/MessageError";
import SyslogContent from "@message-capture/components/proxy/messages/syslog/SyslogContent";
import TcpContent from "@message-capture/components/proxy/messages/tcp/TcpContent";
import ValidationLogs from "@message-capture/components/proxy/validation/ValidationLogs";
import { DataMessageProps, MessageRenderProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

type ComponentType = {
  [key: string]: {
    [key: string]: React.FC<DataMessageProps>;
  };
};
// Available components for each message type
const components: ComponentType = {
  HTTP_REQUEST: {
    Content: HttpContent,
    Validation: ValidationLogs,
  },
  HTTP_RESPONSE: {
    Content: HttpContent,
    Validation: ValidationLogs,
  },
  HTTP_MESSAGE: {
    Content: HttpContent,
    Validation: ValidationLogs,
  },
  HL7V2_MESSAGE: {
    Content: Hl7Content,
    Validation: ValidationLogs,
  },
  SYSLOG_MESSAGE: {
    Content: SyslogContent,
    Validation: ValidationLogs,
  },
  DICOM_MESSAGE: {
    Content: DicomContent,
    Validation: ValidationLogs,
  },
  TCP_MESSAGE: {
    Content: TcpContent,
    Validation: ValidationLogs,
  },
  DECODER_ERROR: {
    Content: DecoderErrorContent,
  },
};

const MessageRenderer = ({ data, tab }: MessageRenderProps) => {
  const { t } = useTranslation();
  if (!data) return <div>{t("gzl.message.capture.loading_message_renderer")}...</div>;

  const { content = {} } = data as { content: { [key: string]: any } };

  const Component = components[content.type]?.[tab] || components[data.type]?.[tab];
  const isErrorMessage = content.unexpectedErrors?.list?.length > 0;
  const isDecoderError = content.unexpectedErrors?.rootType === "DECODER_ERROR";

  return Component ? (
    <div className="flex flex-col gap-4 p-2 border border-lightgrey rounded-b-lg shadow-lg">
      {isErrorMessage && (
        <div className="p-2">
          <MessageError selectedRow={data} />
        </div>
      )}
      <Component data={data} />
      {isDecoderError && tab === "Content" && <DecoderErrorContent data={data} />}
    </div>
  ) : (
    <div className="bg-white p-2">{t("gzl.message.capture.message_type_currently_not_supported")}</div>
  );
};

export default MessageRenderer;
