import useSWR from "swr";
import { fetcher } from "@shared/services/fetcher";

export function useGetTestRunExecutions(executionId: string) {
  const shouldFetch = !!executionId;

  const url = shouldFetch ? `/gazelle/test-execution/api/test-run-executions/${executionId}` : null;

  const fetcherWithAuth = fetcher;

  const { data, error, isLoading, mutate } = useSWR(url, fetcherWithAuth);

  return {
    data: data?.data,
    isError: error,
    isLoading,
    mutate,
  };
}
