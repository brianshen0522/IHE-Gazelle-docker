"use client";
import CookiesBannerWrapper from "@/shared/components/cookies/CookiesBannerWrapper";
import { Header, Footer, Navbar, Skeleton } from "@gazelle/gazelle-component-ui";
import i18n from "@/i18n/i18n";
import { I18nextProvider } from "react-i18next";
import { useEnv } from "@/shared/hooks/useEnv";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useLayoutEffect, useState } from "react";
import { signIn, signOut } from "next-auth/react";
import { useUserPicture } from "@hooks/useUserPicture";
import { GazelleRootLayoutProps } from "./types";
import { getAppsFromServiceRegistry } from "@/app/home/components/services/actions";
import { USER_INTERFACE_STATIC_APPS } from "@shared/components/layout/getStaticApps";
import { Session } from "next-auth";
import { UnsavedChangesProvider } from "@/shared/context/UnsavedChangeContext";
import { useGetUserById } from "@user-management/hooks/swr/useGetUser";
import { isOrgAdmin } from "@shared/utils/permissions";
import { useGetOrganizationFromId } from "@/shared/hooks/useGetUserInformation";

export const LAST_TEST_SUITE_URL_KEY = "gzl_last_test_suite_url";

export function GazelleRootLayout({
  children,
  session,
  testBedConfigurations,
  navbarConfig,
  initialConsent,
  lang,
  userRegistrationEnabled,
}: Readonly<GazelleRootLayoutProps>) {
  const { env } = useEnv();
  const router = useRouter();
  const pathname = usePathname();
  const { pictureUrl, isLoading: isPictureLoading } = useUserPicture(session as Session, "thumbnail");
  const staticApplications = session?.user
    ? USER_INTERFACE_STATIC_APPS
    : USER_INTERFACE_STATIC_APPS.filter((application) => application.url !== "/gazelle/reports");
  const { data: currentUser } = useGetUserById(session?.user?.gazelleId ?? "");
  const { data: organizationUser } = useGetOrganizationFromId(session?.user.organization ?? "");

  // User can edit if: (1) is org admin AND (2) organization is not delegated
  const hasOrgAdminRole = isOrgAdmin(session ?? null);
  const isDelegated = currentUser?.data?.delegated === true;
  const canEditOrganization = hasOrgAdminRole && !isDelegated;
  const editOrganizationPath = canEditOrganization ? "/user-management/organization/edit" : undefined;

  const [isLanguageReady, setIsLanguageReady] = useState(false);

  useLayoutEffect(() => {
    // Set the language and mark as ready
    if (i18n.language === lang) {
      setIsLanguageReady(true);
    } else {
      i18n.changeLanguage(lang).then(() => setIsLanguageReady(true));
    }
  }, [lang]);

  useEffect(() => {
    if (pathname.includes("test-suite")) {
      sessionStorage.setItem(LAST_TEST_SUITE_URL_KEY, pathname);
    }
  }, [pathname]);

  useEffect(() => {
    const listener = (event: CustomEvent<string>) => {
      const newLang = event.detail;
      if (i18n.language !== newLang) {
        i18n.changeLanguage(newLang);
      }
    };

    globalThis.addEventListener("languageChanged", listener as EventListener);
    return () => globalThis.removeEventListener("languageChanged", listener as EventListener);
  }, []);

  if (!isLanguageReady) {
    return <Skeleton className="h-screen" />;
  }

  const appName = testBedConfigurations?.platformName;

  return (
    <I18nextProvider i18n={i18n}>
      <UnsavedChangesProvider>
        <Header
          session={session}
          pathname={pathname}
          env={{ ...env, pathname }}
          logoCustomer={testBedConfigurations?.logoCustomerUrl}
          logoCustomerUrl={testBedConfigurations?.logoCustomerWebsiteUrl}
          signIn={signIn}
          signOut={signOut}
          router={router}
          userPictureUrl={pictureUrl ?? ""}
          isPictureLoading={isPictureLoading}
          lang={lang}
          appName={appName}
          userRegistrationEnabled={userRegistrationEnabled}
          organizationUser={organizationUser?.data?.name}
          editOrganizationPath={editOrganizationPath}
        />

        <main className="flex flex-col md:flex-row flex-grow overflow-auto">
          <Navbar
            staticApplications={staticApplications}
            config={navbarConfig}
            getAppsFromServiceRegistry={getAppsFromServiceRegistry}
            groups={session?.user?.groups ?? []}
            documentationUrl={testBedConfigurations?.favoriteDocUrl}
            testBedName={appName}
          />
          <div className="flex-1 overflow-y-auto overflow-x-auto">{children}</div>
        </main>

        <Footer testBedConfigurations={testBedConfigurations} />
        <CookiesBannerWrapper initialConsent={initialConsent} cookiesPreferencesUrl={testBedConfigurations?.cookiesPreferencesUrl} />
      </UnsavedChangesProvider>
    </I18nextProvider>
  );
}
