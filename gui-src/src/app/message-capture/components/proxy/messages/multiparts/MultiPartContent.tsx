import React, { useState } from "react";
import { MultiPartContentProps, Part } from "@/app/message-capture/components/proxy/messages/multiparts/Types";
import { currentItemType } from "@/app/message-capture/components/proxy/Types";
import { useGetAttachmentAsBase64 } from "@message-capture/hooks/swr-requests/useGetAttachmentB64";
import { getMessageValidationParts } from "@message-capture/components/proxy/validation/MessageValidationPart";
import DownloadContentButton from "@message-capture/components/proxy/messages/DownloadContentButton";
import MultiPartRenderers from "@message-capture/components/proxy/messages/multiparts/MultiPartRenderers";
import ModalValidation from "@message-capture/components/proxy/validation/ModalValidation";
import MultiPartValidationLogs from "@message-capture/components/proxy/messages/multiparts/MultiPartValidationLogs";
import { useTranslation } from "react-i18next";

const MultiPartContent = ({ content, sectionId, expandAll }: MultiPartContentProps & { expandAll: boolean }) => {
  const { unexpectedErrors, id } = content;
  const [validationError, setValidationError] = useState<string | null>(null);
  const attachmentId = content.references.find((ref) => ref.refType === "ATTACHMENT")?.value as string;
  const fetchSectionId = expandAll || sectionId === id ? sectionId : null;
  const { data, isLoading, isError } = useGetAttachmentAsBase64({ fetchSectionId, itemId: sectionId, attachmentId });
  const { t } = useTranslation();

  const getValidationParts = (content: currentItemType<Part>) => {
    return getMessageValidationParts(content).filter((part) => part.isEnable);
  };
  const handleValidationError = (error: string) => {
    setValidationError(error);
  };

  if (isLoading) return <p className="animate-pulse">Loading attachment data...</p>;
  if (isError) return <p className="text-red">Failed to fetch attachment</p>;

  return unexpectedErrors?.rootType ? (
    <section className="flex flex-col gap-2">
      <p className="text-red">{unexpectedErrors?.rootType}</p>
      <p className="text-red">
        {unexpectedErrors?.list?.[0]?.message} : {unexpectedErrors?.list?.[0]?.cause?.message}
      </p>
      <div className="flex flex-col md:flex-row md:justify-end gap-2">
        <DownloadContentButton content={content} attachmentId={attachmentId ?? ""} />
      </div>
      <MultiPartRenderers content={content} data={data ?? ""} />
    </section>
  ) : (
    <section className="flex flex-col gap-2">
      {!isLoading && (data || content.raw) && (
        <>
          <div className="flex flex-col md:flex-row md:justify-end gap-2">
            <ModalValidation
              title={t("gzl.message.capture.validate")}
              btnTriggerText="Validate"
              validationParts={getValidationParts(content)}
              partId={id}
              attachmentId={attachmentId}
              onValidationError={handleValidationError}
            />
            <DownloadContentButton content={content} attachmentId={attachmentId ?? ""} />
          </div>

          <MultiPartValidationLogs id={id} validationError={validationError} />
          <MultiPartRenderers content={content} data={data ?? ""} />
        </>
      )}
    </section>
  );
};

export default MultiPartContent;
