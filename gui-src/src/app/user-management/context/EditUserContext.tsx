"use client";
import { createContext, useContext, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import ppGazelle from "@/shared/assets/gazelle_pp.png";
import { User } from "@/app/user-management/components/user-management/Types";

type EditUserContextData = {
  user: User | null;
  userPictureUrl: string | null;
  setUserPictureUrl: (url: string | null) => void;
  newUserPicture: ArrayBuffer | null;
  setNewUserPicture: (picture: ArrayBuffer | null) => void;
  inputs: {
    label: string;
    type: string;
    htmlFor: string;
    name: string;
    placeholder: string;
    value: string;
    readonly: boolean;
    isValidInput: boolean;
    validationMessage?: string;
  }[];
  orga: {
    value: string;
  };
  setOrga: (orga: { value: string }) => void;
  setInputs: (
    inputs: {
      label: string;
      type: string;
      htmlFor: string;
      name: string;
      placeholder: string;
      value: string;
      readonly: boolean;
      isValidInput: boolean;
      validationMessage?: string;
    }[],
  ) => void;
  userActivation: boolean;
  setUserActivation: (activation: boolean) => void;
  userGroups: string[];
  setUserGroups: (groups: string[]) => void;
};

type EditUserProviderProps = {
  children: React.ReactNode;
  user?: User | null;
};

const EditUserContext = createContext<EditUserContextData | null>(null);

export const EditUserContextProvider = ({ children, user = null }: EditUserProviderProps) => {
  const { t } = useTranslation();
  const [userPictureUrl, setUserPictureUrl] = useState<string | null>(ppGazelle.src);
  const [newUserPicture, setNewUserPicture] = useState<ArrayBuffer | null>(null);
  const [inputs, setInputs] = useState([
    {
      label: t("gzl.gum.first_name"),
      type: "text",
      htmlFor: "firstName",
      name: "firstName",
      placeholder: t("gzl.gum.first_name"),
      value: user?.firstName ?? "",
      readonly: false,
      isValidInput: true,
    },
    {
      label: t("gzl.gum.last_name"),
      type: "text",
      htmlFor: "lastName",
      name: "lastName",
      placeholder: t("gzl.gum.last_name"),
      value: user?.lastName ?? "",
      readonly: false,
      isValidInput: true,
    },
    {
      label: t("gzl.gum.email"),
      type: "text",
      htmlFor: "email",
      name: "email",
      placeholder: t("gzl.gum.email"),
      value: user?.email ?? "",
      readonly: false,
      isValidInput: true,
    },
  ]);

  const [orga, setOrga] = useState({
    value: user?.organizationId ?? "",
  });
  const [userActivation, setUserActivation] = useState(user?.activated ?? false);
  const [userGroups, setUserGroups] = useState(user?.groupIds ?? []);

  const contextValue = useMemo(
    () => ({
      user,
      userPictureUrl,
      setUserPictureUrl,
      newUserPicture,
      setNewUserPicture,
      inputs,
      setInputs,
      orga,
      setOrga,
      userActivation,
      setUserActivation,
      userGroups,
      setUserGroups,
    }),
    [user, userPictureUrl, newUserPicture, inputs, orga, userActivation, userGroups],
  );

  return <EditUserContext.Provider value={contextValue}>{children}</EditUserContext.Provider>;
};

export const useEditUserContext = () => {
  const context = useContext(EditUserContext);
  if (!context) {
    throw new Error("useEditUserContext must be used within a EditUserContext");
  }
  return context;
};
