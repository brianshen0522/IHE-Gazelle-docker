import React from "react";
import { CopyURL, InfoRow } from "@gazelle/gazelle-component-ui";
import MessageError from "@message-capture/components/proxy/error/MessageError";
import TcpSummary from "@message-capture/components/proxy/messages/tcp/TcpSummary";
import SyslogSummary from "@message-capture/components/proxy/messages/syslog/SyslogSummary";
import DicomSummary from "@message-capture/components/proxy/messages/dicom/DicomSummary";
import Hl7Summary from "@message-capture/components/proxy/messages/hl7/Hl7Summary";
import { useGetConnectionIndexes } from "@message-capture/hooks/swr-requests/useGetConnectionIndexes";
import { MsgOverviewPropsConnect } from "@/app/message-capture/components/proxy/Types";
import HttpSummary from "@message-capture/components/proxy/messages/http/HttpSummary";
import ConnectionError from "@message-capture/components/proxy/error/ConnectionError";
import { useSession } from "next-auth/react";
import getHeaderSizeWithoutSeparators from "@/app/message-capture/utils/httpSizeCalculator";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";

const MessageOverview = ({ selectedRow, id, connectionId }: MsgOverviewPropsConnect) => {
  const { data: session } = useSession();
  const { content = {} } = selectedRow || {};
  const isErrorMessage = content.unexpectedErrors?.list?.length > 0;
  const isConnectionError = selectedRow?.type === "CONNECTION_ERROR";
  const { data } = useGetConnectionIndexes(connectionId, session);
  const dataArray = Array.isArray(data) ? data : [];
  const selectedObjectIndex = dataArray.findIndex((obj: { id: string | null }) => obj.id === id);
  const currentIndex = selectedObjectIndex + 1;
  const accessControlList = selectedRow?.accessControlList as AccessControlList;
  const bodySize = content.bodySize ? content.bodySize : content.unexpectedErrors ? getHeaderSizeWithoutSeparators(content.content) : 0;
  const headersSize = content.headerSize ? content.headerSize : content.unexpectedErrors ? getHeaderSizeWithoutSeparators(content.headers) : 0;
  const { t } = useTranslation();

  const renderSummary = () => {
    switch (content.channelType) {
      case "HTTP":
        return <HttpSummary selectedRow={selectedRow} />;
      case "DICOM":
        return <DicomSummary selectedRow={selectedRow} />;
      case "HL7v2":
        return <Hl7Summary selectedRow={selectedRow} />;
      case "SYSLOG":
        return <SyslogSummary selectedRow={selectedRow} />;
      case "RAW_TCP":
        return <TcpSummary selectedRow={selectedRow} />;
      default:
        return null;
    }
  };

  const buildMessageUrl = () => {
    const selectedRowId = selectedRow?.id ?? "";
    const readAccessKeyParam = accessControlList?.readAccessKey ? `&readAccessKey=${accessControlList.readAccessKey}` : "";
    const basePath = "/gazelle/message-capture";
    return `${globalThis.location.origin}${basePath}/message?id=${selectedRowId}&connectionId=${connectionId}${readAccessKeyParam}`;
  };

  return (
    <article className="flex flex-col w-full gap-4">
      <CopyURL
        urlBuilder={buildMessageUrl}
        label={t("gzl.message.capture.permanent_link")}
        title={t("gzl.message.capture.copy_link_to_message_page_details_validation")}
        onCopySuccess={() => toast.success(t("gzl.message.capture.url_copied_to_clipboard"))}
        onCopyError={() => toast.error(t("gzl.message.capture.failed_to_copy_url"))}
      />

      {!isErrorMessage && <InfoRow label={t("gzl.message.capture.index_in_connection")} value={currentIndex > 0 ? currentIndex : ""} />}
      <section className="flex justify-between">{renderSummary()}</section>

      {isConnectionError && <ConnectionError selectedRow={content} />}
      {isErrorMessage && !isConnectionError && <MessageError selectedRow={selectedRow} />}

      {!isConnectionError && content.channelType === "HTTP" && (
        <div className="flex flex-col">
          <div className="flex flex-col">
            <InfoRow label={t("gzl.message.capture.header_size")} value={`${headersSize} ` + t("gzl.message.capture.bytes")} />
            {<InfoRow label={t("gzl.message.capture.body_size")} value={`${bodySize} ` + t("gzl.message.capture.bytes")} />}
          </div>
        </div>
      )}

      {!isErrorMessage && content.channelType === "DICOM" && content.sizeOfDataset > 0 && (
        <InfoRow label={t("gzl.message.capture.size_of_dataset_bytes")} value={content.sizeOfDataset} />
      )}

      {!isErrorMessage && content.channelType === "HL7v2" && (
        <InfoRow label={t("gzl.message.capture.size")} value={`${content.sizeOfMessage} ` + t("gzl.message.capture.bytes")} />
      )}
    </article>
  );
};

export default MessageOverview;
