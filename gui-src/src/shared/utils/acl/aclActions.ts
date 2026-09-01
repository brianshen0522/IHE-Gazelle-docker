import { Session } from "next-auth";
import { toast } from "react-toastify";
import { AccessControlList, PersistAclFunction } from "@/shared/types/AccessControlListTypes";
import { updateConnectionAndAllReferencedMessageAcl } from "@message-capture/services/updateAclOfMessageOfAConnection";
import { generateReadAccessKey } from "@message-capture/services/generateReadAccessKey";
import { deleteAccessKey } from "@message-capture/services/deleteAccessKey";
import { updateTestRunExecutionAcl } from "@/app/test-execution/components/test-run/actions";

// Update ACL for an item and all referenced messages.
export const persistAcl = async ({
  itemId,
  accessControlList,
  session,
}: {
  itemId: string;
  accessControlList: AccessControlList;
  session: Session | null;
}): Promise<AccessControlList> => {
  try {
    await updateConnectionAndAllReferencedMessageAcl({
      itemId,
      accessControlList,
      session,
    });
    return accessControlList;
  } catch (err: unknown) {
    console.error("Failed to persist ACL:", err);
    throw err;
  }
};

// Update ACL for test-run-execution
export const persistTestRunExecutionAcl = async ({
  itemId,
  accessControlList,
}: {
  itemId: string;
  accessControlList: AccessControlList;
  session: Session | null;
}): Promise<AccessControlList> => {
  try {
    const result = await updateTestRunExecutionAcl({
      executionId: itemId,
      accessControlList,
    });

    if (!result.success) {
      throw new Error(result.error);
    }

    return result.data?.accessControlList || accessControlList;
  } catch (err: unknown) {
    console.error("Failed to persist test run execution ACL:", err);
    throw err;
  }
};

export const changeUserToOwner = async ({
  userId,
  currentAcl,
  itemId,
  session,
  customPersist,
}: {
  userId: string;
  currentAcl: AccessControlList;
  itemId: string;
  session: Session | null;
  customPersist?: PersistAclFunction;
}): Promise<AccessControlList> => {
  const { readers = [], editors = [], owners = [] } = currentAcl;

  const updatedAcl: AccessControlList = {
    ...currentAcl,
    owners: Array.from(new Set([...owners, userId])),
    editors: editors.filter((id) => id !== userId),
    readers: readers.filter((id) => id !== userId),
  };

  const persist = customPersist || persistAcl;
  return persist({ itemId, accessControlList: updatedAcl, session });
};

export const changeUserToEditor = async ({
  userId,
  currentAcl,
  itemId,
  session,
  customPersist,
}: {
  userId: string;
  currentAcl: AccessControlList;
  itemId: string;
  session: Session | null;
  customPersist?: PersistAclFunction;
}): Promise<AccessControlList> => {
  const { readers = [], editors = [], owners = [] } = currentAcl;

  const updatedAcl: AccessControlList = {
    ...currentAcl,
    owners: owners.filter((id) => id !== userId),
    editors: Array.from(new Set([...editors, userId])),
    readers: readers.filter((id) => id !== userId),
  };

  const persist = customPersist || persistAcl;
  return persist({ itemId, accessControlList: updatedAcl, session });
};

export const changeUserToViewer = async ({
  userId,
  currentAcl,
  itemId,
  session,
  customPersist,
}: {
  userId: string;
  currentAcl: AccessControlList;
  itemId: string;
  session: Session | null;
  customPersist?: PersistAclFunction;
}): Promise<AccessControlList> => {
  const { readers = [], editors = [], owners = [] } = currentAcl;

  const updatedAcl: AccessControlList = {
    ...currentAcl,
    owners: owners.filter((id) => id !== userId),
    editors: editors.filter((id) => id !== userId),
    readers: Array.from(new Set([...readers, userId])),
  };

  const persist = customPersist || persistAcl;
  return persist({ itemId, accessControlList: updatedAcl, session });
};

export const removeMemberFromAcl = async ({
  userId,
  currentAcl,
  itemId,
  session,
  customPersist,
}: {
  userId: string;
  currentAcl: AccessControlList;
  itemId: string;
  session: Session | null;
  customPersist?: PersistAclFunction;
}): Promise<AccessControlList> => {
  const { readers = [], editors = [], owners = [] } = currentAcl;

  const updatedAcl: AccessControlList = {
    ...currentAcl,
    owners: owners.filter((id) => id !== userId),
    editors: editors.filter((id) => id !== userId),
    readers: readers.filter((id) => id !== userId),
  };

  const persist = customPersist || persistAcl;
  return persist({ itemId, accessControlList: updatedAcl, session });
};

// Generate a shareable read access key.
export const generateShareableAccessKey = async ({
  itemId,
  session,
  customPersist,
}: {
  itemId: string;
  currentAcl: AccessControlList;
  session: Session | null;
  customPersist?: PersistAclFunction;
}): Promise<{ accessKey: string; acl: AccessControlList }> => {
  try {
    const result = await generateReadAccessKey({ id: itemId, session });

    if (!result.accessControlList) {
      throw new Error("Failed to generate access key");
    }

    const updatedAcl: AccessControlList = {
      ...result.accessControlList,
      isPublic: false,
    };

    const persist = customPersist || persistAcl;
    await persist({
      itemId,
      accessControlList: updatedAcl,
      session,
    });

    return {
      accessKey: updatedAcl.readAccessKey || "",
      acl: updatedAcl,
    };
  } catch (err: unknown) {
    console.error("Failed to generate shareable access key:", err);
    throw err;
  }
};

// Delete the read access key and update privacy policy.
export const deleteShareableAccessKey = async ({
  itemId,
  currentAcl,
  policy,
  session,
  customPersist,
}: {
  itemId: string;
  currentAcl: AccessControlList;
  policy: string;
  session: Session | null;
  customPersist?: PersistAclFunction;
}): Promise<AccessControlList> => {
  // Only call the backend to delete the access key if one exists
  if (currentAcl.readAccessKey) {
    const deleteResult = await deleteAccessKey({ id: itemId, session });

    if (deleteResult.error) {
      throw new Error(deleteResult.error);
    }
  }

  const USER = "user";
  const updatedAcl: AccessControlList = {
    ...currentAcl,
    isPublic: policy === "public",
    readAccessKey: undefined,
    readers: policy === "users" ? [...(currentAcl.readers || []), USER] : (currentAcl.readers || []).filter((id) => id !== USER),
  };

  const persist = customPersist || persistAcl;
  await persist({
    itemId,
    accessControlList: updatedAcl,
    session,
  });

  return updatedAcl;
};

// Update privacy policy for an item.
export const updatePrivacyPolicy = async ({
  policy,
  itemId,
  currentAcl,
  session,
  t,
  customPersist,
}: {
  policy: string;
  itemId: string;
  currentAcl: AccessControlList;
  session: Session | null;
  t: (key: string) => string;
  customPersist?: PersistAclFunction;
}): Promise<AccessControlList> => {
  try {
    let newAcl: AccessControlList;
    const USER = "user";

    if (policy === "link") {
      const { accessKey, acl } = await generateShareableAccessKey({
        itemId,
        currentAcl,
        session,
        customPersist,
      });

      newAcl = {
        ...acl,
        isPublic: false,
        readAccessKey: accessKey,
        readers: (currentAcl.readers || []).filter((id) => id !== USER),
      };
    } else {
      newAcl = await deleteShareableAccessKey({
        itemId,
        currentAcl,
        policy,
        session,
        customPersist,
      });

      if (policy === "users") {
        newAcl = {
          ...newAcl,
          isPublic: false,
          readAccessKey: undefined,
          readers: Array.isArray(currentAcl.readers) ? [...currentAcl.readers, USER] : [USER],
        };
      }
    }

    toast.success(t("gzl.message.capture.privacy_settings_updated") + "!");
    return newAcl;
  } catch (err: unknown) {
    console.error("Failed to update privacy:", err);
    toast.error(t("gzl.message.capture.could_not_update_privacy_settings_please_try_again") + ".");
    throw err;
  }
};
