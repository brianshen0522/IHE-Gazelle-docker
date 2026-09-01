"use client";
import GzLogo from "@/shared/assets/gz_logo.svg";
import AppLink from "@user-management/components/user-management/AppLink";
import { useTranslation } from "react-i18next";
import Image from "next/image";
import { useGetMetaData } from "@user-management/hooks/swr/useGetMetaData";

const UserHome = () => {
  const { t } = useTranslation();
  const { data } = useGetMetaData();

  return (
    <div className="flex flex-col items-center p-5 gap-8">
      <Image
        src={GzLogo}
        alt="Gazelle icon"
        priority={true}
        sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
        style={{
          width: "18rem",
          height: "auto",
        }}
      />

      <h1 className="text-xl p-4">{t("gzl.gum.welcome_management_module")}</h1>

      <div className="grid grid-cols-2 gap-4">
        <AppLink href="/user-management/users" className="p-8 text-md">
          {t("gzl.gum.manage_users")}
        </AppLink>
        <AppLink href="/user-management/registration" className="p-8 text-md">
          {t("gzl.gum.register_new_user")}
        </AppLink>
        <AppLink href="/user-management/account" className="p-8 text-md">
          {t("gzl.gum.manage_your_account")}
        </AppLink>
        <AppLink href="/user-management/organization/list" className="p-8 text-md">
          {t("gzl.gum.manage_your_organization")}
        </AppLink>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <AppLink href="../gazelle-documentation/User-Management/links.html">{t("gzl.gum.view_documentation")}</AppLink>
        <AppLink href="../gazelle-documentation/User-Management/release-note.html">
          {t("gzl.gum.view_release_notes")}
        </AppLink>
      </div>
      <div className="flex flex-col text-center">
        <span>
          {t("gzl.gum.front_end_version")}: {process.env.version}
        </span>
        <span>
          {t("gzl.gum.back_end_version")}: {data?.version}
        </span>
      </div>
    </div>
  );
};

export default UserHome;
