import useSWR, { type KeyedMutator } from "swr";
import type { DatahouseItem } from "@shared/types/datahouse/DatahouseItem";

export interface UseDatahouseItemOptions {
  itemId?: string;
  location?: string; // full URL fallback
  readAccessKey?: string;
  parseContent?: boolean;
  enabled?: boolean;
}

export interface UseDatahouseItemResult {
  item?: DatahouseItem;
  /** @deprecated Use 'item' instead */
  items?: DatahouseItem[];
  isLoading: boolean;
  error?: Error;
  statusCode?: number;
  isError: boolean;
  mutate: KeyedMutator<DatahouseItem | null>;
}

export function useDatahouseItem(options: UseDatahouseItemOptions): UseDatahouseItemResult {
  const { itemId, location, readAccessKey, parseContent = true, enabled = true } = options;

  // Disable fetching if no itemId or location
  const swrKey = enabled && (itemId || location) ? ["datahouse-item", itemId ?? location, readAccessKey, parseContent] : null;

  const fetcher = async (): Promise<DatahouseItem | null> => {
    if (!itemId && !location) return null;

    // Build URL
    const params = new URLSearchParams();
    if (readAccessKey) params.append("readAccessKey", readAccessKey);
    if (parseContent) params.append("parseContent", "true");

    let url: string;
    if (itemId) {
      // Use direct API route for item IDs
      const paramsString = params.toString();
      const query = paramsString ? `?${paramsString}` : "";
      url = `/gazelle/api/items/${itemId}${query}`;
    } else if (location) {
      // Use proxy for external URLs to avoid CORS issues
      params.append("url", location);
      url = `/gazelle/api/datahouse-proxy?${params.toString()}`;
    } else {
      return null;
    }

    // Fetch item
    const res = await fetch(url);

    if (res.status === 404) {
      return null;
    }

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      throw new Error(errorData.error || res.statusText || "Failed to fetch Datahouse item");
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
    items: data ? [data] : undefined, // backward compatibility
    isLoading,
    error,
    statusCode: error?.statusCode,
    isError: !!error,
    mutate,
  };
}
