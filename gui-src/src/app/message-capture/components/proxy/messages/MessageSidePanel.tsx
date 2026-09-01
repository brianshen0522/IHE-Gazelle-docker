import MessageOverview from "@message-capture/components/proxy/messages/MessageOverview";
import ConnectionDetails from "@message-capture/components/proxy/connections/ConnectionDetails";
import SenderReceiver from "@message-capture/components/proxy/connections/SenderReceiver";
import { SidePanel, useSidePanel } from "@gazelle/gazelle-component-ui";
import { ItemRow } from "../Types";
import {useTranslation} from "react-i18next";

const MessageSidePanel = () => {
  const { t } = useTranslation();
  const { selectedRow, isOpen, setIsOpen } = useSidePanel<ItemRow>();
  const connectionId = selectedRow?.references?.[0]?.value;
  const { sender, receiver, initiator, responder, channelType } = selectedRow?.content ?? {};
  const { hostname: senderHostname = "", ip: senderIp = "", port: senderPort = 0 } = sender ?? initiator ?? {};
  const { hostname: receiverHostname = "", ip: receiverIp = "", port: receiverPort = 0 } = receiver ?? responder ?? {};

  // If no message is selected, we don't render the side panel content at first render
  const hasContent = !!selectedRow;

  return (
    <SidePanel isOpen={isOpen} className="p-1">
      {hasContent ? (
        <>
          <SidePanel.Header
            accessDetailsProps={{
              id: selectedRow.id,
              content: "message",
              pathname: "/message-capture/message",
              query: { id: selectedRow.id, connectionId: connectionId ?? "" },
            }}
            onClose={() => setIsOpen(false)}
          />

          <SidePanel.Section id={channelType + " message"} title={channelType + " message"}>
            <MessageOverview selectedRow={selectedRow} id={selectedRow?.id ?? ""} connectionId={connectionId ?? ""} />
          </SidePanel.Section>

          <SidePanel.Section id="connection-details" title={t('gzl.message.capture.connection_details')}>
            <ConnectionDetails selectedRow={selectedRow} />
          </SidePanel.Section>

          <SidePanel.Section id="sender" title={t('gzl.message.capture.sender')}>
            <SenderReceiver hostname={senderHostname} ip={senderIp} port={senderPort} host="sender" />
          </SidePanel.Section>

          <SidePanel.Section id="receiver" title={t('gzl.message.capture.receiver')}>
            <SenderReceiver hostname={receiverHostname} ip={receiverIp} port={receiverPort} host="receiver" />
          </SidePanel.Section>
        </>
      ) : (
        <></>
      )}
    </SidePanel>
  );
};

export default MessageSidePanel;
