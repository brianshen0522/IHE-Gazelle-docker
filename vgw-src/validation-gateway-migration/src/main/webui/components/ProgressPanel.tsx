import { formatDuration } from "@/lib/format";
import { MigrationProgress } from "@/types/migration";

type ProgressPanelProps = {
  progress: MigrationProgress | null;
};

export function ProgressPanel({ progress }: ProgressPanelProps) {
  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-[0_12px_40px_-24px_rgba(15,23,42,0.45)]">
      <h2 className="text-lg font-semibold text-slate-900">Migration Progress</h2>
      {!progress ? (
        <p className="mt-3 text-sm text-slate-600">Migration has not started.</p>
      ) : (
        <>
          <div className="mt-4 flex items-center justify-between text-sm text-slate-700">
            <span>
              {progress.processed.toLocaleString()} / {progress.total.toLocaleString()} processed
            </span>
            <span className="font-semibold text-slate-900">{progress.percentage.toFixed(1)}%</span>
          </div>

          <div className="mt-2 h-3 overflow-hidden rounded-full bg-slate-200">
            <div
              className="h-full rounded-full bg-gradient-to-r from-indigo-600 via-blue-600 to-cyan-600 transition-all duration-500"
              style={{ width: `${Math.min(progress.percentage, 100)}%` }}
            />
          </div>

          <div className="mt-5 grid gap-3 text-sm sm:grid-cols-2 xl:grid-cols-5">
            <Metric label="Succeeded" value={String(progress.succeeded)} />
            <Metric label="Failed" value={String(progress.failed)} />
            <Metric label="Rate" value={`${progress.reportsPerSecond.toFixed(2)} reports/s`} />
            <Metric label="Elapsed" value={formatDuration(progress.elapsedSeconds)} />
            <Metric label="ETA" value={formatDuration(progress.etaSeconds)} />
          </div>
        </>
      )}
    </section>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <article className="rounded-xl border border-slate-100 bg-slate-50 px-3 py-2 text-slate-700">
      <p className="text-[11px] uppercase tracking-[0.12em] text-slate-500">{label}</p>
      <p className="mt-1 font-semibold text-slate-900">{value}</p>
    </article>
  );
}
