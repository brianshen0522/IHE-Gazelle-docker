import React, { PropsWithChildren } from "react";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import {
  SimulatedRoles,
  TestedRoles,
  TestedStandards,
  TestedTransactions
} from "@simulation-portal/components/simulation-sequence/SimulationMetadata";
import ChevronCard from "@shared/components/boxes/ChevronCard";

type SequenceItemType = {
  name: string
}

const SequenceItem = ({name, children}: PropsWithChildren<SequenceItemType>) => {
  return (
    <div className="grid grid-cols-[max-content_1fr] gap-2 pb-2">
      <span className="font-semibold whitespace-nowrap">{name}</span>
      <span>{children}</span>
    </div>
  )
}

const SimulationContext = () => {
  const {simulationSequence} = useSequenceExecutionContext();

  return (
    <ChevronCard title="Context" fallbackText="This sequence has no context.">
      <SequenceItem name="Simulation service">
        {simulationSequence.simulatorName}
      </SequenceItem>
      <SequenceItem name="Sequence version">
        {simulationSequence.version}
      </SequenceItem>
      <SequenceItem name="Simulated roles">
        <SimulatedRoles simulationSequence={simulationSequence}/>
      </SequenceItem>
      <SequenceItem name="Tested roles">
        <TestedRoles simulationSequence={simulationSequence}/>
      </SequenceItem>
      <div className="grid grid-cols-4">
        <div>
          <SequenceItem name="Tested transactions">
            <TestedTransactions simulationSequence={simulationSequence}/>
          </SequenceItem>
        </div>
        <div>
          <SequenceItem name="Tested standards">
            <TestedStandards simulationSequence={simulationSequence}/>
          </SequenceItem>
        </div>
      </div>
    </ChevronCard>
  );
}

export default SimulationContext;