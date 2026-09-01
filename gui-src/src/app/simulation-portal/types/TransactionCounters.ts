import { TransactionReport } from "@simulation-portal/types/SimulationReport";

export interface TransactionCounters {
  numberOfPassedTransactions: number;
  numberOfFailedTransactions: number;
  numberOfUndefinedTransactions: number;
  numberOfTransactions: number;
}

export const computeTransactionCounters = (transactionReports?: TransactionReport[]): TransactionCounters => {
  if (!transactionReports) {
    return {
      numberOfPassedTransactions: 0,
      numberOfFailedTransactions: 0,
      numberOfUndefinedTransactions: 0,
      numberOfTransactions: 0,
    };
  }

  const counts = transactionReports.reduce(
    (acc, report) => {
      if (report.result === "PASSED") acc.numberOfPassedTransactions++;
      if (report.result === "FAILED") acc.numberOfFailedTransactions++;
      if (report.result === "UNDEFINED") acc.numberOfUndefinedTransactions++;
      return acc;
    },
    { numberOfPassedTransactions: 0, numberOfFailedTransactions: 0, numberOfUndefinedTransactions: 0 },
  );

  return {
    ...counts,
    numberOfTransactions: transactionReports.length,
  };
};
