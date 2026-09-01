import useSWR from "swr";
import { authFetcher, fetcher } from "@shared/services/fetcher";
import { Session } from "next-auth";

export function useGetValidationProfile(profileId: string, serviceName: string, session: Session | null, fields?: string[]) {
  const accessToken = session?.access_token;
  const shouldFetch = !!profileId && !!serviceName;

  const encodedProfileId = encodeURIComponent(profileId);
  const encodedServiceName = encodeURIComponent(serviceName);

  let url = shouldFetch ? `/gazelle/validation-portal/api/profiles?profileId=${encodedProfileId}&serviceName=${encodedServiceName}` : null;

  // Add _fields parameter if provided
  if (url && fields && fields.length > 0) {
    const fieldsParam = encodeURIComponent(fields.join(","));
    url += `&_fields=${fieldsParam}`;
  }

  const fetcherWithAuth = accessToken ? authFetcher(accessToken) : fetcher;

  const { data, error, isLoading } = useSWR(url, fetcherWithAuth, {
    revalidateOnFocus: false,
    shouldRetryOnError: false,
  });

  // Extract the profile from the returned data array
  const profile = data?.data?.[0]?.profile ?? null;

  return {
    data: profile,
    isError: error,
    isLoading,
  };
}
