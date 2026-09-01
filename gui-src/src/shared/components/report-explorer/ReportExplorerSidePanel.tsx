"use client";

import { SidePanel, Skeleton, useSidePanel } from "@gazelle/gazelle-component-ui";
import { useDatahouseItem } from "@shared/hooks/useDatahouseItem";
import { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";
import { useGetSequenceById } from "@simulation-portal/hook/useGetSequenceById";
import { getExplorerSummaryTitle, isValidationReportType, renderExplorerSummary } from "./config";

const ReportExplorerSidePanel = () => {
  const { selectedRow, isOpen, setIsOpen } = useSidePanel<DatahouseItem>();
  const { item, isLoading } = useDatahouseItem({
    itemId: selectedRow?.id,
    enabled: Boolean(selectedRow?.id),
  });

  const currentItem = item ?? selectedRow ?? null;
  const { simulationSequence, getByIdLoading } = useGetSequenceById({
    id: currentItem?.type === "SIMULATION_REPORT" ? currentItem.content?.sequenceId ?? null : null,
  });

  if (!currentItem) {
    return (
      <SidePanel isOpen={isOpen} className="p-1">
        <></>
      </SidePanel>
    );
  }

  const accessDetailsProps =
    currentItem.type === "TEST_REPORT"
      ? {
          id: currentItem.id,
          content: "test report",
          pathname: `/test-execution/reports/${currentItem.id}`,
        }
      : currentItem.type === "SIMULATION_REPORT"
        ? {
            id: currentItem.id,
            content: "simulation report",
            pathname: `/simulation-portal/reports/${currentItem.id}`,
          }
        : isValidationReportType(currentItem.type)
          ? {
              id: currentItem.id,
              content: "validation report",
              pathname: `/validation-portal/reports/${currentItem.id}`,
            }
          : undefined;

  return (
    <SidePanel isOpen={isOpen} className="p-1">
      <SidePanel.Header accessDetailsProps={accessDetailsProps} onClose={() => setIsOpen(false)} />
      {((isLoading && !item) || (currentItem.type === "SIMULATION_REPORT" && getByIdLoading)) ? (
        <Skeleton className="w-full h-48" />
      ) : (
        <SidePanel.Section id="report-explorer-summary" title={getExplorerSummaryTitle(currentItem.type)}>
          {renderExplorerSummary(currentItem, { simulationSequence })}
        </SidePanel.Section>
      )}
    </SidePanel>
  );
};

export default ReportExplorerSidePanel;
