"use client";
import React, { useState } from "react";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";
import { Button, ModalACL } from "@gazelle/gazelle-component-ui";
import { canReadOrUpdateResourceACL, isAuthenticated } from "@/shared/utils/acl/accessControlListPermissions";
import { useSearchParams } from "next/navigation";
import { AclDisplayProps } from "@/shared/types/AccessControlListTypes";
import { useAclManagement } from "@shared/hooks/acl/useAclManagement";
import { useAclUsers } from "@shared/hooks/acl/useAclUsers";
import { useAclPrivacy } from "@shared/hooks/acl/useAclPrivacy";
import { AclPolicyDisplay } from "./AclPolicyDisplay";

export const AclDisplay = ({ acl, itemId, className, customPersist, disableLinkPrivacy = false }: AclDisplayProps) => {
  const { data: session } = useSession();
  const { i18n, t } = useTranslation();
  const searchParams = useSearchParams();
  const readAccessKeySearchParam = searchParams.get("readAccessKey");
  const [openModal, setOpenModal] = useState(false);

  // Use custom hooks for ACL management
  const {
    acl: currentAcl,
    formattedMembers,
    onChangeToOwner,
    onChangeToEditor,
    onChangeToViewer,
    onRemoveMember,
    handleGiveAccess,
    updateAcl,
  } = useAclManagement({
    initialAcl: acl,
    itemId,
    session,
    customPersist,
  });

  // Use custom hook for privacy management
  const { policy, currentURL, onPrivacyPolicyChange } = useAclPrivacy({
    acl: currentAcl,
    itemId,
    session,
    onAclUpdate: updateAcl,
    customPersist,
  });

  // Use custom hook for users and groups management
  const { availableUsers, availableGroups, offset, setOffset, setSearchTerm } = useAclUsers({
    owners: formattedMembers.owners,
    editors: formattedMembers.editors,
    viewers: formattedMembers.viewers,
    session,
    enabled: openModal,
  });

  const canUpdateAcl = canReadOrUpdateResourceACL(session, currentAcl);
  const buttonLabel = canUpdateAcl ? t("gzl.acl.manageAccess") : t("gzl.acl.viewAccess");

  // Build privacy options based on what's supported
  const privacyOptions = [
    ...(disableLinkPrivacy ? [] : [{ label: t("gzl.component.library.link_policy"), value: "link" }]),
    { label: t("gzl.component.library.public_policy"), value: "public" },
    { label: t("gzl.component.library.private_policy"), value: "private" },
    { label: t("gzl.component.library.users_policy"), value: "users" },
  ];

  return (
    <div className={`flex items-start flex-start rounded-md flex-col w-full ${className}`}>
      <div className="flex flex-col md:flex-row items-center justify-between gap-10">
        <div className="flex items-center gap-2">
          <p className="font-semibold whitespace-nowrap">{t("gzl.acl.policyLabel")}</p>
          <AclPolicyDisplay icon={policy.icon} label={policy.label} description={policy.description} />
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
        privacyOptions={privacyOptions}
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
          ...formattedMembers,
          isPublic: currentAcl.isPublic,
          readAccessKey: currentAcl.readAccessKey || readAccessKeySearchParam,
        }}
        availableUsers={availableUsers}
        availableGroups={availableGroups}
        offset={offset}
        setOffset={setOffset}
        setSearchTerm={setSearchTerm}
        limit={100}
        i18nInstance={i18n}
      />
    </div>
  );
};
