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

import { authOptions } from "@shared/components/auth/authOptions";
import axios, { AxiosRequestConfig } from "axios";
import { getServerSession } from "next-auth";
import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    const token = session?.access_token;



    const id = req.nextUrl.pathname.split("/").pop();
    const offset = req.nextUrl.searchParams.get("_offset");
    const limit = req.nextUrl.searchParams.get("_limit");

    const url = new URL(`${process.env.GZL_DTH_API_URL}/items?reference=${id}&type=PART&_sort=date&_offset=${offset}&_limit=${limit}`);
    const config : AxiosRequestConfig | undefined = {}
    if (token) {
        config.headers = {
            Authorization: `Bearer ${token}`,
        };
    }
    const response = await axios.get(url.toString(), config);

    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.data;

    return NextResponse.json({
      data,
      contentRange: response.headers["content-range"],
    });
  } catch (err: any) {
    return NextResponse.json({ error: err?.message || "Unable to get parts" }, { status: err.response?.status || 500 });
  }
}
