import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import axios from "axios";

export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    if (!accessToken) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { id } = await params;
    const { searchParams } = new URL(request.url);

    // Build query parameters
    const queryParams = new URLSearchParams(Array.from(searchParams.entries()).filter(([key]) => ["_limit", "_offset", "_sort"].includes(key)));

    const queryString = queryParams.toString();
    const baseUrl = `${process.env.GZL_TEST_EXECUTION_URL}/test-run-executions/${id}`;
    const url = queryString ? `${baseUrl}?${queryString}` : baseUrl;

    const { data, status } = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    return NextResponse.json({ data }, { status });
  } catch (error) {
    console.error("Error fetching test run execution:", error);
    if (axios.isAxiosError(error)) {
      const errorMessage = error.response?.data?.message || error.message || "Failed to fetch test run execution";
      const statusCode = error.response?.status || 500;
      return NextResponse.json({ error: errorMessage }, { status: statusCode });
    }
    return NextResponse.json({ error: "Failed to fetch test run execution" }, { status: 500 });
  }
}
