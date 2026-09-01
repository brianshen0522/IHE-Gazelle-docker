"use client";
import { useTranslation } from "react-i18next";
import { WithSession } from "@/shared/types/session";
import UserManagementHeader from "../../UserManagementHeader";
import CreateUserForm from "./CreateUserForm";
import { ToastContainer } from "react-toastify";

const CreateUser = ({ session }: WithSession) => {
  const { t } = useTranslation();

  const breadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.user.interface.user_management"), url: "" },
    { label: t("gzl.user.interface.create_user"), url: "" },
  ];

  return (
    <>
      <UserManagementHeader id="create-user-header" title="gzl.user.interface.user_management" breadcrumbs={breadcrumbs} session={session} />
      <div className="flex flex-col gap-4 bg-white p-6 m-8 rounded-lg shadow-md">
        <CreateUserForm session={session} />
      </div>
      <ToastContainer />
    </>
  );
};

export default CreateUser;
