import useSWR, { type KeyedMutator } from "swr";
import type { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";

export interface UseValidationReportItemOptions {
  itemId?: string;
  readAccessKey?: string;
  parseContent?: boolean;
  enabled?: boolean;
}

export interface UseValidationReportItemResult {
  item?: DatahouseItem;
  isLoading: boolean;
  error?: Error;
  isError: boolean;
  mutate: KeyedMutator<DatahouseItem | null>;
}

export function useValidationReportItem(options: UseValidationReportItemOptions): UseValidationReportItemResult {
  const { itemId, readAccessKey, parseContent = true, enabled = true } = options;

  const swrKey = enabled && itemId ? ["validation-report-item", itemId, readAccessKey, parseContent] : null;

  const fetcher = async (): Promise<DatahouseItem | null> => {
    if (!itemId) return null;

    const params = new URLSearchParams();
    if (readAccessKey) params.append("readAccessKey", readAccessKey);
    if (parseContent) params.append("parseContent", "true");
    const paramsString = params.toString();
    const query = paramsString ? `?${paramsString}` : "";

    const url = `/gazelle/validation-portal/api/reports/${itemId}${query}`;

    const res = await fetch(url);

    if (res.status === 404) {
      return null;
    }

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      throw new Error(errorData.error || res.statusText || "Failed to fetch validation report");
    }

    const data = await res.json();
    return data as DatahouseItem;
  };

  const { data, error, isLoading, mutate } = useSWR<DatahouseItem | null>(swrKey, fetcher, {
    revalidateOnFocus: false,
    shouldRetryOnError: false,
  });

  return {
    item: data ?? undefined,
    isLoading,
    error,
    isError: !!error,
    mutate,
  };
}
