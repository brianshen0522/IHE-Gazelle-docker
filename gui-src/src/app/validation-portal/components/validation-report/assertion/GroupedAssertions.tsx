import Assertion from "./Assertion";
import { AssertionReportDTO } from "@/shared/types/validation/types";

interface GroupedAssertionsProps {
  items: AssertionReportDTO[];
  groupBy?: "severity" | "report";
}

const groupByKey = <T,>(items: T[], keyFn: (item: T) => string): Record<string, T[]> =>
  items.reduce<Record<string, T[]>>((acc, item) => {
    const key = keyFn(item);
    if (!acc[key]) {
      acc[key] = [];
    }
    acc[key].push(item);
    return acc;
  }, {});

const GROUPING_CONFIG = {
  report: {
    key: (item: AssertionReportDTO) => item.source ?? "Unknown",
    renderHeader: (key: string) => <h3 className="font-semibold mb-1">{key.toUpperCase()}</h3>,
  },
  severity: {
    key: (item: AssertionReportDTO) => item.severity ?? "UNKNOWN",
    renderHeader: () => null,
  },
} as const;

const GroupedAssertions = ({ items, groupBy }: GroupedAssertionsProps) => {
  if (!groupBy) {
    return (
      <div className="flex flex-col gap-4">
        {items.map((assertion, index) => (
          <Assertion key={`assertion-undefined-${index}-${assertion.assertionID ?? ""}`} assertion={assertion} />
        ))}
      </div>
    );
  }

  const { key, renderHeader } = GROUPING_CONFIG[groupBy];
  const groupedItems = groupByKey(items, key);

  return (
    <div className="flex flex-col gap-4">
      {Object.entries(groupedItems).map(([groupKey, assertions]) => (
        <div key={`${groupBy}-${groupKey}`}>
          {renderHeader(groupKey)}
          <div className="flex flex-col gap-2">
            {assertions.map((assertion, index) => (
              <Assertion key={`assertion-${groupBy}-${index}-${assertion.assertionID ?? ""}`} assertion={assertion} />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};

export default GroupedAssertions;
