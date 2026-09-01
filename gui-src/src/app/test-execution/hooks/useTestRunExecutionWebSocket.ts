import { useCallback, useEffect, useRef, useState } from "react";
import { TestRunExecution } from "../types/TestRunExecution";
import { SupportedInput, TestStep } from "../types/TestModel";
import { TestExecutionWebSocketConfig, TestExecutionWebSocketService } from "../services/TestExecutionWebSocketService";
import { TestExecutionMessageHandler } from "../services/TestExecutionMessageHandler";
import { UploadedInput } from "@test-execution/types/TestRun";
import { InteractWithUser, WebSocketMessage } from "@maestro/types/message/WebSocketMessage";
import { WebSocketEvent } from "@/shared/services/websocket/WebSocketTypes";

export interface TestRunUserContext {
  userId?: string;
  organizationGroupId?: string;
}

export function useTestRunExecutionWebSocket(
  executionId: string,
  baseUrl?: string,
  accessToken?: string,
  userContext?: TestRunUserContext,
  testSteps?: TestStep[],
  supportedInputs?: SupportedInput[],
  uploadedInputs?: UploadedInput[],
) {
  const serviceRef = useRef<TestExecutionWebSocketService | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [isRunning, setIsRunning] = useState(false);
  const [execution, setExecution] = useState<TestRunExecution | null>(null);
  const [interactWithUser, setInteractWithUser] = useState<InteractWithUser | undefined>(undefined);

  // Store test model data in refs to avoid re-renders
  const testStepsRef = useRef(testSteps);
  const supportedInputsRef = useRef(supportedInputs);
  const uploadedInputsRef = useRef(uploadedInputs);

  // Track pending start request to execute when "connected" event fires
  const pendingStartRef = useRef(false);
  // Store the execution to start (with updated status) for pending start
  const executionToStartRef = useRef<TestRunExecution | null>(null);
  // Store unsubscribe function to properly cleanup
  const unsubscribeRef = useRef<(() => void) | null>(null);

  // Update refs when props change
  useEffect(() => {
    testStepsRef.current = testSteps;
    supportedInputsRef.current = supportedInputs;
    uploadedInputsRef.current = uploadedInputs;
  }, [testSteps, supportedInputs, uploadedInputs]);

  // Initialize service once
  useEffect(() => {
    serviceRef.current ??= new TestExecutionWebSocketService();

    return () => {
      serviceRef.current?.disconnect();
      serviceRef.current = null;
    };
  }, []);

  // Subscribe to service events
  useEffect(() => {
    if (!serviceRef.current) return;

    unsubscribeRef.current = serviceRef.current.subscribe((event: WebSocketEvent) => {
      handleWebSocketEvent(event);
    });

    return () => {
      unsubscribeRef.current?.();
      unsubscribeRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [execution]); // Re-subscribe when execution changes to capture latest state

  // Auto-disconnect fallback when execution finishes (e.g. STOPPED status set externally)
  useEffect(() => {
    if (!isRunning && execution?.status && ["COMPLETED", "FAILED", "PASSED", "UNDEFINED"].includes(execution.status)) {
      pendingStartRef.current = false;
      executionToStartRef.current = null;
      unsubscribeRef.current?.();
      unsubscribeRef.current = null;
      serviceRef.current?.disconnect();
      setIsConnected(false);
    }
  }, [isRunning, execution?.status]);

  const handleWebSocketEvent = useCallback(
    (event: WebSocketEvent) => {
      const timestamp = new Date().toISOString();

      switch (event.type) {
        case "connected":
          setIsConnected(true);
          addLog({
            type: "CONNECTED",
            dateTime: timestamp,
            message: "WebSocket connected successfully",
            icon: "Check",
          });

          // If there's a pending start request, execute it now
          if (pendingStartRef.current && executionToStartRef.current) {
            pendingStartRef.current = false;

            const execToStart = executionToStartRef.current;
            executionToStartRef.current = null;

            setIsRunning(true);
            addLog({
              type: "TEST_STARTED",
              dateTime: new Date().toISOString(),
              message: "Sending start execution command...",
              icon: "Play",
            });

            serviceRef.current?.startTestRun(
              execToStart,
              testStepsRef.current,
              supportedInputsRef.current,
              userContext?.userId,
              userContext?.organizationGroupId,
              uploadedInputsRef.current,
            );
          }
          break;

        case "disconnected": {
          setIsConnected(false);
          setIsRunning(false);
          const disconnectData = event.data as { code: number; reason: string; wasClean: boolean } | undefined;

          if (disconnectData?.code === 1008 || disconnectData?.code === 1011) {
            // Mark execution as failed and clear pending start on non-retryable error codes
            pendingStartRef.current = false;
            executionToStartRef.current = null;
            setExecution((prev) => {
              if (!prev) return prev;
              return { ...prev, status: "FAILED", completedAt: timestamp };
            });
            addLog({
              type: "CONNECTION_CLOSED_UNEXPECTEDLY",
              dateTime: timestamp,
              message: `WebSocket connection closed (code: ${disconnectData.code}, reason: ${disconnectData.reason || "No reason"})`,
              icon: "TriangleAlert",
            });
          } else {
            addLog({
              type: disconnectData?.wasClean ? "CONNECTION_CLOSED" : "CONNECTION_CLOSED_UNEXPECTEDLY",
              dateTime: timestamp,
              message: `WebSocket connection closed (code: ${disconnectData?.code}, reason: ${disconnectData?.reason || "No reason"})`,
              icon: disconnectData?.wasClean ? "Unplug" : "TriangleAlert",
            });
          }
          break;
        }

        case "error":
          setIsConnected(false);
          addLog({
            type: "CONNECTION_ERROR",
            dateTime: timestamp,
            message: event.error?.message || "WebSocket connection error",
            icon: "X",
          });
          break;

        case "connectionFailed": {
          setIsRunning(false);
          pendingStartRef.current = false;
          executionToStartRef.current = null;
          const failedData = event.data as { code?: number; reason?: string } | undefined;

          if (failedData?.code === 1008 || failedData?.code === 1011) {
            setExecution((prev) => {
              if (!prev) return prev;
              return { ...prev, status: "FAILED", completedAt: timestamp };
            });
          }

          addLog({
            type: "CONNECTION_FAILED",
            dateTime: timestamp,
            message: event.error?.message || "WebSocket connection failed",
            icon: "X",
          });
          break;
        }

        case "reconnecting":
          addLog({
            type: "RECONNECTING",
            dateTime: timestamp,
            message: `Attempting to reconnect...`,
            icon: "PlugZap",
          });
          break;

        case "message": {
          const message = event.data as WebSocketMessage;
          handleMessage(message);
          break;
        }

        default:
          break;
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [execution],
  );

  const handleMessage = useCallback(
    (message: WebSocketMessage) => {
      // Handle user interaction messages
      if (message.type === "INTERACT_WITH_USER") {
        setInteractWithUser({ ...message });
        return;
      }

      if (message.type === "USER_INTERACTION_COMPLETED") {
        setInteractWithUser(undefined);
        return;
      }

      // Handle specific message types that affect running state
      if (message.type === "EXECUTION_FINISHED" || message.type === "TEST_REPORT") {
        setIsRunning(false);

        // Disconnect immediately and synchronously before the server closes the connection
        // This prevents the service from triggering auto-reconnect when the server closes
        pendingStartRef.current = false;
        executionToStartRef.current = null;
        unsubscribeRef.current?.();
        unsubscribeRef.current = null;
        serviceRef.current?.disconnect();
        setIsConnected(false);
      }

      // Process message and get state updates
      setExecution((prev) => {
        if (!prev) return prev;
        const stateUpdates = TestExecutionMessageHandler.handleMessage(message, prev);
        return stateUpdates ? { ...prev, ...stateUpdates } : prev;
      });

      // Extract and add log message
      const logMessage = TestExecutionMessageHandler.extractLogMessage(message);
      if (logMessage) {
        if (typeof logMessage === "string") {
          addLog(`[${new Date().toISOString()}] ${logMessage}`);
        } else {
          addLog(logMessage);
        }
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [execution],
  );

  const addLog = useCallback((logEntry: string | Record<string, unknown>) => {
    setExecution((prev) => {
      if (!prev) return prev;
      const logObj = typeof logEntry === "string" ? { message: logEntry } : logEntry;
      return { ...prev, logs: [...(prev.logs || []), logObj] };
    });
  }, []);

  const connect = useCallback(() => {
    if (!baseUrl || !executionId || !accessToken) {
      console.error("Missing required WebSocket configuration");
      addLog({
        type: "CONNECTION_FAILED",
        dateTime: new Date().toISOString(),
        message: "Missing WebSocket configuration (baseUrl, executionId, or accessToken)",
        icon: "X",
      });
      return;
    }

    addLog({
      type: "CONNECTING",
      dateTime: new Date().toISOString(),
      message: "Attempting to connect to WebSocket...",
      icon: "PlugZap",
    });

    const config: TestExecutionWebSocketConfig = { baseUrl, executionId, accessToken };
    serviceRef.current?.connect(config);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [baseUrl, executionId, accessToken]);

  const disconnect = useCallback(() => {
    pendingStartRef.current = false;
    executionToStartRef.current = null;
    serviceRef.current?.disconnect();
    setIsConnected(false);
  }, []);

  const startExecution = useCallback(() => {
    if (!execution) {
      return;
    }

    // Prevent duplicate starts
    if (isRunning) {
      return;
    }

    // Update UI state to show execution is starting
    setExecution((prev) => {
      if (!prev) return prev;

      // Preserve existing test report summaries from previous runs
      const preservedSummaries = prev.testReportSummaries || [];

      return {
        ...prev,
        status: "RUNNING" as const,
        startedAt: new Date().toISOString(),
        completedAt: undefined,
        // Clear previous execution results when re-running
        liveStepResults: [],
        testReport: undefined,
        logs: [],
        // Keep the test report summaries history
        testReportSummaries: preservedSummaries,
      };
    });

    // Check if WebSocket is connected
    if (!serviceRef.current?.isConnected()) {
      // Store original execution for pending start
      executionToStartRef.current = execution;
      pendingStartRef.current = true;
      connect();
      return;
    }

    setIsRunning(true);
    addLog({
      type: "TEST_STARTED",
      dateTime: new Date().toISOString(),
      message: "Sending start execution command...",
      icon: "Play",
    });

    serviceRef.current.startTestRun(
      execution,
      testStepsRef.current,
      supportedInputsRef.current,
      userContext?.userId,
      userContext?.organizationGroupId,
      uploadedInputsRef.current,
    );
  }, [connect, execution, isRunning, userContext, addLog]);

  const completeUserInteraction = useCallback(() => {
    if (!serviceRef.current?.isConnected()) {
      console.warn("Cannot complete user interaction: WebSocket not connected");
      return;
    }
    serviceRef.current.sendWithAuth({ type: "USER_INTERACTION_COMPLETED" });
    setInteractWithUser(undefined);
  }, []);

  return {
    isConnected,
    isRunning,
    execution,
    setExecution,
    interactWithUser,
    connect,
    disconnect,
    startExecution,
    completeUserInteraction,
  };
}
