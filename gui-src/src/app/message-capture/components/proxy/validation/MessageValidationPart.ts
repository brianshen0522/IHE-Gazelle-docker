import { ValidationPart } from "@/app/message-capture/components/proxy/validation/Types";

const SOAP_BODY_CHILDREN_SELECTOR = "/*[local-name()='Envelope']/*[local-name()='Body']/*";

type MessageContent = Record<string, unknown> & {
  body?: string;
  headers?: Record<string, string>;
  type?: string;
};

function isContentExists(content: MessageContent, fieldName: string) {
  return content[fieldName] !== undefined && content[fieldName] !== null && content[fieldName] !== "";
}

function decodeBase64Content(content: string): string {
  try {
    if (typeof atob === "function") {
      return atob(content);
    }
  } catch {
    return "";
  }

  try {
    return Buffer.from(content, "base64").toString("utf-8");
  } catch {
    return "";
  }
}

function getHeader(headers: Record<string, string> | undefined, headerName: string): string | undefined {
  return Object.entries(headers ?? {}).find(([key]) => key.toLowerCase() === headerName.toLowerCase())?.[1];
}

function hasSoapPayload(content: MessageContent, contentType?: string): boolean {
  if (!isContentExists(content, "body")) return false;

  const normalizedContentType = contentType?.toLowerCase() ?? "";
  if (normalizedContentType.includes("soap")) return true;

  if (typeof content.body !== "string") return false;

  const decodedBody = decodeBase64Content(content.body).toLowerCase();
  return (
    decodedBody.includes(":envelope") ||
    decodedBody.includes("<envelope") ||
    decodedBody.includes("http://schemas.xmlsoap.org/soap/envelope") ||
    decodedBody.includes("http://www.w3.org/2003/05/soap-envelope")
  );
}

export const MESSAGE_VALIDATION_PART: { [key: string]: ValidationPart[] } = {
  HTTP_REQUEST: [
    {
      label: "Full payload",
      value: "fullPayload",
      path: "$.content",
      syntax: "json",
      isEnable: true,
    },
    {
      label: "Body only",
      value: "bodyOnly",
      path: "$.body",
      syntax: "json",
      isEnable: true,
    },
    {
      label: "SOAP Body",
      value: "soapBody",
      path: "$.body",
      syntax: "xml",
      selector: SOAP_BODY_CHILDREN_SELECTOR,
      isEnable: false,
    },
  ],
  HTTP_RESPONSE: [
    {
      label: "Full payload",
      value: "fullPayload",
      path: "$.content",
      syntax: "json",
      isEnable: true,
    },
    {
      label: "Body only",
      value: "bodyOnly",
      path: "$.body",
      syntax: "json",
      isEnable: true,
    },
    {
      label: "SOAP Body",
      value: "soapBody",
      path: "$.body",
      syntax: "xml",
      selector: SOAP_BODY_CHILDREN_SELECTOR,
      isEnable: false,
    },
  ],
  HL7V2_MESSAGE: [
    {
      label: "HL7 Message",
      value: "hl7Message",
      path: "$.hl7Message",
      syntax: "json",
      isEnable: true,
    },
  ],
  TCP_MESSAGE: [
    {
      label: "TCP Message",
      value: "tcpMessage",
      path: "$.content",
      syntax: "json",
      isEnable: true,
    },
  ],
  SYSLOG_MESSAGE: [
    {
      label: "Syslog MSG",
      value: "syslogMSG",
      path: "$.payload",
      syntax: "json",
      isEnable: true,
    },
  ],
  DICOM_MESSAGE: [
    {
      label: "Full payload",
      value: "fullPayload",
      path: "$.content",
      syntax: "json",
      isEnable: true,
    },
    {
      label: "Dicom Dataset",
      value: "dicomDataset",
      path: "$.content",
      syntax: "json",
      isEnable: true,
    },
    {
      label: "Dicom Command Set",
      value: "dicomCommandSet",
      path: "$.commandSet",
      syntax: "json",
      isEnable: true,
    },
  ],
  DICOM_PART: [
    {
      label: "Dicom part",
      value: "dicomPart",
      path: "$.content",
      syntax: "attachment",
      isEnable: true,
    },
  ],
  RAW_PART: [
    {
      label: "Raw part",
      value: "rawPart",
      path: "$.content",
      syntax: "attachment",
      isEnable: true,
    },
  ],
  BINARY_PART: [
    {
      label: "Binary part",
      value: "binaryPart",
      path: "$.content",
      syntax: "attachment",
      isEnable: true,
    },
  ],
  TEXT_PART: [
    {
      label: "Text part",
      value: "textPart",
      path: "$.raw",
      syntax: "json",
      isEnable: true,
    },
  ],
};

export const getMessageValidationParts = (content: MessageContent, attachmentId?: string) => {
  if (!content?.type) return [];

  const configuredParts = MESSAGE_VALIDATION_PART[content.type];
  if (configuredParts === undefined) return [];

  const parts: ValidationPart[] = configuredParts.map((part) => ({ ...part }));

  let contentType: string | undefined;
  switch (content.type) {
    case "HTTP_REQUEST":
    case "HTTP_RESPONSE":
      contentType = getHeader(content?.headers, "Content-Type");
      parts[1].isEnable = isContentExists(content, "body");
      parts[2].isEnable = hasSoapPayload(content, contentType);
      if (contentType?.toLowerCase().includes("multipart")) parts[0].syntax = "attachment";
      break;
    case "DICOM_MESSAGE":
      parts[0].isEnable = isContentExists(content, "content");
      parts[1].isEnable = isContentExists(content, "dumpDataSet");
      parts[1].syntax = attachmentId ? "attachment" : "json";
      parts[2].isEnable = isContentExists(content, "commandSet");
      break;
    case "SYSLOG_MESSAGE":
      if (!isContentExists(content, "payload")) {
        if (isContentExists(content, "content")) parts[0].path = "$.content";
        else parts[0].isEnable = false;
      }
      break;
    default:
      break;
  }
  return parts;
};
