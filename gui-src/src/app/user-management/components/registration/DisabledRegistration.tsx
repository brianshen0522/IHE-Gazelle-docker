import Link from "next/link";
import { useTranslation } from "react-i18next";
import { AlertCircle } from "lucide-react";

const DisabledRegistration = () => {
  const { t } = useTranslation();
  return (
    <div className="flex flex-col w-full md:w-[750px] overflow-hidden shrink-0 gap-12 md:px-20 p-2 py-8" data-cy="disabled-registration">
      <div className="w-full border border-orange flex flex-col justify-center rounded-2xl gap-4 overflow-hidden">
        <h3 className="bg-orange flex justify-center text-white p-2">{t("gzl.gum.disabled_registration_title")}</h3>

        <AlertCircle className="text-orange mx-auto mt-2 size-12" />

        <p className="px-4 pb-5 text-center text-lg text-orange">{t("gzl.gum.disabled_registration")}</p>
      </div>
      <footer className="w-full flex justify-center items-center">
        <Link
          href={"/home"}
          className="border border-blue bg-blue hover:bg-white hover:text-blue transition duration-300 rounded-md p-2 text-white flex justify-center items-center"
        >
          {t("gzl.gum.home")}
        </Link>
      </footer>
    </div>
  );
};

export default DisabledRegistration;
