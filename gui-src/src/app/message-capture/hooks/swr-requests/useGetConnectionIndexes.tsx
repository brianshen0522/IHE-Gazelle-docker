import useSWR from "swr";
import { authFetcher, fetcher } from "@shared/services/fetcher";
import { Session } from "next-auth";
import useEnvironmentVar from "@message-capture/hooks/useEnvironmentVar";

export function useGetConnectionIndexes(id: string, session: Session | null | undefined) {
  const { envBaseUrl } = useEnvironmentVar();

  const itemsWithId = "itemsWithId";
  const presentation = envBaseUrl ? `${envBaseUrl}/message-capture/api/presentationSchemas/${itemsWithId}` : undefined;
  const query = new URLSearchParams({ reference: id, ...(presentation ? { _presentation: presentation } : {}) }).toString();
  const accessToken = session?.access_token;
  const fetcherWithAuth = accessToken ? authFetcher(accessToken) : fetcher;

  const queryParams = query ? `${query}` : "";
  const { data, error, isLoading } = useSWR(() => (id ? `/gazelle/message-capture/api/connectionIndex?${queryParams}` : null), fetcherWithAuth);

  return { data, isError: error, isLoading };
}
