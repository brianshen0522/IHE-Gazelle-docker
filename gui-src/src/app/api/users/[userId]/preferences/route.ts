import axios from "axios";
import { NextRequest, NextResponse } from "next/server";

type ParamsProps = {
  userId: string;
};

export async function GET(req: NextRequest, { params }: { params: Promise<ParamsProps> }) {
  const { userId } = await params;
  try {
    const auth = req.headers.get("Authorization");
    const { data, status } = await axios.get(`${process.env.GZL_GUM_API_URL}/v2/users/${userId}/preferences`, {
      headers: {
        Authorization: auth,
      },
    });

    return NextResponse.json({ data, status });
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : "Unable to get user preferences";
    const statusCode = axios.isAxiosError(err) ? err.response?.status || 500 : 500;

    return NextResponse.json({ error: errorMessage }, { status: statusCode });
  }
}
