import axios from "axios";
import { NextRequest, NextResponse } from "next/server";
import { parseContent } from "@/app/message-capture/utils/parseContent";
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";

export async function GET(req: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    const incomingAuthorization = req.headers.get("Authorization");

    if (!session?.user && !incomingAuthorization) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const params = new URLSearchParams();

    req.nextUrl.searchParams.forEach((value, key) => {
      if (value !== "") {
        params.append(key, value);
      }
    });

    const auth = await getAuthorizationHeader(req);
    const url = new URL(`${process.env.GZL_DTH_API_URL}/items`);
    url.search = params.toString();

    const response = await axios.get(url.toString(), auth ? { headers: { Authorization: auth } } : undefined);

    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return NextResponse.json({
      data: parseContent(response.data),
      contentRange: response.headers["content-range"],
    });
  } catch (err: unknown) {
    const error = err as {
      message?: string;
      response?: { status?: number; data?: unknown };
      config?: { url?: string };
    };

    return NextResponse.json(
      {
        error: {
          message: error.message ?? "Unable to retrieve reports",
          details: error.response?.data ?? "No additional details available",
          url: error.config?.url ?? "Unknown URL",
          status: error.response?.status ?? 500,
        },
      },
      { status: error.response?.status ?? 500 },
    );
  }
}
