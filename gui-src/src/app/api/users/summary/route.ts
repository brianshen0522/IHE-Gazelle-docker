import axios from "axios";
import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
  const params: { [key: string]: string | null } = {
    search: req.nextUrl.searchParams.get("search"),
    offset: req.nextUrl.searchParams.get("offset"),
    limit: req.nextUrl.searchParams.get("limit"),
    sortBy: req.nextUrl.searchParams.get("sortBy"),
    sortOrder: req.nextUrl.searchParams.get("sortOrder"),
  };
  const auth = req.headers.get("Authorization");

  // Remove null, undefined, or empty string properties
  const filteredParams = params
    ? Object.fromEntries(Object.entries(params).filter(([, value]) => value !== null && value !== "" && value !== undefined))
    : {};
  const url = new URL(`${process.env.GZL_GUM_API_URL}/v2/users/summary`);
  url.search = new URLSearchParams(filteredParams as Record<string, string>).toString();
  try {
    const response = await axios.get(url.toString(), {
      headers: {
        Authorization: auth,
      },
    });

    return NextResponse.json(await response.data);
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : "Unable to get users summary";
    const statusCode = axios.isAxiosError(err) ? err.response?.status || 500 : 500;

    return NextResponse.json({ error: errorMessage }, { status: statusCode });
  }
}
