import { SubjectLocation } from "@/shared/types/validation/types";
import { findLineNumberFromJsonPath } from "@/app/validation-portal/utils/lineFinderFromJsonPath";
import { findLineNumberFromXmlPath } from "@/app/validation-portal/components/validation-report/content/utils/lineFinderFromXmlPath";

/**
 * Extracts the line number from a subject location string.
 * @param subjectLocationType - The type of the subject location (e.g., "line-column", "XPath", "JSONPath").
 * @param subjectLocationValue - The value of the subject location.
 * @param content - The content of the file to analyze.
 * @returns The line number (1-based) or 0 if not found.
 */
function getLineNumberFromSubjectLocationString(
  subjectLocationType: string | undefined,
  subjectLocationValue: string | undefined,
  content?: string,
): number {
  if (!subjectLocationType || !subjectLocationValue) {
    return 0;
  }

  // Extract line numbers from the subject location (^line \\d+(, column \\d+)?$) [example: "line 42, column 5"]
  if (subjectLocationType === "line-column") {
    const matchFirstRegex = new RegExp(/line (\d+)(, column \d+)?/).exec(subjectLocationValue);
    if (matchFirstRegex?.[1]) {
      return Number.parseInt(matchFirstRegex[1], 10);
    }
    return 0;
  }

  // Handle JSON paths with json-source-map (supports both "JSONPath" and "json-path")
  const isJsonPath = subjectLocationType === "JSONPath" || subjectLocationType === "json-path";
  if (isJsonPath && content) {
    return findLineNumberFromJsonPath(subjectLocationValue, content);
  }

  // Handle XML/XPath (supports both "XPath" and "xml-path")
  const isXmlPath = subjectLocationType === "XPath" || subjectLocationType === "xml-path";
  if (isXmlPath && content) {
    return findLineNumberFromXmlPath(subjectLocationValue, content);
  }

  return 0;
}

/**
 * Extracts the line number from the subject location string.
 * The subject location can be in two formats:
 * 1. "line 42, column 5" (or similar) - where the line number is after "line ".
 * 2. "line:42" - where the line number is after the colon.
 * @param fileName - The name of the file to match with the subject location's inputId.
 * @param subjectLocations - An array of SubjectLocation objects from which to extract the line number.
 * @param content - The content of the file to analyze (used for JSON/XML path extraction).
 * @returns The extracted line number as an integer, or 0 if no line number is found.
 */
export function getLineNumberFromSubjectLocation(
  fileName: string,
  subjectLocations: SubjectLocation[] | null | undefined,
  content: string | undefined,
): number {
  if (!subjectLocations || subjectLocations.length === 0) {
    return 0;
  }

  const results: number[] = subjectLocations.map((subjectLocation) => {
    // We highlight the line only if the inputId is not defined or matches the fileName
    // Also handle cases where fileName has been modified with suffixes like " (2)"
    const inputIdMatches =
      !subjectLocation.inputId ||
      subjectLocation.inputId === fileName ||
      fileName.startsWith(subjectLocation.inputId + " (") ||
      fileName.startsWith(subjectLocation.inputId + ".");

    if (inputIdMatches) {
      return getLineNumberFromSubjectLocationString(subjectLocation.type, subjectLocation.value, content);
    } else {
      return 0;
    }
  });

  return results.pop() ?? 0;
}
