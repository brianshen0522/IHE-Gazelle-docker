// Formats a timestamp into a localized time string
export function formatTimestamp(dateTime?: string | number): string {
  if (!dateTime) return "";
  try {
    return new Date(dateTime).toLocaleTimeString();
  } catch {
    return "";
  }
}

// Safely converts an unknown value to a string
export function formatToString(value?: unknown): string {
  if (value === undefined || value === null) {
    return "";
  }

  if (typeof value === "string" || typeof value === "number" || typeof value === "boolean") {
    return String(value);
  }

  if (typeof value === "object") {
    try {
      return JSON.stringify(value);
    } catch {
      return "[Object]";
    }
  }

  return "";
}

// Type guard to check if a log entry is a structured object
export function isStructuredLog(log: unknown): log is Record<string, unknown> {
  return typeof log === "object" && log !== null;
}
