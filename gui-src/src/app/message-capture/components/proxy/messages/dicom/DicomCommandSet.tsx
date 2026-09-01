import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import { RenderersConfig } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";
import { DicomCommandSetProps } from "./Types";

const DicomCommandSet = ({ data, showRenderer }: DicomCommandSetProps) => {
  const {
    commandSet,
    channelType,
    dumpCommandSet,
    requestedSopClassName,
    requestedSopClassUid,
    status,
    messageId,
    captureDate,
    commandField,
    affectedSopClassName,
    affectedSopClassUid,
    commandDataSetType,
  } = data?.content || {};
  const { t } = useTranslation();

  const fallbackCommandSetDump = [
    `Capture Date: ${captureDate ?? ""}`,
    `Command Field: ${commandField ?? ""}`,
    `Affected SOP Class Name: ${affectedSopClassName ?? ""}`,
    `Affected SOP Class UID: ${affectedSopClassUid ?? ""}`,
    `Requested SOP Class Name: ${requestedSopClassName ?? ""}`,
    `Requested SOP Class UID: ${requestedSopClassUid ?? ""}`,
    `Command Data Set Type: ${commandDataSetType ?? ""}`,
    `Message Id: ${messageId ?? ""}`,
    `Status: ${status ?? ""}`,
  ].join("\n");

  const rendererMapping: RenderersConfig = {
    "text dump": <RawRenderer base64Data={dumpCommandSet ?? fallbackCommandSetDump} dataType={channelType} />,
    "hex dump": <HexRenderer base64Data={commandSet} dataType={channelType} />,
  };

  return <>{rendererMapping[showRenderer] || <div> {t("gzl.message.capture.renderer_currently_not_supported")}</div>}</>;
};

export default DicomCommandSet;
