import { getSearch, SearchProps, SearchResultProps } from "@shared/actions/getSearch";
import useSWR from "swr";
import { useSession } from "next-auth/react";

interface SearchResponse<T> {
  searchResult: T[];
  contentRange?: string | null;
  searchLoading: boolean,
  isValidating: boolean,
  searchError?: string;
}

export function useSearch<T>({searchParameters, field, sortOrder, offset, limit}: SearchProps): SearchResponse<T> {
  const { data: session } = useSession();
  const token = session?.access_token;
  const key = [
    "search",
    searchParameters,
    field,
    sortOrder,
    offset,
    limit
  ];

  const {data, isLoading, isValidating} = useSWR<SearchResultProps<T>>(
    key,
    () => getSearch<T>({ searchParameters, field, sortOrder, offset, limit, token })
  );

  return {
    searchResult: data?.searchResult ?? [],
    contentRange: data?.contentRange,
    searchLoading: isLoading,
    isValidating: isValidating,
    searchError: data?.error
  };
}