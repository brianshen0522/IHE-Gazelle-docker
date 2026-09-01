import i18n from "i18next";
import { initReactI18next } from "react-i18next/initReactI18next";
import french from "../locales/fr/gzl_user_interface_translation.json";
import english from "../locales/sources.json";
import { libResources } from "@gazelle/gazelle-component-ui";

const resources = {
  en: {
    translation: english,
    lib: libResources.en.lib,
  },
  fr: {
    translation: french,
    lib: libResources.fr.lib,
  },
  // de: {
  //   translation: german,
  //   lib: libResources.de.lib,
  // },
  // it: {
  //   translation: italian,
  //   lib: libResources.it.lib,
  // },
  // es: {
  //   translation: spanish,
  //   lib: libResources.es.lib,
  // },
};

i18n.use(initReactI18next).init({
  resources,
  lng: "en",
  fallbackLng: "en",
  debug: false,
  interpolation: {
    escapeValue: false, // react already safes from xss
  },
});

export default i18n;
