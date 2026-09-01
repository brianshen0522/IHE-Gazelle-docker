import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const SyslogOverview = ({ data }: DataMessageProps) => {
  const { content = {} } = data as { content: { [key: string]: any } };
  const formatDate = useDateFormat(false);
  const { t } = useTranslation();

  return (
    <>
      <InfoRow label={t("gzl.message.capture.capture_date")} value={formatDate(content.captureDate)} />
      <InfoRow label={t("gzl.message.capture.timestamp")} value={content.timestamp} />
      <InfoRow label={t("gzl.message.capture.hostname")} value={content.hostName} />
      <InfoRow label={t("gzl.message.capture.application_name")} value={content.appName} />
      <InfoRow label={t("gzl.message.capture.proc_id")} value={content.procId} />
      <InfoRow label={t("gzl.message.capture.message_id")} value={content.messageId} />
      <InfoRow label={t("gzl.message.capture.facility")} value={content.facility} />
      <InfoRow label={t("gzl.message.capture.severity")} value={content.severity} />
      <InfoRow label={t("gzl.message.capture.event")} value={data.type} />
    </>
  );
};

export default SyslogOverview;
