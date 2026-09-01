import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import CreateOrganization from "../../components/organization/create/CreateOrganization";
import { ToastContainer } from "react-toastify";
import Unauthorized from "@/shared/components/auth/Unauthorized";
import { canManageOrganizations } from "@user-management/utils/permissions";

const CreateOrganizationPage = async () => {
  const session = await getServerSession(authOptions);

  if (!canManageOrganizations(session)) {
    return <Unauthorized />;
  }

  return (
    <>
      <CreateOrganization session={session} />
      <ToastContainer />
    </>
  );
};

export default CreateOrganizationPage;
