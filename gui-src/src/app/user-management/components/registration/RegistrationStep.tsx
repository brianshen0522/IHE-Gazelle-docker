import React from "react";
import { useTranslation } from "react-i18next";
import { Steps } from "@gazelle/gazelle-component-ui";

const RegistrationStep = ({ currentStep }: { currentStep: number }) => {
  const { t } = useTranslation();
  const registrationStepTitle: string[] = [
    t("gzl.gum.user_information"),
    t("gzl.gum.organization_information"),
    t("gzl.gum.summary_and_confirmation"),
  ];

  return <Steps stepsTitle={registrationStepTitle} currentStep={currentStep} />;
};

export default RegistrationStep;
