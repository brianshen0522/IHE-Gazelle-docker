/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import DelegatedOrgaDetails from "../DelegatedOrgaDetails";

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  NoticeBanner: ({ children, color }: any) => (
    <div data-testid="notice-banner" data-color={color}>
      {children}
    </div>
  ),
  InfoRow: ({ label, value }: any) => (
    <div data-testid="info-row" data-label={label}>
      <span className="label">{label}:</span> <span className="value">{value}</span>
    </div>
  ),
}));

describe("DelegatedOrgaDetails", () => {
  const defaultProps = {
    name: "Test Organization",
    shortName: "test-org",
  };

  it("renders notice banner with delegated organization info", () => {
    render(<DelegatedOrgaDetails {...defaultProps} />);

    const noticeBanner = screen.getByTestId("notice-banner");
    expect(noticeBanner).toBeInTheDocument();
    expect(noticeBanner).toHaveAttribute("data-color", "yellow");
    expect(noticeBanner).toHaveTextContent("This organization is delegated, it cannot be edited nor deleted in Gazelle.");
  });

  it("renders legal name info row", () => {
    render(<DelegatedOrgaDetails {...defaultProps} />);

    const infoRows = screen.getAllByTestId("info-row");
    const nameRow = infoRows.find((row) => row.dataset.label === "Name");

    expect(nameRow).toBeInTheDocument();
    expect(nameRow).toHaveTextContent("Name: Test Organization");
    expect(nameRow).toHaveTextContent("Test Organization");
  });

  it("renders short name info row", () => {
    render(<DelegatedOrgaDetails {...defaultProps} />);

    const infoRows = screen.getAllByTestId("info-row");
    const shortNameRow = infoRows.find((row) => row.dataset.label === "Short name");

    expect(shortNameRow).toBeInTheDocument();
    expect(shortNameRow).toHaveTextContent("Short name: test-org");
    expect(shortNameRow).toHaveTextContent("test-org");
  });

  it("renders all info rows with correct structure", () => {
    render(<DelegatedOrgaDetails {...defaultProps} />);

    const infoRows = screen.getAllByTestId("info-row");
    // Should have 2 info rows: legal name, short name
    expect(infoRows).toHaveLength(2);
  });

  it("applies correct styling classes", () => {
    const { container } = render(<DelegatedOrgaDetails {...defaultProps} />);

    const section = container.querySelector("section");
    expect(section).toHaveClass("w-full", "flex", "flex-col", "gap-y-2");
  });
});
