import { useState } from "react";
import { FileUpload } from "@gazelle/gazelle-component-ui";
import { FieldError, useFormContext } from "react-hook-form";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { SupportedParameter } from "@simulation-portal/types/SimulationSequence";
import { toast } from "react-toastify";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";

const SimulationRequestFileUpload = ({ supportedParameter }: { supportedParameter: SupportedParameter }) => {
  const { updatePropertyValue } = useSequenceExecutionContext();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const [paramValue, setParamValue] = useState<File | null>(null);
  const {
    setValue,
    formState: { errors },
  } = useFormContext();
  const error = (errors[supportedParameter.name] as FieldError)?.message;

  function handleChange(file: File | null) {
    const maxSize = 2 * 1024 * 1024 * 1024; // 2GB in bytes

    if (file) {
      if (file.size > maxSize) {
        setValue(supportedParameter.name, null, { shouldValidate: true });
        setParamValue(null);
        toast.error(`The selected file "${file.name}" exceeds the maximum allowed size of 2GB.`);
        return;
      }
      setParamValue(file);
      setValue(supportedParameter.name, file, { shouldValidate: true });
      const reader64 = new FileReader();
      reader64.onloadend = () => {
        const result = reader64.result as string;
        // Remove prefix file content
        const base64Content = result.split(",")[1];
        updatePropertyValue(supportedParameter.name, base64Content, "BYTE_ARRAY");
      };
      reader64.readAsDataURL(file);
      setHasUnsavedChanges(true);
    } else {
      setHasUnsavedChanges(false);
      updatePropertyValue(supportedParameter.name, "", "BYTE_ARRAY");
      setValue(supportedParameter.name, null, { shouldValidate: true });
      setParamValue(null);
    }
  }

  function getLabel() {
    return (
      <>
        {supportedParameter.name}
        {supportedParameter.required && <span className="text-red ml-0.5">*</span>}
      </>
    );
  }

  return (
    <FileUpload
      isValid
      label={getLabel()}
      labelWithFile={paramValue?.name ?? ""}
      labelWithoutFile="Select the file to upload"
      setValue={(file) => handleChange(file)}
      value={paramValue}
      error={error}
    />
  );
};

export default SimulationRequestFileUpload;
