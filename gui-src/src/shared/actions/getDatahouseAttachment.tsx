"use server";
import { getServerSession } from "next-auth";
import { authOptions } from "@auth/authOptions";

export interface GetDatahouseAttachmentProps {
  itemId: string;
  attachmentId: string;
  readAccessKey?: string;
}

export type GetDatahouseAttachmentResult = {
  data?: string; // base64 encoded attachment data
  error?: string;
};

/**
 * Unified server action for fetching Datahouse attachments
 *
 * Fetches an attachment from a Datahouse item and returns it as base64 encoded string.
 * This replaces the need for API routes to fetch attachments.
 *
 * @param itemId - The ID of the Datahouse item
 * @param attachmentId - The ID of the attachment to fetch
 * @param readAccessKey - Optional read access key for public/shared access
 * @returns Base64 encoded attachment data or error
 */
export async function getDatahouseAttachment({
  itemId,
  attachmentId,
  readAccessKey,
}: GetDatahouseAttachmentProps): Promise<GetDatahouseAttachmentResult | null> {
  try {
    if (!itemId || !attachmentId) {
      return { error: "itemId and attachmentId are required" };
    }

    // Build URL
    let url = `${process.env.GZL_DTH_API_URL}/items/${itemId}/attachments/${attachmentId}`;
    if (readAccessKey) {
      url += `?readAccessKey=${readAccessKey}`;
    }

    // Get auth token (skip if using readAccessKey)
    let authHeader: string | undefined;
    if (!readAccessKey) {
      try {
        const session = await getServerSession(authOptions as any);
        const accessToken = (session as any)?.access_token as string | undefined;
        if (accessToken) {
          authHeader = `Bearer ${accessToken}`;
        }
      } catch (e) {
        console.error("Failed to get server session:", e);
      }
    }

    // Fetch attachment
    const headers: HeadersInit = {};
    if (authHeader) {
      headers.Authorization = authHeader;
    }

    const res = await fetch(url, { headers });

    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${res.statusText}`);
    }

    // Convert to base64
    const arrayBuffer = await res.arrayBuffer();
    const base64 = arrayBufferToBase64(arrayBuffer);

    return { data: base64 };
  } catch (err) {
    const errorMessage =
      err instanceof Error
        ? `Unable to retrieve attachment ${attachmentId} from item ${itemId}: ${err.message}`
        : `Unable to retrieve attachment ${attachmentId} from item ${itemId}`;
    return { error: errorMessage };
  }
}

/**
 * Converts an ArrayBuffer to a base64 string
 * Uses chunked processing to avoid stack overflow on large files
 */
function arrayBufferToBase64(buffer: ArrayBuffer): string {
  const uint8Array = new Uint8Array(buffer);
  let binaryString = "";
  const chunkSize = 0x8000; // 32KB chunks
  for (let i = 0; i < uint8Array.length; i += chunkSize) {
    binaryString += String.fromCodePoint(...uint8Array.subarray(i, i + chunkSize));
  }
  return Buffer.from(binaryString, "binary").toString("base64");
}
