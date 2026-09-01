type Segment = {
  [key: string]: any;
};

export function createExpandableObjects(data: any) {
  return data.map((item: { [s: string]: unknown } | ArrayLike<unknown>, index: any) => {
    return Object.entries(item).map(([key, value], subIndex) => (
      <details key={`${index}-${subIndex}`}>
        <summary>{key}</summary>
        <pre>{JSON.stringify(value, null, 2)}</pre>
      </details>
    ));
  });
}

// HL7v2Parser for json
function parseHL7v2JsonBinary(data: Buffer): object[] {
  const segments = data.toString().split("\r");
  return (
    segments &&
    segments
      .filter((segment) => segment.trim() !== "")
      ?.map((segment) => {
        const fields = segment.split("|");
        const segmentObject: { [key: string]: string } = {};
        fields.forEach((field, index) => {
          segmentObject[`Field${index}`] = field;
        });
        return segmentObject;
      })
  );
}

export function parseHl7v2Json(base64Data: string): string {
  try {
    const bufferData = Buffer.from(base64Data, "base64");
    const segments: Segment[] = parseHL7v2JsonBinary(bufferData);
    const nestedJson = segments.map((segment) => {
      const json: { [key: string]: any } = {};
      for (let i = 1; i < Object.keys(segment).length; i++) {
        json[`Field${i}`] = segment[`Field${i}`];
      }
      return { [segment.Field0]: json };
    });
    return createExpandableObjects(nestedJson);
  } catch (error) {
    return `Error parsing HL7v2 data: ${error}`;
  }
}
