/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TestStepWrapper from "../TestStepWrapper";
import { TestRunExecution } from "../../../../types/TestRunExecution";
import { TestStep } from "../../../../types/TestModel";

// Mock components
vi.mock("@gazelle/gazelle-component-ui", () => ({
  CollapsableCard: (props: any) =>
    React.createElement(
      "div",
      { "data-testid": "collapsable-card" },
      React.createElement("div", { "data-testid": "card-title" }, props.title),
      props.children,
    ),
  CollapsableSubCard: (props: any) =>
    React.createElement(
      "div",
      { "data-testid": "collapsable-sub-card" },
      React.createElement("div", { "data-testid": "sub-card-title" }, props.title),
      props.children,
    ),
  Badge: (props: any) => React.createElement("span", { "data-testid": "badge", className: props.variant }, props.children),
}));

vi.mock("../TestStepResults", () => ({
  TestStepResults: (props: any) => React.createElement("div", { "data-testid": "test-step-results" }, props.execution.id),
}));

vi.mock("../../test-logs/TestRunLogs", () => ({
  default: (props: any) =>
    React.createElement(
      "div",
      { "data-testid": "test-run-logs" },
      React.createElement("span", { "data-testid": "logs-is-running" }, props.isRunning?.toString() ?? "false"),
      React.createElement("span", { "data-testid": "logs-test-steps" }, props.testSteps ? "has-steps" : "no-steps"),
    ),
}));

vi.mock("../../diagram/DiagramSequence", () => ({
  default: (props: any) => React.createElement("div", { "data-testid": "diagram-sequence" }, props.execution.id),
}));

vi.mock("../../../ResizableSplit", () => ({
  default: (props: any) =>
    React.createElement(
      "div",
      { "data-testid": "resizable-split" },
      React.createElement("div", { "data-testid": "left-component" }, props.leftComponent),
      props.showRight && React.createElement("div", { "data-testid": "right-component" }, props.rightComponent),
      props.onToggleRight && React.createElement("button", { "data-testid": "toggle-diagram", onClick: props.onToggleRight }, "Toggle"),
    ),
}));

vi.mock("lucide-react", () => ({
  PanelLeftOpen: () => React.createElement("svg", { "data-testid": "panel-left-icon" }),
  PanelRightOpen: () => React.createElement("svg", { "data-testid": "panel-right-icon" }),
  Unplug: () => React.createElement("svg", { "data-testid": "unplug-icon" }),
  TriangleAlert: () => React.createElement("svg", { "data-testid": "triangle-alert-icon" }),
  X: () => React.createElement("svg", { "data-testid": "x-icon" }),
  PlugZap: () => React.createElement("svg", { "data-testid": "plug-zap-icon" }),
  Check: () => React.createElement("svg", { "data-testid": "check-icon" }),
  Terminal: () => React.createElement("svg", { "data-testid": "terminal-icon" }),
}));

describe("TestStepWrapper", () => {
  const createMockExecution = (overrides?: Partial<TestRunExecution>): TestRunExecution =>
    ({
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
      testSuiteId: "suite-1",
      testSpecification: "{}",
      testName: "Test Execution",
      status: "NOT_STARTED",
      startedAt: "2026-01-01",
      executedBy: "user-1",
      inputs: [],
      ...overrides,
    }) as TestRunExecution;

  const mockTestSteps: TestStep[] = [
    {
      name: "Step 1",
      type: "ACTION",
      description: "",
    },
    {
      name: "Step 2",
      type: "ASSERTION",
      description: "",
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Component Rendering", () => {
    it("should render CollapsableCard with test steps title", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} testSteps={mockTestSteps} isRunning={false} />);

      expect(screen.getByTestId("collapsable-card")).toBeInTheDocument();
      expect(screen.getByTestId("card-title")).toHaveTextContent("Test steps");
    });

    it("should render TestStepResults component", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} testSteps={mockTestSteps} isRunning={false} />);

      // TestStepResults renders with execution id
      expect(screen.getByTestId("test-step-results")).toBeInTheDocument();
      expect(screen.getByTestId("test-step-results")).toHaveTextContent("exec-1");
    });

    it("should render TestRunLogs with correct isRunning prop", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} testSteps={mockTestSteps} isRunning={true} />);

      expect(screen.getByTestId("test-run-logs")).toBeInTheDocument();
      expect(screen.getByTestId("logs-is-running")).toHaveTextContent("true");
    });

    it("should pass testSteps to TestRunLogs", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} testSteps={mockTestSteps} isRunning={false} />);

      expect(screen.getByTestId("logs-test-steps")).toHaveTextContent("has-steps");
    });

    it("should render ResizableSplit with TestStepResults and DiagramSequence", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} testSteps={mockTestSteps} isRunning={false} />);

      expect(screen.getByTestId("resizable-split")).toBeInTheDocument();
      expect(screen.getByTestId("left-component")).toBeInTheDocument();
      expect(screen.getByTestId("test-step-results")).toBeInTheDocument();
    });
  });

  describe("Diagram Toggle", () => {
    it("should toggle diagram visibility when toggle button is clicked", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} testSteps={mockTestSteps} isRunning={false} />);

      const toggleButton = screen.getByTestId("toggle-diagram");

      // Initially, right component should not be visible (diagram hidden)
      expect(screen.queryByTestId("right-component")).not.toBeInTheDocument();

      // Click to show diagram
      fireEvent.click(toggleButton);
      expect(screen.getByTestId("right-component")).toBeInTheDocument();
      expect(screen.getByTestId("diagram-sequence")).toBeInTheDocument();

      // Click to hide diagram
      fireEvent.click(toggleButton);
      expect(screen.queryByTestId("right-component")).not.toBeInTheDocument();
    });
  });

  describe("Default Props", () => {
    it("should use default isRunning value (false) when not provided", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} />);

      expect(screen.getByTestId("test-run-logs")).toBeInTheDocument();
      expect(screen.getByTestId("logs-is-running")).toHaveTextContent("false");
    });

    it("should handle undefined testSteps", () => {
      const testRun = createMockExecution();

      render(<TestStepWrapper testRun={testRun} isRunning={false} />);

      expect(screen.getByTestId("test-step-results")).toBeInTheDocument();
    });
  });
});
