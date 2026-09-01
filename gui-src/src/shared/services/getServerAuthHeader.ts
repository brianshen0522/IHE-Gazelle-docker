import { getServerSession } from "next-auth";
import type { NextRequest } from "next/server";
import { authOptions } from "@auth/authOptions";

export async function getAuthorizationHeader(req: NextRequest): Promise<string | undefined> {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = (session)?.access_token;
    if (accessToken) {
      return `Bearer ${accessToken}`;
    }
  } catch (e) {
    console.error("Failed to get server session:", e);
    // fall back to the incoming header
  }
  return req.headers.get("Authorization") ?? undefined;
}

