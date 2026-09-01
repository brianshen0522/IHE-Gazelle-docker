"use client";
import { useEffect, useMemo, useState } from "react";
import { LineProperties } from "@shared/components/renderers/Renderers";
import { JsonRenderer } from "@shared/components/renderers/json/JsonRenderer";
import { XmlRenderer } from "@shared/components/renderers/xml/XmlRenderer";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { useReportAssertions } from "@validation-portal/context/selectedAssertionContext";
import { useTranslation } from "react-i18next";
import { useDatahouseAttachment } from "@hooks/useDatahouseAttachment";
import { getLineNumberFromSubjectLocation } from "./utils/getLineNumberFromSubjectLocation";
import { getColorToHighlight } from "./utils/getColorToHighlight";
import { ExternalLink, Loader2, CheckCircle, FileText } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Skeleton } from "@gazelle/gazelle-component-ui";
import { inspectForRendering } from "@shared/utils/fileInspection/detectContent";
import { Route } from "next";
import { prepareContentForLineCalculation } from "./utils/prepareContentForLineCalculation";
import { base64ToUtf8 } from "@message-capture/utils/base64ToUtf8";
import { isCdaDocument } from "./utils/cdaDocumentUtils";

type FileRendererProps = {
  fileName: string;
  content?: string;
  itemId: string;
  attachmentId?: string;
  inputId?: string;
  readAccessKey?: string;
};

type CdaStatus = "idle" | "checking" | "generating" | "ready" | "error";

function FileRenderer({ fileName, content, itemId, attachmentId, inputId, readAccessKey }: Readonly<FileRendererProps>) {
  const { selectedAssertion, setSelectedAssertion, assertionsWithLocation } = useReportAssertions();
  const { t } = useTranslation();
  const pathname = usePathname();
  const isContentPage = pathname === "/validation-portal/content";
  const [cdaStatus, setCdaStatus] = useState<CdaStatus>("idle");

  // Fetch attachment content if not provided inline
  const { data: downloadedAttachmentContentBase64, isLoading } = useDatahouseAttachment({
    itemId,
    attachmentId,
    readAccessKey,
    enabled: !content && !!itemId && !!attachmentId,
  });

  // Determine which content to use: inline or downloaded
  const fileContentBase64 = content ?? downloadedAttachmentContentBase64;

  const fileType = fileContentBase64 ? inspectForRendering(fileContentBase64) : { renderer: "RAW" as const, base64Data: "" };
  const xmlContent = useMemo(() => {
    if (!fileContentBase64 || fileType.renderer !== "XML") {
      return undefined;
    }

    try {
      return base64ToUtf8(fileContentBase64);
    } catch {
      return undefined;
    }
  }, [fileContentBase64, fileType.renderer]);
  const canStyleCda = !!xmlContent && isCdaDocument(xmlContent);

  // Prepare content for line calculation - applies same transformations as renderer
  const contentForLineCalculation =
    fileContentBase64 ?
      (prepareContentForLineCalculation(
        fileContentBase64,
        fileType.renderer,
        fileType.dataType,
        "application/xml", // contentType for HTTP XML
      ) ?? fileContentBase64)
    : "";

  // Calculate the selected line number for THIS file
  const selectedLineNumber = selectedAssertion
    ? getLineNumberFromSubjectLocation(fileName, selectedAssertion.subjectLocations, contentForLineCalculation)
    : 0;

  function buildLinesProperties(): LineProperties[] {
    const result: LineProperties[] = [];

    for (const element of assertionsWithLocation) {
      const assertion = element;
      const lineNumber = getLineNumberFromSubjectLocation(fileName, assertion?.subjectLocations, contentForLineCalculation);
      if (lineNumber > 0) {
        result.push({
          lineNumber: lineNumber,
          severity: assertion.severity,
          color: getColorToHighlight(assertion.result!, assertion.severity),
          onClickHandler: () => setSelectedAssertion(assertion),
          selected: lineNumber === selectedLineNumber && selectedLineNumber > 0,
        });
      }
    }
    return result;
  }

  const linesProperties: LineProperties[] = buildLinesProperties();

  function buildStyledCdaApiPath(): string {
    const params = new URLSearchParams({ reportItemId: itemId });
    if (attachmentId) params.set("attachmentId", attachmentId);
    if (inputId) params.set("inputId", inputId);
    if (readAccessKey) params.set("readAccessKey", readAccessKey);
    const currentPathname = window.location.pathname;
    const basePath = currentPathname.endsWith(pathname) ? currentPathname.slice(0, currentPathname.length - pathname.length) : "";
    return `${basePath}/validation-portal/api/styled-cda?${params.toString()}`;
  }

  const styledCdaUrl = useMemo(() => {
    if (!canStyleCda || typeof window === "undefined") {
      return null;
    }

    return buildStyledCdaApiPath();
  }, [buildStyledCdaApiPath, canStyleCda]);

  useEffect(() => {
    if (!styledCdaUrl) {
      setCdaStatus("idle");
      return;
    }

    let cancelled = false;
    setCdaStatus("checking");

    void (async () => {
      try {
        const cacheResponse = await fetch(styledCdaUrl, { method: "HEAD" });
        if (cancelled) {
          return;
        }

        if (cacheResponse.ok && cacheResponse.headers.get("X-Styled-Cda-Cache") === "HIT") {
          setCdaStatus("ready");
          return;
        }

        setCdaStatus("idle");
      } catch {
        if (!cancelled) {
          setCdaStatus("idle");
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [styledCdaUrl]);

  async function handleDisplayWithStylesheet() {
    if (cdaStatus === "checking" || cdaStatus === "generating" || !styledCdaUrl) return;

    if (cdaStatus === "ready") {
      window.open(styledCdaUrl, "_blank", "noopener,noreferrer");
      return;
    }

    setCdaStatus("generating");

    try {
      const response = await fetch(styledCdaUrl);
      if (!response.ok) {
        setCdaStatus("error");
        return;
      }

      setCdaStatus("ready");
    } catch {
      setCdaStatus("error");
    }
  }

  if (isLoading) {
    return <Skeleton className="h-screen w-full" />;
  }

  if (!fileContentBase64) {
    return <p>{t("gzl.texec.no_content_available")}</p>;
  }

  return (
    <div id={`file-renderer-${attachmentId ?? itemId}`}>
      <div className="flex items-center justify-end gap-3 px-2 pb-2">
        {canStyleCda && (
          <button
            onClick={handleDisplayWithStylesheet}
            disabled={cdaStatus === "checking" || cdaStatus === "generating"}
            className={`inline-flex items-center gap-2 rounded-lg border-2 px-4 py-2 text-sm font-medium transition-all duration-200 ${
              cdaStatus === "checking" || cdaStatus === "generating"
                ? "cursor-not-allowed border-grey_disabled text-grey_disabled opacity-70"
                : cdaStatus === "ready"
                  ? "border-green text-green hover:opacity-80 active:scale-95"
                  : "border-blue text-blue hover:bg-lightblue active:scale-95"
            }`}
          >
            {cdaStatus !== "checking" && cdaStatus !== "generating" && cdaStatus !== "ready" && <FileText size={16} />}
            {(cdaStatus === "checking" || cdaStatus === "generating") && <Loader2 size={16} className="animate-spin" />}
            {cdaStatus === "ready" && <CheckCircle size={16} />}
            {cdaStatus === "checking" || cdaStatus === "generating"
              ? t("gzl.validation_portal.generating")
              : t("gzl.validation_portal.display_with_stylesheet")}
          </button>
        )}

        {!isContentPage && (
          <Link
            href={
              `/validation-portal/content?itemId=${itemId}&attachmentId=${attachmentId}&fileName=${encodeURIComponent(fileName)}${
                readAccessKey ? `&readAccessKey=${encodeURIComponent(readAccessKey)}` : ""
              }` as Route
            }
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 p-2 text-blue hover:text-visited_link hover:underline"
          >
            <ExternalLink />
            {t("gzl.user.interface.open_in_new_tab")}
          </Link>
        )}
      </div>

      <div className={`${isContentPage ? "" : "max-h-[500px]"} overflow-y-auto rounded-b-xl`}>
        {fileType.renderer === "JSON" && (
          <JsonRenderer base64Data={fileType.base64Data} dataType={fileType.dataType ?? "HTTP"} linesProperties={linesProperties} />
        )}

        {fileType.renderer === "XML" && (
          <XmlRenderer
            base64Data={fileType.base64Data}
            dataType={fileType.dataType ?? "HTTP"}
            contentType="application/xml"
            linesProperties={linesProperties}
          />
        )}

        {fileType.renderer === "HEX" && <HexRenderer base64Data={fileType.base64Data} dataType={fileType.dataType ?? "BINARY"} />}

        {fileType.renderer === "RAW" && (
          <RawRenderer base64Data={fileType.base64Data} dataType={fileType.dataType ?? "HTTP"} linesProperties={linesProperties} />
        )}
      </div>
    </div>
  );
}

export default FileRenderer;
