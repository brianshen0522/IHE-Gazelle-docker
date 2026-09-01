import { CollapsableCard } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { TestRunExecution } from "../../../types/TestRunExecution";
import { TestStep as TestModelStep } from "../../../types/TestModel";
import { PanelLeftOpen, PanelRightOpen } from "lucide-react";
import ResizableSplit from "../../ResizableSplit";
import { useState } from "react";
import DiagramSequence from "../diagram/DiagramSequence";
import TestRunLogs from "../test-logs/TestRunLogs";
import { TestStepResults } from "./TestStepResults";

interface TestStepProps {
  testRun: TestRunExecution;
  testSteps?: TestModelStep[];
  isRunning?: boolean;
}

const TestStepWrapper = ({ testRun, testSteps, isRunning = false }: Readonly<TestStepProps>) => {
  const { t } = useTranslation();
  const [showDiagram, setShowDiagram] = useState(false);

  return (
    <CollapsableCard title={t("gzl.texec.test_steps")}>
      <div className="flex flex-col gap-4">
        <ResizableSplit
          leftComponent={<TestStepResults execution={testRun} testSteps={testSteps} />}
          rightComponent={<DiagramSequence execution={testRun} testSteps={testSteps} />}
          initialLeftWidth={50}
          showRight={showDiagram}
          onToggleRight={() => setShowDiagram(!showDiagram)}
          toggleButtonLabel={showDiagram ? t("gzl.texec.hide_sequence_diagram") : t("gzl.texec.show_sequence_diagram")}
          toggleButtonIcon={showDiagram ? <PanelLeftOpen size={20} /> : <PanelRightOpen size={20} />}
        />

        <TestRunLogs logs={testRun.logs} isRunning={isRunning} testSteps={testSteps} />
      </div>
    </CollapsableCard>
  );
};

export default TestStepWrapper;
