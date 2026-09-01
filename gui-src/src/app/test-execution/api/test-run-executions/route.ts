import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import axios from "axios";

export async function GET(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    if (!accessToken) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const testId = searchParams.get("testId");
    const testSessionId = searchParams.get("testSessionId");
    const limit = searchParams.get("limit") ?? "10";
    const sortBy = searchParams.get("sortBy") ?? "id";
    const sortOrder = searchParams.get("sortOrder") ?? "DESC";
    const owner = searchParams.get("owner");

    if (!testId || !testSessionId) {
      return NextResponse.json({ error: "testId and testSessionId are required" }, { status: 400 });
    }

    // Build query parameters for backend API
    const queryParams = new URLSearchParams();
    queryParams.set("testId", testId);
    queryParams.set("testSessionId", testSessionId);
    if (owner) queryParams.set("owner", owner);

    const sortPrefix = sortOrder === "DESC" ? "-" : "";
    const url = `${process.env.GZL_TEST_EXECUTION_URL}/test-run-executions?_limit=${limit}&_sort=${sortPrefix}${sortBy}&${queryParams.toString()}`;

    const { data, status } = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    return NextResponse.json({ data }, { status });
  } catch (error) {
    console.error("Error fetching test run executions:", error);
    if (axios.isAxiosError(error)) {
      const errorMessage = error.response?.data?.message || error.message || "Failed to fetch test run executions";
      const statusCode = error.response?.status || 500;
      return NextResponse.json({ error: errorMessage }, { status: statusCode });
    }
    return NextResponse.json({ error: "Failed to fetch test run executions" }, { status: 500 });
  }
}
