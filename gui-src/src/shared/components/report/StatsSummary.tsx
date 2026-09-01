import { CounterGauge } from "@gazelle/gazelle-component-ui";

export interface StatItem {
  id: string;
  count: number;
  label: string;
  color: string;
}

interface StatsSummaryProps {
  title: string;
  totalCount: number;
  items: StatItem[];
}

export const StatsSummary = ({ title, totalCount, items }: StatsSummaryProps) => {
  const computePercentOfCategorizedIssues = (numberOfIssue: number, totalIssues: number): number => {
    const result = totalIssues > 0 ? (numberOfIssue / totalIssues) * 100 : 0;
    return Number.isNaN(result) ? 0 : result;
  };

  return (
    <div className="flex justify-center gap-8 border border-grey rounded-md shadow-sm text-center p-4">
      <div className="flex gap-2 items-center">
        <b>{title}</b>
        <b className="font-semibold">{totalCount}</b>
      </div>

      {items.map((item) => (
        <div key={item.id} className="flex flex-col items-center">
          <div className="flex gap-1">
            <b>{item.count}</b>
            {item.label}
          </div>
          <CounterGauge percent={computePercentOfCategorizedIssues(item.count, totalCount)} color={item.color} />
        </div>
      ))}
    </div>
  );
};
