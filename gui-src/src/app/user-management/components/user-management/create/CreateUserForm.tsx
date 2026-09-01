"use client";
import React, { useActionState, useEffect, useState } from "react";
import { Button, Input, SelectInput, NoticeBanner, ToggleSwitch } from "@gazelle/gazelle-component-ui";
import { Info } from "lucide-react";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { useUnsavedChanges } from "@/shared/context/UnsavedChangeContext";
import { isOnlyOrgaAdmin } from "@user-management/utils/permissions";
import { Option } from "@user-management/hooks/useFilterOptions";
import { validateEmail, validateUserNames, validateShortName } from "@user-management/utils/validation";
import { useGetOrganizations } from "@/app/user-management/hooks/swr/useGetGroups";
import { useGumConfig } from "@user-management/hooks/useGumConfig";
import { useGetOrganizationFromId } from "@/shared/hooks/useGetUserInformation";
import { createUser } from "../actions";
import Form from "next/form";
import { mutate } from "swr";
import OrganizationFormFields from "../../organization/create/OrganizationFormFields";
import { WithSession } from "@/shared/types/session";
import { useDebounce } from "@/shared/hooks/useDebounce";

type OrgaOptionProps = {
  value: string;
  label: string;
};

const initialState = {
  success: false,
  message: "",
};

const CreateUserForm = ({ session }: WithSession) => {
  const { t } = useTranslation();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const [state, formAction, pending] = useActionState(createUser, initialState);
  const [orgaSearchInput, setOrgaSearchInput] = useState("");
  const debouncedOrgaSearch = useDebounce(orgaSearchInput, 300);

  // For org admins, fetch only their specific organization
  const isOrgAdmin = isOnlyOrgaAdmin(session);
  const orgAdminOrgId = isOrgAdmin ? session?.user.organization : "";

  const { data: orgAdminOrgData, isLoading: orgAdminOrgLoading } = useGetOrganizationFromId(orgAdminOrgId || "");
  const { data: allOrgsData, isLoading: allOrgsLoading } = useGetOrganizations({
    search: debouncedOrgaSearch,
    archived: false,
  });

  const organizationsOptions = React.useMemo(() => {
    if (isOrgAdmin) {
      // For org admins, use only their organization
      if (!orgAdminOrgData?.data) return undefined;
      return [
        {
          value: orgAdminOrgData.data.id,
          label: orgAdminOrgData.data.name,
        },
      ];
    }

    // For other users, use the full list
    return allOrgsData?.data?.map(
      (orga: Record<string, string>): OrgaOptionProps => ({
        value: orga.id,
        label: orga.name,
      }),
    );
  }, [isOrgAdmin, orgAdminOrgData, allOrgsData]);

  const orgaLoading = isOrgAdmin ? orgAdminOrgLoading : allOrgsLoading;

  const { data } = useGumConfig();
  const userCreationEmailNotificationEnabled = data?.data?.userCreationEmailNotificationEnabled;

  const [newUserFirstName, setNewUserFirstName] = useState<string | undefined>(undefined);
  const [newUserLastName, setNewUserLastName] = useState<string | undefined>(undefined);
  const [newUserEmail, setNewUserEmail] = useState<string | undefined>(undefined);
  const [newUserOrgaOption, setNewUserOrgaOption] = useState<Option | undefined>(undefined);
  const [showOrgaCreation, setShowOrgaCreation] = useState(false);

  // Organization fields
  const [orgaName, setOrgaName] = useState<string>("");
  const [orgaShortName, setOrgaShortName] = useState<string>("");

  // Computed organization value: for org admins, use their organization directly
  const selectedOrgaOption = isOrgAdmin && organizationsOptions?.[0] ? (organizationsOptions[0] as Option) : newUserOrgaOption;

  useEffect(() => {
    if (state?.message && !pending) {
      if (state?.message.includes("success")) {
        mutate((key) => typeof key === "string" && key.startsWith("/gazelle/user-management/api/users"));
        setHasUnsavedChanges(false);
        toast.success(t("gzl.gum.account_created"));
      } else {
        toast.error(state?.message);
      }
    }
  }, [state, pending, t, setHasUnsavedChanges]);

  const isValidCreation =
    newUserFirstName &&
    newUserLastName &&
    newUserEmail &&
    validateUserNames(newUserFirstName) &&
    validateUserNames(newUserLastName) &&
    validateEmail(newUserEmail) &&
    (showOrgaCreation
      ? // When creating a new org, validate org fields (website is required)
        orgaName && orgaShortName && validateShortName(orgaShortName)
      : // When selecting existing org, require selection
        selectedOrgaOption !== undefined);

  return (
    <Form className="flex flex-col space-y-4 mr-auto" action={formAction}>
      <Input
        id={t("gzl.gum.first_name")}
        key="firstName"
        type="text"
        label={t("gzl.gum.first_name") + "*"}
        htmlFor="firstName"
        name="firstName"
        placeholder={t("gzl.gum.first_name")}
        value={newUserFirstName ?? ""}
        setValue={(newValue) => {
          setNewUserFirstName(newValue);
          setHasUnsavedChanges(true);
        }}
        isValidInput={validateUserNames(newUserFirstName ?? "")}
        validationMessage={t("gzl.gum.invalid_first_name")}
        required
      />

      <Input
        id={t("gzl.gum.last_name")}
        key="lastName"
        type="text"
        label={t("gzl.gum.last_name") + "*"}
        htmlFor="lastName"
        name="lastName"
        placeholder={t("gzl.gum.last_name")}
        value={newUserLastName ?? ""}
        setValue={(newValue) => {
          setNewUserLastName(newValue);
          setHasUnsavedChanges(true);
        }}
        isValidInput={validateUserNames(newUserLastName ?? "")}
        validationMessage={t("gzl.gum.invalid_last_name")}
        required
      />

      <Input
        id={t("gzl.gum.email")}
        key="email"
        type="text"
        label={t("gzl.gum.email") + "*"}
        htmlFor="email"
        name="email"
        placeholder={t("gzl.gum.email")}
        value={newUserEmail ?? ""}
        setValue={(newValue) => {
          setNewUserEmail(newValue);
          setHasUnsavedChanges(true);
        }}
        isValidInput={validateEmail(newUserEmail ?? "")}
        validationMessage={t("gzl.gum.invalid_email")}
        required
      />

      {!isOnlyOrgaAdmin(session) && (
        <ToggleSwitch
          id="create_new_organization"
          label={t("gzl.user.interface.create_new_organization")}
          checked={showOrgaCreation}
          onChange={() => {
            setShowOrgaCreation(!showOrgaCreation);
            setHasUnsavedChanges(true);
          }}
          className="font-semibold"
        />
      )}

      {showOrgaCreation ? (
        <OrganizationFormFields
          name={orgaName}
          setName={(value) => {
            setOrgaName(value);
            setHasUnsavedChanges(true);
          }}
          shortName={orgaShortName}
          setShortName={(value) => {
            setOrgaShortName(value);
            setHasUnsavedChanges(true);
          }}
        />
      ) : (
        <SelectInput
          id={t("gzl.gum.organization")}
          label={t("gzl.gum.organization") + "*"}
          name="organizationId"
          ariaLabelledby={t("gzl.gum.organization")}
          placeholder="Select one organization"
          noOptionsMessage={t("gzl.gum.no_organization_available")}
          isClearable={false}
          isDisabled={isOrgAdmin}
          isLoading={orgaLoading}
          options={organizationsOptions}
          value={selectedOrgaOption ?? null}
          onInputChange={(input) => setOrgaSearchInput(input)}
          filterOption={null}
          handleChange={(newValue) => {
            setNewUserOrgaOption(newValue as Option);
            setHasUnsavedChanges(true);
          }}
          menuPlacement="top"
        />
      )}

      {userCreationEmailNotificationEnabled && <p className=" mx-auto mt-10">{t("gzl.gum.instruction_post_user_creation")}.</p>}

      {state?.message && state?.success === false && (
        <NoticeBanner aria-live="polite" color="red">
          <Info size={16} />
          {state?.message}
        </NoticeBanner>
      )}

      <div className="flex">
        <Button
          id={t("gzl.gum.create_account")}
          variant="primary"
          title={t("gzl.gum.create_account")}
          ariaLabel="create-account"
          type="submit"
          disabled={!isValidCreation || pending}
        >
          {t("gzl.user.interface.create")}
        </Button>
      </div>
    </Form>
  );
};

export default CreateUserForm;
