import * as dicomParser from "dicom-parser";

export function parseDicomJson(base64Data: string): string {
  try {
    const raw = Buffer.from(base64Data, "base64");

    const array = new Uint8Array(raw);
    if (array.length < 132) {
      return "Buffer is too short to be a valid DICOM file";
    } else {
      const dataSet = dicomParser.parseDicom(array);
      const json: { [key: string]: any } = {};
      for (const element of Object.values(dataSet.elements)) {
        json[element.tag] = dataSet.string(element.tag);
      }
      return JSON.stringify(json, null, 2);
    }
  } catch (error) {
    return `Error parsing DICOM data: ${error}`;
  }
}
