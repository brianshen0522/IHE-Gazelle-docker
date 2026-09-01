"use client";
import { useTranslation } from "react-i18next";
import { Skeleton, NoticeBanner } from "@gazelle/gazelle-component-ui";
import { useGetOrganizationFromId } from "@/shared/hooks/useGetUserInformation";
import { WithSession } from "@/shared/types/session";
import DelegatedOrgaDetails from "./DelegatedOrgaDetails";
import UserManagementHeader from "../../UserManagementHeader";
import EditOrganizationForm from "./EditOrganizationForm";
import OrganizationArchived from "../archive/OrganizationArchived";

interface EditOrganizationProps extends WithSession {
  organizationId?: string;
  isFromSidepanel?: boolean;
}

const EditOrganization = ({ session, organizationId: propOrganizationId, isFromSidepanel = false }: Readonly<EditOrganizationProps>) => {
  const { t } = useTranslation();
  // Use provided organizationId if available, otherwise fall back to session's organization
  const organizationId = propOrganizationId ?? session?.user.organization ?? "";
  const { data: organizationData, isLoading, isError } = useGetOrganizationFromId(organizationId);
  const { name, shortname } = organizationData?.data ?? {};

  const breadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.user.interface.user_management"), url: "" },
    { label: t("gzl.user.interface.edit_my_organization"), url: "" },
  ];

  const isDelegated = organizationData?.data?.delegated ?? false;
  const isArchived = organizationData?.data?.archived ?? false;

  if (isLoading) return <Skeleton className="h-full m-6" />;

  if (isError) {
    return (
      <>
        {!isFromSidepanel && (
          <UserManagementHeader
            id="edit-organization-header"
            title="gzl.user.interface.user_management"
            breadcrumbs={breadcrumbs}
            session={session}
          />
        )}
        <div className={isFromSidepanel ? "flex flex-col gap-4" : "flex flex-col gap-4 bg-white p-6 m-8 rounded-lg shadow-md"}>
          <NoticeBanner color="red" className="mr-auto">
            {t("gzl.user.interface.error_loading_organization")}
          </NoticeBanner>
        </div>
      </>
    );
  }

  const renderOrganizationContent = () => {
    if (isDelegated) {
      return <DelegatedOrgaDetails name={name} shortName={shortname} />;
    }
    if (isArchived) {
      return <OrganizationArchived name={name} shortName={shortname} />;
    }
    return <EditOrganizationForm key={organizationId} organizationId={organizationId} organizationData={organizationData} />;
  };

  const formContent = <>{renderOrganizationContent()}</>;

  return (
    <>
      {!isFromSidepanel && (
        <UserManagementHeader id="edit-organization-header" title="gzl.user.interface.user_management" breadcrumbs={breadcrumbs} session={session} />
      )}
      {isFromSidepanel ? (
        <div className="flex flex-col gap-4">{formContent}</div>
      ) : (
        <div className="flex flex-col gap-4 bg-white p-6 m-8 rounded-lg shadow-md">
          <h3 className="text-xl font-semibold">{t("gzl.user.interface.organization_details")}</h3>
          {formContent}
        </div>
      )}
    </>
  );
};

export default EditOrganization;
