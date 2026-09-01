import { useCallback, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { formatGroups, formatUsers } from "@gazelle/gazelle-component-ui";
import { User } from "@/app/user-management/components/user-management/Types";
import { useGetUserSummary } from "@/shared/hooks/SWR/useGetUserSummary";
import { useSearchGroupsWithResolvedOrganizationName } from "@message-capture/hooks/useGetGroupsWithOrganizationNames";
import { formatWithTranslation } from "@shared/utils/acl/aclFormatters";
import { UseAclUsersParams, UseAclUsersResult } from "../../types/AccessControlListTypes";

// Custom hook to fetch and format available users and groups for ACL management.
// Automatically filters out users/groups that already have assigned roles.
export const useAclUsers = ({ owners, editors, viewers, session, enabled }: UseAclUsersParams): UseAclUsersResult => {
  const { t } = useTranslation();
  const [offset, setOffset] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");

  const handleSetOffset = useCallback((value: number | ((prevOffset: number) => number)) => {
    setOffset(value);
  }, []);

  const handleSetSearchTerm = useCallback((newTerm: string) => {
    setSearchTerm(newTerm);
  }, []);

  // Fetch users
  const { data: usersData, isLoading: isLoadingUsers } = useGetUserSummary({
    offset,
    limit: 100,
    search: searchTerm,
    enabled,
  });

  // Fetch groups
  const { data: groupsData, isLoading: isLoadingGroups } = useSearchGroupsWithResolvedOrganizationName({
    offset,
    limit: 100,
    search: searchTerm,
    session,
    enabled,
  });

  // Format users from API
  const formattedAvailableUsers = useMemo(() => formatUsers(usersData?.users as User[]), [usersData?.users]);

  // Format groups from API
  const formattedAvailableGroups = useMemo(() => formatGroups(groupsData?.resolvedData as any[]), [groupsData?.resolvedData]);

  // Get list of existing member IDs
  const existingMemberIds = useMemo(() => {
    const ownerIds = owners?.map((u) => u.id) || [];
    const editorIds = editors?.map((u) => u.id) || [];
    const viewerIds = viewers?.map((u) => u.id) || [];

    return [...ownerIds, ...editorIds, ...viewerIds];
  }, [owners, editors, viewers]);

  // Filter available users to exclude those who already have roles
  const availableUsers = useMemo(
    () =>
      formattedAvailableUsers.filter((user: { id: string; name: string }) => {
        if (!user?.id) return false;
        return !existingMemberIds.includes(user.id);
      }),
    [formattedAvailableUsers, existingMemberIds],
  );

  // Filter available groups to exclude those who already have roles and apply translations
  const availableGroups = useMemo(
    () =>
      formattedAvailableGroups
        .filter((group: { id: string; name: string }) => {
          if (!group?.id) return false;
          return !existingMemberIds.includes(group.id);
        })
        .map((group) => formatWithTranslation(group, t)),
    [formattedAvailableGroups, existingMemberIds, t],
  );

  return {
    availableUsers,
    availableGroups,
    offset,
    setOffset: handleSetOffset,
    searchTerm,
    setSearchTerm: handleSetSearchTerm,
    isLoading: isLoadingUsers || isLoadingGroups,
  };
};
