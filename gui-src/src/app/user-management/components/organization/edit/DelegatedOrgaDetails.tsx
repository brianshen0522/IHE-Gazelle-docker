import { NoticeBanner, InfoRow } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";

interface DelegatedOrgaDetailsProps {
  name: string;
  shortName: string;
}

const DelegatedOrgaDetails = ({ name, shortName }: DelegatedOrgaDetailsProps) => {
  const { t } = useTranslation();
  return (
    <section className="w-full flex flex-col gap-y-2">
      <div className="max-w-1/2 mr-auto">
        <NoticeBanner color="yellow">{t("gzl.gum.delegated_organization_info")}</NoticeBanner>
      </div>
      <InfoRow label={t("gzl.user.interface.name")} value={name} />
      <InfoRow label={t("gzl.user.interface.short_name")} value={shortName} />
    </section>
  );
};

export default DelegatedOrgaDetails;
