"use client";

import { usePathname } from "next/navigation";
import type { Session } from "next-auth";
import AuthTransitionScreen from "@/shared/components/auth/AuthTransitionScreen";
import { AuthErrorHandler } from "@/shared/components/auth/AuthErrorHandler";

type AuthGuardProps = {
  readonly session: Session | null;
  readonly children: React.ReactNode;
};

export default function AuthGuard({ session, children }: AuthGuardProps) {
  const pathname = usePathname();
  const isAuthenticated = !!session?.user;

  // Define routes that should be accessible without authentication
  // Some of these routes are based on ACL requirements (e.g., validation-portal reports)
  const publicRoutes = {
    exact: ["/user-management"], // Exact matches only
    prefix: ["/user-management/registration", "/user-management/validate", "/validation-portal/reports", "/test-execution/reports"], // Prefix matches (includes sub-routes)
  };

  const isExactPublicRoute = publicRoutes.exact.includes(pathname);
  const isPrefixPublicRoute = publicRoutes.prefix.some((route) => pathname.startsWith(route));
  const isPublicRoute = isExactPublicRoute || isPrefixPublicRoute;

  return (
    <>
      {!isPublicRoute && <AuthErrorHandler />}

      {isAuthenticated || isPublicRoute ? children : <AuthTransitionScreen />}
    </>
  );
}
