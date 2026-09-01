import { formatBytes, formatDate } from "@/lib/format";
import { PreMigrationStats } from "@/types/migration";

type StatsCardsProps = {
  stats: PreMigrationStats | null;
};

export function StatsCards({ stats }: StatsCardsProps) {
  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-[0_12px_40px_-24px_rgba(15,23,42,0.45)] h-fit">
      <h2 className="text-lg font-semibold text-slate-900">Pre-migration Snapshot</h2>
      {!stats ? (
        <p className="mt-3 text-sm text-slate-600">Statistics unavailable.</p>
      ) : (
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <Card label="Reports" value={stats.totalReports.toLocaleString()} accent="from-blue-50 to-indigo-50" />
          <Card label="Estimated size" value={formatBytes(stats.totalSizeBytes)} accent="from-cyan-50 to-teal-50" />
          <Card label="Oldest report" value={formatDate(stats.oldestReportDate)} accent="from-violet-50 to-fuchsia-50" />
          <Card label="Newest report" value={formatDate(stats.newestReportDate)} accent="from-amber-50 to-orange-50" />
        </div>
      )}
    </section>
  );
}

function Card({ label, value, accent }: { label: string; value: string; accent: string }) {
  return (
    <article className={`rounded-2xl border border-slate-100 bg-gradient-to-br p-4 ${accent}`}>
      <p className="text-[11px] uppercase tracking-[0.18em] text-slate-500">{label}</p>
      <p className="mt-2 text-lg font-semibold text-slate-900">{value}</p>
    </article>
  );
}
