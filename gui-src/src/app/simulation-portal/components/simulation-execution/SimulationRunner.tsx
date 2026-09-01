"use client";
import { useSession } from "next-auth/react";
import { Button } from "@gazelle/gazelle-component-ui";
import { useSimulationForm } from "@simulation-portal/context/SimulationFormContext";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import SimulationReportPreview from "@simulation-portal/components/simulation-execution/SimulationReportPreview";
import SimulationRunnerMessage from "@simulation-portal/components/simulation-execution/SimulationRunnerMessage";
import NoticeBanner from "@shared/components/banner/NoticeBanner";
import MaestroTestRunner from "@maestro/components/MaestroTestRunner";
import ErrorMessage from "@shared/components/filter/ErrorMessage";
import { buildStartSimulationMessage } from "@simulation-portal/service/simulationMessageBuilder";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";

const SimulationRunner = () => {
  const { data: session } = useSession();
  const gazelleId = session?.user?.gazelleId;
  const organization = session?.user?.organization;
  const {
    simulationSequence,
    simulationStep,
    isRunning,
    error,
    reason,
    execution,
    interactWithUser,
    completeUserInteraction,
    startSimulation,
    setStartEvent,
  } = useSequenceExecutionContext();
  const { validateForm } = useSimulationForm();
  const { setHasUnsavedChanges } = useUnsavedChanges();

  const isRunnable = simulationSequence.runnable && simulationSequence.valid;

  const handleRun = async () => {
    setHasUnsavedChanges(false);
    const isValid = await validateForm();
    if (isValid) {
      const startEvent = buildStartSimulationMessage(simulationSequence.id, simulationStep, { gazelleId, organization });
      setStartEvent(startEvent);
      startSimulation(startEvent);
    }
  };

  return (
    <>
      <div className="flex flex-col gap-2 m-2">
        <div className="flex justify-center">
          {isRunnable ? (
            <Button
              ariaLabel="Run"
              id="button"
              onClick={handleRun}
              title="Run"
              type="button"
              variant="primary"
              disabled={isRunning}
              className="rounded-lg"
            >
              {isRunning ? "Running..." : "Run"}
            </Button>
          ) : (
            <SimulationRunnerMessage />
          )}
        </div>
      </div>

      {isRunning && (
        <div className="flex justify-center items-start w-full">
          <NoticeBanner color="yellow" weight="normal" className="text-sm">
            {
              "The execution of the simulation sequence is in progress. Don't leave this page, it will automatically refresh once the report is available."
            }
          </NoticeBanner>
        </div>
      )}

      <MaestroTestRunner execution={execution} interactWithUser={interactWithUser} onComplete={completeUserInteraction} showExecutionLogs />

      {error && <ErrorMessage error={reason || "Unable to start the simulation. Please contact an administrator."} />}

      {!isRunning && !error && <SimulationReportPreview />}
    </>
  );
};

export default SimulationRunner;
