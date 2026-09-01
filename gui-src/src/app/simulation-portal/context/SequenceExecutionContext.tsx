"use client";

import React, { createContext, useCallback, useContext, useMemo, useState } from "react";
import { useSession } from "next-auth/react";
import { useEnv } from "@hooks/useEnv";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";
import { addOrUpdateProperty, createProperty, PropertyType, Step } from "@maestro/types/execution/TestSuiteRun";
import { SimulationReport } from "@simulation-portal/types/SimulationReport";
import { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";
import { useSimulationWebSocket } from "@simulation-portal/hook/useSimulationWebSocket";
import { StartTestRun, StartTestSuiteRun, InteractWithUser } from "@maestro/types/message/WebSocketMessage";
import { TestSuiteExecution } from "@maestro/types/execution/TestSuiteExecution";
import { TestReport } from "@maestro/types/report/TestReport";

type SequenceExecutionContextType = {
  simulationSequence: SimulationSequence;
  setSimulationSequence: (simulationSequence: SimulationSequence) => void;
  simulationStep: Step;
  setSimulationStep: (simulationStep: Step) => void;
  updatePropertyValue: (parameterName: string, newValue: string, propertyType: PropertyType) => void;
  simulationReportItem?: DatahouseItem;
  setReportItem: (reportItem: DatahouseItem) => void;
  simulationReport?: SimulationReport;
  // WebSocket execution state
  isRunning: boolean;
  error: boolean;
  reason: string | undefined;
  execution: TestSuiteExecution | null;
  testReport: TestReport | undefined;
  location: string | undefined;
  interactWithUser: InteractWithUser | undefined;
  // WebSocket actions
  startEvent: StartTestRun | StartTestSuiteRun | null;
  setStartEvent: React.Dispatch<React.SetStateAction<StartTestRun | StartTestSuiteRun | null>>;
  startSimulation: (event?: StartTestRun | StartTestSuiteRun) => void;
  completeUserInteraction: () => void;
};

export const SequenceExecutionContext = createContext<SequenceExecutionContextType>({
  simulationSequence: {
    id: "",
    valid: false,
    simulatedRoles: [],
    testedRoles: [],
    supportedParameters: [],
  },
  setSimulationSequence: () => {},
  simulationStep: {
    name: "",
    type: "",
    properties: [],
  },
  setSimulationStep: () => {},
  updatePropertyValue: () => {},
  simulationReportItem: undefined,
  setReportItem: () => {},
  simulationReport: undefined,
  isRunning: false,
  error: false,
  reason: undefined,
  execution: null,
  testReport: undefined,
  location: undefined,
  interactWithUser: undefined,
  startEvent: null,
  setStartEvent: () => {},
  startSimulation: () => {},
  completeUserInteraction: () => {},
});

export function SequenceExecutionContextProvider({ children }: Readonly<{ children: React.ReactNode }>) {
  const { env } = useEnv();
  const { data: session } = useSession();

  const [simulationSequence, setSimulationSequence] = useState<SimulationSequence>({
    id: "",
    valid: false,
    simulatedRoles: [],
    testedRoles: [],
    supportedParameters: [],
  });
  const [simulationStep, setSimulationStep] = useState<Step>({
    name: "",
    type: "",
    properties: [],
  });
  const [simulationReportItem, setSimulationReportItem] = useState<DatahouseItem | undefined>(undefined);
  const [simulationReport, setSimulationReport] = useState<SimulationReport | undefined>(undefined);

  // Use WebSocket hook directly in the context
  const {
    isRunning,
    error,
    reason,
    execution,
    testReport,
    location,
    interactWithUser,
    startEvent,
    setStartEvent,
    startSimulation,
    completeUserInteraction,
  } = useSimulationWebSocket(env?.GZL_MAESTRO_WEBSOCKET_URL, session?.access_token);

  function updatePropertyValue(propertyName: string, newValue: string, propertyType: PropertyType) {
    setSimulationStep((prev) => {
      const updated = addOrUpdateProperty(prev, createProperty(propertyName, newValue, propertyType));
      return { ...updated };
    });
  }

  const setReportItem = useCallback((item: DatahouseItem) => {
    setSimulationReportItem(item);
    // item.content is already parsed by server action with parseContent: true
    setSimulationReport(item.content as SimulationReport);
  }, []);

  const value = useMemo(
    () => ({
      simulationSequence,
      setSimulationSequence,
      simulationStep,
      setSimulationStep,
      updatePropertyValue,
      simulationReport,
      simulationReportItem,
      setReportItem,
      isRunning,
      error,
      reason,
      execution,
      testReport,
      location,
      interactWithUser,
      startEvent,
      setStartEvent,
      startSimulation,
      completeUserInteraction,
    }),
    [
      simulationSequence,
      simulationStep,
      simulationReport,
      simulationReportItem,
      setReportItem,
      isRunning,
      error,
      reason,
      execution,
      testReport,
      location,
      interactWithUser,
      startEvent,
      setStartEvent,
      startSimulation,
      completeUserInteraction,
    ],
  );

  return <SequenceExecutionContext.Provider value={value}>{children}</SequenceExecutionContext.Provider>;
}

export function useSequenceExecutionContext(): SequenceExecutionContextType {
  const context = useContext(SequenceExecutionContext);
  if (!context) {
    throw new Error("useSequenceExecutionContext must be used within a SequenceExecutionContextProvider");
  }
  return context;
}
