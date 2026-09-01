import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import { Test } from "@/app/test-execution/types/TestCase";

const BACKEND_URLS: Record<string, string | undefined> = {
  validation: process.env.GZL_VALIDATION_GATEWAY_URL,
  simulation: process.env.GZL_SIMULATION_GATEWAY_URL,
  test_execution: process.env.GZL_TEST_EXECUTION_URL,
  users: process.env.GZL_GUM_API_URL,
  organizations: process.env.GZL_GUM_API_URL,
};

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

    // Build headers - include Authorization only if token exists
    const headers: HeadersInit = { "Content-Type": "application/json" };
    if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;

    // Build query string without 'type' and 'path' params (they're only for internal routing)
    const { searchParams } = new URL(request.url);
    const backendParams = new URLSearchParams();
    const path = searchParams.get("path") ?? "";

    // Test execution doesn't support pagination parameters
    const excludeParams = type === "test_execution" ? ["type", "path", "offset", "limit"] : ["type", "path"];

    // Copy all params except excluded ones
    for (const [key, value] of searchParams.entries()) {
      if (!excludeParams.includes(key)) {
        backendParams.append(key, value);
      }
    }

    const queryString = backendParams.toString();
    const url = `${backendUrl}${path}${queryString ? "?" + queryString : ""}`;

    const response = await fetch(url, { headers });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: `Backend error: ${response.status}` }));
      return NextResponse.json(errorData, { status: response.status });
    }

    const data = await response.json();
    const contentRange = response.headers.get("Content-Range");

    // Normalize response: test execution returns test suite with 'testsMetadata' array
    let normalizedData = data;
    if (type === "test_execution" && data.testsMetadata && Array.isArray(data.testsMetadata)) {
      // Add suite-level fields to each test metadata item
      normalizedData = data.testsMetadata.map((test: Test) => ({
        ...test,
        id: data.id, // testSuiteId
        testSessionId: data.testSessionId,
      }));
    }
    if (type === "users_management" && data.users && Array.isArray(data.users)) {
      normalizedData = data.users;
    }

    const responseHeaders: HeadersInit = {};
    if (contentRange) {
      responseHeaders["Content-Range"] = contentRange;
    }

    return NextResponse.json({ data: normalizedData, contentRange }, { status: 200, headers: responseHeaders });
  } catch (error) {
    console.error("Error fetching data:", error);
    return NextResponse.json({ error: "Failed to fetch data" }, { status: 500 });
  }
}
