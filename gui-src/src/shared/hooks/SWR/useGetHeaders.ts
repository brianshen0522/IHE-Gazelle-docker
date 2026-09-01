import useSWR from "swr";

interface UseGetHeadersOptions {
  searchParameters: Record<string, string>;
  type: string;
  path: string;
  headersToExtract?: string[];
}

/**
 * Generic hook to fetch HTTP headers using HEAD request
 * More efficient than GET when only metadata is needed
 */
export function useGetHeaders({ searchParameters, type, path, headersToExtract }: UseGetHeadersOptions) {
  const queryParams = new URLSearchParams();
  Object.entries(searchParameters).forEach(([key, value]) => {
    if (value) queryParams.append(key, value);
  });

  const queryString = queryParams.toString();
  const separator = queryString ? queryString + "&" : "";

  const hasParams = Object.values(searchParameters).some(Boolean);
  const url = hasParams ? `/gazelle/api/headers?${separator}type=${encodeURIComponent(type)}&path=${encodeURIComponent(path)}` : null;

  const { data, error, isLoading } = useSWR(
    url,
    async (url: string) => {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Failed to fetch headers: ${response.status}`);
      }
      const result = await response.json();
      return result.headers as Record<string, string | null>;
    },
    {
      revalidateOnFocus: false,
      keepPreviousData: true,
    },
  );

  // If specific headers requested, extract only those
  const headers = headersToExtract ? Object.fromEntries(headersToExtract.map((header) => [header, data?.[header] ?? null])) : data;

  return {
    headers: headers ?? null,
    isLoading,
    error,
  };
}
