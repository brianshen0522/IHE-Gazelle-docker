import { Session } from "next-auth";
import useSWR from "swr";
import { fetcher } from "@user-management/services/fetcher";

interface GetOrganizationsParams {
  delegated?: boolean;
  limit?: number;
  search?: string;
  archived?: boolean;
}

export function useGetOrganizations({ delegated, limit, search, archived }: GetOrganizationsParams = {}) {
  const params: string[] = [];
  if (delegated !== undefined) params.push("delegated=" + delegated);
  if (limit !== undefined) params.push("_limit=" + limit);
  if (search) params.push("search=" + encodeURIComponent(search));
  if (archived !== undefined) params.push("archived=" + archived);

  const param = params.length > 0 ? "?" + params.join("&") : "";
  const key = `/gazelle/api/organizations${param}`;

  const { data, error, isLoading, mutate } = useSWR(key, fetcher, { keepPreviousData: true });
  return { data, isError: error, isLoading, mutateOrga: mutate, key };
}

export function useGetRoles(session: Session) {
  const token = session?.access_token;
  const key = token ? [`/gazelle/api/groups?type=role`, token] : null;

  const { data, error, isLoading, mutate } = useSWR(key, key ? ([url, token]) => fetcher(url, token) : null);
  return { data, isError: error, isLoading, mutateOrga: mutate, key };
}
