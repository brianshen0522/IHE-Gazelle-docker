// Helper function to check if a segment is a dynamic route parameter (UUID, etc.)
export const isDynamicSegment = (segment: string): boolean => {
  const dynamicRoutePattern = /^[a-f0-9-]{36}$|^[A-Za-z0-9-_]{20,}$/;
  return dynamicRoutePattern.exec(segment) !== null;
};

// Helper function to check if a segment should be excluded from breadcrumbs
// Excludes route groups (Next.js convention: folders in parentheses)
// and common organizational folders that don't represent actual pages
export const isExcludedSegment = (segment: string, allSegments: string[], currentIndex: number): boolean => {
  // Exclude Next.js route groups: (group-name)
  if (segment.startsWith("(") && segment.endsWith(")")) {
    return true;
  }

  const isLastSegment = currentIndex === allSegments.length - 1;

  // Common organizational folder patterns that typically don't have their own pages
  const organizationalPatterns = [
    /^test-bed$/, // test-bed folder
    /^api$/, // API routes
    /^\(.*\)$/, // Route groups
  ];

  // Only exclude non-last segments that match organizational patterns
  if (!isLastSegment && organizationalPatterns.some((pattern) => pattern.test(segment))) {
    return true;
  }

  return false;
};

// Helper function to get the default list page for an app folder
export const getDefaultListPage = (appFolder: string, allSegments: string[]): string => {
  // If we have more segments, try to find a plural version of the last segment
  if (allSegments.length > 1) {
    const lastSegment = allSegments.at(-1);

    if (!lastSegment) {
      return `/${appFolder}`;
    }

    // Check if last segment is singular, try to find its plural in the path
    // Common patterns: message -> messages, sequence -> sequences, user -> users
    const pluralForm = lastSegment + "s";

    // If we're on a singular page (like "message"), link to plural (like "messages")
    if (allSegments.length >= 2 && !lastSegment.endsWith("s")) {
      return `/${appFolder}/${pluralForm}`;
    }

    // Otherwise, use the second segment (like "messages", "reports", etc.)
    return `/${appFolder}/${allSegments[1]}`;
  }

  // Fallback to just the app folder
  return `/${appFolder}`;
};
