import { useTranslation } from "react-i18next";
import { FileCheck } from "lucide-react";
import OutputItem from "./OutputItem";

export interface Output {
  name: string;
  value?: string;
  reference?: string;
  fileName?: string;
  mimeType?: string;
  itemType?: string;
}

interface TestStepOutputsProps {
  executionResult?: { outputs?: Output[] };
  inline?: boolean;
  itemId?: string; // Datahouse item ID for attachment downloads
}

const TestStepOutputs = ({ executionResult, inline, itemId }: Readonly<TestStepOutputsProps>) => {
  const { t } = useTranslation();
  const rawOutputs = executionResult?.outputs;

  const outputs = Array.isArray(rawOutputs) ? rawOutputs : [];
  if (!outputs.length) return null;

  if (inline) {
    return (
      <div className="pt-2 flex items-center gap-2 justify-center">
        <FileCheck className="w-4 h-4 text-blue" />
        {outputs.map((output, idx) => (
          <span key={`${output.name}-${output.reference ?? output.fileName}`} className="flex text-sm">
            <OutputItem output={output} inline itemId={itemId} />
            {idx < outputs.length - 1 && <span className="mx-1">|</span>}
          </span>
        ))}
      </div>
    );
  }

  return (
    <div className="pt-2 flex">
      <p className="text-sm font-medium mb-2">{t("gzl.texec.outputs")}:</p>
      <ul className="list-disc pl-5 space-y-1">
        {outputs.map((output) => (
          <li key={`${output.name}-${output.reference ?? output.fileName}`} className="text-sm flex">
            <OutputItem output={output} itemId={itemId} />
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TestStepOutputs;
