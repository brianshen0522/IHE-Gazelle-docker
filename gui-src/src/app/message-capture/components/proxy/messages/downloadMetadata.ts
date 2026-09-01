import { getHttpContentType, getHttpMimeType } from "@message-capture/utils/httpHeaders";
import { Part } from "@/app/message-capture/components/proxy/messages/multiparts/Types";
import { MessageContent } from "@/app/message-capture/components/proxy/Types";

type DownloadableMessageContent = Partial<Part> &
  Partial<MessageContent> & {
    headers?: Record<string, unknown>;
  };

const DOWNLOAD_BASENAME_BY_TYPE: Record<string, string> = {
  HTTP: "HTTP_MESSAGE",
  HTTP_MESSAGE: "HTTP_MESSAGE",
  HTTP_REQUEST: "HTTP_MESSAGE",
  HTTP_RESPONSE: "HTTP_MESSAGE",
  SYSLOG_MESSAGE: "SYSLOG_MESSAGE",
  HL7V2_MESSAGE: "HL7V2_MESSAGE",
  DICOM_MESSAGE: "DICOM_MESSAGE",
};

const HTTP_MESSAGE_TYPES = new Set(["HTTP", "HTTP_MESSAGE", "HTTP_REQUEST", "HTTP_RESPONSE"]);
const TEXT_MESSAGE_TYPES = new Set(["SYSLOG_MESSAGE", "HL7V2_MESSAGE"]);

const resolveExtensionFromMimeType = (mimeType: string) => {
  if (!mimeType) return "";
  if (mimeType.includes("json")) return ".json";
  if (mimeType.includes("xml")) return ".xml";
  if (mimeType === "text/html") return ".html";
  if (mimeType.startsWith("text/")) return ".txt";
  if (mimeType === "application/pdf") return ".pdf";
  if (mimeType === "application/dicom") return ".dcm";
  if (mimeType === "application/octet-stream") return ".bin";
  if (mimeType === "image/jpeg") return ".jpg";
  if (mimeType === "image/png") return ".png";
  return "";
};

const resolveBaseName = (type?: string) => DOWNLOAD_BASENAME_BY_TYPE[type ?? ""] ?? type ?? "download";

export const resolveDownloadMimeType = (content: DownloadableMessageContent, fetchedMimeType?: string) => {
  if (fetchedMimeType) return getHttpMimeType(fetchedMimeType);
  if (HTTP_MESSAGE_TYPES.has(content.type ?? "")) {
    return getHttpMimeType(content.contentType || getHttpContentType(content.headers));
  }
  if (TEXT_MESSAGE_TYPES.has(content.type ?? "")) return "text/plain";
  if (content.type === "DICOM_MESSAGE") return "application/dicom";
  return content.contentType ?? "";
};

export const resolveDownloadFilename = (content: DownloadableMessageContent, fetchedMimeType?: string) => {
  const baseName = resolveBaseName(content.type);

  if (TEXT_MESSAGE_TYPES.has(content.type ?? "")) return `${baseName}.txt`;
  if (content.type === "DICOM_MESSAGE") return `${baseName}.dcm`;
  if (HTTP_MESSAGE_TYPES.has(content.type ?? "")) {
    return `${baseName}${resolveExtensionFromMimeType(resolveDownloadMimeType(content, fetchedMimeType))}`;
  }

  return baseName;
};
