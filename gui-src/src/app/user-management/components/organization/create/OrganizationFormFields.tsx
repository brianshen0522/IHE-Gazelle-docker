import { Input } from "@gazelle/gazelle-component-ui";
import { validateOrganizationName, validateShortName } from "@/app/user-management/utils/validation";
import { useTranslation } from "react-i18next";

interface OrganizationFormFieldsProps {
  name: string;
  setName: (value: string) => void;
  shortName: string;
  setShortName: (value: string) => void;
}

const OrganizationFormFields = ({ name, setName, shortName, setShortName }: OrganizationFormFieldsProps) => {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col gap-4">
      <Input
        id="name"
        type="text"
        label={t("gzl.user.interface.name") + "*"}
        htmlFor="name"
        name="name"
        placeholder={t("gzl.user.interface.name")}
        value={name}
        setValue={setName}
        required
        isValidInput={validateOrganizationName(name)}
        validationMessage={t("gzl.user.interface.invalid_name")}
      />

      <Input
        id="shortName"
        type="text"
        label={t("gzl.user.interface.short_name") + "*"}
        htmlFor="shortName"
        name="shortName"
        placeholder={t("gzl.user.interface.short_name")}
        value={shortName}
        setValue={setShortName}
        required
        isValidInput={validateShortName(shortName)}
        validationMessage={t("gzl.user.interface.invalid_short_name")}
      />
    </div>
  );
};

export default OrganizationFormFields;
