import useSWR from "swr";
import { useSession } from "next-auth/react";
import { authFetcher, fetcher } from "@shared/services/fetcher";

/**
 * Search for users summary
 * @param offset the offset of the search
 * @param limit the max number of elements to return
 * @param search a string that must be found in the result
 * @param sortBy the attribute to sort the result on
 * @param sortOrder the order of the sort i.e., ascendant or descendant
 * @param enabled whether to enable or disable this query
 * @return the data found, a boolean if the result is loading, an error if a problem occurred
 */

interface UserSummary {
  offset: number;
  limit: number;
  search?: string | null;
  enabled: boolean;
  sortBy?: string;
  sortOrder?: string;
}

export function useGetUserSummary({ offset, limit, search, enabled = true, sortBy = "firstName", sortOrder = "ASC" }: UserSummary) {
  const { data: session } = useSession();
  const accessToken = session?.access_token;

  const fetcherWithAuth = accessToken ? authFetcher(accessToken) : fetcher;

  const url = `/gazelle/api/users/summary?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}&search=${search ?? ""}`;

  const { data, isLoading, error } = useSWR(enabled ? url : null, fetcherWithAuth, {
    keepPreviousData: true,
  });

  return { data, isLoading, error };
}
