import format from "xml-formatter";
import { base64ToUtf8 } from "@/app/message-capture/utils/base64ToUtf8";

export function parseHttpXml(base64Data: string, contentType = ""): string {
  const normalizedContentType = contentType.toLowerCase();
  const isXml = normalizedContentType.includes("xml");
  const isHtml = normalizedContentType.includes("text/html");
  // Decode the base64 string
  const decodedData = base64ToUtf8(base64Data);
  // Parse the decoded string based on content type
  const parser = new DOMParser();

  let doc;
  if (isXml) {
    doc = parser.parseFromString(decodedData, "application/xml");
  } else if (isHtml) {
    doc = parser.parseFromString(decodedData, "text/html");
  } else {
    return "Error: Unsupported content type.";
  }

  // Check for parsing errors
  const parseError = doc.getElementsByTagName("parsererror");
  if (parseError.length > 0) {
    return "Error: The provided data is not valid XML/HTML.";
  }

  // Convert document to string
  const xmlSerializer = new XMLSerializer();
  let docStr = xmlSerializer.serializeToString(doc);

  // Format the output if it's XML
  if (isXml) {
    docStr = format(docStr, { indentation: "  " });
  }

  return docStr;
}
