/*
 * Copyright 2024 Kereval.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import useSWR from "swr";
import {authFetcher, fetcher} from "@shared/services/fetcher";
import {Session} from "next-auth";

/**
 * @deprecated Use useDatahouseItem instead
 */
export function useGetItem(itemId: string, session: Session | null | undefined, readAccessKey: string | null | undefined  ) {
  const readAccessKeyQueryParam = readAccessKey ? "?readAccessKey=" + readAccessKey : '';
  const url = `/gazelle/message-capture/api/items/${itemId}${readAccessKeyQueryParam}`;
  const accessToken = session?.access_token

  const fetcherWithAuth = accessToken ? authFetcher(accessToken) : fetcher;
  const { data, error, isLoading } = useSWR(url, fetcherWithAuth);
  return { data, isError: error, isLoading };
}
