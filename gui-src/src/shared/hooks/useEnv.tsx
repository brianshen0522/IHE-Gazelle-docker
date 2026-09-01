import useSWR from "swr";
import {EnvConfig, getEnv} from "@shared/actions/getEnv";

type EnvResponse = {
    env?: EnvConfig;
    envLoading: boolean;
    envError?: string;
}

export function useEnv(): EnvResponse {
    const { data, error, isLoading } = useSWR<EnvConfig>("env", getEnv);

    return {
        env: data,
        envLoading: isLoading,
        envError: error
    };
}