import { getPossibleValues, PossibleValuesResult } from "@shared/actions/getPossibleValues";
import useSWR from "swr";
import { useSession } from "next-auth/react";

interface PossibleValuesProps {
  field: string;
  searchParameters: Record<string, string>;
}

interface PossibleValuesResponse {
  possibleValues: string[];
  possibleValuesLoading: boolean;
  possibleValuesError?: string;
}

export function useGetPossibleValues({ field, searchParameters }: PossibleValuesProps): PossibleValuesResponse {
  const key = field ? [`possibleValues`, field, searchParameters] : null;
  const { data: session } = useSession();
  const token = session?.access_token;

  const { data, isLoading } = useSWR<PossibleValuesResult>(key, () => getPossibleValues({ field, searchParameters, token }));
  return {
    possibleValues: data?.possibleValues ?? [],
    possibleValuesLoading: isLoading,
    possibleValuesError: data?.error,
  };
}
