import React from "react";
import { UnexpectedError } from "@maestro/types/report/UnexpectedError";
import { useTranslation } from "react-i18next";

interface UnexpectedErrorDisplayProps {
  /** Array of unexpected errors to display */
  errors: UnexpectedError[];
  /** Optional ID for the error container (for testing/accessibility) */
  id?: string;
  /** Optional custom class name */
  className?: string;
}

/**
 * Recursively renders an unexpected error with its cause chain
 */
function renderError(error: UnexpectedError, index: number, causedBy = false): React.ReactNode {
  return (
    <span key={index} className="ml-3 block">
      {`${causedBy ? "Caused by: " : ""}${error.name ?? ""}: ${error.message}`}
      {error.cause && renderError(error.cause, index, true)}
    </span>
  );
}

/**
 * Displays unexpected errors in a styled error box
 *
 * This component is reusable across test-execution and simulation-portal
 * for displaying unexpected errors that occurred during test/simulation execution.
 *
 * @example
 * ```tsx
 * <UnexpectedErrorDisplay
 *   errors={testRun.unexpectedErrors}
 *   id="test-run-errors"
 * />
 * ```
 */
export function UnexpectedErrorDisplay({ errors, id, className = "" }: Readonly<UnexpectedErrorDisplayProps>) {
  const { t } = useTranslation();

  if (!errors || errors.length === 0) {
    return null;
  }

  return (
    <div className={`bg-[#f8d7da] border-red border-1 p-2 rounded-medium my-2 ${className}`} role="alert" aria-live="polite">
      <p id={id} className="font-semibold">
        {t("gzl.texec.unexpected_errors")}:
      </p>
      <div>{errors.map((error, index) => renderError(error, index, false))}</div>
    </div>
  );
}
