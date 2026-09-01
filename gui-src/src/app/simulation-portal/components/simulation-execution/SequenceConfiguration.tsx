import React from "react";
import { SupportedParameter } from "@simulation-portal/types/SimulationSequence";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import SimulationRequestInput from "@simulation-portal/components/simulation-request/SimulationRequestInput";

const SequenceConfiguration = () => {
  const {simulationSequence} = useSequenceExecutionContext();

  const grouped = simulationSequence.supportedParameters?.reduce<Record<string, SupportedParameter[]>>((acc, param) => {
    if (!acc[param.groupName]) {
      acc[param.groupName] = [];
    }
    acc[param.groupName].push(param);
    return acc;
  }, {});

  return grouped ? (
    <div>
      <div className="mb-2">
        <p className="mb-2 text-grey-600 italic">
          The parameters below are used to configure the messages that will be sent to / expected from your
          system under test.
          Please fill at least all the required parameters marked with <span className="text-red">*</span>.
        </p>
        {Object.entries(grouped).map(([groupName, params]) => (
          <div key={groupName}>
            <p className="text-purple mb-2 font-semibold">
              {groupName}
            </p>
            <div className="pl-2 w-1/2">
              <ul>
                {params.map((param) => (
                  <li key={param.name} className="mb-2">
                    <SimulationRequestInput supportedParameter={param}/>
                    <p className="text-grey-500 text-sm">{param.description}</p>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        ))}
      </div>
    </div>
  ) : null;
}

export default SequenceConfiguration;