import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest) {
  try {
    const status = req.nextUrl.searchParams.get('status');

    if (!status) {
      return NextResponse.json({ error: 'Missing status parameter' }, { status: 400 });
    }

    const url = new URL(status);
    const response = await fetch(url, { method: 'HEAD' });

    return NextResponse.json({ status: response.status });
  } catch (err: any) {
    console.warn(err + ' not accessible')
    return NextResponse.json({ error: err?.message || "Unable to get status"}, { status: err.response?.status || 500 });
  }
}
