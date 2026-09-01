import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const DicomOverview = ({ data }: DataMessageProps) => {
  const { t } = useTranslation();
  const { content = {} } = data as { content: { [key: string]: any } };
  const formatDate = useDateFormat(false);

  const sizeOfDataset = content.sizeOfDataset || 0;

  return (
    <>
      <InfoRow label={t("gzl.message.capture.capture_date")} value={formatDate(content.captureDate)} />
      <InfoRow label={t("gzl.message.capture.size_of_dataset_bytes")} value={`${sizeOfDataset} bytes`} />
      <InfoRow label={t("gzl.message.capture.command_field")} value={content.commandField} />
      <InfoRow label={t("gzl.message.capture.affected_sop_class_uid")} value={content.affectedSopClassUid} />
      <InfoRow label={t("gzl.message.capture.requested_sop_class_uid")} value={content.requestedSopClassUid} />
      <InfoRow label={t("gzl.message.capture.message_id")} value={content.messageId} />
      <InfoRow label={t("gzl.user.interface.status")} value={content.status} />
      <InfoRow label={t("gzl.message.capture.command_data_set_type")} value={content.commandDataSetType} />
    </>
  );
};

export default DicomOverview;
