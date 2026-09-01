import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { ParameterType } from "@simulation-portal/types/ParameterType";
import { Parameter } from "@simulation-portal/types/SimulationReport";

const SimulationParameters = () => {
  const { simulationReport } = useSequenceExecutionContext();
  const simulationParameters = simulationReport?.simulationParameters;

  if (!simulationParameters) {
    return null;
  }

  const renderParameter = (parameter: Parameter) => {
    if (parameter.type !== ParameterType.FILE) {
      return (
        <div key={parameter.name}>
          <b>{parameter.name}</b>
          <div>{parameter.value}</div>
        </div>
      );
    }
  }

  return (
    <div className="px-2 pb-2 space-y-2">
      {simulationParameters.map((parameter) => (
        renderParameter(parameter)
      ))}
    </div>
  );
}

export default SimulationParameters;