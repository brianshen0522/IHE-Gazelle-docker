"use client";
import { ContentHeader, formatSegmentLabel } from "@gazelle/gazelle-component-ui";
import { usePathname, useRouter } from "next/navigation";
import { JSX, PropsWithChildren, useMemo } from "react";
import { Route } from "next";
import { useTranslation } from "react-i18next";
import { getDefaultListPage, isDynamicSegment, isExcludedSegment } from "@/shared/utils/breadcrumbsBuilder";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";
import SignInAutoRedirectWrapper from "@auth/SignInAutoRedirectWrapper";

interface ContentHeaderWrapperProps {
  id: string;
  title?: string | JSX.Element;
  onGoBack?: () => void;
  enableAutoGoBack?: boolean; // Enable automatic go back using prevUrl from localStorage
  breadcrumbs?: Array<{ label: string; url: string }>; // Optional manual breadcrumbs override
  secured?: boolean; // Whether the page requires authentication
}

const ContentHeaderWrapper = ({
  id,
  title,
  onGoBack,
  enableAutoGoBack = false,
  breadcrumbs: customBreadcrumbs,
  secured = false,
  children,
}: PropsWithChildren<ContentHeaderWrapperProps>) => {
  const { t } = useTranslation();
  const pathname = usePathname();
  const router = useRouter();
  const { handleNavigation } = useUnsavedChanges();

  const handleAutoGoBack = useMemo(() => {
    if (!enableAutoGoBack) return undefined;

    return () => {
      const prevUrl = globalThis.window ? globalThis.localStorage.getItem("prevUrl") : null;
      if (prevUrl) {
        handleNavigation(() => router.push(prevUrl as Route));
      } else {
        handleNavigation(() => router.back());
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enableAutoGoBack, router]);

  // Use custom onGoBack if provided, otherwise use auto go back if enabled
  const goBackHandler = onGoBack || handleAutoGoBack;

  const autoBreadcrumbs = useMemo(() => {
    const crumbs = [
      {
        label: t('gzl.user.interface.home'),
        url: "/home",
        key: "root",
      },
    ];

    if (!pathname || pathname === "/" || pathname === "/home") {
      return crumbs;
    }

    // Split path into segments and filter out empty strings and dynamic segments
    const allSegments = pathname.split("/").filter(Boolean);
    const staticSegments = allSegments.filter((segment, index) => !isDynamicSegment(segment) && !isExcludedSegment(segment, allSegments, index));

    // Build breadcrumbs from path segments
    staticSegments.forEach((segment, index) => {
      const isLast = index === staticSegments.length - 1;
      const label = formatSegmentLabel(segment);
      const pathUpToHere = "/" + staticSegments.slice(0, index + 1).join("/");

      if (isLast) {
        crumbs.push({
          label: t(['gzl.user.interface.'+label.toLowerCase().replaceAll(" ", "_"), label]),
          url: "",
          key: pathUpToHere || `segment-${index}`,
        });
      } else {
        // For intermediate segments, get the default list page
        const url = getDefaultListPage(pathUpToHere.replace(/^\//, ""), allSegments);
        crumbs.push({
          label: t(['gzl.user.interface.'+label.toLowerCase().replaceAll(" ", "_"), label]),
          url,
          key: pathUpToHere,
        });
      }
    });

    return crumbs;
  }, [pathname]);

  // Use custom breadcrumbs if provided, otherwise use auto-generated ones
  const breadcrumbs = customBreadcrumbs || autoBreadcrumbs;
  const content = enableAutoGoBack ? t("gzl.user.interface.go_back") : "";

  const headerContent = (
    <>
      <ContentHeader id={id} title={title} content={content} breadcrumbs={breadcrumbs} onGoBack={goBackHandler} isGoBack={enableAutoGoBack} />
      {children}
    </>
  );

  return secured ? <SignInAutoRedirectWrapper>{headerContent}</SignInAutoRedirectWrapper> : headerContent;
};

export default ContentHeaderWrapper;
