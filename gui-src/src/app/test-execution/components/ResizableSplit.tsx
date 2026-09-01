// TODO: move to ui library
"use client";
import React, { useState, useCallback, useRef, useEffect } from "react";
import { useTranslation } from "react-i18next";

interface ResizableSplitProps {
  leftComponent: React.ReactNode;
  rightComponent: React.ReactNode;
  initialLeftWidth?: number; // en pourcentage
  minLeftWidth?: number; // en pourcentage
  maxLeftWidth?: number; // en pourcentage
  className?: string;
  showRight?: boolean;
  onToggleRight?: () => void;
  toggleButtonLabel?: string;
  toggleButtonIcon?: React.ReactNode;
}

const ResizableSplit = ({
  leftComponent,
  rightComponent,
  initialLeftWidth = 50,
  minLeftWidth = 30,
  maxLeftWidth = 70,
  className,
  showRight = true,
  onToggleRight,
  toggleButtonLabel,
  toggleButtonIcon,
}: ResizableSplitProps) => {
  const { t } = useTranslation();
  const [leftWidth, setLeftWidth] = useState(initialLeftWidth);
  const containerRef = useRef<HTMLDivElement>(null);
  const isDragging = useRef(false);
  const [isResizing, setIsResizing] = useState(false);

  const handleMouseDown = useCallback(() => {
    isDragging.current = true;
    setIsResizing(true);
  }, []);

  const handleMouseUp = useCallback(() => {
    isDragging.current = false;
    setIsResizing(false);
  }, []);

  const handleMouseMove = useCallback(
    (e: MouseEvent) => {
      if (!isDragging.current || !containerRef.current) return;

      e.preventDefault();

      const container = containerRef.current;
      const containerRect = container.getBoundingClientRect();
      const newLeftWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100;

      // Limiter le redimensionnement
      if (newLeftWidth >= minLeftWidth && newLeftWidth <= maxLeftWidth) {
        setLeftWidth(newLeftWidth);
      }
    },
    [minLeftWidth, maxLeftWidth],
  );

  useEffect(() => {
    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);

    return () => {
      document.removeEventListener("mousemove", handleMouseMove);
      document.removeEventListener("mouseup", handleMouseUp);
    };
  }, [handleMouseMove, handleMouseUp]);

  return (
    <div className={`flex w-full h-full ${className}`} ref={containerRef}>
      <div
        className={`overflow-auto pr-1 h-full min-h-0 ${isResizing ? "" : "transition-all duration-500 ease-in-out"}`}
        style={{ width: showRight ? `${leftWidth}%` : "100%" }}
      >
        {leftComponent}
      </div>

      {onToggleRight && (
        <div className="flex items-center justify-center">
          <button
            onClick={onToggleRight}
            className="flex flex-col items-center p-1 rounded-l-lg hover:bg-lightgrey transition-all duration-300 border border-grey shadow-sm"
            title={toggleButtonLabel}
            type="button"
          >
            {toggleButtonIcon}
            <span className="mt-2 text-xs [writing-mode:vertical-rl] transition-all duration-300 ease-in-out">
              {showRight ? t("gzl.texec.hide_sequence_diagram") : t("gzl.texec.show_sequence_diagram")}
            </span>
          </button>
        </div>
      )}

      <div
        className={`bg-grey hover:bg-blue cursor-col-resize flex-shrink-0 active:bg-purple select-none ${isResizing ? "" : "transition-all duration-500 ease-in-out"} ${showRight ? "w-1 opacity-100 my-3" : "w-0 opacity-0"}`}
        onMouseDown={handleMouseDown}
      />

      <div
        className={`overflow-auto h-full min-h-0 ${isResizing ? "" : "transition-all duration-500 ease-in-out"} ${showRight ? "opacity-100 pl-1" : "opacity-0 pointer-events-none"}`}
        style={{ width: showRight ? `${100 - leftWidth}%` : "0%" }}
      >
        {rightComponent}
      </div>
    </div>
  );
};

export default ResizableSplit;
