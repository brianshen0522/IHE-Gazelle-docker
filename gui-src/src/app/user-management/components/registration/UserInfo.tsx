import { useTranslation } from "react-i18next";
import { InfoRow } from "@gazelle/gazelle-component-ui";
import { UserInfos } from "./types";

const UserInfo = ({ orgName, userInfos }: { orgName: string; userInfos: UserInfos }) => {
  const { t } = useTranslation();
  const { firstName, lastName, email } = userInfos;

  const userFields = [
    { id: "firstName", label: t("gzl.gum.first_name"), value: firstName },
    { id: "lastName", label: t("gzl.gum.last_name"), value: lastName },
    { id: "email", label: t("gzl.gum.email"), value: email },
    { id: "organization", label: t("gzl.gum.organization"), value: orgName },
  ];

  return (
    <section className="w-full flex flex-col gap-y-2 text-md" aria-labelledby="user-info">
      {userFields.map((field) => (
        <InfoRow key={field.id} label={field.label} value={field.value} />
      ))}
    </section>
  );
};

export default UserInfo;
