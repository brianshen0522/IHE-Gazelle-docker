import React from "react";
import "./globals.css";
import "@gazelle/gazelle-component-ui/dist/index.css";
import type { Metadata } from "next";
import { Montserrat } from "next/font/google";
import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import { TabProvider } from "@/shared/context/tabContext";
import SessionWrapper from "@shared/components/auth/SessionWrapper";
import { readTestBedConfigurations, readNavbarConfiguration } from "@home/actions";
import { getCookieConsent } from "@/shared/actions/cookies";
import { GazelleRootLayout } from "@/shared/components/layout/GazelleRootLayout";
import { cookies } from "next/headers";
import { getGUMConfigurations } from "../app/user-management/actions";

const montserrat = Montserrat({
  subsets: ["latin"],
  variable: "--font-montserrat",
});

export const metadata: Metadata = {
  title: "Gazelle",
  description: "Gazelle user interface",
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const session = await getServerSession(authOptions);
  const testBedConfigurations = await readTestBedConfigurations();
  const navbarConfig = await readNavbarConfiguration();
  const initialConsent = await getCookieConsent();
  const cookieLocale = await cookies();
  const locale = cookieLocale.get("GZL_LOCALE")?.value ?? "en";
  const { data: GUMConfigurations } = await getGUMConfigurations();

  return (
    <html lang={locale}>
      <SessionWrapper session={session?.user ? session : undefined}>
        <body className={`${montserrat.className} flex flex-col min-h-[calc(100dvh)] antialiased bg-bg_body`}>
          <TabProvider>
            <GazelleRootLayout
              session={session}
              testBedConfigurations={testBedConfigurations}
              navbarConfig={navbarConfig}
              initialConsent={initialConsent}
              lang={locale}
              userRegistrationEnabled={GUMConfigurations?.userRegistrationEnabled}
            >
              {children}
            </GazelleRootLayout>
          </TabProvider>
        </body>
      </SessionWrapper>
    </html>
  );
}
