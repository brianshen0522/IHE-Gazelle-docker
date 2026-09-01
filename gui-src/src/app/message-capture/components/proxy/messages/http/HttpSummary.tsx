import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { MsgOverviewProps } from "@/app/message-capture/components/proxy/Types";
import { HttpContentData } from "./Types";
import { useTranslation } from "react-i18next";

const HttpSummary = ({ selectedRow }: MsgOverviewProps) => {
  const { t } = useTranslation();
  const { content = {} } = selectedRow || {};
  const formatDate = useDateFormat(false);
  const messageType = content.type === "HTTP_REQUEST" ? "Request" : "Response";
  const isErrorMessage = content.unexpectedErrors?.list?.length > 0;

  type MapContent = HttpContentData;

  const dataMap = [
    {
      label: t("gzl.message.capture.capture_date"),
      value: (content: MapContent) => formatDate(content.captureDate),
      show: (messageType === "Request" || messageType === "Response") && selectedRow.type !== "CONNECTION_ERROR",
    },
    {
      label: t("gzl.message.capture.method"),
      value: (content: MapContent) => content.method,
      show: messageType === "Request" && !isErrorMessage,
    },
    {
      label: t("gzl.user.interface.status"),
      value: (content: MapContent) => content.status,
      show: messageType === "Response" && !isErrorMessage,
    },
    {
      label: "URL",
      value: (content: MapContent) => content.uri,
      show: messageType === "Request" || (messageType === "Response" && !isErrorMessage),
    },
  ];

  return (
    <article className="flex flex-col w-full gap-4">
      <section className="flex justify-between">
        <div>
          {dataMap
            .filter((item) => item.show)
            .map((item) => (
              <InfoRow key={item.label} label={item.label} value={item.value(content)} />
            ))}
        </div>
      </section>
    </article>
  );
};

export default HttpSummary;
