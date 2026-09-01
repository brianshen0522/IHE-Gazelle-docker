"use client";
import { ToastContainer } from "react-toastify";
import { SidePanelProvider } from "@gazelle/gazelle-component-ui";
import ContentHeaderWrapper from "@shared/components/layout/ContentHeaderWrapper";
import ProfilesList from "../components/profiles-list/ProfilesList";
import ProfileFilters from "../components/profiles-list/ProfileFilters";
import { useTranslation } from "react-i18next";

export default function ProfilesPageClient() {
  const { t } = useTranslation();

  const breadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.user.interface.validation_portal"), url: "" },
  ];

  return (
    <SidePanelProvider>
      <div className="flex flex-col w-full p-2">
        <ContentHeaderWrapper id="validation-portal-header" title={t("gzl.user.interface.validation_portal")} breadcrumbs={breadcrumbs} />

        <ProfileFilters />
        <ProfilesList />
        <ToastContainer />
      </div>
    </SidePanelProvider>
  );
}

