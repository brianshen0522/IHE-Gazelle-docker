import { useTranslation } from "react-i18next";
import { useTranslateErrorMessage } from "@user-management/hooks/useTranslateErrorMessage";
import { TriangleAlert } from "lucide-react";

interface RegistrationFailedProps {
  errorMessage: string;
}

const RegistrationFailed: React.FC<RegistrationFailedProps> = ({ errorMessage }) => {
  const { t } = useTranslation();
  const translatedErrorMessage = useTranslateErrorMessage(errorMessage);

  return (
    <div
      id="cypressValidation"
      data-testid="registration-failed"
      className="w-full border border-red flex flex-col justify-center rounded-2xl gap-4 overflow-hidden"
    >
      <h3 className="bg-red flex justify-center text-white p-2">{t("gzl.gum.registration_error")}</h3>

      <TriangleAlert className="text-red mx-auto mt-2 size-12" />

      <p className="px-4 pb-5 text-center text-lg text-red">{translatedErrorMessage}</p>
    </div>
  );
};

export default RegistrationFailed;
