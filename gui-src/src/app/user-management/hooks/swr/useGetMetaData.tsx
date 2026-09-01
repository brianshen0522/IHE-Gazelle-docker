import useSWR from 'swr';
import {fetcher} from "@user-management/services/fetcher";

export function useGetMetaData() {
  const { data, isLoading, error } = useSWR('/gazelle/user-management/api/metadata', fetcher);

  return { data, isLoading, isError: error };
}
