// Extract datahouse item ID from testReportUrl
// Returns undefined if no valid item ID is found
export function extractItemIdFromReportUrl(testReportUrl?: string): string | undefined {
  if (!testReportUrl) return undefined;
  const match = /\/items\/([^/?]+)/.exec(testReportUrl);
  return match?.[1];
}
