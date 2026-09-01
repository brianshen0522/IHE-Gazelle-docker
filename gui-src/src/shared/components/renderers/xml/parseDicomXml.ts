import * as dicomParser from "dicom-parser";

export function parseDicomXml(base64Data: string): string {
  try {
    // Decode the base64 string into an ArrayBuffer
    const binaryString = atob(base64Data);
    const len = binaryString.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
      bytes[i] = binaryString.codePointAt(i)!;
    }
    // Validate the DICOM data before parsing
    if (len < 132 || binaryString.slice(128, 132) !== "DICM") {
      throw new Error("Invalid DICOM file");
    }
    // Parse the DICOM data
    const dataSet = dicomParser.parseDicom(bytes);

    // Convert the DICOM data to XML
    let xmlStr = "<dicom>";
    Object.values(dataSet.elements).forEach((element) => {
      xmlStr += `<element tag="${element.tag}" vr="${element.vr}" length="${element.length}">`;
      xmlStr += dataSet.string(element.tag);
      xmlStr += "</element>";
    });
    xmlStr += "</dicom>";

    return xmlStr;
  } catch (error) {
    return `<dicom>Error parsing DICOM data: ${error}</dicom>`;
  }
}
