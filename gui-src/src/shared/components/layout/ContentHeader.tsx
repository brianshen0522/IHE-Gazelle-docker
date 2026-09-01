"use client"

import {BreadcrumbItem, BreadcrumbsProps} from "@gazelle/gazelle-component-ui";
import React, { JSX, PropsWithChildren, useEffect } from "react";
import {BreadCrumbsContextProvider, useBreadCrumbsContext} from "@shared/components/breadcrumbs/BreadcrumbsContext";
import {OptionalBreadcrumbs} from "@shared/components/breadcrumbs/OptionalBreadcrumbs";
import SignInAutoRedirectWrapper from "@auth/SignInAutoRedirectWrapper";

export type ContentHeaderProps = {
  breadcrumbsProps?: BreadcrumbsProps;
  breadcrumbsItems?: BreadcrumbItem[];
  title?: string | JSX.Element;
  secured?: boolean;
};

const ContentHeader = ({ breadcrumbsProps, breadcrumbsItems, title, secured = true, children }: PropsWithChildren<ContentHeaderProps>) => {
    const { setBreadCrumbsProps, setBreadCrumbsItems } = useBreadCrumbsContext();

  useEffect(() => {
    if (breadcrumbsProps) setBreadCrumbsProps(breadcrumbsProps);
  }, [breadcrumbsProps, setBreadCrumbsProps]);

  useEffect(() => {
    if (breadcrumbsItems) setBreadCrumbsItems(breadcrumbsItems);
  }, [breadcrumbsItems, setBreadCrumbsItems]);

    const content = (
        <>
            <OptionalBreadcrumbs />
            <div className="mx-4 my-2">
                {title && <h1 className="text-purple font-medium text-md">{title}</h1>}
                {children}
            </div>
        </>
    );

  return secured ? <SignInAutoRedirectWrapper>{content}</SignInAutoRedirectWrapper> : content;
};

const ContentHeaderWrapper = (props: PropsWithChildren<ContentHeaderProps>) => {
  return (
    <BreadCrumbsContextProvider>
      <ContentHeader {...props} />
    </BreadCrumbsContextProvider>
  );
};

export default ContentHeaderWrapper;
