"use client";
import React from "react";
import type { Session } from "next-auth";
import { SessionProvider } from "next-auth/react";
import { authOptions } from "./authOptions";

type SessionWrapperProps = {
  session?: Session;
  children: React.ReactNode;
};

const SessionWrapper = ({ session, children }: SessionWrapperProps) => {
  const refetchInterval = authOptions.session.maxAge / 3;
  return (
    <SessionProvider basePath="/gazelle/api/auth" session={session} refetchInterval={refetchInterval}>
      {children}
    </SessionProvider>
  );
};

export default SessionWrapper;
