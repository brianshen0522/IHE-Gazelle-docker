import { formatDate } from "@/lib/format";
import { MigrationSummary } from "@/types/migration";

type PreviewTableProps = {
  summary: MigrationSummary | null;
};

export function PreviewTable({ summary }: PreviewTableProps) {
  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-[0_12px_40px_-24px_rgba(15,23,42,0.45)]">
      <h2 className="text-lg font-semibold text-slate-900">Recently Migrated Reports</h2>
      {!summary || summary.previews.length === 0 ? (
        <p className="mt-3 text-sm text-slate-600">No migrated report preview yet.</p>
      ) : (
        <div className="mt-4 overflow-x-auto rounded-2xl border border-slate-100">
          <table className="min-w-full bg-white text-sm">
            <thead className="bg-slate-50">
              <tr className="text-left text-[11px] uppercase tracking-[0.14em] text-slate-500">
                <th className="px-3 py-2">EVS OID</th>
                <th className="px-3 py-2">Item ID</th>
                <th className="px-3 py-2">Result</th>
                <th className="px-3 py-2">Date</th>
              </tr>
            </thead>
            <tbody>
              {summary.previews.map((preview) => (
                <tr key={preview.itemId} className="border-t border-slate-100 text-slate-700">
                  <td className="px-3 py-2 font-mono text-xs">{preview.evsOid}</td>
                  <td className="px-3 py-2 font-mono text-xs">{preview.itemId}</td>
                  <td className="px-3 py-2">
                    <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                      {preview.result}
                    </span>
                  </td>
                  <td className="px-3 py-2">{formatDate(preview.validationDate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
