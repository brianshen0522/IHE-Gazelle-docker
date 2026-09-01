import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import InstructionDiagram from "../InstructionDiagram";

// Mock the dependencies
vi.mock("@/shared/services/maestro/utils/logStyles", () => ({
  getResultColor: vi.fn((result: string) => {
    if (result === "DONE") return "text-green-500";
    if (result === "FAILED") return "text-red-500";
    if (result === "UNDEFINED") return "text-grey-500";
    return "";
  }),
}));

vi.mock("@/shared/services/RenderSanitizedHTML", () => ({
  default: ({ untrustedHTML }: { untrustedHTML: string }) => <div data-testid="sanitized-html">{untrustedHTML}</div>,
}));

vi.mock("lucide-react", () => ({
  CircleCheck: ({ className }: { className: string }) => <span data-testid="circle-check" className={className} />,
  CircleX: ({ className }: { className: string }) => <span data-testid="circle-x" className={className} />,
  CircleSlash: ({ className }: { className: string }) => <span data-testid="circle-slash" className={className} />,
}));

describe("InstructionDiagram", () => {
  it("renders instruction name and description", () => {
    render(<InstructionDiagram name="User Action" description="Please click the button" />);

    expect(screen.getByText("User Action")).toBeInTheDocument();
    expect(screen.getByTestId("sanitized-html")).toHaveTextContent("Please click the button");
  });

  it("renders without execution result icons when no result is provided", () => {
    render(<InstructionDiagram name="Step 1" description="Description" />);

    expect(screen.queryByTestId("circle-check")).not.toBeInTheDocument();
    expect(screen.queryByTestId("circle-x")).not.toBeInTheDocument();
    expect(screen.queryByTestId("circle-slash")).not.toBeInTheDocument();
  });

  it("renders CircleCheck icon when execution result is DONE", () => {
    render(<InstructionDiagram name="Step 1" description="Description" executionResult={{ result: "DONE" }} />);

    const checkIcon = screen.getByTestId("circle-check");
    expect(checkIcon).toBeInTheDocument();
    expect(checkIcon).toHaveClass("text-green-500");
  });

  it("renders CircleX icon when execution result is FAILED", () => {
    render(<InstructionDiagram name="Step 1" description="Description" executionResult={{ result: "FAILED" }} />);

    const xIcon = screen.getByTestId("circle-x");
    expect(xIcon).toBeInTheDocument();
    expect(xIcon).toHaveClass("text-red-500");
  });

  it("renders CircleSlash icon when execution result is UNDEFINED", () => {
    render(<InstructionDiagram name="Step 1" description="Description" executionResult={{ result: "UNDEFINED" }} />);

    const slashIcon = screen.getByTestId("circle-slash");
    expect(slashIcon).toBeInTheDocument();
    expect(slashIcon).toHaveClass("text-grey-500");
  });

  it("does not render icons for other result statuses", () => {
    render(<InstructionDiagram name="Step 1" description="Description" executionResult={{ result: "PENDING" }} />);

    expect(screen.queryByTestId("circle-check")).not.toBeInTheDocument();
    expect(screen.queryByTestId("circle-x")).not.toBeInTheDocument();
    expect(screen.queryByTestId("circle-slash")).not.toBeInTheDocument();
  });

  it("applies correct styling with border and padding", () => {
    const { container } = render(<InstructionDiagram name="Test" description="Test description" />);

    const wrapper = container.firstChild as HTMLElement;
    expect(wrapper).toHaveClass("border", "border-purple", "p-2", "flex", "flex-col");
  });

  it("renders name with font-semibold styling", () => {
    render(<InstructionDiagram name="Important Step" description="Description" />);

    const nameElement = screen.getByText("Important Step").parentElement;
    expect(nameElement).toHaveClass("font-semibold");
  });

  it("passes HTML content to RenderSanitizedHTML component", () => {
    const htmlDescription = "<strong>Bold text</strong> and <em>italic</em>";
    render(<InstructionDiagram name="Step" description={htmlDescription} />);

    const sanitizedContent = screen.getByTestId("sanitized-html");
    expect(sanitizedContent).toHaveTextContent(htmlDescription);
  });

  it("handles empty description", () => {
    render(<InstructionDiagram name="Step" description="" />);

    const sanitizedContent = screen.getByTestId("sanitized-html");
    expect(sanitizedContent).toHaveTextContent("");
  });

  it("combines name and DONE icon in the same flex container", () => {
    render(<InstructionDiagram name="Complete Step" description="Done" executionResult={{ result: "DONE" }} />);

    const nameContainer = screen.getByText("Complete Step").parentElement;
    const checkIcon = screen.getByTestId("circle-check");

    expect(nameContainer).toContainElement(checkIcon);
    expect(nameContainer).toHaveClass("flex", "items-center", "gap-2");
  });

  it("combines name and FAILED icon in the same flex container", () => {
    render(<InstructionDiagram name="Failed Step" description="Error" executionResult={{ result: "FAILED" }} />);

    const nameContainer = screen.getByText("Failed Step").parentElement;
    const xIcon = screen.getByTestId("circle-x");

    expect(nameContainer).toContainElement(xIcon);
    expect(nameContainer).toHaveClass("flex", "items-center", "gap-2");
  });
});
