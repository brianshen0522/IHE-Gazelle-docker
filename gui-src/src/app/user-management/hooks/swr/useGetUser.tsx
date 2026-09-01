import { useSession } from "next-auth/react";
import useSWR from "swr";
import { fetcher } from "@user-management/services/fetcher";

export function useGetUserById(userId: string) {
  const { data: session } = useSession();
  const token = session?.access_token;
  const isFetchReady = Boolean(token && userId);
  const key: [string, string] | null = isFetchReady ? [`/gazelle/api/users/${userId}`, token!] : null;
  const { data, error, isLoading, mutate } = useSWR(key, ([url, token]) => fetcher(url, token));

  return { data, isError: error, isLoading, mutate, key };
}

export function useGetUserPreferencesById(userId: string) {
  const { data: session } = useSession();
  const token = session?.access_token;
  const isFetchReady = Boolean(token && userId);
  const { data, error, isLoading, mutate } = useSWR(isFetchReady ? [`/gazelle/api/users/${userId}/preferences`, token!] : null, ([url, token]) =>
    fetcher(url, token),
  );

  return { data, isError: error, isLoading, mutate };
}

export function useGetUserPicture(userId: string, format: string) {
  const { data: session } = useSession();
  const token = session?.access_token;
  const isFetchReady = Boolean(token && userId);
  const key: [string, string] | null = isFetchReady ? [`/gazelle/api/users/${userId}/picture?format=${format}`, token!] : null;
  const { data, error, isLoading, mutate } = useSWR(key, key ? ([url, token]) => fetcher(url, token) : null);

  return { data, isError: error, isLoading, mutate, key };
}
