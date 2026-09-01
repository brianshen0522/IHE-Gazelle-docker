import useSWR from "swr";
import { fetcher } from "@shared/services/fetcher";

export function useGetTestModel(testId: string) {
  const shouldFetch = !!testId;

  const url = shouldFetch ? `/gazelle/test-execution/api/test-model/${testId}` : null;

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
