import { CopyURL, InfoRow } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { ConnectionDetailsProps } from "./Types";

const ConnectionDetails = ({ selectedRow, data, connection }: ConnectionDetailsProps) => {
  const { t } = useTranslation();
  const { content = {}, references = [{}] } = data || selectedRow || {};
  const rewriteEnabled = content.additionalParameters?.HTTP_REWRITE === true || content.additionalParameters?.HTTP_REWRITE === "true";
  const validationEnabled = content.additionalParameters?.HTTP_VALIDATION === true || content.additionalParameters?.HTTP_VALIDATION === "true";
  const certificate = connection?.content?.channelSummary?.proxyCertificateChain || [];
  const reference = references.length > 0 ? references[0] : {};
  const connectionTimestamp = content.connectionTimestamp || content.captureDate;
  const formatDate = useDateFormat(false); // false to use flex-row date format
  const securityProtocol = content.mtls ? "mTLS" : "TLS";
  const readAccessKey = connection?.accessControlList.readAccessKey ?? "";
  const readAccessKeyQueryParam = readAccessKey ? "&readAccessKey=" + readAccessKey : "";
  const currentURL = globalThis && globalThis.location.href + readAccessKeyQueryParam;

  return (
    <article className="flex justify-between">
      <section className="flex flex-col w-full gap-2">
        <div className="flex justify-between">
          <InfoRow
            label={t("gzl.user.interface.type")}
            value={
              <span className="flex items-center gap-2">
                {content.channelType}
                {content.secured && (
                  <div
                    title={t("gzl.message.capture.secured_by") + " " + securityProtocol}
                    className="border border-purple text-purple rounded cursor-help"
                  >
                    <p className="text-tiny px-1">{securityProtocol}</p>
                  </div>
                )}
                {validationEnabled && (
                  <div
                    title={t("gzl.message.capture.header_has_been_rewritten")}
                    className="border border-purple text-purple rounded cursor-help"
                  >
                    <p className="text-tiny px-1">{t("gzl.message.capture.validation_auto")}</p>
                  </div>
                )}
                {rewriteEnabled && (
                  <div
                    title={t("gzl.message.capture.header_host_forwarded_host_forwarded_for_rewritten")}
                    className="border border-purple text-purple rounded cursor-help"
                  >
                    <p className="text-tiny px-1">{t("gzl.message.capture.http_rewrite")}</p>
                  </div>
                )}
              </span>
            }
          />
        </div>

        <InfoRow label={t("gzl.message.capture.connection_id")} value={reference.value} />
        {data && (
          <CopyURL
            label={t("gzl.message.capture.permanent_link")}
            currentURL={currentURL}
            title={t("gzl.message.capture.copy_link_to_message_page_details_validation")}
            readAccessKey={readAccessKey}
            onCopySuccess={() => toast.success(t("gzl.message.capture.url_copied_to_clipboard"))}
            onCopyError={() => toast.error(t("gzl.message.capture.failed_to_copy_url"))}
          />
        )}
        <InfoRow label={t("gzl.message.capture.proxy_port")} value={content.proxyPort} />
        <InfoRow label={t("gzl.message.capture.timestamp")} value={formatDate(connectionTimestamp)} />
        {content.secured && data && <InfoRow label={t("gzl.message.capture.certificate_subject")} value={certificate?.[0]?.subject} />}
      </section>
    </article>
  );
};

export default ConnectionDetails;
