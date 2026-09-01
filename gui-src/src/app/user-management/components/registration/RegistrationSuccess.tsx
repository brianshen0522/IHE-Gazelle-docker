import { useTranslation } from "react-i18next";
import { CircleCheck } from "lucide-react";
import { RegistrationResultProps } from "./types";
import Link from "next/link";
import {Route} from "next";

const RegistrationSuccess = ({ joinOrCreateOrg, selectedOrg }: Omit<RegistrationResultProps, "result">) => {
  const { t } = useTranslation();
  const name: string = selectedOrg?.name ?? "";

  return (
    <>
      <div id="cypressValidation" className="border border-lightpurple flex flex-col justify-center rounded-2xl gap-4">
        <h3 className="text-black bg-lightblue flex justify-center rounded-t-2xl p-6">{t("gzl.gum.account_created")}</h3>

        <div className="flex justify-center items-center">
          <CircleCheck size={42} className="text-green" />
        </div>

        <div className="px-4 text-center text-2xl text-green">
          {joinOrCreateOrg === "CREATE" ? (
            <>
              <p>{t("gzl.gum.registration_request_received")}</p>
              <p>{t("gzl.gum.activation_link_sent")}</p>
              <p>{t("gzl.gum.account_activation_info")}</p>
            </>
          ) : (
            <>
              <p>{t("gzl.gum.registration_request_forwarded", { name })}</p>
              <p>{t("gzl.gum.account_activation_info")}</p>
            </>
          )}
        </div>
      </div>
      {joinOrCreateOrg === "CREATE" && <div className="flex text-center px-[20%]">{t("gzl.gum.activation_email_not_received")}</div>}
      <Link href={'/home' as Route} rel="noopener noreferrer"
        className="border-1 border-blue bg-white text-blue hover:bg-blue hover:text-white transition duration-300 rounded-md p-2"
      >
        {t("gzl.gum.home")}
      </Link>
    </>
  );
};

export default RegistrationSuccess;
