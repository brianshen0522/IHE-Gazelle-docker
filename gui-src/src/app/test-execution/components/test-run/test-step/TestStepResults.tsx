import { useState } from "react";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { useTranslation } from "react-i18next";
import FormattedDate from "@shared/components/dates/FormattedDate";
import { TestRunExecution } from "../../../types/TestRunExecution";
import { TestStep as TestModelStep, StepProperty } from "../../../types/TestModel";
import TestStepOutputs from "./TestStepOutputs";
import { getStepResult } from "../utils/getStepResult";
import { Bot, User } from "lucide-react";
import { extractItemIdFromReportUrl } from "@/shared/utils/datahouse/extractItemId";
import RenderSanitizedHTML from "@shared/services/RenderSanitizedHTML";
import { NoticeBanner } from "@gazelle/gazelle-component-ui";

// Generic function to render step properties
function renderProperties(properties?: StepProperty[]) {
  if (!properties || properties.length === 0) return null;

  return (
    <div className="mt-2 space-y-1">
      {properties.map((prop, index) => (
        <div key={`${prop.name}-${index}`} className="text-sm">
          <span className="font-medium">{prop.name}:</span> <span className="text-gray-700">{prop.value}</span>
        </div>
      ))}
    </div>
  );
}

interface TestStepResultsProps {
  execution: TestRunExecution;
  testSteps?: TestModelStep[];
}

export function TestStepResults({ execution, testSteps }: Readonly<TestStepResultsProps>) {
  const { t } = useTranslation();
  const [openSteps, setOpenSteps] = useState<Set<number>>(new Set());

  // Prioritize live step results during execution, fall back to final report when complete
  const stepRunReports = execution.liveStepResults || execution.testReport?.testRunReports?.[0]?.stepRunReports || [];

  // Extract datahouse item ID for attachment downloads
  const itemId = extractItemIdFromReportUrl(execution.testReportUrl);

  if (!testSteps || testSteps.length === 0) {
    return null;
  }

  const toggleStep = (index: number) => {
    setOpenSteps((prev) => {
      const newSet = new Set(prev);
      newSet.has(index) ? newSet.delete(index) : newSet.add(index);
      return newSet;
    });
  };

  return (
    <div className="p-2 space-y-3">
      {testSteps.map((step, index) => {
        const executionResult = stepRunReports[index];
        const displayResult = getStepResult({ stepRunReports, index, executionResult });
        const stepIcon = step.type === "USER_INTERACTION" ? <User /> : <Bot />;
        const isOpen = openSteps.has(index);

        return (
          <div key={`${step.name}-${step.type}-${index}`} className="rounded-md border border-grey-300 bg-white select-text">
            <div className="flex flex-col p-3 w-full">
              {/* Header section */}
              <div className="flex flex-row justify-between items-center mb-2">
                <div className="flex items-center gap-2 font-semibold">
                  {index + 1}. {step.name} {stepIcon}
                </div>
                <div className="flex items-center gap-3">
                  {executionResult && displayResult !== "RUNNING" && (
                    <div className="flex flex-row gap-2 text-sm">
                      <span className="font-medium">{t("gzl.user.interface.executed_at")}:</span>
                      <FormattedDate dateString={executionResult.dateTime} />
                    </div>
                  )}
                  {displayResult && <ResultBadge result={displayResult} />}
                </div>
              </div>

              <div className="space-y-2">
                {step.description && <RenderSanitizedHTML untrustedHTML={step.description} />}
                {executionResult?.result === "UNDEFINED" && <NoticeBanner color="red">{t("gzl.user.interface.step_undefined_result")}</NoticeBanner>}
                <TestStepOutputs executionResult={executionResult} itemId={itemId} />
                {isOpen && <div className="mt-2 pt-2 border-t border-grey-200">{renderProperties(step.properties)}</div>}
                <button onClick={() => toggleStep(index)} className="text-blue hover:text-visited_link text-sm">
                  {isOpen ? t("gzl.texec.show_less") : t("gzl.texec.show_more")}
                </button>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
