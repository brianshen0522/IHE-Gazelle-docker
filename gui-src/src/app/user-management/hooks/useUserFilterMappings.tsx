import { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useSession } from "next-auth/react";
import { Session } from "next-auth";
import { PREFIX_ORGANIZATION_ADMIN } from "@user-management/utils/permissions";
import { getDisplayGroupFromGroupId } from "@/app/user-management/utils/roleMappers";
import { useGetOrganizations, useGetRoles } from "./swr/useGetGroups";

/**
 * Hook to generate value display mappings for user filters
 * Converts technical IDs to human-readable translated names
 */
export const useUserFilterMappings = () => {
  const { t } = useTranslation();
  const { data: session } = useSession() as { data: Session };
  // Get all organizations with high limit to avoid pagination - tmp solution
  const { data: orgaData } = useGetOrganizations({ limit: 1000 });
  // Get all roles dynamically from the API
  const { data: rolesData } = useGetRoles(session);

  const valueDisplayMapping = useMemo(() => {
    const mapping: Record<string, Record<string, string>> = {};

    // Map organization IDs to names (handle both plain IDs and org:id format)
    if (orgaData?.data) {
      mapping.organizationId = {};
      orgaData.data.forEach((orga: Record<string, string>) => {
        // Map plain ID
        mapping.organizationId[orga.id] = orga.name;
        // Map org:id format
        mapping.organizationId[`org:${orga.id}`] = orga.name;
        // Also map the shortName if it exists and is different from id
        if (orga.shortName && orga.shortName !== orga.id) {
          mapping.organizationId[orga.shortName] = orga.name;
          mapping.organizationId[`org:${orga.shortName}`] = orga.name;
        }
      });
    }

    // Map group IDs to display names
    const groupMapping: Record<string, string> = {};

    if (rolesData?.data) {
      rolesData.data.forEach((role: { id: string; reference: string; type: string }) => {
        const displayName = getDisplayGroupFromGroupId(role.id);
        if (displayName) {
          const translatedName = t(`gzl.gum.${displayName}`);

          // Map the full id (e.g., "role:gazelle_admin") for display
          groupMapping[role.id] = translatedName;

          // Map the reference (e.g., "gazelle_admin") for API queries
          // This is added last so the reverse mapping will use this format
          groupMapping[role.reference] = translatedName;
        }
      });
    }

    // Generic fallbacks
    groupMapping["org"] = t("gzl.gum.org");
    groupMapping["org-adm"] = t("gzl.gum.org-adm");
    groupMapping[PREFIX_ORGANIZATION_ADMIN] = t("gzl.gum.org-adm");

    mapping.groupIds = groupMapping;
    mapping.group = groupMapping;

    // Map boolean values
    mapping.delegated = {
      true: t("gzl.gum.delegated"),
      false: t("gzl.gum.local"),
    };

    mapping.activated = {
      true: t("gzl.gum.activated"),
      false: t("gzl.gum.disabled"),
    };

    return mapping;
  }, [orgaData, rolesData, t]);

  return valueDisplayMapping;
};
