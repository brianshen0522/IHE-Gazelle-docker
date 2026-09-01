"use client";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";
import { Earth, Lock, Share2, Users } from "lucide-react";
import { Button, formatGroups, formatUsers, ModalACL, Group } from "@gazelle/gazelle-component-ui";
import { canReadOrUpdateResourceACL, isAuthenticated } from "@/shared/utils/acl/accessControlListPermissions";
import { generateReadAccessKey } from "@message-capture/services/generateReadAccessKey";
import { updateConnectionAndAllReferencedMessageAcl } from "@message-capture/services/updateAclOfMessageOfAConnection";
import { toast } from "react-toastify";
import { deleteAccessKey } from "@message-capture/services/deleteAccessKey";
import { useSearchParams } from "next/navigation";
import { formatUserOrGroupById } from "@message-capture/services/formatUserOrGroupById";
import { useGetUserSummary } from "@/shared/hooks/SWR/useGetUserSummary";
import { User } from "@/app/user-management/components/user-management/Types";
import { useSearchGroupsWithResolvedOrganizationName } from "@message-capture/hooks/useGetGroupsWithOrganizationNames";
import { AccessControlList } from "@shared/types/AccessControlListTypes";

interface AclDisplayProps {
  acl: AccessControlList;
  itemId: string;
  className?: string;
}

export const AclDisplay: React.FC<AclDisplayProps> = ({ acl, itemId, className }) => {
  const { data: session } = useSession();
  const { i18n, t } = useTranslation();
  const searchParams = useSearchParams();
  const readAccessKeySearchParam = searchParams.get("readAccessKey") as string;
  const [offset, setOffset] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  // Local ACL state
  const [currentAcl, setCurrentAcl] = useState<AccessControlList>(acl);
  const [openModal, setOpenModal] = useState(false);
  const [accessKey, setAccessKey] = useState(acl.readAccessKey);

  const [usersFormatted, setUsersFormatted] = useState<{
    owners: { id: string; name: string; organization: string }[];
    editors: { id: string; name: string; organization: string }[];
    viewers: { id: string; name: string; organization: string }[];
  }>({
    owners: [],
    editors: [],
    viewers: [],
  });

  const formatWithTranslation = useCallback(
    (value: { id: string; name: string }) => {
      if (value.name.includes("gzl.gum.organization_admin")) {
        const translation = t("gzl.gum.organization_admin");
        value.name = value.name.replace("gzl.gum.organization_admin", translation);
      }
      return {
        ...value,
        name: t(value.name),
      };
    },
    [t],
  );

  const formatWithTranslationWithOrga = (value: { id: string; name: string; organization: string }) => {
    if (value.name.includes("gzl.gum.organization_admin")) {
      const translation = t("gzl.gum.organization_admin");
      value.name = value.name.replace("gzl.gum.organization_admin", translation);
    }
    return {
      ...value,
      name: formatWithTranslation(value).name,
    };
  };

  const formatUsersForDisplay = async () => {
    const formattedOwners = currentAcl.owners ? await Promise.all(currentAcl.owners.map((id) => formatUserOrGroupById(id))) : [];
    const formattedEditors = currentAcl.editors ? await Promise.all(currentAcl.editors.map((id) => formatUserOrGroupById(id))) : [];
    const formattedViewers = currentAcl.readers ? await Promise.all(currentAcl.readers.map((id) => formatUserOrGroupById(id))) : [];

    setUsersFormatted((prevState) => ({
      ...prevState,
      owners: formattedOwners.map(formatWithTranslationWithOrga),
      editors: formattedEditors.map(formatWithTranslationWithOrga),
      viewers: formattedViewers.map(formatWithTranslationWithOrga),
    }));
  };
  // Destructure lists
  const { isPublic, readAccessKey = accessKey, readers = [], editors = [], owners = [] } = currentAcl;

  const canUpdateAcl = canReadOrUpdateResourceACL(session, currentAcl);
  const buttonLabel = canUpdateAcl ? t("gzl.acl.manageAccess") : t("gzl.acl.viewAccess");

  const persistAcl = async (updated: AccessControlList) => {
    try {
      await updateConnectionAndAllReferencedMessageAcl({
        itemId: itemId,
        accessControlList: updated,
        session,
      });
      setCurrentAcl(updated);
    } catch (err: any) {
      console.error(err);
      setOpenModal(false);
    }
  };

  const onChangeToOwner = (userId: string) =>
    persistAcl({
      ...currentAcl,
      owners: Array.from(new Set([...owners, userId])),
      editors: editors.filter((id) => id !== userId),
      readers: readers.filter((id) => id !== userId),
    });

  const onChangeToEditor = (userId: string) =>
    persistAcl({
      ...currentAcl,
      owners: owners.filter((id) => id !== userId),
      editors: Array.from(new Set([...editors, userId])),
      readers: readers.filter((id) => id !== userId),
    });

  const onChangeToViewer = (userId: string) =>
    persistAcl({
      ...currentAcl,
      owners: owners.filter((id) => id !== userId),
      editors: editors.filter((id) => id !== userId),
      readers: Array.from(new Set([...readers, userId])),
    });

  const onRemoveMember = (userId: string) =>
    persistAcl({
      ...currentAcl,
      owners: owners.filter((id) => id !== userId),
      editors: editors.filter((id) => id !== userId),
      readers: readers.filter((id) => id !== userId),
    });

  const handleGiveAccess = (userId: string, role: string) => {
    if (role === "owner") onChangeToOwner(userId);
    if (role === "editor") onChangeToEditor(userId);
    if (role === "viewer") onChangeToViewer(userId);
  };
  useEffect(() => {
    formatUsersForDisplay();
  }, [currentAcl]);

  let policyLabelText: string, policyDescription: string;
  const USER = "user";

  const handleShare = async (): Promise<string | undefined> => {
    try {
      const result = await generateReadAccessKey({ id: itemId, session });
      result.accessControlList!.isPublic = false;

      try {
        await updateConnectionAndAllReferencedMessageAcl({
          itemId: itemId,
          accessControlList: result.accessControlList as AccessControlList,
          session,
        });
      } catch (err: any) {
        console.error(err);
        setOpenModal(false);
        return;
      }

      return result.accessControlList?.readAccessKey;
    } catch (err: any) {
      console.error(err);
      setOpenModal(false);
    }
  };

  const handleStopShare = async (policy: string): Promise<AccessControlList> => {
    await deleteAccessKey({ id: itemId, session });
    const updated: AccessControlList = {
      ...currentAcl,
      isPublic: policy === "public",
      readAccessKey: undefined,
      readers: policy === "users" ? [...(currentAcl.readers || []), USER] : (currentAcl.readers || []).filter((id) => id !== USER),
    };
    await updateConnectionAndAllReferencedMessageAcl({
      itemId: itemId,
      accessControlList: updated,
      session,
    });
    return updated;
  };

  const currentURL = useMemo(() => {
    if (typeof globalThis === "undefined" || !readAccessKey) {
      return globalThis?.location.href;
    }
    const url = globalThis.location.href;
    const separator = url.includes("?") ? "&" : "?";
    return url + separator + "readAccessKey=" + readAccessKey;
  }, [readAccessKey]);

  let Icon: React.ComponentType<{ className?: string }>;

  if (isPublic) {
    policyLabelText = t("gzl.acl.public.label");
    policyDescription = t("gzl.acl.public.description");
    Icon = Earth;
  } else if (readAccessKey || readAccessKeySearchParam) {
    policyLabelText = t("gzl.acl.link.label");
    policyDescription = t("gzl.acl.link.description");
    Icon = Share2;
  } else if (readers.includes(USER)) {
    policyLabelText = t("gzl.acl.gazelle.label");
    policyDescription = t("gzl.acl.gazelle.description");
    Icon = Users;
  } else {
    policyLabelText = t("gzl.acl.private.label");
    policyDescription = t("gzl.acl.private.description");
    Icon = Lock;
  }

  const onPrivacyPolicyChange = async (policy: string) => {
    try {
      let newAcl: AccessControlList;

      if (policy === "link") {
        const key = await handleShare();

        newAcl = {
          ...currentAcl,
          isPublic: false,
          readAccessKey: key ?? undefined,
          readers: (currentAcl.readers || []).filter((id) => id !== USER),
        };
      } else {
        newAcl = await handleStopShare(policy);

        if (policy === "users") {
          newAcl = {
            ...newAcl,
            isPublic: false,
            readAccessKey: undefined,
            readers: Array.isArray(currentAcl.readers) ? [...currentAcl.readers, USER] : [USER],
          };
        }
      }

      setCurrentAcl(newAcl);
      setAccessKey(newAcl.readAccessKey);
      toast.success(t("gzl.message.capture.privacy_settings_updated") + "!");
    } catch (err: any) {
      console.error("Failed to update privacy:", err);
      setOpenModal(false);
      toast.error(t("gzl.message.capture.could_not_update_privacy_settings_please_try_again") + ".");
    }
  };

  const handleSetOffset = useCallback((value: number | ((prevOffset: number) => number)) => {
    setOffset(value);
  }, []);
  const handleSetSearchTerm = useCallback((newTerm: string) => {
    setSearchTerm(newTerm);
  }, []);

  const { data: usersData } = useGetUserSummary({ offset, limit: 100, search: searchTerm, enabled: openModal });
  const formattedAvailableUsers = useMemo(() => formatUsers(usersData?.users as User[]), [usersData?.users]);

  // Filter available users to exclude those who already have roles
  const availableUsers = useMemo(
    () =>
      formattedAvailableUsers.filter((user: { id: string; name: string }) => {
        if (!user?.id) return false;
        const existingUserIds = [
          ...(usersFormatted.owners?.map((u) => u.id) || []),
          ...(usersFormatted.editors?.map((u) => u.id) || []),
          ...(usersFormatted.viewers?.map((u) => u.id) || []),
        ];
        return !existingUserIds.includes(user.id);
      }),
    [formattedAvailableUsers, usersFormatted.owners, usersFormatted.editors, usersFormatted.viewers],
  );

  const { data: groupsData } = useSearchGroupsWithResolvedOrganizationName({ offset, limit: 100, search: searchTerm, session, enabled: openModal });

  const formattedAvailableGroups = useMemo(() => formatGroups(groupsData?.resolvedData as Group[]), [groupsData?.resolvedData]);
  // Filter available groups to exclude those who already have roles
  const availableGroups = useMemo(
    () =>
      formattedAvailableGroups
        .filter((group: { id: string; name: string }) => {
          if (!group?.id) return false;
          const existingUserIds = [
            ...(usersFormatted.owners?.map((u) => u.id) || []),
            ...(usersFormatted.editors?.map((u) => u.id) || []),
            ...(usersFormatted.viewers?.map((u) => u.id) || []),
          ];
          return !existingUserIds.includes(group.id);
        })
        .map(formatWithTranslation),
    [formattedAvailableGroups, formatWithTranslation, usersFormatted.owners, usersFormatted.editors, usersFormatted.viewers],
  );

  return (
    <div className={`flex items-start flex-start p-2 rounded-md flex-col w-full ${className}`}>
      <div className="flex flex-col md:flex-row items-center justify-between gap-10">
        <div className="flex items-center gap-2">
          <p className="font-semibold whitespace-nowrap">{t("gzl.acl.policyLabel")}</p>
          <Icon className="w-10 h-10 text-muted-foreground text-purple" />
          <div className="flex flex-col leading-tight w-full">
            <span className="font-semibold">{policyLabelText}</span>
            <span className="text-xs text-muted-foreground">{policyDescription}</span>
          </div>
        </div>

        {isAuthenticated(session) && (
          <Button id="acl-button" type="button" title={buttonLabel} onClick={() => setOpenModal(true)} variant="secondary">
            {buttonLabel}
          </Button>
        )}
      </div>

      <ModalACL
        isOpenModal={openModal}
        toggleModal={() => setOpenModal(false)}
        // Privacy
        privacyOptions={[
          { label: t("gzl.component.library.link_policy"), value: "link" },
          { label: t("gzl.component.library.public_policy"), value: "public" },
          { label: t("gzl.component.library.private_policy"), value: "private" },
          { label: t("gzl.component.library.users_policy"), value: "users" },
        ]}
        isOwner={canUpdateAcl}
        onPrivacyPolicyChange={onPrivacyPolicyChange}
        isPrivacyPolicyLoading={false}
        // Copy link
        currentURL={currentURL}
        // Per-user actions
        handleGiveAccess={handleGiveAccess}
        onChangeToOwner={onChangeToOwner}
        onChangeToEditor={onChangeToEditor}
        onChangeToViewer={onChangeToViewer}
        onRemoveMember={onRemoveMember}
        accessControlList={{
          ...usersFormatted,
          isPublic: isPublic,
          readAccessKey: readAccessKey || readAccessKeySearchParam,
        }}
        availableUsers={availableUsers}
        availableGroups={availableGroups}
        offset={offset}
        setOffset={handleSetOffset}
        setSearchTerm={handleSetSearchTerm}
        limit={100}
        i18nInstance={i18n}
      />
    </div>
  );
};
