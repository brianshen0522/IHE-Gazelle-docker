/**
 * Validates if a string is valid base64 encoded data
 * @param str - The string to validate
 * @returns true if the string is valid base64, false otherwise
 */
export const isBase64Encoded = (str: string | null | undefined): boolean => {
  // Check if string exists and is not empty
  if (!str || str.trim() === '') {
    return false;
  }

  // Remove whitespace for validation
  const trimmed = str.trim();

  // Base64 regex pattern:
  // - Must contain only valid base64 characters: A-Z, a-z, 0-9, +, /, =
  // - Padding (=) can only be at the end
  // - Length must be a multiple of 4
  const base64Regex = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;

  // Check if string matches base64 pattern
  if (!base64Regex.test(trimmed)) {
    return false;
  }

  // Additional validation: try to decode it
  try {
    atob(trimmed);
    return true;
  } catch (e) {
    // If atob throws an error, it's not valid base64
    return false;
  }
};

