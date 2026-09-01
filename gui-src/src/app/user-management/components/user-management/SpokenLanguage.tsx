import React from "react";
import { MultiSelectInput } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";

type SpokenLanguageProps = {
  userActivated: boolean;
  languagesSpoken: string[];
  setLanguagesSpoken: (languages: string[]) => void;
};

const SpokenLanguage = ({ userActivated, languagesSpoken, setLanguagesSpoken }: SpokenLanguageProps) => {
  const { t } = useTranslation();

  const handleLanguageChange = (input: Array<{ value: string; label: string }> | { value: string; label: string } | null) => {
    if (Array.isArray(input)) {
      setLanguagesSpoken(input.map((option) => option.value));
    } else if (input) {
      setLanguagesSpoken([input.value]);
    } else {
      setLanguagesSpoken([]);
    }
  };

  return (
    <MultiSelectInput
      id={t("gzl.gum.spoken_languages")}
      label={t("gzl.gum.spoken_languages")}
      htmlFor={t("gzl.gum.spoken_languages")}
      name="languages"
      ariaLabel={t("gzl.gum.spoken_languages")}
      ariaLabelledby={t("gzl.gum.spoken_languages")}
      placeholder={t("gzl.gum.choose_language")}
      options={[
        { value: "english", label: t("gzl.gum.english") },
        { value: "french", label: t("gzl.gum.french") },
        { value: "german", label: t("gzl.gum.german") },
        { value: "portuguese", label: t("gzl.gum.portuguese") },
        { value: "spanish", label: t("gzl.gum.spanish") },
        { value: "italian", label: t("gzl.gum.italian") },
        { value: "japanese", label: t("gzl.gum.japanese") },
        { value: "chinese", label: t("gzl.gum.chinese") },
        { value: "russian", label: t("gzl.gum.russian") },
        { value: "arabic", label: t("gzl.gum.arabic") },
      ]}
      value={languagesSpoken?.map((language) => ({ value: language, label: t(language) })) ?? []}
      isClearable={true}
      // @ts-ignore because the react-select lib does not accept true as boolean ...
      isMulti={true}
      isDisabled={!userActivated}
      handleChange={handleLanguageChange}
    />
  );
};

export default SpokenLanguage;
