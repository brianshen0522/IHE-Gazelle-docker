"use client"

import React, { useEffect } from "react";
import { useParams, useSearchParams } from "next/navigation";
import { sequence } from "@simulation-portal/service/constants";
import { BreadcrumbItem, CustomNotFoundError, Skeleton } from "@gazelle/gazelle-component-ui";
import SimulationReportRenderer from "@simulation-portal/components/simulation-report/SimulationReportRenderer";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { useDatahouseItem } from "@hooks/useDatahouseItem";
import Unauthorized from "@auth/Unauthorized";
import ContentHeader from "@shared/components/layout/ContentHeader";
import { useTranslation } from "react-i18next";


const SimulationReportMain = () => {
  const { t } = useTranslation();
  const { reportId } = useParams<{ reportId: string }>();
  const { setReportItem, simulationReport } = useSequenceExecutionContext();
  const searchParams = useSearchParams();
  const readAccessKeySearchParam = searchParams.get("readAccessKey") as string;

  const { item, isLoading, error } = useDatahouseItem({
    itemId: reportId,
    readAccessKey: readAccessKeySearchParam ?? undefined,
    enabled: !!reportId
  });

  useEffect(() => {
    if (item) {
      setReportItem(item);
    }
  }, [item, setReportItem]);

  if (isLoading) {
    return <Skeleton className="h-screen" />
  }

  const breadcrumbs: BreadcrumbItem[] = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.simulation_portal.simulation"), url: sequence.basePath },
    ...(simulationReport?.sequenceId
      ? [{ label: simulationReport?.sequenceId, url: `${sequence.basePath}/${simulationReport?.sequenceId}` }]
      : []),
    { label: t("gzl.simulation_portal.simulation_report"), url: "/" },
  ];

  if (error) {
    return <Unauthorized />;
  }

  if (!item) {
    return <CustomNotFoundError />;
  }

  // Type guard: Only handle SIMULATION_REPORT
  if (item.type !== "SIMULATION_REPORT") {
    return (
      <CustomNotFoundError
        message={t("gzl.simulation_portal.not_simulation_report")}
      />
    );
  }

  return (
    <ContentHeader
      secured={false}
      breadcrumbsItems={breadcrumbs}
      title={
        <span className="flex items-center gap-2">
          {t("gzl.simulation_portal.simulation_report")}
          {simulationReport &&
            <ResultBadge result={simulationReport.result} />
          }
        </span>
      }
    >
      <SimulationReportRenderer />
    </ContentHeader>
  );
}

export default SimulationReportMain;