import { StartTestRun } from "@maestro/types/message/WebSocketMessage";
import { GAZELLE_ADMIN, PREFIX_ORGANIZATION, PROJECT_ADMIN, TESTING_SESSION_MANAGER } from "@shared/types/GazelleRole";
import { MONITOR } from "@home/utils/permissions";
import { Step } from "@/shared/services/maestro/types/execution/TestSuiteRun";

export interface SimulationMessageOptions {
  gazelleId?: string;
  organization?: string;
}

export function buildStartSimulationMessage(sequenceId: string, simulationStep: Step, options: SimulationMessageOptions): StartTestRun {
  const owners = [GAZELLE_ADMIN];
  if (options.gazelleId) {
    owners.push(options.gazelleId);
  }

  const readers = [PROJECT_ADMIN, TESTING_SESSION_MANAGER, MONITOR];
  if (options.organization) {
    readers.push(`${PREFIX_ORGANIZATION}${options.organization}`);
  }

  return {
    type: "START_TEST_RUN",
    testRun: {
      test: {
        id: sequenceId,
        name: sequenceId,
        steps: [simulationStep],
      },
      accessControlList: {
        isPublic: false,
        owners,
        readers,
      },
    },
    persist: true,
  };
}
