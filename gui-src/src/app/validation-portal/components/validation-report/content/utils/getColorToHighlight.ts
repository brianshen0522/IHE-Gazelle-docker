/**
 * Returns the color to highlight a line based on the result and severity.
 * - If the result is "FAILED" and severity is "ERROR", it returns a light red color.
 * - If the result is "FAILED" and severity is "WARNING", it returns a light orange color.
 * - If the result is "FAILED" and severity is "INFO", it returns a light blue color.
 * @param result - The result of the assertion (e.g., "PASSED", "FAILED").
 * @param severity - The severity of the assertion (e.g., "ERROR", "WARNING", "INFO").
 * @returns A string representing the color to highlight the line, or an empty string if no
 */
export function getColorToHighlight(result: string, severity: string): string {
  if (result === "FAILED") {
    if (severity === "ERROR") return "#f4433655";
    else if (severity === "WARNING") return "#ff980055";
    else if (severity === "INFO") return "#45327d44";
  }
  return "";
}
