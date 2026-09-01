import { toast } from "react-toastify";
import { downloadBlob } from "@/shared/utils/downloadBlob";
import { ValidationReportDTO } from "@/shared/types/validation/types";

export type TranslationFunction = (key: string) => string;

export function handleDownloadValidationReport(validationReport: ValidationReportDTO, t: TranslationFunction) {
  const content = JSON.stringify(validationReport);
  if (!content) {
    toast.error(t("gzl.texec.no_content_to_download"));
    return;
  }
  const blob = new Blob([content], { type: "text/plain" });
  downloadBlob(blob, `validation_report_${validationReport.uuid}.json`);
}

export function handleDownloadValidatedItem(title: string, content64: string, t: TranslationFunction) {
  if (!content64?.length) {
    toast.error(t("gzl.texec.no_content_to_download"));
    return;
  }
  const validatedFile = atob(content64);
  const blob = new Blob([validatedFile], { type: "text/plain" });
  downloadBlob(blob, title.replaceAll(" ", "_"));
}

export async function handleDownloadAttachment(
  itemId: string,
  attachmentId: string,
  itemName: string,
  t: TranslationFunction,
  readAccessKey?: string
) {
  try {
    const readAccessKeyParam = readAccessKey ? `&readAccessKey=${encodeURIComponent(readAccessKey)}` : "";
    const url = `/gazelle/message-capture/api/attachment/${itemId}?attachmentId=${encodeURIComponent(attachmentId)}${readAccessKeyParam}`;
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(`Error: ${response.statusText} (status: ${response.status})`);
    }

    const blob = await response.blob();
    downloadBlob(blob, itemName.replaceAll(" ", "_"));
  } catch (error) {
    console.error("Error downloading attachment:", error);
    toast.error(t("gzl.texec.no_content_to_download"));
  }
}
