import { useEffect, useState } from "react";
import { ToggleSwitch } from "@gazelle/gazelle-component-ui";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { SupportedParameter } from "@simulation-portal/types/SimulationSequence";
import { readParam, saveParam } from "@simulation-portal/types/SequenceStorage";

const SimulationRequestCheckbox = ({ supportedParameter }: { supportedParameter: SupportedParameter }) => {
  const { simulationSequence, updatePropertyValue, isRunning } = useSequenceExecutionContext();
  const [paramValue, setParamValue] = useState<boolean>(supportedParameter.defaultValue?.value === "true");

  useEffect(() => {
    const defaultValue = readParam(simulationSequence.id, supportedParameter.name) || supportedParameter.defaultValue?.value;
    if (defaultValue) {
      const boolValue = defaultValue === "true";
      updatePropertyValue(supportedParameter.name, String(boolValue), "BOOLEAN");
      setParamValue(boolValue);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleChange() {
    const inverse = !paramValue;
    saveParam(simulationSequence.id, supportedParameter.name, String(inverse));
    updatePropertyValue(supportedParameter.name, String(inverse), "BOOLEAN");
    setParamValue(inverse);
  }

  return (
    <>
      <p className="font-semibold">{supportedParameter.name}</p>
      <div className="flex items-center gap-2">
        <ToggleSwitch
          id={`default-toggle-${supportedParameter.name}`}
          status={paramValue ? "Enabled" : "Disabled"}
          checked={paramValue}
          disabled={isRunning}
          onChange={() => handleChange()}
        />
      </div>
    </>
  );
};

export default SimulationRequestCheckbox;
