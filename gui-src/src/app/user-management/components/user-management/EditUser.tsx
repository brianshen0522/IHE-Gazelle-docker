"use client";
import React, { useEffect, useId, useState } from "react";
import { Badge, Button } from "@gazelle/gazelle-component-ui";
import { ArrowLeft, Save } from "lucide-react";
import { useRouter } from "next/navigation";
import { Session } from "next-auth";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";
import EditUserSecurity from "./user-security/EditUserSecurity";
import ProfilePicture from "./ProfilePicture";
import { useEditUserContext } from "@user-management/context/EditUserContext";
import { handleFormSubmit, validateInputs } from "./editUserActions";
import EditUserAttributes from "@user-management/components/user-management/EditUserAttributes";
import RoleAttribution from "@user-management/components/user-management/RoleAttribution";
import EditUserPreferences from "@user-management/components/user-management/EditUserPreferences";
import { useGetUserById } from "@/app/user-management/hooks/swr/useGetUser";
import { EditUserProps } from "@/app/user-management/components/user-management/Types";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";
import { Route } from "next";

const EditUser = (editUserProps: EditUserProps) => {
  const componentId = useId();
  const { t } = useTranslation();
  const router = useRouter();
  const { data: session } = useSession() as { data: Session };
  const userId = session?.user.gazelleId;
  const { mutate: mutateUser, key: keyUser } = useGetUserById(userId);
  const [isSaveEnabled, setIsSaveEnabled] = useState(true);
  const { hasUnsavedChanges, setHasUnsavedChanges, handleNavigation } = useUnsavedChanges();

  const { user, userPictureUrl, newUserPicture, inputs, orga, userActivation, userGroups } = useEditUserContext();

  const [initialInputs, setInitialInputs] = useState(inputs);

  useEffect(() => {
    const hasInvalidInput = validateInputs(inputs);
    setIsSaveEnabled(hasUnsavedChanges && !hasInvalidInput);
  }, [inputs, initialInputs, hasUnsavedChanges]);

  const userFirstName = inputs?.find((input) => input.name === "firstName")?.value ?? "";
  const userLastName = inputs?.find((input) => input.name === "lastName")?.value ?? "";
  const userEmail = inputs?.find((input) => input.name === "email")?.value ?? "";
  const userOrganizationId = orga.value;

  const isSidePanelContext = editUserProps.isSidePanelContext;

  const getUserInformationMessage = () => {
    if (user?.delegated)
      return (
        <div className="m-auto">
          <div className="max-w-sm m-auto">
            <Badge id="typeUser" variant="variant-2">
              {t("gzl.gum.delegated_account")}
            </Badge>
          </div>
          <div className="pt-5 max-w-1/2 m-auto">
            <Badge id="typeUser" variant="warning">
              {t("gzl.gum.delegated_info")}
            </Badge>
          </div>
        </div>
      );
    else if (userActivation) {
      return [];
    } else {
      return (
        <div className="m-auto">
          <div className="pt-5 max-w-1/2 m-auto">
            <Badge id="typeUser" variant="warning">
              {t("gzl.gum.disabled_info")}
            </Badge>
          </div>
        </div>
      );
    }
  };

  const handleCancel = () => {
    handleNavigation(() => {
      if (editUserProps.originUrl !== null && editUserProps.originUrl !== undefined) {
        router.push(editUserProps.originUrl as Route);
      } else {
        router.back();
      }
    });
  };

  const handleSubmit = (event: React.SubmitEvent) => {
    const formData = new FormData(event.currentTarget as HTMLFormElement);

    const userPrefTableLabel = formData.get("tableLabel") as string;
    const userPrefNotificationByEmail = formData.get("notificationByEmail") === "on";
    const languagesSpokenValue = formData.get("languagesSpoken") as string;
    const userPrefLanguagesSpoken = languagesSpokenValue ? JSON.parse(languagesSpokenValue) : [];

    const actionParams = {
      user,
      userFirstName,
      userLastName,
      userEmail,
      userActivation,
      userGroups,
      userOrganizationId,
      userPrefTableLabel,
      userPrefNotificationByEmail,
      userPrefLanguagesSpoken,
      userPictureUrl,
      newUserPicture,
      session: session,
      inputs,
      setHasUnsavedChanges,
      setInitialInputs,
      mutateUser,
      keyUser,
      isSidePanelContext,
      t,
    };

    handleFormSubmit(event, actionParams);
  };

  return (
    <form onSubmit={handleSubmit} className={`flex flex-col gap-8 inset-shadow-sm p-2 bg-white`}>
      <div className={`${isSidePanelContext && "flex-col"} flex justify-center items-center gap-4`}>
        <ProfilePicture userFirstName={userFirstName} userLastName={userLastName} />
      </div>
      {getUserInformationMessage()}
      <div className={`flex flex-col ${!isSidePanelContext && "md:flex-row"} justify-around gap-4`}>
        <div className="basis-1/4">
          <EditUserAttributes delegated={user?.delegated ?? false} />
        </div>

        <div className="basis-1/4">
          <RoleAttribution
            account={editUserProps.account}
            onSelf={session?.user.gazelleId === user?.id}
            delegated={user?.delegated ?? false}
            session={session}
          />
        </div>

        <div className="basis-1/4">
          <EditUserPreferences userId={user?.id} userActivation={userActivation} onPreferencesChange={() => setHasUnsavedChanges(true)} />
          {!isSidePanelContext && user && <EditUserSecurity user={user} account={editUserProps.account} delegated={user?.delegated ?? false} />}
        </div>
      </div>

      {userEmail !== user?.email && (
        <p className="text-center text-red">
          {session?.user.gazelleId === user?.id ? t("gzl.gum.email_updated_account_disabled") : t("gzl.gum.email_updated_user_disabled")}.
        </p>
      )}

      <div className="flex justify-center gap-5">
        {!isSidePanelContext && (
          <Button id={componentId} variant="secondary" title={t("gzl.user.interface.cancel")} ariaLabel="cancel" type="button" onClick={handleCancel}>
            <ArrowLeft size={16} />
            {t("gzl.user.interface.cancel")}
          </Button>
        )}
        <Button
          id={componentId}
          variant="validation"
          title={t("gzl.user.interface.save")}
          ariaLabel="save"
          type="submit"
          disabled={!isSaveEnabled || !userActivation}
        >
          <Save size={16} />
          {t("gzl.user.interface.save")}
        </Button>
      </div>

      {isSidePanelContext && user && (
        <EditUserSecurity user={user} account={session?.user.gazelleId === user?.id} delegated={user?.delegated ?? false} />
      )}
    </form>
  );
};

export default EditUser;
