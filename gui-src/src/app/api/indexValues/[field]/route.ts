import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";

const BACKEND_URLS: Record<string, string | undefined> = {
  datahouse: process.env.GZL_DTH_API_URL,
  validation: process.env.GZL_VALIDATION_GATEWAY_URL,
  simulation: process.env.GZL_SIMULATION_GATEWAY_URL,
  test_execution: process.env.GZL_TEST_EXECUTION_URL,
  organizations: `${process.env.GZL_GUM_API_URL}`,
  users: `${process.env.GZL_GUM_API_URL}/v2/users`,
};

export async function GET(req: NextRequest, { params }: { params: Promise<{ field: string }> }) {
  try {
    const { field } = await params;
    const type = req.nextUrl.searchParams.get("type") ?? "";
    const indexPath = req.nextUrl.searchParams.get("indexPath") ?? "";
    const backendUrl = BACKEND_URLS[type];

    if (!backendUrl) {
      return NextResponse.json({ error: "Invalid backend type" }, { status: 400 });
    }

    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    // Extract all query params except 'type' to forward to backend
    const queryParams: Record<string, string> = {};
    req.nextUrl.searchParams.forEach((value, key) => {
      if (key !== "type" && key !== "indexPath" && value) {
        queryParams[key] = value;
      }
    });

    // Build the backend URL with query params
    const url = new URL(`${backendUrl}${indexPath}/indexes/${field}/values`);
    url.search = new URLSearchParams(queryParams).toString();

    // Build headers - include Authorization only if token exists
    const headers: HeadersInit = { "Content-Type": "application/json" };
    if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;

    const response = await fetch(url.toString(), { headers });

    if (!response.ok) throw new Error(`Backend error: ${response.status}`);

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Error fetching index values:", error);
    return NextResponse.json({ error: "Failed to fetch index values" }, { status: 500 });
  }
}
