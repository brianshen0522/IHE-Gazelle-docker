import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import { ToastContainer } from "react-toastify";
import OrganizationsList from "../../components/organization/list/OrganizationsList";
import { SidePanelProvider } from "@gazelle/gazelle-component-ui";
import Unauthorized from "@/shared/components/auth/Unauthorized";
import { canManageOrganizations } from "@user-management/utils/permissions";

const OrganizationsListPage = async () => {
  const session = await getServerSession(authOptions);

  if (!canManageOrganizations(session)) {
    return <Unauthorized />;
  }

  return (
    <SidePanelProvider>
      <OrganizationsList session={session} />
      <ToastContainer />
    </SidePanelProvider>
  );
};

export default OrganizationsListPage;
