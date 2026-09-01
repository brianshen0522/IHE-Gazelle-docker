import React from "react";
import type { Metadata } from "next";
import AuthGuard from "@/shared/components/auth/AuthGuard";
import { authOptions } from "@/shared/components/auth/authOptions";
import { getServerSession } from "next-auth/next";
import { TestSessionProvider } from "./context/TestSessionContext";

export const metadata: Metadata = {
  title: "Test execution",
  description: "Gazelle test execution",
};

export default async function TestExecutionLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const session = await getServerSession(authOptions);

  return (
    <AuthGuard session={session}>
      <TestSessionProvider>{children}</TestSessionProvider>
    </AuthGuard>
  );
}
