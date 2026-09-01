"use client";
import { ScrollTop, useSmallScreen } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { useUsersColumns } from "./UsersColumns";
import UserSidePanel from "../user-management/UserSidePanel";
import TablePaginationWrapper from "@/shared/components/table/TablePaginationWrapper";
import { User } from "../user-management/Types";

const UsersList = () => {
  const isSmallScreen = useSmallScreen();
  const { t } = useTranslation();
  const columns = useUsersColumns();

  const path = "/v2/users";

  return (
    <div className="flex flex-grow overflow-hidden p-2">
      <div className="flex flex-col flex-grow overflow-hidden gap-2">
        <TablePaginationWrapper<User>
          tableColumns={columns}
          type="users"
          path={path}
          emptyDataMessage={t("gzl.gum.no_users_available")}
          paramPrefix="_"
          paramMap={{ sortBy: "_sort" }}
        />
      </div>
      <ScrollTop />
      {!isSmallScreen && <UserSidePanel />}
    </div>
  );
};

export default UsersList;
