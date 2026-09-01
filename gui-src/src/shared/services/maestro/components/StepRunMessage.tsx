import { StepRunExecution } from "@maestro/types/execution/TestSuiteExecution";
import LogMessage from "@maestro/components/logs/LogMessage";

export interface StepRunMessageProps {
  stepRun?: StepRunExecution;
}

const StepRunMessage = ({ stepRun }: StepRunMessageProps) => {
  if (!stepRun) {
    return null;
  }

  return (
    <LogMessage
      startDateTime={stepRun.startDateTime}
      finishDateTime={stepRun.finishDateTime}
      messageType="Step"
      messageId={stepRun.stepName}
      executionStatus={stepRun.executionStatus}
      result={stepRun.result}
    />
  );
};

export default StepRunMessage;
