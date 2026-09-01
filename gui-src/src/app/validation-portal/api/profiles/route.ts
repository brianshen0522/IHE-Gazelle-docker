import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";

export async function GET(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    if (!accessToken) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const queryString = searchParams.toString();
    const url = `${process.env.GZL_VALIDATION_GATEWAY_URL}${queryString ? "?" + queryString : ""}`;

    // Build headers - include Authorization only if token exists
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };
    if (accessToken) {
      headers["Authorization"] = `Bearer ${accessToken}`;
    }

    const response = await fetch(url, { headers });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: `Backend error: ${response.status}` }));
      return NextResponse.json(errorData, { status: response.status });
    }

    const data = await response.json();
    const contentRange = response.headers.get("Content-Range");

    const responseHeaders: HeadersInit = {};
    if (contentRange) {
      responseHeaders["Content-Range"] = contentRange;
    }

    return NextResponse.json({ data, contentRange }, { status: 200, headers: responseHeaders });
  } catch (error) {
    console.error("Error fetching validation profiles:", error);
    return NextResponse.json({ error: "Failed to fetch validation profiles" }, { status: 500 });
  }
}
