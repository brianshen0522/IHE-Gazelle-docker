import { getResultColor } from "@/shared/services/maestro/utils/logStyles";
import RenderSanitizedHTML from "@/shared/services/RenderSanitizedHTML";
import { CircleCheck, CircleSlash, CircleX } from "lucide-react";

interface InstructionDiagramProps {
  name: string;
  description: string;
  executionResult?: {
    result: string;
  };
}

function InstructionDiagram({ name, description, executionResult }: Readonly<InstructionDiagramProps>) {
  return (
    <div className="border border-purple p-2 flex flex-col gap-2 items-start flex-1 min-w-0">
      <div className="flex items-center gap-2 font-semibold">
        <span>{name}</span>
        {executionResult?.result === "DONE" && <CircleCheck className={getResultColor(executionResult?.result)} />}
        {executionResult?.result === "FAILED" && <CircleX className={getResultColor(executionResult?.result)} />}
        {executionResult?.result === "UNDEFINED" && <CircleSlash className={getResultColor(executionResult?.result)} />}
      </div>
      <RenderSanitizedHTML untrustedHTML={description} />
    </div>
  );
}

export default InstructionDiagram;
