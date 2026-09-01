import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import DiagramSequence from "../DiagramSequence";
import { TestStep } from "@/app/test-execution/types/TestModel";
import { TestRunExecution } from "@/app/test-execution/types/TestRunExecution";

vi.mock("lucide-react", async (importOriginal) => {
  const actual = await importOriginal<typeof import("lucide-react")>();
  return {
    ...actual,
    Cog: () => <svg aria-label="cog" />,
    Settings: ({ className }: { className?: string }) => <svg aria-label="settings" className={className} />,
    CircleCheck: ({ className }: { className?: string }) => <svg aria-label="circle-check" className={className} />,
    CircleX: ({ className }: { className?: string }) => <svg aria-label="circle-x" className={className} />,
    CircleSlash: ({ className }: { className?: string }) => <svg aria-label="circle-slash" className={className} />,
  };
});

vi.mock("@/shared/services/maestro/utils/logStyles", () => ({
  getResultColor: (result?: string) => `color-${result ?? "none"}`,
}));

vi.mock("@/app/test-execution/components/test-run/test-step/TestStepOutputs", () => ({
  default: ({ inline }: { inline?: boolean }) => <div aria-label={inline ? "outputs-inline" : "outputs-block"} />,
}));

vi.mock("@/app/test-execution/components/test-run/diagram/diagram-element/InstructionDiagram", () => ({
  default: ({ name, description }: { name: string; description?: string }) => (
    <div>
      {name}
      {description && <span>{description}</span>}
    </div>
  ),
}));

const makeExecution = (stepRunReports: { stepName: string; result?: string }[] = []): TestRunExecution =>
  ({
    testReport: {
      testRunReports: [{ stepRunReports }],
    },
  }) as unknown as TestRunExecution;

const makeStep = (name: string, type: string): TestStep => ({ name, type }) as unknown as TestStep;

describe("DiagramSequence", () => {
  it("renders the test engine header", () => {
    render(<DiagramSequence execution={makeExecution()} />);

    expect(screen.getByLabelText("cog")).toBeInTheDocument();
    expect(screen.getByText("Test engine")).toBeInTheDocument();
  });

  it("renders nothing extra when testSteps is undefined", () => {
    render(<DiagramSequence execution={makeExecution()} />);

    expect(screen.queryByLabelText("settings")).not.toBeInTheDocument();
  });

  it("renders nothing extra when testSteps is empty", () => {
    render(<DiagramSequence execution={makeExecution()} testSteps={[]} />);

    expect(screen.queryByLabelText("settings")).not.toBeInTheDocument();
  });

  it("renders a USER_INTERACTION step via InstructionDiagram", () => {
    const steps = [makeStep("Do the thing", "USER_INTERACTION")];
    render(<DiagramSequence execution={makeExecution()} testSteps={steps} />);

    expect(screen.queryByText("1. Do the thing")).not.toBeInTheDocument(); // USER_INTERACTION steps don't show numbered labels
    expect(screen.queryByLabelText("settings")).not.toBeInTheDocument();
  });

  it("renders a non-INSTRUCTION step with its type label and inline outputs", () => {
    const steps = [makeStep("Step A", "ACTION")];
    render(<DiagramSequence execution={makeExecution()} testSteps={steps} />);

    expect(screen.getByLabelText("settings")).toBeInTheDocument();
    expect(screen.getByText("1. Step A")).toBeInTheDocument();
    expect(screen.getByLabelText("outputs-inline")).toBeInTheDocument();
  });

  it("applies the result color class to the Settings icon", () => {
    const steps = [makeStep("Step A", "ACTION")];
    const execution = makeExecution([{ stepName: "Step A", result: "PASS" }]);
    render(<DiagramSequence execution={execution} testSteps={steps} />);

    expect(screen.getByLabelText("settings")).toHaveClass("color-PASS");
  });

  it("applies fallback color when no matching stepRunReport exists", () => {
    const steps = [makeStep("Step A", "ACTION")];
    const execution = makeExecution([{ stepName: "Other Step" }]);
    render(<DiagramSequence execution={execution} testSteps={steps} />);

    expect(screen.getByLabelText("settings")).toHaveClass("color-none");
  });

  it("does not render a divider before the first step", () => {
    const steps = [makeStep("Step A", "ACTION")];
    render(<DiagramSequence execution={makeExecution()} testSteps={steps} />);

    expect(document.querySelector(".border-l.border-dashed")).not.toBeInTheDocument();
  });

  it("renders dividers between steps but not before the first", () => {
    const steps = [makeStep("Step A", "ACTION"), makeStep("Step B", "ACTION"), makeStep("Step C", "ACTION")];
    render(<DiagramSequence execution={makeExecution()} testSteps={steps} />);

    expect(document.querySelectorAll(".border-l.border-dashed")).toHaveLength(2);
  });

  it("renders mixed USER_INTERACTION and non-USER_INTERACTION steps correctly", () => {
    const steps = [makeStep("Step A", "ACTION"), makeStep("Do the thing", "USER_INTERACTION"), makeStep("Step C", "ACTION")];
    render(<DiagramSequence execution={makeExecution()} testSteps={steps} />);

    expect(screen.getAllByLabelText("settings")).toHaveLength(2);
    expect(screen.getByText("Do the thing")).toBeInTheDocument();
  });

  it("handles missing testRunReports gracefully", () => {
    const execution = { testReport: {} } as unknown as TestRunExecution;
    const steps = [makeStep("Step A", "ACTION")];

    expect(() => render(<DiagramSequence execution={execution} testSteps={steps} />)).not.toThrow();
  });

  it("renders CircleCheck icon when step result is PASSED", () => {
    const steps = [makeStep("Step A", "ACTION")];
    const execution = makeExecution([{ stepName: "Step A", result: "PASSED" }]);
    render(<DiagramSequence execution={execution} testSteps={steps} />);

    expect(screen.getByLabelText("circle-check")).toBeInTheDocument();
    expect(screen.getByLabelText("circle-check")).toHaveClass("color-PASSED");
    expect(screen.queryByLabelText("circle-x")).not.toBeInTheDocument();
  });

  it("renders CircleX icon when step result is FAILED", () => {
    const steps = [makeStep("Step A", "ACTION")];
    const execution = makeExecution([{ stepName: "Step A", result: "FAILED" }]);
    render(<DiagramSequence execution={execution} testSteps={steps} />);

    expect(screen.getByLabelText("circle-x")).toBeInTheDocument();
    expect(screen.getByLabelText("circle-x")).toHaveClass("color-FAILED");
    expect(screen.queryByLabelText("circle-check")).not.toBeInTheDocument();
  });

  it("renders CircleSlash icon when step result is UNDEFINED", () => {
    const steps = [makeStep("Step A", "ACTION")];
    const execution = makeExecution([{ stepName: "Step A", result: "UNDEFINED" }]);
    render(<DiagramSequence execution={execution} testSteps={steps} />);

    expect(screen.getByLabelText("circle-slash")).toBeInTheDocument();
    expect(screen.getByLabelText("circle-slash")).toHaveClass("color-UNDEFINED");
    expect(screen.queryByLabelText("circle-check")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("circle-x")).not.toBeInTheDocument();
  });

  it("does not render CircleCheck, CircleX, or CircleSlash when result is neither PASSED, FAILED, nor UNDEFINED", () => {
    const steps = [makeStep("Step A", "ACTION")];
    const execution = makeExecution([{ stepName: "Step A", result: "RUNNING" }]);
    render(<DiagramSequence execution={execution} testSteps={steps} />);

    expect(screen.queryByLabelText("circle-check")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("circle-x")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("circle-slash")).not.toBeInTheDocument();
  });

  it("does not render CircleCheck or CircleX when no execution result exists", () => {
    const steps = [makeStep("Step A", "ACTION")];
    const execution = makeExecution([]);
    render(<DiagramSequence execution={execution} testSteps={steps} />);

    expect(screen.queryByLabelText("circle-check")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("circle-x")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("circle-slash")).not.toBeInTheDocument();
  });
});
