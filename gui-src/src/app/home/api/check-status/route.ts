import { NextRequest, NextResponse } from 'next/server';
import { isAllowedOutboundUrl } from '@shared/utils/outboundUrl';

export async function GET(req: NextRequest) {
  try {
    const status = req.nextUrl.searchParams.get('status');

    if (!status) {
      return NextResponse.json({ error: 'Missing status parameter' }, { status: 400 });
    }

    const url = new URL(status);
    if (!isAllowedOutboundUrl(url, [process.env.BASE_URL])) {
      return NextResponse.json({ error: 'URL not allowed' }, { status: 403 });
    }

    const response = await fetch(url, { method: 'HEAD', redirect: 'manual' });

    return NextResponse.json({ status: response.status });
  } catch (err: any) {
    console.warn(err + ' not accessible')
    return NextResponse.json({ error: err?.message || "Unable to get status"}, { status: err.response?.status || 500 });
  }
}
