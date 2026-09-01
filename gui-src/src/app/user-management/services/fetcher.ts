import { signIn, signOut } from "next-auth/react";

export const fetcher = async (url: string, token?: string, init?: RequestInit) => {
  const headers = {
    ...init?.headers,
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };

  const res = await fetch(url, { ...init, headers });

  // Handle expired token
  if (res.status === 401) {
    // Trigger token refresh silently
    const refreshResult = await signIn("keycloak", { redirect: false });

    if (!refreshResult?.ok) {
      // If refresh failed, log the user out
      signOut({ callbackUrl: "/", redirect: true });
      return;
    }

    throw new Error("Session refreshed. Retry the request.");
  }

  if (!res.ok) {
    throw new Error(`Error: ${res.statusText} (status: ${res.status})`);
  }

  return res.json();
};
