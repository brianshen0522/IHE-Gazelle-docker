import { describe, expect, it } from "vitest";
import { getStepResult } from "../getStepResult";
import { StepRunReport } from "@/app/test-execution/types/TestRunExecution";

function createStep(result: StepRunReport["result"]): StepRunReport {
  return {
    stepName: "step",
    type: "ACTION",
    dateTime: new Date().toISOString(),
    result,
  };
}

describe("getStepResult", () => {
  it("return RUNNING during execution", () => {
    const executionResult = createStep("RUNNING");

    const result = getStepResult({
      stepRunReports: [executionResult],
      index: 0,
      executionResult,
    });

    expect(result).toBe("RUNNING");
  });

  it("return NOT_STARTED if step failed", () => {
    const stepRunReports = [createStep("FAILED")];

    const result = getStepResult({
      stepRunReports,
      index: 1,
      executionResult: undefined,
    });

    expect(result).toBe("NOT_STARTED");
  });

  it("return NOT_STARTED if not result step", () => {
    const result = getStepResult({
      stepRunReports: [],
      index: 0,
      executionResult: undefined,
    });

    expect(result).toBe("NOT_STARTED");
  });
});
