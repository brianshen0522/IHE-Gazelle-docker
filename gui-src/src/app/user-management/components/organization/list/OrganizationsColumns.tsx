"use client";
import { useMemo } from "react";
import { createColumnHelper } from "@tanstack/react-table";
import { Organization } from "@/app/user-management/components/user-management/Types";
import { useTranslation } from "react-i18next";
import { StatusBadgeCell } from "@/shared/components/table/StatusBadgeCell";
import { Badge } from "@gazelle/gazelle-component-ui";
import useDateFormat from "@/shared/hooks/useDateFormat";

const columnHelper = createColumnHelper<Organization>();

export const OrganizationsColumns = () => {
  const columns = useOrganizationsColumns();
  return <>{columns}</>;
};

export const useOrganizationsColumns = () => {
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);
  return useMemo(
    () => [
      columnHelper.accessor("delegated", {
        id: "delegated",
        header: () => <span className="capitalize">{t("gzl.user.interface.type")}</span>,
        cell: (info) => (
          <StatusBadgeCell
            id={info.row.original.id}
            value={info.getValue()}
            trueVariant="variant-2"
            falseVariant="variant-1"
            trueLabel={t("gzl.gum.delegated")}
            falseLabel={t("gzl.gum.local")}
          />
        ),
      }),
      columnHelper.accessor("name", {
        id: "name",
        header: () => <span>{t("gzl.user.interface.name")}</span>,
        cell: (info) => (
          <div className="flex items-center gap-2">
            {info.getValue()}{" "}
            {info.row.original.archived && (
              <Badge id="archived-organization-badge" variant="warning">
                {t("gzl.user.interface.archived")}
              </Badge>
            )}
          </div>
        ),
      }),
      columnHelper.accessor("shortname", {
        id: "shortname",
        header: () => <span>{t("gzl.user.interface.short_name")}</span>,
        cell: (info) => info.getValue(),
      }),
      columnHelper.accessor("lastUpdateTimestamp", {
        id: "lastUpdateTimestamp",
        header: () => <span>{t("gzl.user.interface.last_update")}</span>,
        cell: (info) => {
          const value = info.getValue();
          return value ? <span>{formatDate(Number(value))}</span> : "";
        },
        enableColumnFilter: false,
      }),
    ],
    [formatDate, t],
  );
};
