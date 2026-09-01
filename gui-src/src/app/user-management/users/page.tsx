import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import UsersWrapper from "../components/users-list/UsersWrapper";

export default async function UserManagement() {
  const session = await getServerSession(authOptions);
  return <UsersWrapper session={session} />;
}
