"use client";
import { useState } from "react";
import { useSession } from "next-auth/react";
import useEnvironmentVar from "@message-capture/hooks/useEnvironmentVar";
import { currentItemType } from "@/app/message-capture/components/proxy/Types";
import { getMessageValidationParts } from "@message-capture/validation/MessageValidationPart";
import ModalValidation from "@message-capture/validation/ModalValidation";
import ValidationResult from "@message-capture/validation/ValidationResult";
import { Card } from "@gazelle/gazelle-component-ui";
import { ValidationWrapperProps } from "./Types";
import { useTranslation } from "react-i18next";

const ValidationWrapper = ({ messageItem }: ValidationWrapperProps) => {
  const { t } = useTranslation();
  const { data: session } = useSession();
  const { envGzlDthValidation } = useEnvironmentVar();
  const [validationError, setValidationError] = useState<string | null>(null);
  const attachmentId = messageItem.references.find((ref) => ref.refType === "ATTACHMENT")?.value as string;

  const getValidationParts = (item: currentItemType<{ content: any; type?: string }>, attachmentId?: string) => {
    return getMessageValidationParts(item.content, attachmentId).filter((part) => part.isEnable);
  };

  const handleValidationError = (error: string) => {
    setValidationError(error);
  };

  return (
    <>
      {envGzlDthValidation === "true" && (
        <Card id="message-validation" title={t("gzl.message.capture.validation")} className="w-1/3">
          {session?.access_token ? (
            <div className="flex flex-col">
              <ModalValidation
                title={t("gzl.message.capture.validate")}
                btnTriggerText="Validate"
                validationParts={getValidationParts(messageItem, attachmentId)}
                onValidationError={handleValidationError}
                attachmentId={attachmentId}
              />

              <ValidationResult validationError={validationError} />
            </div>
          ) : (
            <div>Log into Gazelle to access the validation feature</div>
          )}
        </Card>
      )}
    </>
  );
};

export default ValidationWrapper;
