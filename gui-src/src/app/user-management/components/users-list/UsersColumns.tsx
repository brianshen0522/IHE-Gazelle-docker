"use client";
import { useMemo, useId } from "react";
import { createColumnHelper } from "@tanstack/react-table";
import { User } from "@/app/user-management/components/user-management/Types";
import { customFilterFn } from "@user-management/hooks/useFilterOptions";
import { useGetOrganizations } from "@/app/user-management/hooks/swr/useGetGroups";
import { useTranslation } from "react-i18next";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { OrganizationCell } from "@user-management/components/users-list/cells/OrganizationCell";
import { RolesCell } from "@user-management/components/users-list/cells/RolesCell";
import { StatusBadgeCell } from "@/shared/components/table/StatusBadgeCell";

const columnHelper = createColumnHelper<User>();

export const UsersColumns = () => {
  const columns = useUsersColumns();
  return <>{columns}</>;
};

export const useUsersColumns = () => {
  const { data: orgaData } = useGetOrganizations();
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);
  const id = useId();

  const orgaMapping = useMemo(() => {
    return new Map<string, string>(orgaData?.data?.map((orga: Record<string, string>) => [orga.id, orga.name]) || []);
  }, [orgaData]);

  // Columns definition for the users list table
  return useMemo(
    () => [
      columnHelper.accessor("delegated", {
        header: () => <span>{t("gzl.user.interface.type")}</span>,
        cell: (info) => (
          <StatusBadgeCell
            id={id}
            value={info.getValue()}
            trueVariant="variant-2"
            falseVariant="variant-1"
            trueLabel={t("gzl.gum.delegated")}
            falseLabel={t("gzl.gum.local")}
          />
        ),
      }),
      columnHelper.accessor("firstName", {
        header: () => <span>{t("gzl.gum.first_name")}</span>,
        cell: (info) => info.getValue(),
      }),
      columnHelper.accessor("lastName", {
        header: () => <span>{t("gzl.gum.last_name")}</span>,
        cell: (info) => info.getValue(),
      }),
      columnHelper.accessor("email", {
        header: () => <span>{t("gzl.gum.email")}</span>,
        cell: (info) => info.getValue(),
      }),
      columnHelper.accessor("organizationId", {
        header: () => <span>{t("gzl.gum.organization")}</span>,
        cell: (info) => <OrganizationCell organizationId={info.getValue()} orgaMapping={orgaMapping} />,
      }),
      columnHelper.accessor("groupIds", {
        header: () => <span>{t("gzl.gum.roles")}</span>,
        cell: (info) => <RolesCell groupIds={info.getValue()} t={t} />,
        filterFn: customFilterFn,
        enableSorting: false,
      }),
      columnHelper.accessor("activated", {
        header: () => <span>{t("gzl.gum.status")}</span>,
        cell: (info) => (
          <StatusBadgeCell
            id={id}
            value={info.getValue() === true}
            trueVariant="success"
            falseVariant="failed"
            trueLabel={t("gzl.gum.activated")}
            falseLabel={t("gzl.gum.disabled")}
          />
        ),
      }),
      columnHelper.accessor("lastLoginTimestamp", {
        header: () => <span>{t("gzl.gum.last_login")}</span>,
        cell: (info) => {
          const value = info.getValue();
          return value ? <span>{formatDate(Number(value))}</span> : "";
        },
        enableColumnFilter: false,
      }),
    ],
    [id, t, orgaMapping, formatDate],
  );
};
