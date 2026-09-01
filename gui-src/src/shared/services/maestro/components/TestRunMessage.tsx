import { TestRunExecution } from "@maestro/types/execution/TestSuiteExecution";
import LogMessage from "@/shared/services/maestro/components/logs/LogMessage";
import { PropsWithChildren } from "react";

export interface TestRunMessageProps {
  testRun?: TestRunExecution;
}

const TestRunMessage = ({ testRun, children }: PropsWithChildren<TestRunMessageProps>) => {
  if (!testRun) {
    return null;
  }

  return (
    <LogMessage
      startDateTime={testRun.startDateTime}
      finishDateTime={testRun.finishDateTime}
      messageType="Test run"
      messageId={testRun.testId}
      executionStatus={testRun.executionStatus}
      result={testRun.result}
    >
      {children}
    </LogMessage>
  );
};

export default TestRunMessage;
