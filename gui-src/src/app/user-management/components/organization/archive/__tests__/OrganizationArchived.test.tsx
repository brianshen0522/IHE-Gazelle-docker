/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import OrganizationArchived from "../OrganizationArchived";

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Badge: ({ id, variant, children }: any) => (
    <span data-testid={id} data-variant={variant}>
      {children}
    </span>
  ),
  NoticeBanner: ({ color, children }: any) => (
    <div data-testid="notice-banner" data-color={color}>
      {children}
    </div>
  ),
  InfoRow: ({ label, value }: any) => (
    <div data-testid="info-row">
      <span data-testid="info-label">{label}</span>
      <span data-testid="info-value">{value}</span>
    </div>
  ),
}));

describe("OrganizationArchived", () => {
  const defaultProps = {
    name: "Test Organization",
    shortName: "test-org",
  };

  describe("Rendering", () => {
    it("renders archived badge", () => {
      render(<OrganizationArchived {...defaultProps} />);

      const badge = screen.getByTestId("archived-organization-badge");
      expect(badge).toBeInTheDocument();
      expect(badge).toHaveAttribute("data-variant", "warning");
    });

    it("renders notice banner with yellow color", () => {
      render(<OrganizationArchived {...defaultProps} />);

      const banner = screen.getByTestId("notice-banner");
      expect(banner).toBeInTheDocument();
      expect(banner).toHaveAttribute("data-color", "yellow");
    });

    it("renders organization name in info row", () => {
      render(<OrganizationArchived {...defaultProps} />);

      const infoRows = screen.getAllByTestId("info-row");
      const nameRow = infoRows.find((row) => row.textContent?.includes("Test Organization"));
      expect(nameRow).toBeInTheDocument();
    });

    it("renders organization short name in info row", () => {
      render(<OrganizationArchived {...defaultProps} />);

      const infoRows = screen.getAllByTestId("info-row");
      const shortNameRow = infoRows.find((row) => row.textContent?.includes("test-org"));
      expect(shortNameRow).toBeInTheDocument();
    });

    it("renders all expected elements", () => {
      render(<OrganizationArchived {...defaultProps} />);

      expect(screen.getByTestId("archived-organization-badge")).toBeInTheDocument();
      expect(screen.getByTestId("notice-banner")).toBeInTheDocument();
      expect(screen.getAllByTestId("info-row")).toHaveLength(2);
    });
  });

  describe("Props handling", () => {
    it("displays correct organization name", () => {
      render(<OrganizationArchived name="Custom Org" shortName="custom" />);

      expect(screen.getByText("Custom Org")).toBeInTheDocument();
    });

    it("displays correct short name", () => {
      render(<OrganizationArchived name="Test" shortName="custom-short" />);

      expect(screen.getByText("custom-short")).toBeInTheDocument();
    });

    it("handles empty name", () => {
      render(<OrganizationArchived name="" shortName="test" />);

      const infoRows = screen.getAllByTestId("info-row");
      expect(infoRows).toHaveLength(2);
    });

    it("handles empty short name", () => {
      render(<OrganizationArchived name="Test" shortName="" />);

      const infoRows = screen.getAllByTestId("info-row");
      expect(infoRows).toHaveLength(2);
    });
  });

  describe("Translation keys", () => {
    it("uses correct translation key for badge", () => {
      render(<OrganizationArchived {...defaultProps} />);

      expect(screen.getByText("Archived organization")).toBeInTheDocument();
    });

    it("uses correct translation key for notice", () => {
      render(<OrganizationArchived {...defaultProps} />);

      expect(screen.getByText("This organization has been archived and cannot be edited.")).toBeInTheDocument();
    });

    it("uses correct translation key for name label", () => {
      render(<OrganizationArchived {...defaultProps} />);

      const labels = screen.getAllByTestId("info-label");
      expect(labels.some((label) => label.textContent === "Name")).toBe(true);
    });

    it("uses correct translation key for short name label", () => {
      render(<OrganizationArchived {...defaultProps} />);

      const labels = screen.getAllByTestId("info-label");
      expect(labels.some((label) => label.textContent === "Short name")).toBe(true);
    });
  });
});
