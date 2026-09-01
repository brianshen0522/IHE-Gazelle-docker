import { useState } from "react";
import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { XmlRenderer } from "@shared/components/renderers/xml/XmlRenderer";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import { MultiPartRenderersConfig, Part, PartType } from "@/app/message-capture/components/proxy/messages/multiparts/Types";
import { Renderers } from "@/app/message-capture/components/proxy/Types";
import { JsonRenderer } from "@shared/components/renderers/json/JsonRenderer";
import TabRenderers from "@message-capture/components/proxy/renderers/TabRenderers";

const MultiPartRenderers = ({ content, data }: { content: Part; data: string }) => {
  const { xmlDataSetDump, type, raw, syntax } = content;
  const initRenderer = type === "DICOM_PART" ? "hex dump" : type === "XOP_PART" ? "xml" : "hex";
  const [showRenderer, setShowRenderer] = useState<string>(initRenderer);

  const handleRendererChange = (renderer: string) => {
    setShowRenderer(renderer);
  };

  const renderersConfig: MultiPartRenderersConfig = {
    DICOM_PART: {
      specificRenderers: ["hex dump", "text dump", "xml dump"],
      renderers: {
        "hex dump": <HexRenderer base64Data={data} dataType="HTTP" />,
        "text dump": <RawRenderer xmlData={xmlDataSetDump} dataType="HTTP" />,
        "xml dump": <XmlRenderer xmlData={xmlDataSetDump} dataType="HTTP" contentType={content?.contentType} />,
      },
    },
    BINARY_PART: {
      specificRenderers: ["hex"],
      renderers: {
        hex: <HexRenderer base64Data={data} dataType="HTTP" />,
      },
    },
    TEXT_PART: {
      specificRenderers: ["hex", syntax as Renderers],
      renderers: {
        hex: <HexRenderer base64Data={raw} dataType="HTTP" />,
        xml: <XmlRenderer base64Data={raw} dataType="HTTP" contentType={content?.contentType} />,
        json: <JsonRenderer base64Data={raw} dataType="HTTP" />,
      },
    },
    PART: {
      specificRenderers: ["hex"],
      renderers: {
        hex: <HexRenderer base64Data={data} dataType="HTTP" />,
      },
    },
    XOP_PART: {
      specificRenderers: ["hex", "xml"],
      renderers: {
        hex: <HexRenderer base64Data={raw} dataType="HTTP" />,
        xml: <XmlRenderer base64Data={raw} dataType="HTTP" contentType={content?.contentType} />,
      },
    },
  };

  const config = renderersConfig[type as PartType];

  return (
    <>
      {config && (
        <>
          <TabRenderers specificRenderers={config.specificRenderers} showRenderer={showRenderer} onRendererChange={handleRendererChange} />
          {config.renderers[showRenderer]}
        </>
      )}
    </>
  );
};

export default MultiPartRenderers;
