import { useTranslation } from "react-i18next";
import fr from "../../../../locales/fr/gzl_user_interface_translation.json";
import en from "../../../../locales/sources.json";

// This function is used to reverse the object keys and values
const reverseObject = (object: Record<string, string>) => {
  return Object.keys(object).reduce((acc, key) => {
    acc[object[key]] = key;
    return acc;
  }, {} as Record<string, string>);
};

export const useTranslateErrorMessage = (errorMessage: string) => {
  const { i18n } = useTranslation();

  const translationMap = i18n.language === "en" ? en["gzl.gum"] : fr["gzl.gum"];
  const reverseMap = reverseObject(en["gzl.gum"]);

  const key = reverseMap[errorMessage];

  // If the key exists in the translation map, return the value
  if (key && key in translationMap) {
    return translationMap[key as keyof typeof translationMap];
  }

  return errorMessage;
};
