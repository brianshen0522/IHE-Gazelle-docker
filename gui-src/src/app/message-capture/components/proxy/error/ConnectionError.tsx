import React from "react";
import { MsgOverviewProps } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";

const ConnectionError = ({ selectedRow }: MsgOverviewProps) => {
  const connectionError = selectedRow?.list?.[0]?.message ?? "";
  const initiatorHostname = selectedRow?.initiator?.hostname ?? selectedRow?.initiator?.hostname ?? "";
  const responderHostname = selectedRow?.responder?.hostname ?? selectedRow?.responder?.hostname ?? "";
  const responderError = selectedRow?.rootType === "RESPONDER_ERROR" || "";
  const tlsError = selectedRow?.rootType === "TLS_ERROR" || "";
  const { t } = useTranslation();

  const parseResponderError = () => {
    const hostname = selectedRow?.responder?.hostname ?? selectedRow?.responder?.ip;
    return t("gzl.message.capture.could_not_establish_connection_with") + " " + `${hostname}`;
  };

  const isInitiatorSideError = connectionError.toLowerCase().includes("initiator side error");
  const isResponderSideError = connectionError.toLowerCase().includes("responder side error");

  const parseInitiatorTlsError = () => {
    return isInitiatorSideError ? t("gzl.message.capture.tls_handshake_has_failed_between_proxy") + ` ${initiatorHostname}` : "";
  };

  const parseResponderTlsError = () => {
    return isResponderSideError ? t("gzl.message.capture.tls_handshake_has_failed_between_proxy") + ` ${responderHostname}` : "";
  };

  return (
    <div className="flex flex-col text-red border border-red rounded-lg p-2 break-all">
      {tlsError && parseInitiatorTlsError() && <span>{parseInitiatorTlsError()}</span>}
      {tlsError && parseResponderTlsError() && <span>{parseResponderTlsError()}</span>}
      {responderError && <span>{parseResponderError()}</span>}
    </div>
  );
};

export default ConnectionError;
