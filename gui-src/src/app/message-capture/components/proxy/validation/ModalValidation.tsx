import { useEffect, useState } from "react";
import { Button, Modal, SelectInput } from "@gazelle/gazelle-component-ui";
import { CircleCheckBig } from "lucide-react";
import { useSearchParams } from "next/navigation";
import { toast } from "react-toastify";
import { useGetValidationProfiles } from "@message-capture/hooks/swr-requests/useGetValidationProfiles";
import { usePostValidation } from "@message-capture/hooks/swr-requests/usePostValidateItem";
import { useGetValidationLogs } from "@message-capture/hooks/swr-requests/useGetValidationLogs";
import {
  ModalValidationProps,
  OptionType,
  ValidationPart,
  ValidationRequest,
  ProfileItem,
} from "@/app/message-capture/components/proxy/validation/Types";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";

const ModalValidation = ({ title, btnTriggerText, validationParts, partId, attachmentId, onValidationError }: ModalValidationProps) => {
  const { t } = useTranslation();
  const { data: session } = useSession();
  const { data, isError, isLoading } = useGetValidationProfiles();
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const validationItemId = partId ?? id;
  const { validateItem } = usePostValidation(session);
  const { mutate: mutateLogs } = useGetValidationLogs(validationItemId as string);
  const [isOpenModal, setIsOpenModal] = useState(false);
  const [selectedOption, setSelectedOption] = useState<OptionType | null>(null);
  const [selectedValidationPart, setSelectedValidationPart] = useState<ValidationPart | null>(null);
  const [options, setOptions] = useState<OptionType[]>([]);

  // This useEffect is used to pre-select the options if there is only one option
  useEffect(() => {
    if (isOpenModal && validationParts?.length === 1) {
      setSelectedValidationPart(validationParts[0]);
    } else if (isOpenModal && options?.length === 1) {
      setSelectedOption(options[0]);
    }
  }, [isOpenModal, options, validationParts]);

  useEffect(() => {
    if (data && data.length > 0) {
      setOptions(
        data
          .filter((item: { serviceName: string | null | undefined }) => item.serviceName)
          .map((profile: ProfileItem) => ({
            value: JSON.stringify({ profileID: profile.validator?.keyword, serviceName: profile.serviceName }),
            label: `${profile?.validator?.name} - ${profile.serviceName}`,
          })),
      );
    }
  }, [data]);

  const handleValidationSubmit = async () => {
    if (selectedValidationPart === null || selectedOption === null) {
      throw new Error("Invalid validation part or validation profile");
    }
    const validationRequest = createValidationRequest(validationItemId, selectedOption, selectedValidationPart);
    try {
      await validateItem(validationRequest);
      toast.success(t("gzl.message.capture.the_validation_process_is_complete") + ".");
      onValidationError("");
    } catch (error) {
      toast.error(t("gzl.message.capture.the_validation_process_has_failed") + ".");
      error instanceof Error && onValidationError(error.message);
    }
    mutateLogs();
  };

  const createValidationRequest = (itemId: string | null, profile: OptionType, validationPart: ValidationPart): ValidationRequest => {
    if (itemId === null) {
      throw new Error("Invalid itemId");
    }

    const { profileID, serviceName } = JSON.parse(profile.value) as { profileID: string; serviceName: string };
    return {
      itemId: itemId,
      validator: { keyword: profileID ?? "" },
      serviceName: serviceName,
      contentPath: validationPart.syntax === "attachment" ? (attachmentId as string) : validationPart.path,
      syntax: validationPart.syntax,
      selector: validationPart.selector,
    };
  };

  if (isLoading) return <div>Loading validation profiles...</div>;
  if (isError) return <div>Error while loading validation profiles...</div>;

  return (
    <Modal
      id="modal-validation"
      size="lg"
      title={title}
      isOpen={isOpenModal}
      toggleModal={() => setIsOpenModal(!isOpenModal)}
      trigger={
        <div>
          <Button
            id="open-validation-modal"
            type="button"
            title={t("gzl.message.capture.open_validation_modal")}
            ariaLabelledby="validate"
            onClick={() => setIsOpenModal(true)}
            variant="validation"
          >
            <CircleCheckBig size={14} />
            {btnTriggerText}
          </Button>
        </div>
      }
    >
      <div className="flex flex-col gap-4 w-full">
        {!partId && (
          <SelectInput<ValidationPart>
            id="validation-part"
            name={t("gzl.message.capture.message_part")}
            ariaLabel="Message part"
            ariaLabelledby="Message part"
            placeholder={t("gzl.message.capture.select_part_of_message_to_validate")}
            options={validationParts}
            isClearable={true}
            value={selectedValidationPart}
            handleChange={setSelectedValidationPart}
            noOptionsMessage="No parts available"
          />
        )}
        <SelectInput<OptionType>
          id="validation-profile"
          name="Validation profile"
          ariaLabel="Validation profile"
          ariaLabelledby="Validation profile"
          placeholder={t("gzl.message.capture.select_search_by_typing_validation_profile")}
          options={options}
          value={selectedOption}
          handleChange={setSelectedOption}
          isClearable={true}
          noOptionsMessage="No profiles available"
        />
        <div className="flex justify-end w-full">
          <Button
            id="validate"
            type="button"
            title={t("gzl.message.capture.validate")}
            ariaLabelledby="Validate"
            onClick={() => {
              handleValidationSubmit();
              setIsOpenModal(false);
            }}
            variant="primary"
            disabled={selectedOption === null || selectedValidationPart === null}
          >
            {t("gzl.message.capture.validate")}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default ModalValidation;
