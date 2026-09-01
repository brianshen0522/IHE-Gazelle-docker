import { NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";

export async function GET() {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    const backendUrl = `${process.env.GZL_VALIDATION_GATEWAY_URL}/indexes`;

    // Build headers - include Authorization only if token exists
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };
    if (accessToken) {
      headers["Authorization"] = `Bearer ${accessToken}`;
    }

    const response = await fetch(backendUrl, { headers });

    if (!response.ok) {
      throw new Error(`Backend error: ${response.status}`);
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Error fetching validation indexes:", error);
    return NextResponse.json({ error: "Failed to fetch validation indexes" }, { status: 500 });
  }
}
