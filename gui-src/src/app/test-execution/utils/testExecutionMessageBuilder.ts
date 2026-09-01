import { TestRunExecution } from "@test-execution/types/TestRunExecution";
import { SupportedInput, TestStep } from "@test-execution/types/TestModel";
import { MONITOR, TESTING_SESSION_MANAGER } from "@home/utils/permissions";
import { UploadedInput } from "@test-execution/types/TestRun";

/**
 * Build the START_TEST_RUN message payload for test execution.
 * This is test-execution specific.
 */
export function buildStartTestRunMessage(
  execution: TestRunExecution,
  testSteps?: TestStep[],
  supportedInputs?: SupportedInput[],
  accessToken?: string,
  userId?: string,
  organizationGroupId?: string,
  uploadedInputs?: UploadedInput[],
): Record<string, unknown> {
  // Construct steps array from testModel.steps
  const steps =
    testSteps?.map((step) => ({
      name: step.name,
      type: step.type,
      properties: step.properties || [],
    })) || [];

  function getTypeFromUploadedInputId(uploadedInputId: string): string | null {
    const type = supportedInputs?.find((input) => input.id === uploadedInputId)?.type;

    switch (type) {
      case "TEXT":
        return "STRING";
      case "FILE":
        return "BYTE_ARRAY";
      case "BOOLEAN":
        return "BOOLEAN";
      default:
        throw new Error("Unsupported input type");
    }
  }

  // Start with persisted inputs from execution, then override with uploadedInputs
  const persistedInputs = execution.inputs ?? [];
  const uploadedInputIds = new Set(uploadedInputs?.map((input) => input.id) ?? []);

  // Include persisted inputs that were not replaced by new uploads
  const inputsFromPersisted = persistedInputs
    .filter((persisted) => !uploadedInputIds.has(persisted.name))
    .map((persisted) => ({
      name: persisted.name,
      type: persisted.type,
      value: persisted.value,
    }));

  // Include newly uploaded inputs
  const inputsFromUploaded =
    uploadedInputs?.map((uploadedInput) => ({
      name: uploadedInput.id,
      type: getTypeFromUploadedInputId(uploadedInput.id),
      value: uploadedInput.value,
    })) || [];

  // Merge: uploaded inputs override persisted ones
  const inputsPart = [...inputsFromPersisted, ...inputsFromUploaded];

  return {
    authorization: accessToken,
    type: "START_TEST_RUN",
    testRun: {
      accessControlList: {
        owners: userId ? [userId] : [],
        readers: [...(organizationGroupId ? [organizationGroupId] : []), MONITOR, TESTING_SESSION_MANAGER],
        isPublic: false,
      },
      systemsUnderTest: [],
      test: {
        id: execution.testId ?? "unknown-test",
        name: execution.testName ?? execution.testId ?? "Unknown Test",
        steps: steps,
      },
      inputs: inputsPart,
    },
    persist: true,
  };
}

// Build WebSocket URL for test execution
export function buildTestExecutionWebSocketUrl(baseUrl: string, executionId: string): string {
  return `${baseUrl}/v1/test-stream/run/${executionId}`;
}
