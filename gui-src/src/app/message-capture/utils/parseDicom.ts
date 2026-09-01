export function parseDicom(base64Data: string, type: string): string {
  if (base64Data === null) {
    return "No data provided.";
  }

  if (!base64Data) {
    return "Error: The provided Base64 data is invalid.";
  }

  try {
    const lines = base64Data.split("\n");
    let formattedContent = "";
    let outputStr = "";
    const startsNewLine = (line: string) => /^(\d+:|\d+\s{2}\()/.test(line.trimStart());

    if (!lines.some(startsNewLine)) {
      return base64Data;
    }

    lines.forEach((line) => {
      if (startsNewLine(line)) {
        formattedContent += "\n" + line;
      } else {
        formattedContent += line.trim() + " ";
      }
    });

    if (type === "Raw") {
      outputStr = formattedContent.trim();
    } else {
      throw new Error(`Invalid type specified: ${type}`);
    }

    return outputStr;
  } catch (error) {
    console.error("Error parsing DICOM data:", error);
    return "<dicom>Error parsing DICOM data. The dataset or command set might not be valid.</dicom>";
  }
}
