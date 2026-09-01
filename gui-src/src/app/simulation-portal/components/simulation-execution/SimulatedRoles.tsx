import React from "react";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { Parameter, SimulatedRole } from "@simulation-portal/types/SimulationSequence";

const SimulatedRoles = () => {
  const {simulationSequence} = useSequenceExecutionContext();

  return (
    <div className="mb-2">
      <p className="mb-2 text-grey-600 italic">
        As a first step, you need to configure your system under test to interact with the following
        components.
      </p>
      <ul>
        {simulationSequence?.simulatedRoles?.map((role) => (
          <li key={role.name}>
            <p className="text-purple font-semibold">
              {`${role.name} (acting as ${role.type})`}
            </p>
            <RoleConfiguration role={role}/>
          </li>
        ))}
      </ul>
    </div>
  );
}

const RoleConfiguration = ({role}: { role: SimulatedRole }) => {
  return (
    <ul>
      {role.configs?.map((config) => (
        <Configuration key={config.name} config={config}/>
      ))}
    </ul>
  );
}

const Configuration = ({config}: { config: Parameter }) => {
  return (
    <li key={config.name}>
      <div className="ml-2 mt-2 flex flex-row gap-2">
        <span className="font-semibold whitespace-nowrap">{config.name}</span>
        <span>{config.value}</span>
      </div>
    </li>
  );
}

export default SimulatedRoles;