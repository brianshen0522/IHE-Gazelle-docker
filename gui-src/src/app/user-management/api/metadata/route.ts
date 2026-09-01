import axios from "axios";
import { NextResponse } from "next/server";

export async function GET() {
  try {
    if (!process.env.GZL_GUM_API_URL) {
      throw new Error("GZL_GUM_API_URL is not defined");
    }

    const response = await axios.get(`${process.env.GZL_GUM_API_URL.replace("/rest", "/metadata")}`);
    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.data;
    return NextResponse.json(data);
  } catch (err: unknown) {
    console.error(err);
    return NextResponse.json(
      { error: (err as Error)?.message || "Unable to get gum backend metadata" },
      { status: (err as any)?.response?.status || 500 },
    );
  }
}
