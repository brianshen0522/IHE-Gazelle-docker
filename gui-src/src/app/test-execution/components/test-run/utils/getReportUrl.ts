import { TestRunExecution } from "@/app/test-execution/types/TestRunExecution";

// Extract report ID from testReportUrl and construct front-end URL
interface GetReportUrlParams {
  testReportUrl?: string;
  testSessionId?: string;
  kindOfReport?: string;
}

export const getReportUrl = ({ testReportUrl, testSessionId, kindOfReport = "TEST_REPORT" }: GetReportUrlParams): string | null => {
  if (!testReportUrl) return null;

  const regex = /\/items\/([^/]+)/;
  const match = regex.exec(testReportUrl);
  if (match?.[1]) {
    const reportId = match[1];
    const params = new URLSearchParams();

    // Add testSessionId if available
    if (testSessionId) {
      params.set("testSessionId", testSessionId);
    }

    const queryString = params.toString();
    let urlPath = `/test-execution/reports/${reportId}`;
    if (kindOfReport === "VALIDATION_REPORT") urlPath = `/validation-portal/reports/${reportId}`;
    else if (kindOfReport === "SIMULATION_REPORT") urlPath = `/simulation-portal/reports/${reportId}`;

    return queryString ? `${urlPath}?${queryString}` : urlPath;
  }
  return testReportUrl;
};

// Get test report URL from execution, with fallback to last summary location
export const getTestReportUrl = (execution: TestRunExecution): string | undefined => {
  if (!execution) {
    return undefined;
  }

  if (execution.testReportUrl) {
    return execution.testReportUrl;
  }

  return execution.testReportSummaries?.at(-1)?.location;
};
