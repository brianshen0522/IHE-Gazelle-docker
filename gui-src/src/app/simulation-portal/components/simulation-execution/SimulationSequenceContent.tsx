"use client";
import { useEffect } from "react";
import { useParams } from "next/navigation";
import { Skeleton } from "@gazelle/gazelle-component-ui";
import ErrorMessage from "@shared/components/filter/ErrorMessage";
import { useGetSequenceById } from "@simulation-portal/hook/useGetSequenceById";
import SequenceRenderer from "@simulation-portal/components/simulation-execution/SequenceRenderer";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { useSimulationForm } from "@simulation-portal/context/SimulationFormContext";
import { Step } from "@maestro/types/execution/TestSuiteRun";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";

const SimulationSequenceContent = () => {
  const { sequenceId } = useParams<{ sequenceId: string }>();
  const id = decodeURIComponent(sequenceId);
  const { simulationSequence, getByIdLoading, getByIdError } = useGetSequenceById({ id: id });
  const { setSimulationSequence, setSimulationStep } = useSequenceExecutionContext();
  const { resetForm } = useSimulationForm();

  const initSimulationStep = (sequence: SimulationSequence): Step => {
    return {
      name: sequence.id,
      type: "SIMULATION",
      timeout: 180 * 1000,
      properties: [
        { name: "simulationService", value: sequence.simulatorName!, type: "STRING" },
        { name: "sequenceId", value: sequence.id, type: "STRING" },
      ],
    };
  };

  useEffect(() => {
    if (simulationSequence) {
      setSimulationSequence(simulationSequence);
      setSimulationStep(initSimulationStep(simulationSequence));
      resetForm();
    }

    return () => {
      // Cleanup when component unmounts or sequence changes
      setSimulationSequence({
        id: "",
        valid: false,
        simulatedRoles: [],
        testedRoles: [],
        supportedParameters: [],
      });
      setSimulationStep({
        name: "",
        type: "",
        properties: [],
      });
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [simulationSequence?.id]);

  if (getByIdLoading) {
    return (
      <div className="p-1">
        <Skeleton className="h-screen" />
      </div>
    );
  }

  if ((!simulationSequence && !getByIdLoading) || getByIdError) {
    return <ErrorMessage error={getByIdError ?? `Unable to find Simulation Sequence with id ${id}`} />;
  }

  return <SequenceRenderer />;
};

export default SimulationSequenceContent;
