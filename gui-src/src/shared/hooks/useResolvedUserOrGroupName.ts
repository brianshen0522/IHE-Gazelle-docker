"use client";

import { useEffect, useState } from "react";
import { Session } from "next-auth";
import { getDisplayGroupFromGroupId } from "@/app/user-management/utils/roleMappers";
import { formatUserOrGroupById } from "@message-capture/services/formatUserOrGroupById";

type ResolvedUserOrGroupName = {
  displayName: string;
  isLoading: boolean;
};

const resolvedNameCache = new Map<string, string>();
const inFlightNameCache = new Map<string, Promise<string>>();

function getCacheKey(id: string, session: Session | null | undefined): string {
  return `${session?.access_token ?? "anonymous"}:${id}`;
}

function translateResolvedName(name: string, t: (key: string) => string): string {
  if (name.includes("gzl.gum.organization_admin")) {
    name = name.replace("gzl.gum.organization_admin", t("gzl.gum.organization_admin"));
  }

  const translated = t(name);
  return translated === name ? name : translated;
}

function resolveClientSideName(id: string, t: (key: string) => string): string | undefined {
  const groupKey = getDisplayGroupFromGroupId(id);
  if (groupKey !== undefined) {
    return t(`gzl.gum.${groupKey}`);
  }
  return undefined;
}

async function loadResolvedName(id: string, session: Session | null | undefined, t: (key: string) => string): Promise<string> {
  const clientResolvedName = resolveClientSideName(id, t);
  if (clientResolvedName !== undefined) {
    return clientResolvedName;
  }

  const result = await formatUserOrGroupById(id, session);
  if (result.name === "Unknown user or group") {
    return id;
  }

  return translateResolvedName(result.name, t);
}

export function useResolvedUserOrGroupName(id: string, session: Session | null | undefined, t: (key: string) => string): ResolvedUserOrGroupName {
  const clientResolvedName = resolveClientSideName(id, t);
  const canResolveServerSide = Boolean(session?.access_token) || id.startsWith("org") || id === "gazelle" || id === "user";
  const cacheKey = getCacheKey(id, session);
  const cachedName = resolvedNameCache.get(cacheKey) ?? clientResolvedName;
  const [displayName, setDisplayName] = useState<string>(cachedName ?? "");
  const [isLoading, setIsLoading] = useState<boolean>(cachedName === undefined && canResolveServerSide);

  useEffect(() => {
    let isActive = true;

    if (cachedName !== undefined) {
      setDisplayName(cachedName);
      setIsLoading(false);
      return;
    }

    if (!canResolveServerSide) {
      setDisplayName("");
      setIsLoading(true);
      return;
    }

    const pendingRequest = inFlightNameCache.get(cacheKey) ?? loadResolvedName(id, session, t);
    inFlightNameCache.set(cacheKey, pendingRequest);
    setIsLoading(true);

    pendingRequest
      .then((resolvedName) => {
        if (resolvedName !== id) {
          resolvedNameCache.set(cacheKey, resolvedName);
        }
        inFlightNameCache.delete(cacheKey);
        if (!isActive) {
          return;
        }
        setDisplayName(resolvedName);
        setIsLoading(false);
      })
      .catch(() => {
        inFlightNameCache.delete(cacheKey);
        if (!isActive) {
          return;
        }
        setDisplayName(id);
        setIsLoading(false);
      });

    return () => {
      isActive = false;
    };
  }, [cacheKey, cachedName, canResolveServerSide, id, session, t]);

  return {
    displayName,
    isLoading,
  };
}
