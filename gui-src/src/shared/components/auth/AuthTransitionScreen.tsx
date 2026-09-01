"use client";

import { Loader2 } from "lucide-react";
import Image from "next/image";
import GazelleLogo from "../../assets/gz_logo.svg";
import { useTranslation } from "react-i18next";

const AuthTransitionScreen = () => {
  const { t } = useTranslation();
  return (
    <div className="h-screen flex flex-col items-center justify-center transition-colors duration-300">
      <Image
        src={GazelleLogo}
        alt="Gazelle icon"
        priority={true}
        sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
        style={{
          width: "7rem",
          height: "auto",
        }}
        width={112}
        height={50}
      />
      <p className="text-lg mb-2">{t("gzl.user.interface.checking_authentication")}…</p>
      <Loader2 className="animate-spin text-purple w-6 h-6" />
    </div>
  );
};

export default AuthTransitionScreen;
