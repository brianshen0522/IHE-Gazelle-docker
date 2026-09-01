"use client";
import { SidePanel, Skeleton, useSidePanel } from "@gazelle/gazelle-component-ui";
import { ValidationProfileResponse } from "@validation-portal/types/ValidationProfile";
import ProfileDetails from "./ProfileDetails";
import { useTranslation } from "react-i18next";
import { CirclePlay } from "lucide-react";
import { useSession } from "next-auth/react";
import { useGetValidationProfile } from "@validation-portal/hooks/SWR/useGetValidationProfile";

export default function ProfileSidePanel() {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, selectedRow: profileResponse } = useSidePanel<ValidationProfileResponse>();
  const { data: session } = useSession();
  const { data: fullProfile, isLoading } = useGetValidationProfile(
    profileResponse?.profile?.profileID ?? "",
    profileResponse?.validationService ?? "",
    session,
  );

  const handleClose = () => {
    setIsOpen(false);
  };

  const profile = fullProfile || profileResponse?.profile;

  const accessDetailsProps = {
    id: profile?.profileID,
    redirectLink: <>{t("gzl.validation_portal.new_validation")}</>,
    pathname: "/validation-portal/validate",
    query: {
      profileId: profile?.profileID ?? "",
      serviceName: profileResponse?.validationService ?? "",
    },
    icon: CirclePlay,
    iconSize: 16,
  };

  return (
    <SidePanel isOpen={isOpen} className="p-1">
      {profileResponse && profile ? (
        <>
          <SidePanel.Header accessDetailsProps={accessDetailsProps} onClose={handleClose} />
          {isLoading ? (
            <Skeleton className="w-full h-48" />
          ) : (
            <ProfileDetails profile={profile} validationService={profileResponse.validationService} />
          )}
        </>
      ) : null}
    </SidePanel>
  );
}
