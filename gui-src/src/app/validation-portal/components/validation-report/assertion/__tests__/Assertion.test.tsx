/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Assertion from "../Assertion";
import { AssertionReportDTO } from "@/shared/types/validation/types";
import { useReportAssertions } from "@validation-portal/context/selectedAssertionContext";
import * as isEqualAssertionModule from "@/app/validation-portal/utils/isEqualAssertion";
import * as getStatusColorModule from "@/shared/utils/getStatusColor";
import { toast } from "react-toastify";

// Mock dependencies
vi.mock("@validation-portal/context/selectedAssertionContext", () => ({
  useReportAssertions: vi.fn(),
}));

vi.mock("@/shared/utils/getStatusColor", () => ({
  getStatusColor: vi.fn(),
}));

vi.mock("@/app/validation-portal/utils/isEqualAssertion", () => ({
  isEqualAssertion: vi.fn(),
}));

vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock("@validation-portal/components/validation-report/UnexpectedErrorDisplay", () => ({
  default: ({ errors }: any) => <div data-testid="unexpected-errors">{errors.length} errors</div>,
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  InfoRow: ({ label, value }: any) => (
    <div data-testid="info-row">
      <span data-testid="info-label">{label}</span>
      <span data-testid="info-value">{Array.isArray(value) ? value.join(", ") : value}</span>
    </div>
  ),
  CopyURL: ({ linkText, onCopySuccess, onCopyError }: any) => (
    <div
      role="button"
      tabIndex={0}
      data-testid="copy-url"
      onClick={() => {
        try {
          onCopySuccess();
        } catch {
          onCopyError();
        }
      }}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          try {
            onCopySuccess();
          } catch {
            onCopyError();
          }
        }
      }}
    >
      {linkText}
    </div>
  ),
}));

describe("Assertion", () => {
  const mockSetSelectedAssertion = vi.fn();
  const mockUseReportAssertions = useReportAssertions as any;
  const mockIsEqualAssertion = isEqualAssertionModule.isEqualAssertion as any;
  const mockGetStatusColor = getStatusColorModule.getStatusColor as any;

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseReportAssertions.mockReturnValue({
      selectedAssertion: null,
      setSelectedAssertion: mockSetSelectedAssertion,
      assertionsWithLocation: [],
      setAssertionsWithLocation: vi.fn(),
      isLocationNavigationDisabled: false,
      setFileLocationNavigationDisabled: vi.fn(),
    });
    mockIsEqualAssertion.mockReturnValue(false);
    mockGetStatusColor.mockReturnValue("#000000");

    // Reset globalThis.location.hash
    globalThis.location.hash = "";
  });

  const createAssertion = (overrides: Partial<AssertionReportDTO> = {}): AssertionReportDTO => ({
    assertionID: "TEST-001",
    assertionType: "Schematron",
    description: "Test assertion description",
    subjectLocations: undefined,
    subjectValue: undefined,
    formalExpression: undefined,
    requirementIDs: [],
    severity: "ERROR",
    priority: "HIGH",
    result: "FAILED",
    unexpectedErrors: [],
    ...overrides,
  });

  describe("rendering", () => {
    it("renders assertion with description", () => {
      const assertion = createAssertion({ description: "Test description" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Test description")).toBeInTheDocument();
    });

    it("renders with correct status color", () => {
      mockGetStatusColor.mockReturnValue("#FF0000");
      const assertion = createAssertion();

      const { container } = render(<Assertion assertion={assertion} />);

      const assertionDiv = container.querySelector(".rounded-md");
      expect(assertionDiv).toHaveStyle({ borderColor: "#FF0000" });
    });

    it("renders show more button by default", () => {
      const assertion = createAssertion();

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Show more...")).toBeInTheDocument();
    });

    it("applies selected styling when assertion is selected", () => {
      mockIsEqualAssertion.mockReturnValue(true);
      const assertion = createAssertion();

      const { container } = render(<Assertion assertion={assertion} />);

      const assertionDiv = container.querySelector(".bg-grey-100");
      expect(assertionDiv).toBeInTheDocument();
    });

    it("applies default styling when assertion is not selected", () => {
      mockIsEqualAssertion.mockReturnValue(false);
      const assertion = createAssertion();

      const { container } = render(<Assertion assertion={assertion} />);

      const assertionDiv = container.querySelector(".bg-white");
      expect(assertionDiv).toBeInTheDocument();
    });
  });

  describe("status text formatting", () => {
    it("displays severity for FAILED assertions", () => {
      const assertion = createAssertion({ result: "FAILED", severity: "ERROR" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Error")).toBeInTheDocument();
    });

    it("displays result for PASSED assertions", () => {
      const assertion = createAssertion({ result: "PASSED" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Passed")).toBeInTheDocument();
    });

    it("displays result for UNDEFINED assertions", () => {
      const assertion = createAssertion({ result: "UNDEFINED" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Undefined")).toBeInTheDocument();
    });

    it("formats status text with capital first letter", () => {
      const assertion = createAssertion({ result: "FAILED", severity: "WARNING" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Warning")).toBeInTheDocument();
    });
  });

  describe("expand/collapse functionality", () => {
    it("shows more details when show more button is clicked", () => {
      const assertion = createAssertion({ assertionID: "TEST-001" });

      render(<Assertion assertion={assertion} />);

      const showMoreButton = screen.getByText("Show more...");
      fireEvent.click(showMoreButton);

      expect(screen.getByText("Show less")).toBeInTheDocument();
      const infoRows = screen.getAllByTestId("info-row");
      expect(infoRows.length).toBeGreaterThan(0);
    });

    it("hides details when show less button is clicked", () => {
      const assertion = createAssertion();

      render(<Assertion assertion={assertion} />);

      const showMoreButton = screen.getByText("Show more...");
      fireEvent.click(showMoreButton);

      const showLessButton = screen.getByText("Show less");
      fireEvent.click(showLessButton);

      expect(screen.getByText("Show more...")).toBeInTheDocument();
    });

    it("automatically expands when assertion is selected", () => {
      mockIsEqualAssertion.mockReturnValue(true);
      const assertion = createAssertion({ assertionID: "TEST-001" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Show less")).toBeInTheDocument();
    });
  });

  describe("assertion selection", () => {
    it("calls setSelectedAssertion when clicked", () => {
      const assertion = createAssertion();

      render(<Assertion assertion={assertion} />);

      const button = screen.getByRole("button", { name: /Test assertion description/i });
      fireEvent.click(button);

      expect(mockSetSelectedAssertion).toHaveBeenCalledWith(assertion);
    });

    it("does not call setSelectedAssertion when already selected", () => {
      mockIsEqualAssertion.mockReturnValue(true);
      const assertion = createAssertion();

      render(<Assertion assertion={assertion} />);

      const button = screen.getByRole("button", { name: /Test assertion description/i });
      fireEvent.click(button);

      expect(mockSetSelectedAssertion).not.toHaveBeenCalled();
    });

    it("selects assertion on Enter key press", () => {
      const assertion = createAssertion();

      render(<Assertion assertion={assertion} />);

      const button = screen.getByRole("button", { name: /Test assertion description/i });
      fireEvent.keyDown(button, { key: "Enter" });

      expect(mockSetSelectedAssertion).toHaveBeenCalledWith(assertion);
    });

    it("selects assertion on Space key press", () => {
      const assertion = createAssertion();

      render(<Assertion assertion={assertion} />);

      const button = screen.getByRole("button", { name: /Test assertion description/i });
      fireEvent.keyDown(button, { key: " " });

      expect(mockSetSelectedAssertion).toHaveBeenCalledWith(assertion);
    });

    it("does not select assertion on other key presses", () => {
      const assertion = createAssertion();

      render(<Assertion assertion={assertion} />);

      const button = screen.getByRole("button", { name: /Test assertion description/i });
      fireEvent.keyDown(button, { key: "Tab" });

      expect(mockSetSelectedAssertion).not.toHaveBeenCalled();
    });
  });

  describe("UNDEFINED assertions", () => {
    it("displays priority for UNDEFINED assertions without expanding", () => {
      const assertion = createAssertion({ result: "UNDEFINED", priority: "HIGH" });

      render(<Assertion assertion={assertion} />);

      const infoRows = screen.getAllByTestId("info-row");
      expect(infoRows.length).toBeGreaterThan(0);
    });

    it("auto-expands UNDEFINED assertions when hash is #undefined-assertions", () => {
      globalThis.location.hash = "#undefined-assertions";
      const assertion = createAssertion({ result: "UNDEFINED", assertionID: "TEST-001" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Show less")).toBeInTheDocument();
    });

    it("does not auto-expand non-UNDEFINED assertions with hash", () => {
      globalThis.location.hash = "#undefined-assertions";
      const assertion = createAssertion({ result: "FAILED" });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByText("Show more...")).toBeInTheDocument();
    });
  });

  describe("expanded details", () => {
    it("displays source when expanded", () => {
      const assertion = createAssertion({ source: "Test Source" });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("Test Source")).toBeInTheDocument();
    });

    it("displays assertion ID when expanded", () => {
      const assertion = createAssertion({ assertionID: "TEST-123" });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("TEST-123")).toBeInTheDocument();
    });

    it("displays priority for non-UNDEFINED assertions when expanded", () => {
      const assertion = createAssertion({ result: "FAILED", priority: "HIGH" });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      const infoRows = screen.getAllByTestId("info-row");
      const priorityRow = infoRows.find((row) => row.textContent?.includes("HIGH"));
      expect(priorityRow).toBeInTheDocument();
    });

    it("displays requirement IDs when expanded", () => {
      const assertion = createAssertion({ requirementIDs: ["REQ-1", "REQ-2"] });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("REQ-1, REQ-2")).toBeInTheDocument();
    });

    it("displays assertion type when expanded", () => {
      const assertion = createAssertion({ assertionType: "Schematron" });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("Schematron")).toBeInTheDocument();
    });

    it("displays formal expression when expanded", () => {
      const assertion = createAssertion({ formalExpression: "count(//element) > 0" });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("count(//element) > 0")).toBeInTheDocument();
    });

    it("displays subject value when expanded", () => {
      const assertion = createAssertion({ subjectValue: "Test Value" });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("Test Value")).toBeInTheDocument();
    });
  });

  describe("subject locations", () => {
    it("displays subject locations when expanded", () => {
      const assertion = createAssertion({
        subjectLocations: [{ inputId: "input-1", type: "XPath", value: "//element[1]" }],
      });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("input-1")).toBeInTheDocument();
      expect(screen.getByText("XPath")).toBeInTheDocument();
      expect(screen.getByText("//element[1]")).toBeInTheDocument();
    });

    it("displays multiple subject locations", () => {
      const assertion = createAssertion({
        subjectLocations: [
          { inputId: "input-1", type: "XPath", value: "//element[1]" },
          { inputId: "input-2", type: "JSONPath", value: "$.data" },
        ],
      });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("input-1")).toBeInTheDocument();
      expect(screen.getByText("input-2")).toBeInTheDocument();
      expect(screen.getByText("XPath")).toBeInTheDocument();
      expect(screen.getByText("JSONPath")).toBeInTheDocument();
    });

    it("displays copy button for subject location value", () => {
      const assertion = createAssertion({
        subjectLocations: [{ value: "//element[1]" }],
      });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByTestId("copy-url")).toBeInTheDocument();
    });

    it("shows success toast when copy succeeds", () => {
      const assertion = createAssertion({
        subjectLocations: [{ value: "//element[1]" }],
      });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      const copyButton = screen.getByTestId("copy-url");
      fireEvent.click(copyButton);

      expect(toast.success).toHaveBeenCalledWith("Value copied to clipboard");
    });

    it("does not render subject locations section when empty", () => {
      const assertion = createAssertion({ subjectLocations: [] });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.queryByText("gzl.texec.subject_location")).not.toBeInTheDocument();
    });

    it("does not render subject locations section when undefined", () => {
      const assertion = createAssertion({ subjectLocations: undefined });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.queryByText("gzl.texec.subject_location")).not.toBeInTheDocument();
    });
  });

  describe("unexpected errors", () => {
    it("displays unexpected errors component when present", () => {
      const assertion = createAssertion({
        unexpectedErrors: [{ message: "Error 1" }, { message: "Error 2" }] as any,
      });

      render(<Assertion assertion={assertion} />);

      expect(screen.getByTestId("unexpected-errors")).toBeInTheDocument();
      expect(screen.getByText("2 errors")).toBeInTheDocument();
    });

    it("does not display unexpected errors when empty", () => {
      const assertion = createAssertion({ unexpectedErrors: [] });

      render(<Assertion assertion={assertion} />);

      expect(screen.queryByTestId("unexpected-errors")).not.toBeInTheDocument();
    });
  });

  describe("edge cases", () => {
    it("handles assertion with no optional fields", () => {
      const assertion = createAssertion({
        source: undefined,
        assertionID: undefined,
        priority: undefined,
        requirementIDs: [],
        assertionType: undefined,
        formalExpression: undefined,
        subjectValue: undefined,
        subjectLocations: undefined,
      });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("Test assertion description")).toBeInTheDocument();
    });

    it("handles empty requirement IDs array", () => {
      const assertion = createAssertion({ requirementIDs: [] });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      // Should not crash and render other fields
      expect(screen.getByText("Test assertion description")).toBeInTheDocument();
    });

    it("handles empty string values", () => {
      const assertion = createAssertion({
        source: "",
        assertionType: "",
        formalExpression: "",
      });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));

      expect(screen.getByText("Test assertion description")).toBeInTheDocument();
    });

    it("handles special characters in description", () => {
      const assertion = createAssertion({
        description: "Test with <special> & characters",
      });

      render(<Assertion assertion={assertion} />);
      fireEvent.click(screen.getByText("Show more..."));
      expect(screen.getByText("Test with <special> & characters")).toBeInTheDocument();
    });
  });

  describe("status color integration", () => {
    it("calls getStatusColor with correct parameters", () => {
      const assertion = createAssertion({ result: "FAILED", severity: "ERROR" });

      render(<Assertion assertion={assertion} />);

      expect(mockGetStatusColor).toHaveBeenCalledWith("FAILED", "ERROR");
    });

    it("applies returned color to border and status text", () => {
      mockGetStatusColor.mockReturnValue("#FF5733");
      const assertion = createAssertion();

      const { container } = render(<Assertion assertion={assertion} />);

      const assertionDiv = container.querySelector(".rounded-md");
      const statusText = container.querySelector(".font-bold");

      expect(assertionDiv).toHaveStyle({ borderColor: "#FF5733" });
      expect(statusText).toHaveStyle({ color: "#FF5733" });
    });
  });
});
