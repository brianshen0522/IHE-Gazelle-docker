import { InfoRow } from "@gazelle/gazelle-component-ui";
import { MsgOverviewProps, SyslogMessage } from "@/app/message-capture/components/proxy/Types";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { useTranslation } from "react-i18next";

const SyslogSummary = ({ selectedRow }: MsgOverviewProps) => {
  const formatDate = useDateFormat(false);
  const { content = {} } = selectedRow || {};
  const isErrorMessage = content.unexpectedErrors?.list?.length > 0;
  type MapContent = SyslogMessage["content"];
  const { t } = useTranslation();

  const dataMap = [
    {
      label: t("gzl.message.capture.capture_date"),
      value: (content: MapContent) => formatDate(content.captureDate),
      show: selectedRow.type !== "CONNECTION_ERROR",
    },
    {
      label: t("gzl.message.capture.application_name"),
      value: (content: MapContent) => content.appName,
      show: !isErrorMessage,
    },
    {
      label: t("gzl.message.capture.message_id"),
      value: (content: MapContent) => content.messageId,
      show: !isErrorMessage,
    },
  ];

  return (
    <article className="flex flex-col w-full gap-4">
      <section className="flex flex-col justify-between">
        {dataMap
          .filter((item) => item.show)
          .map((item) => (
            <InfoRow key={item.label} label={item.label} value={item.value(content)} />
          ))}
      </section>
    </article>
  );
};

export default SyslogSummary;
