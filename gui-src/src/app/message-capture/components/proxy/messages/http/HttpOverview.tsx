import React from "react";
import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { HttpContentData } from "./Types";
import getHeaderSizeWithoutSeparators from "@/app/message-capture/utils/httpSizeCalculator";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";
import { getHttpContentType } from "@/app/message-capture/utils/httpHeaders";

const HttpOverview = ({ data }: DataMessageProps) => {
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);

  const content = data.content as HttpContentData;
  const contentType = getHttpContentType(content.headers);
  const messageType = content.type === "HTTP_REQUEST" ? "Request" : "Response";

  type MapContent = HttpContentData;

  const bodySize = content.bodySize ? content.bodySize : content.unexpectedErrors ? getHeaderSizeWithoutSeparators(content.content) : 0;
  const headersSize = content.headerSize ? content.headerSize : content.unexpectedErrors ? getHeaderSizeWithoutSeparators(content.headers) : 0;

  const dataMap = [
    {
      label: t("gzl.message.capture.capture_date"),
      value: (content: MapContent) => formatDate(content.captureDate),
      show: messageType === "Request" || messageType === "Response",
    },
    {
      label: t("gzl.message.capture.method"),
      value: (content: MapContent) => content.method,
      show: messageType === "Request",
    },
    {
      label: t("gzl.user.interface.status"),
      value: (content: MapContent) => content.status,
      show: messageType === "Response",
    },
    {
      label: "URL",
      value: (content: MapContent) => content.uri,
      show: messageType === "Request" || messageType === "Response",
    },
    {
      label: t("gzl.user.interface.version"),
      value: (content: MapContent) => content.version,
      show: messageType === "Request" || messageType === "Response",
    },
    {
      label: "Content-type",
      value: () => contentType,
      show: messageType === "Response",
    },
  ];

  return (
    <>
      <div>
        {dataMap
          ?.filter((item) => item.show)
          ?.map((item) => (
            <InfoRow key={item.label} label={item.label} value={item.value(content)} />
          ))}
      </div>

      <div className="flex flex-col">
        <InfoRow label={t("gzl.message.capture.header_size")} value={`${headersSize} ` + t("gzl.message.capture.bytes")} />
        <InfoRow label={t("gzl.message.capture.body_size")} value={`${bodySize} ` + t("gzl.message.capture.bytes")} />
      </div>
    </>
  );
};

export default HttpOverview;
