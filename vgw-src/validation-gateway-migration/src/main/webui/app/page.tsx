"use client";

import { AlertBanner } from "@/components/AlertBanner";
import { ErrorTable } from "@/components/ErrorTable";
import { HeroCard } from "@/components/HeroCard";
import { IncrementalStatsPanel } from "@/components/IncrementalStatsPanel";
import { PreviewTable } from "@/components/PreviewTable";
import { ProgressPanel } from "@/components/ProgressPanel";
import { StatsCards } from "@/components/StatsCards";
import { StatusPanel } from "@/components/StatusPanel";
import { VerificationPanel } from "@/components/VerificationPanel";
import { useMigrationDashboard } from "@/hooks/useMigrationDashboard";

export default function Home() {
  const dashboard = useMigrationDashboard();

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_0%_0%,#dbeafe_0%,#f8fafc_38%,#fefce8_100%)] px-4 py-8 text-slate-900 md:px-6">
      <div className="mx-auto grid w-full max-w-7xl gap-5">
        <HeroCard />

        {dashboard.dbAlert && (
          <AlertBanner text={`${dashboard.dbAlert} All actions are disabled until EVS DB is reachable.`} tone="warning" />
        )}
        {dashboard.targetAlert && (
          <AlertBanner
            text={`${dashboard.targetAlert} All actions are disabled until the ${dashboard.status?.targetType ?? "target"} backend is reachable.`}
            tone="warning"
          />
        )}
        {dashboard.error && <AlertBanner text={dashboard.error} tone="danger" />}

        <div className="grid gap-5 xl:grid-cols-[340px_1fr]">
          <div className="grid gap-5 content-start">
            <StatusPanel
              status={dashboard.status}
              loading={dashboard.loading}
              canStart={dashboard.canStart}
              runningAction={dashboard.runningAction}
              disabled={dashboard.actionsDisabled}
              failedCount={dashboard.failedCount}
              hasFailures={dashboard.hasFailures}
              onStart={dashboard.startMigration}
            />
            <StatsCards stats={dashboard.stats} />
            <IncrementalStatsPanel status={dashboard.status} />
          </div>

          <div className="grid gap-5 content-start">
            <ProgressPanel progress={dashboard.progress} />
            <PreviewTable summary={dashboard.summary} />
            <VerificationPanel
              verification={dashboard.verification}
              running={dashboard.runningVerification}
              disabled={dashboard.actionsDisabled || !dashboard.canRunVerification}
              migrationCompleted={Boolean(dashboard.status?.migrationCompleted)}
              targetType={dashboard.status?.targetType}
              onRun={dashboard.runVerification}
            />
            <ErrorTable summary={dashboard.summary} />
          </div>
        </div>
      </div>
    </main>
  );
}
