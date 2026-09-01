import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import AppLink from "../AppLink";

// Mock next/link
vi.mock("next/link", () => ({
  default: ({
    href,
    children,
    className,
    target,
    rel,
  }: {
    href: string;
    children: React.ReactNode;
    className?: string;
    target?: string;
    rel?: string;
  }) => (
    <a href={href} className={className} target={target} rel={rel}>
      {children}
    </a>
  ),
}));

describe("AppLink", () => {
  it("renders children correctly", () => {
    render(<AppLink href="/test">Test Link</AppLink>);
    const link = screen.getByRole("link", { name: /test link/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveTextContent("Test Link");
  });

  it("renders with correct href", () => {
    render(<AppLink href="/user-management">User Management</AppLink>);
    const link = screen.getByRole("link", { name: /user management/i });
    expect(link).toHaveAttribute("href", "/user-management");
  });

  it("applies default styling classes", () => {
    render(<AppLink href="/test">Link</AppLink>);
    const link = screen.getByRole("link");
    expect(link).toHaveClass("flex");
    expect(link).toHaveClass("items-center");
    expect(link).toHaveClass("justify-center");
    expect(link).toHaveClass("border");
    expect(link).toHaveClass("border-purple");
    expect(link).toHaveClass("p-2");
    expect(link).toHaveClass("rounded-md");
    expect(link).toHaveClass("hover:bg-purple");
    expect(link).toHaveClass("hover:text-white");
    expect(link).toHaveClass("transition-colors");
    expect(link).toHaveClass("duration-300");
    expect(link).toHaveClass("shadow-md");
  });

  it("applies custom className", () => {
    render(
      <AppLink href="/test" className="custom-class">
        Link
      </AppLink>,
    );
    const link = screen.getByRole("link");
    expect(link).toHaveClass("custom-class");
    expect(link).toHaveClass("border-purple"); // Should still have default classes
  });

  it("does not add target and rel attributes for internal links", () => {
    render(<AppLink href="/internal-page">Internal Link</AppLink>);
    const link = screen.getByRole("link");
    expect(link).not.toHaveAttribute("target");
    expect(link).not.toHaveAttribute("rel");
  });

  it("adds target='_blank' for external http links", () => {
    render(<AppLink href="http://example.com">External Link</AppLink>);
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("target", "_blank");
  });

  it("adds target='_blank' for external https links", () => {
    render(<AppLink href="https://example.com">External Link</AppLink>);
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("target", "_blank");
  });

  it("adds rel='noopener noreferrer' for external http links", () => {
    render(<AppLink href="http://example.com">External Link</AppLink>);
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("rel", "noopener noreferrer");
  });

  it("adds rel='noopener noreferrer' for external https links", () => {
    render(<AppLink href="https://example.com">External Link</AppLink>);
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("rel", "noopener noreferrer");
  });

  it("renders with ReactNode children", () => {
    render(
      <AppLink href="/test">
        <span>Icon</span>
        <span>Text</span>
      </AppLink>,
    );
    const link = screen.getByRole("link");
    expect(link).toHaveTextContent("IconText");
    expect(screen.getByText("Icon")).toBeInTheDocument();
    expect(screen.getByText("Text")).toBeInTheDocument();
  });
});
