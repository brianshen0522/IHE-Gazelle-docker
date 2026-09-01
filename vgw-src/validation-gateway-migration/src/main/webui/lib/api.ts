import {
  MigrationStatus,
  PreMigrationStats,
  MigrationProgress,
  MigrationSummary,
  VerificationResult,
  FailedCountResponse,
  FailedReportsResponse,
  MigrationStartMode,
} from "@/types/migration";

const API_BASE = "/evs-migration/api/migration";

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url, { cache: "no-store" });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `HTTP ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export async function fetchStatus(): Promise<MigrationStatus> {
  return fetchJson<MigrationStatus>(`${API_BASE}/status`);
}

export async function fetchStats(): Promise<PreMigrationStats> {
  return fetchJson<PreMigrationStats>(`${API_BASE}/stats`);
}

export async function fetchProgress(): Promise<MigrationProgress> {
  return fetchJson<MigrationProgress>(`${API_BASE}/progress`);
}

export async function fetchSummary(): Promise<MigrationSummary> {
  return fetchJson<MigrationSummary>(`${API_BASE}/summary`);
}

export async function fetchFailedCount(): Promise<FailedCountResponse> {
  return fetchJson<FailedCountResponse>(`${API_BASE}/failed-count`);
}

export async function fetchFailedReports(
  offset: number,
  limit: number
): Promise<FailedReportsResponse> {
  return fetchJson<FailedReportsResponse>(
    `${API_BASE}/failed-reports?offset=${offset}&limit=${limit}`
  );
}

export async function startMigration(
  freezeConfirmed: boolean,
  mode: MigrationStartMode,
  ignoreAllMissingInputs: boolean = false,
  specificIgnoredOids: string[] = []
): Promise<void> {
  const params = new URLSearchParams();
  params.set("freezeConfirmed", String(freezeConfirmed));
  params.set("mode", mode);
  if (ignoreAllMissingInputs) {
    params.set("ignoreAllMissingInputs", "true");
  }

  const requestBody = specificIgnoredOids.length > 0
    ? JSON.stringify({ ignoredOids: specificIgnoredOids })
    : undefined;

  const response = await fetch(`${API_BASE}/start?${params.toString()}`, {
    method: "POST",
    headers: requestBody ? { "Content-Type": "application/json" } : {},
    body: requestBody,
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `HTTP ${response.status}`);
  }
}

export async function runVerification(count: number): Promise<VerificationResult[]> {
  return fetchJson<VerificationResult[]>(`${API_BASE}/verification?count=${count}`);
}

export async function fetchMigratedItem(itemId: string): Promise<unknown> {
  return fetchJson<unknown>(`${API_BASE}/items/${encodeURIComponent(itemId)}`);
}

function filenameFromDisposition(contentDisposition: string | null): string {
  if (!contentDisposition) {
    return "input.bin";
  }
  const match = /filename=\"?([^\";]+)\"?/i.exec(contentDisposition);
  return match?.[1] ?? "input.bin";
}

export async function downloadMigratedInput(itemId: string): Promise<void> {
  const response = await fetch(`${API_BASE}/items/${encodeURIComponent(itemId)}/input`, { cache: "no-store" });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `HTTP ${response.status}`);
  }
  const blob = await response.blob();
  const filename = filenameFromDisposition(response.headers.get("content-disposition"));
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}
