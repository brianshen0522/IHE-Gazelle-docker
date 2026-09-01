import { TFunction } from "i18next";

// Internal helper to translate a name, handling special organization_admin key.
const translateName = (name: string, t: TFunction): string => {
  let translatedName = name;

  if (name.includes("gzl.gum.organization_admin")) {
    const translation = t("gzl.gum.organization_admin");
    translatedName = name.replace("gzl.gum.organization_admin", translation);
  }

  return t(translatedName);
};

// Format a user or group with translation support.
// Replaces translation keys (e.g., "gzl.gum.organization_admin") with their translated values.
export const formatWithTranslation = (value: { id: string; name: string }, t: TFunction): { id: string; name: string } => {
  return {
    ...value,
    name: translateName(value.name, t),
  };
};

// Format a user or group with organization and translation support.
// Handles translation keys and organization information.
export const formatWithTranslationWithOrga = (
  value: { id: string; name: string; organization: string },
  t: TFunction,
): { id: string; name: string; organization: string } => {
  const formatted = formatWithTranslation(value, t);
  return {
    ...formatted,
    organization: value.organization,
  };
};

// Format an array of users or groups for display with translations.
export const formatItemsWithTranslation = <T extends { id: string; name: string }>(items: T[], t: TFunction): T[] => {
  return items.map((item) => formatWithTranslation(item, t) as T);
};

// Format an array of users or groups with organization information.
export const formatItemsWithTranslationAndOrga = <T extends { id: string; name: string; organization: string }>(items: T[], t: TFunction): T[] => {
  return items.map((item) => formatWithTranslationWithOrga(item, t) as T);
};
