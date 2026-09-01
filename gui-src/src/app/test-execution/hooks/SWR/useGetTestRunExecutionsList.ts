import useSWR from "swr";
import { fetcher } from "@shared/services/fetcher";

export function useGetTestRunExecutionsList(testId: string, testSessionId: string, owner : string|undefined) {
  const shouldFetch = !!testId && !!testSessionId;

  let url = shouldFetch ? `/gazelle/test-execution/api/test-run-executions?testId=${testId}&testSessionId=${testSessionId}` : null;
  url = url ? `${owner}&owner=${owner}` : null;

  const { data, error, isLoading } = useSWR(url, fetcher, {
    revalidateOnFocus: false,
    shouldRetryOnError: false,
  });

  return {
    data: data?.data,
    isError: error,
    isLoading,
  };
}
