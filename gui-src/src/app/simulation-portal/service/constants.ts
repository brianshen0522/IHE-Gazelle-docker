export const sequenceIndexNameMapping: Record<string, string> = {
  id: "Sequence name",
  serviceName: "Simulation service",
  simulatedRole: "Simulator roles",
  testedRole: "Tested roles",
  standards: "Standards",
  transactions: "Transactions",
  shortDescription: "Summary",
  runnable: "Runnable",
  valid: "Valid",
};

export const sequenceKeys = {
  id: "id" as const,
  serviceName: "serviceName" as const,
  simulatedRole: "simulatedRole" as const,
  testedRole: "testedRole" as const,
  standards: "standards" as const,
  transactions: "transactions" as const,
  shortDescription: "shortDescription" as const,
  runnable: "runnable" as const,
  valid: "valid" as const,
};

export const sequence: Record<string, string> = {
  basePath: "/simulation-portal/sequences",
  reportPath: "/simulation-portal/reports",
}