import { base64Decode } from "./base64";
import { isBinaryEncodedString } from "./binary/detectBinary";
import { detectBinaryDICOM, detectTextDICOM } from "./dicom/detectDicom";
import { detectHL7v2 } from "./hl7/detectHl7";
import { isValidJson } from "./json/detectJson";
import { RenderDecision } from "./types";
import { detectXmlDataType, isValidXml } from "./xml/detectXml";

export function inspectForRendering(base64: string): RenderDecision {
  // Check for binary DICOM first (before trying to decode as text)
  if (detectBinaryDICOM(base64)) {
    return {
      renderer: "HEX",
      base64Data: base64,
      dataType: "DICOM",
    };
  }

  if (isBinaryEncodedString(base64)) {
    return {
      renderer: "HEX",
      base64Data: base64,
      dataType: "BINARY",
    };
  }

  const decoded = base64Decode(base64);

  if (!decoded) {
    return { renderer: "RAW", base64Data: base64 };
  }

  const trimmed = decoded.trim();

  // Check if content is purely binary digits (0s and 1s) - likely binary data sent as text
  if (/^[01\s]+$/.test(trimmed) && trimmed.replaceAll(/\s/g, "").length >= 32) {
    return {
      renderer: "HEX",
      base64Data: base64,
      dataType: "BINARY",
    };
  }

  // Check for DICOM text dump format
  if (detectTextDICOM(trimmed)) {
    return {
      renderer: "RAW",
      base64Data: base64,
      dataType: "DICOM",
    };
  }

  if (detectHL7v2(trimmed)) {
    return {
      renderer: "RAW",
      base64Data: base64,
      dataType: "HL7v2",
    };
  }

  // JSON
  if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
    const isSample = decoded.length >= 4096;
    const isValid = isSample ? true : isValidJson(trimmed);

    if (isValid) {
      return {
        renderer: "JSON",
        base64Data: base64,
        textPreview: decoded,
      };
    }
  }

  // XML
  if (trimmed.startsWith("<") || trimmed.startsWith("<?xml")) {
    if (isValidXml(trimmed)) {
      return {
        renderer: "XML",
        base64Data: base64,
        dataType: detectXmlDataType(decoded),
      };
    }
  }

  return { renderer: "RAW", base64Data: base64 };
}
