import React, { JSX } from "react";
import { LineProperties } from "@shared/components/renderers/Renderers";
import { CircleAlert, CircleX, Info, TriangleAlert } from "lucide-react";

export function displayLineFromLinesProperties(
  lineProperties: LineProperties[] | undefined,
  line: number,
): React.HTMLProps<HTMLElement> & { icon?: JSX.Element } {
  if (lineProperties === undefined) return {};
  for (const lp of lineProperties) {
    if (lp.lineNumber === line) {
      const style: React.CSSProperties = { display: "block", width: "100" };
      let className = "";
      style.backgroundColor = lp.color;
      style.cursor = "pointer";
      if (lp.selected) {
        style.outline = "2px solid purple ";
        style.borderRadius = "2px";
        className += "gzl-selected-line";
      }
      return { style, onClick: lp.onClickHandler, className, icon: <CircleAlert size={16} color="orange" /> };
    }
  }
  return {};
}

export function getCLassNameForLine(lineProperties: LineProperties[] | undefined, line: number): string {
  if (lineProperties === undefined) return "";
  for (const lp of lineProperties) {
    if (lp.lineNumber === line) {
      let result = "w-full block cursor-pointer ";
      if (lp.selected) {
        result += "border-purple border-2 rounded gzl-selected-line";
      }
      return result;
    }
  }
  return "";
}

export function getColorToHighlight(linesProperties: LineProperties[] | undefined, lineNumber: number): string {
  if (!linesProperties || linesProperties.length === 0) return "";
  const lineProperties = linesProperties.find((l) => l.lineNumber === lineNumber);
  return lineProperties?.lineNumber === lineNumber ? lineProperties.color : "";
}

export function getIconToHighlight(linesProperties: LineProperties[] | undefined, lineNumber: number): JSX.Element {
  if (!linesProperties || linesProperties.length === 0) return <></>;
  const lineProperties = linesProperties.find((l) => l.lineNumber === lineNumber);
  if (lineProperties?.lineNumber === lineNumber) {
    switch (lineProperties.severity) {
      case "ERROR":
        return <CircleX color="black" className="inline items-center" strokeWidth={2.5} height={18} />;
      case "WARNING":
        return <TriangleAlert color="black" className="inline" strokeWidth={2.5} height={18} />;
      case "INFO":
        return <Info color="black" className="inline" strokeWidth={2.5} height={18} />;
      default:
        return <></>;
    }
  }
  return <></>;
}
