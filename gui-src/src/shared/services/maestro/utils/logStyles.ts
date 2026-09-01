import { Unplug, TriangleAlert, X, PlugZap, Check, LucideIcon } from "lucide-react";

// Supported log icon types
export type LogIconType = "Unplug" | "TriangleAlert" | "X" | "PlugZap" | "Check";

// Map of icon names to Lucide icon components
export const ICON_MAP: Record<LogIconType, LucideIcon> = {
  Unplug: Unplug,
  TriangleAlert: TriangleAlert,
  X: X,
  PlugZap: PlugZap,
  Check: Check,
};

// Icon color mapping based on icon type
export const ICON_COLORS: Record<LogIconType, string> = {
  TriangleAlert: "text-orange",
  X: "text-red",
  Check: "text-green",
  Unplug: "",
  PlugZap: "",
};

// Result color mapping based on result value
export const RESULT_COLORS: Record<string, string> = {
  PASSED: "text-green border-green",
  SUCCESS: "text-green",
  FAILED: "text-red",
  UNDEFINED: "text-grey-500",
  ERROR: "text-red",
  SKIPPED: "text-orange",
  DONE: "text-green",
};

// Border color mapping based on result value
export const BORDER_COLORS: Record<string, string> = {
  PASSED: "border-green",
  SUCCESS: "border-green",
  FAILED: "border-red",
  UNDEFINED: "border-grey-500",
  ERROR: "border-red",
  SKIPPED: "border-orange",
};

// Gets the appropriate color class for an icon
export function getIconColor(icon?: string): string {
  if (!icon || typeof icon !== "string") return "";
  return ICON_COLORS[icon as LogIconType] || "";
}

// Gets the appropriate color class for a result value
export function getResultColor(result?: string): string {
  if (!result) return "";
  const resultUpper = result.toUpperCase();
  return RESULT_COLORS[resultUpper] || "";
}

// Gets the appropriate border color class for a result value
export function getBorderColor(result?: string): string {
  if (!result) return "border-blue";
  const resultUpper = result.toUpperCase();
  return BORDER_COLORS[resultUpper] || "border-blue";
}
