import useSWR, { mutate } from "swr";
import { authFetcher, fetcher } from "@shared/services/fetcher";
import { useAccessToken } from "@shared/hooks/useAccessToken";
import { buildUrl } from "@shared/utils/URLParamsBuilder";

export interface ApiData<T> {
  data: T[];
  contentRange?: string | null;
}
export interface GetDataOptions<T> {
  searchParameters: Record<string, string>;
  field: string;
  sortOrder: "asc" | "desc" | null;
  offset: number;
  limit: number;
  // Either provide a baseUrl OR a serverAction
  /** @deprecated: For backward compatibility only, will be removed */
  baseUrl?: string;
  /** @deprecated: For backward compatibility only, will be removed */
  serverAction?: () => Promise<ApiData<T>>;
  apiFolder?: string; // Folder name for Nextjs API method
  paramPrefix?: string; // Optional prefix for offset/limit/sort params (e.g. '_')
  paramMap?: Partial<Record<"offset" | "limit" | "sortBy" | "sortOrder", string>>; // Optional custom param names
  fields?: string[]; // Optional comma-separated list of fields to fetch presentation schema
  // Temporary optional: type for generic API route (e.g., 'simulation', 'validation', 'test_execution')
  // Used with /api/items?type=X or /api/indexes?type=X routes
  // Will be mandatory after migration when all components use the generic routes
  type?: string;
  path?: string; // Optional path to append to backend URL (e.g., '/sessions/123/tests')
}

interface SWRResponse<T> {
  data: T[];
  contentRange?: string | null;
  isLoading: boolean;
  isValidating: boolean;
  error: Error | undefined;
  mutateData?: () => Promise<T>;
}

export function useGetData<T>(options: GetDataOptions<T>): SWRResponse<T> {
  const {
    baseUrl,
    serverAction,
    apiFolder = "",
    searchParameters = {},
    field,
    sortOrder,
    offset,
    limit,
    paramPrefix,
    paramMap,
    fields,
    type,
    path,
  } = options;
  const accessToken = useAccessToken();

  // Build SWR key and fetcherFn
  let swrKey: string | [() => Promise<ApiData<T>>] | null = null;
  let fetcherFn: any = null;

  if (serverAction) {
    /** Backward compatibility: handle old baseUrl approach @deprecated: */
    swrKey = [serverAction];
    fetcherFn = serverAction;
  } else if (baseUrl) {
    /** Backward compatibility: handle old baseUrl approach @deprecated: */
    const queryParams = buildUrl({
      searchParameters,
      field,
      sortOrder,
      offset,
      limit,
      paramPrefix,
      paramMap,
    });

    const url = `${baseUrl}/${apiFolder}${queryParams}`;

    swrKey = url;
    fetcherFn = accessToken ? authFetcher(accessToken) : fetcher;
  } else if (type) {
    // Use generic API route with type parameter
    const queryParams = buildUrl({
      searchParameters,
      field,
      sortOrder,
      offset,
      limit,
      paramPrefix,
      paramMap,
    });

    const hasQueryParams = queryParams && queryParams.length > 0;
    const separator = hasQueryParams ? "&" : "?";
    const pathParam = path ? `&path=${encodeURIComponent(path)}` : "";
    const url = `/gazelle/api/items${queryParams}${separator}type=${encodeURIComponent(type)}${pathParam}`;

    swrKey = url;
    fetcherFn = fetcher; // Use fetcher without auth token since Next.js API route handles auth
  }

  // Add _fields parameter if provided (for baseUrl and type paths)
  if (swrKey && typeof swrKey === "string" && fields && fields.length > 0) {
    const fieldsParam = encodeURIComponent(fields.join(","));
    const separator = swrKey.includes("?") ? "&" : "?";
    swrKey = `${swrKey}${separator}_fields=${fieldsParam}`;
  }

  const { data, error, isLoading, isValidating } = useSWR<ApiData<T>>(swrKey, swrKey ? fetcherFn : null, {
    keepPreviousData: true,
    revalidateOnFocus: false,
  });

  // Initialize data
  const filteredData = data?.data ?? [];

  const mutateData = typeof swrKey === "string" ? () => mutate(swrKey) : undefined;

  return {
    data: filteredData,
    contentRange: data?.contentRange,
    isLoading,
    isValidating,
    error: swrKey === null ? new Error("Missing required parameters") : error,
    mutateData,
  };
}
