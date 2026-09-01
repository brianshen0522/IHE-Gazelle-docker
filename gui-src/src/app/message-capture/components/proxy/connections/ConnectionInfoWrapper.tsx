import ConnectionDetails from "@message-capture/components/proxy/connections/ConnectionDetails";
import HostType from "@message-capture/components/proxy/connections/HostType";
import { Card } from "@gazelle/gazelle-component-ui";
import useIdentifyReqRes from "@message-capture/hooks/useInitiatorResponderIdentifier";
import { useTranslation } from "react-i18next";
import { ConnectionInfoWrapperProps } from "./Types";

const ConnectionInfoWrapper = ({ connectionItem, messageItem }: ConnectionInfoWrapperProps) => {
  // Identify the initiator and responder
  const arrowDirection = useIdentifyReqRes(messageItem);
  const { t } = useTranslation();

  return (
    <div className="flex flex-col md:flex-row gap-4 justify-between md:items-stretch">
      <Card id="connection-detail" title={t("gzl.message.capture.connection_detail")} cardWrapperClassName="min-w-[25%] bg-grey-50">
        <ConnectionDetails data={messageItem} connection={connectionItem} />
      </Card>
      <Card id="initiator" title={t("gzl.message.capture.initiator")} cardWrapperClassName="min-w-[25%] bg-grey-50">
        <HostType data={messageItem} hostType="initiator" connection={connectionItem} />
      </Card>
      <div className="flex items-center">{arrowDirection}</div>
      <Card id="responder" title={t("gzl.message.capture.responder")} cardWrapperClassName="min-w-[25%] bg-grey-50">
        <HostType data={messageItem} hostType="responder" connection={connectionItem} />
      </Card>
    </div>
  );
};

export default ConnectionInfoWrapper;
