import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { StatusBadgeCell } from "./StatusBadgeCell";

// Mock the Badge component from the library
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Badge: ({ id, variant, children }: { id: string; variant: string; children: React.ReactNode }) =>
    React.createElement("div", { "data-testid": "badge", "data-id": id, "data-variant": variant }, children),
}));

describe("StatusBadgeCell", () => {
  it("renders with true value and correct variant", () => {
    render(<StatusBadgeCell id="test-badge" value={true} trueVariant="success" falseVariant="failed" trueLabel="Active" falseLabel="Inactive" />);

    const badge = screen.getByTestId("badge");
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveAttribute("data-variant", "success");
    expect(badge).toHaveTextContent("Active");
  });

  it("renders with false value and correct variant", () => {
    render(<StatusBadgeCell id="test-badge" value={false} trueVariant="success" falseVariant="failed" trueLabel="Active" falseLabel="Inactive" />);

    const badge = screen.getByTestId("badge");
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveAttribute("data-variant", "failed");
    expect(badge).toHaveTextContent("Inactive");
  });

  it("renders delegated/local variant correctly", () => {
    render(
      <StatusBadgeCell id="delegated-badge" value={true} trueVariant="variant-2" falseVariant="variant-1" trueLabel="Delegated" falseLabel="Local" />,
    );

    const badge = screen.getByTestId("badge");
    expect(badge).toHaveAttribute("data-variant", "variant-2");
    expect(badge).toHaveTextContent("Delegated");
  });

  it("renders with wrapper div with correct classes", () => {
    const { container } = render(
      <StatusBadgeCell id="test-badge" value={true} trueVariant="primary" falseVariant="default" trueLabel="Yes" falseLabel="No" />,
    );

    const wrapper = container.querySelector(".mx-3.my-1");
    expect(wrapper).toBeInTheDocument();
  });

  it("passes correct id to Badge component", () => {
    render(
      <StatusBadgeCell id="custom-id-123" value={false} trueVariant="warning" falseVariant="unknown" trueLabel="Warning" falseLabel="Unknown" />,
    );

    const badge = screen.getByTestId("badge");
    expect(badge).toHaveAttribute("data-id", "custom-id-123");
  });
});
