import { useEffect } from "react";
import MessageRenderer from "@message-capture/components/proxy/messages/MessageRenderer";
import Tabs from "@message-capture/components/proxy/messages/Tabs";
import { useTab } from "@/shared/context/tabContext";
import { Item } from "@/app/message-capture/components/proxy/Types";

interface MessageItemContentProps {
  connectionItem: Record<string, any>;
  messageItem: Item;
  itemConnection: Record<string, any>;
}

const MessageItemContent = ({ itemConnection, connectionItem, messageItem }: MessageItemContentProps) => {
  const { selectedTab, setSelectedRenderer } = useTab();

  const isConnectionError = messageItem?.type === "CONNECTION_ERROR";

  useEffect(() => {
    if (connectionItem?.content.channelType === "DICOM") {
      setSelectedRenderer("hex");
    } else {
      setSelectedRenderer("raw");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [itemConnection]);

  return (
    <div className="bg-grey-50">
      <Tabs isConnectionError={isConnectionError} />
      <div className="flex flex-col w-full gap-4 mt-1 border-2 shadow-md border-lightpurple rounded-md overflow-hidden">
        <MessageRenderer data={messageItem} tab={selectedTab} />
      </div>
    </div>
  );
};

export default MessageItemContent;
