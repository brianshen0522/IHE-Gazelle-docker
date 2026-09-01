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
    const id = req.nextUrl.pathname.split("/").pop();
    const attachmentId = req.nextUrl.searchParams.get("attachmentId");
    const readAccessKey = req.nextUrl.searchParams.get("readAccessKey");
    const readAccessKeyQueryParam = readAccessKey ? `?readAccessKey=${encodeURIComponent(readAccessKey)}` : "";
    const url = new URL(`${process.env.GZL_DTH_API_URL}/items/${id}/attachments/${attachmentId}${readAccessKeyQueryParam}`);
    const session = await getServerSession(authOptions);
    const token = session?.access_token;

    const config: AxiosRequestConfig | undefined = {}
    if (token) {
      config.headers = {
        Authorization: `Bearer ${token}`,
      };
    }

    const response = await axios.get(url.toString(), config);
    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = Buffer.from(response.data, "binary");
    return new NextResponse(data, {
      headers: {
        "Content-Disposition": `attachment; filename="${attachmentId}"`,
        "Content-Type": response.headers["content-type"]?.toString() || "",
        "Content-Length": data.length.toString(),
      },
    });
  } catch (err: any) {
    return NextResponse.json({ error: err?.message || "Unable to get attachment in base64", status: err.response?.status || 500 });
  }
}
