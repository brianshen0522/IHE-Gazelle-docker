"use client";
import Image from "next/image";
import Unauthorized from "@shared/assets/unauthorized.png";
import { useTranslation } from "react-i18next";

const UnauthorizedPage = () => {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col items-center justify-center w-full gap-4">
      <h1 className="text-xl font-semibold">{t("gzl.user.interface.unauthorized")}</h1>
      <Image src={Unauthorized} alt="500" width={400} height={400} />
      <p className="text-red">{t("gzl.user.interface.you_do_not_have_permission_to_access_this_page")}</p>
      <a href="/gazelle/home" className="hover:underline hover:text-visited_link">
        {t("gzl.user.interface.back_to_home")}
      </a>
    </div>
  );
};

export default UnauthorizedPage;
