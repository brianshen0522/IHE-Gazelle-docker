import { useState } from "react";
import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import { TreeRenderer } from "@message-capture/components/proxy/renderers/tree/TreeRenderer";
import { XmlRenderer } from "@shared/components/renderers/xml/XmlRenderer";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import DownloadContentButton from "@message-capture/components/proxy/messages/DownloadContentButton";
import TabRenderers from "@message-capture/components/proxy/renderers/TabRenderers";

const Hl7Content = ({ data }: DataMessageProps) => {
  const [showRenderer, setShowRenderer] = useState<string>("raw");
  const isDecoderError = !!data?.content?.unexpectedErrors?.rootType;

  const handleRendererChange = (renderer: string) => {
    setShowRenderer(renderer);
  };

  return (
    <>
      <div className="flex justify-end gap-2">
        <DownloadContentButton content={data.content} messageId={data.id} attachmentId={""} />
      </div>

      {!isDecoderError && (
        <>
          <TabRenderers specificRenderers={["raw", "hex", "xml", "tree"]} showRenderer={showRenderer} onRendererChange={handleRendererChange} />
          {showRenderer === "raw" && <RawRenderer base64Data={data.content.hl7Message} dataType="HL7v2" />}
          {showRenderer === "hex" && <HexRenderer base64Data={data.content.hl7Message} dataType="HL7v2" />}
          {showRenderer === "xml" && <XmlRenderer base64Data={data.content.hl7Message} dataType="HL7v2" />}
          {showRenderer === "tree" && <TreeRenderer node={data.content.hl7Message} dataType="HL7v2" />}
        </>
      )}
    </>
  );
};
export default Hl7Content;
