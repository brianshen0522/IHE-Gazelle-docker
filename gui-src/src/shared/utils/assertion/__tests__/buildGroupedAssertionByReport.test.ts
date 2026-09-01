import { describe, expect, it } from "vitest";
import { buildGroupedAssertionByReport } from "../buildGroupedAssertionByReport";
import { ValidationSubReportDTO } from "@/shared/types/validation/types";

describe("buildGroupedAssertionByReport", () => {
  it("collects assertions from the full recursive report tree", () => {
    const reports: ValidationSubReportDTO[] = [
      {
        name: "Schematron Validation",
        subReportResult: "PASSED",
        subCounters: {
          numberOfAssertions: 1,
          numberOfFailedWithInfos: 0,
          numberOfFailedWithWarnings: 0,
          numberOfFailedWithErrors: 0,
          numberOfUnexpectedErrors: 0,
          numberOfUndefined: 0,
        },
        assertionReports: [],
        subReports: [
          {
            name: "Active pattern",
            subReportResult: "PASSED",
            subCounters: {
              numberOfAssertions: 1,
              numberOfFailedWithInfos: 0,
              numberOfFailedWithWarnings: 0,
              numberOfFailedWithErrors: 0,
              numberOfUnexpectedErrors: 0,
              numberOfUndefined: 0,
            },
            assertionReports: [{ result: "PASSED", severity: "INFO", description: "Nested assertion" }],
            subReports: [
              {
                name: "Deep node",
                subReportResult: "FAILED",
                subCounters: {
                  numberOfAssertions: 1,
                  numberOfFailedWithInfos: 0,
                  numberOfFailedWithWarnings: 0,
                  numberOfFailedWithErrors: 1,
                  numberOfUnexpectedErrors: 0,
                  numberOfUndefined: 0,
                },
                assertionReports: [{ result: "FAILED", severity: "ERROR", description: "Deep assertion" }],
                subReports: [],
                standards: [],
                unexpectedErrors: [],
              },
            ],
            standards: [],
            unexpectedErrors: [],
          },
        ],
        standards: [],
        unexpectedErrors: [],
      },
    ];

    expect(buildGroupedAssertionByReport(reports, "PASSED")).toEqual([
      {
        reportName: "Schematron Validation / Active pattern",
        assertions: [{ result: "PASSED", severity: "INFO", description: "Nested assertion" }],
      },
    ]);

    expect(buildGroupedAssertionByReport(reports, "FAILED")).toEqual([
      {
        reportName: "Schematron Validation / Active pattern / Deep node",
        assertions: [{ result: "FAILED", severity: "ERROR", description: "Deep assertion" }],
      },
    ]);
  });
});
