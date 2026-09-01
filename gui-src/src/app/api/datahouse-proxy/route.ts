import axios from "axios";
import { NextRequest, NextResponse } from "next/server";
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";
import { parseContent } from "@/app/message-capture/utils/parseContent";

/**
 * Generic proxy for external Datahouse URLs
 * Handles CORS issues by proxying requests through the server
 *
 * Usage: /api/datahouse-proxy?url={encoded_url}&parseContent={true|false}
 * Used by useDatahouseItem when fetching items by location to avoid CORS issues with external URLs
 */
export async function GET(req: NextRequest) {
  try {
    // Get the target URL from query parameter
    const targetUrl = req.nextUrl.searchParams.get("url");
    if (!targetUrl) {
      return NextResponse.json({ error: "Missing 'url' query parameter" }, { status: 400 });
    }

    // Optional query parameters
    const readAccessKey = req.nextUrl.searchParams.get("readAccessKey");
    const parseContentParam = req.nextUrl.searchParams.get("parseContent") === "true";

    // Build the final URL with readAccessKey if provided
    const url = new URL(targetUrl);
    if (readAccessKey) {
      url.searchParams.append("readAccessKey", readAccessKey);
    }

    // Get authentication header if available
    const auth = await getAuthorizationHeader(req);
    const axiosConfig = auth ? { headers: { Authorization: auth } } : undefined;

    // Fetch from external URL
    const response = await axios.get(url.toString(), axiosConfig);

    if (response.status === 404) {
      return NextResponse.json({ error: "Item not found" }, { status: 404 });
    }

    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const rawData = response.data;

    // Optionally parse stringified content
    const data = parseContentParam ? parseContent([rawData])[0] : rawData;

    return NextResponse.json(data);
  } catch (err: unknown) {
    const e = err as { response?: { status?: number; data?: { error?: string } }; message?: string };
    const status = e.response?.status ?? 500;
    const message = e.response?.data?.error ?? e.message ?? "Unable to fetch external Datahouse item";

    return NextResponse.json({ error: message, status }, { status });
  }
}

