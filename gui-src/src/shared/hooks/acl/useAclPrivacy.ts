import { useCallback, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "next/navigation";
import { Earth, Lock, Share2, Users } from "lucide-react";
import { updatePrivacyPolicy } from "@/shared/utils/acl/aclActions";
import { PolicyMetadata, UseAclPrivacyParams, UseAclPrivacyResult } from "../../types/AccessControlListTypes";

const USER = "user";

// Custom hook to manage ACL privacy policies and access keys.
// Determines current policy, generates shareable URLs, and handles policy changes.
export const useAclPrivacy = ({ acl, itemId, session, onAclUpdate, customPersist }: UseAclPrivacyParams): UseAclPrivacyResult => {
  const { t } = useTranslation();
  const searchParams = useSearchParams();
  const readAccessKeySearchParam = searchParams.get("readAccessKey");
  const [isUpdating, setIsUpdating] = useState(false);

  const { isPublic, readAccessKey, readers = [] } = acl;

  // Determine the current privacy policy based on ACL state.
  const policy = useMemo((): PolicyMetadata => {
    if (isPublic) {
      return {
        type: "public",
        label: t("gzl.acl.public.label"),
        description: t("gzl.acl.public.description"),
        icon: Earth,
      };
    }

    if (readAccessKey || readAccessKeySearchParam) {
      return {
        type: "link",
        label: t("gzl.acl.link.label"),
        description: t("gzl.acl.link.description"),
        icon: Share2,
      };
    }

    if (readers.includes(USER)) {
      return {
        type: "users",
        label: t("gzl.acl.gazelle.label"),
        description: t("gzl.acl.gazelle.description"),
        icon: Users,
      };
    }

    return {
      type: "private",
      label: t("gzl.acl.private.label"),
      description: t("gzl.acl.private.description"),
      icon: Lock,
    };
  }, [isPublic, readAccessKey, readAccessKeySearchParam, readers, t]);

  // Generate shareable URL with access key.
  const currentURL = useMemo(() => {
    if (typeof globalThis === "undefined" || !readAccessKey) {
      return globalThis?.location.href || "";
    }

    const url = globalThis.location.href;
    const separator = url.includes("?") ? "&" : "?";
    return url + separator + "readAccessKey=" + readAccessKey;
  }, [readAccessKey]);

  const onPrivacyPolicyChange = useCallback(
    async (newPolicy: string) => {
      setIsUpdating(true);
      try {
        const updatedAcl = await updatePrivacyPolicy({
          policy: newPolicy,
          itemId,
          currentAcl: acl,
          session,
          t,
          customPersist,
        });

        onAclUpdate(updatedAcl);
      } catch (error) {
        console.error("Failed to update privacy policy:", error);
        // Error toast already handled in updatePrivacyPolicy
      } finally {
        setIsUpdating(false);
      }
    },
    [acl, itemId, session, t, onAclUpdate, customPersist],
  );

  return {
    policy,
    currentURL,
    isUpdating,
    onPrivacyPolicyChange,
  };
};
