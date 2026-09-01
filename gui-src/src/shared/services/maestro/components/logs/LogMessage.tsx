import { PropsWithChildren } from "react";
import { StepResult, TestResult } from "@maestro/types/report/Result";
import { ExecutionStatus } from "@maestro/types/execution/TestSuiteExecution";
import { RefreshCw } from "lucide-react";
import FormattedDate from "@shared/components/dates/FormattedDate";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { getResultColor } from "@/shared/services/maestro/utils/logStyles";

export interface LogMessageProps {
  startDateTime?: string;
  finishDateTime?: string;
  messageType: string;
  messageId: string;
  executionStatus: ExecutionStatus;
  result?: TestResult | StepResult;
}

const LogMessage = ({
  startDateTime,
  finishDateTime,
  messageType,
  messageId,
  executionStatus,
  result,
  children,
}: PropsWithChildren<LogMessageProps>) => {
  return (
    <div className="text-sm">
      {startDateTime && (
        <div className="my-1 p-2 bg-green-50 border-l-4 border-blue rounded">
          <p className="inline-flex items-center gap-2">
            <FormattedDate dateString={startDateTime} /> - {messageType} <i>{messageId}</i> started...
            {executionStatus === ExecutionStatus.RUNNING && <RefreshCw className="h-5 w-5 text-blue animate-spin" />}
          </p>
        </div>
      )}
      {children}
      {finishDateTime && result && (
        <div className={`my-1 p-2 bg-green-50 border-l-4 ${result ? getResultColor(result) : "border-blue"} rounded`}>
          <div className="inline-flex items-center gap-2">
            <FormattedDate dateString={finishDateTime} /> - {messageType} <i>{messageId}</i> finished with result
            <ResultBadge result={result} />
          </div>
        </div>
      )}
    </div>
  );
};

export default LogMessage;
