/**
 * Normalizes a key by converting dot notation to camelCase
 * For example: "user.first.name" becomes "userFirstName"
 *
 * @param key The key to normalize
 * @returns The normalized key
 */
export const normalizeKey = (key: string): string => {
  // Replace dots followed by lowercase letters with uppercase letters
  return (
    key
      .replaceAll(/\.([a-z])/g, (_, letter) => letter.toUpperCase())
      // Remove any remaining dots
      .replaceAll('.', "")
  );
};
