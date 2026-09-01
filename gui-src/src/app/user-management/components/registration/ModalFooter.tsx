import Link from "next/link";
import { useTranslation } from "react-i18next";
import { useIsValidStepContext } from "@/app/user-management/context/IsValidStepContext";
import { FullResponse } from "@/app/user-management/components/registration/types";
import { Button } from "@gazelle/gazelle-component-ui";

interface ModalFooterProps {
  step: number;
  goToPreviousStep: () => void;
  goToNextStep: () => void;
  result: FullResponse;
}

const ModalFooter = ({ step, goToPreviousStep, goToNextStep, result }: ModalFooterProps) => {
  const { isValidStep } = useIsValidStepContext();
  const { t } = useTranslation();

  const showPreviousButton = (step > 1 && step < 4) || (step === 4 && result.status === "error");
  const showNextButton = step < 3;
  const showConfirmButton = step === 3;
  const showHomeLink = step === 4 && result?.status === "error";

  return (
    <footer className="w-full flex items-center">
      {showPreviousButton && (
        <Button id={t("gzl.gum.previous")} type="button" variant="secondary" onClick={goToPreviousStep}>
          {t("gzl.gum.previous")}
        </Button>
      )}
      <div className="w-full flex justify-end">
        {showNextButton && (
          <Button id={t("gzl.gum.next")} type="button" variant="primary" onClick={goToNextStep} disabled={!isValidStep}>
            {t("gzl.gum.next")}
          </Button>
        )}
        {showConfirmButton && (
          <Button id={t("gzl.gum.confirm")} type="submit" variant="primary">
            {t("gzl.gum.confirm")}
          </Button>
        )}
        {showHomeLink && (
          <Link
            href="/home"
            className="border border-blue bg-blue hover:bg-white hover:text-blue transition duration-300 rounded-md p-2 text-white flex justify-center items-center"
          >
            {t("gzl.gum.home")}
          </Link>
        )}
      </div>
    </footer>
  );
};

export default ModalFooter;
