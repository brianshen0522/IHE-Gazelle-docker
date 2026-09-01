import { useTranslation } from "react-i18next";
import { Output } from "./TestStepOutputs";
import { getReportUrl } from "@test-execution/components/test-run/utils/getReportUrl";
import Link from "next/link";
import { Route } from "next";
import { AttachmentRenderer } from "@test-execution/components/test-report/maestroReport/AttachmentRenderer";

export function computeKindOfReport(fileName: string): string {
  if (fileName.toLowerCase().includes("validation")) return "VALIDATION_REPORT";
  if (fileName.toLowerCase().includes("simulation")) return "SIMULATION_REPORT";
  return "";
}

function displayOutputValue(output: Output) {
  if (output.fileName)
    return output.fileName;
  if (output.value === undefined)
    return output.name;
  return output.name + ": " + output.value;
}

const OutputItem = ({ output, inline, itemId }: { output: Output; inline?: boolean; itemId?: string }) => {
  const { t } = useTranslation();
  const label = output.fileName ?? output.name ?? t("gzl.texec.view_output");

  if (output.reference) {
    // Determine the kind of report first
    const kindOfReport = computeKindOfReport(output.fileName ?? output.name);

    // Handle validation/simulation reports (prioritize over generic attachments)
    if (kindOfReport === "VALIDATION_REPORT" || kindOfReport === "SIMULATION_REPORT") {
      const reportUrl = getReportUrl({
        testReportUrl: output.reference,
        kindOfReport,
      }) as Route;

      if (reportUrl) {
        return (
          <Link href={reportUrl} target="_blank" rel="noopener noreferrer" className="text-blue underline hover:text-visited_link">
            {label}
          </Link>
        );
      }
    }

    // Handle attachment or datahouse references using unified renderer
    if ((output.reference.includes("attachments") || output.reference.includes("datahouse")) && itemId) {
      return <AttachmentRenderer name={label} reference={output.reference} mimeType={output.mimeType} itemType={output.itemType} itemId={itemId} />;
    }
  }

  // Handle inline content (value present) or fallback to displaying name and value
  return <span className={inline ? "" : "ml-2"}>{displayOutputValue(output)}</span>;
};

export default OutputItem;
