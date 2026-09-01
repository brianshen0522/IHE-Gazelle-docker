import axios from "axios";
import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
  const delegated = req.nextUrl.searchParams.get("delegated");
  const archived = req.nextUrl.searchParams.get("archived");
  const limit = req.nextUrl.searchParams.get("_limit");
  const offset = req.nextUrl.searchParams.get("_offset");
  const search = req.nextUrl.searchParams.get("search");

  try {
    const params: Record<string, string | boolean> = {};
    if (delegated !== null) params.delegated = delegated === "true";
    if (archived !== null) params.archived = archived === "true";
    if (limit !== null) params._limit = limit;
    if (offset !== null) params._offset = offset;
    if (search) params.search = search;

    const { data, status } = await axios.get(`${process.env.GZL_GUM_API_URL}/organizations`, { params });
    return NextResponse.json({ data, status });
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.data) {
      return NextResponse.json({ error: err.response.data.error }, { status: err.response.status ?? 500 });
    }
    return NextResponse.json({ error: (err as Error)?.message || "Unable to get organizations" }, { status: 500 });
  }
}
