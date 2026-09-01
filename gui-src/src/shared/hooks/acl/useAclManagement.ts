import { useCallback, useEffect, useState, useRef, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { AccessControlList, FormattedMembers, UseAclManagementParams, UseAclManagementResult } from "@/shared/types/AccessControlListTypes";
import { formatUserOrGroupById } from "@message-capture/services/formatUserOrGroupById";
import { formatWithTranslationWithOrga } from "@shared/utils/acl/aclFormatters";
import { changeUserToOwner, changeUserToEditor, changeUserToViewer, removeMemberFromAcl } from "@/shared/utils/acl/aclActions";

// Helper: Compare two string arrays for equality
const arraysEqual = (a: string[] = [], b: string[] = []): boolean => {
  return a.length === b.length && a.every((val, idx) => val === b[idx]);
};

// Custom hook to manage ACL state and role changes.
// Handles formatting of members and provides actions for role management.
export const useAclManagement = ({ initialAcl, itemId, session, customPersist }: UseAclManagementParams): UseAclManagementResult => {
  const { t } = useTranslation();
  const [currentAcl, setCurrentAcl] = useState<AccessControlList>(initialAcl);
  const [isUpdating, setIsUpdating] = useState(false);
  const [formattedMembers, setFormattedMembers] = useState<FormattedMembers>({
    owners: [],
    editors: [],
    viewers: [],
  });

  // Sync internal state when external ACL changes (e.g., from parent component)
  useEffect(() => {
    setCurrentAcl(initialAcl);
  }, [initialAcl]);

  // Create stable references to member arrays using useMemo
  const owners = useMemo(() => currentAcl.owners || [], [currentAcl.owners]);
  const editors = useMemo(() => currentAcl.editors || [], [currentAcl.editors]);
  const readers = useMemo(() => currentAcl.readers || [], [currentAcl.readers]);

  // Track previous member lists to detect actual changes - start with empty arrays
  const prevMembersRef = useRef<{ owners: string[]; editors: string[]; readers: string[] }>({
    owners: [],
    editors: [],
    readers: [],
  });

  // Format members only when the actual member lists change
  useEffect(() => {
    const prev = prevMembersRef.current;
    const hasChanged = !arraysEqual(prev.owners, owners) || !arraysEqual(prev.editors, editors) || !arraysEqual(prev.readers, readers);

    if (hasChanged) {
      prevMembersRef.current = { owners, editors, readers };

      // Format members asynchronously
      const formatMembers = async () => {
        const [formattedOwners, formattedEditors, formattedViewers] = await Promise.all([
          Promise.all(owners.map((id) => formatUserOrGroupById(id))),
          Promise.all(editors.map((id) => formatUserOrGroupById(id))),
          Promise.all(readers.map((id) => formatUserOrGroupById(id))),
        ]);

        setFormattedMembers({
          owners: formattedOwners.map((member) => formatWithTranslationWithOrga(member, t)),
          editors: formattedEditors.map((member) => formatWithTranslationWithOrga(member, t)),
          viewers: formattedViewers.map((member) => formatWithTranslationWithOrga(member, t)),
        });
      };

      formatMembers();
    }
  }, [owners, editors, readers, session, t]);

  const onChangeToOwner = useCallback(
    async (userId: string) => {
      setIsUpdating(true);
      try {
        const updatedAcl = await changeUserToOwner({
          userId,
          currentAcl,
          itemId,
          session,
          customPersist,
        });
        setCurrentAcl(updatedAcl);
      } catch (error) {
        console.error("Failed to change user to owner:", error);
      } finally {
        setIsUpdating(false);
      }
    },
    [currentAcl, itemId, session, customPersist],
  );

  const onChangeToEditor = useCallback(
    async (userId: string) => {
      // Prevent current user from demoting themselves from owner to editor
      const currentUserId = session?.user?.gazelleId;
      const isCurrentUserOwner = currentAcl.owners?.includes(currentUserId ?? "");
      const isTargetOwner = currentAcl.owners?.includes(userId);

      if (userId === currentUserId && isCurrentUserOwner) {
        toast.error(t("gzl.user.interface.cannot_change_own_owner_role"));
        return;
      }

      // Prevent removing the last owner
      if (isTargetOwner && (currentAcl.owners?.length || 0) <= 1) {
        toast.error(t("gzl.user.interface.cannot_remove_last_owner"));
        return;
      }

      setIsUpdating(true);
      try {
        const updatedAcl = await changeUserToEditor({
          userId,
          currentAcl,
          itemId,
          session,
          customPersist,
        });
        setCurrentAcl(updatedAcl);
      } catch (error) {
        console.error("Failed to change user to editor:", error);
      } finally {
        setIsUpdating(false);
      }
    },
    [currentAcl, itemId, session, customPersist, t],
  );

  const onChangeToViewer = useCallback(
    async (userId: string) => {
      // Prevent current user from demoting themselves from owner to viewer
      const currentUserId = session?.user?.gazelleId;
      const isCurrentUserOwner = currentAcl.owners?.includes(currentUserId ?? "");
      const isTargetOwner = currentAcl.owners?.includes(userId);

      if (userId === currentUserId && isCurrentUserOwner) {
        toast.error(t("gzl.user.interface.cannot_change_own_owner_role"));
        return;
      }

      // Prevent removing the last owner
      if (isTargetOwner && (currentAcl.owners?.length || 0) <= 1) {
        toast.error(t("gzl.user.interface.cannot_remove_last_owner"));
        return;
      }

      setIsUpdating(true);
      try {
        const updatedAcl = await changeUserToViewer({
          userId,
          currentAcl,
          itemId,
          session,
          customPersist,
        });
        setCurrentAcl(updatedAcl);
      } catch (error) {
        console.error("Failed to change user to viewer:", error);
      } finally {
        setIsUpdating(false);
      }
    },
    [currentAcl, itemId, session, customPersist, t],
  );

  const onRemoveMember = useCallback(
    async (userId: string) => {
      const currentUserId = session?.user?.gazelleId;
      const isTargetOwner = currentAcl.owners?.includes(userId);

      // Prevent removing owners (including yourself) via this function
      // Owners should use role change functions instead (to editor/viewer)
      // or can only be completely removed if not the last owner
      if (isTargetOwner) {
        // Check if removing the last owner
        if ((currentAcl.owners?.length || 0) <= 1) {
          toast.error(t("gzl.user.interface.cannot_remove_last_owner"));
          return;
        }

        // If trying to remove yourself as owner
        if (userId === currentUserId) {
          toast.error(t("gzl.user.interface.cannot_remove_yourself_as_owner"));
          return;
        }
      }

      setIsUpdating(true);
      try {
        const updatedAcl = await removeMemberFromAcl({
          userId,
          currentAcl,
          itemId,
          session,
          customPersist,
        });
        setCurrentAcl(updatedAcl);
      } catch (error) {
        console.error("Failed to remove member:", error);
      } finally {
        setIsUpdating(false);
      }
    },
    [currentAcl, itemId, session, customPersist, t],
  );

  const handleGiveAccess = useCallback(
    async (userId: string, role: string) => {
      if (role === "owner") await onChangeToOwner(userId);
      if (role === "editor") await onChangeToEditor(userId);
      if (role === "viewer") await onChangeToViewer(userId);
    },
    [onChangeToOwner, onChangeToEditor, onChangeToViewer],
  );

  const updateAcl = useCallback((newAcl: AccessControlList) => {
    setCurrentAcl(newAcl);
  }, []);

  return {
    acl: currentAcl,
    formattedMembers,
    isUpdating,
    onChangeToOwner,
    onChangeToEditor,
    onChangeToViewer,
    onRemoveMember,
    handleGiveAccess,
    updateAcl,
  };
};
