import { render, screen } from "@testing-library/react";
import { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";
import { ValidationOverview } from "./ValidationOverview";
import { ValidationReportDTO } from "@/shared/types/validation/types";

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) =>
      (
        {
          "gzl.texec.validation_overview": "Validation overview",
          "gzl.texec.validation_report": "Validation report",
          "gzl.texec.validation_subreport": "Validation subreport",
        } as Record<string, string>
      )[key] ?? key,
  }),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  CollapsableSubCard: ({ title, children }: { title: ReactNode; children: ReactNode }) => (
    <section>
      <div>{title}</div>
      <div>{children}</div>
    </section>
  ),
  IssueCounter: () => <div>IssueCounter</div>,
}));

vi.mock("@shared/components/report/ResultBadge", () => ({
  ResultBadge: ({ result }: { result: string }) => <span>{result}</span>,
}));

vi.mock("@validation-portal/components/validation-report/UnexpectedErrorDisplay", () => ({
  default: ({ errors }: { errors: { message?: string }[] }) => <div>{errors.map((error) => error.message).join(", ")}</div>,
}));

describe("ValidationOverview", () => {
  it("renders nested subreports recursively", () => {
    const report = {
      modelVersion: "2.1",
      uuid: "report-id",
      dateTime: "2026-03-10T13:32:07.651Z",
      disclaimer: "disclaimer",
      overallResult: "PASSED",
      validationMethod: {
        validationServiceName: "svc",
        validationServiceVersion: "1",
        validationProfileID: "profile",
        validationProfileVersion: "1",
      },
      reports: [
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
          unexpectedErrors: [],
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
              assertionReports: [],
              unexpectedErrors: [],
              subReports: [
                {
                  name: "Deep node",
                  subReportResult: "FAILED",
                  subCounters: {
                    numberOfAssertions: 1,
                    numberOfFailedWithInfos: 0,
                    numberOfFailedWithWarnings: 0,
                    numberOfFailedWithErrors: 1,
                    numberOfUnexpectedErrors: 1,
                    numberOfUndefined: 0,
                  },
                  assertionReports: [],
                  unexpectedErrors: [{ name: "NestedError", message: "Nested unexpected error" }],
                  subReports: [],
                  standards: [],
                },
              ],
              standards: [],
            },
          ],
          standards: [],
        },
      ],
      counters: {
        numberOfAssertions: 1,
        numberOfFailedWithInfos: 0,
        numberOfFailedWithWarnings: 0,
        numberOfFailedWithErrors: 0,
        numberOfUnexpectedErrors: 1,
        numberOfUndefined: 0,
      },
      additionalMetadata: [],
    } satisfies ValidationReportDTO;

    render(<ValidationOverview validationReport={report} />);

    expect(screen.getByText("Schematron Validation")).toBeInTheDocument();
    expect(screen.getByText("Active pattern")).toBeInTheDocument();
    expect(screen.getByText("Deep node")).toBeInTheDocument();
    expect(screen.getByText("Nested unexpected error")).toBeInTheDocument();
  });
});
