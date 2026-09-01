import { useEffect, useMemo, useState } from "react";
import { Session } from "next-auth";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";
import {
  GAZELLE_ADMIN,
  LATE_REGISTRATION,
  MONITOR,
  PREFIX_ORGANIZATION_ADMIN,
  PROJECT_ADMIN,
  TEST_DESIGNER,
} from "@user-management/utils/permissions";
import { getDisplayGroupFromGroupId } from "@/app/user-management/utils/roleMappers";
import { useGetRoles, useGetOrganizations } from "./swr/useGetGroups";

export type Option = {
  value: string;
  label: string;
};

// Custom filter function for the 'groups' column
export const customFilterFn = (row: any, columnId: string, filterValue: string) => {
  if (!filterValue) return true;
  const groups = row.getValue(columnId) as string[];
  const processedGroups = groups.map((group) => getDisplayGroupFromGroupId(group) ?? group);
  return processedGroups.includes(filterValue);
};

const useFilterOptions = (id: string): Option[] => {
  const { t } = useTranslation();
  const [options, setOptions] = useState<Option[]>([]);
  const { data: session } = useSession() as { data: Session };
  const { data: orgaData } = useGetOrganizations({});
  const { data: groupData } = useGetRoles(session);

  // Calculate options
  useEffect(() => {
    let calculatedOptions: Option[] = [];

    if (id === "delegated") {
      calculatedOptions = [
        { value: "true", label: "gzl.gum.delegated" },
        { value: "false", label: "gzl.gum.local" },
      ];
    } else if (id === "organizationId") {
      calculatedOptions = orgaData?.data?.map((orga: Record<string, string>) => ({ value: orga.id, label: orga.name })) ?? [];
    } else if (id === "groupIds") {
      // VLD : Hardcoded options here for filter because we have difficulties to translate dynamic groups
      calculatedOptions = [];
      calculatedOptions.push(
        { value: GAZELLE_ADMIN, label: "gzl.gum.gazelle_admin" },
        { value: PROJECT_ADMIN, label: "gzl.gum.project_admin" },
        { value: MONITOR, label: "gzl.gum.monitor" },
        { value: TEST_DESIGNER, label: "gzl.gum.test_designer" },
        { value: LATE_REGISTRATION, label: "gzl.gum.late_registration" },
        { value: PREFIX_ORGANIZATION_ADMIN, label: "gzl.gum.organization_admin" },
      );
      calculatedOptions.sort((a, b) => t(a.label).localeCompare(t(b.label)));
    } else if (id === "activated") {
      calculatedOptions = [
        { value: "true", label: "gzl.gum.activated" },
        { value: "false", label: "gzl.gum.disabled" },
      ];
    }

    // Remove duplicates from options
    const uniqueOptions = Array.from(new Set(calculatedOptions.map((option) => option.value)))
      .map((value) => calculatedOptions.find((option) => option.value === value))
      .filter((option): option is Option => option !== undefined);

    setOptions(uniqueOptions);
  }, [id, groupData, orgaData, t]);

  const translatedOptions = useMemo(() => {
    return options.map((option) => ({
      ...option,
      label: t(option.label),
    }));
  }, [options, t]);

  return translatedOptions;
};

export default useFilterOptions;
