import { useEffect, useState } from "react";
import { SelectInput } from "@gazelle/gazelle-component-ui";
import { FieldError, useFormContext } from "react-hook-form";
import { Option, SupportedParameter } from "@simulation-portal/types/SimulationSequence";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { readParam, removeParam, saveParam } from "@simulation-portal/types/SequenceStorage";

const SimulationRequestSelectInput = ({ supportedParameter }: { supportedParameter: SupportedParameter }) => {
  const { simulationSequence, updatePropertyValue, isRunning } = useSequenceExecutionContext();
  const defaultOption = {
    value: supportedParameter.defaultValue?.value ?? "",
    label: supportedParameter.defaultValue?.label ?? "",
  };
  const [option, setOption] = useState<Option | undefined>(defaultOption);
  const {
    setValue,
    formState: { errors },
  } = useFormContext();
  const error = (errors[supportedParameter.name] as FieldError)?.message;

  useEffect(() => {
    // Refactored to prioritize localStorage values over defaults + error handling + handles empty state when switching between sequences
    const storedValue = readParam(simulationSequence.id, supportedParameter.name);

    if (storedValue) {
      try {
        const storedOption: Option = JSON.parse(storedValue);
        updatePropertyValue(supportedParameter.name, storedOption.value, "STRING");
        setValue(supportedParameter.name, storedOption.value, { shouldValidate: false });
        setOption(storedOption);
      } catch {
        // If parsing fails, use default
        if (supportedParameter.defaultValue) {
          updatePropertyValue(supportedParameter.name, supportedParameter.defaultValue.value, "STRING");
          setValue(supportedParameter.name, supportedParameter.defaultValue, { shouldValidate: false });
          setOption(supportedParameter.defaultValue);
        }
      }
    } else if (supportedParameter.defaultValue) {
      updatePropertyValue(supportedParameter.name, supportedParameter.defaultValue.value, "STRING");
      setValue(supportedParameter.name, supportedParameter.defaultValue, { shouldValidate: false });
      setOption(supportedParameter.defaultValue);
    } else {
      // No stored value and no default - reset to undefined
      setOption(undefined);
      updatePropertyValue(supportedParameter.name, "", "STRING");
      setValue(supportedParameter.name, null, { shouldValidate: false });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [simulationSequence.id, supportedParameter.name]);

  function getLabel() {
    return (
      <>
        {supportedParameter.name}
        {supportedParameter.required && <span className="text-red ml-0.5">*</span>}
        {supportedParameter.error && <span className="text-red font-semibold"> {supportedParameter.error}</span>}
      </>
    );
  }

  function handleChange(option: { value: string; label: string } | null) {
    if (option) {
      setOption(option);
      updatePropertyValue(supportedParameter.name, option.value, "STRING");
      setValue(supportedParameter.name, option, { shouldValidate: true });
      saveParam(simulationSequence.id, supportedParameter.name, JSON.stringify(option));
    } else {
      setOption(undefined);
      updatePropertyValue(supportedParameter.name, "", "STRING");
      setValue(supportedParameter.name, null, { shouldValidate: true });
      removeParam(simulationSequence.id, supportedParameter.name);
    }
  }

  const selectOptions = supportedParameter.options ?? [];

  return (
    <SelectInput
      isClearable
      id={supportedParameter.name}
      label={getLabel()}
      name={supportedParameter.name}
      ariaLabelledby={supportedParameter.name}
      placeholder={"Please select..."}
      options={selectOptions?.map((option) => {
        return { value: option.value, label: option.label };
      })}
      isDisabled={isRunning}
      value={option?.value ? { value: option.value, label: option.label } : null}
      handleChange={(option) => handleChange(option ?? null)}
      error={error}
    />
  );
};

export default SimulationRequestSelectInput;
