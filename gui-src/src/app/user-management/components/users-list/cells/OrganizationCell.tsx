"use client";
import { useGetOrganizationFromId } from "@/shared/hooks/useGetUserInformation";

interface OrganizationCellProps {
  organizationId: string;
  orgaMapping: Map<string, string>;
}

export const OrganizationCell = ({ organizationId, orgaMapping }: OrganizationCellProps) => {
  const orgaName = orgaMapping.get(organizationId);

  // If organization is in the mapping (old orga), use it directly
  // Otherwise, fetch it using the hook (new orga)
  const { data: orgaData } = useGetOrganizationFromId(orgaName ? "" : organizationId);

  const displayName = orgaName || orgaData?.data?.name || organizationId;
  return <span title={displayName}>{displayName}</span>;
};
