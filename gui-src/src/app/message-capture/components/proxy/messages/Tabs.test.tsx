/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import CTabs from "./Tabs";

// Mock useTab context
const mockSetSelectedTab = vi.fn();
const mockSelectedTab = vi.fn();
vi.mock("@/shared/context/tabContext", () => ({
  useTab: () => ({
    selectedTab: mockSelectedTab(),
    setSelectedTab: mockSetSelectedTab,
  }),
}));

// Mock Tabs component
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Tabs: ({ tabNames, selectedTab, onTabSelect }: any) => (
    <div data-testid="tabs">
      {tabNames.map((name: string) => (
        <button key={name} onClick={() => onTabSelect(name)} data-selected={selectedTab === name} data-testid={`tab-${name}`}>
          {name}
        </button>
      ))}
    </div>
  ),
}));

describe("CTabs (Message Capture Tabs)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders both Content and Validation tabs when no connection error", () => {
    mockSelectedTab.mockReturnValue("Content");
    render(<CTabs isConnectionError={false} />);

    expect(screen.getByTestId("tab-Content")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Validation")).toBeInTheDocument();
  });

  it("does not render tabs when there is a connection error", () => {
    mockSelectedTab.mockReturnValue("Content");
    render(<CTabs isConnectionError={true} />);

    expect(screen.queryByTestId("tab-Content")).not.toBeInTheDocument();
    expect(screen.queryByTestId("tab-Validation")).not.toBeInTheDocument();
  });

  it("sets selected tab to Content on mount when connection error occurs", () => {
    mockSelectedTab.mockReturnValue("Validation");
    const { rerender } = render(<CTabs isConnectionError={false} />);

    rerender(<CTabs isConnectionError={true} />);

    expect(mockSetSelectedTab).toHaveBeenCalledWith("Content");
  });

  it("calls setSelectedTab when a tab is clicked", () => {
    mockSelectedTab.mockReturnValue("Content");
    render(<CTabs isConnectionError={false} />);

    const validationTab = screen.getByTestId("tab-Validation");
    fireEvent.click(validationTab);

    expect(mockSetSelectedTab).toHaveBeenCalledWith("Validation");
  });

  it("highlights the currently selected tab", () => {
    mockSelectedTab.mockReturnValue("Validation");
    render(<CTabs isConnectionError={false} />);

    const validationTab = screen.getByTestId("tab-Validation");
    expect(validationTab).toHaveAttribute("data-selected", "true");
  });

  it("applies correct CSS classes", () => {
    mockSelectedTab.mockReturnValue("Content");
    render(<CTabs isConnectionError={false} />);

    const container = screen.getByTestId("tabs").parentElement;
    expect(container).toHaveClass("flex", "items-center", "sm:gap-4", "pl-4");
  });

  it("resets to Content tab when isConnectionError changes", () => {
    mockSelectedTab.mockReturnValue("Validation");
    const { rerender } = render(<CTabs isConnectionError={false} />);

    // Trigger useEffect by changing isConnectionError
    rerender(<CTabs isConnectionError={true} />);

    expect(mockSetSelectedTab).toHaveBeenCalledWith("Content");
  });
});
