import { deleteUserPicture, editUserAttribute, editUserPreferences, uploadUserPicture } from "./actions";
import { mutate, type KeyedMutator } from "swr";
import { Session } from "next-auth";
import { toast } from "react-toastify";
import { User } from "@/app/user-management/components/user-management/Types";

interface UserInput {
  label: string;
  type: string;
  htmlFor: string;
  name: string;
  placeholder: string;
  value: string;
  readonly: boolean;
  isValidInput: boolean;
  validationMessage?: string;
}

interface EditUserActionsParams {
  user: User | null;
  userFirstName: string;
  userLastName: string;
  userEmail: string;
  userActivation: boolean;
  userGroups: string[];
  userOrganizationId: string;
  userPrefTableLabel: string;
  userPrefNotificationByEmail: boolean;
  userPrefLanguagesSpoken: string[];
  userPictureUrl: string | null;
  newUserPicture: ArrayBuffer | null;
  session: Session;
  inputs: UserInput[];
  setHasUnsavedChanges: (value: boolean) => void;
  setInitialInputs: (inputs: UserInput[]) => void;
  mutateUser: KeyedMutator<unknown>;
  keyUser: [string, string] | null;
  isSidePanelContext?: boolean;
  t: (key: string) => string;
}

interface MutateUserDataParams {
  mutateUser: KeyedMutator<unknown>;
  keyUser: [string, string] | null;
  isSidePanelContext?: boolean;
}

export const mutateUserData = (params: MutateUserDataParams) => {
  const { mutateUser, keyUser } = params;

  // Mutate the specific user data
  mutateUser(keyUser);

  // Invalidate all users list caches (supports both old and new API structures)
  mutate((key) => typeof key === "string" && key.includes("/gazelle/api/items") && key.includes("type=users_management"), undefined, {
    revalidate: true,
  });

  // Invalidate user preferences cache
  mutate((key) => Array.isArray(key) && typeof key[0] === "string" && key[0].includes("/preferences"), undefined, {
    revalidate: true,
  });
};

export const updateUserAttributes = (params: EditUserActionsParams) => {
  const { user, userFirstName, userLastName, userEmail, userActivation, userGroups, userOrganizationId } = params;

  return editUserAttribute(
    {
      id: user?.id ?? "",
      firstName: userFirstName,
      lastName: userLastName,
      email: userEmail,
      activated: userActivation,
      delegated: user?.delegated!,
      groupIds: userGroups,
      organizationId: userOrganizationId,
    },
    user?.id ?? "",
  );
};

export const updateUserPreferences = (params: EditUserActionsParams) => {
  const { userPrefTableLabel, userPrefNotificationByEmail, userPrefLanguagesSpoken, user } = params;

  return editUserPreferences(
    {
      tableLabel: userPrefTableLabel,
      notifiedByEmail: userPrefNotificationByEmail,
      languagesSpoken: userPrefLanguagesSpoken,
      profileThumbnailUri: "",
      profilePictureUri: "",
    },
    user?.id ?? "",
  );
};

export const handleUserPicture = async (params: EditUserActionsParams) => {
  const { userPictureUrl, newUserPicture, user } = params;

  if (userPictureUrl === null) {
    return deleteUserPicture(user?.id ?? "");
  } else if (newUserPicture !== null) {
    return uploadUserPicture(newUserPicture, user?.id ?? "");
  }
};

export const handleFormSubmit = async (event: React.SyntheticEvent, params: EditUserActionsParams) => {
  event.preventDefault();
  const { session, t, setHasUnsavedChanges, setInitialInputs, inputs, mutateUser, keyUser, isSidePanelContext } = params;

  if (!session) {
    toast.error(t("gzl.gum.session_expired"));
    return;
  }

  try {
    await updateUserPreferences(params);
    await handleUserPicture(params);
    await updateUserAttributes(params);

    toast.success(t("gzl.gum.user_updated_successfully"));

    // Call the mutateUserData function with the required parameters
    mutateUserData({
      mutateUser,
      keyUser,
      isSidePanelContext,
    });

    setHasUnsavedChanges(false);
    setInitialInputs(inputs);
  } catch (error: unknown) {
    console.error(error);
    toast.error((error as Error).message);
  }
};

export const validateInputs = (inputs: UserInput[]): boolean => {
  return inputs?.some((input) => !input.value || !input.isValidInput) ?? false;
};
