import MaestroTestExecutionConsole from "@maestro/components/MaestroTestExecutionConsole";
import ChevronCard from "@shared/components/boxes/ChevronCard";
import { InteractWithUser } from "@maestro/types/message/WebSocketMessage";
import { TestSuiteExecution } from "@maestro/types/execution/TestSuiteExecution";
import UserInteractionModal from "@maestro/components/UserInteractionModal";

export interface MaestroTestRunnerProps {
  execution: TestSuiteExecution | null;
  interactWithUser?: InteractWithUser;
  onComplete?: () => void;
  showExecutionLogs?: boolean;
}

const MaestroTestRunner = ({ execution, interactWithUser, onComplete, showExecutionLogs }: MaestroTestRunnerProps) => {
  if (!execution) {
    return null;
  }

  return (
    <ChevronCard title="Test execution logs">
      <div className="w-full rounded-md bg-white">{showExecutionLogs && <MaestroTestExecutionConsole execution={execution} />}</div>
      {interactWithUser && <UserInteractionModal interactWithUser={interactWithUser} onComplete={onComplete} />}
    </ChevronCard>
  );
};

export default MaestroTestRunner;
