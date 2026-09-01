import React, { useId } from "react";
import { Checkbox, SectionTitle } from "@gazelle/gazelle-component-ui";
import { Session } from "next-auth";
import { useTranslation } from "react-i18next";
import { useEditUserContext } from "@user-management/context/EditUserContext";
import { useGetOrganizationFromId } from "@/shared/hooks/useGetUserInformation";
import { canEditRole } from "@user-management/utils/permissions";
import { createSpecificGroupMap } from "@/app/user-management/utils/roleMappers";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";

const RoleAttribution = ({ session, account, onSelf, delegated }: { session: Session; account: boolean; onSelf: boolean; delegated: boolean }) => {
  const id = useId();
  const { t } = useTranslation();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const { userGroups, setUserGroups, user, userActivation } = useEditUserContext();

  const { data: orgaData } = useGetOrganizationFromId(user?.organizationId ?? "");
  const orgaName = orgaData?.data?.name;

  const handleRoleChange = (group: string) => {
    const updatedGroups = userGroups.includes(group) ? userGroups.filter((r) => r !== group) : [...userGroups, group];
    setUserGroups(updatedGroups);
    setHasUnsavedChanges(true);
  };

  const buildCheckboxFromParam = (groupKey: string) => {
    const selected = userGroups.includes(groupKey);
    if (account && !onSelf && !selected) {
      return <></>;
    }
    const editable = canEditRole(groupKey, onSelf, delegated, session);

    return (
      <div key={groupKey} className={"ml-5"}>
        <Checkbox
          id={id}
          label={getTranslationForRole(groupsMapping.get(groupKey) ?? "")}
          value={userGroups.includes(groupKey)}
          disabled={!editable || !userActivation}
          onChange={() => handleRoleChange(groupKey)}
        />
      </div>
    );
  };

  const getTranslationForRole = (role: string): string => {
    if (role.includes("organization")) return t("gzl.gum.organization_admin") + role.replaceAll(/.{0,200} \(/gi, " (");
    return t("gzl.gum." + role);
  };

  const groupsMapping: Map<string, string> = createSpecificGroupMap(user?.organizationId ?? "", orgaName ?? "");
  const listRoles: React.ReactElement[] = Array.from(groupsMapping.keys()).map((groupKey) => buildCheckboxFromParam(groupKey));

  return (
    <div className="flex flex-col gap-4">
      <SectionTitle id={t("gzl.gum.roles")} title={t("gzl.gum.roles")} />
      {listRoles}
    </div>
  );
};

export default RoleAttribution;
