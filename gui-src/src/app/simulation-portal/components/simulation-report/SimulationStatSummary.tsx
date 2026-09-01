import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { computeTransactionCounters, TransactionCounters } from "@simulation-portal/types/TransactionCounters";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import { StatItem, StatsSummary } from "@shared/components/report/StatsSummary";
import { Skeleton } from "@gazelle/gazelle-component-ui";

const SimulationStatSummary = () => {
  const { t } = useTranslation();
  const { simulationReport } = useSequenceExecutionContext();
  const [transactionCounters, setTransactionCounters] = useState<TransactionCounters | null>(null);

  useEffect(() => {
    if (simulationReport?.transactionReports) {
      const counters = computeTransactionCounters(simulationReport.transactionReports);
      setTransactionCounters(counters);
    }
  }, [simulationReport]);

  if (!simulationReport || !transactionCounters) {
    return <Skeleton className="h-screen"/>;
  }

  const totalIssues = transactionCounters.numberOfTransactions ?? 0;

  const items: StatItem[] = [
    {
      id: "passed",
      count: transactionCounters.numberOfPassedTransactions ?? 0,
      label: "passed",
      color: "#599C35",
    },
    {
      id: "failed",
      count: transactionCounters.numberOfFailedTransactions ?? 0,
      label: "failed",
      color: "#cc3333",
    },
    {
      id: "undefined",
      count: transactionCounters.numberOfUndefinedTransactions ?? 0,
      label: t("gzl.texec.undefined"),
      color: "#6f6f6f",
    },
  ];

  return (
    <StatsSummary
      title={"Number of transactions"}
      totalCount={totalIssues}
      items={items}
    />
  );
};

export default SimulationStatSummary;