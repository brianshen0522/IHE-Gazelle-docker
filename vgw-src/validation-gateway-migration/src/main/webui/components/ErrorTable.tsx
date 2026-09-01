import { formatDate } from "@/lib/format";
import { MigrationSummary } from "@/types/migration";

type ErrorTableProps = {
  summary: MigrationSummary | null;
};

export function ErrorTable({ summary }: ErrorTableProps) {
  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-[0_12px_40px_-24px_rgba(15,23,42,0.45)]">
      <h2 className="text-lg font-semibold text-slate-900">Recent Errors</h2>
      {!summary || summary.errors.length === 0 ? (
        <p className="mt-3 text-sm text-slate-600">No migration error recorded.</p>
      ) : (
        <div className="mt-4 overflow-x-auto rounded-2xl border border-slate-100">
          <table className="min-w-full bg-white text-sm">
            <thead className="bg-rose-50/70">
              <tr className="text-left text-[11px] uppercase tracking-[0.14em] text-rose-700/80">
                <th className="px-3 py-2">EVS OID</th>
                <th className="px-3 py-2">Time</th>
                <th className="px-3 py-2">Error</th>
              </tr>
            </thead>
            <tbody>
              {summary.errors.slice(0, 20).map((item, index) => (
                <tr key={`${item.evsOid}-${item.occurredAt}-${index}`} className="border-t border-slate-100 text-slate-700">
                  <td className="px-3 py-2 font-mono text-xs">{item.evsOid}</td>
                  <td className="px-3 py-2">{formatDate(item.occurredAt)}</td>
                  <td className="px-3 py-2 text-rose-700">{item.message}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
