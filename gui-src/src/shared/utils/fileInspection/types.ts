export type RendererType = "JSON" | "XML" | "RAW" | "HEX";

export interface RenderDecision {
  renderer: RendererType;
  base64Data: string;
  textPreview?: string;
  dataType?: "DICOM" | "HL7v2" | "HTTP" | "SYSLOG" | "BINARY";
}
