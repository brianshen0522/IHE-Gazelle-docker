import { Session } from "next-auth";
import useSWR from "swr";
import { fetcher } from "@user-management/services/fetcher";

/**
 * Search for groups with multiple param
 * @param offset the offset to start the search
 * @param limit the max number of groups returned
 * @param search the search term that is included in results
 * @param session the session of the current logged entity
 * @param enabled whether to enable or disable this query
 * @return returns a list of Groups. If the group is of type <strong>org</strong> or <strong>org-adm</strong> then its name
 * will be resolved to the one of the corresponding organization.
 */
interface SearchGroupsResponse {
  offset: number;
  limit: number;
  search: string | null | undefined;
  session: Session | null;
  enabled: boolean;
}

export function useSearchGroupsWithResolvedOrganizationName({ offset, limit, search, session, enabled = true }: SearchGroupsResponse) {
  const token = session?.access_token;
  const key = enabled && token ? [`/gazelle/message-capture/api/groups?offset=${offset}&limit=${limit}&search=${search}`, token] : null;

  const { data, error, isLoading, mutate } = useSWR(key, key ? ([url, token]) => fetcher(url, token) : null);
  return { data, isError: error, isLoading, mutateOrga: mutate, key };
}
