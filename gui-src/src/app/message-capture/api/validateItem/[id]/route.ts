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
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const id = req.nextUrl.pathname.split("/").pop();
    const auth = await getAuthorizationHeader(req);
    const response = await axios.post(`${process.env.GZL_DTH_EVSGATEWAY_URL}/items/${id}/validate`, body, {
        headers: {
            "Content-Type": "application/json",
            ...(auth ? { Authorization: auth } : {}),
        },
    });
    const data = await response.data;
    return NextResponse.json(data);
  } catch (err: unknown) {
    console.error({ err });
    const message = axios.isAxiosError(err) ? (err.message || "Unable to get validated item") : "Unable to get validated item";
    const status = axios.isAxiosError(err) ? (err.response?.status || 500) : 500;
    return NextResponse.json({ error: message }, { status });
  }
}
