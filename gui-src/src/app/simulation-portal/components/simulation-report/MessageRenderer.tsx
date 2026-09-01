import Link from "next/link";
import {ArrowRight, ExternalLink} from "lucide-react";
import {type Message, ValidationReport} from "@simulation-portal/types/SimulationReport";
import {ResultBadge} from "@shared/components/report/ResultBadge";
import FormattedDate from "@shared/components/dates/FormattedDate";
import useDataDownloader from "@hooks/useDataDownloader";
import {useItemLink} from "@shared/hooks/useItemLink";
import {useParams} from "next/navigation";

export interface MessageRendererProps {
  validatedMessage: ValidatedMessage;
  sutInitiator?: string;
  sutResponder?: string;
}

type ValidatedMessage = {
  message: Message;
  validationReport?: ValidationReport;
};

const MessageRenderer = ({ validatedMessage, sutInitiator, sutResponder }: MessageRendererProps) => {
  const { reportId } = useParams<{ reportId: string }>();
  const getItemLink = useItemLink(reportId);

  // Decode message content for download
  const messageContent = validatedMessage.message.content ? atob(validatedMessage.message.content) : "";
  const downloadMessage = useDataDownloader(
    messageContent,
    `message_${validatedMessage.message.name}`,
    "text/plain"
  );

  // Prepare validation report for download
  const validationReportContent = validatedMessage.validationReport?.content
    ? JSON.stringify(validatedMessage.validationReport.content, null, 2)
    : "";
  const downloadValidationReport = useDataDownloader(
    validationReportContent,
    `validation_report_${validatedMessage.message.name}.json`,
    "application/json"
  );

  /**
   * Renders a notice if message has no content
   */
  const renderNoContentNotice = () => {
    if (validatedMessage.message.content) return null;

    return (
      <span className="text-grey-400 text-sm italic">
        (message has no content)
      </span>
    );
  };

  /**
   * Renders a link to view a validation report
   * Uses the useItemLink hook to route to the correct portal based on itemType
   */
  const renderValidationReportLink = () => {
    if (!validatedMessage.validationReport?.reference) return null;

    const reference = validatedMessage.validationReport.reference;

    // Skip if reference is a placeholder like "ref" and not a valid URL or ID
    const isValidReference = reference.length > 3 && (
      reference.includes('/') || // URL or path
      /^[a-f0-9]{24}$/i.test(reference) // MongoDB ObjectId format
    );

    if (!isValidReference) {
      return null;
    }

    // Use the hook with simple datahouse item parameters
    const linkResult = getItemLink({
      reference: validatedMessage.validationReport.reference,
      itemType: validatedMessage.validationReport.itemType || "VALIDATION_REPORT",
      displayName: `Validation Report - ${validatedMessage.message.name}`,
    });

    if (linkResult.path) {
      return (
        <Link
          href={linkResult.path}
          className="hover:underline text-blue inline-flex items-center gap-1"
        >
          (view) <ExternalLink size={12} />
        </Link>
      );
    }

    // Fallback to direct link if itemType is unknown but reference looks valid
    if (reference.includes('/')) {
      return (
        <a
          href={validatedMessage.validationReport.reference}
          target="_blank"
          rel="noopener noreferrer"
          className="hover:underline text-blue inline-flex items-center gap-1"
        >
          (view) <ExternalLink size={12} />
        </a>
      );
    }

    return null;
  };

  return (
    <div
      key={validatedMessage.message.name}
      className="flex flex-col border border-grey-200 rounded-md bg-white p-3 px-6"
    >
      {/* Message header */}
      <div className="flex justify-between items-start">
        <div className="flex flex-wrap items-center gap-2">
          <p className="font-semibold">{validatedMessage.message.name}</p>
          {validatedMessage.message.content && (
            <span
              onClick={downloadMessage}
              className="hover:underline text-blue font-semibold cursor-pointer"
            >
              (download)
            </span>
          )}
          {renderNoContentNotice()}
        </div>
        <FormattedDate dateString={validatedMessage.message.dateTime} />
      </div>

      {/* Message flow: Sender -> Receiver */}
      <div className="flex items-center gap-1 mt-1 mx-10">
        {validatedMessage.message.sender || sutInitiator}
        <ArrowRight className="inline w-4 h-4" />
        {validatedMessage.message.receiver || sutResponder}
      </div>

      {/* Validation report section */}
      {validatedMessage.validationReport && (
        <div className="mt-2">
          <b>Validation report</b>
          <div className="flex items-center gap-2 mt-1 mx-10">
            <span>{validatedMessage.message.name}</span>
            {validatedMessage.validationReport.content && (
              <span
                onClick={downloadValidationReport}
                className="hover:underline text-blue cursor-pointer"
              >
                (download)
              </span>
            )}
            {renderValidationReportLink()}
            <ResultBadge result={validatedMessage.validationReport.validationResult} />
          </div>
        </div>
      )}
    </div>
  );
};

export default MessageRenderer;
