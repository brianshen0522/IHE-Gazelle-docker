import { WebSocketService } from "@shared/services/websocket/WebSocketService";
import { buildStartSimulationMessage, SimulationMessageOptions } from "./simulationMessageBuilder";
import { BaseWebSocketConfig } from "@/shared/services/websocket/WebSocketTypes";
import { Step } from "@/shared/services/maestro/types/execution/TestSuiteRun";

type SimulationWebSocketConfig = BaseWebSocketConfig;

export class SimulationWebSocketService extends WebSocketService<SimulationWebSocketConfig> {
  protected buildUrl(config: SimulationWebSocketConfig): string {
    return `${config.baseUrl}/v1/test-stream/run`;
  }

  startSimulation(sequenceId: string, simulationStep: Step, options: SimulationMessageOptions): boolean {
    if (!this.config) {
      console.error("[SimulationWebSocketService] WebSocket not configured");
      return false;
    }

    const message = buildStartSimulationMessage(sequenceId, simulationStep, options);

    return this.send(message as unknown as Record<string, unknown>);
  }
}
