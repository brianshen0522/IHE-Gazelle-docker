"use client";
import { useTranslation } from "react-i18next";

export default function MissingParamsClient() {
  const { t } = useTranslation();

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-red">{t("gzl.user.interface.missing_parameters")}</h1>
      <p>{t("gzl.user.interface.profile_id_and_service_name_required")}</p>
    </div>
  );
}

