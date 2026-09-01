import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { HL7V2Message, MsgOverviewProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const Hl7Summary = ({ selectedRow }: MsgOverviewProps) => {
  const { content = {} } = selectedRow || {};
  const formatDate = useDateFormat(false);
  const isErrorMessage = content.unexpectedErrors?.list?.length > 0;
  type MapContent = HL7V2Message["content"];
  const { t } = useTranslation();

  const dataMap = [
    {
      label: t("gzl.message.capture.capture_date"),
      value: (content: MapContent) => formatDate(content.captureDate),
      show: selectedRow.type !== "CONNECTION_ERROR",
    },
    {
      label: t("gzl.message.capture.message_type"),
      value: (content: MapContent) => content.hl7MessageType,
      show: !isErrorMessage,
    },
    {
      label: t("gzl.message.capture.hl7_version"),
      value: (content: MapContent) => content.hl7Version,
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

export default Hl7Summary;
