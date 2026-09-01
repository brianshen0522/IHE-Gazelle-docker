"use client";
import { ValidationProfile } from "@validation-portal/types/ValidationProfile";
import { useTranslation } from "react-i18next";
import ProfileHistoryTable from "./ProfileHistoryTable";
import { InfoRow, SidePanel, TagSection } from "@gazelle/gazelle-component-ui";

interface ProfileDetailsProps {
  profile: ValidationProfile | undefined;
  validationService: string;
}

export default function ProfileDetails({ profile, validationService }: Readonly<ProfileDetailsProps>) {
  const { t } = useTranslation();

  if (!profile) {
    return null;
  }

  return (
    <>
      <SidePanel.Section id="profile-details" title={t("gzl.user.interface.title")}>
        <InfoRow label={t("gzl.user.interface.profile_id")} value={profile.profileID} />

        {profile.profileName && <InfoRow label={t("gzl.user.interface.profile_name")} value={profile.profileName} />}

        {profile.version && <InfoRow label={t("gzl.user.interface.profile_version")} value={profile.version} />}

        <InfoRow label={t("gzl.user.interface.provided_by")} value={validationService} />

        {profile.domain && <InfoRow label={t("gzl.user.interface.domain")} value={profile.domain} />}
        <TagSection items={profile.standards} labelKey={t("gzl.user.interface.standards")} keyPrefix="standard" />

        <TagSection items={profile.coveredItems} labelKey={t("gzl.user.interface.applies_on")} keyPrefix="covered" />

        <TagSection items={profile.tags} labelKey={t("gzl.user.interface.tags")} keyPrefix="detail-tag" />
      </SidePanel.Section>

      <SidePanel.Section id="profile-history" title={t("gzl.user.interface.history")}>
        <ProfileHistoryTable profileId={profile.profileID} />
      </SidePanel.Section>
    </>
  );
}
