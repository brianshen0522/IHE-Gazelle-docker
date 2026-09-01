"use server";
import { cookies } from "next/headers";

const COOKIE_NAME = "Cookie_Consent_Gazelle";
const MAX_AGE = 60 * 60 * 24 * 183; // 6 months as part of exigence [BR-3][NAV-058]

export async function setCookieConsent() {
  const cookieStore = await cookies();
  cookieStore.set(COOKIE_NAME, "true", {
    maxAge: MAX_AGE,
    path: "/",
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
  });
}

export async function getCookieConsent(): Promise<string | null> {
  const cookieStore = await cookies();
  return cookieStore.get(COOKIE_NAME)?.value || null;
}
