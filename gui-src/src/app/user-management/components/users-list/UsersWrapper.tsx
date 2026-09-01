"use client";
import { useTranslation } from "react-i18next";
import { SidePanelProvider } from "@gazelle/gazelle-component-ui";
import { ToastContainer } from "react-toastify";
import { canManageUsers } from "@user-management/utils/permissions";
import { WithSession } from "@/shared/types/session";
import UsersList from "@/app/user-management/components/users-list/UsersList";
import Unauthorized from "@/shared/components/auth/Unauthorized";
import UserManagementHeader from "../UserManagementHeader";
import GenericFilters from "@/shared/components/filter/GenericFilters";
import { useSearchParamsUrl } from "@/shared/hooks/useSearchParamsUrl";
import { useUserFilterMappings } from "@user-management/hooks/useUserFilterMappings";

export const USERS_PATH = "/user-management/users";

const UsersWrapper = ({ session }: WithSession) => {
  const { t } = useTranslation();
  const { searchParameters } = useSearchParamsUrl();
  const valueDisplayMapping = useUserFilterMappings();

  const indexNameMapping: Record<string, string> = {
    delegated: t("gzl.user.interface.type"),
    firstName: t("gzl.user.interface.first_name"),
    lastName: t("gzl.user.interface.last_name"),
    organizationName: t("gzl.user.interface.organization"),
    group: t("gzl.user.interface.group"),
    activated: t("gzl.user.interface.status").charAt(0).toUpperCase() + t("gzl.user.interface.status").slice(1),
    email: t("gzl.user.interface.email"),
  };

  return (
    <SidePanelProvider>
      <UserManagementHeader id="users-header" title="gzl.user.interface.user_management" session={session} />

      <GenericFilters
        searchParameters={searchParameters}
        indexNameMapping={indexNameMapping}
        type="users"
        valueDisplayMapping={valueDisplayMapping}
        customIndexFilter={(item) => !["search", "lastLoginTimestamp", "organizationId"].includes(item.name)}
        globalSearch={["search"]}
      />

      <div className="flex flex-col gap-2 w-full">{session && <>{canManageUsers(session) ? <UsersList /> : <Unauthorized />}</>}</div>
      <ToastContainer />
    </SidePanelProvider>
  );
};

export default UsersWrapper;
