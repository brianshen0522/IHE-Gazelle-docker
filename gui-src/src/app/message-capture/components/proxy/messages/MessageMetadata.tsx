import { FC } from "react";
import DicomOverview from "@message-capture/components/proxy/messages/dicom/DicomOverview";
import ConnectionErrorOverview from "@message-capture/components/proxy/error/ConnectionErrorOverview";
import Hl7Overview from "@message-capture/components/proxy/messages/hl7/Hl7Overview";
import HttpOverview from "@message-capture/components/proxy/messages/http/HttpOverview";
import SyslogOverview from "@message-capture/components/proxy/messages/syslog/SyslogOverview";
import TcpOverview from "@message-capture/components/proxy/messages/tcp/TcpOverview";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { Card } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";

// Available overview components for each message type
const components: { [key: string]: FC<DataMessageProps> } = {
  HTTP_REQUEST: HttpOverview,
  HTTP_RESPONSE: HttpOverview,
  HTTP_MESSAGE: HttpOverview,
  HL7V2_MESSAGE: Hl7Overview,
  SYSLOG_MESSAGE: SyslogOverview,
  DICOM_MESSAGE: DicomOverview,
  TCP_MESSAGE: TcpOverview,
  CONNECTION_ERROR: ConnectionErrorOverview,
};

const MessageMetadata = ({ data }: DataMessageProps) => {
  const { t } = useTranslation();
  if (!data) return <div>{t("gzl.message.capture.loading_message_metadata")}...</div>;
  const { content } = data;
  const Component = components[content.type] || components[data.type];

  return Component ? (
    <Card id="message-overview" title={t("gzl.message.capture.overview")} className="min-w-1/3">
      <Component data={data} />
    </Card>
  ) : (
    <div>{t("gzl.message.capture.message_type_currently_not_supported")}</div>
  );
};

export default MessageMetadata;
