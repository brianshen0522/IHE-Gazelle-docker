import { Badge, NoticeBanner, InfoRow } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";

interface OrganizationArchivedProps {
  name: string;
  shortName: string;
}

const OrganizationArchived = ({ name, shortName }: OrganizationArchivedProps) => {
  const { t } = useTranslation();
  return (
    <>
      <Badge id="archived-organization-badge" variant="warning">
        {t("gzl.user.interface.archived_organization")}
      </Badge>
      <NoticeBanner color="yellow">{t("gzl.user.interface.archived_organization_notice")}</NoticeBanner>
      <InfoRow label={t("gzl.user.interface.name")} value={name} />
      <InfoRow label={t("gzl.user.interface.short_name")} value={shortName} />
    </>
  );
};

export default OrganizationArchived;
