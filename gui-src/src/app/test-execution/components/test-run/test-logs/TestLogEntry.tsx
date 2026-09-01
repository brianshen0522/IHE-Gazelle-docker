import { LogEntry } from "@/app/test-execution/types/TestRunExecution";
import { TestStep as TestModelStep } from "@/app/test-execution/types/TestModel";
import { formatTimestamp, formatToString, isStructuredLog } from "../../../../../shared/services/maestro/utils/logFormatters";
import { ICON_MAP, getIconColor, getBorderColor, LogIconType } from "@/shared/services/maestro/utils/logStyles";
import { ResultBadge, isValidResult } from "@/shared/components/report/ResultBadge";
import { JSX } from "react";

interface TestLogEntryProps {
  log: LogEntry;
  testSteps?: TestModelStep[];
}

// Renders a structured log entry with timestamp, icon, message, and result
const StructuredLog = ({ log, testSteps }: { log: Record<string, unknown>; testSteps?: TestModelStep[] }) => {
  const { type, dateTime, result, message, icon, testId, name, stepIndex } = log;

  const timestamp = formatTimestamp(dateTime as string | number | undefined);
  const typeStr = formatToString(type) || "LOG";
  const messageStr = formatToString(message);
  const resultStr = formatToString(result);
  const testIdStr = formatToString(testId);
  const nameStr = formatToString(name);

  // Look up step name by stepIndex if available
  const stepName = typeof stepIndex === "number" && testSteps?.[stepIndex]?.name;
  const displayName = stepName || nameStr || testIdStr;

  const IconComponent = icon && typeof icon === "string" ? ICON_MAP[icon as LogIconType] : null;
  const iconColor = getIconColor(icon as string);

  // Format display text based on log type
  const displayText = (): JSX.Element => {
    if (typeStr === "STEP_RUN_STARTED" || typeStr === "STEP_RUN_FINISHED" || typeStr === "STEP_RUN_RUNNING") {
      // Extract status: STEP_RUN_STARTED -> "started"
      const status = typeStr.replace("STEP_RUN_", "").toLowerCase();
      return (
        <span className="font-semibold">
          {displayName} {status}
        </span>
      );
    }
    if (typeStr === "EXECUTION_FINISHED")
      return <>Execution finished.</>;
    return <>{messageStr || displayName || typeStr}</>;
  };

  return (
    <div className={`border-l-4 rounded pl-2 py-1 ${getBorderColor(resultStr)}`}>
      <div className="flex items-center gap-2 text-sm">
        {timestamp && <span>[{timestamp}]</span>}
        {IconComponent && <IconComponent size={20} className={iconColor} />}
        <span>{displayText()}</span>
        {resultStr && isValidResult(resultStr) && <ResultBadge result={resultStr} />}
      </div>
    </div>
  );
};

const PlainLog = ({ log }: { log: string }) => {
  return <>{log}</>;
};

const TestLogEntry = ({ log, testSteps }: TestLogEntryProps) => {
  if (isStructuredLog(log)) {
    return <StructuredLog log={log} testSteps={testSteps} />;
  }

  return <PlainLog log={String(log)} />;
};

export default TestLogEntry;
