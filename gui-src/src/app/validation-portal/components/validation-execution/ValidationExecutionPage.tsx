"use client";
import { Session } from "next-auth";
import ValidationProfilePanel from "./ValidationProfilePanel";
import ValidationConfigurationPanel from "./ValidationConfigurationPanel";
import { Skeleton, CollapsableCard } from "@gazelle/gazelle-component-ui";
import { useGetValidationProfile } from "../../hooks/SWR/useGetValidationProfile";
import InternalErrors from "@/shared/components/errors/InternalError";
import { useTranslation } from "react-i18next";

interface ValidationExecutionPageProps {
  profileId: string;
  serviceName: string;
  session: Session | null;
}

export default function ValidationExecutionPage({ profileId, serviceName, session }: Readonly<ValidationExecutionPageProps>) {
  const { t } = useTranslation();
  const { data: profile, isLoading, isError } = useGetValidationProfile(profileId, serviceName, session);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-60" />
        <Skeleton className="h-60" />
      </div>
    );
  }

  if (isError || !profile) {
    return (
      <InternalErrors
        title={t("gzl.user.interface.error_loading_validation_profile")}
        message={t("gzl.user.interface.error_fetching_validation_profile")}
      />
    );
  }

  return (
    <div className="space-y-4">
      {/* Validation Profile Details */}
      <CollapsableCard title={t("gzl.user.interface.validation_profile")}>
        <ValidationProfilePanel profile={profile} serviceName={serviceName} />
      </CollapsableCard>

      {/* File Upload and Configuration */}
      <CollapsableCard title={t("gzl.user.interface.configuration")}>
        <ValidationConfigurationPanel profile={profile} serviceName={serviceName} profileId={profileId} session={session} />
      </CollapsableCard>
    </div>
  );
}
