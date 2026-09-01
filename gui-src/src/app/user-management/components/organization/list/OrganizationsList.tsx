"use client";
import { ScrollTop, useSmallScreen } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import TablePaginationWrapper from "@/shared/components/table/TablePaginationWrapper";
import { useOrganizationsColumns } from "./OrganizationsColumns";
import { WithSession } from "@/shared/types/session";
import UserManagementHeader from "../../UserManagementHeader";
import OrganizationSidepanel from "./OrganizationSidepanel";
import GenericFilters from "@/shared/components/filter/GenericFilters";
import { useSearchParamsUrl } from "@/shared/hooks/useSearchParamsUrl";

const OrganizationsList = ({ session }: WithSession) => {
  const isSmallScreen = useSmallScreen();
  const { t } = useTranslation();
  const columns = useOrganizationsColumns();
  const { searchParameters } = useSearchParamsUrl();

  const breadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.user.interface.user_management"), url: "" },
    { label: t("gzl.user.interface.organizations"), url: "" },
  ];

  const path = "/organizations";

  const indexNameMapping: Record<string, string> = {
    delegated: t("gzl.user.interface.type"),
    archived: t("gzl.user.interface.status").replace(/^./, (c) => c.toUpperCase()),
    name: t("gzl.user.interface.name"),
    shortname: t("gzl.user.interface.shortname"),
  };

  const valueDisplayMapping: Record<string, Record<string, string>> = {
    delegated: {
      true: t("gzl.gum.delegated"),
      false: t("gzl.gum.local"),
    },
    archived: {
      true: t("gzl.user.interface.archived"),
      false: t("gzl.user.interface.active"),
    },
  };

  return (
    <>
      <UserManagementHeader id="list-organization-header" title="gzl.user.interface.user_management" breadcrumbs={breadcrumbs} session={session} />

      <GenericFilters
        defaultFilters={{ archived: "false" }}
        searchParameters={searchParameters}
        indexNameMapping={indexNameMapping}
        type="organizations"
        indexPath={path}
        valueDisplayMapping={valueDisplayMapping}
        customIndexFilter={(item) => !["search", "lastUpdateTimestamp"].includes(item.name)}
        globalSearch={["search"]}
      />

      <div className="flex flex-grow overflow-hidden p-2">
        <div className="flex flex-col flex-grow overflow-hidden gap-2">
          <TablePaginationWrapper
            tableColumns={columns}
            type="organizations"
            path={path}
            emptyDataMessage={t("gzl.user.interface.no_organizations_available")}
            getRowId={(row) => row.id}
            paramPrefix="_"
            paramMap={{ sortBy: "_sort" }}
          />
        </div>
        <ScrollTop />
        {!isSmallScreen && <OrganizationSidepanel session={session} />}
      </div>
    </>
  );
};

export default OrganizationsList;
