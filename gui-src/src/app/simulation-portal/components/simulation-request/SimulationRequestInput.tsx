import { useFormContext } from "react-hook-form";
import { ParameterType } from "@simulation-portal/types/ParameterType";
import { SupportedParameter } from "@simulation-portal/types/SimulationSequence";
import SimulationRequestCheckbox from "@simulation-portal/components/simulation-request/SimulationRequestCheckbox";
import SimulationRequestInputText from "@simulation-portal/components/simulation-request/SimulationRequestInputText";
import SimulationRequestFileUpload from "@simulation-portal/components/simulation-request/SimulationRequestFileUpload";
import SimulationRequestSelectInput from "@simulation-portal/components/simulation-request/SimulationRequestSelectInput";

const SimulationRequestInput = ({supportedParameter}: { supportedParameter: SupportedParameter }) => {
  const {register} = useFormContext();
  const requiredMessage = "This attribute is required to start the simulation."

  switch (supportedParameter.type) {
    case ParameterType.TEXT:
      return !supportedParameter.valueSetId && !supportedParameter.options ? (
        <SimulationRequestInputText
          {...register(supportedParameter.name, {
            required: supportedParameter.required ? requiredMessage : false,
          })}
          supportedParameter={supportedParameter}
        />
      ) : (
        <SimulationRequestSelectInput
          {...register(supportedParameter.name, {
            required: supportedParameter.required ? requiredMessage : false,
          })}
          supportedParameter={supportedParameter}
        />
      );

    case ParameterType.BOOLEAN:
      return <SimulationRequestCheckbox supportedParameter={supportedParameter} />;

    case ParameterType.FILE:
      return (
        <SimulationRequestFileUpload
          {...register(supportedParameter.name, {
            required: supportedParameter.required ? requiredMessage : false,
          })}
          supportedParameter={supportedParameter}
        />
      );

    default:
      return null;
  }
};


export default SimulationRequestInput;