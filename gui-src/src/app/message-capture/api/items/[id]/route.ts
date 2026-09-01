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

import axios from "axios";
import { NextRequest, NextResponse } from "next/server";
import { parseContent } from "@/app/message-capture/utils/parseContent";
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";

export async function GET(req: NextRequest) {
    try {
        const id = req.nextUrl.pathname.split("/").pop();
        const readAccessKey = req.nextUrl.searchParams.get("readAccessKey");
        const readAccessKeyQueryParams = readAccessKey ? "?readAccessKey=" + readAccessKey : '';

        const url = new URL(`${process.env.GZL_DTH_API_URL}/items/${id}${readAccessKeyQueryParams}`);
        const auth = await getAuthorizationHeader(req);
        const response = await axios.get(
          url.toString(),
          auth ? { headers: { Authorization: auth } } : undefined
        );
        if (response.status < 200 || response.status >= 300) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        let data = await response.data;
        data = Array.isArray(data) ? parseContent(data) : parseContent([data]);
        return NextResponse.json(data);
    } catch (err: any) {
        return NextResponse.json({error: err?.message || "Unable to get item by id", status: err.response?.status || 500});
    }
}
