import Form from "next/form";
import {useActionState, useEffect, useState} from "react";
import {editOrganizationAction} from "../actions";
import {Button, Input} from "@gazelle/gazelle-component-ui";
import {useTranslation} from "react-i18next";
import {toast} from "react-toastify";
import {useUnsavedChanges} from "@/shared/context/UnsavedChangeContext";
import {validateOrganizationName, validateShortName} from "@/app/user-management/utils/validation";
import {Organization} from "../../user-management/Types";
import NoticeBanner from "@/shared/components/banner/NoticeBanner";
import {mutate} from "swr";

const initialState = {
  success: false,
  message: "",
};

interface EditOrganizationFormProps {
  organizationId: string;
  organizationData: { data: Organization };
}

const EditOrganizationForm = ({ organizationId, organizationData }: Readonly<EditOrganizationFormProps>) => {
  const { t } = useTranslation();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const [state, formAction, pending] = useActionState(editOrganizationAction, initialState);
  const [name, setName] = useState<string>(organizationData?.data?.name ?? "");
  const [shortName, setShortName] = useState<string>(organizationData?.data?.shortname ?? "");

  // Check if there are any changes from the original data
  const hasChanges = name !== (organizationData?.data?.name ?? "") || shortName !== (organizationData?.data?.shortname ?? "");

  // Track unsaved changes
  useEffect(() => {
    setHasUnsavedChanges(hasChanges);
  }, [hasChanges, setHasUnsavedChanges]);

  // Invalidate SWR cache when organization is successfully updated
  useEffect(() => {
    if (state?.message && !pending) {
      if (state?.success) {
        // Invalidate organization list cache
        mutate(
          (key) =>
            typeof key === "string" &&
            key.includes("/gazelle/api/items") &&
            key.includes("type=organizations") &&
            key.includes("path=%2Forganizations"),
          undefined,
          { revalidate: true },
        );
        // Also invalidate the specific organization's cache
        mutate((key) => Array.isArray(key) && key[0] === `/gazelle/api/organizations/${organizationId}`);
        setHasUnsavedChanges(false);
        toast.success(t("gzl.user.interface.organization_updated"));
      } else {
        toast.error(state?.message);
      }
    }
  }, [state, pending, organizationId, t, setHasUnsavedChanges]);

  // Validation: form is valid + has changes
  const isFormValid = name && shortName && validateOrganizationName(name) && validateShortName(shortName);
  const isSaveDisabled = pending || !isFormValid || !hasChanges;

  return (
    <Form className="flex flex-col gap-4 mr-auto" action={formAction}>
      <input type="hidden" name="organizationId" value={organizationId} />

      <Input
        id="name"
        type="text"
        label={t("gzl.user.interface.name") + "*"}
        htmlFor="name"
        name="name"
        placeholder={t("gzl.user.interface.name")}
        value={name}
        setValue={setName}
        required
        isValidInput={validateOrganizationName(name)}
        validationMessage={t("gzl.user.interface.invalid_name")}
      />

      <Input
        id="shortName"
        type="text"
        label={t("gzl.user.interface.short_name") + "*"}
        htmlFor="shortName"
        name="shortName"
        placeholder={t("gzl.user.interface.short_name")}
        value={shortName}
        setValue={setShortName}
        readonly
        disabled
      />
      <p>{t("gzl.user.interface.organization_short_name_info")}</p>

      <p className="italic">{t("gzl.user.interface.mandatory_inputs")}</p>

      <div className="flex justify-center w-full">
        <Button id="save-organization" type="submit" disabled={isSaveDisabled} variant="primary">
          {pending ? t("gzl.user.interface.saving") : t("gzl.user.interface.save")}
        </Button>
      </div>

      {state?.message &&
        (state.success ? <NoticeBanner color="green">{state.message}</NoticeBanner> : <NoticeBanner color="red">{state.message}</NoticeBanner>)}
    </Form>
  );
};

export default EditOrganizationForm;
