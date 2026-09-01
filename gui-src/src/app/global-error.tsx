"use client"; // Error boundaries must be Client Components
import React, { useEffect } from "react";
import { CustomInternalError, Header } from "@gazelle/gazelle-component-ui";
import { Montserrat } from "next/font/google";
import SessionWrapper from "@shared/components/auth/SessionWrapper";
import { usePathname, useRouter } from "next/navigation";
import { signIn, signOut } from "next-auth/react";

const montserrat = Montserrat({
  subsets: ["latin"],
  variable: "--font-montserrat",
});

export default function GlobalError({
  error,
}: Readonly<{
  error: Error & { digest?: string };
  reset: () => void;
}>) {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    // Log the error to an error reporting service
    console.error(error);
  }, [error]);

  return (
    // global-error must include html and body tags

    <html lang="en" className={`${montserrat.variable} antialiased`}>
      <SessionWrapper>
        <body>
          <Header session={null} pathname={pathname} signIn={signIn} signOut={signOut} router={router} lang={"en"} />
          <CustomInternalError />
        </body>
      </SessionWrapper>
    </html>
  );
}
