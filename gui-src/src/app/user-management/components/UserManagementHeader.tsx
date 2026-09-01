"use client";
import ContentHeaderWrapper from "@/shared/components/layout/ContentHeaderWrapper";
import UsersTabs from "./UsersTabs";
import { useTranslation } from "react-i18next";
import { WithSession } from "@/shared/types/session";

interface UserManagementHeaderProps extends WithSession {
  id: string;
  title: string;
  breadcrumbs?: Array<{ label: string; url: string }>;
}

const UserManagementHeader = ({ id, title, breadcrumbs, session }: UserManagementHeaderProps) => {
  const { t } = useTranslation();
  return (
    <div className="px-6">
      <ContentHeaderWrapper id={id} title={t(title)} breadcrumbs={breadcrumbs} />
      <UsersTabs session={session} />
    </div>
  );
};

export default UserManagementHeader;
