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
import { authFetcher } from "@shared/services/fetcher";
import { useSession } from "next-auth/react";

export function useGetValidationProfiles() {
  const { data: session } = useSession();
  const accessToken = session?.access_token;
  const { data, error, isLoading } = useSWR(accessToken ? "/gazelle/message-capture/api/validationProfiles" : null, authFetcher(accessToken ?? ""));

  return { data, isError: error, isLoading };
}
