import axios from "axios";
import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
  try {
    const versionUrl = req.nextUrl.searchParams.get("version");

    if (!versionUrl) {
      return NextResponse.json({ error: "Missing version parameter" }, { status: 400 });
    }

    const url = new URL(versionUrl);
    const response = await axios.get(url.toString());

    if (response.status < 200 || response.status >= 300) {
      return NextResponse.json({});
    }

    const data = await response.data;
    return NextResponse.json({ data: data });
  } catch (err: unknown) {
    console.error("Failed to fetch version:", err);
    return NextResponse.json({ error: "Failed to fetch version data" }, { status: 500 });
  }
}
