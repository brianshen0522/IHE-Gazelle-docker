import axios from "axios";
import { NextRequest, NextResponse } from "next/server";
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";
import { parseContent } from "@/app/message-capture/utils/parseContent";

/**
 * Generic API route for fetching Datahouse items by ID
 * Used by all portals (simulation-portal, test-execution, validation-portal)
 *
 * For message-capture specific functionality with content parsing,
 * use /message-capture/api/items/[id] instead
 */
export async function GET(req: NextRequest) {
  try {
    const id = req.nextUrl.pathname.split("/").pop();
    if (!id) {
      return NextResponse.json({ error: "Missing item ID" }, { status: 400 });
    }

    // Optional query parameters
    const readAccessKey = req.nextUrl.searchParams.get("readAccessKey");
    const parseContentParam = req.nextUrl.searchParams.get("parseContent") === "true";
    const queryString = readAccessKey ? `?readAccessKey=${readAccessKey}` : "";

    // Build Datahouse API URL
    const url = new URL(`${process.env.GZL_DTH_API_URL}/items/${id}${queryString}`);

    const auth = await getAuthorizationHeader(req);
    const axiosConfig = auth ? { headers: { Authorization: auth } } : undefined;

    const response = await axios.get(url.toString(), axiosConfig);

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
    const message = e.response?.data?.error ?? e.message ?? "Unable to get item by id";

    return NextResponse.json({ error: message, status }, { status });
  }
}
