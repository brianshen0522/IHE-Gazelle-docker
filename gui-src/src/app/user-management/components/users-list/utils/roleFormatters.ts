import { getDisplayGroupFromGroupId } from "@/app/user-management/utils/roleMappers";

// Converts an array of group IDs to a comma-separated display string of role names
export function getRoleDisplayFromGroups(userGroups: string[] | undefined, t: (key: string) => string): string {
  if (userGroups === undefined) {
    return "";
  }

  const userGroupFiltered = userGroups.filter((userGroup) => userGroup !== undefined && !userGroup.startsWith("org:") && userGroup.length > 0);

  return userGroupFiltered.map((userGroup) => t("gzl.gum." + String(getDisplayGroupFromGroupId(userGroup) ?? userGroup))).join(", ");
}
