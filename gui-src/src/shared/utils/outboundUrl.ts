/**
 * Server-side outbound requests built from user input must stay on origins the
 * deployment configured; this is what keeps the proxy-style routes from reaching
 * internal services.
 */
export function isAllowedOutboundUrl(target: URL, allowedBaseUrls: (string | undefined)[]): boolean {
  if (target.protocol !== "http:" && target.protocol !== "https:") {
    return false;
  }

  return allowedBaseUrls.some((baseUrl) => {
    if (!baseUrl) {
      return false;
    }
    try {
      return new URL(baseUrl).origin === target.origin;
    } catch {
      return false;
    }
  });
}
