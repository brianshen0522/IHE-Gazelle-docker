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
import {fetcher} from "@shared/services/fetcher";

export function useGetParts(hasHTTPParts: boolean, messageId: string, offset: number, limit: number) {
  const { data, error, isLoading, mutate } = useSWR(hasHTTPParts ? `/gazelle/message-capture/api/parts/${messageId}&_offset=${offset}&_limit=${limit}` : null, fetcher);

  const contentRange = data?.contentRange ? Number(data?.contentRange.split("/")[1]) : 0;

  return { data: data?.data, contentRange, isError: error, isLoading, mutate };
}
