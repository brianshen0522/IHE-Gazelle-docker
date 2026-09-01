import { Session } from "next-auth";
import useSignInAutoRedirect from "@hooks/useSignInAutoRedirect";
import React, { useEffect } from "react";
import { Skeleton } from "@gazelle/gazelle-component-ui";
import { signIn, useSession } from "next-auth/react";

const SignInAutoRedirectWrapper = ({ children }: Readonly<{ children: React.ReactNode }>) => {
  const { data: session, status } = useSession() as { data: Session | null; status: "loading" | "authenticated" | "unauthenticated" };
  useSignInAutoRedirect();

  // If status says authenticated but token is missing (refresh failed), force login
  useEffect(() => {
    const tokenMissing = status === "authenticated" && !session?.access_token;
    if (tokenMissing) {
      signIn("keycloak");
    }
  }, [session?.access_token, status]);

  if (!session) {
    return (
      <div className="flex flex-col gap-2 w-full p-2">
        <Skeleton className="h-8" />
        <Skeleton className="h-screen" />
      </div>
    );
  }

  return <>{children}</>;
};

export default SignInAutoRedirectWrapper;
