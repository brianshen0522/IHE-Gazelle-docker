import type { Metadata } from "next";
import { EditUserContextProvider } from "@user-management/context/EditUserContext";
import { getServerSession } from "next-auth";
import { authOptions } from "@auth/authOptions";
import AuthGuard from "../../shared/components/auth/AuthGuard";

export const metadata: Metadata = {
  title: "User Management",
  description: "Gazelle User Management",
};

export default async function AuthenticatedLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const session = await getServerSession(authOptions);

  return (
    <AuthGuard session={session}>
      <EditUserContextProvider>{children}</EditUserContextProvider>
    </AuthGuard>
  );
}
