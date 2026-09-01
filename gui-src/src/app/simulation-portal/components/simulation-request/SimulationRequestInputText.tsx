import { useEffect, useState } from "react";
import { Input } from "@gazelle/gazelle-component-ui";
import { FieldError, useFormContext } from "react-hook-form";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { SupportedParameter } from "@simulation-portal/types/SimulationSequence";
import { readParam, removeParam, saveParam } from "@simulation-portal/types/SequenceStorage";

const SimulationRequestInputText = ({ supportedParameter }: { supportedParameter: SupportedParameter }) => {
  const { simulationSequence, updatePropertyValue, isRunning } = useSequenceExecutionContext();
  const [paramValue, setParamValue] = useState<string>(supportedParameter.defaultValue?.value ?? "");
  const {
    setValue,
    formState: { errors },
  } = useFormContext();
  const error = (errors[supportedParameter.name] as FieldError)?.message;

  useEffect(() => {
    // Set value to properly reset when switching sequences
    const storedValue = readParam(simulationSequence.id, supportedParameter.name);
    const defaultValue = storedValue ?? supportedParameter.defaultValue?.value ?? "";

    updatePropertyValue(supportedParameter.name, defaultValue, "STRING");
    setValue(supportedParameter.name, defaultValue, { shouldValidate: false });
    setParamValue(defaultValue);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [simulationSequence.id, supportedParameter.name]);

  function getLabel() {
    return (
      <>
        {supportedParameter.name}
        {supportedParameter.required && <span className="text-red ml-0.5">*</span>}
      </>
    );
  }

  function handleChange(paramValue: string) {
    if (paramValue === "") {
      removeParam(simulationSequence.id, supportedParameter.name);
    } else {
      saveParam(simulationSequence.id, supportedParameter.name, paramValue);
    }
    updatePropertyValue(supportedParameter.name, paramValue, "STRING");
    setValue(supportedParameter.name, paramValue, { shouldValidate: true });
    setParamValue(paramValue);
  }

  return (
    <Input
      clearIcon
      htmlFor="input"
      id={supportedParameter.name}
      label={getLabel()}
      disabled={isRunning}
      name={supportedParameter.name}
      placeholder={supportedParameter.name}
      required={supportedParameter.required}
      setValue={(paramValue) => handleChange(paramValue)}
      type="text"
      value={paramValue ?? ""}
      error={error}
    />
  );
};

export default SimulationRequestInputText;
