import { useTransition } from "react";
import { SupportedInput } from "../../types/ValidationProfile";
import { cloneMap, handleFileError, readFileWithContent } from "@/shared/utils/fileHandling/fileHandling";
import ConfigurationInputItem from "./ConfigurationInputItem";

interface ConfigurationInputProps {
  inputs: SupportedInput[];
  fileData: Map<string, { file: File; content: string }>;
  setFileData: (newFileData: Map<string, { file: File; content: string }>) => void;
  reviewEnabled: boolean;
}

const ConfigurationInput = ({ inputs, fileData, setFileData, reviewEnabled }: ConfigurationInputProps) => {
  const [isPending, startTransition] = useTransition();

  const handleFileChange = (inputId: string) => async (file: File | null) => {
    if (!file) {
      const newFileData = cloneMap(fileData);
      newFileData.delete(inputId);
      setFileData(newFileData);
      return;
    }

    try {
      const { file: fileObj, content } = await readFileWithContent(file);
      const newFileData = cloneMap(fileData);
      newFileData.set(inputId, { file: fileObj, content });

      startTransition(() => {
        setFileData(newFileData);
      });
    } catch (error) {
      handleFileError(error, `Input ${inputId}`);
    }
  };

  return (
    <>
      {inputs.map((input) => {
        const data = fileData.get(input.id);

        return (
          <ConfigurationInputItem
            key={input.id}
            input={input}
            data={data}
            isPending={isPending}
            reviewEnabled={reviewEnabled}
            onFileChange={handleFileChange(input.id)}
          />
        );
      })}
    </>
  );
};

export default ConfigurationInput;
