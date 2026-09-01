"use client";
import { Session } from "next-auth";
import { ToastContainer } from "react-toastify";
import ContentHeaderWrapper from "@shared/components/layout/ContentHeaderWrapper";
import ValidationExecutionPage from "../components/validation-execution/ValidationExecutionPage";
import { useTranslation } from "react-i18next";

interface ValidatePageClientProps {
  profileId: string;
  serviceName: string;
  session: Session | null;
}

export default function ValidatePageClient({ profileId, serviceName, session }: Readonly<ValidatePageClientProps>) {
  const { t } = useTranslation();

  const breadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.user.interface.validation_portal"), url: "/validation-portal/profiles" },
    { label: t("gzl.user.interface.validate"), url: "" },
  ];

  return (
    <>
      <ContentHeaderWrapper id="validation-execute-header" title={t("gzl.user.interface.validate")} breadcrumbs={breadcrumbs} />
      <div className="px-6 py-4 w-full">
        <ValidationExecutionPage profileId={profileId} serviceName={serviceName} session={session} />
        <ToastContainer />
      </div>
    </>
  );
}

