import React from "react";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import SimulationParameters from "@simulation-portal/components/simulation-report/SimulationParameters";
import SimulationReportSummary from "@simulation-portal/components/simulation-report/SimulationReportSummary";
import TransactionReportRenderer from "@simulation-portal/components/simulation-report/TransactionReportRenderer";
import ChevronCard from "@shared/components/boxes/ChevronCard";
import NoticeBanner from "@shared/components/banner/NoticeBanner";
import { useTranslation } from "react-i18next";

const SimulationReportRenderer = () => {
  const { t } = useTranslation();
  const { simulationReport } = useSequenceExecutionContext();

  return (
    <div className="flex flex-col text-grey-500">
      <NoticeBanner color="magenta" weight="normal" className="text-sm">
        Copy the URL to this report and paste it in the location of your choice in order to retrieve it for future use. There is currently no search
        capabilities for simulation reports.
      </NoticeBanner>
      <ChevronCard title={t("gzl.user.interface.summary")}>
        <SimulationReportSummary />
      </ChevronCard>
      <ChevronCard
        title={t("gzl.simulation_portal.simulation_parameters")}
        fallbackText="No simulation parameters were required during the simulation."
        defaultExpanded={false}
      >
        {(simulationReport?.simulationParameters?.length ?? 0) > 0 && <SimulationParameters />}
      </ChevronCard>
      <ChevronCard title={t("gzl.simulation_portal.transactions")}>
        <div className="px-2 pb-2">
          {simulationReport?.transactionReports?.map((transactionReport, index) => (
            <TransactionReportRenderer key={index + transactionReport.transaction} transactionReport={transactionReport} />
          ))}
        </div>
      </ChevronCard>
    </div>
  );
};

export default SimulationReportRenderer;
