import useSWR from "swr";
import { fetcher } from "@shared/services/fetcher";

interface SWRKeyProps {
  field: string;
  searchParameters: Record<string, string>;
  /** @deprecated: For backward compatibility only, will be removed */
  baseUrl?: string;
  // Type for backend selection (e.g., 'simulation', 'validation')
  // will be mandatory after migration when the generic /api/indexValues?type=X route is used by all components
  type?: string;
  indexPath?: string; // Optional path to append to backend URL (e.g., '/sessions/123/tests')
  searchValue?: string; // Optional LIKE search value to filter index values server-side
}

type useGetIndexValuesProps = {
  field: string;
  searchParameters: Record<string, string>;
  /** @deprecated: For backward compatibility only, will be removed */
  baseUrl?: string;
  // Type for backend selection (e.g., 'simulation', 'validation')
  // will be mandatory after migration when the generic /api/indexValues?type=X route is used by all components
  type?: string;
  indexPath?: string; // Optional path to append to backend URL (e.g., '/sessions/123/tests')
  searchValue?: string; // Optional LIKE search value to filter index values server-side
};

function getSWRKey({ field, searchParameters, baseUrl, type, indexPath, searchValue }: SWRKeyProps): string | null {
  if (field === "secured") return null;
  if (!field) return null; // Don't fetch if no field is selected yet

  let url: string;

  if (baseUrl) {
    /** Backward compatibility: use the provided baseUrl @deprecated: */
    url = baseUrl + "/" + field;
  } else if (type) {
    // New approach: use generic /gazelle/api/indexValues with type param
    const indexPathParam = indexPath ? `&indexPath=${encodeURIComponent(indexPath)}` : "";
    url = `/gazelle/api/indexValues/${field}?type=${encodeURIComponent(type)}${indexPathParam}`;
  } else {
    return null;
  }

  // Append search parameters
  const params = Object.keys(searchParameters).length ? new URLSearchParams(searchParameters).toString() : "";
  if (params) {
    url += (url.includes("?") ? "&" : "?") + params;
  }

  // Append LIKE search value if provided (>= 3 chars enforced by caller)
  if (searchValue && searchValue.length >= 3) {
    url += (url.includes("?") ? "&" : "?") + `_like=${encodeURIComponent(searchValue)}`;
  }

  return url;
}

export function useGetIndexValues({ field, searchParameters, baseUrl, type, indexPath, searchValue }: useGetIndexValuesProps) {
  const isSecuredField = field === "secured";
  const swrKey = getSWRKey({ field, searchParameters, baseUrl, type, indexPath, searchValue });

  const { data, error, isLoading } = useSWR(swrKey, swrKey ? fetcher : null);

  return {
    indexValues: isSecuredField ? ["true", "false"] : data,
    isLoadingIndexValues: isSecuredField ? false : isLoading,
    isError: isSecuredField ? null : error,
  };
}
