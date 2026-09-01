"use client";
import { getRoleDisplayFromGroups } from "@/app/user-management/utils/roleMappers";

interface RolesCellProps {
  groupIds: string[];
  t: (key: string) => string;
}

export const RolesCell = ({ groupIds, t }: RolesCellProps) => {
  const displayRoles = getRoleDisplayFromGroups(groupIds, t);
  return (
    <span className="italic" title={displayRoles}>
      {displayRoles}
    </span>
  );
};
