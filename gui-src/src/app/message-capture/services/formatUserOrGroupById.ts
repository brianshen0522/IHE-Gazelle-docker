"use server";
import { getOrganizationById } from "./getGroups";
import { getUserSummaryById } from "./getUserSummaryById";
import { getDisplayGroupFromGroupId } from "@/app/user-management/utils/roleMappers";
import type { Session } from "next-auth";

function isGroup(id: string): boolean {
  return getDisplayGroupFromGroupId(id) !== undefined || id.startsWith("org") || id === "gazelle" || id === "user";
}

/**
 * Format a user or group for display using its id
 * @param id the id of the user or group
 * @param session the current logged entity
 * @return an object containing the <strong>id</strong> passed in parameter, the <strong>name</strong> of the user/group or a placeholder if not found,
 * the <strong>organization name</strong> in the case of user otherwise an empty string.
 */
export async function formatUserOrGroupById(id: string, _session?: Session | null): Promise<{
  id: string;
  name: string;
  organization: string;
}> {
  let formattedResult = {
    id,
    name: "Unknown user or group",
    organization: "",
  };

  if (isGroup(id)) {
    //if failed to get user, get group
    let groupName: string | undefined;
    if (id.startsWith("org")) {
      const orgaId = id.split(":")[1];
      const result = await getOrganizationById(orgaId);
      if (id.startsWith("org-adm")) {
        const organizationName = result.organization?.name ?? orgaId;
        groupName = organizationName + " (" + "gzl.gum." + String(getDisplayGroupFromGroupId(id)) + ")";
      } else {
        groupName = result.organization?.name ?? orgaId;
      }
    } else {
      const displayGroupId = getDisplayGroupFromGroupId(id);
      groupName = displayGroupId !== undefined ? "gzl.gum." + String(displayGroupId) : id;
    }
    formattedResult = {
      id,
      name: groupName ?? formattedResult.name,
      organization: "",
    };
  } else {
    //First get user
    const result = await getUserSummaryById(id);
    if (result.error === undefined) {
      const summary = result.userSummary;
      const name = [summary?.firstName, summary?.lastName].filter(Boolean).join(" ") || id;
      const resultOrga = await getOrganizationById(encodeURI(summary!.organizationId));

      formattedResult = {
        id,
        name: name,
        organization: resultOrga.organization?.name ?? "Unknown organization",
      };
    } else {
      console.error("User not found with id: " + id, result.error);
    }
  }
  return formattedResult;
}
