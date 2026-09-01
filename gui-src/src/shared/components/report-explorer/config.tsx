"use client";

import { ReactNode, useState } from "react";
import { AccessDetailsLink, InfoRow } from "@gazelle/gazelle-component-ui";
import { createColumnHelper } from "@tanstack/react-table";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";
import useDateFormat from "@shared/hooks/useDateFormat";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";
import { TestReport } from "@maestro/types/report/TestReport";
import { ValidationReportDTO } from "@shared/types/validation/types";
import { SimulationReport } from "@simulation-portal/types/SimulationReport";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";
import { useResolvedUserOrGroupName } from "@shared/hooks/useResolvedUserOrGroupName";
import { useGetSequenceById } from "@simulation-portal/hook/useGetSequenceById";

export type ExplorerItemType = "VALIDATION_REPORT" | "TEST_REPORT" | "SIMULATION_REPORT";

export interface ExplorerItemTypeDefinition {
  type: ExplorerItemType;
  label: string;
  searchType: string;
  dateFilterParamName: string;
  dateFilterLabel: string;
  filterableIndexNames: string[];
  indexNameMapping: Record<string, string>;
  emptyDataMessage: string;
}

const columnHelper = createColumnHelper<DatahouseItem>();

const validationIndexNameMapping: Record<string, string> = {
  validation_overall_result: "Result",
  validation_profile_id: "Validation profile identifier",
  validation_profile_name: "Validation profile name",
  validation_service_name: "Validation service",
  validation_service_version: "Validation service version",
};

const testReportIndexNameMapping: Record<string, string> = {
  test_report_result: "Result",
  test_report_test_name: "Test case",
  test_report_test_version: "Test case version",
  test_report_test_session: "Test session",
  test_report_system_under_test: "System under test",
};

const simulationIndexNameMapping: Record<string, string> = {
  simulation_report_result: "Result",
  simulation_report_service_name: "Simulation service",
  simulation_report_service_version: "Simulation service version",
  simulation_report_sequence_id: "Simulation sequence",
};

export const reportExplorerDefinitions: Record<ExplorerItemType, ExplorerItemTypeDefinition> = {
  VALIDATION_REPORT: {
    type: "VALIDATION_REPORT",
    label: "Validation reports",
    searchType: "VALIDATION_REPORT,VALIDATION",
    dateFilterParamName: "validation_date_time",
    dateFilterLabel: "Validation date",
    filterableIndexNames: [
      "validation_overall_result",
      "validation_profile_name",
      "validation_profile_id",
      "validation_service_name",
      "validation_service_version",
    ],
    indexNameMapping: validationIndexNameMapping,
    emptyDataMessage: "No validation reports available",
  },
  TEST_REPORT: {
    type: "TEST_REPORT",
    label: "Test reports",
    searchType: "TEST_REPORT",
    dateFilterParamName: "test_report_date_time",
    dateFilterLabel: "Execution date",
    filterableIndexNames: [
      "test_report_result",
      "test_report_test_name",
      "test_report_test_version",
      "test_report_test_session",
      "test_report_system_under_test",
    ],
    indexNameMapping: testReportIndexNameMapping,
    emptyDataMessage: "No test reports available",
  },
  SIMULATION_REPORT: {
    type: "SIMULATION_REPORT",
    label: "Simulation reports",
    searchType: "SIMULATION_REPORT",
    dateFilterParamName: "simulation_report_date_time",
    dateFilterLabel: "Execution date",
    filterableIndexNames: [
      "simulation_report_result",
      "simulation_report_sequence_id",
      "simulation_report_service_name",
      "simulation_report_service_version",
    ],
    indexNameMapping: simulationIndexNameMapping,
    emptyDataMessage: "No simulation reports available",
  },
};

export const reportExplorerTypeOrder: ExplorerItemType[] = ["VALIDATION_REPORT", "TEST_REPORT", "SIMULATION_REPORT"];

export function getExplorerDefinition(type: string | null | undefined): ExplorerItemTypeDefinition {
  if (type && type in reportExplorerDefinitions) {
    return reportExplorerDefinitions[type as ExplorerItemType];
  }
  return reportExplorerDefinitions.VALIDATION_REPORT;
}

export function getExplorerType(rawType: string | null | undefined): ExplorerItemType {
  return getExplorerDefinition(rawType).type;
}

export function isValidationReportType(type: string | undefined | null): boolean {
  return type === "VALIDATION_REPORT" || type === "VALIDATION";
}

function getOwners(item: DatahouseItem): string[] {
  return item.accessControlList?.owners ?? [];
}

function getPrimaryOwnerValue(item: DatahouseItem): string {
  return getOwners(item)[0] ?? (item.accessControlList?.isPublic ? "Public" : "-");
}

function OwnerName({ id }: Readonly<{ id: string }>) {
  const { data: session } = useSession();
  const { t } = useTranslation();
  const { displayName, isLoading } = useResolvedUserOrGroupName(id, session, t);

  if (isLoading) {
    return <span className="text-gray-500">...</span>;
  }

  return <span>{displayName}</span>;
}

function OwnersCell({ item }: Readonly<{ item: DatahouseItem }>) {
  const owners = getOwners(item);
  const primaryOwnerId = owners[0];
  const additionalOwnerIds = owners.slice(1);
  const [expanded, setExpanded] = useState(false);

  if (!primaryOwnerId) {
    return <span>{item.accessControlList?.isPublic ? "Public" : "-"}</span>;
  }

  return (
    <div className="flex flex-col gap-0.5">
      <div className="flex items-center gap-2">
        <OwnerName id={primaryOwnerId} />
        {additionalOwnerIds.length > 0 && (
          <button
            type="button"
            onClick={(e) => { e.stopPropagation(); setExpanded((v) => !v); }}
            className="inline-flex items-center justify-center w-8 h-8 rounded-full border border-magenta text-magenta bg-transparent cursor-pointer hover:bg-magenta/10 transition-colors flex-shrink-0 text-xs"
          >
            {expanded ? "-" : `+${additionalOwnerIds.length}`}
          </button>
        )}
      </div>
      {expanded && (
        <div className="flex flex-col gap-0.5 pl-1">
          {additionalOwnerIds.map((ownerId) => (
            <OwnerName key={ownerId} id={ownerId} />
          ))}
        </div>
      )}
    </div>
  );
}

function getValidationReport(item: DatahouseItem): ValidationReportDTO {
  return item.content as ValidationReportDTO;
}

function getTestReport(item: DatahouseItem): TestReport {
  return item.content as TestReport;
}

function getSimulationReport(item: DatahouseItem): SimulationReport {
  return item.content as SimulationReport;
}

function getValidationProfileLabel(item: DatahouseItem): string {
  const report = getValidationReport(item);
  const name =
    report?.validationMethod?.validationProfileName ??
    report?.validationMethod?.validationProfileID ??
    "-";
  const version = report?.validationMethod?.validationProfileVersion;
  return version ? `${name} (${version})` : name;
}

function getTestCaseName(report: TestReport): string {
  return report?.testRunReports?.[0]?.test?.name ?? report?.testSuiteName ?? "-";
}

function getTestCaseVersion(report: TestReport): string | null {
  return report?.testRunReports?.[0]?.test?.version ?? null;
}

function getTestCaseLabel(item: DatahouseItem): string {
  const report = getTestReport(item);
  const name = getTestCaseName(report);
  const version = getTestCaseVersion(report);
  return version ? `${name} (${version})` : name;
}

function getSimulationSequenceLabel(item: DatahouseItem): string {
  const report = getSimulationReport(item);
  return report?.sequenceId ?? "-";
}

function SimulationSequenceCell({ sequenceId }: Readonly<{ sequenceId: string }>) {
  const { simulationSequence } = useGetSequenceById({
    id: sequenceId === "-" ? null : sequenceId,
  });
  const version = simulationSequence?.version;

  if (!version) {
    return <span title={sequenceId}>{sequenceId}</span>;
  }

  return (
    <span className="flex min-w-0 items-center" title={`${sequenceId} (${version})`}>
      <span className="min-w-0 truncate">{sequenceId}</span>
      <span className="shrink-0">&nbsp;({version})</span>
    </span>
  );
}

function getTestSession(report: TestReport): string {
  return report?.testRunReports?.[0]?.runId ?? "-";
}

function getSystemsUnderTestValue(report: TestReport): ReactNode {
  if (!report?.systemsUnderTest?.length) {
    return "-";
  }

  return (
    <div className="flex flex-col">
      {report.systemsUnderTest.map((sut, index) => (
        <span key={`${sut.systemIdentification?.name ?? sut.hostNames?.[0] ?? "sut"}-${index}`}>
          {sut.systemIdentification?.name ?? sut.hostNames?.[0] ?? "-"}
        </span>
      ))}
    </div>
  );
}

function ActionCell({ item }: Readonly<{ item: DatahouseItem }>) {
  if (item.type === "TEST_REPORT") {
    return <AccessDetailsLink id={item.id} redirectLink="Access report" pathname={`/test-execution/reports/${item.id}`} className="justify-center" />;
  }

  if (item.type === "SIMULATION_REPORT") {
    return <AccessDetailsLink id={item.id} redirectLink="Access report" pathname={`/simulation-portal/reports/${item.id}`} className="justify-center" />;
  }

  return <AccessDetailsLink id={item.id} redirectLink="Access report" pathname={`/validation-portal/reports/${item.id}`} className="justify-center" />;
}

export function useExplorerColumns(type: ExplorerItemType) {
  const formatDate = useDateFormat(false);

  if (type === "VALIDATION_REPORT") {
    return [
      columnHelper.accessor((row) => getValidationProfileLabel(row), {
        id: "validation_profile_name",
        header: () => <span>Validation profile</span>,
        cell: (info) => info.getValue(),
      }),
      columnHelper.accessor((row) => getValidationReport(row)?.overallResult ?? "-", {
        id: "validation_overall_result",
        header: () => <span>Result</span>,
        cell: (info) => <ResultBadge result={info.getValue()} />,
      }),
      columnHelper.accessor((row) => getPrimaryOwnerValue(row), {
        id: "owners",
        header: () => <span>Owner</span>,
        cell: (info) => <OwnersCell item={info.row.original} />,
        enableSorting: false,
      }),
      columnHelper.accessor((row) => row.date || getValidationReport(row)?.dateTime, {
        id: "date",
        header: () => <span>Validation date</span>,
        cell: (info) => formatDate(info.getValue() ?? ""),
      }),
      columnHelper.display({
        id: "action",
        header: () => <span>Action</span>,
        cell: (info) => <ActionCell item={info.row.original} />,
        enableSorting: false,
      }),
    ];
  }

  if (type === "TEST_REPORT") {
    return [
      columnHelper.accessor((row) => getTestCaseLabel(row), {
        id: "test_report_test_name",
        header: () => <span>Test case</span>,
        cell: (info) => info.getValue(),
      }),
      columnHelper.accessor((row) => getTestReport(row)?.result ?? "-", {
        id: "test_report_result",
        header: () => <span>Result</span>,
        cell: (info) => <ResultBadge result={info.getValue()} />,
      }),
      columnHelper.accessor((row) => getPrimaryOwnerValue(row), {
        id: "owners",
        header: () => <span>Owner</span>,
        cell: (info) => <OwnersCell item={info.row.original} />,
        enableSorting: false,
      }),
      columnHelper.accessor((row) => row.date || getTestReport(row)?.dateTime, {
        id: "date",
        header: () => <span>Execution date</span>,
        cell: (info) => formatDate(info.getValue() ?? ""),
      }),
      columnHelper.display({
        id: "action",
        header: () => <span>Action</span>,
        cell: (info) => <ActionCell item={info.row.original} />,
        enableSorting: false,
      }),
    ];
  }

  return [
    columnHelper.accessor((row) => getSimulationSequenceLabel(row), {
      id: "simulation_report_sequence_id",
      header: () => <span>Simulation sequence</span>,
      cell: (info) => <SimulationSequenceCell sequenceId={info.getValue()} />,
    }),
    columnHelper.accessor((row) => getSimulationReport(row)?.result ?? "-", {
      id: "simulation_report_result",
      header: () => <span>Result</span>,
      cell: (info) => <ResultBadge result={info.getValue()} />,
    }),
    columnHelper.accessor((row) => getPrimaryOwnerValue(row), {
      id: "owners",
      header: () => <span>Owner</span>,
      cell: (info) => <OwnersCell item={info.row.original} />,
      enableSorting: false,
    }),
    columnHelper.accessor((row) => row.date || getSimulationReport(row)?.dateTime, {
      id: "date",
      header: () => <span>Execution date</span>,
      cell: (info) => formatDate(info.getValue() ?? ""),
    }),
    columnHelper.display({
      id: "action",
      header: () => <span>Action</span>,
      cell: (info) => <ActionCell item={info.row.original} />,
      enableSorting: false,
    }),
  ];
}

interface ExplorerSummaryOptions {
  simulationSequence?: SimulationSequence;
}

export function getExplorerSummaryTitle(itemType: string): string {
  if (itemType === "TEST_REPORT") {
    return "Test report";
  }

  if (itemType === "SIMULATION_REPORT") {
    return "Simulation report";
  }

  return "Validation report";
}

export function renderExplorerSummary(item: DatahouseItem, options?: ExplorerSummaryOptions): ReactNode {
  if (isValidationReportType(item.type)) {
    const report = getValidationReport(item);
    const fileNames = Object.values(report?.inputFileNames ?? {}).join(", ") || "-";

    return (
      <>
        <InfoRow label="Validated content" value={fileNames} />
        <InfoRow
          label="Profile name"
          value={report?.validationMethod?.validationProfileName ?? report?.validationMethod?.validationProfileID ?? "-"}
        />
        <InfoRow label="Profile version" value={report?.validationMethod?.validationProfileVersion ?? "-"} />
        <InfoRow label="Provided by" value={report?.validationMethod?.validationServiceName ?? "-"} />
        <InfoRow label="Validation date" value={report?.dateTime ?? item.date ?? "-"} />
        <InfoRow label="Results" value={<ResultBadge result={report?.overallResult ?? "-"} />} />
      </>
    );
  }

  if (item.type === "TEST_REPORT") {
    const report = getTestReport(item);

    return (
      <>
        <InfoRow label="Test case" value={getTestCaseName(report)} />
        <InfoRow label="Test case version" value={getTestCaseVersion(report) ?? "-"} />
        <InfoRow label="Execution date" value={report?.dateTime ?? item.date ?? "-"} />
        <InfoRow label="Results" value={<ResultBadge result={report?.result ?? "-"} />} />
        <InfoRow label="Test session" value={getTestSession(report)} />
        <InfoRow label="Systems under test" value={getSystemsUnderTestValue(report)} />
      </>
    );
  }

  const report = getSimulationReport(item);
  const simulationSequence = options?.simulationSequence;

  return (
    <>
      <InfoRow label="Simulation sequence" value={report?.sequenceId ?? "-"} />
      <InfoRow label="Sequence version" value={simulationSequence?.version ?? "-"} />
      <InfoRow label="Provided by" value={report?.serviceName ?? "-"} />
      <InfoRow label="Simulation service version" value={report?.serviceVersion ?? "-"} />
      <InfoRow label="Execution date" value={report?.dateTime ?? item.date ?? "-"} />
      <InfoRow label="Results" value={<ResultBadge result={report?.result ?? "-"} />} />
    </>
  );
}
