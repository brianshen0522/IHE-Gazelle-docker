import axios from 'axios';
import { NextRequest, NextResponse } from 'next/server';

type ParamsProps = {
  userId: string;
}

export async function GET(request: NextRequest, {params}: { params: Promise<ParamsProps> }) {
  try {
    const {userId} = await params;
    const auth = request.headers.get('Authorization');
    const { data, status } = await axios.get(`${process.env.GZL_GUM_API_URL}/v2/users/${userId}`, {
      headers: {
        Authorization: auth,
      },
    });

    return NextResponse.json({ data, status });
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : "Unable to get users summary";
    const statusCode = axios.isAxiosError(err) ? err.response?.status || 500 : 500;

    return NextResponse.json({ error: errorMessage }, { status: statusCode });
  }
}
