import { NextRequest, NextResponse } from "next/server";
import { getToken } from "next-auth/jwt";

const adminRoles = ["role:gazelle_admin", "role:project_admin", "role:testing_session_manager"];
const TEST_DESIGNER_ROLE = "role:test_designer";

export async function proxy(request: NextRequest) {
  // Skip middleware for auth-related paths and static assets
  if (request.nextUrl.pathname.includes("/unauthorized") || request.nextUrl.pathname.includes("/api/auth")) {
    return NextResponse.next();
  }

  try {
    const path = request.nextUrl.pathname;
    const token = await getToken({
      req: request,
      secret: process.env.NEXTAUTH_SECRET,
    });

    // If path starts with /admin, require token and required role
    if (path.startsWith("/admin")) {
      const userRoles: string[] = Array.isArray(token?.roles) ? token.roles : [];
      const hasRequiredRoles = adminRoles.some((role) => userRoles.includes(role)) || userRoles.includes(TEST_DESIGNER_ROLE);
      if (!token || !hasRequiredRoles) {
        return NextResponse.redirect(new URL("/gazelle/unauthorized", request.url));
      }
    }

    // Read the excluded paths from the environment variable
    const excludedPaths = process.env.GZL_EXCLUDED_PATHS?.split(",") || [];

    // Check if the request path starts with any of the excluded paths
    if (excludedPaths.some((excludedPath) => path.startsWith(excludedPath))) {
      return NextResponse.rewrite(new URL("/404", request.url));
    }

    return NextResponse.next();
  } catch (error) {
    console.error("Middleware error:", error);
    return NextResponse.next();
  }
}

export const config = {
  matcher: "/((?!_next/static|_next/image|favicon.ico).*)",
};
