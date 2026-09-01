import axios from "axios";
import { NextRequest, NextResponse } from "next/server";
import { parseContent } from "@/app/message-capture/utils/parseContent";
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";

export async function GET(req: NextRequest) {
  try {
    // Extract checkbox value from the request
    const hideError = req.nextUrl.searchParams.get("hide_error") === "true";
    // Extract query parameters from the request
    const params = Object.fromEntries(
      Object.entries({
        type: hideError ? "MESSAGE" : "MESSAGE, CONNECTION_ERROR",
        channel_type: req.nextUrl.searchParams.get("channel_type"),
        message_type: req.nextUrl.searchParams.get("message_type"),
        proxy_port: req.nextUrl.searchParams.get("proxy_port"),
        sender_hostname: req.nextUrl.searchParams.get("sender_hostname"),
        receiver_hostname: req.nextUrl.searchParams.get("receiver_hostname"),
        sender_ip: req.nextUrl.searchParams.get("sender_ip"),
        receiver_ip: req.nextUrl.searchParams.get("receiver_ip"),
        capture_date: req.nextUrl.searchParams.get("capture_date"),
        secured_message: req.nextUrl.searchParams.get("secured_message"),
        hide_errors: req.nextUrl.searchParams.get("hide_errors"),
        _offset: req.nextUrl.searchParams.get("_offset"),
        _limit: req.nextUrl.searchParams.get("_limit"),
        _sort: req.nextUrl.searchParams.get("_sort"),
        _sort_order: req.nextUrl.searchParams.get("_sort_order"),
        _presentation: req.nextUrl.searchParams.get("_presentation"),
      }).filter(([, v]) => v !== null && v !== "")
    );

    const auth = await getAuthorizationHeader(req);

    const url = new URL(`${process.env.GZL_DTH_API_URL}/items`);
    url.search = new URLSearchParams(params as Record<string, string>).toString();

    const response = await axios.get(
      url.toString(),
      auth ? { headers: { Authorization: auth } } : undefined
    );

    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    let data = await response.data;
    data = parseContent(data);
    return NextResponse.json({
      data,
      contentRange: response.headers["content-range"],
    });
  } catch (err: any) {
    return NextResponse.json(
      {
        error: {
          message: err.message,
          details: err.response?.data ?? "No additional details available",
          url: err.config?.url ?? "Unknown URL",
          status: err.response?.status ?? 500,
        },
      },
      { status: err.response?.status ?? 500 }
    );
  }
}
