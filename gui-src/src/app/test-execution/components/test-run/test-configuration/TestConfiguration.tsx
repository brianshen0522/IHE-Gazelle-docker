import { Button, CollapsableCard, NoticeBanner } from "@gazelle/gazelle-component-ui";
import { Info } from "lucide-react";
import { useState, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { readFileWithContent, handleFileError } from "@/shared/utils/fileHandling/fileHandling";
import { SupportedInput } from "../../../types/TestModel";
import { UploadedInput } from "@test-execution/types/TestRun";
import { TestRunExecution } from "../../../types/TestRunExecution";
import { SupportedInputField } from "./SupportedInputField";

interface TestConfigurationProps {
  currentExecution: TestRunExecution;
  supportedInputs?: SupportedInput[];
  onFileChange: (inputId: string, fileName: string, fileContent: string | null) => void;
  onInputChange: (inputId: string, content: string) => void;
  uploadedInputs: UploadedInput[];
  areRequiredInputsFilled: boolean;
  onRun?: () => void;
  isRunning: boolean;
}

const TestConfiguration = ({
  currentExecution,
  supportedInputs,
  onFileChange,
  onInputChange,
  uploadedInputs,
  areRequiredInputsFilled,
  onRun,
  isRunning,
}: TestConfigurationProps) => {
  const { t } = useTranslation();
  const [uploadedFileNames, setUploadedFileNames] = useState<Map<string, string>>(new Map());

  const isRunDisabled = isRunning || !areRequiredInputsFilled;

  // Derive persisted file names from currentExecution.inputs
  const persistedFileNames = useMemo(() => {
    const fileNamesMap = new Map<string, string>();

    currentExecution?.inputs?.forEach((input) => {
      if (input.type === "BYTE_ARRAY") {
        if (supportedInputs?.length) {
          const matched = supportedInputs.find((si) => si.type === "FILE" && (si.label === input.name || si.id === input.name));
          if (matched) fileNamesMap.set(matched.id, input.name);
        } else {
          fileNamesMap.set("null", input.name);
        }
      }
    });

    return fileNamesMap;
  }, [currentExecution?.inputs, supportedInputs]);

  // Get input value - check session data first, then persisted data
  const getInputValue = (inputId: string): string => {
    const uploaded = uploadedInputs?.find((input) => input.id === inputId);
    if (uploaded !== undefined) return uploaded.value;

    const persisted = currentExecution?.inputs?.find((input) => input.type === "STRING" && input.name === inputId);
    return persisted?.value ?? "";
  };

  const handleFileChange = async (inputId: string, file: File | null) => {
    if (!file) {
      setUploadedFileNames((prev) => {
        const updated = new Map(prev);
        // Store empty string to mark as explicitly cleared
        updated.set(inputId, "");
        return updated;
      });
      onFileChange(inputId, "", "");
      return;
    }

    try {
      const { file: uploadedFile, content } = await readFileWithContent(file);
      setUploadedFileNames((prev) => {
        const updated = new Map(prev);
        updated.set(inputId, uploadedFile.name);
        return updated;
      });
      onFileChange(inputId, uploadedFile.name, content);
    } catch (error) {
      handleFileError(error, "Error processing uploaded test file");
      setUploadedFileNames((prev) => {
        const updated = new Map(prev);
        updated.delete(inputId);
        return updated;
      });
      onFileChange(inputId, file.name, null);
    }
  };

  return (
    <CollapsableCard title={t("gzl.user.interface.test_run_configuration")}>
      {!areRequiredInputsFilled && (
        <NoticeBanner className="m-2 flex items-center gap-2">
          <Info className="hidden md:flex" />
          {t("gzl.texec.test_input_info")}
        </NoticeBanner>
      )}
      {supportedInputs && supportedInputs.length > 0 ? (
        <div className="space-y-4 p-4">
          {supportedInputs.map((input) => (
            <SupportedInputField
              key={input.id}
              input={input}
              inputValue={getInputValue(input.id)}
              uploadedFileName={uploadedFileNames.get(input.id)}
              persistedFileName={persistedFileNames.get(input.id)}
              isRunning={isRunning}
              onFileChange={(file) => handleFileChange(input.id, file)}
              onInputChange={(content) => onInputChange(input.id, content)}
            />
          ))}
        </div>
      ) : (
        <p className="p-2 text-grey-600">No input expected for this test</p>
      )}

      <div className="flex justify-center mb-4">
        <Button id="test-run" type="button" variant="primary" onClick={onRun} disabled={isRunDisabled}>
          {isRunning ? t("gzl.user.interface.running") : t("gzl.user.interface.run")}
        </Button>
      </div>
    </CollapsableCard>
  );
};

export default TestConfiguration;
