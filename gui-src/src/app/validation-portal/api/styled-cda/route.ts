import { NextRequest, NextResponse } from "next/server";
import crypto from "node:crypto";
import fs from "node:fs/promises";
import path from "node:path";
import { XmlParser, Xslt } from "xslt-processor";
import { getDatahouseAttachment } from "@/shared/actions/getDatahouseAttachment";
import { getDatahouseItem } from "@/shared/actions/getDatahouseItem";
import type { ValidationInput, ValidationReportDTO } from "@/shared/types/validation/types";
import { base64ToUtf8 } from "@/app/message-capture/utils/base64ToUtf8";
import { STYLED_CDA_STYLESHEET, type StyledCdaStylesheetConfig } from "@/app/validation-portal/config/styledCdaStylesheets";

const CACHE_ROOT_DIR = "/opt/gazelle-user-interface/styled-cda-cache";
const CACHE_HTML_EXTENSION = ".html";
const CACHE_LOCK_EXTENSION = ".lock";
const CACHE_CLEANUP_MARKER = path.join(CACHE_ROOT_DIR, ".cleanup-marker");
const CACHE_TTL_MS = 24 * 60 * 60 * 1000;
const CACHE_MAX_SIZE_BYTES = 512 * 1024 * 1024;
const CLEANUP_INTERVAL_MS = 15 * 60 * 1000;
const LOCK_STALE_MS = 60 * 1000;
const LOCK_WAIT_TIMEOUT_MS = 20 * 1000;
const LOCK_WAIT_INTERVAL_MS = 250;

type CacheEntry = {
  htmlPath: string;
  lockPath: string;
};

type CacheFileStats = {
  path: string;
  size: number;
  mtimeMs: number;
};

function hashValue(value: string): string {
  return crypto.createHash("sha256").update(value).digest("hex");
}

function escapeHtml(value: string): string {
  return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}

function buildCacheEntry(stylesheetHash: string, xmlContent: string): CacheEntry {
  const xmlHash = hashValue(xmlContent);
  const cacheKey = hashValue(`${STYLED_CDA_STYLESHEET.id}:${stylesheetHash}:${xmlHash}`);

  return {
    htmlPath: path.join(CACHE_ROOT_DIR, `${cacheKey}${CACHE_HTML_EXTENSION}`),
    lockPath: path.join(CACHE_ROOT_DIR, `${cacheKey}${CACHE_LOCK_EXTENSION}`),
  };
}

async function ensureCacheDirectory(): Promise<void> {
  await fs.mkdir(CACHE_ROOT_DIR, { recursive: true });
}

async function fileExists(filePath: string): Promise<boolean> {
  try {
    await fs.access(filePath);
    return true;
  } catch {
    return false;
  }
}

async function touchFile(filePath: string): Promise<void> {
  const now = new Date();
  await fs.utimes(filePath, now, now);
}

async function getCachedHtml(cacheEntry: CacheEntry): Promise<string | null> {
  if (!(await fileExists(cacheEntry.htmlPath))) {
    return null;
  }

  const html = await fs.readFile(cacheEntry.htmlPath, "utf-8");
  await touchFile(cacheEntry.htmlPath);
  return html;
}

async function sleep(ms: number): Promise<void> {
  await new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

async function removeFileIfPresent(filePath: string): Promise<void> {
  try {
    await fs.unlink(filePath);
  } catch (error: unknown) {
    if (!isErrnoException(error) || error.code !== "ENOENT") {
      throw error;
    }
  }
}

function isErrnoException(error: unknown): error is NodeJS.ErrnoException {
  return error instanceof Error;
}

async function isLockStale(lockPath: string): Promise<boolean> {
  try {
    const stats = await fs.stat(lockPath);
    return Date.now() - stats.mtimeMs > LOCK_STALE_MS;
  } catch {
    return false;
  }
}

async function acquireLock(lockPath: string): Promise<boolean> {
  try {
    const handle = await fs.open(lockPath, "wx");
    await handle.close();
    return true;
  } catch (error: unknown) {
    if (!isErrnoException(error) || error.code !== "EEXIST") {
      throw error;
    }

    if (await isLockStale(lockPath)) {
      await removeFileIfPresent(lockPath);
      const retriedHandle = await fs.open(lockPath, "wx");
      await retriedHandle.close();
      return true;
    }

    return false;
  }
}

async function waitForCachedHtml(cacheEntry: CacheEntry): Promise<string | null> {
  const deadline = Date.now() + LOCK_WAIT_TIMEOUT_MS;

  while (Date.now() < deadline) {
    const cachedHtml = await getCachedHtml(cacheEntry);
    if (cachedHtml) {
      return cachedHtml;
    }

    if (!(await fileExists(cacheEntry.lockPath))) {
      return null;
    }

    await sleep(LOCK_WAIT_INTERVAL_MS);
  }

  return null;
}

async function writeCacheAtomically(cacheEntry: CacheEntry, html: string): Promise<void> {
  const tempPath = `${cacheEntry.htmlPath}.${process.pid}.${Date.now()}.tmp`;
  await fs.writeFile(tempPath, html, "utf-8");
  await fs.rename(tempPath, cacheEntry.htmlPath);
}

async function listCacheFiles(): Promise<CacheFileStats[]> {
  const entries = await fs.readdir(CACHE_ROOT_DIR, { withFileTypes: true });
  const cacheFiles = entries.filter((entry) => entry.isFile() && entry.name.endsWith(CACHE_HTML_EXTENSION));

  return Promise.all(
    cacheFiles.map(async (entry) => {
      const filePath = path.join(CACHE_ROOT_DIR, entry.name);
      const stats = await fs.stat(filePath);
      return { path: filePath, size: stats.size, mtimeMs: stats.mtimeMs };
    }),
  );
}

async function cleanupCacheDirectory(): Promise<void> {
  await ensureCacheDirectory();

  const now = Date.now();
  let cacheFiles = await listCacheFiles();
  const expiredFiles = cacheFiles.filter((file) => now - file.mtimeMs > CACHE_TTL_MS);

  for (const file of expiredFiles) {
    await removeFileIfPresent(file.path);
  }

  cacheFiles = cacheFiles.filter((file) => now - file.mtimeMs <= CACHE_TTL_MS);
  let totalSize = cacheFiles.reduce((sum, file) => sum + file.size, 0);
  if (totalSize <= CACHE_MAX_SIZE_BYTES) {
    return;
  }

  const oldestFirst = [...cacheFiles].sort((left, right) => left.mtimeMs - right.mtimeMs);
  for (const file of oldestFirst) {
    await removeFileIfPresent(file.path);
    totalSize -= file.size;
    if (totalSize <= CACHE_MAX_SIZE_BYTES) {
      break;
    }
  }
}

async function maybeCleanupCacheDirectory(): Promise<void> {
  await ensureCacheDirectory();

  let shouldCleanup = false;
  try {
    const markerStats = await fs.stat(CACHE_CLEANUP_MARKER);
    shouldCleanup = Date.now() - markerStats.mtimeMs > CLEANUP_INTERVAL_MS;
  } catch {
    shouldCleanup = true;
  }

  if (!shouldCleanup) {
    return;
  }

  await cleanupCacheDirectory();
  await fs.writeFile(CACHE_CLEANUP_MARKER, `${Date.now()}\n`, "utf-8");
}

async function loadStylesheetContent(stylesheet: StyledCdaStylesheetConfig): Promise<string> {
  if (stylesheet.sourceType === "url") {
    const response = await fetch(stylesheet.location);
    if (!response.ok) {
      throw new Error(`Unable to fetch stylesheet from ${stylesheet.location}: HTTP ${response.status}`);
    }
    return response.text();
  }

  const stylesheetPath = path.isAbsolute(stylesheet.location) ? stylesheet.location : path.resolve(process.cwd(), stylesheet.location);
  return fs.readFile(stylesheetPath, "utf-8");
}

async function fetchStylesheetDependency(baseLocation: string, dependencyUri: string): Promise<string> {
  const isRemoteBase = /^https?:\/\//i.test(baseLocation);

  if (isRemoteBase) {
    const dependencyUrl = new URL(dependencyUri, baseLocation).toString();
    const response = await fetch(dependencyUrl);
    if (!response.ok) {
      throw new Error(`Unable to fetch stylesheet dependency ${dependencyUrl}: HTTP ${response.status}`);
    }
    return response.text();
  }

  const baseDirectory = path.dirname(baseLocation);
  const dependencyPath = path.isAbsolute(dependencyUri) ? dependencyUri : path.resolve(baseDirectory, dependencyUri);
  return fs.readFile(dependencyPath, "utf-8");
}

async function getInlineInputContent(reportItemId: string, inputId: string, readAccessKey?: string): Promise<string> {
  const result = await getDatahouseItem({
    itemId: reportItemId,
    readAccessKey,
    parseContent: true,
  });

  if (!result?.datahouseItem) {
    throw new Error(result?.error ?? `Unable to load report item ${reportItemId}`);
  }

  const report = result.datahouseItem.content as ValidationReportDTO;
  const inputs = report.inputs ?? report.validationItems ?? [];
  const input = inputs.find((candidate: ValidationInput) => candidate.id === inputId || candidate.itemId === inputId);

  if (!input?.content) {
    throw new Error(`Unable to find inline CDA content for input ${inputId}`);
  }

  return input.content;
}

async function getAttachmentContent(reportItemId: string, attachmentId: string, readAccessKey?: string): Promise<string> {
  const result = await getDatahouseAttachment({
    itemId: reportItemId,
    attachmentId,
    readAccessKey,
  });

  if (!result?.data) {
    throw new Error(result?.error ?? `Unable to load attachment ${attachmentId}`);
  }

  return result.data;
}

async function loadXmlContentBase64(reportItemId: string, inputId?: string, attachmentId?: string, readAccessKey?: string): Promise<string> {
  if (attachmentId) {
    return getAttachmentContent(reportItemId, attachmentId, readAccessKey);
  }

  if (inputId) {
    return getInlineInputContent(reportItemId, inputId, readAccessKey);
  }

  throw new Error("Either attachmentId or inputId is required");
}

async function resolveStyledCdaCacheEntry(xmlContent: string): Promise<{ cacheEntry: CacheEntry; stylesheetContent: string }> {
  const stylesheetContent = await loadStylesheetContent(STYLED_CDA_STYLESHEET);
  const stylesheetHash = hashValue(stylesheetContent);
  return {
    cacheEntry: buildCacheEntry(stylesheetHash, xmlContent),
    stylesheetContent,
  };
}

async function hasCachedStyledCda(cacheEntry: CacheEntry): Promise<boolean> {
  return fileExists(cacheEntry.htmlPath);
}

async function renderStyledCdaHtml(xmlContent: string): Promise<string> {
  const { cacheEntry, stylesheetContent } = await resolveStyledCdaCacheEntry(xmlContent);

  await ensureCacheDirectory();
  await maybeCleanupCacheDirectory();

  const cachedHtml = await getCachedHtml(cacheEntry);
  if (cachedHtml) {
    return cachedHtml;
  }

  let lockAcquired = await acquireLock(cacheEntry.lockPath);
  if (!lockAcquired) {
    const waitedHtml = await waitForCachedHtml(cacheEntry);
    if (waitedHtml) {
      return waitedHtml;
    }

    lockAcquired = await acquireLock(cacheEntry.lockPath);
    if (!lockAcquired) {
      throw new Error("Styled CDA cache lock timeout");
    }
  }

  try {
    const doubleCheckedHtml = await getCachedHtml(cacheEntry);
    if (doubleCheckedHtml) {
      return doubleCheckedHtml;
    }

    const parser = new XmlParser();
    const xslt = new Xslt({
      escape: false,
      outputMethod: "html",
      fetchFunction: async (dependencyUri: string) => fetchStylesheetDependency(STYLED_CDA_STYLESHEET.location, dependencyUri),
    });

    const html = await xslt.xsltProcess(parser.xmlParse(xmlContent), parser.xmlParse(stylesheetContent));
    await writeCacheAtomically(cacheEntry, html);
    return html;
  } finally {
    await removeFileIfPresent(cacheEntry.lockPath);
  }
}

function buildErrorHtml(message: string): string {
  return `<!doctype html><html><head><meta charset="utf-8"><title>Styled CDA Error</title></head><body><h1>Styled CDA Error</h1><p>${escapeHtml(message)}</p></body></html>`;
}

async function loadRequestContext(req: NextRequest): Promise<{ reportItemId: string; xmlContent: string }> {
  const reportItemId = req.nextUrl.searchParams.get("reportItemId");
  const inputId = req.nextUrl.searchParams.get("inputId") ?? undefined;
  const attachmentId = req.nextUrl.searchParams.get("attachmentId") ?? undefined;
  const readAccessKey = req.nextUrl.searchParams.get("readAccessKey") ?? undefined;

  if (!reportItemId) {
    throw new Error("Missing reportItemId");
  }

  const xmlContentBase64 = await loadXmlContentBase64(reportItemId, inputId, attachmentId, readAccessKey);
  return {
    reportItemId,
    xmlContent: base64ToUtf8(xmlContentBase64),
  };
}

export async function HEAD(req: NextRequest) {
  try {
    const { xmlContent } = await loadRequestContext(req);
    const { cacheEntry } = await resolveStyledCdaCacheEntry(xmlContent);
    const cached = await hasCachedStyledCda(cacheEntry);

    return new NextResponse(null, {
      status: cached ? 200 : 204,
      headers: {
        "X-Styled-Cda-Cache": cached ? "HIT" : "MISS",
      },
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unable to inspect styled CDA cache";
    return new NextResponse(null, {
      status: 500,
      headers: {
        "X-Styled-Cda-Error": message,
      },
    });
  }
}

export async function GET(req: NextRequest) {
  try {
    const { xmlContent } = await loadRequestContext(req);
    const html = await renderStyledCdaHtml(xmlContent);

    return new NextResponse(html, {
      status: 200,
      headers: { "Content-Type": "text/html; charset=utf-8" },
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unable to render styled CDA document";
    return new NextResponse(buildErrorHtml(message), {
      status: 500,
      headers: { "Content-Type": "text/html; charset=utf-8" },
    });
  }
}
