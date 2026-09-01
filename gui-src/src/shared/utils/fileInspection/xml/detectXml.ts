import { RenderDecision } from "../types";

export function isValidXml(str: string): boolean {
  const trimmed = str.trim();
  if (!trimmed.startsWith("<")) return false;

  // Prevent ReDoS by limiting the search scope
  // Check only first 10000 chars for closing/self-closing tags
  const sample = trimmed.length > 10000 ? trimmed.slice(0, 10000) : trimmed;
  // Check for at least one closing tag or self-closing tag
  // Use non-backtracking pattern by limiting character class repetition
  return /<\/[^>]{1,200}>|<[^>]{1,200}\/>/.test(sample);
}

export function detectXmlDataType(decoded: string): RenderDecision["dataType"] {
  const lowerDecoded = decoded.toLowerCase();

  if (/<dicom/i.test(decoded)) return "DICOM";
  if (lowerDecoded.includes("<http")) return "HTTP";
  if (/<syslog/i.test(decoded)) return "SYSLOG";

  return undefined;
}
