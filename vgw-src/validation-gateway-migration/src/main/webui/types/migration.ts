export type PreMigrationStats = {
  totalReports: number;
  oldestReportDate: string | null;
  newestReportDate: string | null;
  totalSizeBytes: number;
};

export type MigrationStatus = {
  state: "IDLE" | "RUNNING" | "COMPLETED" | "FAILED";
  migrationCompleted: boolean;
  completedAt: string | null;
  message: string;
  evsDatabaseAccessible: boolean;
  evsDatabaseMessage: string;
  targetType: string;
  targetAccessible: boolean;
  targetMessage: string;
  newReportsCountKnown: boolean;
  newReportsCount: number;
  oldestNewReportDate: string | null;
  newestNewReportDate: string | null;
};

export type MigrationStartMode = "FULL" | "INCREMENTAL" | "RETRY_FAILED_ONLY";

export type MigrationPreview = {
  evsOid: string;
  itemId: string;
  result: string;
  validationDate: string;
};

export type MigrationErrorType =
  | "MISSING_INPUT"
  | "MISSING_VALIDATION_REPORT"
  | "FILE_PARSING_ERROR"
  | "DATAHOUSE_ERROR"
  | "UNKNOWN_ERROR";

export type MigrationError = {
  evsOid: string;
  message: string;
  occurredAt: string;
  type: MigrationErrorType;
};

export type MigrationProgress = {
  total: number;
  processed: number;
  succeeded: number;
  failed: number;
  percentage: number;
  reportsPerSecond: number;
  elapsedSeconds: number;
  etaSeconds: number | null;
  previews: MigrationPreview[];
  recentErrors: MigrationError[];
};

export type MigrationSummary = {
  migrationCompleted: boolean;
  completedAt: string | null;
  total: number;
  succeeded: number;
  failed: number;
  previews: MigrationPreview[];
  errors: MigrationError[];
};

export type VerificationResult = {
  evsOid: string;
  foundInDatahouse: boolean;
  itemId: string | null;
  hasInput: boolean;
};

export type FailedCountResponse = {
  count: number;
  hasFailures: boolean;
};

export type FailedReportsResponse = {
  errors: MigrationError[];
  total: number;
};

export type DashboardData = {
  status: MigrationStatus | null;
  stats: PreMigrationStats | null;
  progress: MigrationProgress | null;
  summary: MigrationSummary | null;
  verification: VerificationResult[];
  loading: boolean;
  runningAction: boolean;
  runningVerification: boolean;
  error: string | null;
  dbAlert: string | null;
  targetAlert: string | null;
  actionsDisabled: boolean;
  canStart: boolean;
  canRunVerification: boolean;
  failedCount: number;
  hasFailures: boolean;
  startMigration: (
    freezeConfirmed: boolean,
    mode: MigrationStartMode,
    ignoreAllMissingInputs?: boolean,
    specificIgnoredOids?: string[]
  ) => Promise<void>;
  runVerification: () => Promise<void>;
};
