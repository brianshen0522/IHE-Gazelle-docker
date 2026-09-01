"use client";

import { Breadcrumbs } from "@gazelle/gazelle-component-ui";
import React from "react";
import { useBreadCrumbsContext } from "@shared/components/breadcrumbs/BreadcrumbsContext";

export const OptionalBreadcrumbs = () => {
  const { breadCrumbsProps, breadCrumbsItems } = useBreadCrumbsContext();

  if (!breadCrumbsProps?.items && !breadCrumbsItems) {
    return null;
  }

  return (
    <div className="m-2">
      <Breadcrumbs
        id={breadCrumbsProps?.id ?? "breadcrumbs"}
        ariaLabel={breadCrumbsProps?.ariaLabel ?? "breadcrumb"}
        items={breadCrumbsProps?.items ?? breadCrumbsItems!}
        separator={breadCrumbsProps?.separator ?? ">"}
        className={breadCrumbsProps?.className ?? "text-blue"}
      />
    </div>
  );
};
