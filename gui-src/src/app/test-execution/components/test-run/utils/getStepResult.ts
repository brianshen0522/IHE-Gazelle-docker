import { StepRunReport, TestRunExecution } from "@/app/test-execution/types/TestRunExecution";

interface GetStepResultProps {
  stepRunReports: StepRunReport[];
  index: number;
  executionResult?: StepRunReport;
}

export function getStepResult({ stepRunReports, index, executionResult }: Readonly<GetStepResultProps>) {
  if (executionResult?.result) {
    return executionResult.result;
  }

  return "NOT_STARTED";
}
