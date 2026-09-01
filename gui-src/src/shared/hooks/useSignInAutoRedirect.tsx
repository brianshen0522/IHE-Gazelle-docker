"use client";
import { useEffect, useMemo } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { signIn, useSession } from "next-auth/react";
import { Route } from "next";

export default function useSignInAutoRedirect() {
  const router = useRouter();
  const pathname = usePathname();
  const { status } = useSession();
  const searchParams = useSearchParams();

  const callBackUrl = useMemo(() => {
    let url = searchParams.get("callbackUrl") ?? pathname;

    if (url.includes("api")) {
      url = "/";
    }

    return url;
  }, [searchParams, pathname]);

  useEffect(() => {
    if (status === "unauthenticated") {
      signIn("keycloak");
    } else if (status === "authenticated") {
      // Redirect only if the current path is different from the callbackUrl
      if (pathname !== callBackUrl) {
        router.push(callBackUrl as Route);
      }
    }
  }, [callBackUrl, pathname, router, status]);
}
