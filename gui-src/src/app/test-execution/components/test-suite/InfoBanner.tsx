"use client";
import NoticeBanner from "@/shared/components/banner/NoticeBanner";
import { Info } from "lucide-react";
import Link from "next/link";
import { useTranslation } from "react-i18next";
import { Route } from "next";
import { useEnv } from "@hooks/useEnv";

const InfoBanner = () => {
  const { t } = useTranslation();
  const { env } = useEnv();
  const gzlTmUrl = env?.GZL_TM_URL ?? "/../tm";

  return (
    <NoticeBanner color="blue" className="flex flex-col sm:flex-row sm:items-center gap-2">
      <Info className="hidden md:flex" />
      <span>{t("gzl.texec.test_suite_info")}</span>
      <Link href={`${gzlTmUrl}/testing/test/testExecution.seam` as Route} className="underline hover:text-visited_link">
        {t("gzl.user.interface.gazelle_test_management")}
      </Link>
    </NoticeBanner>
  );
};

export default InfoBanner;
