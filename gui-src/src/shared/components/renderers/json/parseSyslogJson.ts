import * as xml2js from "xml2js";

export function parseSyslogJson(base64Data: string): string {
  let dataStr = "";
  try {
    xml2js.parseString(base64Data, { explicitArray: false }, (error, result) => {
      if (error) {
        dataStr = `Error parsing XML: ${error}`;
      } else {
        dataStr = JSON.stringify(result, null, 2);
      }
    });
    return dataStr;
  } catch (error) {
    return `Error parsing Syslog data: ${error}`;
  }
}
