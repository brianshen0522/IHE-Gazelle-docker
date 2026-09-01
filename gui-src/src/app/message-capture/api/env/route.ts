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

import { NextResponse } from "next/server";

interface EnvProps {
  GZL_DTH_VALIDATION: string;
  GZL_REGISTRATION_URL: string;
  BASE_URL: string;
}

export const dynamic = "force-dynamic";

export async function GET() {
  const env: EnvProps = {
    GZL_DTH_VALIDATION: process.env.GZL_DTH_VALIDATION_ENABLED ?? "true", // true is the default value
    GZL_REGISTRATION_URL: process.env.GZL_REGISTRATION_URL ?? "",
    BASE_URL: process.env.BASE_URL ?? "",
  };
  return NextResponse.json(env);
}
