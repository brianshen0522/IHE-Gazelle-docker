import axios from "axios";
import { NextRequest, NextResponse } from "next/server";
import { getAuthorizationHeader } from "@shared/services/getServerAuthHeader";
import { parseContent } from "@/app/message-capture/utils/parseContent";
import type { ValidationReportDTO, ValidationInput } from "@/shared/types/validation/types";
import { getAttachmentIdFromItem } from "@/app/validation-portal/utils/getAttachmentIdFromItem";

async function fetchAttachmentFileName(
  itemId: string,
  attachmentId: string,
  encodedReadAccessKey?: string | null,
  headerConfig?: { headers: { Authorization: string } },
): Promise<[string, string] | null> {
  const readAccessKeyQueryParam = encodedReadAccessKey ? `?readAccessKey=${encodedReadAccessKey}` : "";
  const metadataUrl = new URL(`${process.env.GZL_DTH_API_URL}/items/${itemId}/attachments/${attachmentId}/metadata${readAccessKeyQueryParam}`);
  const metadataResponse = await axios.get(metadataUrl.toString(), headerConfig);
  const fileName = metadataResponse.data?.filename as string | undefined;
  return fileName ? [attachmentId, fileName] : null;
}

function buildInputFileNamesMap(results: PromiseSettledResult<[string, string] | null>[]): Record<string, string> {
  const inputFileNames: Record<string, string> = {};
  for (const result of results) {
    if (result.status !== "fulfilled" || !result.value) continue;
    const [attachmentId, fileName] = result.value;
    inputFileNames[attachmentId] = fileName;
  }
  return inputFileNames;
}

function extractAttachmentIds(report: ValidationReportDTO): string[] {
  const inputs = report.inputs ?? report.validationItems ?? [];
  return inputs.map((item: ValidationInput) => getAttachmentIdFromItem(item)).filter((value): value is string => !!value);
}

async function addInputFileNames(data: unknown, itemId: string, encodedReadAccessKey?: string | null, auth?: string | null): Promise<void> {
  const report = (data as { content: ValidationReportDTO }).content;
  const attachmentIds = extractAttachmentIds(report);

  if (attachmentIds.length === 0) return;

  const headerConfig = auth ? { headers: { Authorization: auth } } : undefined;
  const results = await Promise.allSettled(
    attachmentIds.map((attachmentId) => fetchAttachmentFileName(itemId, attachmentId, encodedReadAccessKey, headerConfig)),
  );

  const inputFileNames = buildInputFileNamesMap(results);
  if (Object.keys(inputFileNames).length > 0) {
    report.inputFileNames = inputFileNames;
  }
}

function shouldAttachInputFileNames(data: unknown, parseContentParam: boolean): boolean {
  return (
    parseContentParam &&
    ["VALIDATION_REPORT", "VALIDATION"].includes((data as { type?: string; content?: unknown })?.type ?? "") &&
    !!(data as { content?: unknown })?.content
  );
}

export async function GET(req: NextRequest) {
  try {
    const id = req.nextUrl.pathname.split("/").pop();
    if (!id) {
      return NextResponse.json({ error: "Missing item ID" }, { status: 400 });
    }

    const readAccessKey = req.nextUrl.searchParams.get("readAccessKey");
    const parseContentParam = req.nextUrl.searchParams.get("parseContent") === "true";
    const encodedReadAccessKey = readAccessKey ? encodeURIComponent(readAccessKey) : null;
    const queryString = encodedReadAccessKey ? `?readAccessKey=${encodedReadAccessKey}` : "";

    const url = new URL(`${process.env.GZL_DTH_API_URL}/items/${id}${queryString}`);

    const auth = await getAuthorizationHeader(req);
    const axiosConfig = auth ? { headers: { Authorization: auth } } : undefined;

    const response = await axios.get(url.toString(), axiosConfig);

    if (response.status < 200 || response.status >= 300) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const rawData = response.data;
    const data = parseContentParam ? parseContent([rawData])[0] : rawData;

    if (shouldAttachInputFileNames(data, parseContentParam)) {
      await addInputFileNames(data, id, encodedReadAccessKey, auth);
    }

    return NextResponse.json(data);
  } catch (err: unknown) {
    const e = err as { response?: { status?: number; data?: { error?: string } }; message?: string };
    const status = e.response?.status ?? 500;
    const message = e.response?.data?.error ?? e.message ?? "Unable to get validation report by id";

    return NextResponse.json({ error: message, status }, { status });
  }
}
