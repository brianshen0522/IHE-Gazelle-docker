import type { ReactNode } from "react";
import { SidePanelSection, SidePanelHeader, Skeleton } from "@gazelle/gazelle-component-ui";

export interface SidePanelProps {
  isOpen: boolean;
  children: ReactNode;
  className?: string;
  isLoading?: boolean;
  isError?: boolean;
  errorMessage?: string;
}

const SidePanel = ({
                     isOpen,
                     children,
                     className = "",
                     isLoading = false,
                     isError = false,
                     errorMessage = "An error has occurred while loading data.",
                   }: SidePanelProps) => {
  if (isError) {
    return <div className="text-red">{errorMessage}</div>;
  }

  if (isLoading) {
    return <Skeleton className="h-screen md:w-1/3"/>;
  }

  return (
    <div
      data-testid="sidepanel"
      className={`flex flex-col gap-8 overflow-auto z-10 rounded-lg transition-all duration-300 ease-in-out sticky top-2 max-h-[calc(100vh-0.5rem)]
                ${isOpen ? "translate-x-0 md:w-1/3" : "translate-x-full w-0"} ${className}`}
    >
      {children}
    </div>
  );
};

SidePanel.Header = SidePanelHeader;
SidePanel.Section = SidePanelSection;

export default SidePanel;
