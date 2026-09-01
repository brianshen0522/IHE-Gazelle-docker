import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";

const BACKEND_URLS: Record<string, string | undefined> = {
  users: process.env.GZL_GUM_API_URL,
  organizations: process.env.GZL_GUM_API_URL,
};

/**
 * API route to get HTTP headers using HEAD request
 * More efficient than GET when only metadata is needed
 */
export async function GET(request: NextRequest) {
  try {
    const type = request.nextUrl.searchParams.get("type") ?? "";
    const backendUrl = BACKEND_URLS[type];

    if (!backendUrl) {
      return NextResponse.json({ error: "Invalid backend type" }, { status: 400 });
    }

    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    if (!accessToken) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const requestHeaders: HeadersInit = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    };

    // Build query string
    const { searchParams } = new URL(request.url);
    const backendParams = new URLSearchParams();
    const path = searchParams.get("path") ?? "";

    for (const [key, value] of searchParams.entries()) {
      if (key !== "type" && key !== "path") {
        backendParams.append(key, value);
      }
    }

    const queryString = backendParams.toString();
    const url = `${backendUrl}${path}${queryString ? "?" + queryString : ""}`;

    // Use HEAD request to get only headers
    const response = await fetch(url, {
      method: "HEAD",
      headers: requestHeaders,
    });

    if (!response.ok) {
      return NextResponse.json({ error: `Backend error: ${response.status}` }, { status: response.status });
    }

    // Extract useful headers
    const headers: Record<string, string | null> = {
      "content-range": response.headers.get("Content-Range"),
      "content-type": response.headers.get("Content-Type"),
      "content-length": response.headers.get("Content-Length"),
      etag: response.headers.get("ETag"),
      "last-modified": response.headers.get("Last-Modified"),
    };

    return NextResponse.json({ headers }, { status: 200 });
  } catch (error) {
    console.error("Error fetching content range:", error);
    return NextResponse.json({ error: "Failed to fetch content range" }, { status: 500 });
  }
}
