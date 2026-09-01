"use client";
import { useProfilesColumns } from "./ProfilesColumns";
import TablePaginationWrapper from "@shared/components/table/TablePaginationWrapper";
import { ValidationProfileResponse } from "@validation-portal/types/ValidationProfile";
import { ScrollTop, useSmallScreen } from "@gazelle/gazelle-component-ui";
import ProfileSidePanel from "../profile-details/ProfileSidePanel";
import { useTranslation } from "react-i18next";

export default function ProfilesList() {
  const { t } = useTranslation();
  const columns = useProfilesColumns();
  const isSmallScreen = useSmallScreen();

  // Generate unique row IDs to handle duplicate profileIDs with different versions or services
  const getRowId = (row: ValidationProfileResponse, index: number) => {
    const { profile, validationService } = row;
    const versionSuffix = profile.version ? `-${profile.version}` : "";
    return `${validationService}-${profile.profileID}${versionSuffix}-${index}`;
  };

  return (
    <div className="flex flex-grow overflow-hidden">
      <div className="flex flex-col flex-grow overflow-hidden gap-2 p-1">
        <TablePaginationWrapper<ValidationProfileResponse>
          tableColumns={columns}
          baseUrl="/gazelle/validation-portal/api"
          apiFolder="profiles"
          emptyDataMessage={t("gzl.validation_portal.empty")}
          getRowId={getRowId}
          paramMap={{
            offset: "_offset",
            limit: "_limit",
            sortBy: "_sort",
            sortOrder: "_sort_order",
          }}
          fields={["profile.profileID", "profile.profileName", "profile.tags", "validationService"]}
        />
      </div>
      <ScrollTop />
      {!isSmallScreen && <ProfileSidePanel />}
    </div>
  );
}
