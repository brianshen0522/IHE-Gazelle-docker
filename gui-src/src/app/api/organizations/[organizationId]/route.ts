import axios from "axios";
import {NextRequest, NextResponse} from "next/server";

type ParamsProps = {
  organizationId: string;
}

export async function GET(request: NextRequest, {params}: { params: Promise<ParamsProps> }) {
  const {organizationId} = await params
  try {
    const delegated: string | null = request.nextUrl.searchParams.get('delegated');
    const paramRequest = delegated === null ? {} : { params: { delegated: (delegated === "true") }} ;
    const {data, status} = await axios.get(`${process.env.GZL_GUM_API_URL}/organizations/${organizationId}`, paramRequest);
    return NextResponse.json({data, status});
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.data) {
      return NextResponse.json({error: err.response.data.error}, {status: err.response.status ?? 500});
    } else {
      console.warn(err);
      return NextResponse.json({error: (err as Error)?.message || `Request to get organization by id failed (${organizationId})`}, {status: 500})
    }
  }
}