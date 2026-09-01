import React from "react";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";

type SequenceType = {
  simulationSequence: SimulationSequence
}

export const TestedTransactions = ({simulationSequence}: SequenceType) => {
  return (
    <ul className="list-disc list-inside space-y-1">
      {simulationSequence?.transactions?.map((transaction) => (
        <li key={transaction}>{transaction}</li>
      ))}
    </ul>
  )
}

export const TestedStandards = ({simulationSequence}: SequenceType) => {
  return (
    <ul className="list-disc list-inside space-y-1">
      {simulationSequence?.standards?.map((standard) => (
        <li key={standard}>{standard}</li>
      ))}
    </ul>
  )
}

export const TestedRoles = ({simulationSequence}: SequenceType) => {
  return (
    <ul className="list-disc list-inside space-y-1">
      {simulationSequence?.testedRoles?.map((role) => (
        <li key={role.name}>{`${role.name} (as ${role.type})`}</li>
      ))}
    </ul>
  );
}

export const SimulatedRoles = ({simulationSequence}: SequenceType) => {
  return (
    <ul className="list-disc list-inside space-y-1">
      {simulationSequence?.simulatedRoles?.map((role) => (
        <li key={role.name}>{`${role.name} (as ${role.type})`}</li>
      ))}
    </ul>
  );
}