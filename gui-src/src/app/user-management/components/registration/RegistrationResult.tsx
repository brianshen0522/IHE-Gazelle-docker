import { useTranslation } from "react-i18next";
import RegistrationFailed from "./RegistrationFailed";
import RegistrationSuccess from "./RegistrationSuccess";
import { ErrorResponse, RegistrationResultProps } from "@/app/user-management/components/registration/types";

const RegistrationResult = ({ result, joinOrCreateOrg, selectedOrg }: RegistrationResultProps) => {
  const { t } = useTranslation();
  return (
    <>
      {result?.status === "success" ? (
        <RegistrationSuccess joinOrCreateOrg={joinOrCreateOrg} selectedOrg={selectedOrg} />
      ) : (
        <RegistrationFailed errorMessage={(result.response.data as ErrorResponse)?.message || t("gzl.gum.default_error")} />
      )}
    </>
  );
};

export default RegistrationResult;
