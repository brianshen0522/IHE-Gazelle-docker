export type DocumentRenderer = {
  base64Data?: string;
  xmlData?: string;
  dataType: "DICOM" | "HTTP" | "HL7v2" | "SYSLOG" | "TCP" | "BINARY";
  contentType?: string;
  showLineNumbers?: boolean;
  linesProperties?: LineProperties[];
  bytesPerLine?: number;
  node?: string;
};

export type LineProperties = {
  lineNumber: number;
  severity: string;
  color: string;
  selected: boolean;
  onClickHandler: () => void;
}