/**
 * Parse Content-Range header to extract range information
 * @param range Content-Range header value (e.g., "0-49/100", "bytes 0-49/100", "items 1-50/100")
 * @returns Object with start, end, and total or null if parsing fails
 */
export const parseContentRange = (range: string | null | undefined): { start: number; end: number; total: number } | null => {
  if (!range) return null;
  // Match formats: "0-49/100", "bytes 0-49/100", "items 1-50/100", etc.
  const regex = /^(?:\w+\s+)?(\d+)-(\d+)\/(\d+)$/;
  const match = regex.exec(range);
  if (!match) return null;
  return {
    start: Number.parseInt(match[1], 10),
    end: Number.parseInt(match[2], 10),
    total: Number.parseInt(match[3], 10),
  };
};
