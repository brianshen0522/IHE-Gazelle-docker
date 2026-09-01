import CollapsableSubCard from "@shared/components/boxes/CollapsableSubCard";
import { type Message, Role, type TransactionReport, ValidationReport } from "@simulation-portal/types/SimulationReport";
import MessageRenderer from "@simulation-portal/components/simulation-report/MessageRenderer";
import { ArrowRightLeft } from "lucide-react";
import { useEffect, useState } from "react";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import UnexpectedErrorDisplay from "@simulation-portal/components/simulation-report/UnexpectedErrorDisplay";
import NoticeBanner from "@shared/components/banner/NoticeBanner";

type ValidatedMessage = {
  message: Message;
  validationReport?: ValidationReport;
};

const TransactionReportRenderer = ({ transactionReport }: { transactionReport: TransactionReport }) => {
  const sut = " [SUT]";
  const initiator = `${transactionReport.initiator.name}${transactionReport.initiator.simulated ? "" : sut}`;
  const responder = `${transactionReport.responder.name}${transactionReport.responder.simulated ? "" : sut}`;
  const [validatedMessages, setValidatedMessages] = useState<ValidatedMessage[]>([]);

  useEffect(() => {
    if (!transactionReport.messages) return;

    const pairs: ValidatedMessage[] = transactionReport.messages.map((message) => ({
      message,
      validationReport: transactionReport.validationReports?.find((report) => report.subject === message.name),
    }));

    setValidatedMessages(pairs);
  }, [transactionReport]);

  const transactionTitle = () => {
    return (
      <div className="flex justify-between items-start w-full">
        <div className="flex flex-wrap items-center gap-1">
          <b>{transactionReport.transaction}: </b>
          {initiator}
          <ArrowRightLeft className="inline w-10" />
          {responder}
        </div>
        <ResultBadge result={transactionReport.result} />
      </div>
    );
  };

  const getSut = (role: Role) => {
    return role.simulated ? undefined : `${role.name}${sut}`;
  };

  const hasUnexpectedErrors = (): boolean => {
    return (transactionReport?.unexpectedErrors?.length ?? 0) > 0;
  };

  if (!transactionReport) {
    return null;
  }

  return (
    <CollapsableSubCard key={transactionReport.transaction} title={transactionTitle()}>
      <div className="px-6 pb-2">
        {hasUnexpectedErrors() && <UnexpectedErrorDisplay errors={transactionReport.unexpectedErrors} />}
        {transactionReport.result === "FAILED" && (
          <NoticeBanner color="red" weight="semibold">
            {transactionReport.note || "The transaction failed."}
          </NoticeBanner>
        )}
        <p className="font-semibold">Messages</p>
        <div className="mt-2 w-full space-y-2">
          {validatedMessages.map((validatedMessage) => (
            <MessageRenderer
              key={validatedMessage.message.name}
              validatedMessage={validatedMessage}
              sutInitiator={getSut(transactionReport.initiator)}
              sutResponder={getSut(transactionReport.responder)}
            />
          ))}
        </div>
      </div>
    </CollapsableSubCard>
  );
};

export default TransactionReportRenderer;
