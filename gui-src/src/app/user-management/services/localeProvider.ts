import { cookies, headers } from "next/headers";

/**
 * Return the selected locale of the current user.
 *
 * This provider will first check in the cookies, then in the accept-language of the request and if it is not found, it
 * will default to "en".
 * @param request a Request
 */
export const selectedLocaleProvider = async () => {
  const cookieStore = await cookies();
  let locale = cookieStore.get("GZL_LOCALE")?.value;
  if (!locale) {
    const hdrs = await headers();
    locale = hdrs.get("accept-language-header") || "en";
  }
  return locale;
};
