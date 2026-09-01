/**
 * Returns the appropriate localStorage key for saved searches based on the current app.
 * This function extracts the app name from the URL path to create a dynamic storage key.
 * URL structure is expected to be /gazelle/{app-name}/...
 *
 * @returns The app-specific localStorage key for saved searches
 */
export const getAppStorageKey = (): string => {
  // Use globalThis.location.pathname to determine which app we're in
  if (typeof globalThis === "undefined") {
    return "default-savedSearch"; // Fallback for server-side rendering
  }

  const pathname = globalThis.location.pathname;

  // Extract the relevant path segments
  const pathSegments = pathname.split("/").filter(Boolean);

  // URL pattern is /gazelle/{app-name}/...
  if (pathSegments.length >= 2 && pathSegments[0] === "gazelle") {
    return `${pathSegments[1]}-savedSearch`;
  }

  // Default fallback for home or unknown paths
  return "gazelle-savedSearch";
};
