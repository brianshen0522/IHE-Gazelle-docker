export function detectHL7v2(decoded: string): boolean {
  const trimmed = decoded.trim();
  return trimmed.startsWith("MSH|") || /^\s*MSH\|/.test(decoded);
}
