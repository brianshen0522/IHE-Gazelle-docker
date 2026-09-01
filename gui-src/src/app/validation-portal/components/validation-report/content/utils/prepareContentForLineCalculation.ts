import { base64ToUtf8 } from "@/app/message-capture/utils/base64ToUtf8";
import { parseHl7v2Xml } from "@shared/components/renderers/xml/parseHl7Xml";
import { parseDicomXml } from "@shared/components/renderers/xml/parseDicomXml";
import { parseHttpXml } from "@shared/components/renderers/xml/parseHttpXml";
import { parseDicomJson } from "@shared/components/renderers/json/parseDicomJson";
import { parseHttpJson } from "@shared/components/renderers/json/parseHttpJson";
import { parseHl7v2Json } from "@shared/components/renderers/json/parseHl7v2Json";
import { parseSyslogJson } from "@shared/components/renderers/json/parseSyslogJson";
import format from "xml-formatter";

// Transforms XML content based on its dataType to match the transformations applied in the XmlRenderer.
// This ensures that line number calculations based on the transformed content will align with what is displayed in the renderer.
function transformXmlContent(base64Data: string, dataType?: string, contentType?: string): string {
  if (!dataType) {
    return base64ToUtf8(base64Data);
  }

  switch (dataType) {
    case "DICOM":
      return parseDicomXml(base64Data);
    case "HTTP":
      return parseHttpXml(base64Data, contentType ?? "");
    case "HL7v2":
      return parseHl7v2Xml(base64Data);
    case "SYSLOG":
    default:
      return base64ToUtf8(base64Data);
  }
}

/**
 * Prepares content for line number calculation by applying the same transformations
 * that will be applied in the renderer. This ensures line numbers match the displayed content.
 *
 * @param base64Data - The base64-encoded content
 * @param renderer - The renderer type ("XML", "RAW", "JSON", "HEX")
 * @param dataType - The data type (e.g., "HL7v2", "DICOM", "HTTP")
 * @param contentType - The content type (for HTTP XML)
 * @returns Base64-encoded content ready for line calculation, or undefined if transformation failed
 */
export function prepareContentForLineCalculation(base64Data: string, renderer: string, dataType?: string, contentType?: string): string | undefined {
  if (renderer === "XML") {
    try {
      const xmlStr = transformXmlContent(base64Data, dataType, contentType);

      const prettifiedXml = format(xmlStr, {
        indentation: "  ",
        lineSeparator: "\r\n", // Must match XmlRenderer.tsx
      });

      // Return as base64 so it can be decoded by the line finder functions
      return btoa(prettifiedXml);
    } catch (error) {
      console.error("Failed to prepare XML content for line calculation:", error);
      return base64Data;
    }
  }

  if (renderer === "JSON" && dataType) {
    try {
      let jsonStr = "";

      switch (dataType) {
        case "DICOM":
          jsonStr = parseDicomJson(base64Data);
          break;
        case "HTTP":
          jsonStr = parseHttpJson(base64Data);
          break;
        case "HL7v2":
          jsonStr = parseHl7v2Json(base64Data);
          break;
        case "SYSLOG":
          jsonStr = parseSyslogJson(base64Data);
          break;
        default: {
          // For unknown types, try to parse and prettify as generic JSON
          const decoded = base64ToUtf8(base64Data);
          try {
            const parsed = JSON.parse(decoded);
            jsonStr = JSON.stringify(parsed, null, 2);
          } catch {
            jsonStr = decoded;
          }
          break;
        }
      }

      // Return as base64 so it can be decoded by the line finder functions
      return btoa(jsonStr);
    } catch (error) {
      console.error("Failed to prepare JSON content for line calculation:", error);
      return base64Data;
    }
  }

  // For other renderers, use the original content
  return base64Data;
}
