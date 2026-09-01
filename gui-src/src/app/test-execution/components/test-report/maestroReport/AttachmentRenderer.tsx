import Link from "next/link";
import { ExternalLink } from "lucide-react";
import { useItemLink } from "@shared/hooks/useItemLink";
import { ButtonDownloadAttachment } from "@test-execution/components/test-report/ButtonDownloadAttachment";
import { DatahouseItemReference } from "@shared/types/datahouse/DatahouseItem";
import { getTypeFromReferences } from "./utils/propertyUtils";

interface AttachmentRendererProps {
  /** File/attachment name to display */
  name: string;
  /** Reference URL (attachment or datahouse) */
  reference: string;
  /** MIME type of the file */
  mimeType?: string;
  /** Datahouse item type (e.g., TEST_REPORT, VALIDATION_REPORT) */
  itemType?: string;
  /** Current report/item ID for fetching attachments */
  itemId: string;
  /** Datahouse item references for type lookup */
  references?: DatahouseItemReference[];
}

/**
 * Unified renderer for attachments and datahouse references
 * Handles both attachment downloads and datahouse item links
 *
 * Used by PropertyRenderer (for Maestro report properties) and OutputItem (for test step outputs)
 *
 * @example
 * ```tsx
 * <AttachmentRenderer
 *   name="test-report.pdf"
 *   reference="/attachments/abc123"
 *   mimeType="application/pdf"
 *   itemId="report-456"
 * />
 * ```
 */
export function AttachmentRenderer({ name, reference, mimeType, itemType, itemId, references = [] }: Readonly<AttachmentRendererProps>) {
  const getItemLinkUrl = useItemLink(itemId);

  // Handle attachment references (e.g., "/attachments/69e09685fc196c89c26605a7")
  if (reference.includes("attachments")) {
    const attachmentId = reference.split("/").pop();
    if (attachmentId) {
      const attachmentType = mimeType || getTypeFromReferences(attachmentId, references);
      return <ButtonDownloadAttachment reportName={name} itemId={itemId} attachmentId={attachmentId} attachmentType={attachmentType} />;
    }
  }

  // Handle datahouse item references
  if (reference.includes("datahouse")) {
    const linkResult = getItemLinkUrl({
      reference,
      itemType,
      displayName: name,
    });

    if (linkResult.path) {
      return (
        <Link className="flex gap-1 items-center text-blue" href={linkResult.path} title={linkResult.displayName || "Open report"}>
          {name} <ExternalLink size={14} />
        </Link>
      );
    }

    // Reference exists but no valid path (unknown item type or missing config)
    return (
      <a className="flex gap-1 items-center text-blue" href={reference} target="_blank" rel="noopener noreferrer" title="Open item">
        {name} <ExternalLink size={14} />
      </a>
    );
  }

  // Fallback for unknown reference types
  return <span className="text-grey-500">{name}</span>;
}
