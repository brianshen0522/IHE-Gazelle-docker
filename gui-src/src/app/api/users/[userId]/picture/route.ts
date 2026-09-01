import axios from "axios";
import { NextRequest, NextResponse } from "next/server";

type ParamsProps = {
  userId: string;
};

export async function GET(req: NextRequest, { params }: { params: Promise<ParamsProps> }) {
  const { userId } = await params;
  const format: string = req.nextUrl.searchParams.get("format") || "normal";
  const auth = req.headers.get("Authorization");
  try {
    const { data, status, headers } = await axios.get(`${process.env.GZL_GUM_API_URL}/v2/users/${userId}/preferences/picture?format=${format}`, {
      headers: {
        Authorization: auth,
      },
      responseType: "arraybuffer",
    });

    // Convert binary data to base64
    const base64Data = Buffer.from(data, "binary").toString("base64");
    const contentType = headers["content-type"];
    const dataUrl = `data:${contentType};base64,${base64Data}`;

    return NextResponse.json({ data: dataUrl, status });
  } catch (err: unknown) {
    console.warn(err);
    const errorMessage = err instanceof Error ? err.message : "Unable to get users summary";
    const statusCode = axios.isAxiosError(err) ? err.response?.status || 500 : 500;

    // Handle 401 Unauthorized - token expired or invalid
    if (axios.isAxiosError(err) && err.response?.status === 401) {
      return NextResponse.json(
        {
          error: "Unauthorized - Token expired or invalid",
          details: err.response?.data || err?.message,
        },
        { status: 401 },
      );
    }

    return NextResponse.json(
      { error: axios.isAxiosError(err) ? err.response?.data?.error || "Request to get user picture failed" : errorMessage },
      { status: statusCode },
    );
  }
}
