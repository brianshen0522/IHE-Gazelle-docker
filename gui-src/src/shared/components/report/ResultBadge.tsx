import { JSX } from "react";
import { useTranslation } from "react-i18next";
import { Badge } from "@gazelle/gazelle-component-ui";
import { StepResult, TestResult } from "@maestro/types/report/Result";
import { TestRunStatus, TestStepStatus } from "@/app/test-execution/types/TestRunExecution";

type ResultType = TestResult | StepResult | TestRunStatus | TestStepStatus;

const VALID_RESULTS = [
  "SUCCESS",
  "PASSED",
  "FAILED",
  "UNDEFINED",
  "DONE",
  "IN_PROGRESS",
  "RUNNING",
  "COMPLETED",
  "NOT_STARTED",
  "READY_FOR_REVIEW",
  "TODO",
  "SKIPPED",
  "UNKNOWN",
] as const;

export const isValidResult = (value: string): value is ResultType => {
  return VALID_RESULTS.includes(value as ResultType);
};

export function ResultBadge({ result }: Readonly<{ result: ResultType }>): JSX.Element | null {
  const { t } = useTranslation();

  switch (result) {
    case "SUCCESS":
      return (
        <Badge id={t("gzl.user.interface.success")} variant="success">
          {t("gzl.user.interface.success")}
        </Badge>
      );

    case "PASSED":
      return (
        <Badge id={t("gzl.user.interface.passed")} variant="success">
          {t("gzl.user.interface.passed")}
        </Badge>
      );

    case "FAILED":
      return (
        <Badge id={t("gzl.user.interface.failed")} variant="failed">
          {t("gzl.user.interface.failed")}
        </Badge>
      );

    case "UNDEFINED":
      return (
        <Badge id={t("gzl.user.interface.undefined")} variant="default">
          {t("gzl.user.interface.undefined")}
        </Badge>
      );

    case "DONE":
      return (
        <Badge id={t("gzl.user.interface.done")} variant="primary">
          {t("gzl.user.interface.done")}
        </Badge>
      );

    case "IN_PROGRESS":
    case "RUNNING":
      return (
        <Badge id={t("gzl.user.interface.in_progress")} variant="primary">
          {t("gzl.user.interface.in_progress")}
        </Badge>
      );

    case "COMPLETED":
      return (
        <Badge id={t("gzl.user.interface.completed")} variant="primary">
          {t("gzl.user.interface.completed")}
        </Badge>
      );

    case "NOT_STARTED":
      return (
        <Badge id={t("gzl.user.interface.not_started")} variant="unknown">
          {t("gzl.user.interface.not_started")}
        </Badge>
      );

    case "READY_FOR_REVIEW":
      return (
        <Badge id={t("gzl.user.interface.ready_for_review")} variant="warning">
          {t("gzl.user.interface.ready_for_review")}
        </Badge>
      );

    default:
      return null;
  }
}
