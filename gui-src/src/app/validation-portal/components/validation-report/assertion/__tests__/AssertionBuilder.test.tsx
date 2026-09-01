/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent, within } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import AssertionBuilder from "../AssertionBuilder";
import { ValidationReportDTO } from "@/shared/types/validation/types";
import { useAssertionsData } from "@/app/validation-portal/hooks/useAssertionData";
import { AssertionGroup } from "../../types";
import { useInfiniteScroll } from "@gazelle/gazelle-component-ui";

// Mock dependencies
vi.mock("@/app/validation-portal/hooks/useAssertionData", () => ({
  useAssertionsData: vi.fn(),
}));

vi.mock("../GroupedAssertions", () => ({
  default: ({ items, groupBy }: any) => (
    <div data-testid="grouped-assertions">
      <span data-testid="items-count">{items.length}</span>
      <span data-testid="group-by">{groupBy}</span>
    </div>
  ),
}));

vi.mock("../GroupingToggleAssertion", () => ({
  default: ({ value, onChange }: any) => (
    <div data-testid="grouping-toggle">
      <button data-testid="toggle-severity" onClick={() => onChange("severity")}>
        Severity {value === "severity" && "✓"}
      </button>
      <button data-testid="toggle-report" onClick={() => onChange("report")}>
        Report {value === "report" && "✓"}
      </button>
    </div>
  ),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  CollapsableSubCard: ({ title, children, defaultExpanded, className }: any) => (
    <div data-testid="collapsable-sub-card" data-expanded={defaultExpanded} className={className}>
      <div data-testid="card-title">{title}</div>
      <div data-testid="card-content">{children}</div>
    </div>
  ),
  useInfiniteScroll: vi.fn(),
}));

describe("AssertionBuilder", () => {
  const mockUseAssertionsData = useAssertionsData as any;
  const mockUseInfiniteScroll = vi.mocked(useInfiniteScroll);

  beforeEach(() => {
    vi.clearAllMocks();

    // Default mock implementations
    mockUseAssertionsData.mockReturnValue({
      assertions: [],
      total: 0,
    });

    mockUseInfiniteScroll.mockReturnValue({
      visibleItems: [],
      hasMore: false,
      isLoadingMore: false,
      loadingTriggerId: "loading-trigger",
    });
  });

  const createValidationReport = (overrides?: Partial<ValidationReportDTO>): ValidationReportDTO =>
    ({
      ...overrides,
    }) as ValidationReportDTO;

  const createAssertionGroup = (name: string, assertionCount: number): AssertionGroup => ({
    reportName: name,
    assertions: Array.from({ length: assertionCount }, (_, i) => ({
      assertionID: `${name}-${i}`,
      description: `Test assertion ${i}`,
      result: "FAILED",
      severity: "ERROR",
      priority: "HIGH",
      assertionType: "Schematron",
      requirementIDs: [],
      unexpectedErrors: [],
      subjectLocations: [],
    })),
  });

  const mockBuildGroupedAssertion = vi.fn();

  describe("rendering", () => {
    it("renders nothing when total is 0", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [],
        total: 0,
      });

      const validationReport = createValidationReport();

      const { container } = render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(container.firstChild).toBeNull();
    });

    it("renders CollapsableSubCard with title and count", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 5,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(screen.getByTestId("collapsable-sub-card")).toBeInTheDocument();
      expect(screen.getByText("Failed Assertions (5)")).toBeInTheDocument();
    });

    it("renders description", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="PASSED"
          title="Passed Assertions"
          description="All assertions passed successfully"
        />,
      );

      expect(screen.getByText("All assertions passed successfully")).toBeInTheDocument();
    });

    it("renders with correct id for failed assertions", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(screen.getByText("Failed Assertions (1)")).toHaveAttribute("id", "failed-assertions");
    });

    it("applies custom className to CollapsableSubCard", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(screen.getByTestId("collapsable-sub-card")).toHaveClass("border-lightpurple");
    });
  });

  describe("expansion behavior", () => {
    it("is expanded by default when initiallyExpanded is true", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
          initiallyExpanded={true}
        />,
      );

      expect(screen.getByTestId("collapsable-sub-card")).toHaveAttribute("data-expanded", "true");
    });

    it("is collapsed when initiallyExpanded is false", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="PASSED"
          title="Passed Assertions"
          description="List of passed assertions"
          initiallyExpanded={false}
        />,
      );

      expect(screen.getByTestId("collapsable-sub-card")).toHaveAttribute("data-expanded", "false");
    });

    it("defaults to expanded when initiallyExpanded is not provided", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="UNDEFINED"
          title="Undefined Assertions"
          description="List of undefined assertions"
        />,
      );

      expect(screen.getByTestId("collapsable-sub-card")).toHaveAttribute("data-expanded", "true");
    });
  });

  describe("groupBy functionality", () => {
    it("renders grouping toggle when allowGroupBySeverity is true", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
          allowGroupBySeverity={true}
        />,
      );

      expect(screen.getByTestId("grouping-toggle")).toBeInTheDocument();
    });

    it("does not render grouping toggle when allowGroupBySeverity is false", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="PASSED"
          title="Passed Assertions"
          description="List of passed assertions"
          allowGroupBySeverity={false}
        />,
      );

      expect(screen.queryByTestId("grouping-toggle")).not.toBeInTheDocument();
    });

    it("defaults to severity grouping", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
          allowGroupBySeverity={true}
        />,
      );

      const groupedAssertions = screen.getByTestId("grouped-assertions");
      expect(within(groupedAssertions).getByTestId("group-by")).toHaveTextContent("severity");
    });

    it("changes groupBy when toggle is clicked", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
          allowGroupBySeverity={true}
        />,
      );

      const toggleButton = screen.getByTestId("toggle-report");
      fireEvent.click(toggleButton);

      const groupedAssertions = screen.getByTestId("grouped-assertions");
      expect(within(groupedAssertions).getByTestId("group-by")).toHaveTextContent("report");
    });

    it("can toggle back to severity grouping", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
          allowGroupBySeverity={true}
        />,
      );

      // Switch to report
      fireEvent.click(screen.getByTestId("toggle-report"));

      // Switch back to severity
      fireEvent.click(screen.getByTestId("toggle-severity"));

      const groupedAssertions = screen.getByTestId("grouped-assertions");
      expect(within(groupedAssertions).getByTestId("group-by")).toHaveTextContent("severity");
    });
  });

  describe("infinite scroll integration", () => {
    it("passes correct parameters to useInfiniteScroll", () => {
      const assertions = [
        { assertionID: "1", description: "Test 1" },
        { assertionID: "2", description: "Test 2" },
      ];

      mockUseAssertionsData.mockReturnValue({
        assertions,
        total: 2,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: assertions,
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(mockUseInfiniteScroll).toHaveBeenCalledWith({
        data: assertions,
        itemsPerPage: 20,
        loadingDelay: 300,
        threshold: 0.1,
      });
    });

    it("renders visible items from infinite scroll", () => {
      const assertions = Array.from({ length: 30 }, (_, i) => ({
        assertionID: `${i}`,
        description: `Test ${i}`,
      }));

      mockUseAssertionsData.mockReturnValue({
        assertions,
        total: 30,
      });

      const visibleItems = assertions.slice(0, 20);
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems,
        hasMore: true,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      const groupedAssertions = screen.getByTestId("grouped-assertions");
      expect(within(groupedAssertions).getByTestId("items-count")).toHaveTextContent("20");
    });

    it("displays loading trigger when hasMore is true", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 30,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: true,
        isLoadingMore: false,
        loadingTriggerId: "test-loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      const loadingTrigger = screen.getByTestId("card-content").querySelector("#test-loading-trigger");
      expect(loadingTrigger).toBeInTheDocument();
    });

    it("displays loading message when isLoadingMore is true", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 30,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: true,
        isLoadingMore: true,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();
    });

    it("does not display loading trigger when hasMore is false", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
      const cardContent = screen.getByTestId("card-content");
      expect(cardContent.querySelector("#loading-trigger")).not.toBeInTheDocument();
    });
  });

  describe("buildGroupedAssertionByReport integration", () => {
    it("calls buildGroupedAssertionByReport with reports and type", () => {
      const reports = [{ id: "report-1" }, { id: "report-2" }];
      const validationReport = createValidationReport({ reports: reports as any });

      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(mockBuildGroupedAssertion).toHaveBeenCalledWith(reports, "FAILED");
    });

    it("passes grouped reports to useAssertionsData", () => {
      const groupedReports = [createAssertionGroup("Report 1", 3), createAssertionGroup("Report 2", 2)];

      mockBuildGroupedAssertion.mockReturnValue(groupedReports);

      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 5,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport({ reports: [] as any });

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="PASSED"
          title="Passed Assertions"
          description="List of passed assertions"
        />,
      );

      expect(mockUseAssertionsData).toHaveBeenCalledWith(groupedReports, "PASSED");
    });

    it("memoizes grouped reports based on dependencies", () => {
      const reports = [{ id: "report-1" }];
      const validationReport = createValidationReport({ reports: reports as any });

      mockBuildGroupedAssertion.mockReturnValue([createAssertionGroup("Report 1", 1)]);

      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const { rerender } = render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(mockBuildGroupedAssertion).toHaveBeenCalledTimes(1);

      // Rerender with same props - should not call again
      rerender(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(mockBuildGroupedAssertion).toHaveBeenCalledTimes(1);
    });
  });

  describe("edge cases", () => {
    it("handles undefined reports gracefully", () => {
      const validationReport = createValidationReport({ reports: undefined });

      mockBuildGroupedAssertion.mockReturnValue([]);

      mockUseAssertionsData.mockReturnValue({
        assertions: [],
        total: 0,
      });

      const { container } = render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(container.firstChild).toBeNull();
    });

    it("handles empty reports array", () => {
      const validationReport = createValidationReport({ reports: [] });

      mockBuildGroupedAssertion.mockReturnValue([]);

      mockUseAssertionsData.mockReturnValue({
        assertions: [],
        total: 0,
      });

      const { container } = render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="PASSED"
          title="Passed Assertions"
          description="List of passed assertions"
        />,
      );

      expect(container.firstChild).toBeNull();
    });

    it("handles different assertion types", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: [{ assertionID: "1" }],
        total: 1,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: [{ assertionID: "1" }],
        hasMore: false,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      const { rerender } = render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(screen.getByText("Failed Assertions (1)")).toHaveAttribute("id", "failed-assertions");

      rerender(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="PASSED"
          title="Passed Assertions"
          description="List of passed assertions"
        />,
      );

      expect(screen.getByText("Passed Assertions (1)")).toHaveAttribute("id", "passed-assertions");

      rerender(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="UNDEFINED"
          title="Undefined Assertions"
          description="List of undefined assertions"
        />,
      );

      expect(screen.getByText("Undefined Assertions (1)")).toHaveAttribute("id", "undefined-assertions");
    });

    it("handles large assertion counts", () => {
      mockUseAssertionsData.mockReturnValue({
        assertions: Array.from({ length: 1000 }, (_, i) => ({ assertionID: `${i}` })),
        total: 1000,
      });
      mockUseInfiniteScroll.mockReturnValue({
        visibleItems: Array.from({ length: 20 }, (_, i) => ({ assertionID: `${i}` })),
        hasMore: true,
        isLoadingMore: false,
        loadingTriggerId: "loading-trigger",
      });

      const validationReport = createValidationReport();

      render(
        <AssertionBuilder
          validationReport={validationReport}
          buildGroupedAssertionByReport={mockBuildGroupedAssertion}
          type="FAILED"
          title="Failed Assertions"
          description="List of failed assertions"
        />,
      );

      expect(screen.getByText("Failed Assertions (1000)")).toBeInTheDocument();
      const groupedAssertions = screen.getByTestId("grouped-assertions");
      expect(within(groupedAssertions).getByTestId("items-count")).toHaveTextContent("20");
    });
  });
});
