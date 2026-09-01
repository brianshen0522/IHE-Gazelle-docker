import { useMemo } from "react";
import { AssertionReportDTO } from "@/shared/types/validation/types";
import { AssertionGroup } from "../components/validation-report/types";

const severityOrder: Record<string, number> = { ERROR: 0, WARNING: 1, INFO: 2 };

const compareBySeverity = (a: AssertionReportDTO, b: AssertionReportDTO) =>
  (severityOrder[a.severity] ?? 3) - (severityOrder[b.severity] ?? 3);

const worstSeverity = (group: AssertionGroup) =>
  Math.min(...group.assertions.map((a) => severityOrder[a.severity] ?? 3));

export const useAssertionsData = (reports: AssertionGroup[] | undefined, type: string) => {
  return useMemo(() => {
    if (!reports?.length) {
      return { assertions: [], total: 0 };
    }

    const normalizedGroups =
      type === "FAILED"
        ? [...reports]
            .sort((a, b) => worstSeverity(a) - worstSeverity(b))
            .map((group) => ({
              ...group,
              assertions: [...group.assertions].sort(compareBySeverity),
            }))
        : reports;

    const assertions = normalizedGroups.flatMap((group, gIdx) =>
      group.assertions.map((assertion, aIdx) => ({
        ...assertion,
        source: group.reportName ?? "Unknown",
        id: `${gIdx}-${aIdx}-${assertion.assertionID ?? ""}`,
      })),
    );

    const total = normalizedGroups.reduce((acc, g) => acc + g.assertions.length, 0);

    return { assertions, total };
  }, [reports, type]);
};
