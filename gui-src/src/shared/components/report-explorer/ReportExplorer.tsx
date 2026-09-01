"use client";

import { useEffect, useMemo } from "react";
import { ScrollTop, useSidePanel, useSmallScreen } from "@gazelle/gazelle-component-ui";
import { useSearchParams } from "next/navigation";
import { useSession } from "next-auth/react";
import GenericFilters from "@shared/components/filter/GenericFilters";
import ContentHeaderWrapper from "@shared/components/layout/ContentHeaderWrapper";
import TablePaginationWrapper from "@shared/components/table/TablePaginationWrapper";
import { useCreateQuery } from "@shared/hooks/useCreateQuery";
import { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";
import ReportExplorerSidePanel from "./ReportExplorerSidePanel";
import { ExplorerItemType, getExplorerDefinition, getExplorerType, reportExplorerTypeOrder, useExplorerColumns } from "./config";

function OwnerFilter({
  currentUserName,
  currentUserOwnerValue,
  currentOrganizationName,
  currentOrganizationReaderValue,
  owner,
  reader,
  onChange,
}: Readonly<{
  currentUserName: string;
  currentUserOwnerValue: string;
  currentOrganizationName: string;
  currentOrganizationReaderValue: string;
  owner?: string;
  reader?: string;
  onChange: (params: Record<string, string | null>) => void;
}>) {
  const value =
    owner && owner === currentUserOwnerValue
      ? "current-user"
      : reader && reader === currentOrganizationReaderValue
        ? "current-organization"
        : "any";

  return (
    <label className="flex items-center gap-2 text-sm text-grey-700">
      <span className="font-medium">Owner</span>
      <select
        className="rounded-md border border-grey-300 bg-white px-2 py-1 text-sm"
        value={value}
        onChange={(event) => {
          if (event.target.value === "current-user") {
            onChange({ owner: currentUserOwnerValue, reader: null });
            return;
          }

          if (event.target.value === "current-organization") {
            onChange({ owner: null, reader: currentOrganizationReaderValue });
            return;
          }

          onChange({ owner: null, reader: null });
        }}
        disabled={!currentUserOwnerValue && !currentOrganizationReaderValue}
        aria-label="Owner"
      >
        <option value="any">Any</option>
        {currentUserOwnerValue && <option value="current-user">Current user{currentUserName ? ` (${currentUserName})` : ""}</option>}
        {currentOrganizationReaderValue && (
          <option value="current-organization">Current user&apos;s organization{currentOrganizationName ? ` (${currentOrganizationName})` : ""}</option>
        )}
      </select>
    </label>
  );
}

function TestReportsLink() {
  return (
    <a
      href="/gazelle/reports?type=TEST_REPORT"
      className="rounded-md border border-purple px-3 py-1 text-sm font-medium text-purple hover:bg-lightpurple/25"
    >
      Access test reports
    </a>
  );
}

const RESERVED_PARAMS = new Set(["row", "_offset", "_limit", "_sort", "_sort_order", "type"]);
const DATE_FILTER_PARAMS = new Set(["date", "validation_date_time", "test_report_date_time", "simulation_report_date_time"]);

export function ReportExplorer() {
  const searchParams = useSearchParams();
  const { createQuery } = useCreateQuery();
  const { data: session } = useSession();
  const { setIsOpen, setSelectedRow } = useSidePanel<DatahouseItem>();
  const isSmallScreen = useSmallScreen();

  const activeType = getExplorerType(searchParams.get("type"));
  const definition = getExplorerDefinition(activeType);
  const dateFilterParamName = definition.dateFilterParamName;
  const columns = useExplorerColumns(activeType);
  const currentUserName = session?.user?.name ?? "";
  const currentUserOwnerValue = session?.user?.gazelleId ?? "";
  const currentOrganizationName = session?.user?.organization ?? "";
  const currentOrganizationReaderValue = currentOrganizationName ? `org:${currentOrganizationName}` : "";

  const filterSearchParameters = useMemo<Record<string, string>>(() => {
    const params: Record<string, string> = {};

    searchParams.forEach((value, key) => {
      if (!value || RESERVED_PARAMS.has(key)) {
        return;
      }
      params[key] = value;
    });

    return params;
  }, [searchParams]);

  const activeFilterSearchParameters = useMemo<Record<string, string>>(() => {
    return Object.fromEntries(
      Object.entries(filterSearchParameters).filter(([key]) => !DATE_FILTER_PARAMS.has(key) || key === dateFilterParamName),
    );
  }, [dateFilterParamName, filterSearchParameters]);

  const effectiveSearchParameters = useMemo<Record<string, string>>(
    () => ({
      type: definition.searchType,
      ...activeFilterSearchParameters,
    }),
    [definition.searchType, activeFilterSearchParameters],
  );

  useEffect(() => {
    if (isSmallScreen) {
      setIsOpen(false);
    }
  }, [isSmallScreen, setIsOpen]);

  useEffect(() => {
    if (!searchParams.get("row")) {
      setSelectedRow(null);
      setIsOpen(false);
    }
  }, [searchParams, setIsOpen, setSelectedRow]);

  const switchType = (nextType: ExplorerItemType) => {
    if (nextType === activeType) {
      return;
    }

    setSelectedRow(null);
    setIsOpen(false);
    createQuery({ type: nextType }, true);
  };

  return (
    <>
      <ContentHeaderWrapper id="report-explorer-header" title="Report explorer" />

      <div className="flex flex-grow overflow-hidden">
        <div className="flex flex-col flex-grow overflow-hidden gap-3 p-1">
          <div className="flex items-center gap-6 border-b border-lightgrey px-1">
            {reportExplorerTypeOrder.map((type) => {
              const itemDefinition = getExplorerDefinition(type);
              const isActive = type === activeType;

              return (
                <button
                  key={type}
                  type="button"
                  onClick={() => switchType(type)}
                  className={`border-b-2 px-1 py-2 text-sm transition-colors ${
                    isActive ? "border-purple text-purple font-semibold" : "border-transparent text-grey-600 hover:text-purple"
                  }`}
                >
                  {itemDefinition.label}
                </button>
              );
            })}
          </div>

          <GenericFilters
            searchParameters={activeFilterSearchParameters}
            indexNameMapping={definition.indexNameMapping}
            customIndexFilter={(item) => definition.filterableIndexNames.includes(item.name)}
            hiddenActiveFilterKeys={["type", "date", "validation_date_time", "test_report_date_time", "simulation_report_date_time", "owner", "reader"]}
            excludedKeys={["type", "date", "validation_date_time", "test_report_date_time", "simulation_report_date_time", "owner", "reader"]}
            isDateFilter={true}
            dateFilterParamName={dateFilterParamName}
            dateFilterLabel={definition.dateFilterLabel}
            persistentParams={{
              type: activeType,
              ...(activeFilterSearchParameters.owner ? { owner: activeFilterSearchParameters.owner } : {}),
              ...(activeFilterSearchParameters.reader ? { reader: activeFilterSearchParameters.reader } : {}),
              ...(activeFilterSearchParameters[dateFilterParamName] ? { [dateFilterParamName]: activeFilterSearchParameters[dateFilterParamName] } : {}),
            }}
            type="datahouse"
          />

          <div className="flex flex-wrap items-center justify-between gap-2">
            <OwnerFilter
              currentUserName={currentUserName}
              currentUserOwnerValue={currentUserOwnerValue}
              currentOrganizationName={currentOrganizationName}
              currentOrganizationReaderValue={currentOrganizationReaderValue}
              owner={activeFilterSearchParameters.owner}
              reader={activeFilterSearchParameters.reader}
              onChange={(params) => createQuery(params)}
            />
            {activeType !== "TEST_REPORT" && <TestReportsLink />}
          </div>

          <TablePaginationWrapper<DatahouseItem>
            tableColumns={columns}
            baseUrl="/gazelle/test-execution/api"
            apiFolder="reports"
            emptyDataMessage={definition.emptyDataMessage}
            initialField="date"
            initialSortOrder="desc"
            initialLimit={25}
            loadMoreStep={25}
            paramMap={{
              offset: "_offset",
              limit: "_limit",
              sortBy: "_sort",
              sortOrder: "_sort_order",
            }}
            searchParameters={effectiveSearchParameters}
          />
        </div>

        {!isSmallScreen && <ReportExplorerSidePanel />}
        <ScrollTop />
      </div>
    </>
  );
}
