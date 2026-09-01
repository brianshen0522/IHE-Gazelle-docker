/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { TestStepResults } from "../TestStepResults";
import { TestStep as TestModelStep } from "@/app/test-execution/types/TestModel";

vi.mock("@shared/components/report/ResultBadge", () => ({
  ResultBadge: (props: { result: string }) => <div data-testid="result-badge">{props.result}</div>,
}));

vi.mock("@shared/components/dates/FormattedDate", () => ({
  default: (props: { dateString: string }) => <div data-testid="formatted-date">{props.dateString}</div>,
}));

vi.mock("../TestStepOutputs", () => ({ default: () => <div data-testid="test-step-outputs" /> }));

vi.mock("lucide-react", () => ({
  Bot: () => <div data-testid="bot-icon">Bot Icon</div>,
  User: () => <div data-testid="user-icon">User Icon</div>,
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  NoticeBanner: (props: { color: string; children: React.ReactNode }) => (
    <div data-testid="notice-banner" data-color={props.color}>
      {props.children}
    </div>
  ),
}));

describe("TestStepResults", () => {
  it("should render null when no test steps provided", () => {
    const execution = {} as any;
    const { container } = render(<TestStepResults execution={execution} testSteps={[]} />);
    expect(container.firstChild).toBeNull();
  });

  it("should render steps with NOT_STARTED status when no results", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Login",
        type: "GIVEN",
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    expect(screen.getByText("NOT_STARTED")).toBeInTheDocument();
    expect(screen.getByText(/1\. Login/)).toBeInTheDocument();
  });

  it("should render live step results when provided", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Login",
        type: "GIVEN",
        description: "",
      },
    ];
    const execution = {
      liveStepResults: [
        {
          stepName: "",
          type: "",
          dateTime: "2026-04-14T10:00:00Z",
          result: "RUNNING",
        },
      ],
    } as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    expect(screen.getByText("RUNNING")).toBeInTheDocument();
  });

  it("should prioritize live results over final report", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Login",
        type: "GIVEN",
        description: "",
      },
    ];
    const execution = {
      liveStepResults: [
        {
          stepName: "",
          type: "",
          dateTime: "2026-04-14T10:00:00Z",
          result: "RUNNING",
        },
      ],
      testReport: {
        testRunReports: [
          {
            runId: "run-1",
            result: "PASSED",
            stepRunReports: [
              {
                stepName: "Login",
                type: "GIVEN",
                dateTime: "2026-04-14T10:00:00Z",
                result: "PASSED",
              },
            ],
          },
        ],
      },
    } as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    expect(screen.getByText("RUNNING")).toBeInTheDocument();
  });

  it("should match steps by index", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Step 1",
        type: "GIVEN",
        description: "",
      },
      {
        name: "Step 2",
        type: "WHEN",
        description: "",
      },
    ];
    const execution = {
      liveStepResults: [
        { stepName: "", type: "", dateTime: "2026-04-14T10:00:00Z", result: "PASSED" },
        { stepName: "", type: "", dateTime: "2026-04-14T10:01:00Z", result: "RUNNING" },
      ],
    } as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    const badges = screen.getAllByTestId("result-badge");
    expect(badges[0]).toHaveTextContent("PASSED");
    expect(badges[1]).toHaveTextContent("RUNNING");
    expect(screen.getByText(/1\. Step 1/)).toBeInTheDocument();
    expect(screen.getByText(/2\. Step 2/)).toBeInTheDocument();
  });

  it("should render step properties when show more is clicked", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Validate",
        type: "ACTION",
        properties: [
          { name: "validationService", type: "STRING", value: "Matchbox" },
          { name: "validationProfile", type: "STRING", value: "http://example.com/profile" },
        ],
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);

    // Properties should not be visible initially
    expect(screen.queryByText("validationService:")).not.toBeInTheDocument();

    // Click show more button
    const showMoreButton = screen.getByRole("button", { name: /show more/i });
    fireEvent.click(showMoreButton);

    // Properties should now be visible
    expect(screen.getByText("validationService:")).toBeInTheDocument();
    expect(screen.getByText("Matchbox")).toBeInTheDocument();
    expect(screen.getByText("validationProfile:")).toBeInTheDocument();
    expect(screen.getByText("http://example.com/profile")).toBeInTheDocument();
  });

  it("should not render properties section when no properties", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Login",
        type: "GIVEN",
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    // Properties with colon should not be visible initially
    expect(screen.queryByText(/validationService:/)).not.toBeInTheDocument();
  });

  it("should toggle show more/show less button", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Step",
        type: "ACTION",
        properties: [{ name: "prop", type: "STRING", value: "value" }],
        description: "Test description",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);

    // Initially should show "show more"
    const showMoreButton = screen.getByRole("button", { name: /show more/i });
    expect(showMoreButton).toBeInTheDocument();

    // Click to show more
    fireEvent.click(showMoreButton);
    expect(screen.getByRole("button", { name: /show less/i })).toBeInTheDocument();

    // Click to show less
    const showLessButton = screen.getByRole("button", { name: /show less/i });
    fireEvent.click(showLessButton);
    expect(screen.getByRole("button", { name: /show more/i })).toBeInTheDocument();
  });

  it("should always display description and outputs", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Step",
        type: "ACTION",
        description: "This is a test description",
        properties: [{ name: "prop", type: "STRING", value: "value" }],
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);

    // Description should always be visible
    expect(screen.getByText("This is a test description")).toBeInTheDocument();

    // Outputs should always be visible
    expect(screen.getByTestId("test-step-outputs")).toBeInTheDocument();
  });

  it("should render multiple steps with correct numbering", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "First Step",
        type: "GIVEN",
        description: "",
      },
      {
        name: "Second Step",
        type: "WHEN",
        description: "",
      },
      {
        name: "Third Step",
        type: "THEN",
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    expect(screen.getByText(/1\. First Step/)).toBeInTheDocument();
    expect(screen.getByText(/2\. Second Step/)).toBeInTheDocument();
    expect(screen.getByText(/3\. Third Step/)).toBeInTheDocument();
  });

  it("should handle empty properties array", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Step",
        type: "ACTION",
        properties: [],
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);

    // Click show more button
    const showMoreButton = screen.getByRole("button", { name: /show more/i });
    fireEvent.click(showMoreButton);

    // Properties div should not be rendered when array is empty
    const { container } = render(<TestStepResults execution={execution} testSteps={testSteps} />);
    const propertiesDiv = container.querySelector(".mt-2.space-y-1");
    expect(propertiesDiv).not.toBeInTheDocument();
  });

  it("should render Bot icon for non-USER_INTERACTION steps", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Login Step",
        type: "GIVEN",
        description: "",
      },
      {
        name: "Action Step",
        type: "ACTION",
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    const botIcons = screen.getAllByTestId("bot-icon");
    expect(botIcons).toHaveLength(2);
    expect(screen.queryByTestId("user-icon")).not.toBeInTheDocument();
  });

  it("should render User icon for USER_INTERACTION steps", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "User Input Step",
        type: "USER_INTERACTION",
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    expect(screen.getByTestId("user-icon")).toBeInTheDocument();
    expect(screen.queryByTestId("bot-icon")).not.toBeInTheDocument();
  });

  it("should render both Bot and User icons for mixed step types", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Automated Step",
        type: "GIVEN",
        description: "",
      },
      {
        name: "User Step",
        type: "USER_INTERACTION",
        description: "",
      },
      {
        name: "Another Automated Step",
        type: "THEN",
        description: "",
      },
    ];
    const execution = {} as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);
    const botIcons = screen.getAllByTestId("bot-icon");
    const userIcons = screen.getAllByTestId("user-icon");
    expect(botIcons).toHaveLength(2);
    expect(userIcons).toHaveLength(1);
  });

  it("should display red NoticeBanner when step result is UNDEFINED", () => {
    const testSteps: TestModelStep[] = [
      {
        name: "Test Step",
        type: "GIVEN",
        description: "",
      },
    ];
    const execution = {
      liveStepResults: [
        {
          stepName: "Test Step",
          type: "GIVEN",
          dateTime: "2026-04-14T10:00:00Z",
          result: "UNDEFINED",
        },
      ],
    } as any;

    render(<TestStepResults execution={execution} testSteps={testSteps} />);

    const noticeBanner = screen.getByTestId("notice-banner");
    expect(noticeBanner).toBeInTheDocument();
    expect(noticeBanner).toHaveAttribute("data-color", "red");
    expect(noticeBanner).toHaveTextContent("Please refer to the test report to identify the cause of the unexpected error.");
  });
});
