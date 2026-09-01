import Form from "next/form";
import { useActionState, useEffect, useState } from "react";
import { createOrganizationAction } from "../actions";
import { Button, NoticeBanner } from "@gazelle/gazelle-component-ui";
import { validateShortName, validateOrganizationName } from "@/app/user-management/utils/validation";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { useUnsavedChanges } from "@/shared/context/UnsavedChangeContext";
import OrganizationFormFields from "./OrganizationFormFields";

const initialState = {
  success: false,
  message: "",
};

const CreateOrganizationForm = () => {
  const { t } = useTranslation();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const [state, formAction, pending] = useActionState(createOrganizationAction, initialState);
  const [name, setName] = useState<string>("");
  const [shortName, setShortName] = useState<string>("");
  const [showMessage, setShowMessage] = useState<boolean>(true);

  useEffect(() => {
    if (state?.message && !pending) {
      if (state?.success) {
        setHasUnsavedChanges(false);
        toast.success(t("gzl.user.interface.organization_created"));
      } else {
        toast.error(state?.message);
      }
      setShowMessage(true);
    }
  }, [state, pending, t, setHasUnsavedChanges]);

  const handleInputChange = (setter: (value: string) => void) => (value: string) => {
    setter(value);
    setHasUnsavedChanges(true);
    if (state?.message) {
      setShowMessage(false);
    }
  };

  return (
    <Form className="flex flex-col gap-4 mr-auto" action={formAction}>
      <OrganizationFormFields name={name} setName={handleInputChange(setName)} shortName={shortName} setShortName={handleInputChange(setShortName)} />

      <p className="italic">{t("gzl.user.interface.mandatory_inputs")}</p>

      <div className="flex justify-center w-full">
        <Button
          id="save-organization"
          type="submit"
          disabled={Boolean(pending || !name || !shortName || !validateShortName(shortName) || !validateOrganizationName(name))}
          variant="primary"
        >
          {pending ? t("gzl.user.interface.creating_organization") : t("gzl.user.interface.create_organization")}
        </Button>
      </div>

      {state?.message &&
        showMessage &&
        (state.success ? <NoticeBanner color="green">{state.message}</NoticeBanner> : <NoticeBanner color="red">{state.message}</NoticeBanner>)}
    </Form>
  );
};

export default CreateOrganizationForm;
