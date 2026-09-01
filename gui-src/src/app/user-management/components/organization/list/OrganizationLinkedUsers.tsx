import { Skeleton } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { ExternalLink } from "lucide-react";
import Link from "next/link";
import { Route } from "next";

interface OrganizationLinkedUsersProps {
  organizationId: string;
  registeredUsersCount: number;
  orgAdminCount: number;
  isLoading: boolean;
  isError: boolean;
}

const OrgLinkedUsersLink = ({ href, count, label }: { href: string; count: number; label: string }) => (
  <Link href={href as Route} className="flex items-center gap-1 text-blue hover:text-visited_link underline">
    {count} {label.toLowerCase()} <ExternalLink />
  </Link>
);

const OrganizationLinkedUsers = ({ organizationId, registeredUsersCount, orgAdminCount, isLoading, isError }: OrganizationLinkedUsersProps) => {
  const { t } = useTranslation();

  const registeredUsers = t("gzl.user.interface.registered_users");
  const organizationAdministrators = t("gzl.user.interface.organization_administrators");

  if (isLoading) return <Skeleton className="h-4 w-1/2" />;
  if (isError) return <p className="text-red">{t("gzl.user.interface.error_loading_linked_users")}</p>;

  return (
    <>
      <OrgLinkedUsersLink
        href={`/user-management/users?organizationId=${encodeURIComponent(organizationId)}`}
        count={registeredUsersCount}
        label={registeredUsers}
      />
      {orgAdminCount > 0 && (
        <OrgLinkedUsersLink
          href={`/user-management/users?organizationId=${encodeURIComponent(organizationId)}&group=org-adm`}
          count={orgAdminCount}
          label={organizationAdministrators}
        />
      )}
    </>
  );
};

export default OrganizationLinkedUsers;
