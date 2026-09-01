import { TestModel } from "../../../types/TestModel";
import { TestRoleCard } from "@test-execution/components/test-suite/TestRoleCard";

interface TestParticipantsProps {
  testModel?: TestModel;
}

const TestParticipants = ({ testModel }: TestParticipantsProps) => {
  if (!testModel) {
    return null;
  }

  return (
    <div className="flex flex-row gap-10 p-2">
      {testModel.testRoles.map((role, index) => (
        <TestRoleCard key={`${role.name}-${index}`} name={role.name} capabilities={role.capabilities!} isExpanded={true} />
      ))}
    </div>
  );
};

export default TestParticipants;
