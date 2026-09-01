/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import GroupedAssertions from "../GroupedAssertions";
import { AssertionReportDTO } from "@/shared/types/validation/types";

// Mock Assertion component
vi.mock("../Assertion", () => ({
  default: ({ assertion }: any) => (
    <div data-testid="assertion" data-assertion-id={assertion.assertionID}>
      {assertion.description}
    </div>
  ),
}));

describe("GroupedAssertions", () => {
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
    source: "Report1",
    ...overrides,
  });

  describe("rendering without groupBy", () => {
    it("renders all assertions in a flat list", () => {
      const items = [
        createAssertion({ assertionID: "1", description: "Assertion 1" }),
        createAssertion({ assertionID: "2", description: "Assertion 2" }),
        createAssertion({ assertionID: "3", description: "Assertion 3" }),
      ];

      render(<GroupedAssertions items={items} />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(3);
      expect(assertions[0]).toHaveTextContent("Assertion 1");
      expect(assertions[1]).toHaveTextContent("Assertion 2");
      expect(assertions[2]).toHaveTextContent("Assertion 3");
    });

    it("renders empty list when items array is empty", () => {
      render(<GroupedAssertions items={[]} />);

      expect(screen.queryAllByTestId("assertion")).toHaveLength(0);
    });

    it("generates unique keys with assertion IDs", () => {
      const items = [createAssertion({ assertionID: "TEST-1" }), createAssertion({ assertionID: "TEST-2" })];

      const { container } = render(<GroupedAssertions items={items} />);

      const assertions = container.querySelectorAll("[data-assertion-id]");
      expect(assertions[0]).toHaveAttribute("data-assertion-id", "TEST-1");
      expect(assertions[1]).toHaveAttribute("data-assertion-id", "TEST-2");
    });

    it("handles assertions without assertionID", () => {
      const items = [createAssertion({ assertionID: undefined }), createAssertion({ assertionID: "" })];

      render(<GroupedAssertions items={items} />);

      expect(screen.getAllByTestId("assertion")).toHaveLength(2);
    });

    it("applies correct container classes", () => {
      const items = [createAssertion()];

      const { container } = render(<GroupedAssertions items={items} />);

      const wrapper = container.firstChild as HTMLElement;
      expect(wrapper).toHaveClass("flex", "flex-col", "gap-4");
    });
  });

  describe("rendering with groupBy='severity'", () => {
    it("groups assertions by severity", () => {
      const items = [
        createAssertion({ assertionID: "1", severity: "ERROR", description: "Error 1" }),
        createAssertion({ assertionID: "2", severity: "ERROR", description: "Error 2" }),
        createAssertion({ assertionID: "3", severity: "WARNING", description: "Warning 1" }),
        createAssertion({ assertionID: "4", severity: "INFO", description: "Info 1" }),
      ];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(4);

      // Verify all assertions are rendered
      expect(screen.getByText("Error 1")).toBeInTheDocument();
      expect(screen.getByText("Error 2")).toBeInTheDocument();
      expect(screen.getByText("Warning 1")).toBeInTheDocument();
      expect(screen.getByText("Info 1")).toBeInTheDocument();
    });

    it("does not render headers for severity grouping", () => {
      const items = [createAssertion({ severity: "ERROR" }), createAssertion({ severity: "WARNING" })];

      const { container } = render(<GroupedAssertions items={items} groupBy="severity" />);

      const headers = container.querySelectorAll("h3");
      expect(headers).toHaveLength(0);
    });

    it("handles assertions with undefined severity", () => {
      const items = [
        createAssertion({ assertionID: "1", severity: "ERROR" }),
        createAssertion({ assertionID: "2", severity: undefined }),
        createAssertion({ assertionID: "3", severity: "WARNING" }),
      ];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(3);
    });

    it("groups all assertions with same severity together", () => {
      const items = [
        createAssertion({ assertionID: "1", severity: "ERROR", description: "Error 1" }),
        createAssertion({ assertionID: "2", severity: "WARNING", description: "Warning 1" }),
        createAssertion({ assertionID: "3", severity: "ERROR", description: "Error 2" }),
        createAssertion({ assertionID: "4", severity: "ERROR", description: "Error 3" }),
      ];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(4);
    });

    it("applies gap-2 spacing within groups", () => {
      const items = [createAssertion({ severity: "ERROR" }), createAssertion({ severity: "ERROR" })];

      const { container } = render(<GroupedAssertions items={items} groupBy="severity" />);

      const innerContainer = container.querySelector(".gap-2");
      expect(innerContainer).toBeInTheDocument();
    });
  });

  describe("rendering with groupBy='report'", () => {
    it("groups assertions by report source", () => {
      const items = [
        createAssertion({ assertionID: "1", source: "Report1", description: "Report1 Assertion 1" }),
        createAssertion({ assertionID: "2", source: "Report1", description: "Report1 Assertion 2" }),
        createAssertion({ assertionID: "3", source: "Report2", description: "Report2 Assertion 1" }),
      ];

      const { container } = render(<GroupedAssertions items={items} groupBy="report" />);

      const headers = container.querySelectorAll("h3");
      expect(headers.length).toBeGreaterThan(0);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(3);
    });

    it("renders headers with uppercase report names", () => {
      const items = [createAssertion({ source: "report1" }), createAssertion({ source: "report2" })];

      render(<GroupedAssertions items={items} groupBy="report" />);

      expect(screen.getByText("REPORT1")).toBeInTheDocument();
      expect(screen.getByText("REPORT2")).toBeInTheDocument();
    });

    it("renders headers with correct styling", () => {
      const items = [createAssertion({ source: "TestReport" })];

      const { container } = render(<GroupedAssertions items={items} groupBy="report" />);

      const header = container.querySelector("h3");
      expect(header).toHaveClass("font-semibold", "mb-1");
    });

    it("handles assertions with undefined source", () => {
      const items = [
        createAssertion({ assertionID: "1", source: "Report1" }),
        createAssertion({ assertionID: "2", source: undefined }),
        createAssertion({ assertionID: "3", source: "Report2" }),
      ];

      render(<GroupedAssertions items={items} groupBy="report" />);

      expect(screen.getByText("REPORT1")).toBeInTheDocument();
      expect(screen.getByText("UNKNOWN")).toBeInTheDocument();
      expect(screen.getByText("REPORT2")).toBeInTheDocument();

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(3);
    });

    it("handles assertions with null source", () => {
      const items = [createAssertion({ source: null as any })];

      render(<GroupedAssertions items={items} groupBy="report" />);

      expect(screen.getByText("UNKNOWN")).toBeInTheDocument();
    });

    it("groups multiple assertions from same report together", () => {
      const items = [
        createAssertion({ assertionID: "1", source: "Report1" }),
        createAssertion({ assertionID: "2", source: "Report1" }),
        createAssertion({ assertionID: "3", source: "Report1" }),
      ];

      render(<GroupedAssertions items={items} groupBy="report" />);

      const headers = screen.getAllByText("REPORT1");
      expect(headers).toHaveLength(1);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(3);
    });

    it("renders groups with gap-4 spacing", () => {
      const items = [createAssertion({ source: "Report1" }), createAssertion({ source: "Report2" })];

      const { container } = render(<GroupedAssertions items={items} groupBy="report" />);

      const outerContainer = container.querySelector(".gap-4");
      expect(outerContainer).toBeInTheDocument();
    });
  });

  describe("key generation", () => {
    it("generates unique keys for ungrouped assertions", () => {
      const items = [createAssertion({ assertionID: "1" }), createAssertion({ assertionID: "2" })];

      render(<GroupedAssertions items={items} />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);
    });

    it("generates unique keys for severity-grouped assertions", () => {
      const items = [createAssertion({ assertionID: "1", severity: "ERROR" }), createAssertion({ assertionID: "2", severity: "ERROR" })];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);
    });

    it("generates unique keys for report-grouped assertions", () => {
      const items = [createAssertion({ assertionID: "1", source: "Report1" }), createAssertion({ assertionID: "2", source: "Report1" })];

      render(<GroupedAssertions items={items} groupBy="report" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);
    });

    it("handles duplicate assertion IDs", () => {
      const items = [
        createAssertion({ assertionID: "TEST-1", description: "First" }),
        createAssertion({ assertionID: "TEST-1", description: "Second" }),
      ];

      render(<GroupedAssertions items={items} />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);
      expect(assertions[0]).toHaveTextContent("First");
      expect(assertions[1]).toHaveTextContent("Second");
    });
  });

  describe("edge cases", () => {
    it("handles single assertion", () => {
      const items = [createAssertion({ description: "Single assertion" })];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      expect(screen.getByText("Single assertion")).toBeInTheDocument();
    });

    it("handles empty items with groupBy", () => {
      render(<GroupedAssertions items={[]} groupBy="severity" />);

      expect(screen.queryAllByTestId("assertion")).toHaveLength(0);
    });

    it("handles mixed severity values", () => {
      const items = [
        createAssertion({ severity: "ERROR" }),
        createAssertion({ severity: "WARNING" }),
        createAssertion({ severity: "INFO" }),
        createAssertion({ severity: undefined }),
        createAssertion({ severity: "CRITICAL" as any }),
      ];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(5);
    });

    it("handles mixed source values", () => {
      const items = [
        createAssertion({ source: "Report1" }),
        createAssertion({ source: "Report2" }),
        createAssertion({ source: undefined }),
        createAssertion({ source: "" }),
      ];

      render(<GroupedAssertions items={items} groupBy="report" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(4);
    });

    it("handles special characters in source names", () => {
      const items = [createAssertion({ source: "Report-1" }), createAssertion({ source: "Report_2" }), createAssertion({ source: "Report.3" })];

      render(<GroupedAssertions items={items} groupBy="report" />);

      expect(screen.getByText("REPORT-1")).toBeInTheDocument();
      expect(screen.getByText("REPORT_2")).toBeInTheDocument();
      expect(screen.getByText("REPORT.3")).toBeInTheDocument();
    });

    it("handles very long source names", () => {
      const longName = "A".repeat(100);
      const items = [createAssertion({ source: longName })];

      render(<GroupedAssertions items={items} groupBy="report" />);

      expect(screen.getByText(longName.toUpperCase())).toBeInTheDocument();
    });

    it("handles large number of assertions", () => {
      const items = Array.from({ length: 100 }, (_, i) => {
        const calculSeverity = i % 3 === 1 ? "WARNING" : "INFO";
        return createAssertion({
          assertionID: `TEST-${i}`,
          description: `Assertion ${i}`,
          severity: i % 3 === 0 ? "ERROR" : calculSeverity,
        });
      });

      render(<GroupedAssertions items={items} groupBy="severity" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(100);
    });

    it("maintains order of assertions within groups", () => {
      const items = [
        createAssertion({ assertionID: "1", severity: "ERROR", description: "First Error" }),
        createAssertion({ assertionID: "2", severity: "ERROR", description: "Second Error" }),
        createAssertion({ assertionID: "3", severity: "ERROR", description: "Third Error" }),
      ];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions[0]).toHaveTextContent("First Error");
      expect(assertions[1]).toHaveTextContent("Second Error");
      expect(assertions[2]).toHaveTextContent("Third Error");
    });

    it("handles switching between groupBy modes", () => {
      const items = [
        createAssertion({ assertionID: "1", severity: "ERROR", source: "Report1" }),
        createAssertion({ assertionID: "2", severity: "WARNING", source: "Report2" }),
      ];

      const { rerender } = render(<GroupedAssertions items={items} groupBy="severity" />);

      let assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);

      rerender(<GroupedAssertions items={items} groupBy="report" />);

      assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);
      expect(screen.getByText("REPORT1")).toBeInTheDocument();
      expect(screen.getByText("REPORT2")).toBeInTheDocument();
    });

    it("handles undefined groupBy prop", () => {
      const items = [createAssertion({ assertionID: "1" }), createAssertion({ assertionID: "2" })];

      render(<GroupedAssertions items={items} groupBy={undefined} />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);
    });
  });

  describe("groupByKey utility function behavior", () => {
    it("correctly groups items by severity", () => {
      const items = [createAssertion({ severity: "ERROR" }), createAssertion({ severity: "ERROR" }), createAssertion({ severity: "WARNING" })];

      render(<GroupedAssertions items={items} groupBy="severity" />);

      // Verify all items are rendered (grouped correctly)
      expect(screen.getAllByTestId("assertion")).toHaveLength(3);
    });

    it("correctly groups items by report source", () => {
      const items = [createAssertion({ source: "Report1" }), createAssertion({ source: "Report1" }), createAssertion({ source: "Report2" })];

      render(<GroupedAssertions items={items} groupBy="report" />);

      // Verify headers are created for each unique source
      expect(screen.getByText("REPORT1")).toBeInTheDocument();
      expect(screen.getByText("REPORT2")).toBeInTheDocument();
      expect(screen.getAllByTestId("assertion")).toHaveLength(3);
    });

    it("handles empty string as grouping key", () => {
      const items = [createAssertion({ source: "" }), createAssertion({ source: "" })];

      render(<GroupedAssertions items={items} groupBy="report" />);

      const assertions = screen.getAllByTestId("assertion");
      expect(assertions).toHaveLength(2);
    });

    it("groups items with same key together", () => {
      const items = [
        createAssertion({ assertionID: "1", source: "SameReport" }),
        createAssertion({ assertionID: "2", source: "DifferentReport" }),
        createAssertion({ assertionID: "3", source: "SameReport" }),
      ];

      render(<GroupedAssertions items={items} groupBy="report" />);

      expect(screen.getByText("SAMEREPORT")).toBeInTheDocument();
      expect(screen.getByText("DIFFERENTREPORT")).toBeInTheDocument();
    });
  });
});
