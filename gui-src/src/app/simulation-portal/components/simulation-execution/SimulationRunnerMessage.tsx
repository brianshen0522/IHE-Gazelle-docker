import React from "react";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import ChevronCard from "@shared/components/boxes/ChevronCard";
import NoticeBanner from "@shared/components/banner/NoticeBanner";

const SimulationRunnerMessage = () => {
  const {simulationSequence} = useSequenceExecutionContext();

  if (simulationSequence.valid) {
    return (
      <NoticeBanner color="blue" weight="semibold">
        This sequence cannot be run from the simulation portal. Please follow the description to interact with the
        simulator.
      </NoticeBanner>
    );
  }

  return (
    <ChevronCard title="Simulation Sequence errors">
      <NoticeBanner color="red" weight="semibold">
        This Simulation sequence is invalid and cannot be executed for the following reason :
        {simulationSequence.validReportMessage}
      </NoticeBanner>
    </ChevronCard>
  )
}

export default SimulationRunnerMessage;