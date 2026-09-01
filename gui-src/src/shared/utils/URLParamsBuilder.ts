import { GetDataOptions } from "@shared/hooks/SWR/useGetData";

// Reserved parameter keys that are always injected explicitly by this builder
const RESERVED_KEYS = new Set(["offset", "limit", "sortBy", "sortOrder"]);

export function buildUrl<T>({
  searchParameters,
  field,
  sortOrder,
  offset,
  limit,
  paramPrefix = "",
  paramMap = {},
}: Omit<GetDataOptions<T>, "baseUrl" | "serverAction" | "apiFolder"> & { paramPrefix?: string; paramMap?: Partial<Record<"offset"|"limit"|"sortBy"|"sortOrder", string>> }) {
  // Create a shallow copy excluding any reserved keys to avoid duplication (e.g., offset=0&offset=25)
  const sanitizedSearchParams: Record<string, string> = {};
  Object.entries(searchParameters || {}).forEach(([k, v]) => {
    if (!RESERVED_KEYS.has(k) && v !== undefined && v !== null && v !== "") {
      sanitizedSearchParams[k] = v;
    }
  });

  const query = new URLSearchParams(sanitizedSearchParams).toString();

  // Allow paramMap to override, otherwise use paramPrefix if provided
  const offsetKey = paramMap.offset || (paramPrefix ? `${paramPrefix}offset` : "offset");
  const limitKey = paramMap.limit || (paramPrefix ? `${paramPrefix}limit` : "limit");
  const sortByKey = paramMap.sortBy || (paramPrefix ? `${paramPrefix}sortBy` : "sortBy");
  const sortOrderKey = paramMap.sortOrder || (paramPrefix ? `${paramPrefix}sortOrder` : "sortOrder");

  let url = `?${query}${query ? "&" : ""}${offsetKey}=${offset}&${limitKey}=${limit}`;

  if (field && sortOrder !== null) {
    // Special case: if sortByKey is '_sort', combine field and sortOrder as _sort=-field for desc, _sort=field for asc
    if (sortByKey === "_sort") {
      const sortValue = sortOrder === "desc" ? `-${field}` : field;
      url += `&_sort=${sortValue}`;
    } else {
      url += `&${sortByKey}=${field}&${sortOrderKey}=${sortOrder?.toUpperCase()}`;
    }
  }
  return url;
}
