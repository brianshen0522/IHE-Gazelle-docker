import ConnectionError from "@message-capture/components/proxy/error/ConnectionError";
import { InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const ConnectionErrorOverview = ({ data }: DataMessageProps) => {
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);
  const { content = {} } = data as { content: { [key: string]: any } };
  const isErrorMessage = content.unexpectedErrors?.list?.length > 0 || data?.type === "CONNECTION_ERROR";

  return (
    <>
      <InfoRow label={t("gzl.message.capture.capture_date")} value={formatDate(content.captureDate)} />
      {isErrorMessage && <ConnectionError selectedRow={content} />}
    </>
  );
};

export default ConnectionErrorOverview;
