import { JsonRenderer } from "@shared/components/renderers/json/JsonRenderer";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import { TreeRenderer } from "@message-capture/components/proxy/renderers/tree/TreeRenderer";
import { XmlRenderer } from "@shared/components/renderers/xml/XmlRenderer";
import { Download } from "lucide-react";
import DecompressedMsg from "@message-capture/components/proxy/messages/http/DecompressedMsg";
import TabRenderers from "@message-capture/components/proxy/renderers/TabRenderers";
import { Button, SectionTitle } from "@gazelle/gazelle-component-ui";
import { useState, useMemo } from "react";
import { useTranslation } from "react-i18next";
import useDataDownloader from "@message-capture/hooks/useDataDownloader";
import { useGetItem } from "@hooks/useGetItem";
import { useSession } from "next-auth/react";
import { HttpBodyProps } from "./Types";
import { getHttpMimeType } from "@/app/message-capture/utils/httpHeaders";

const HttpBody = ({ data, contentTypeHeader, id, searchParams }: HttpBodyProps) => {
  const { t } = useTranslation();
  const readAccessKey = searchParams.get("readAccessKey");
  const { data: session } = useSession();
  const { data: item } = useGetItem(id as string, session, readAccessKey ?? null);
  const isDecompressed = item ? item[0]?.content?.additionalParameters?.DECOMPRESSED : null;
  const { content } = data;

  const mimeType = getHttpMimeType(contentTypeHeader);

  const availableRenderers = useMemo(() => {
    // Handle HTTP request soap messages with body
    if (!mimeType && content.body) {
      return ["xml"];
    }

    if (mimeType.includes("x-www-form-urlencoded")) return ["raw"];

    const xmlContentType = mimeType.includes("xml") || mimeType.includes("html") || mimeType.includes("soap");
    const jsonContentType = mimeType.includes("json");
    const jsonContent = jsonContentType ? ["raw", "json", "tree"] : [];
    return xmlContentType ? ["raw", "xml", "tree"] : jsonContent;
  }, [content.body, mimeType]);
  const defaultRenderer = availableRenderers[0] || "raw";
  const [showRenderer, setShowRenderer] = useState<string>(defaultRenderer);

  // Reset renderer if it's not available for current message
  const activeRenderer = availableRenderers.includes(showRenderer) ? showRenderer : defaultRenderer;

  const binaryMimeTypes = ["application/dicom", "application/octet-stream", "image/.*", "audio/.*", "video/.*", "application/pdf"];

  const mimeTypeToFileExtension = {
    "application/xml": ".xml",
    "application/json": ".json",
    "application/dicom": ".dcm",
    "application/octet-stream": ".bin",
    "application/pdf": ".pdf",
    "text/html": ".html",
    "image/*": ".jpeg",
    "audio/*": ".mp3",
    "video/*": ".mp4",
  };

  const fileExtension = mimeTypeToFileExtension[mimeType as keyof typeof mimeTypeToFileExtension] || ".txt";
  const messageBody = content.body ? atob(content.body) : "";

  const isBinary = binaryMimeTypes.some((pattern) => {
    return new RegExp(`^${pattern}$`).test(mimeType);
  });

  const downloadBody = useDataDownloader(messageBody, `${data.id}${fileExtension}`, mimeType);

  const handleRendererChange = (renderer: string) => {
    setShowRenderer(renderer);
  };

  return (
    <>
      {(data.content.body ?? false) && (
        <div className="flex flex-col gap-4">
          <SectionTitle id="http-body" title={t('gzl.message.capture.body')} />
          {isDecompressed && <DecompressedMsg />}
          <div className="flex items-center justify-between">
            <TabRenderers specificRenderers={availableRenderers} showRenderer={activeRenderer} onRendererChange={handleRendererChange} />
            <div className="flex justify-end gap-2">
              <Button
                id="download-http-body"
                type="button"
                title={t("gzl.message.capture.download_body_of_http_message")}
                ariaLabelledby="download"
                onClick={downloadBody}
                variant="primary"
              >
                <Download size={14} />
                {t('gzl.message.capture.download_body')} {isBinary}
              </Button>
            </div>
          </div>
          {!isBinary && activeRenderer === "raw" && <RawRenderer base64Data={content.body} dataType="HTTP" />}
          {!isBinary && activeRenderer === "json" && <JsonRenderer base64Data={content.body} dataType="HTTP" />}
          {!isBinary && activeRenderer === "xml" && (
            <XmlRenderer base64Data={content.body} dataType="HTTP" contentType={contentTypeHeader} />
          )}
          {!isBinary && activeRenderer === "tree" && (
            <TreeRenderer node={content.body} dataType="HTTP" contentType={contentTypeHeader} />
          )}
          {isBinary && <p>{t("gzl.message.capture.content_is_binary_cannot_be_displayed_download_file_to_view")}.</p>}
        </div>
      )}
    </>
  );
};

export default HttpBody;
