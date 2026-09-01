import type { Metadata } from "next";
import { getServerSession } from "next-auth";
import { authOptions } from "@auth/authOptions";
import AuthGuard from "../../shared/components/auth/AuthGuard";

export const metadata: Metadata = {
  title: "Validation portal",
  description: "Gazelle validation portal",
};

export default async function AuthenticatedLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const session = await getServerSession(authOptions);

  return <AuthGuard session={session}>{children}</AuthGuard>;
}
