import { useDatahouseAttachment } from "@hooks/useDatahouseAttachment";
import { Download, ExternalLink } from "lucide-react";
import React, { JSX } from "react";
import {useTranslation} from "react-i18next";

/**
 * Returns the MIME type based on the attachment type.
 * @param attachmentType The type of the attachment (e.g., "json", "xml", "txt", "jpg", "png", "pdf").
 * @returns The corresponding MIME type as a string. Defaults to "application/octet-stream" if the type is unknown or not provided.
 */
export const getMimeType = (attachmentType: string): string => {
  if (attachmentType) {
    switch (attachmentType.toLowerCase()) {
      case "json":
        return "application/json";
      case "xml":
        return "application/xml";
      case "txt":
      case "text":
        return "text/plain";
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "png":
        return "image/png";
      case "pdf":
        return "application/pdf";
      default:
        return "application/octet-stream";
    }
  }
  return "application/octet-stream";
};

/**
 * Returns the file extension based on the attachment type.
 * @param attachmentType The type of the attachment (e.g., "json", "xml", "txt", "jpg", "png", "pdf").
 * @returns The corresponding file extension as a string. Returns an empty string if the type is unknown or not provided.
 */
const getFileExtension = (attachmentType: string): string => {
  if (attachmentType) {
    switch (attachmentType.toLowerCase()) {
      case "json":
        return ".json";
      case "xml":
        return ".xml";
      case "txt":
      case "text":
        return ".txt";
      case "jpg":
      case "jpeg":
        return ".jpg";
      case "png":
        return ".png";
      case "pdf":
        return ".pdf";
      default:
        return "";
    }
  }
  return "";
};

const isAPdf = (fileType: string): boolean => {
  return fileType?.toLowerCase() === "pdf";
};

/**
 * Handles the download of an attachment.
 * Creates a Blob from the data, generates a URL, and triggers the download or opens it in a new tab if it's a PDF.
 * @param file The file data as a Blob.
 * @param fileName The desired name for the downloaded file (without extension).
 * @param fileType The type of the file (e.g., "json", "xml", "txt", "jpg", "png", "pdf").
 */
export const handleDownloadFile = (file: Blob, fileName: string, fileType: string) => {
  const blob = new Blob([file], { type: getMimeType(fileType) });
  const url = URL.createObjectURL(blob);

  if (isAPdf(fileType)) {
    window.open(url, "_blank");
  } else {
    const a = document.createElement("a");
    a.href = url;
    a.download = fileName + getFileExtension(fileType);
    document.body.appendChild(a);
    a.click();
    a.remove();
  }
  setTimeout(() => URL.revokeObjectURL(url), 100);
};

export const ButtonDownloadAttachment = ({
  reportName,
  itemId,
  attachmentId,
  attachmentType,
}: {
  reportName: string;
  itemId: string;
  attachmentId: string;
  attachmentType: string;
}): JSX.Element => {
  const { t } = useTranslation();
  const { data } = useDatahouseAttachment({
    itemId,
    attachmentId,
    asBlob: true,
    enabled: !!(itemId && attachmentId),
  });

  return (
    <button
      id={`download_content_${attachmentId}`}
      title={t('gzl.message.capture.download_content')}
      onClick={() => handleDownloadFile(data!, reportName, attachmentType)}
      className="flex items-center gap-1 text-blue cursor-pointer"
    >
      {reportName}
      {isAPdf(attachmentType) ? <ExternalLink size={14} /> : <Download size={14} />}
    </button>
  );
};
