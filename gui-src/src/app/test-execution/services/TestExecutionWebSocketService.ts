import { WebSocketService } from "@shared/services/websocket/WebSocketService";
import { buildStartTestRunMessage, buildTestExecutionWebSocketUrl } from "../utils/testExecutionMessageBuilder";
import { TestRunExecution } from "../types/TestRunExecution";
import { SupportedInput, TestStep } from "../types/TestModel";
import { UploadedInput } from "@test-execution/types/TestRun";
import { BaseWebSocketConfig } from "@/shared/services/websocket/WebSocketTypes";

export interface TestExecutionWebSocketConfig extends BaseWebSocketConfig {
  executionId: string;
}

export class TestExecutionWebSocketService extends WebSocketService<TestExecutionWebSocketConfig> {
  protected buildUrl(config: TestExecutionWebSocketConfig): string {
    return buildTestExecutionWebSocketUrl(config.baseUrl, config.executionId);
  }

  protected validateConfig(config: TestExecutionWebSocketConfig): boolean {
    if (!super.validateConfig(config) || !config.executionId) {
      console.error("[TestExecutionWebSocketService] Missing required config: executionId");
      return false;
    }
    return true;
  }

  startTestRun(
    execution: TestRunExecution,
    testSteps?: TestStep[],
    supportedInputs?: SupportedInput[],
    userId?: string,
    organizationGroupId?: string,
    uploadedInputs?: UploadedInput[],
  ): boolean {
    if (!this.config) {
      console.error("[TestExecutionWebSocketService] WebSocket not configured");
      return false;
    }

    const message = buildStartTestRunMessage(
      execution,
      testSteps,
      supportedInputs,
      this.config.accessToken,
      userId,
      organizationGroupId,
      uploadedInputs,
    );

    return this.send(message);
  }
}
