import { TestSuiteExecution } from "@maestro/types/execution/TestSuiteExecution";
import TestRunMessage from "@maestro/components/TestRunMessage";
import StepRunMessage from "@maestro/components/StepRunMessage";

interface MaestroTestExecutionConsoleProps {
  execution: TestSuiteExecution | null;
}

const MaestroTestExecutionConsole = ({ execution }: MaestroTestExecutionConsoleProps) => {
  if (!execution?.tests) {
    return null;
  }

  const testRunExecution = execution.tests[0];

  return (
    <TestRunMessage key={"test-run-" + testRunExecution?.testId} testRun={testRunExecution}>
      {testRunExecution?.stepRuns.map((stepRun) => (
        <StepRunMessage key={"step-run-" + stepRun.stepName} stepRun={stepRun} />
      ))}
    </TestRunMessage>
  );
};

export default MaestroTestExecutionConsole;
