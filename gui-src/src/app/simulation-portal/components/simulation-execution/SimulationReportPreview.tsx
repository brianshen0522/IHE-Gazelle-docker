import { Route } from "next";
import { useRouter } from "next/navigation";
import { Eye, FileChartColumn } from "lucide-react";
import { sequence } from "@simulation-portal/service/constants";
import FormattedDate from "@shared/components/dates/FormattedDate";
import useGetSimulationReport from "@simulation-portal/hook/useGetSimulationReport";
import NoticeBanner from "@shared/components/banner/NoticeBanner";
import UnexpectedErrorDisplay from "@simulation-portal/components/simulation-report/UnexpectedErrorDisplay";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";

const SimulationReportPreview = () => {
  const { simulationReportItem, isLoading, error } = useGetSimulationReport();
  const { testReport, execution } = useSequenceExecutionContext();
  const router = useRouter();

  function getResult() {
    const result = execution?.result;
    let color = "bg-grey";
    if (result === "PASSED") {
      color = "bg-green";
    } else if (result === "FAILED") {
      color = "bg-red";
    }
    return <p className={color + " text-white font-semibold p-1 rounded"}>{result}</p>;
  }

  if (isLoading) {
    return null;
  }

  const getUnexpectedErrors = () => {
    if (testReport?.unexpectedErrors) {
      return testReport.unexpectedErrors && <UnexpectedErrorDisplay errors={testReport.unexpectedErrors} />;
    } else if (testReport?.testRunReports[0]?.unexpectedErrors) {
      return testReport.testRunReports[0].unexpectedErrors && <UnexpectedErrorDisplay errors={testReport.testRunReports[0].unexpectedErrors} />;
    } else if (testReport?.testRunReports[0].stepRunReports[0]?.unexpectedErrors) {
      return (
        testReport.testRunReports[0].stepRunReports[0].unexpectedErrors && (
          <UnexpectedErrorDisplay errors={testReport.testRunReports[0].stepRunReports[0].unexpectedErrors} />
        )
      );
    }
  };

  const redirect = () => {
    return router.push(`${sequence.reportPath}/${simulationReportItem?.id}` as Route);
  };

  return (
    <div>
      {error && (
        <NoticeBanner color="red" weight="semibold" className="text-sm">
          {error}
        </NoticeBanner>
      )}
      {getUnexpectedErrors()}
      {simulationReportItem && execution && !error && (
        <div className="ml-2">
          <div className="p-2">
            <FileChartColumn className="inline justify-center" />
            <p className="font-medium align-middle inline pl-1">Report</p>
            <div className="flex flex-row gap-2 mt-2">
              {getResult()}
              <div className="flex flex-row gap-2 p-1">
                <b>Date</b>
                <FormattedDate dateString={execution?.finishDateTime} />
              </div>
            </div>
          </div>
          <div className="flex flex-row items-center gap-2 mt-2">
            <button className="bg-purple text-white p-2 rounded hover:bg-opacity-80" onClick={redirect}>
              <Eye className="inline align-middle" size={20} /> View Simulation report
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default SimulationReportPreview;
