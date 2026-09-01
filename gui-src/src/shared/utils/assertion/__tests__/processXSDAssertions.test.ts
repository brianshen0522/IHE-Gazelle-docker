/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect } from "vitest";
import { processXSDAssertions } from "../processXSDAssertions";
import { AssertionReportDTO, ValidationSubReportDTO, ValidationResult } from "@/shared/types/validation/types";

describe("processXSDAssertions", () => {
  const createAssertion = (id: string, result: ValidationResult): AssertionReportDTO => ({
    assertionID: id,
    assertionType: "XSD",
    description: `Assertion ${id}`,
    subjectLocations: undefined,
    subjectValue: "value",
    formalExpression: "expression",
    requirementIDs: ["REQ-1"],
    severity: "ERROR",
    priority: "HIGH",
    result: result,
    unexpectedErrors: [],
  });

  const createReport = (
    name: string,
    standards: string[],
    assertions: AssertionReportDTO[],
    subReports?: ValidationSubReportDTO[],
  ): ValidationSubReportDTO => ({
    name,
    subReportResult: "PASSED",
    subReports,
    assertionReports: assertions,
    subCounters: {
      numberOfAssertions: 0,
      numberOfFailedWithInfos: 0,
      numberOfFailedWithWarnings: 0,
      numberOfFailedWithErrors: 0,
      numberOfUnexpectedErrors: 0,
      numberOfUndefined: 0,
    },
    standards,
    unexpectedErrors: [],
  });

  describe("basic filtering by name", () => {
    it("processes report with name 'XSD Validation'", () => {
      const reports = [createReport("XSD Validation", [], [createAssertion("A1", "PASSED")])];

      const result = processXSDAssertions(reports, "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].reportName).toBe("XSD Validation");
      expect(result[0].assertions).toHaveLength(1);
      expect(result[0].assertions[0].assertionID).toBe("A1");
      expect(result[0].assertions[0].source).toBe("XSD Validation");
    });

    it("ignores reports without XSD name or standard", () => {
      const reports = [createReport("Other Validation", [], [createAssertion("A1", "PASSED")])];

      const result = processXSDAssertions(reports, "PASSED");

      expect(result).toHaveLength(0);
    });
  });

  describe("filtering by standards", () => {
    it("processes report with XSD in standards array", () => {
      const reports = [createReport("Custom Validation", ["XSD"], [createAssertion("A1", "FAILED")])];

      const result = processXSDAssertions(reports, "FAILED");

      expect(result).toHaveLength(1);
      expect(result[0].reportName).toBe("Custom Validation");
      expect(result[0].assertions).toHaveLength(1);
    });

    it("processes report with XSD among other standards", () => {
      const reports = [createReport("Multi Standard", ["JSON", "XSD", "XML"], [createAssertion("A1", "PASSED")])];

      const result = processXSDAssertions(reports, "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].reportName).toBe("Multi Standard");
    });
  });

  describe("result filtering", () => {
    it("filters assertions by PASSED result", () => {
      const assertions = [createAssertion("A1", "PASSED"), createAssertion("A2", "FAILED"), createAssertion("A3", "PASSED")];
      const reports = [createReport("XSD Validation", [], assertions)];

      const result = processXSDAssertions(reports, "PASSED");

      expect(result[0].assertions).toHaveLength(2);
      expect(result[0].assertions[0].assertionID).toBe("A1");
      expect(result[0].assertions[1].assertionID).toBe("A3");
    });

    it("filters assertions by FAILED result", () => {
      const assertions = [createAssertion("A1", "PASSED"), createAssertion("A2", "FAILED"), createAssertion("A3", "FAILED")];
      const reports = [createReport("XSD Validation", [], assertions)];

      const result = processXSDAssertions(reports, "FAILED");

      expect(result[0].assertions).toHaveLength(2);
      expect(result[0].assertions[0].assertionID).toBe("A2");
      expect(result[0].assertions[1].assertionID).toBe("A3");
    });

    it("filters assertions by UNDEFINED result", () => {
      const assertions = [createAssertion("A1", "UNDEFINED"), createAssertion("A2", "PASSED")];
      const reports = [createReport("XSD Validation", [], assertions)];

      const result = processXSDAssertions(reports, "UNDEFINED");

      expect(result[0].assertions).toHaveLength(1);
      expect(result[0].assertions[0].assertionID).toBe("A1");
    });

    it("returns empty group when no assertions match result", () => {
      const assertions = [createAssertion("A1", "PASSED"), createAssertion("A2", "PASSED")];
      const reports = [createReport("XSD Validation", [], assertions)];

      const result = processXSDAssertions(reports, "FAILED");

      expect(result).toHaveLength(0);
    });
  });

  describe("subReports processing", () => {
    it("processes assertions from subReports", () => {
      const subReport = createReport("SubReport", [], [createAssertion("S1", "PASSED")]);
      const mainReport = createReport("XSD Validation", [], [], [subReport]);

      const result = processXSDAssertions([mainReport], "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].assertions).toHaveLength(1);
      expect(result[0].assertions[0].assertionID).toBe("S1");
      expect(result[0].assertions[0].source).toBe("XSD Validation - SubReport");
    });

    it("combines main report and subReport assertions", () => {
      const subReport = createReport("SubReport", [], [createAssertion("S1", "FAILED")]);
      const mainReport = createReport("XSD Validation", [], [createAssertion("M1", "FAILED")], [subReport]);

      const result = processXSDAssertions([mainReport], "FAILED");

      expect(result).toHaveLength(1);
      expect(result[0].assertions).toHaveLength(2);
      expect(result[0].assertions[0].assertionID).toBe("M1");
      expect(result[0].assertions[0].source).toBe("XSD Validation");
      expect(result[0].assertions[1].assertionID).toBe("S1");
      expect(result[0].assertions[1].source).toBe("XSD Validation - SubReport");
    });

    it("processes multiple subReports", () => {
      const subReport1 = createReport("SubReport1", [], [createAssertion("S1", "PASSED")]);
      const subReport2 = createReport("SubReport2", [], [createAssertion("S2", "PASSED")]);
      const mainReport = createReport("XSD Validation", [], [], [subReport1, subReport2]);

      const result = processXSDAssertions([mainReport], "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].assertions).toHaveLength(2);
      expect(result[0].assertions[0].source).toBe("XSD Validation - SubReport1");
      expect(result[0].assertions[1].source).toBe("XSD Validation - SubReport2");
    });

    it("filters subReport assertions by result", () => {
      const subReport = createReport("SubReport", [], [createAssertion("S1", "PASSED"), createAssertion("S2", "FAILED")]);
      const mainReport = createReport("XSD Validation", [], [], [subReport]);

      const result = processXSDAssertions([mainReport], "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].assertions).toHaveLength(1);
      expect(result[0].assertions[0].assertionID).toBe("S1");
    });

    it("handles subReports without assertions", () => {
      const subReport = createReport("SubReport", [], []);
      const mainReport = createReport("XSD Validation", [], [createAssertion("M1", "PASSED")], [subReport]);

      const result = processXSDAssertions([mainReport], "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].assertions).toHaveLength(1);
      expect(result[0].assertions[0].assertionID).toBe("M1");
    });
  });

  describe("multiple reports", () => {
    it("processes multiple XSD reports", () => {
      const report1 = createReport("XSD Validation", [], [createAssertion("A1", "PASSED")]);
      const report2 = createReport("Another Validation", ["XSD"], [createAssertion("A2", "PASSED")]);

      const result = processXSDAssertions([report1, report2], "PASSED");

      expect(result).toHaveLength(2);
      expect(result[0].reportName).toBe("XSD Validation");
      expect(result[1].reportName).toBe("Another Validation");
    });

    it("filters out non-XSD reports from multiple reports", () => {
      const xsdReport = createReport("XSD Validation", [], [createAssertion("A1", "PASSED")]);
      const otherReport = createReport("JSON Validation", [], [createAssertion("A2", "PASSED")]);

      const result = processXSDAssertions([xsdReport, otherReport], "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].reportName).toBe("XSD Validation");
    });
  });

  describe("edge cases", () => {
    it("handles empty reports array", () => {
      const result = processXSDAssertions([], "PASSED");

      expect(result).toHaveLength(0);
    });

    it("handles report with undefined assertionReports", () => {
      const report: ValidationSubReportDTO = {
        name: "XSD Validation",
        subReportResult: "PASSED",
        subReports: undefined,
        assertionReports: undefined as any,
        subCounters: {
          numberOfAssertions: 0,
          numberOfFailedWithInfos: 0,
          numberOfFailedWithWarnings: 0,
          numberOfFailedWithErrors: 0,
          numberOfUnexpectedErrors: 0,
          numberOfUndefined: 0,
        },
        standards: [],
        unexpectedErrors: [],
      };

      const result = processXSDAssertions([report], "PASSED");

      expect(result).toHaveLength(0);
    });

    it("handles report with empty assertionReports array", () => {
      const report = createReport("XSD Validation", [], []);

      const result = processXSDAssertions([report], "PASSED");

      expect(result).toHaveLength(0);
    });

    it("handles report with undefined subReports", () => {
      const report = createReport("XSD Validation", [], [createAssertion("A1", "PASSED")]);
      report.subReports = undefined;

      const result = processXSDAssertions([report], "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].assertions).toHaveLength(1);
    });

    it("handles subReport with undefined assertionReports", () => {
      const subReport: ValidationSubReportDTO = {
        name: "SubReport",
        subReportResult: "PASSED",
        subReports: undefined,
        assertionReports: undefined as any,
        subCounters: {
          numberOfAssertions: 0,
          numberOfFailedWithInfos: 0,
          numberOfFailedWithWarnings: 0,
          numberOfFailedWithErrors: 0,
          numberOfUnexpectedErrors: 0,
          numberOfUndefined: 0,
        },
        standards: [],
        unexpectedErrors: [],
      };
      const mainReport = createReport("XSD Validation", [], [createAssertion("M1", "PASSED")], [subReport]);

      const result = processXSDAssertions([mainReport], "PASSED");

      expect(result).toHaveLength(1);
      expect(result[0].assertions).toHaveLength(1);
      expect(result[0].assertions[0].assertionID).toBe("M1");
    });

    it("handles empty standards array", () => {
      const report = createReport("Other Name", [], [createAssertion("A1", "PASSED")]);

      const result = processXSDAssertions([report], "PASSED");

      expect(result).toHaveLength(0);
    });
  });

  describe("source field", () => {
    it("sets source field for main report assertions", () => {
      const reports = [createReport("XSD Validation", [], [createAssertion("A1", "PASSED")])];

      const result = processXSDAssertions(reports, "PASSED");

      expect(result[0].assertions[0].source).toBe("XSD Validation");
    });

    it("sets source field with report and subReport names", () => {
      const subReport = createReport("Detailed Check", [], [createAssertion("S1", "FAILED")]);
      const mainReport = createReport("XSD Validation", [], [], [subReport]);

      const result = processXSDAssertions([mainReport], "FAILED");

      expect(result[0].assertions[0].source).toBe("XSD Validation - Detailed Check");
    });

    it("preserves existing source field from assertion (overrides it)", () => {
      const assertion = createAssertion("A1", "PASSED");
      assertion.source = "Original Source";
      const reports = [createReport("XSD Validation", [], [assertion])];

      const result = processXSDAssertions(reports, "PASSED");

      expect(result[0].assertions[0].source).toBe("XSD Validation");
    });
  });
});
