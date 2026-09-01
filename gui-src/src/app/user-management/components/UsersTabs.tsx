"use client";
import { useMemo } from "react";
import { Tabs } from "@gazelle/gazelle-component-ui";
import { usePathname } from "next/navigation";
import { useTranslation } from "react-i18next";
import { useUnsavedChanges } from "@/shared/context/UnsavedChangeContext";
import { canCreateUser, canManageOrganizations, canEditOrganization } from "@user-management/utils/permissions";
import { WithSession } from "@/shared/types/session";
import { USERS_PATH } from "./users-list/UsersWrapper";

interface UsersTabsProps extends WithSession {}

const UsersTabs = ({ session }: UsersTabsProps) => {
  const { t } = useTranslation();
  const { router } = useUnsavedChanges();
  const pathname = usePathname();

  const tabs = useMemo(() => {
    const allTabs = [
      {
        id: "users",
        label: t("gzl.user.interface.users"),
        path: USERS_PATH,
      },
      {
        id: "create-user",
        label: t("gzl.user.interface.create_user"),
        path: "/user-management/user/create",
        permission: canCreateUser(session),
      },
      {
        id: "list-org",
        label: t("gzl.user.interface.organizations"),
        path: "/user-management/organization/list",
        permission: canManageOrganizations(session),
      },
      {
        id: "create-org",
        label: t("gzl.user.interface.create_organization"),
        path: "/user-management/organization/create",
        permission: canManageOrganizations(session),
      },
      {
        id: "edit-org",
        label: t("gzl.user.interface.edit_my_organization"),
        path: "/user-management/organization/edit",
        permission: canEditOrganization(session),
      },
    ];

    return allTabs.filter((tab) => tab.permission !== false);
  }, [t, session]);

  const selectedTabId = tabs.find((tab) => tab.path === pathname)?.id ?? "users";

  const handleTabSelect = (tabId: string) => {
    const tab = tabs.find((t) => t.id === tabId);
    if (tab) {
      router.push(tab.path);
    }
  };

  return (
    <Tabs
      tabNames={tabs.map((tab) => tab.label)}
      onTabSelect={(label) => {
        const tab = tabs.find((t) => t.label === label);
        if (tab) handleTabSelect(tab.id);
      }}
      selectedTab={tabs.find((t) => t.id === selectedTabId)?.label ?? tabs[0].label}
    />
  );
};

export default UsersTabs;
