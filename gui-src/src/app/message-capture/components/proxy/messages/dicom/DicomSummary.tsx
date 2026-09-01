import { MsgOverviewProps } from "@/app/message-capture/components/proxy/Types";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { InfoRow } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";

const DicomSummary = ({ selectedRow }: MsgOverviewProps) => {
  const { t } = useTranslation();
  const { content = {} } = selectedRow || {};
  const { captureDate, commandField, affectedSopClassUid, status } = content;
  const formatDate = useDateFormat(false);
  const isErrorMessage = content.unexpectedErrors?.list?.length > 0;

  const dataMap = [
    {
      label: t("gzl.message.capture.capture_date"),
      value: formatDate(captureDate),
      show: selectedRow.type !== "CONNECTION_ERROR",
    },
    {
      label: t("gzl.message.capture.command_field"),
      value: commandField,
      show: true,
    },
    {
      label: t("gzl.message.capture.affected_sop_class_uid"),
      value: affectedSopClassUid,
      show: !isErrorMessage,
    },
    {
      label: t("gzl.user.interface.status"),
      value: status,
      show: !isErrorMessage && status,
    },
  ];

  return (
    <article className="flex flex-col w-full gap-4">
      <section className="flex flex-col justify-between">
        {dataMap
          .filter((item) => item.show)
          .map((item) => (
            <InfoRow key={item.label} label={item.label} value={item.value} />
          ))}
      </section>
    </article>
  );
};

export default DicomSummary;
