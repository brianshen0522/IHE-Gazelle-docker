import { InfoRow } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { Certificate, HostTypeProps } from "./Types";

const HostType = ({ data, hostType, connection }: HostTypeProps) => {
  const { t } = useTranslation();
  const { content = {} } = data;
  const { initiator, channelSummary } = connection?.content || {};
  const { initiatorTLSParameters, responderTLSParameters } = channelSummary || {};
  const getHostType = hostType === "initiator" ? initiator : channelSummary?.responder;
  const tlsParameters = hostType === "initiator" ? initiatorTLSParameters : responderTLSParameters;
  const certificateSubject = tlsParameters?.certificateChain?.map((cert: Certificate) => cert.subject).join(", ");

  if (!data) return <div>{t("gzl.message.capture.loading_host_details")}</div>;

  return (
    <article className="flex justify-between w-full gap-4">
      <section className="flex flex-col gap-4">
        <InfoRow label={t("gzl.message.capture.hostname")} value={getHostType?.hostname} />
        <InfoRow label={t("gzl.message.capture.ip_address")} value={getHostType?.ip} />
        <InfoRow label={t("gzl.message.capture.port")} value={getHostType?.port} />

        {content.secured && (
          <>
            {tlsParameters?.protocol && <InfoRow label={t("gzl.message.capture.tls_version")} value={tlsParameters.protocol} />}
            {tlsParameters?.cipherSuite && <InfoRow label={t("gzl.message.capture.cipher_suite")} value={tlsParameters.cipherSuite} />}
            {tlsParameters?.certificateChain && <InfoRow label={t("gzl.message.capture.certificate_subject")} value={certificateSubject} />}
          </>
        )}
      </section>
    </article>
  );
};

export default HostType;
