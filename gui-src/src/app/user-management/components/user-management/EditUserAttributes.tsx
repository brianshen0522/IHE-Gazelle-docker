import { useId } from "react";
import { Input, SectionTitle, InfoRow } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { validateEmail, validateUserNames } from "@user-management/utils/validation";
import { useEditUserContext } from "@user-management/context/EditUserContext";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";
import { useGetOrganizationFromId } from "@/shared/hooks/useGetUserInformation";

const EditUserAttributes = ({ delegated }: { delegated: boolean }) => {
  const id = useId();
  const { t } = useTranslation();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const { inputs, setInputs, orga, userActivation } = useEditUserContext();

  const { data: orgaData } = useGetOrganizationFromId(orga.value ?? "");
  const { name } = orgaData?.data || {};

  const validateInput = (name: string, value: string) => {
    let isValid = true;
    let validationMessage = "";

    if (name === "firstName" || name === "lastName") {
      isValid = validateUserNames(value);
      if (!isValid) {
        validationMessage = `${name === "firstName" ? t("gzl.gum.first_name") : t("gzl.gum.last_name")} ${t("gzl.gum.is_invalid")}`;
      }
    } else if (name === "email") {
      isValid = validateEmail(value);
      if (!isValid) {
        validationMessage = t("gzl.gum.invalid_email");
      }
    }

    return { isValid, validationMessage };
  };

  const handleInputChange = (name: string, newValue: string) => {
    const { isValid, validationMessage } = validateInput(name, newValue);
    setInputs(
      inputs.map((input) =>
        input.name === name
          ? {
              ...input,
              value: newValue,
              isValidInput: isValid,
              validationMessage,
            }
          : input,
      ),
    );
    setHasUnsavedChanges(true);
  };

  return (
    <div className="flex flex-col gap-4">
      <SectionTitle id={t("gzl.gum.general_information")} title={t("gzl.gum.general_information")} />
      {inputs.map((input, index) => (
        <Input
          id={`${id}-${index}`}
          key={`${id}-${index}`}
          type={input.type}
          label={t(input.label)}
          htmlFor={`${id}-${index}`}
          name={input.name}
          placeholder={input.value || ""}
          value={input.value || ""}
          setValue={(newValue) => handleInputChange(input.name, newValue)}
          readonly={input.readonly}
          isValidInput={input.isValidInput}
          validationMessage={input.validationMessage}
          disabled={delegated || !userActivation}
          required
        />
      ))}

      <InfoRow label={t("gzl.user.interface.organization")} value={name || ""} />
    </div>
  );
};
export default EditUserAttributes;
