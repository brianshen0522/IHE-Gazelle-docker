import useSWR from "swr";
import { fetcher } from "@shared/services/fetcher";

export interface GetIndexesOptions {
  // Type for backend selection (e.g., 'simulation', 'validation')
  // will be mandatory after migration when the generic /api/indexValues?type=X route is used by all components
  type?: string;
  /** @deprecated: For backward compatibility only, will be removed */
  baseUrl?: string;
}

export function useGetIndexes(options: GetIndexesOptions) {
  const { type, baseUrl } = options;

  // Build the URL: use generic /api/indexes with type param, or fallback to baseUrl for backward compat
  let swrKey: string | null = null;

  if (baseUrl) {
    /** Backward compatibility: handle old baseUrl approach @deprecated: */
    if (baseUrl.includes("?")) {
      swrKey = baseUrl.replace("?", "/indexes?");
    } else {
      swrKey = `${baseUrl}/indexes`;
    }
  } else if (type) {
    // New approach: always use /gazelle/api/indexes with type param
    swrKey = `/gazelle/api/indexes?type=${encodeURIComponent(type)}`;
  }

  const { data, error, isLoading } = useSWR(swrKey, swrKey ? fetcher : null);

  return {
    indexes: data ?? [],
    isLoadingIndexes: isLoading,
    isError: error ?? (swrKey === null ? new Error("Missing type or baseUrl parameter") : null),
  };
}
