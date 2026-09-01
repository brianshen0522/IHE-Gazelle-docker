"use client";
import { useState } from "react";
import Link from "next/link";
import { useTranslation } from "react-i18next";
import { CircleAlert, CircleCheck } from "lucide-react";
import { activateUser, ActivationResponse, ActivationData } from "./actions";
import { Button } from "@gazelle/gazelle-component-ui";

const UserActivation = ({ activationCode }: { activationCode: string }) => {
  const { t } = useTranslation();
  const [response, setResponse] = useState<ActivationResponse>();

  const activate = () => {
    activateUser(activationCode).then((response) => setResponse(response));
    return response;
  };

  function getMessageAboutResponse() {
    if (!response) return null;
    if (response.status === 200) {
      const activationData = response.data as ActivationData;
      return (
        <>
          <CircleCheck size={32} className="text-green" />
          <p className="text-green" data-cy="success-activation">
            {activationData.firstName} {activationData.lastName} {t("gzl.gum.has_been_activated_successfully")}
          </p>
        </>
      );
    }

    return (
      <>
        <CircleAlert size={32} className="text-red" />
        <p className="text-red" data-cy="failed-activation">
          {t("gzl.gum.account_activation_failure")}
        </p>
      </>
    );
  }

  return (
    <div className="flex flex-col border bg-white shadow border-lightpurple rounded-xl mx-36 my-16">
      <h3 className="bg-lightblue flex justify-center text-center text-black rounded-t-xl p-4">{t("gzl.gum.activate_account")}</h3>

      <div className="w-full flex flex-col items-center justify-center text-center p-4">
        <h4 className="pt-16 pb-2">{t("gzl.gum.click_to_activate_user")}</h4>
        <Button id="activate-button" type="button" variant="primary" onClick={activate} disabled={response !== undefined}>
          {t("gzl.gum.activate_account")}
        </Button>

        <div className="flex flex-row justify-center items-center align-middle mt-5 gap-2">{getMessageAboutResponse()}</div>
      </div>

      <div className="w-full flex justify-center mb-16">
        <Link
          href={"/home"}
          className="border-1 border-blue bg-white text-blue hover:bg-blue hover:text-white transition duration-300 rounded-md px-2 py-1"
        >
          {t("gzl.gum.home")}
        </Link>
      </div>
    </div>
  );
};

export default UserActivation;
