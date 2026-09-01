import { authOptions } from "@/shared/components/auth/authOptions";
import { getServerSession } from "next-auth/next";
import { ToastContainer } from "react-toastify";
import EditOrganization from "../../components/organization/edit/EditOrganization";
import Unauthorized from "@/shared/components/auth/Unauthorized";
import { canEditOrganization } from "@user-management/utils/permissions";

const EditOrganizationPage = async () => {
  const session = await getServerSession(authOptions);

  if (!canEditOrganization(session)) {
    return <Unauthorized />;
  }

  return (
    <>
      <EditOrganization session={session} />
      <ToastContainer />
    </>
  );
};

export default EditOrganizationPage;
