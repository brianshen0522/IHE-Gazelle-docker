import { useState, useEffect } from "react";
import { ArrowRight, ArrowLeft } from "lucide-react";
import { Item } from "@/app/message-capture/components/proxy/Types";

const useIdentifyReqRes = (item: Item) => {
  // This is use to identify the direction of the message if there is no command field for DICOM
  const [previousMessage, setPreviousMessage] = useState<Item | null>(null);

  useEffect(() => {
    if (item?.content.type === "DICOM_MESSAGE") {
      setPreviousMessage(item);
    }
  }, [item]);

  const getDicomArrow = () => {
    if (item?.content.commandField) {
      const messageTypeSuffix = item?.content?.commandField?.slice(-3);
      if (messageTypeSuffix === "-RQ") return <ArrowRight size={24} />;
      if (messageTypeSuffix === "RSP") return <ArrowLeft size={24} />;
      return null;
    }
    const senderIp = item?.content?.sender?.ip ?? null;
    const receiverIp = item?.content?.receiver?.ip ?? null;
    if (!previousMessage) return null;
    const prevSenderIp = previousMessage?.content?.sender?.ip ?? null;
    const prevReceiverIp = previousMessage?.content?.receiver?.ip ?? null;
    if (senderIp === prevReceiverIp && receiverIp === prevSenderIp && prevSenderIp !== null && prevReceiverIp !== null) {
      return <ArrowLeft size={24} />;
    }
    if (prevSenderIp !== null && prevReceiverIp !== null) {
      return <ArrowRight size={24} />;
    }
    return null;
  };

  const getHL7Arrow = () => {
    const messageTypePrefix = item?.content?.hl7MessageType?.substring(0, 3);
    if (!messageTypePrefix) return null;
    if (["QBP", "ORM", "ADT"].includes(messageTypePrefix)) return <ArrowRight size={24} />;
    if (["ACK", "RSP"].includes(messageTypePrefix)) return <ArrowLeft size={24} />;
    return null;
  };

  const getArrowDirection = () => {
    const type = item?.content.type;
    if (type === "HTTP_REQUEST" || type === "SYSLOG_MESSAGE") return <ArrowRight size={24} />;
    if (type === "HTTP_RESPONSE") return <ArrowLeft size={24} />;
    if (type === "DICOM_MESSAGE") return getDicomArrow();
    if (type === "HL7V2_MESSAGE") return getHL7Arrow();
    return null;
  };

  return getArrowDirection();
};

export default useIdentifyReqRes;
