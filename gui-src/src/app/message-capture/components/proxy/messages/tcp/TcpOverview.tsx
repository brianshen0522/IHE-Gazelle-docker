import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const TcpOverview = ({ data }: DataMessageProps) => {
  const { content = {} } = data as { content: { [key: string]: any } };
  const formatDate = useDateFormat(false);
  const { t } = useTranslation();

  return (
    <>
      <InfoRow label={t("gzl.message.capture.capture_date")} value={formatDate(content.captureDate)} />
      {content.sizeOfMessage && (
        <InfoRow label={t("gzl.message.capture.message_size")} value={`${content.sizeOfMessage} ` + t("gzl.message.capture.bytes")} />
      )}
    </>
  );
};

export default TcpOverview;
