import { TestStep } from "@/app/test-execution/types/TestModel";
import { Cog, Settings, CircleCheck, CircleX, CircleSlash, User } from "lucide-react";
import { getResultColor } from "@/shared/services/maestro/utils/logStyles";
import { TestRunExecution } from "@/app/test-execution/types/TestRunExecution";
import TestStepOutputs from "../test-step/TestStepOutputs";
import InstructionDiagram from "./diagram-element/InstructionDiagram";
import { useTranslation } from "react-i18next";
import { extractItemIdFromReportUrl } from "@/shared/utils/datahouse/extractItemId";

interface DiagramSequenceProps {
  execution: TestRunExecution;
  testSteps?: TestStep[];
}

const DiagramSequence = ({ execution, testSteps }: DiagramSequenceProps) => {
  const { t } = useTranslation();

  // Use live results during execution, final results after completion
  const isRunning = execution.status === "RUNNING";
  const stepRunReports = isRunning ? execution.liveStepResults || [] : execution.testReport?.testRunReports?.[0]?.stepRunReports || [];

  // Extract datahouse item ID for attachment downloads
  const itemId = extractItemIdFromReportUrl(execution.testReportUrl);

  return (
    <div className="flex flex-col items-center">
      <div className="flex items-center gap-2 mb-4">
        <Cog /> <span>{t("gzl.texec.test_engine")}</span>
      </div>

      <div className="flex flex-col">
        {testSteps?.map((step, index) => {
          // Find matching execution result for this step
          const executionResult = stepRunReports.find((report) => report.stepName === step.name);
          const hasNextStep = index < testSteps.length - 1;
          const { name, type, description } = step;

          return (
            <div key={name + type + index} className="flex gap-2">
              {/* Left column - icons and connectors */}
              <div className="flex flex-col items-center flex-shrink-0">
                {type === "USER_INTERACTION" ? (
                  <User className={getResultColor(executionResult?.result)} />
                ) : (
                  <Settings className={getResultColor(executionResult?.result)} />
                )}
                {hasNextStep && <div className="border-l border-dashed h-10 my-2" />}
              </div>

              {/* Right column - step details */}
              <div className="flex flex-col flex-1 min-w-0 pb-4">
                {type === "USER_INTERACTION" ? (
                  <InstructionDiagram name={name} description={description} executionResult={executionResult} />
                ) : (
                  <>
                    <div className="flex items-center gap-2">
                      <span>
                        {index + 1}. {name}
                      </span>
                      {executionResult?.result === "PASSED" && <CircleCheck className={getResultColor(executionResult?.result)} />}
                      {executionResult?.result === "FAILED" && <CircleX className={getResultColor(executionResult?.result)} />}
                      {executionResult?.result === "UNDEFINED" && <CircleSlash className={getResultColor(executionResult?.result)} />}
                    </div>
                    <TestStepOutputs executionResult={executionResult} inline itemId={itemId} />
                  </>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default DiagramSequence;
