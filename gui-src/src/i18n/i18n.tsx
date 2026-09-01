import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import french from "../../locales/fr/gzl_user_interface_translation.json";
import czech from "../../locales/cs/gzl_user_interface_translation.json";
import english from "../../locales/sources.json";
import { libResources } from "@gazelle/gazelle-component-ui";

const getLibraryResources = (language: string) => libResources?.[language]?.lib ?? {};

const resources: any = {
  en: {
    translation: english,
    lib: getLibraryResources("en"),
  },
  fr: {
    translation: french,
    lib: libResources.fr.lib,
  },
};

if (libResources?.cs) {
  resources.cs = {
    translation: czech,
    lib: getLibraryResources("cs"),
  };
}

if (libResources?.br) {
  const breton = require("../../locales/br/gzl_user_interface_translation.json");
  resources.br = {
    translation: breton,
    lib: getLibraryResources("br"),
  };
}

i18n.use(initReactI18next).init({
  resources,
  fallbackLng: "en",
  interpolation: {
    escapeValue: false, // react already safes from xss
  },
});

export default i18n;
