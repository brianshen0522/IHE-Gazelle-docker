import { useCallback, useEffect, useRef, useState } from "react";
import { SimulationWebSocketService } from "../service/SimulationWebSocketService";
import { InteractWithUser, StartTestRun, StartTestSuiteRun, WebSocketMessage } from "@maestro/types/message/WebSocketMessage";
import { TestSuiteExecution, ExecutionStatus } from "@maestro/types/execution/TestSuiteExecution";
import { TestRun, TestSuiteRun } from "@maestro/types/execution/TestSuiteRun";
import { TestReport } from "@maestro/types/report/TestReport";
import { WebSocketEvent } from "@/shared/services/websocket/WebSocketTypes";

export function useSimulationWebSocket(baseUrl?: string, accessToken?: string) {
  const serviceRef = useRef<SimulationWebSocketService | null>(null);

  // Connection state
  const [isConnected, setIsConnected] = useState(false);
  const [isRunning, setIsRunning] = useState(false);
  const [error, setError] = useState<boolean>(false);
  const [reason, setReason] = useState<string | undefined>(undefined);

  // Execution state (from TestExecutionContext)
  const [run, setRun] = useState<TestRun | TestSuiteRun | null>(null);
  const [execution, setExecution] = useState<TestSuiteExecution | null>(null);
  const [interactWithUser, setInteractWithUser] = useState<InteractWithUser | undefined>(undefined);
  const [location, setLocation] = useState<string | undefined>(undefined);
  const [testReport, setTestReport] = useState<TestReport | undefined>(undefined);
  const [isTestRun, setIsTestRun] = useState<boolean>(true);

  // Simulation specific state (from MaestroTestSocketContext)
  const [startEvent, setStartEvent] = useState<StartTestRun | StartTestSuiteRun | null>(null);

  // Initialize service once
  useEffect(() => {
    serviceRef.current ??= new SimulationWebSocketService();
    return () => {
      serviceRef.current?.disconnect();
      serviceRef.current = null;
    };
  }, []);

  // Subscribe to service events
  useEffect(() => {
    if (!serviceRef.current) return;
    const unsubscribe = serviceRef.current.subscribe((event: WebSocketEvent) => {
      handleWebSocketEvent(event);
    });
    return unsubscribe;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleWebSocketEvent = useCallback((event: WebSocketEvent) => {
    switch (event.type) {
      case "connected":
        setIsConnected(true);
        setError(false);
        setReason(undefined);
        break;

      case "disconnected":
        setIsConnected(false);
        setIsRunning(false);
        break;

      case "error":
        setIsConnected(false);
        setError(true);
        setReason(event.error?.message);
        setIsRunning(false);
        break;

      case "connectionFailed":
        setIsConnected(false);
        setIsRunning(false);
        setError(true);
        setReason(event.error?.message);
        break;

      case "reconnecting":
        // optionally expose reconnecting state if UI needs it
        break;

      case "message":
        handleMessage(event.data as WebSocketMessage);
        break;

      default:
        break;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleMessage = useCallback((message: WebSocketMessage) => {
    updateExecution(message);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Full port of TestExecutionContext.updateExecution
  const updateExecution = useCallback(
    (message: WebSocketMessage) => {
      const timestamp = new Date().toISOString();

      switch (message.type) {
        case "START_TEST_SUITE_RUN": {
          setIsTestRun(false);
          setIsRunning(true);
          const suite = message.testSuiteRun;
          setExecution({
            testSuiteRunId: suite.testSuite.id ?? "",
            startDateTime: timestamp,
            executionStatus: ExecutionStatus.RUNNING,
            tests: suite.tests?.map((test) => ({
              testId: test.id,
              executionStatus: ExecutionStatus.QUEUED,
              stepRuns: test.steps.map((step) => ({
                stepName: step.name,
                executionStatus: ExecutionStatus.QUEUED,
              })),
            })),
          });
          break;
        }

        case "START_TEST_RUN": {
          const run = message.testRun;
          setIsTestRun(true);
          setIsRunning(true);
          setExecution({
            testSuiteRunId: run.test.id ?? "",
            startDateTime: timestamp,
            executionStatus: ExecutionStatus.RUNNING,
            tests: [
              {
                testId: run.test.id,
                startDateTime: timestamp,
                executionStatus: ExecutionStatus.RUNNING,
                stepRuns: run.test.steps.map((step) => ({
                  stepName: step.name,
                  executionStatus: ExecutionStatus.QUEUED,
                })),
              },
            ],
          });
          break;
        }

        case "TEST_RUN_STARTED":
          setExecution((prev) => {
            if (!prev) return prev;
            const next = structuredClone(prev);
            const test = next.tests?.find((t) => t.testId === message.testId);
            if (test) {
              test.executionStatus = ExecutionStatus.RUNNING;
              test.startDateTime = message.dateTime;
            }
            return next;
          });
          break;

        case "STEP_RUN_STARTED":
          setExecution((prev) => {
            if (!prev) return prev;
            const next = structuredClone(prev);
            const test = next.tests?.find((t) => t.testId === message.testId);
            const step = test?.stepRuns?.[message.stepIndex];
            if (step) {
              step.executionStatus = ExecutionStatus.RUNNING;
              step.startDateTime = message.dateTime;
            }
            return next;
          });
          break;

        case "INTERACT_WITH_USER":
          setInteractWithUser({ ...message });
          break;

        case "USER_INTERACTION_COMPLETED":
          setInteractWithUser(undefined);
          break;

        case "STEP_RUN_FINISHED":
          setExecution((prev) => {
            if (!prev) return prev;
            const next = structuredClone(prev);
            const test = next.tests?.find((t) => t.testId === message.testId);
            const step = test?.stepRuns?.[message.stepIndex];
            if (step) {
              step.executionStatus = ExecutionStatus.FINISHED;
              step.result = message.result;
              step.finishDateTime = message.dateTime;
              next.unexpectedErrors = message.unexpectedErrors;
            }
            return next;
          });
          break;

        case "TEST_RUN_FINISHED":
          setExecution((prev) => {
            if (!prev) return prev;
            const next = structuredClone(prev);
            const test = next.tests?.find((t) => t.testId === message.testId);
            if (test) {
              test.executionStatus = ExecutionStatus.FINISHED;
              test.result = message.result;
              test.finishDateTime = message.dateTime;
              next.unexpectedErrors = message.unexpectedErrors;
            }
            return next;
          });
          break;

        case "EXECUTION_FINISHED":
          setExecution((prev) => {
            if (!prev) return prev;
            const next = structuredClone(prev);
            if (isTestRun) {
              const testRunReport = message.testReport.testRunReports[0];
              const test = next.tests?.[0];
              if (test) {
                test.executionStatus = ExecutionStatus.FINISHED;
                test.result = testRunReport.result;
                test.finishDateTime = testRunReport.dateTime;
                next.unexpectedErrors = testRunReport.unexpectedErrors;
              }
            }
            next.executionStatus = ExecutionStatus.FINISHED;
            next.result = message.testReport.result;
            next.finishDateTime = message.testReport.dateTime;
            next.unexpectedErrors = message.testReport.unexpectedErrors;
            return next;
          });
          setLocation(message.location);
          setTestReport(message.testReport);
          setIsRunning(false);
          break;

        default:
          break;
      }
    },
    [isTestRun],
  );

  const connect = useCallback(() => {
    if (!baseUrl || !accessToken) {
      setError(true);
      setReason("Missing WebSocket configuration (baseUrl or accessToken)");
      return;
    }
    serviceRef.current?.connect({ baseUrl, accessToken });
  }, [baseUrl, accessToken]);

  const disconnect = useCallback(() => {
    serviceRef.current?.disconnect();
    setIsConnected(false);
  }, []);

  const startSimulation = useCallback(
    (event?: StartTestRun | StartTestSuiteRun) => {
      const eventToUse = event || startEvent;

      if (!eventToUse) {
        console.warn("[Simulation] Cannot start: no start event set");
        return;
      }

      setError(false);
      setReason(undefined);

      if (!serviceRef.current?.isConnected()) {
        connect();
        setTimeout(() => {
          if (serviceRef.current?.isConnected()) {
            setIsRunning(true);
            serviceRef.current.sendWithAuth(eventToUse as unknown as Record<string, unknown>);
            updateExecution(eventToUse);
          }
        }, 100);
        return;
      }

      setIsRunning(true);
      serviceRef.current.sendWithAuth(eventToUse as unknown as Record<string, unknown>);
      updateExecution(eventToUse);
    },
    [startEvent, connect, updateExecution],
  );

  const completeUserInteraction = useCallback(() => {
    serviceRef.current?.sendWithAuth({ type: "USER_INTERACTION_COMPLETED" });
  }, []);

  return {
    // connection
    isConnected,
    isRunning,
    error,
    reason,
    connect,
    disconnect,
    // simulation
    startEvent,
    setStartEvent,
    startSimulation,
    // execution state
    run,
    setRun,
    execution,
    setExecution,
    testReport,
    location,
    // user interaction
    interactWithUser,
    completeUserInteraction,
  };
}
