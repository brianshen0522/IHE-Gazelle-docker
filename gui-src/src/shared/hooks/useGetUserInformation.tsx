import type { Session } from "next-auth";
import useSWR from "swr";
import { fetcher } from "@shared/services/fetcher";

/**
 * Retrieve user from the id of the user
 * @param session the current session
 * @param userId the id of the user
 */
export function useGetUserById(session: Session, userId: string) {
  const token = session?.access_token;
  const isFetchReady = Boolean(token && userId);
  const key = isFetchReady ? [`/gazelle/api/users/${userId}`, token] : null;
  const { data, error, isLoading, mutate } = useSWR(key, fetcher);

  return { data, isError: error, isLoading, mutate, key };
}

/**
 * Retrieve the user picture from the id of the user
 * @param session the current session
 * @param userId the id of the user
 * @param format the format of the desired picture (thumbnail | normal)
 */
export function useGetUserPicture(session: Session, userId: string, format: string) {
  const token = session?.access_token;
  const isFetchReady = Boolean(token && userId);
  const key = isFetchReady ? [`/gazelle/api/users/${userId}/picture?format=${format}`, token] : null;
  const { data, error, isLoading, mutate } = useSWR(key, fetcher);

  return { data, isError: error, isLoading, mutate, key };
}

/**
 * Retrieve the organization from the id of the organization
 * @param orgaId the id of the organization
 */
export function useGetOrganizationFromId(orgaId: string) {
  const key = [`/gazelle/api/organizations/${orgaId}`];
  const { data, error, isLoading, mutate } = useSWR(key, fetcher, { shouldRetryOnError: false });
  return { data, isError: error, isLoading, mutateOrga: mutate, key };
}
