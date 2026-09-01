import { useTranslation } from "react-i18next";
import UserInfo from "./UserInfo";
import { RegistrationValidationProps } from "./types";

const RegistrationValidation = ({ userInfos, joinOrCreateOrg, selectedOrg, organizationInfos, configs }: RegistrationValidationProps) => {
  const { t } = useTranslation();
  const { termsOfServiceUrl } = configs;

  const isCreateOrganization = joinOrCreateOrg === "CREATE" && organizationInfos ? organizationInfos.name : "";
  const orgName = joinOrCreateOrg === "JOIN" ? selectedOrg?.name || "" : isCreateOrganization;

  const orgMessage = joinOrCreateOrg === "JOIN" ? t("gzl.gum.join_existing_organization") : t("gzl.gum.organization_creation_confirmation");

  return (
    <div className="flex flex-col gap-8">
      <UserInfo orgName={orgName} userInfos={userInfos} />

      <div data-cy={joinOrCreateOrg === "JOIN" ? "join-summary" : undefined}>
        {orgMessage} {orgName}
      </div>

      <div className="w-full text-base">
        {t("gzl.gum.terms_service_privacy_policy_agreement")}
        <a
          href={termsOfServiceUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="underline text-link visited:text-visited_link hover:text-visited_link"
          data-cy="tos"
        >
          {t("gzl.gum.terms_service")}
        </a>
      </div>
    </div>
  );
};

export default RegistrationValidation;
