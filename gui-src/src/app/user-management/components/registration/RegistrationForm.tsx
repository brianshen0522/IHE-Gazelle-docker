"use client";
import { useState } from "react";
import Form from "next/form";
import { useIsValidStepContext } from "@/app/user-management/context/IsValidStepContext";
import {
  FullResponse,
  type OrganizationInfos as OrganizationInfoType,
  RegistrationFormProps,
} from "@/app/user-management/components/registration/types";
import UserInformations from "@/app/user-management/components/registration/UserInformations";
import OrganizationInfos from "@/app/user-management/components/registration/OrganizationInfos";
import RegistrationValidation from "@/app/user-management/components/registration/RegistrationValidation";
import RegistrationResult from "@/app/user-management/components/registration/RegistrationResult";
import DisabledRegistration from "@/app/user-management/components/registration/DisabledRegistration";
import ModalFooter from "@/app/user-management/components/registration/ModalFooter";
import { registerUser } from "./actions";
import RegistrationStep from "./RegistrationStep";

export default function RegistrationForm({ configs, privacyPolicyUrl }: Readonly<RegistrationFormProps>) {
  const [userInfos, setUserInfos] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    passwordConfirmation: "",
  });
  const [joinOrCreateOrg, setJoinOrCreateOrg] = useState<"JOIN" | "CREATE">("JOIN");
  const [selectedOrg, setSelectedOrg] = useState<OrganizationInfoType | null>(null);
  const [organizationInfos, setOrganizationInfos] = useState<OrganizationInfoType>({
    name: "",
    shortname: "",
  });
  const acceptedTOS = true;
  const { isValidStep } = useIsValidStepContext();
  const [step, setStep] = useState<number>(1);
  const [result, setResult] = useState<FullResponse>();

  const handleSubmit = () => {
    if (isValidStep) {
      goToNextStep();
    } else {
      return;
    }
  };

  const goToNextStep = async () => {
    if (step === 3) {
      const data = await registerUser({ userInfos, organizationInfos, selectedOrg, joinOrCreateOrg, acceptedTOS });

      if (data) {
        if (data.status >= 400 && data.status <= 599) {
          setResult({ status: "error", response: data });
        } else {
          setResult({ status: "success", response: data });
        }
      }
      setStep(step + 1);
      return;
    }
    setStep(step + 1);
  };

  const goToPreviousStep = () => {
    setStep(step - 1);
  };

  if (!configs?.userRegistrationEnabled) return <DisabledRegistration />;

  return (
    <Form
      id="registration-form"
      className="bg-white flex flex-col w-full max-w-[750px] mx-auto shadow-lg rounded-lg overflow-hidden shrink-0 gap-10 mt-2 md:px-20 p-2 py-8"
      action={handleSubmit}
    >
      {step < 4 && <RegistrationStep currentStep={step} />}
      <div className="w-full h-full flex flex-col items-center gap-y-8 p-2" data-cy="main-content">
        {step === 1 && <UserInformations userInfos={userInfos} setUserInfos={setUserInfos} />}
        {step === 2 && (
          <OrganizationInfos
            creationEnabled={configs.organizationCreationEnabled}
            joinOrCreateOrg={joinOrCreateOrg}
            setJoinOrCreateOrg={setJoinOrCreateOrg}
            organizationInfos={organizationInfos}
            setOrganizationInfos={setOrganizationInfos}
            selectedOrg={selectedOrg}
            setSelectedOrg={setSelectedOrg}
          />
        )}
        {step === 3 && (
          <RegistrationValidation
            userInfos={userInfos}
            joinOrCreateOrg={joinOrCreateOrg}
            selectedOrg={selectedOrg}
            organizationInfos={organizationInfos}
            configs={configs}
          />
        )}
        {step === 4 && result && <RegistrationResult result={result} joinOrCreateOrg={joinOrCreateOrg} selectedOrg={selectedOrg} />}
      </div>
      <a
        href={privacyPolicyUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="text-blue hover:text-visited_link"
        title="Read our privacy policy"
      >
        Privacy policy
      </a>
      <ModalFooter step={step} goToNextStep={goToNextStep} goToPreviousStep={goToPreviousStep} result={result as FullResponse} />
    </Form>
  );
}
