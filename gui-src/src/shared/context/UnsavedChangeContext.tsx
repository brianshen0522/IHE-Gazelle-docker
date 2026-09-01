"use client";
import React, { createContext, useContext, ReactNode, useEffect } from "react";
import { useUnsavedChangesGuard, UnsavedChangesGuard } from "@hooks/useUnsavedChangeGuard";
import { useRouter } from "next/navigation";
import { Route } from "next";

const UnsavedChangesContext = createContext<UnsavedChangesGuard | null>(null);

type UnsavedChangesProviderProps = {
  children: ReactNode;
};

const shouldIgnoreHref = (href: string) => href.startsWith("#") || href.startsWith("mailto:") || href.startsWith("tel:") || href.startsWith("blob:");

const isExternalLink = (link: HTMLAnchorElement, href: string) =>
  link.target === "_blank" || link.rel.includes("external") || (href.startsWith("http") && !href.includes(globalThis.location.hostname));

const clickLinkAfterStateUpdate = (link: HTMLAnchorElement) => {
  setTimeout(() => link.click(), 0);
};

const continueNavigationAfterConfirmation = (guard: UnsavedChangesGuard, link: HTMLAnchorElement) => {
  guard.setHasUnsavedChanges(false);
  clickLinkAfterStateUpdate(link);
};

export const UnsavedChangesProvider = ({ children }: UnsavedChangesProviderProps) => {
  const guard = useUnsavedChangesGuard();

  // Intercept all link clicks globally
  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      // Only intercept if there are unsaved changes
      if (!guard.hasUnsavedChanges) return;

      const target = e.target as HTMLElement;
      // Find the closest anchor element
      const link = target.closest("a");

      if (!link) return;

      // Check if it's a Next.js Link (internal navigation)
      const href = link.getAttribute("href");
      if (!href || shouldIgnoreHref(href)) {
        return;
      }

      // Check if it's an external link
      if (isExternalLink(link, href)) return;

      // Prevent navigation and show confirmation
      e.preventDefault();
      e.stopPropagation();

      guard.handleNavigation(() => continueNavigationAfterConfirmation(guard, link));
    };

    document.addEventListener("click", handleClick, true);
    return () => document.removeEventListener("click", handleClick, true);
  }, [guard]);

  return (
    <UnsavedChangesContext.Provider value={guard}>
      {children}
      <guard.ConfirmationModal />
    </UnsavedChangesContext.Provider>
  );
};

export const useUnsavedChanges = () => {
  const context = useContext(UnsavedChangesContext);
  if (!context) {
    throw new Error("useUnsavedChanges must be used within UnsavedChangesProvider");
  }

  const router = useRouter(); // Using the router from the context will enable navigation control for unsaved changes
  const { handleNavigation } = context;

  // Return both the original context AND a protected router
  return {
    ...context,
    router: {
      push: (href: string, options?: any) => {
        handleNavigation(() => router.push(href as Route, options));
      },
      replace: (href: string, options?: any) => {
        handleNavigation(() => router.replace(href as Route, options));
      },
      back: () => {
        handleNavigation(() => router.back());
      },
      forward: () => {
        handleNavigation(() => router.forward());
      },
      refresh: () => {
        handleNavigation(() => router.refresh());
      },
      prefetch: router.prefetch,
    },
  };
};
