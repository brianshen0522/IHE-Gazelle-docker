import { useMemo } from "react";
import { createColumnHelper } from "@tanstack/react-table";
import { TriangleAlert } from "lucide-react";
import { textFormatter } from "@/app/message-capture/utils/labelFormatter";
import { AccessDetailsLink } from "@gazelle/gazelle-component-ui";
import { ProxyMessages } from "@/app/message-capture/components/proxy/Types";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { useMsgTypeAccessor } from "@message-capture/hooks/useMsgTypeAccessor";
import { useTranslation } from "react-i18next";

const columnHelper = createColumnHelper<ProxyMessages>();

export const MessagesColumns = () => {
  const columns = useMessagesColumns();
  return <>{columns}</>;
};

export const useMessagesColumns = () => {
  const { t } = useTranslation();
  const msgTypeAccessor = useMsgTypeAccessor();
  const formatDate = useDateFormat(false);

  // Columns definition for the table
  return useMemo(
    () => [
      columnHelper.accessor("content.channelType", {
        id: "channel_type",
        header: () => <span>{t("gzl.message.capture.standard")}</span>,
        cell: (info) => {
          const value = info.getValue();
          const securityProtocol = info.row.original.content.mtls ? "mTLS" : "TLS";
          return info.row.original.content.secured ? (
            <span className="flex items-center gap-2">
              {value}{" "}
              <div title={"Secured by " + securityProtocol} className="border border-purple text-purple rounded cursor-help">
                <p className="text-tiny px-1">{securityProtocol}</p>
              </div>
            </span>
          ) : (
            value
          );
        },
      }),
      columnHelper.accessor("content.captureDate", {
        id: "capture_date",
        header: () => <span>{t("gzl.message.capture.timestamp")}</span>,
        cell: (info) => formatDate(info.getValue()),
      }),
      columnHelper.accessor((row) => (row.type === "CONNECTION_ERROR" ? row.content?.initiator?.hostname : row.content?.sender?.hostname), {
        id: "sender_hostname",
        header: () => <span>{t("gzl.message.capture.sender")}</span>,
        cell: (info) => info.getValue() ?? "",
      }),
      columnHelper.accessor("content.proxyPort", {
        id: "proxy_port",
        header: () => <span>{t("gzl.message.capture.proxy_port")}</span>,
        cell: (info) => info.getValue(),
      }),
      columnHelper.accessor((row) => (row.type === "CONNECTION_ERROR" ? row.content?.responder?.hostname : row.content?.receiver?.hostname), {
        id: "receiver_hostname",
        header: () => <span>{t("gzl.message.capture.receiver")}</span>,
        cell: (info) => info.getValue() ?? "",
      }),
      columnHelper.accessor(msgTypeAccessor, {
        id: "message_type",
        header: () => <span>{t("gzl.message.capture.message_type")}</span>,
        cell: (info) => {
          const additionalParameters = info.row.original.content.additionalParameters;
          const messageType = Array.isArray(additionalParameters)
            ? additionalParameters.find((p) => p.key === "message_type")?.value
            : additionalParameters?.["message_type"];
          if (
            info.row.original.content.type === "HTTP_MESSAGE" ||
            info.row.original.content.unexpectedErrors ||
            info.row.original.content.rootType ||
            messageType === "Validation failed"
          ) {
            return (
              <span className="text-red flex gap-2 items-center">
                <TriangleAlert size={14} />
                {textFormatter(info.getValue())}
              </span>
            );
          } else {
            return info.getValue();
          }
        },
        enableColumnFilter: false,
      }),
      columnHelper.display({
        id: "action",
        header: () => <span>{t("gzl.message.capture.action")}</span>,
        cell: (info) => (
          <div className="flex items-center cursor-help" title="Link to message details and content validation">
            <AccessDetailsLink
              id={info.row.original.id}
              pathname="/message-capture/message"
              query={{ id: info.row.original.id, connectionId: info.row.original.references[0]?.value }}
            />
          </div>
        ),
        enableColumnFilter: false,
        enableSorting: false,
      }),
    ],
    [formatDate, msgTypeAccessor, t],
  );
};
