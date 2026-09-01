import i18n from "i18next";

const map: Map<string, string> = new Map();
map.set("role:gazelle_admin", "gazelle_admin");
map.set("role:project_admin", "project_admin");
map.set("role:testing_session_manager", "testing_session_manager");
map.set("role:monitor", "monitor");
map.set("role:late_registration", "late_registration");
map.set("role:test_designer", "test_designer");
map.set("role:sut_operator", "sut_operator");

/**
 *
 * @param organizationId the organization id
 * @returns the map composed of groupIds to group display name
 */
export const createSpecificGroupMap = (organizationId: string, organizationName: string) => {
  const newMap = new Map(map);
  const orgaName = "organization_admin";
  const processOrgaName = i18n.t(orgaName) + " (" + organizationName + ")";
  newMap.set("org-adm:" + organizationId, processOrgaName);
  return newMap;
};

/**
 * Retrieve the display group name from technical group id
 * @param groupId the id of the group
 * @returns the display string corresponding to the group
 */
export const getDisplayGroupFromGroupId = (groupId: string) => {
  let result: string | undefined = map.get(groupId);

  if (result === undefined && groupId?.startsWith("org-adm:")) result = "organization_admin";

  return result;
};

// Converts an array of group IDs to a comma-separated display string of role names
export function getRoleDisplayFromGroups(userGroups: string[] | undefined, t: (key: string) => string): string {
  if (userGroups === undefined) {
    return "";
  }

  const userGroupFiltered = userGroups.filter((userGroup) => userGroup !== undefined && !userGroup.startsWith("org:") && userGroup.length > 0);

  return userGroupFiltered.map((userGroup) => t("gzl.gum." + String(getDisplayGroupFromGroupId(userGroup) ?? userGroup))).join(", ");
}
