import React, { useId } from "react";
import { Button, type ButtonProps, Modal } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";

interface ConfirmModalProps {
  title: string;
  isOpen: boolean;
  onCancel: () => void;
  onContinue: () => void;
  toggleModal: () => void;
  textOnCancel?: string;
  textOnContinue: string;
  children: React.ReactNode;
  confirmVariant?: ButtonProps["variant"];
  disableConfirm?: boolean;
}

const ConfirmModal = ({
  title,
  isOpen,
  onCancel,
  onContinue,
  toggleModal,
  textOnCancel,
  textOnContinue,
  children,
  confirmVariant = "primary",
  disableConfirm = false,
}: ConfirmModalProps) => {
  const id = useId();
  const { t } = useTranslation();

  return (
    <Modal id={id} size={"md"} title={title} isOpen={isOpen} toggleModal={toggleModal}>
      <div className="flex flex-col w-full">
        {children}
        <div className="flex gap-4 justify-end pt-6">
          {textOnCancel && (
            <Button id={id} variant="default" title={t(textOnCancel)} ariaLabel="stop-editing" type="button" onClick={onCancel}>
              <span>{t(textOnCancel)}</span>
            </Button>
          )}
          <Button
            id={id}
            variant={confirmVariant}
            title={t(textOnContinue)}
            ariaLabel="leave-edition"
            type="button"
            onClick={onContinue}
            disabled={disableConfirm}
          >
            <div>{t(textOnContinue)}</div>
          </Button>
        </div>
      </div>
    </Modal>
  );
};
export default ConfirmModal;
