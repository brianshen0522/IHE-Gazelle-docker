import { useEffect, useState } from "react";
import { Button } from "@gazelle/gazelle-component-ui";
import { Download } from "lucide-react";
import { useGetAttachment } from "@message-capture/hooks/swr-requests/useGetAttachment";
import useDataDownloader from "@message-capture/hooks/useDataDownloader";
import { downloadBlob } from "@shared/utils/downloadBlob";
import { Part } from "@/app/message-capture/components/proxy/messages/multiparts/Types";
import { DownloadContentButtonProps, MessageContent } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";
import { resolveDownloadFilename, resolveDownloadMimeType } from "./downloadMetadata";

const DownloadContentButton = ({ content, messageId, attachmentId }: DownloadContentButtonProps) => {
  const { t } = useTranslation();
  const { type, id, contentType, raw, headers } = content as Part & {
    headers?: Record<string, unknown>;
  };
  const [downloadAttachment, setDownloadAttachment] = useState(false);
  const itemId = messageId ?? id;
  const { data, isError } = useGetAttachment({ downloadAttachment, itemId, attachmentId: attachmentId });
  const downloadDataContent = raw ?? (content?.content ? atob((content as MessageContent).content) : "No content to download");
  const downloadFilename = resolveDownloadFilename(content);
  const downloadMimeType = resolveDownloadMimeType(content);
  const downloadData = useDataDownloader(downloadDataContent, downloadFilename, downloadMimeType || contentType);
  const textPart = type === "TEXT_PART";

  useEffect(() => {
    if (!downloadAttachment || !data) return;

    const attachmentBlob = new Blob([data], { type: data.type });
    const attachmentFilename = resolveDownloadFilename({ type, contentType, headers }, data.type);
    downloadBlob(attachmentBlob, attachmentFilename);
    setDownloadAttachment(false);
  }, [contentType, data, downloadAttachment, headers, type]);

  const handleDownload = () => {
    setDownloadAttachment(true);
  };

  if (isError) return <div className="text-red">Failed to fetch attachment</div>;

  return (
    <Button
      id={`download_content_${id}`}
      type="button"
      title={t("gzl.message.capture.download_content")}
      ariaLabelledby={t("gzl.message.capture.download_content")}
      variant="primary"
      onClick={!textPart && attachmentId ? handleDownload : downloadData}
      className="flex items-center gap-2"
    >
      <Download size={14} />
      {t("gzl.message.capture.download_content")}
    </Button>
  );
};

export default DownloadContentButton;
