import { getIndexes, Index, IndexResult } from "@shared/actions/getIndexes";
import useSWR from "swr";
import { useSession } from "next-auth/react";

interface IndexResponse {
  indexes: Index[];
  indexLoading: boolean;
  indexError?: string;
}

export function useGetIndexes(): IndexResponse {
  const { data: session } = useSession();
  const token = session?.access_token;
  const { data, isLoading } = useSWR<IndexResult>(
    "indexes",
    () => getIndexes({ token }));

  return {
    indexes: data?.indexes ?? [],
    indexLoading: isLoading,
    indexError: data?.error
  };
}