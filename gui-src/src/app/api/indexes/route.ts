import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";

const BACKEND_URLS: Record<string, string | undefined> = {
  datahouse: process.env.GZL_DTH_API_URL,
  validation: process.env.GZL_VALIDATION_GATEWAY_URL,
  simulation: process.env.GZL_SIMULATION_GATEWAY_URL,
  test_execution: `${process.env.GZL_TEST_EXECUTION_URL}/sessions/tests`,
  organizations: `${process.env.GZL_GUM_API_URL}/organizations`,
  users: `${process.env.GZL_GUM_API_URL}/v2/users`,
};

export async function GET(req: NextRequest) {
  try {
    const type = req.nextUrl.searchParams.get("type") ?? "";
    const backendUrl = BACKEND_URLS[type];

    if (!backendUrl) {
      return NextResponse.json({ error: "Invalid backend type" }, { status: 400 });
    }

    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    // Build headers - include Authorization only if token exists
    const headers: HeadersInit = { "Content-Type": "application/json" };
    if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;

    const response = await fetch(`${backendUrl}/indexes`, { headers });

    if (!response.ok) throw new Error(`Backend error: ${response.status}`);

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Error fetching indexes:", error);
    return NextResponse.json({ error: "Failed to fetch indexes" }, { status: 500 });
  }
}
