import { Button, Modal } from "@gazelle/gazelle-component-ui";
import { useState, useEffect } from "react";
import RenderSanitizedHTML from "@shared/services/RenderSanitizedHTML";
import { InteractWithUser } from "@maestro/types/message/WebSocketMessage";
import CountdownTimer from "./CountdownTimer";
import { useTranslation } from "react-i18next";

interface UserInteractionModalProps {
  interactWithUser: InteractWithUser;
  onComplete?: () => void;
}

const UserInteractionModal = ({ interactWithUser, onComplete }: UserInteractionModalProps) => {
  const { t } = useTranslation();
  const [timeout, setTimeout] = useState(false);
  const [isOpen, setIsOpen] = useState(true);

  useEffect(() => {
    setIsOpen(true);
    setTimeout(false);
  }, [interactWithUser]);

  const handleTimeout = () => {
    setTimeout(true);
  };

  const handleContinue = () => {
    setIsOpen(false);
    onComplete?.();
  };

  const handleClose = () => {
    // Only allow closing the modal after timeout
    if (!timeout) {
      return;
    }
    setIsOpen(false);
    onComplete?.();
  };

  const modalTitle = (
    <div className="flex items-center justify-between w-full gap-4">
      <span className={timeout ? "text-red" : ""}>{timeout ? t("gzl.user.interface.execution_timed_out") : interactWithUser?.interactionTitle}</span>
      {!timeout && <CountdownTimer initialTimeout={interactWithUser.timeout} onTimeout={handleTimeout} />}
    </div>
  );

  return (
    <Modal id="user-interaction-modal" title={modalTitle} isOpen={isOpen} toggleModal={handleClose} size="md">
      <div className="flex flex-col items-center w-full">
        <div className={`mb-4 text-center ${timeout ? "text-red" : ""}`}>
          <RenderSanitizedHTML untrustedHTML={interactWithUser?.message} />
          {timeout && <p className="mt-2">{t("gzl.user.interface.please_click_continue_or_rerun")}</p>}
        </div>

        <Button id="continue-button" type="button" onClick={handleContinue} variant={timeout ? "danger" : "primary"}>
          {t("gzl.user.interface.continue")}
        </Button>
      </div>
    </Modal>
  );
};

export default UserInteractionModal;
