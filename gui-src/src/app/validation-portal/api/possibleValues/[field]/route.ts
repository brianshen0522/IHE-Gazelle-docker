import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";

export async function GET(request: NextRequest, context: { params: Promise<{ field: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    const params = await context.params;
    const { searchParams } = new URL(request.url);
    const queryString = searchParams.toString();

    const backendUrl = `${process.env.GZL_VALIDATION_GATEWAY_URL}/indexes/${params.field}/values${queryString ? "?" + queryString : ""}`;

    // Build headers - include Authorization only if token exists
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };
    if (accessToken) {
      headers["Authorization"] = `Bearer ${accessToken}`;
    }

    const response = await fetch(backendUrl, { headers });

    if (!response.ok) {
      if (response.status === 404) {
        return NextResponse.json({ error: `Field '${params.field}' not found or not indexed` }, { status: 404 });
      }
      throw new Error(`Backend error: ${response.status}`);
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Error fetching possible values:", error);
    return NextResponse.json({ error: "Failed to fetch possible values" }, { status: 500 });
  }
}
