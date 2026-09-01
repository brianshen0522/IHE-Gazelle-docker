import { Download } from "lucide-react";
import { Button, useSmallScreen } from "@gazelle/gazelle-component-ui";
import FormattedDate from "@shared/components/dates/FormattedDate";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import UnexpectedErrorAlert from "@simulation-portal/components/simulation-report/UnexpectedErrorAlert";
import SimulationStatSummary from "@simulation-portal/components/simulation-report/SimulationStatSummary";
import UnexpectedErrorDisplay from "@simulation-portal/components/simulation-report/UnexpectedErrorDisplay";
import { AclDisplay } from "@/shared/components/ACL/AclDisplay";
import useDataDownloader from "@hooks/useDataDownloader";
import { useGetSequenceById } from "@simulation-portal/hook/useGetSequenceById";

const SimulationReportSummary = () => {
  const isSmallScreen = useSmallScreen();
  const { simulationReportItem, simulationReport } = useSequenceExecutionContext();
  const { simulationSequence } = useGetSequenceById({ id: simulationReport?.sequenceId ?? null });
  const download = useDataDownloader(JSON.stringify(simulationReport, null, 2), `simulation_report_${simulationReport?.uuid}.json`, "text/plain");
  const sequenceVersion = simulationSequence?.version ? ` (${simulationSequence.version})` : "";
  const hasUnexpectedErrors = (): boolean => {
    return (simulationReport?.unexpectedErrors?.length ?? 0) > 0;
  };

  if (!simulationReportItem || !simulationReport) {
    return <></>;
  }

  return (
    <div className="px-2 pb-2">
      <div className={`flex ${isSmallScreen ? "flex-col" : "flex-row"} justify-start gap-2 mt-3`}>
        <div className="flex flex-col gap-4 justify-start basis-7/12">
          <div className="flex flex-col gap-2">
            <div className="flex gap-2">
              <b className="w-36 shrink-0">Sequence name</b>
              <p>{`${simulationReport.sequenceId}${sequenceVersion}`}</p>
            </div>
            <div className="flex gap-2">
              <b className="w-36 shrink-0">Simulation service</b>
              <p>
                {simulationReport.serviceName} ({simulationReport.serviceVersion})
              </p>
            </div>
            <div className="flex gap-2">
              <b className="w-36 shrink-0">Executed on</b>
              <FormattedDate dateString={simulationReport.dateTime} />
            </div>
          </div>
          <AclDisplay acl={simulationReportItem?.accessControlList} itemId={simulationReportItem.id} />
        </div>
        <div className="flex flex-col gap-2 items-center">
          <SimulationStatSummary />
          <Button id="download-simulation-report" type={"button"} className="m-auto" variant="primary" onClick={download}>
            <Download /> {"Download report"}
          </Button>
        </div>
      </div>
      <UnexpectedErrorAlert hasUnexpectedErrors={hasUnexpectedErrors} />
      {hasUnexpectedErrors() && <UnexpectedErrorDisplay errors={simulationReport.unexpectedErrors} />}
    </div>
  );
};

export default SimulationReportSummary;
