export function extractOid(reportLocation: string) {
  const oidParam = "oid=";
  const startIndex = reportLocation?.indexOf(oidParam);
  if (startIndex === -1) {
    return null; // oid parameter not found
  }
  return reportLocation?.substring(startIndex + oidParam.length);
}
