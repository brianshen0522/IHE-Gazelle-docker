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

export async function GET(req: NextRequest) {
  try {
    const fieldName = req.nextUrl.pathname.split("/").pop();
    const params = Object.fromEntries(
      Object.entries({
        type: req.nextUrl.searchParams.get("type"),
        channel_type: req.nextUrl.searchParams.get("channel_type"),
        message_type: req.nextUrl.searchParams.get("message_type"),
        proxy_port: req.nextUrl.searchParams.get("proxy_port"),
        sender_hostname: req.nextUrl.searchParams.get("sender_hostname"),
        receiver_hostname: req.nextUrl.searchParams.get("receiver_hostname"),
        sender_ip: req.nextUrl.searchParams.get("sender_ip"),
        receiver_ip: req.nextUrl.searchParams.get("receiver_ip"),
      }).filter(([, value]) => value !== null && value !== "")
    );

    const url = new URL(`${process.env.GZL_DTH_API_URL}/indexes/${fieldName}/values?`);
    url.search = new URLSearchParams(params as Record<string, string>).toString();

    const response = await axios.get(url.toString());

    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.data;
    return NextResponse.json(data);
  } catch (err: any) {
    return NextResponse.json({ error: err?.message || "Unable to get possible values" }, { status: 500 });
  }
}
