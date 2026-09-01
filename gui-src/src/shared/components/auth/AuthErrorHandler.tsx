"use client";

import { useSession, signIn } from "next-auth/react";
import { useEffect } from "react";
import { usePathname } from "next/navigation";

export function AuthErrorHandler() {
  const { data: session, status } = useSession();
  const pathname = usePathname();

  useEffect(() => {
    // Handle unauthenticated state or token refresh errors
    if (session?.error === "RefreshAccessTokenError" || status === "unauthenticated") {
      signIn("keycloak");
    }
  }, [session, status, pathname]);

  return null;
}
