import CreateUser from "@/app/user-management/components/user-management/create/CreateUser";
import { authOptions } from "@/shared/components/auth/authOptions";
import Unauthorized from "@/shared/components/auth/Unauthorized";
import { canCreateUser } from "@user-management/utils/permissions";
import { getServerSession } from "next-auth/next";

const CreateUserPage = async () => {
  const session = await getServerSession(authOptions);

  if (!canCreateUser(session)) {
    return <Unauthorized />;
  }

  return <CreateUser session={session} />;
};

export default CreateUserPage;
