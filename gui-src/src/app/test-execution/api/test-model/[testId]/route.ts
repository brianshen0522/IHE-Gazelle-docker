import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import axios from "axios";

export async function GET(request: NextRequest, { params }: { params: Promise<{ testId: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    if (!accessToken) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { testId } = await params;

    if (!testId) {
      return NextResponse.json({ error: "testId is required" }, { status: 400 });
    }

    const url = `${process.env.GZL_TEST_MODEL_REPOSITORY_URL}/test-models/tests/${testId}`;

    const { data, status } = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    return NextResponse.json({ data }, { status });
  } catch (error) {
    console.error("Error fetching test model:", error);
    if (axios.isAxiosError(error)) {
      const errorMessage = error.response?.data?.message || error.message || "Failed to fetch test model";
      const statusCode = error.response?.status || 500;
      return NextResponse.json({ error: errorMessage }, { status: statusCode });
    }
    return NextResponse.json({ error: "Failed to fetch test model" }, { status: 500 });
  }
}
