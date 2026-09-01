"use client";
import { useTranslation } from "react-i18next";
import { WithSession } from "@/shared/types/session";
import UserManagementHeader from "../../UserManagementHeader";
import CreateOrganizationForm from "./CreateOrganizationForm";

const CreateOrganization = ({ session }: WithSession) => {
  const { t } = useTranslation();

  const breadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.user.interface.user_management"), url: "" },
    { label: t("gzl.user.interface.create_organization"), url: "" },
  ];

  return (
    <>
      <UserManagementHeader id="create-organization-header" title="gzl.user.interface.user_management" breadcrumbs={breadcrumbs} session={session} />
      <div className="flex flex-col gap-4 bg-white p-6 m-8 rounded-lg shadow-md">
        <h3 className="text-xl font-semibold">{t("gzl.user.interface.new_organization_details")}</h3>
        <CreateOrganizationForm />
      </div>
    </>
  );
};

export default CreateOrganization;
