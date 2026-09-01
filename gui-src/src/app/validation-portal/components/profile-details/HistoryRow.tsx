import { ValidationHistory } from "../../types/ValidationProfile";
import { useRef } from "react";
import Link from "next/link";
import { Badge, Tooltip } from "@gazelle/gazelle-component-ui";
import { Route } from "next";

interface HistoryRowProps {
  entry: ValidationHistory;
  formatDate: (value: string | number) => React.ReactNode;
  reportHref: Route;
  tooltipText: string;
  linkTitle: string;
}

type HistoryResult = ValidationHistory["result"];

const RESULT_VARIANT_MAP: Record<HistoryResult, "success" | "failed" | "unknown"> = {
  PASSED: "success",
  FAILED: "failed",
  UNKNOWN: "unknown",
};

function HistoryRow({ entry, formatDate, reportHref, tooltipText, linkTitle }: Readonly<HistoryRowProps>) {
  const linkRef = useRef<HTMLAnchorElement>(null);

  return (
    <tr className="hover:bg-lightpurple/20 transition-colors duration-150">
      <td className="px-4 py-3 whitespace-nowrap">
        <Link
          ref={linkRef}
          href={reportHref}
          className="text-blue hover:text-visited_link hover:underline transition-colors duration-150"
          title={linkTitle}
        >
          {formatDate(entry.executionDate)}
        </Link>

        <Tooltip id={`history-link-${entry.reportId}`} position="bottom" triggerRef={linkRef}>
          {tooltipText}
        </Tooltip>
      </td>

      <td className="px-4 py-3 whitespace-nowrap">
        <Badge id={`result-${entry.reportId}`} variant={RESULT_VARIANT_MAP[entry.result]}>
          {entry.result}
        </Badge>
      </td>
    </tr>
  );
}

export default HistoryRow;
