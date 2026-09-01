const CLINICAL_DOCUMENT_PATTERN = /<((?:[\w-]+:)?ClinicalDocument)\b[^>]*>/i;
const CDA_NAMESPACE_PATTERN = /xmlns(?::[\w-]+)?=["']urn:hl7-org:v3["']/i;

export function isCdaDocument(xmlContent: string): boolean {
  if (!xmlContent) {
    return false;
  }

  return CLINICAL_DOCUMENT_PATTERN.test(xmlContent) && CDA_NAMESPACE_PATTERN.test(xmlContent);
}
