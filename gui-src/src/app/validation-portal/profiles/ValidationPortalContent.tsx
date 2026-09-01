"use client";
import { SidePanelProvider } from "@gazelle/gazelle-component-ui";
import { ToastContainer } from "react-toastify";
import ContentHeaderWrapper from "@shared/components/layout/ContentHeaderWrapper";
import ProfilesList from "../components/profiles-list/ProfilesList";
import ProfileFilters from "../components/profiles-list/ProfileFilters";

interface ValidationPortalContentProps {
  breadcrumbs: Array<{ label: string; url: string }>;
}

export default function ValidationPortalContent({ breadcrumbs }: Readonly<ValidationPortalContentProps>) {
  return (
    <SidePanelProvider>
      <div className="flex flex-col w-full p-2">
        <ContentHeaderWrapper id="validation-portal-header" title="Validation portal" breadcrumbs={breadcrumbs} />

        <ProfileFilters />
        <ProfilesList />
        <ToastContainer />
      </div>
    </SidePanelProvider>
  );
}
