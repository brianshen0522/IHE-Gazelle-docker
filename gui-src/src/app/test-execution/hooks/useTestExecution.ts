import { useEffect, useRef, useState } from "react";
import { useTestRunExecutionWebSocket } from "./useTestRunExecutionWebSocket";
import { TestStep, SupportedInput } from "../types/TestModel";
import { UploadedInput } from "../types/TestRun";
import { useGetTestRunExecutions } from "./SWR/useGetTestRunExecutions";

interface UseTestExecutionParams {
  executionId: string;
  wsUrl: string | undefined;
  accessToken: string | undefined;
  userInfo: {
    userId: string | undefined;
    organizationGroupId: string | undefined;
  };
  testSteps: TestStep[] | undefined;
  supportedInputs: SupportedInput[] | undefined;
  uploadedInputs: UploadedInput[];
  onUnsavedChange?: (hasChanges: boolean) => void;
}

// Hook to manage test execution via WebSocket
// Handles connection, execution state, and completion detection
export function useTestExecution({
  executionId,
  wsUrl,
  accessToken,
  userInfo,
  testSteps,
  supportedInputs,
  uploadedInputs,
  onUnsavedChange,
}: UseTestExecutionParams) {
  const wasRunningRef = useRef(false);
  const [isCompletionModalOpen, setIsCompletionModalOpen] = useState(false);

  const { execution, setExecution, isRunning, interactWithUser, disconnect, startExecution, completeUserInteraction } = useTestRunExecutionWebSocket(
    executionId,
    wsUrl,
    accessToken,
    {
      userId: userInfo.userId,
      organizationGroupId: userInfo.organizationGroupId,
    },
    testSteps,
    supportedInputs,
    uploadedInputs,
  );
  const { mutate } = useGetTestRunExecutions(executionId);

  // Detect execution completion and show modal
  useEffect(() => {
    if (!wasRunningRef.current || isRunning || !execution) {
      wasRunningRef.current = isRunning;
      return;
    }

    if (execution.status !== "PASSED" && execution.status !== "FAILED" && execution.status !== "UNDEFINED") {
      wasRunningRef.current = isRunning;
      return;
    }

    setIsCompletionModalOpen(true);
    onUnsavedChange?.(false);

    mutate();

    wasRunningRef.current = isRunning;
  }, [isRunning, execution, onUnsavedChange, mutate, setExecution]);

  // Cleanup WebSocket on unmount
  useEffect(() => {
    return () => {
      disconnect();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCloseModal = () => {
    setIsCompletionModalOpen(false);
  };

  return {
    execution,
    setExecution,
    isRunning,
    interactWithUser,
    startExecution,
    disconnect,
    completeUserInteraction,
    isCompletionModalOpen,
    handleCloseModal,
  };
}
