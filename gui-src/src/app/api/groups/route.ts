import axios from 'axios';
import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest) {
  const type: string | null = req.nextUrl.searchParams.get('type');
  const auth = req.headers.get('Authorization');
  try {
    const { data, status } = await axios.get(`${process.env.GZL_GUM_API_URL}/groups`, {
      params: { type: type },
      headers: {
        Authorization: auth!,
      },
    });

    return NextResponse.json({ data, status });
  } catch (err: any) {
    return NextResponse.json({ error: err?.message || "Unable to get groups" }, { status: err.response?.status || 500 });
  }
}
