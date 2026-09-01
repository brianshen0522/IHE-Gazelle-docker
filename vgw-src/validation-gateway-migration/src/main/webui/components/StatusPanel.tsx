import { useState } from "react";
import { MigrationStartMode, MigrationStatus } from "@/types/migration";
import { formatDate } from "@/lib/format";
import { RetryModal } from "./RetryModal";

type StatusPanelProps = {
  status: MigrationStatus | null;
  loading: boolean;
  canStart: boolean;
  runningAction: boolean;
  disabled: boolean;
  failedCount: number;
  hasFailures: boolean;
  onStart: (
    freezeConfirmed: boolean,
    mode: MigrationStartMode,
    ignoreAllMissingInputs?: boolean,
    specificIgnoredOids?: string[]
  ) => Promise<void>;
};

export function StatusPanel({
  status,
  loading,
  canStart,
  runningAction,
  disabled,
  failedCount,
  hasFailures,
  onStart,
}: StatusPanelProps) {
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [showRetryModal, setShowRetryModal] = useState(false);
  const [freezeConfirmed, setFreezeConfirmed] = useState(false);
  const [rerunConfirmed, setRerunConfirmed] = useState(false);
  const [mode, setMode] = useState<MigrationStartMode>("FULL");
  const [ignoreAllMissingInputs, setIgnoreAllMissingInputs] = useState(false);
  const noNewReports = Boolean(status?.migrationCompleted && status.newReportsCountKnown && status.newReportsCount === 0);

  const stateStyles: Record<string, string> = {
    IDLE: "bg-slate-100 text-slate-800 ring-slate-200",
    RUNNING: "bg-sky-100 text-sky-800 ring-sky-200",
    COMPLETED: "bg-emerald-100 text-emerald-800 ring-emerald-200",
    FAILED: "bg-rose-100 text-rose-800 ring-rose-200",
  };

  // Determine display state (show "Completed with Errors" if migration completed but has failures)
  const displayState = status?.state === "COMPLETED" && status.migrationCompleted && hasFailures
    ? "COMPLETED_WITH_ERRORS"
    : status?.state;

  const displayStateStyles: Record<string, string> = {
    ...stateStyles,
    COMPLETED_WITH_ERRORS: "bg-amber-100 text-amber-800 ring-amber-200",
  };

  const displayStateLabel: Record<string, string> = {
    IDLE: "IDLE",
    RUNNING: "RUNNING",
    COMPLETED: "COMPLETED",
    FAILED: "FAILED",
    COMPLETED_WITH_ERRORS: "COMPLETED WITH ERRORS",
  };

  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-[0_12px_40px_-24px_rgba(15,23,42,0.45)] h-fit">
      <h2 className="text-lg font-semibold text-slate-900">Execution Status</h2>

      {loading && <p className="mt-3 text-sm text-slate-600">Loading migration status...</p>}

      {!loading && status && (
        <div className="mt-4 grid gap-2 text-sm text-slate-700">
          <p>
            <span className="font-medium text-slate-500">State</span>
            <span
              className={`ml-2 inline-flex rounded-lg px-2.5 py-1 text-xs font-semibold uppercase tracking-wide ring-1 ${
                displayStateStyles[displayState ?? "IDLE"] ?? "bg-slate-100 text-slate-800 ring-slate-200"
              }`}
            >
              {displayStateLabel[displayState ?? "IDLE"] ?? displayState}
            </span>
          </p>
          <p>
            <span className="font-medium text-slate-500">Message</span>
            <span className="ml-2">{status.message}</span>
          </p>
          <p>
            <span className="font-medium text-slate-500">Completed</span>
            <span className="ml-2">{formatDate(status.completedAt)}</span>
          </p>
          <p>
            <span className="font-medium text-slate-500">Target</span>
            <span className="ml-2 uppercase">{status.targetType}</span>
          </p>
        </div>
      )}

      <div className="mt-5 space-y-3 border-t border-slate-100 pt-4">
        {/* Retry failed reports */}
        {status?.migrationCompleted && hasFailures && (
          <button
            disabled={!canStart || runningAction || disabled}
            onClick={() => {
              setShowRetryModal(true);
            }}
            className="inline-flex w-full items-center justify-center rounded-xl bg-gradient-to-r from-amber-600 to-orange-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:from-amber-700 hover:to-orange-700 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:text-slate-100"
            suppressHydrationWarning
          >
            {runningAction ? "Starting Retry..." : `Retry ${failedCount} Failed Report${failedCount !== 1 ? "s" : ""}`}
          </button>
        )}

        {/* Run all reports */}
        <button
          disabled={!canStart || runningAction || disabled}
          onClick={() => {
            setFreezeConfirmed(false);
            setMode("FULL");
            setIgnoreAllMissingInputs(false);
            setRerunConfirmed(false);
            setShowConfirmModal(true);
          }}
          className="inline-flex w-full items-center justify-center rounded-xl bg-gradient-to-r from-indigo-700 to-blue-700 px-4 py-2.5 text-sm font-semibold text-white transition hover:from-indigo-800 hover:to-blue-800 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:text-slate-100"
          suppressHydrationWarning
        >
          {runningAction ? "Starting..." : status?.migrationCompleted ? "Rerun All Reports" : "Run All Reports"}
        </button>

        {/* Migrate only new reports (shown only after first migration establishes checkpoint) */}
        {status?.newReportsCountKnown && (
          <button
            disabled={!canStart || runningAction || disabled || noNewReports}
            onClick={() => {
              if (noNewReports) {
                return;
              }
              setFreezeConfirmed(false);
              setMode("INCREMENTAL");
              setIgnoreAllMissingInputs(false);
              setRerunConfirmed(false);
              setShowConfirmModal(true);
            }}
            className={`inline-flex w-full items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold text-white transition disabled:cursor-not-allowed disabled:pointer-events-none ${
              noNewReports
                ? "bg-slate-300 text-slate-100"
                : "bg-gradient-to-r from-emerald-600 to-green-600 hover:from-emerald-700 hover:to-green-700 disabled:from-slate-300 disabled:to-slate-300 disabled:text-slate-100"
            }`}
            suppressHydrationWarning
          >
            {noNewReports ? "No new reports" : runningAction ? "Starting..." : "Migrate New Reports"}
          </button>
        )}
      </div>

      {showConfirmModal && status && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4">
          <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-5 shadow-2xl">
            <h3 className="text-lg font-semibold text-slate-900">
              {mode === "INCREMENTAL" ? "Confirm Incremental Migration" : "Confirm Full Migration"}
            </h3>
            <p className="mt-2 text-sm text-slate-600">
              {mode === "FULL"
                ? status.migrationCompleted
                  ? "This will reprocess ALL reports from scratch, including those that succeeded previously."
                  : "Starting migration will process all EVS reports and write them to the configured target."
                : "This will process only reports created after the last successful migration checkpoint."}
            </p>

            {status.migrationCompleted && hasFailures && (
              <div className="mt-3 rounded-lg bg-amber-50 border border-amber-200 p-3">
                <p className="text-sm text-amber-800">
                  <strong>Note:</strong> The previous migration completed with {failedCount} failure{failedCount !== 1 ? "s" : ""}.
                  Consider using &quot;Retry Failed Reports&quot; instead to only reprocess failed reports.
                </p>
              </div>
            )}

            <div className="mt-4 space-y-3">
              <label className="flex items-start gap-2 text-sm font-medium text-slate-700">
                <input
                  type="checkbox"
                  className="mt-0.5 h-4 w-4 rounded border-slate-300 text-indigo-700 focus:ring-indigo-700"
                  checked={freezeConfirmed}
                  onChange={(event) => setFreezeConfirmed(event.target.checked)}
                />
                I confirm the EVS database is frozen before migration start.
              </label>

              {status.migrationCompleted && mode === "FULL" && (
                <label className="flex items-start gap-2 text-sm font-medium text-amber-800">
                  <input
                    type="checkbox"
                    className="mt-0.5 h-4 w-4 rounded border-amber-300 text-amber-700 focus:ring-amber-700"
                    checked={rerunConfirmed}
                    onChange={(event) => setRerunConfirmed(event.target.checked)}
                  />
                  Migration was already completed. I confirm I want to run it again.
                </label>
              )}

              <label className="flex items-start gap-2 text-sm font-medium text-slate-700">
                <input
                  type="checkbox"
                  className="mt-0.5 h-4 w-4 rounded border-slate-300 text-indigo-700 focus:ring-indigo-700"
                  checked={ignoreAllMissingInputs}
                  onChange={(event) => setIgnoreAllMissingInputs(event.target.checked)}
                />
                Ignore all missing inputs (create validation reports without input attachments)
              </label>
            </div>

            <div className="mt-5 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowConfirmModal(false)}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={!freezeConfirmed || (status.migrationCompleted && mode === "FULL" && !rerunConfirmed) || runningAction}
                onClick={() => {
                  void onStart(freezeConfirmed, mode, ignoreAllMissingInputs);
                  setShowConfirmModal(false);
                }}
                className="rounded-lg bg-indigo-700 px-3 py-2 text-sm font-semibold text-white hover:opacity-90 disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                {runningAction ? "Starting..." : "Confirm & Start"}
              </button>
            </div>
          </div>
        </div>
      )}

      <RetryModal
        isOpen={showRetryModal}
        onClose={() => setShowRetryModal(false)}
        failedCount={failedCount}
        onConfirm={(specificIgnoredOids) => {
          void onStart(true, "RETRY_FAILED_ONLY", false, specificIgnoredOids);
          setShowRetryModal(false);
        }}
      />
    </section>
  );
}
