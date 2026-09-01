import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";

const DecoderErrorContent = ({ data }: DataMessageProps) => {
  return <HexRenderer base64Data={data?.content?.content} dataType={data?.channelType} />;
};

export default DecoderErrorContent;
