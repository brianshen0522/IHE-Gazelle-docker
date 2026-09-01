import { useEffect } from "react";
import { LineProperties } from "@shared/components/renderers/Renderers";

// Custom hook to auto-expand visible lines when a selected line is beyond the current visible range
export function useAutoExpandLines(linesProperties: LineProperties[] | undefined, visibleLines: number, setVisibleLines: (lines: number) => void) {
  useEffect(() => {
    if (linesProperties && linesProperties.length > 0) {
      const selectedLine = linesProperties.find((lp) => lp.selected);
      if (selectedLine && selectedLine.lineNumber > visibleLines) {
        setVisibleLines(selectedLine.lineNumber + 100);
      }
    }
  }, [linesProperties, visibleLines, setVisibleLines]);
}
