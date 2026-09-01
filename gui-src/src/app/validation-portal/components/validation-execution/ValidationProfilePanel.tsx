"use client";
import { ValidationProfile } from "@validation-portal/types/ValidationProfile";
import { Button, InfoRow, TagSection } from "@gazelle/gazelle-component-ui";
import { Route } from "next";
import { useRouter } from "next/navigation";
import { useTranslation } from "react-i18next";

interface ValidationProfilePanelProps {
  profile: ValidationProfile;
  serviceName: string;
}

export default function ValidationProfilePanel({ profile, serviceName }: Readonly<ValidationProfilePanelProps>) {
  const router = useRouter();
  const { t } = useTranslation();

  const handleChange = () => {
    router.push("/validation-portal/profiles" as Route);
  };

  return (
    <div className="space-y-4 p-2">
      <div className="flex items-center gap-8">
        <InfoRow label={t("gzl.user.interface.profile_name")} value={profile.profileName ?? profile.profileID} />
        <Button id="change-profile" variant="secondary" type="button" onClick={handleChange}>
          {t("gzl.user.interface.choose_another_profile")}
        </Button>
      </div>

      {profile.version && <InfoRow label={t("gzl.user.interface.profile_version")} value={profile.version} />}

      <InfoRow label={t("gzl.user.interface.provided_by")} value={serviceName} />

      {profile.domain && <InfoRow label={t("gzl.user.interface.domain")} value={profile.domain} />}
      {profile.standards && profile.standards.length > 0 && (
        <InfoRow label={t("gzl.user.interface.standards")} value={profile.standards.join(", ")} />
      )}

      {profile.coveredItems && profile.coveredItems.length > 0 && (
        <InfoRow label={t("gzl.user.interface.applies_on")} value={profile.coveredItems.join(", ")} />
      )}

      {profile.tags && profile.tags.length > 0 && (
        <TagSection items={profile.tags} labelKey={t("gzl.user.interface.tags")} keyPrefix="validation-profile-tag" />
      )}
    </div>
  );
}
