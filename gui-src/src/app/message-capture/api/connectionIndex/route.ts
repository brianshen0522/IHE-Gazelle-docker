import axios from "axios";
import { NextRequest, NextResponse } from "next/server";
import { parseContent } from "@/app/message-capture/utils/parseContent";
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";

export const dynamic = "force-dynamic";

export async function GET(req: NextRequest) {
  try {
    // Extract query parameters from the request
    const params: { [key: string]: string | null } = {
      reference: req.nextUrl.searchParams.get("reference"),
      _presentation: req.nextUrl.searchParams.get("_presentation"),
      _limit: req.nextUrl.searchParams.get("_limit"),
    };
    const auth = await getAuthorizationHeader(req);

    // Remove null, undefined, or empty string properties
    const filteredParams = params ? Object.fromEntries(Object.entries(params).filter(([_, value]) => value !== null && value !== "")) : {};

    const url = new URL(`${process.env.GZL_DTH_API_URL}/items`);
    url.search = new URLSearchParams(filteredParams as Record<string, string>).toString();
    url.searchParams.append("_sort", "capture_date");
    url.searchParams.append("_sort_order", "asc");
    url.searchParams.append("_limit", "all");

    const response = await axios.get(
      url.toString(),
      auth ? { headers: { Authorization: auth } } : undefined
    );

    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    let data = await response.data;
    data = parseContent(data);
    return NextResponse.json(data);
  } catch (err: any) {
    return NextResponse.json({ error: err?.message || "Unable to get connection index" }, { status: err.response?.status || 500 });
  }
}
