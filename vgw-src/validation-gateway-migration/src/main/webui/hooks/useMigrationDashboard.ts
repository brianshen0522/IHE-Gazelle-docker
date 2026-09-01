"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  fetchProgress,
  fetchStats,
  fetchStatus,
  fetchSummary,
  fetchFailedCount,
  runVerification as runVerificationRequest,
  startMigration as startMigrationRequest,
} from "@/lib/api";
import {
  DashboardData,
  MigrationProgress,
  MigrationStartMode,
  MigrationStatus,
  MigrationSummary,
  PreMigrationStats,
  VerificationResult,
} from "@/types/migration";

export function useMigrationDashboard(): DashboardData {
  const [mounted, setMounted] = useState(false);
  const [status, setStatus] = useState<MigrationStatus | null>(null);
  const [stats, setStats] = useState<PreMigrationStats | null>(null);
  const [progress, setProgress] = useState<MigrationProgress | null>(null);
  const [summary, setSummary] = useState<MigrationSummary | null>(null);
  const [verification, setVerification] = useState<VerificationResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [runningAction, setRunningAction] = useState(false);
  const [runningVerification, setRunningVerification] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [failedCount, setFailedCount] = useState(0);
  const [hasFailures, setHasFailures] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const refresh = useCallback(async () => {
    try {
      const statusData = await fetchStatus();
      setStatus(statusData);

      if (!statusData.evsDatabaseAccessible || !statusData.targetAccessible) {
        setLoading(false);
        setError(null);
        return;
      }

      const [statsData, progressData, summaryData, failedCountData] = await Promise.all([
        fetchStats(),
        fetchProgress(),
        fetchSummary(),
        fetchFailedCount(),
      ]);

      setStats(statsData);
      setProgress(progressData);
      setSummary(summaryData);
      setFailedCount(failedCountData.count);
      setHasFailures(failedCountData.hasFailures);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to load migration data");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
    const id = setInterval(() => {
      void refresh();
    }, 2500);
    return () => clearInterval(id);
  }, [refresh]);

  const startMigration = useCallback(
    async (
      freezeConfirmed: boolean,
      mode: MigrationStartMode,
      ignoreAllMissingInputs?: boolean,
      specificIgnoredOids?: string[]
    ) => {
      if (!status?.evsDatabaseAccessible || !status?.targetAccessible) {
        return;
      }
      setRunningAction(true);
      try {
        await startMigrationRequest(
          freezeConfirmed,
          mode,
          ignoreAllMissingInputs,
          specificIgnoredOids
        );
        await refresh();
      } catch (e) {
        setError(e instanceof Error ? e.message : "Unable to start migration");
      } finally {
        setRunningAction(false);
      }
    },
    [refresh, status?.evsDatabaseAccessible, status?.targetAccessible]
  );

  const runVerification = useCallback(async () => {
    if (!status?.evsDatabaseAccessible || !status?.targetAccessible) {
      return;
    }
    setRunningVerification(true);
    try {
      const result = await runVerificationRequest(10);
      setVerification(result);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to run verification");
    } finally {
      setRunningVerification(false);
    }
  }, [status?.evsDatabaseAccessible, status?.targetAccessible]);

  const canStart = useMemo(() => {
    if (!status) {
      return false;
    }
    return (
      status.evsDatabaseAccessible &&
      status.targetAccessible &&
      status.state !== "RUNNING"
    );
  }, [status]);

  const canRunVerification = useMemo(() => {
    if (!status) {
      return false;
    }
    return (
      status.evsDatabaseAccessible &&
      status.targetAccessible &&
      status.migrationCompleted
    );
  }, [status]);

  const dbAlert = useMemo(() => {
    if (!status || status.evsDatabaseAccessible) {
      return null;
    }
    return status.evsDatabaseMessage || "EVS database is not accessible.";
  }, [status]);

  const targetAlert = useMemo(() => {
    if (!status || status.targetAccessible) {
      return null;
    }
    return status.targetMessage || "Migration target is not accessible.";
  }, [status]);

  const actionsDisabled = useMemo(() => Boolean(dbAlert) || Boolean(targetAlert), [dbAlert, targetAlert]);

  return {
    status,
    stats,
    progress,
    summary,
    verification,
    loading: loading || !mounted,
    runningAction,
    runningVerification,
    error,
    dbAlert,
    targetAlert,
    actionsDisabled,
    canStart,
    canRunVerification,
    failedCount,
    hasFailures,
    startMigration,
    runVerification,
  };
}
