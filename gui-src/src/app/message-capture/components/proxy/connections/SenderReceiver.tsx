import { InfoRow } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { SenderReceiverProps } from "./Types";

const SenderReceiver = ({ hostname, ip, port }: SenderReceiverProps) => {
  const { t } = useTranslation();

  return (
    <article className="flex flex-col w-full gap-4">
      <InfoRow label={t("gzl.message.capture.hostname")} value={hostname} />
      <InfoRow label={t("gzl.message.capture.ip_address")} value={ip} />
      <InfoRow label={t("gzl.message.capture.port")} value={port} />
    </article>
  );
};

export default SenderReceiver;
