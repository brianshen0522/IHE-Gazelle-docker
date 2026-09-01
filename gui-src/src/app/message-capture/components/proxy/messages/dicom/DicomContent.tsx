import { useState } from "react";
import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import DicomCommandSet from "@message-capture/components/proxy/messages/dicom/DicomCommandSet";
import TabRenderers from "@message-capture/components/proxy/renderers/TabRenderers";
import DownloadContentButton from "@message-capture/components/proxy/messages/DownloadContentButton";
import { useGetAttachmentAsBase64 } from "@message-capture/hooks/swr-requests/useGetAttachmentB64";
import { DataMessageProps, ReferenceType } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";
import { SectionTitle } from "@gazelle/gazelle-component-ui";

const DicomContent = ({ data }: DataMessageProps) => {
  const { t } = useTranslation();
  const { id, content } = data;
  const [showRenderer, setShowRenderer] = useState<string>("text dump");
  const attachmentId = data?.references.find((ref: ReferenceType) => ref.refType === "ATTACHMENT")?.value as string;
  const isDecoderError = !!data?.content?.unexpectedErrors?.rootType;
  const { data: attachmentImage, isError } = useGetAttachmentAsBase64({ fetchSectionId: id, itemId: id, attachmentId });
  const handleRendererChange = (renderer: string) => {
    setShowRenderer(renderer);
  };

  if (isError) return <p className="text-red">{t("gzl.message.capture.failed_to_fetch_dicom_attachment")}</p>;

  return (
    <>
      <div className="flex justify-end gap-2">
        <DownloadContentButton content={content} messageId={data?.id} attachmentId={attachmentId ?? ""} />
      </div>

      {!isDecoderError && (
        <>
          <TabRenderers specificRenderers={["text dump", "hex dump"]} showRenderer={showRenderer} onRendererChange={handleRendererChange} />
          {showRenderer !== "tree" && (
            <>
              <SectionTitle id="dicom-command-set" title={t('gzl.message.capture.command_set')} />
              <DicomCommandSet data={data} showRenderer={showRenderer} />
            </>
          )}

          {content?.dumpDataSet !== null && <SectionTitle id="dicom-dataset" title={t('gzl.message.capture.dataset')} />}
          {content?.dumpDataSet !== null && showRenderer === "text dump" && <RawRenderer base64Data={content.dumpDataSet} dataType="DICOM" />}
          {showRenderer === "hex dump" && <HexRenderer base64Data={attachmentImage ?? undefined} dataType="DICOM" />}
        </>
      )}
    </>
  );
};

export default DicomContent;
