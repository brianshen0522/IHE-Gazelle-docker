import useSWR from "swr";
import { GetByIdResult, getSequenceById } from "@simulation-portal/action/getSequenceById";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";

interface UseGetSequenceByIdProps {
  id: string | null;
}

interface GetSequenceByIdResponse {
  simulationSequence?: SimulationSequence;
  getByIdLoading: boolean;
  getByIdError?: string;
}

export function useGetSequenceById({ id }: UseGetSequenceByIdProps): GetSequenceByIdResponse {
  const key = id ? [`getById`, id] : null;

  const { data, isLoading } = useSWR<GetByIdResult>(key, () => getSequenceById({ id }));
  return {
    simulationSequence: data?.simulationSequence,
    getByIdLoading: isLoading,
    getByIdError: data?.error,
  };
}
