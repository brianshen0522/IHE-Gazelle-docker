import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { base64ToBytesSize } from "@/app/message-capture/utils/base64BytesSize";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const Hl7Overview = ({ data }: DataMessageProps) => {
  const { content = {} } = data as { content: { [key: string]: any } };
  const formatDate = useDateFormat(false);
  const { t } = useTranslation();

  const base64String = content.content || "";

  return (
    <>
      <InfoRow label={t("gzl.message.capture.capture_date")} value={formatDate(content.captureDate)} />
      <InfoRow label={t("gzl.message.capture.message_type")} value={content.hl7MessageType} />
      <InfoRow label={t("gzl.message.capture.hl7_version")} value={content.hl7Version} />
      <InfoRow label={t("gzl.message.capture.size")} value={`${base64ToBytesSize(base64String)} ` + t("gzl.message.capture.bytes")} />
    </>
  );
};

export default Hl7Overview;
