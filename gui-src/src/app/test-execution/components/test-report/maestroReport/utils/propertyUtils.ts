import { DatahouseItemReference } from "@shared/types/datahouse/DatahouseItem";
import { getMimeType, handleDownloadFile } from "@test-execution/components/test-report/ButtonDownloadAttachment";

/**
 * Decodes base64 content safely
 * @param content - Base64 encoded content
 * @returns Decoded string or original content if decoding fails
 */
export function decodeBase64(content: string | undefined): string {
  if (!content) return "";
  try {
    return atob(content);
  } catch (e) {
    console.error("Failed to decode base64 content:", e);
    return content;
  }
}

/**
 * Handles downloading of base64 encoded content
 * @param content64 - Base64 encoded content
 * @param fileName - Name of the file to download
 * @param fileType - MIME type of the file
 */
export function handleDownloadItem(content64: string, fileName: string, fileType: string): void {
  const file = atob(content64);
  const blob = new Blob([file], { type: getMimeType(fileType) });
  handleDownloadFile(blob, fileName, fileType);
}

/**
 * Gets the type/mime-type from datahouse references
 * @param itemId - The item ID to look up
 * @param references - Array of datahouse item references
 * @returns The type or "text/plain" as fallback
 */
export function getTypeFromReferences(itemId: string, references: DatahouseItemReference[]): string {
  const reference = references.find((ref) => ref.value === itemId);
  return reference ? reference.type : "text/plain";
}
