import { MigrationErrorType } from "@/types/migration";

type ErrorTypeBadgeProps = {
  type: MigrationErrorType;
};

const errorTypeConfig: Record<MigrationErrorType, { label: string; colorClass: string }> = {
  MISSING_INPUT: {
    label: "Missing Input",
    colorClass: "bg-amber-100 text-amber-800 border-amber-200",
  },
  MISSING_VALIDATION_REPORT: {
    label: "Missing Report",
    colorClass: "bg-rose-100 text-rose-800 border-rose-200",
  },
  FILE_PARSING_ERROR: {
    label: "Parse Error",
    colorClass: "bg-red-100 text-red-800 border-red-200",
  },
  DATAHOUSE_ERROR: {
    label: "Datahouse Error",
    colorClass: "bg-red-100 text-red-800 border-red-200",
  },
  UNKNOWN_ERROR: {
    label: "Unknown",
    colorClass: "bg-gray-100 text-gray-800 border-gray-200",
  },
};

export function ErrorTypeBadge({ type }: ErrorTypeBadgeProps) {
  const config = errorTypeConfig[type];

  return (
    <span
      className={`inline-flex items-center px-2 py-1 text-xs font-medium rounded border ${config.colorClass}`}
    >
      {config.label}
    </span>
  );
}
