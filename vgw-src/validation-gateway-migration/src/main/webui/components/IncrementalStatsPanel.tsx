import { formatDate } from "@/lib/format";
import { MigrationStatus } from "@/types/migration";

type IncrementalStatsPanelProps = {
  status: MigrationStatus | null;
};

export function IncrementalStatsPanel({ status }: IncrementalStatsPanelProps) {
  return (
    <section className="rounded-3xl border border-emerald-200 bg-white p-5 shadow-[0_12px_40px_-24px_rgba(5,150,105,0.35)] h-fit">
      <h2 className="text-lg font-semibold text-slate-900">Incremental Stats</h2>

      {!status?.newReportsCountKnown ? (
        <p className="mt-3 text-sm text-slate-600">
          Incremental cursor not initialized yet. Run a migration once to track newly added reports.
        </p>
      ) : (
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <Card
            label="New reports pending"
            value={status.newReportsCount.toLocaleString()}
            accent="from-emerald-50 to-green-50"
          />
          <Card
            label="Status"
            value={status.newReportsCount === 0 ? "Up to date" : "Pending migration"}
            accent="from-lime-50 to-emerald-50"
          />
          <Card
            label="Oldest new report"
            value={formatDate(status.oldestNewReportDate)}
            accent="from-teal-50 to-cyan-50"
          />
          <Card
            label="Newest new report"
            value={formatDate(status.newestNewReportDate)}
            accent="from-green-50 to-emerald-100"
          />
        </div>
      )}
    </section>
  );
}

function Card({ label, value, accent }: { label: string; value: string; accent: string }) {
  return (
    <article className={`rounded-2xl border border-emerald-100 bg-gradient-to-br p-4 ${accent}`}>
      <p className="text-[11px] uppercase tracking-[0.18em] text-slate-500">{label}</p>
      <p className="mt-2 text-lg font-semibold text-slate-900">{value}</p>
    </article>
  );
}
