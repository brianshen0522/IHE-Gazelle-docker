"use server";
import { authOptions } from "@/shared/components/auth/authOptions";
import { getServerSession } from "next-auth/next";

export async function getSessionAuth() {
  const session = await getServerSession(authOptions);
  const accessToken = session?.access_token;
  const userId = session?.user?.gazelleId ?? session?.user?.name;
  if (!accessToken) {
    return { success: false, message: "Error: Authentication token is missing" };
  }
  return { success: true, accessToken, userId, session };
}
