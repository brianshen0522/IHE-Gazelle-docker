"use client";
import { CookiesBanner } from "@gazelle/gazelle-component-ui";
import { setCookieConsent } from "@/shared/actions/cookies";
import { TestBedConfigurations } from "../layout/types";

interface CookiesBannerWrapperProps extends Pick<TestBedConfigurations, "cookiesPreferencesUrl"> {
  initialConsent: string | null;
}

export default function CookiesBannerWrapper({ cookiesPreferencesUrl, initialConsent }: Readonly<CookiesBannerWrapperProps>) {
  return <CookiesBanner initialConsent={initialConsent} onAccept={setCookieConsent} learnMoreLink={cookiesPreferencesUrl || "/"} />;
}
