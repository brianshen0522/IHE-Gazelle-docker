import React, { useState } from "react";
import DownloadContentButton from "@message-capture/components/proxy/messages/DownloadContentButton";
import TabRenderers from "@message-capture/components/proxy/renderers/TabRenderers";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { XmlRenderer } from "@shared/components/renderers/xml/XmlRenderer";
import { DataMessageProps } from "@/app/message-capture/components/proxy/Types";
import { processSyslogData } from "@/app/message-capture/utils/processSyslogData";
import { useTranslation } from "react-i18next";
import { SectionTitle } from "@gazelle/gazelle-component-ui";

const SyslogContent = ({ data }: DataMessageProps) => {
  const { header } = processSyslogData(data.content.content);
  const isDecoderError = !!data?.content?.unexpectedErrors?.rootType;
  const [showRenderer, setShowRenderer] = useState<string>("raw");
  const handleRendererChange = (renderer: string) => {
    setShowRenderer(renderer);
  };
  const payload = data.content.payload;
  const { t } = useTranslation();

  return (
    <>
      <div className="flex justify-end gap-2">
        <DownloadContentButton content={data.content} messageId={data.id} attachmentId="" />
      </div>
      {!isDecoderError && (
        <>
          <div className="flex flex-col gap-2">
            <SectionTitle id="syslog-header" title="Header" />
            <div>{header}</div>
          </div>
          {/* TODO: to be implemented when backend ready to display structured data */}
          <SectionTitle id="syslog-structured-data" title={t("gzl.message.capture.structured_data")} />
          <div>{t("gzl.message.capture.no_structured_data_enclosed")}</div>
          <SectionTitle id="syslog-msg" title="MSG" />
          <TabRenderers specificRenderers={["raw", "hex", "xml"]} showRenderer={showRenderer} onRendererChange={handleRendererChange} />
          {showRenderer === "raw" && <RawRenderer xmlData={payload} dataType="SYSLOG" />}
          {showRenderer === "hex" && <HexRenderer base64Data={payload} dataType="SYSLOG" />}
          {showRenderer === "xml" && <XmlRenderer xmlData={payload} dataType="SYSLOG" />}
        </>
      )}
    </>
  );
};
export default SyslogContent;
