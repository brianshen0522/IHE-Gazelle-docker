"use client";
import TablePaginationWrapper from "@shared/components/table/TablePaginationWrapper";
import { ScrollTop, useSmallScreen } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { useTestsColumns } from "./TestsColumns";
import GenericFilters from "@/shared/components/filter/GenericFilters";
import { useSearchParamsUrl } from "@/shared/hooks/useSearchParamsUrl";
import TestSidepanel from "./TestSidepanel";
import { useTestSession } from "@test-execution/context/TestSessionContext";

export default function TestsList() {
  const { t } = useTranslation();
  const isSmallScreen = useSmallScreen();
  const columns = useTestsColumns();
  const { testSessionId } = useTestSession();
  const { searchParameters } = useSearchParamsUrl();

  if (!testSessionId) {
    return null;
  }

  // Construct the API path with the session ID
  const path = `/sessions/${testSessionId}/tests`;

  const indexNameMapping: Record<string, string> = {
    name: t("gzl.user.interface.test_name"),
    testRoleName: t("gzl.user.interface.test_role_name"),
    tags: t("gzl.user.interface.tags"),
    testRoleCapability: t("gzl.user.interface.test_role_capability"),
    deprecated: t("gzl.user.interface.deprecated"),
  };

  return (
    <>
      <GenericFilters
        searchParameters={searchParameters}
        defaultFilters={{ deprecated: "false" }}
        indexNameMapping={indexNameMapping}
        type="test_execution"
        indexPath={path}
        excludedKeys={["testSessionId", "testSuiteId"]}
        excludedFromClear={["testSessionId", "testSuiteId"]}
      />

      <div className="flex flex-grow overflow-hidden mt-4">
        <div className="flex flex-col flex-grow overflow-hidden gap-2">
          <TablePaginationWrapper
            tableColumns={columns}
            emptyDataMessage={t("gzl.texec.empty")}
            type="test_execution"
            path={path}
            searchParameters={searchParameters}
            paramMap={{
              offset: "_offset",
              limit: "_limit",
              sortBy: "_sort",
            }}
          />
        </div>
        <ScrollTop />
        {!isSmallScreen && <TestSidepanel />}
      </div>
    </>
  );
}
