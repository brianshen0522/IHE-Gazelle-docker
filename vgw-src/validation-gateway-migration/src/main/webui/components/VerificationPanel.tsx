import { useMemo, useState } from "react";
import { downloadMigratedInput, fetchMigratedItem } from "@/lib/api";
import { VerificationResult } from "@/types/migration";

type VerificationPanelProps = {
  verification: VerificationResult[];
  running: boolean;
  disabled: boolean;
  migrationCompleted: boolean;
  targetType?: string;
  onRun: () => Promise<void>;
};

function highlightJson(value: unknown): string {
  const json = JSON.stringify(value, null, 2) ?? "";
  const escaped = json
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
  return escaped.replace(
    /("(?:\\u[a-fA-F0-9]{4}|\\[^u]|[^\\"])*"\s*:?)|(\btrue\b|\bfalse\b|\bnull\b)|(-?\d+(?:\.\d+)?(?:[eE][+\-]?\d+)?)/g,
    (match, stringToken, booleanOrNullToken, numberToken) => {
      if (stringToken) {
        if (stringToken.endsWith(":")) {
          return `<span class="text-sky-300">${match}</span>`;
        }
        return `<span class="text-emerald-300">${match}</span>`;
      }
      if (booleanOrNullToken) {
        return `<span class="text-fuchsia-300">${match}</span>`;
      }
      if (numberToken) {
        return `<span class="text-amber-300">${match}</span>`;
      }
      return match;
    }
  );
}

export function VerificationPanel({ verification, running, disabled, migrationCompleted, targetType, onRun }: VerificationPanelProps) {
  const [selectedItemId, setSelectedItemId] = useState<string | null>(null);
  const [itemJson, setItemJson] = useState<unknown>(null);
  const [loadingItem, setLoadingItem] = useState(false);
  const [itemError, setItemError] = useState<string | null>(null);
  const [downloadingItemId, setDownloadingItemId] = useState<string | null>(null);
  const [downloadError, setDownloadError] = useState<string | null>(null);

  const highlightedJson = useMemo(() => (itemJson == null ? "" : highlightJson(itemJson)), [itemJson]);

  async function handleView(itemId: string) {
    setSelectedItemId(itemId);
    setLoadingItem(true);
    setItemError(null);
    try {
      const payload = await fetchMigratedItem(itemId);
      setItemJson(payload);
    } catch (e) {
      setItemJson(null);
      setItemError(e instanceof Error ? e.message : "Unable to load item JSON");
    } finally {
      setLoadingItem(false);
    }
  }

  async function handleDownloadInput(itemId: string) {
    setDownloadingItemId(itemId);
    setDownloadError(null);
    try {
      await downloadMigratedInput(itemId);
    } catch (e) {
      setDownloadError(e instanceof Error ? e.message : "Unable to download input attachment");
    } finally {
      setDownloadingItemId(null);
    }
  }

  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-[0_12px_40px_-24px_rgba(15,23,42,0.45)]">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-semibold text-slate-900">Verification Spot-check</h2>
          <p className="text-sm text-slate-600">
            Sample 10 random EVS OIDs and verify they exist in the configured target.
            {!migrationCompleted ? " Spot-check becomes available once migration is completed." : ""}
          </p>
        </div>
        <button
          disabled={running || disabled}
          onClick={() => {
            void onRun();
          }}
          className="inline-flex items-center justify-center rounded-xl border border-indigo-600 bg-indigo-50 px-4 py-2 text-sm font-semibold text-indigo-800 transition hover:bg-indigo-100 disabled:cursor-not-allowed disabled:border-slate-300 disabled:bg-slate-100 disabled:text-slate-400"
        >
          {running ? "Checking..." : "Run Spot-check"}
        </button>
      </div>

      {verification.length > 0 && (
        <div className="mt-4 overflow-x-auto rounded-2xl border border-slate-100">
          <table className="min-w-full bg-white text-sm">
            <thead className="bg-slate-50">
              <tr className="text-left text-[11px] uppercase tracking-[0.14em] text-slate-500">
                <th className="px-3 py-2">EVS OID</th>
                <th className="px-3 py-2">Found</th>
                <th className="px-3 py-2">Item ID</th>
                  <th className="px-3 py-2">Actions</th>
                </tr>
              </thead>
              <tbody>
              {verification.map((item) => (
                <tr key={item.evsOid} className="border-t border-slate-100 text-slate-700">
                  <td className="px-3 py-2 font-mono text-xs">{item.evsOid}</td>
                  <td className="px-3 py-2">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                        item.foundInDatahouse ? "bg-emerald-100 text-emerald-800" : "bg-rose-100 text-rose-800"
                      }`}
                    >
                      {item.foundInDatahouse ? "Yes" : "No"}
                    </span>
                  </td>
                  <td className="px-3 py-2 font-mono text-xs">{item.itemId ?? "-"}</td>
                  <td className="px-3 py-2">
                    <div className="flex items-center gap-2">
                      <button
                        disabled={!item.itemId}
                        onClick={() => {
                          if (item.itemId) {
                            void handleView(item.itemId);
                          }
                        }}
                        className="rounded-lg border border-slate-300 bg-white px-2.5 py-1 text-xs font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400"
                      >
                        View
                      </button>
                      <button
                        disabled={!item.itemId || !item.hasInput || targetType !== "mongo" || downloadingItemId === item.itemId}
                        onClick={() => {
                          if (item.itemId && item.hasInput && targetType === "mongo") {
                            void handleDownloadInput(item.itemId);
                          }
                        }}
                        className="rounded-lg border border-slate-300 bg-white px-2.5 py-1 text-xs font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400"
                        title={!item.hasInput ? "No input attachment available" : undefined}
                      >
                        {downloadingItemId === item.itemId ? "Downloading..." : "Download input"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {targetType === "datahouse" && verification.length > 0 && (
        <p className="mt-3 text-xs font-medium text-amber-700">
          Input download is not implemented for datahouse target.
        </p>
      )}
      {downloadError && <p className="mt-3 text-xs font-medium text-rose-700">{downloadError}</p>}

      {selectedItemId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4">
          <div className="w-full max-w-4xl rounded-2xl border border-slate-200 bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
              <div>
                <h3 className="text-base font-semibold text-slate-900">Migrated Item JSON</h3>
                <p className="font-mono text-xs text-slate-500">{selectedItemId}</p>
              </div>
              <button
                onClick={() => {
                  setSelectedItemId(null);
                  setItemJson(null);
                  setItemError(null);
                }}
                className="rounded-md border border-slate-300 px-2 py-1 text-xs font-medium text-slate-700 hover:bg-slate-50"
              >
                Close
              </button>
            </div>
            <div className="max-h-[70vh] overflow-auto bg-slate-950 p-4">
              {loadingItem && <p className="text-sm text-slate-200">Loading JSON...</p>}
              {!loadingItem && itemError && <p className="text-sm text-rose-300">{itemError}</p>}
              {!loadingItem && !itemError && itemJson != null && (
                <pre
                  className="overflow-x-auto text-xs leading-6 text-slate-100"
                  dangerouslySetInnerHTML={{ __html: highlightedJson }}
                />
              )}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
