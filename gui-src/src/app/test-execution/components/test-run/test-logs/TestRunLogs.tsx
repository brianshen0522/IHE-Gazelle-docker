"use client";
import { useEffect, useRef } from "react";
import { Terminal } from "lucide-react";
import { useTranslation } from "react-i18next";
import TestLogEntry from "./TestLogEntry";
import { LogEntry } from "@/app/test-execution/types/TestRunExecution";
import { TestStep as TestModelStep } from "@/app/test-execution/types/TestModel";
import { CollapsableSubCard } from "@gazelle/gazelle-component-ui";

interface TestRunLogsProps {
  logs?: LogEntry[];
  isRunning: boolean;
  testSteps?: TestModelStep[];
}

export default function TestRunLogs({ logs = [], isRunning, testSteps }: Readonly<TestRunLogsProps>) {
  const { t } = useTranslation();
  const logsEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom when new logs arrive
  useEffect(() => {
    if (logsEndRef.current) {
      logsEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [logs]);

  if (logs.length === 0) {
    return (
      <div className="flex items-center gap-2 p-2">
        <Terminal size={20} />
        <span>{t("gzl.user.interface.no_logs_available")}</span>
      </div>
    );
  }

  return (
    <CollapsableSubCard title={t("gzl.texec.execution_logs")} className="bg-lightgrey rounded-md">
      <div className="flex items-center gap-2 px-4">
        {isRunning && (
          <span className="ml-auto flex items-center gap-2 text-xs text-blue">
            <span className="inline-block w-4 h-4 bg-blue rounded-full animate-pulse"></span>
            {t("gzl.user.interface.running")}
          </span>
        )}
      </div>
      <div className="max-h-64 overflow-y-auto p-4 text-sm space-y-1">
        {logs.map((log, index) => {
          // Generate a stable key - logs are append-only and not reordered
          const key = typeof log === "object" && log !== null && "dateTime" in log ? `log-${index}-${log.dateTime}` : `log-${index}`;
          return <TestLogEntry key={key} log={log} testSteps={testSteps} />;
        })}
        <div ref={logsEndRef} />
      </div>
    </CollapsableSubCard>
  );
}
