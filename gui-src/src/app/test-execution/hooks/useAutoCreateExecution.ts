import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import { Route } from "next";
import { createTestRunExecution } from "../components/test-run/actions";
import { TestModel } from "../types/TestModel";

interface UseAutoCreateExecutionParams {
  useTestModelFlow: boolean;
  testModel: TestModel;
  executionId: string;
  testId: string;
  testSessionId: string | null;
}

/**
 * Temporary hook to automatically create a test run execution when navigating from test suite
 * Handles partner detection logic and execution creation flow
 *
 * Note: Server-side checks for existing executions before rendering to prevent duplicates
 * and avoid URL flashing. This hook only creates new executions when needed.
 *
 * @todo Refactor once we have real partner detection logic based on test model properties
 */
export function useAutoCreateExecution({ useTestModelFlow, testModel, executionId, testId, testSessionId }: UseAutoCreateExecutionParams) {
  const router = useRouter();
  const [isCreatingExecution, setIsCreatingExecution] = useState(false);
  const hasCreatedExecutionRef = useRef(false);

  useEffect(() => {
    // Only run in test model flow
    if (!useTestModelFlow || !testModel || isCreatingExecution || executionId || hasCreatedExecutionRef.current) {
      return;
    }

    // Partner detection: For now, we assume no partner selection is needed
    // In the future, this can check testModel.testRoles or other properties
    const needsPartnerSelection = false;

    if (!needsPartnerSelection) {
      // Mark that we're creating to prevent duplicate calls
      hasCreatedExecutionRef.current = true;

      if (!testId || !testSessionId) throw new Error("Missing testId or testSessionId for execution creation");

      // Automatically create the test run execution
      const createExecution = async () => {
        setIsCreatingExecution(true);
        try {
          const result = await createTestRunExecution({
            testId,
            testSessionId,
            testSpecification: JSON.stringify(testModel),
          });

          if (!result.success) {
            throw new Error(result.error || "Failed to create test run execution");
          }

          const newExecutionId = result.data?.id || result.data?.testRunExecutionId;

          if (!newExecutionId) {
            throw new Error("No execution ID returned from server");
          }

          // Update URL with execution ID only
          router.replace(`/test-execution/test-run-execution?executionId=${newExecutionId}` as Route);

          toast.success("Test execution created successfully");
        } catch (error) {
          console.error("Error creating test run execution:", error);
          toast.error(error instanceof Error ? error.message : "Failed to create test run execution");
        } finally {
          setIsCreatingExecution(false);
        }
      };

      createExecution();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [useTestModelFlow, testModel, isCreatingExecution, executionId, testId, testSessionId]);

  return { isCreatingExecution };
}
