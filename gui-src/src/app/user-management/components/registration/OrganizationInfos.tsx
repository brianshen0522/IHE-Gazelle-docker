import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { validateShortName, isValidInput, isStepValid } from "@user-management/utils/validation";
import { type OrganizationInfos as OrganizationInfosType } from "@/app/user-management/components/registration/types";
import { useIsValidStepContext } from "@/app/user-management/context/IsValidStepContext";
import { Input, SelectInput } from "@gazelle/gazelle-component-ui";
import { useDebounce } from "@/shared/hooks/useDebounce";
import { useGetOrganizations } from "@/app/user-management/hooks/swr/useGetGroups";

interface Organization {
  id: string;
  name: string;
  shortname: string;
  archived: boolean;
}

export interface OrganizationInfosProps {
  creationEnabled: boolean;
  joinOrCreateOrg: "JOIN" | "CREATE";
  setJoinOrCreateOrg: (value: "JOIN" | "CREATE") => void;
  organizationInfos: OrganizationInfosType;
  setOrganizationInfos: (orgInfos: OrganizationInfosType) => void;
  selectedOrg: OrganizationInfosType | null;
  setSelectedOrg: (org: OrganizationInfosType | null) => void;
}

const OrganizationInfos = ({
  creationEnabled,
  joinOrCreateOrg,
  setJoinOrCreateOrg,
  organizationInfos,
  setOrganizationInfos,
  selectedOrg,
  setSelectedOrg,
}: Readonly<OrganizationInfosProps>) => {
  const { t } = useTranslation();
  const { setIsValidStep } = useIsValidStepContext();
  const { name, shortname } = organizationInfos;
  const [orgaSearchInput, setOrgaSearchInput] = useState("");
  const debouncedOrgaSearch = useDebounce(orgaSearchInput, 300);
  const { data: orgaData, isLoading: orgaLoading } = useGetOrganizations({
    search: debouncedOrgaSearch,
    archived: false,
  });

  const handleOrgChange = (id: string) => {
    if (!orgaData?.data) return;
    setSelectedOrg(orgaData.data.find((org: Organization) => org.shortname === id) ?? null);
  };

  useEffect(() => {
    if (joinOrCreateOrg === "JOIN" && selectedOrg?.shortname) {
      setIsValidStep(true);
    } else if (joinOrCreateOrg === "CREATE") {
      const isValid = isStepValid([name, shortname], [() => null, validateShortName]);
      setIsValidStep(isValid);
    } else {
      setIsValidStep(false);
    }
  }, [name, shortname, joinOrCreateOrg, selectedOrg?.shortname, setIsValidStep]);

  useEffect(() => {
    if (!orgaData?.data || orgaData.data.length === 0) {
      setJoinOrCreateOrg("CREATE");
    }
  }, [orgaData?.data, setJoinOrCreateOrg]);

  return (
    <>
      {/* JOIN ORG SECTION */}
      {orgaData?.data && orgaData.data.length > 0 && (
        <div className="w-full flex flex-col justify-center gap-y-4">
          <div className="flex gap-x-4">
            <input
              id="joinOrg"
              type="radio"
              name="joinOrCreate"
              onChange={() => setJoinOrCreateOrg("JOIN")}
              checked={joinOrCreateOrg === "JOIN"}
              data-cy="join-org"
            />
            <label htmlFor="joinOrg">{t("gzl.gum.join_existing_organization")}</label>
          </div>

          <SelectInput
            id="organization-select"
            name="organization"
            ariaLabelledby="organization-select-label"
            placeholder={t("gzl.gum.choose_organization")}
            value={selectedOrg ? { value: selectedOrg.shortname, label: selectedOrg.name } : null}
            options={orgaData.data.map((org: Organization) => ({ value: org.shortname, label: org.name }))}
            isClearable={false}
            isDisabled={joinOrCreateOrg === "CREATE"}
            isLoading={orgaLoading}
            onInputChange={(input) => setOrgaSearchInput(input)}
            filterOption={null}
            handleChange={(option) => option && handleOrgChange(option.value)}
          />
        </div>
      )}
      {/* SECTIONS SEPARATOR */}
      {creationEnabled && orgaData?.data && orgaData.data.length > 0 && <h3>{t("gzl.gum.or")}</h3>}
      {/* CREATE ORG SECTION */}
      {creationEnabled && (
        <div className={`w-full flex flex-col gap-y-4`}>
          <div className="gap-y-4">
            <div className="flex gap-x-4 py-4">
              <input
                id="createOrg"
                type="radio"
                name="joinOrCreate"
                onChange={() => setJoinOrCreateOrg("CREATE")}
                checked={joinOrCreateOrg === "CREATE"}
              />
              <label htmlFor="createOrg">{t("gzl.gum.register_and_administrate_organization")}</label>
            </div>
            <Input
              id="organizationName"
              type="text"
              value={name}
              setValue={(value) => setOrganizationInfos({ ...organizationInfos, name: value })}
              label={t("gzl.gum.organization_legal_name")}
              placeholder={t("gzl.gum.organization_legal_name")}
              disabled={joinOrCreateOrg === "JOIN"}
              data-testid="organizationName"
            />
          </div>

          {joinOrCreateOrg !== "JOIN" && (
            <Input
              id="organizationShortName"
              type="text"
              label={t("gzl.gum.short_name")}
              placeholder={t("gzl.gum.short_name")}
              value={shortname}
              setValue={(value) => setOrganizationInfos({ ...organizationInfos, shortname: value })}
              isValidInput={isValidInput(shortname, validateShortName)}
              error={validateShortName(shortname) ? undefined : t("gzl.gum.invalid_short_name")}
              data-testid="organizationShortName"
            />
          )}
          <div className="w-full justify-start font-semibold">{t("gzl.gum.required_fields")}</div>
        </div>
      )}
    </>
  );
};

export default OrganizationInfos;
