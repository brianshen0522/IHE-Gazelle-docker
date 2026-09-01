"use client";
import { useState } from "react";
import { SidePanel, useSidePanel, Badge, Button } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { Organization } from "../../user-management/Types";
import EditOrganization from "../edit/EditOrganization";
import { WithSession } from "@/shared/types/session";
import OrganizationArchiveModal from "../archive/OrganizationArchiveModal";
import { useGetOrganizationFromId } from "@/shared/hooks/useGetUserInformation";
import { isGazelleAdmin } from "@/shared/utils/permissions";
import OrganizationLinkedUsers from "./OrganizationLinkedUsers";
import { useGetHeaders } from "@/shared/hooks/SWR/useGetHeaders";
import { parseContentRange } from "@/shared/utils/parseContentRange";

export default function OrganizationSidepanel({ session }: Readonly<WithSession>) {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, selectedRow: organization } = useSidePanel<Organization>();
  const [isArchiveModalOpen, setIsArchiveModalOpen] = useState(false);

  const { data: organizationData } = useGetOrganizationFromId(organization?.id ?? "");

  // Fetch total users count using HEAD request
  const {
    headers: usersHeaders,
    isLoading: isLoadingUsers,
    error: errorUsers,
  } = useGetHeaders({
    searchParameters: { organizationId: organization?.id ?? "" },
    type: "users",
    path: "/v2/users",
    headersToExtract: ["content-range"],
  });

  // Fetch org admin count using HEAD request
  const {
    headers: orgAdminsHeaders,
    isLoading: isLoadingOrgAdmins,
    error: errorOrgAdmins,
  } = useGetHeaders({
    searchParameters: { organizationId: organization?.id ?? "", group: "org-adm" },
    type: "users",
    path: "/v2/users",
    headersToExtract: ["content-range"],
  });

  const isErrorUsers = !!errorUsers || !!errorOrgAdmins;

  const registeredUsersCount = parseContentRange(usersHeaders?.["content-range"] ?? null)?.total ?? 0;
  const orgAdminCount = parseContentRange(orgAdminsHeaders?.["content-range"] ?? null)?.total ?? 0;

  const isArchived = organizationData?.data?.archived ?? false;
  const isDelegated = organizationData?.data?.delegated ?? false;
  const organizationId = organizationData?.data?.id ?? "";

  const handleClose = () => {
    setIsOpen(false);
  };

  return (
    <SidePanel isOpen={isOpen} className="p-1">
      {organization ? (
        <>
          <SidePanel.Header onClose={handleClose} />

          {isDelegated && (
            <Badge id="delegated-organization-badge" variant="variant-2">
              {t("gzl.user.interface.delegated_organization")}
            </Badge>
          )}
          <SidePanel.Section id="organization-details" title={t("gzl.user.interface.organization")}>
            <EditOrganization organizationId={organization.id} isFromSidepanel={true} session={session} />
          </SidePanel.Section>

          {registeredUsersCount > 0 && (
            <SidePanel.Section id="linked-users" title={t("gzl.user.interface.linked_users")}>
              <OrganizationLinkedUsers
                organizationId={organizationId}
                registeredUsersCount={registeredUsersCount}
                orgAdminCount={orgAdminCount}
                isLoading={isLoadingUsers || isLoadingOrgAdmins}
                isError={isErrorUsers}
              />
            </SidePanel.Section>
          )}

          {!isArchived && !isDelegated && isGazelleAdmin(session) && (
            <SidePanel.Section id="security" title={t("gzl.user.interface.security")}>
              {t("gzl.user.interface.archive_organization_info")}

              <div className="flex justify-end">
                <Button id="archive-organization" type="button" variant="danger-1" onClick={() => setIsArchiveModalOpen(true)}>
                  {t("gzl.user.interface.archive_organization")}
                </Button>
              </div>
              <OrganizationArchiveModal
                organization={organization}
                isOpen={isArchiveModalOpen}
                toggleModal={() => setIsArchiveModalOpen(!isArchiveModalOpen)}
              />
            </SidePanel.Section>
          )}
        </>
      ) : null}
    </SidePanel>
  );
}
