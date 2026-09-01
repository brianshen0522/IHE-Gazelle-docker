"use server";
import { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";
import { getServerSession } from "next-auth";
import { authOptions } from "@auth/authOptions";
import { parseContent } from "@/app/message-capture/utils/parseContent";

export interface GetDatahouseItemProps {
  location?: string;
  itemId?: string;
  readAccessKey?: string;
  parseContent?: boolean;
}

export type GetDatahouseItemResult = {
  datahouseItem?: DatahouseItem;
  error?: string;
  statusCode?: number;
};

type HttpError = Error & { statusCode?: number };

/**
 * Build the request URL based on provided parameters
 */
function buildRequestUrl({ location, itemId, readAccessKey }: Pick<GetDatahouseItemProps, "location" | "itemId" | "readAccessKey">): string | null {
  if (location) {
    return location;
  }

  if (itemId) {
    const baseUrl = `${process.env.GZL_DTH_API_URL}/items/${itemId}`;
    return readAccessKey ? `${baseUrl}?readAccessKey=${readAccessKey}` : baseUrl;
  }

  return null;
}

/**
 * Get authorization header if available
 */
async function getAuthHeader(skipAuth: boolean): Promise<string | undefined> {
  if (skipAuth) {
    return undefined;
  }

  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    // Only use auth header if we have a valid session with user
    if (accessToken && session?.user) {
      return `Bearer ${accessToken}`;
    }
  } catch (e) {
    console.error("Failed to get server session:", e);
  }

  return undefined;
}

/**
 * Build request headers
 */
function buildHeaders(authHeader?: string): HeadersInit {
  const headers: HeadersInit = {};
  if (authHeader) {
    headers.Authorization = authHeader;
  }
  return headers;
}

/**
 * Create and throw an HTTP error with status code
 */
function throwHttpError(status: number, statusText: string): never {
  const error: HttpError = new Error(`HTTP ${status}: ${statusText}`);
  error.statusCode = status;
  throw error;
}

/**
 * Unified server action for fetching Datahouse items
 * FIXME: GET request should be moved to Next API route
 *
 */
export async function getDatahouseItem({
  location,
  itemId,
  readAccessKey,
  parseContent: shouldParseContent = true,
}: GetDatahouseItemProps): Promise<GetDatahouseItemResult | null> {
  try {
    // Build URL
    const url = buildRequestUrl({ location, itemId, readAccessKey });
    if (!url) {
      return null;
    }

    // Get auth header (skip if using readAccessKey)
    const authHeader = await getAuthHeader(!!readAccessKey);

    // Fetch item
    const headers = buildHeaders(authHeader);
    const res = await fetch(url, { headers });

    if (!res.ok) {
      throwHttpError(res.status, res.statusText);
    }

    let data: DatahouseItem = await res.json();

    // Optionally parse stringified content (for message-capture)
    if (shouldParseContent) {
      const parsed = parseContent([data]);
      data = parsed[0] as DatahouseItem;
    }

    return { datahouseItem: data };
  } catch (err) {
    const message = location ? `Unable to retrieve item from ${location}` : `Unable to retrieve item with id ${itemId}`;
    const errorMessage = err instanceof Error ? `${message}: ${err.message}` : message;
    const statusCode = (err as HttpError)?.statusCode;
    return { error: errorMessage, statusCode };
  }
}
