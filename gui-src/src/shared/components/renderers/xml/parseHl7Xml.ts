// HL7Parser

// Escapes special XML characters to prevent invalid XML
function escapeXml(value: string): string {
  return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&apos;");
}

// Safely decodes base64 data, returns original if already decoded
function safelyDecodeBase64(data: string): string {
  try {
    const decoded = atob(data);
    // Check if decoded result looks like HL7 (starts with MSH)
    if (decoded.includes("MSH")) {
      return decoded;
    }
    return data;
  } catch {
    return data;
  }
}

export function parseHL7v2Binary(data: string): string[] {
  const decoded = safelyDecodeBase64(data);
  const segments = decoded.split("\r");
  return segments.filter((segment) => segment.trim() !== "");
}

// Formats a simple field with XML tags
function formatSimpleField(segmentName: string, index: number, field: string): string {
  return `\t<${segmentName}.${index}>${escapeXml(field)}</${segmentName}.${index}>\n`;
}

// Formats a field with a single subcomponent type
function formatFieldWithSubcomponent(segmentName: string, index: number, field: string, subType: string): string {
  return `\t<${segmentName}.${index}>\n\t\t<${subType}.1>${escapeXml(field)}</${subType}.1>\n\t</${segmentName}.${index}>\n`;
}

// Formats subfields with a given type prefix
function formatSubfields(segmentName: string, index: number, field: string, subType: string): string {
  const subfields = field.split("^").filter((sf) => sf !== null && sf !== "");
  if (subfields.length === 0) return "";

  let xml = `\t<${segmentName}.${index}>\n`;
  subfields.forEach((subfield, subIndex) => {
    xml += `\t\t<${subType}.${subIndex + 1}>${escapeXml(subfield)}</${subType}.${subIndex + 1}>\n`;
  });
  xml += `\t</${segmentName}.${index}>\n`;
  return xml;
}

// Handles MSH segment field formatting
function formatMSHField(segmentName: string, field: string, index: number): string {
  const fieldNum = index + 2; // MSH fields are offset by 2

  if (index >= 1 && index <= 4) {
    return formatFieldWithSubcomponent(segmentName, fieldNum, field, "HD");
  }
  if (index === 5) {
    return formatFieldWithSubcomponent(segmentName, fieldNum, field, "TS");
  }
  if (index === 7) {
    const subfields = field.split("^");
    let xml = `\t<${segmentName}.${fieldNum}>\n`;
    subfields.forEach((subfield, subIndex) => {
      xml += `\t\t<MSG.${subIndex + 1}>${escapeXml(subfield)}</MSG.${subIndex + 1}>\n`;
    });
    return xml + `\t</${segmentName}.${fieldNum}>\n`;
  }
  if (index === 9) {
    return formatFieldWithSubcomponent(segmentName, fieldNum, field, "PT");
  }
  if (index === 10) {
    return formatFieldWithSubcomponent(segmentName, fieldNum, field, "VID");
  }

  return formatSimpleField(segmentName, fieldNum, field);
}

// Handles PID segment field formatting
function formatPIDField(segmentName: string, field: string, index: number): string {
  const fieldNum = index + 1;

  if (index === 2 || index === 17) {
    const subfields = field.split("^").filter((sf) => sf !== null && sf !== "");
    let xml = `\t<${segmentName}.${fieldNum}>\n`;
    subfields.forEach((subfield, subIndex) => {
      if (subfield.includes("&")) {
        const subSubfields = subfield.split("&");
        xml += `\t\t<CX.${subIndex + 1}>\n`;
        subSubfields.forEach((subSubfield, subSubIndex) => {
          xml += `\t\t\t<HD.${subSubIndex + 1}>${escapeXml(subSubfield)}</HD.${subSubIndex + 1}>\n`;
        });
        xml += `\t\t</CX.${subIndex + 1}>\n`;
      } else {
        xml += `\t\t<CX.${subIndex + 1}>${escapeXml(subfield)}</CX.${subIndex + 1}>\n`;
      }
    });
    return xml + `\t</${segmentName}.${fieldNum}>\n`;
  }

  if (index >= 4 && index <= 5) {
    const subfields = field.split("^").filter((sf) => sf !== null && sf !== "");
    let xml = `\t<${segmentName}.${fieldNum}>\n`;
    subfields.forEach((subfield, subIndex) => {
      if (subIndex === 0) {
        xml += `\t\t<XPN.${subIndex + 1}>\t\t<FN.1>${escapeXml(subfield)}</FN.1>\n</XPN.${subIndex + 1}>\n`;
      } else {
        xml += `\t\t<XPN.${subIndex + 1}>${escapeXml(subfield)}</XPN.${subIndex + 1}>\n`;
      }
    });
    return xml + `\t</${segmentName}.${fieldNum}>\n`;
  }

  if (index === 6) {
    return formatFieldWithSubcomponent(segmentName, fieldNum, field, "TS");
  }

  if (index === 10) {
    const subfields = field.split("^").filter((sf) => sf !== null && sf !== "");
    let xml = `\t<${segmentName}.${fieldNum}>\n`;
    subfields.forEach((subfield, subIndex) => {
      if (subIndex === 0) {
        xml += `\t\t<XAD.${subIndex + 1}>\t\t<SAD.1>${escapeXml(subfield)}</SAD.1>\n</XAD.${subIndex + 1}>\n`;
      } else {
        xml += `\t\t<XAD.${subIndex + 1}>${escapeXml(subfield)}</XAD.${subIndex + 1}>\n`;
      }
    });
    return xml + `\t</${segmentName}.${fieldNum}>\n`;
  }

  if (index === 12) {
    return formatSubfields(segmentName, fieldNum, field, "XTN");
  }

  return formatSimpleField(segmentName, fieldNum, field);
}

export function segmentHl7v2Xml(segmentName: string | undefined, fields: string[]): string {
  if (segmentName === undefined || !Array.isArray(fields)) {
    return "Invalid segment name";
  }

  let segmentXML = `<${segmentName}>\n`;

  try {
    switch (segmentName) {
      case "MSH":
        segmentXML += `\t<${segmentName}.1>|</${segmentName}.1>\n`;
        fields.forEach((field, index) => {
          if (field !== null && field !== "") {
            segmentXML += formatMSHField(segmentName, field, index);
          }
        });
        break;

      case "MSA":
      case "ERR":
        fields.forEach((field, index) => {
          if (field !== null && field !== "") {
            segmentXML += formatSimpleField(segmentName, index + 1, field);
          }
        });
        break;

      case "QAK":
        fields.forEach((field, index) => {
          if (field !== null && field !== "") {
            segmentXML += `\t<${segmentName}.${index + 1}>\n${escapeXml(field)}\t</${segmentName}.${index + 1}>\n`;
          }
        });
        break;

      case "QPD":
        fields.forEach((field, index) => {
          if (field !== null && field !== "") {
            if (index === 0) {
              segmentXML += formatFieldWithSubcomponent(segmentName, index + 1, field, "CE");
            } else {
              segmentXML += formatSimpleField(segmentName, index + 1, field);
            }
          }
        });
        break;

      case "PID":
        fields.forEach((field, index) => {
          if (field !== null && field !== "") {
            segmentXML += formatPIDField(segmentName, field, index);
          }
        });
        break;

      default:
        // For unknown segment types, include all fields (even empty) to preserve data integrity
        fields.forEach((field, index) => {
          const value = field ?? "";
          segmentXML += formatSimpleField(segmentName, index + 1, value);
        });
        break;
    }
  } catch (error) {
    console.error(`Error formatting segment ${segmentName}:`, error);
    // Return partial XML on error
    segmentXML += `\t<!-- Error formatting fields: ${error} -->\n`;
  }

  segmentXML += `</${segmentName}>\n`;
  return segmentXML;
}

export function detectSeparator(segment: string): string {
  // For MSH segment, the separator is always at position 3 (after "MSH")
  if (segment.startsWith("MSH")) {
    return segment.charAt(3) || "|";
  }
  // Fallback for other segments: look for common separators
  const separators = ["|", ":", "^", "~", "\\", "&"];
  return separators.find((separator) => segment.includes(separator)) || "|";
}

export function convertToXML(segments: string[]): string {
  let rootElementName = "HL7v2";

  try {
    const xmlSegments = segments.map((segment) => {
      const separator = detectSeparator(segment);
      const fields = segment.split(separator);
      const segmentName = fields.shift();

      // Extract root element name from MSH.9 (message type)
      if (segmentName === "MSH" && fields.length > 7) {
        const msh9 = fields[7];
        const msh9Parts = msh9?.split("^");
        if (msh9Parts && msh9Parts.length > 2 && msh9Parts[2]) {
          rootElementName = msh9Parts[2];
        }
      }

      return segmentHl7v2Xml(segmentName, fields);
    });

    return `<${rootElementName}>\n${xmlSegments.join("")}</${rootElementName}>\n`;
  } catch (error) {
    console.error("Error converting to XML:", error);
    return `<${rootElementName}>\n\t<!-- Error: ${error} -->\n</${rootElementName}>\n`;
  }
}

export function parseHl7v2Xml(base64Data: string): string {
  try {
    if (typeof base64Data !== "string" || base64Data.trim() === "") {
      return "<HL7v2>\n\t<!-- Error: Invalid input - base64Data must be a non-empty string -->\n</HL7v2>\n";
    }

    const segments = parseHL7v2Binary(base64Data);

    if (segments.length === 0) {
      return "<HL7v2>\n\t<!-- Error: No valid segments found -->\n</HL7v2>\n";
    }

    // Verify first segment is MSH
    if (!segments[0].startsWith("MSH")) {
      console.warn("Warning: First segment is not MSH, may not be valid HL7v2 message");
    }

    return convertToXML(segments);
  } catch (error) {
    console.error("Error parsing HL7v2 to XML:", error);
    return `<HL7v2>\n\t<!-- Error parsing message: ${error} -->\n</HL7v2>\n`;
  }
}
