import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import DisabledRegistration from "../DisabledRegistration";

// Mock next/link
vi.mock("next/link", () => ({
  default: ({ href, children, className }: { href: string; children: React.ReactNode; className?: string }) => (
    <a href={href} className={className}>
      {children}
    </a>
  ),
}));

// Mock lucide-react icons
vi.mock("lucide-react", () => ({
  AlertCircle: ({ className }: { className?: string }) => <svg data-testid="alert-icon" className={className} />,
}));

describe("DisabledRegistration", () => {
  it("renders the component with correct data-cy attribute", () => {
    const { container } = render(<DisabledRegistration />);
    const component = container.querySelector('[data-cy="disabled-registration"]');
    expect(component).toBeInTheDocument();
  });

  it("renders the title", () => {
    render(<DisabledRegistration />);
    const title = screen.getByText("Registration disabled");
    expect(title).toBeInTheDocument();
    expect(title.tagName).toBe("H3");
  });

  it("renders the disabled registration message", () => {
    render(<DisabledRegistration />);
    const message = screen.getByText("Sorry, user registration is disabled on this platform.");
    expect(message).toBeInTheDocument();
  });

  it("renders the AlertCircle icon", () => {
    render(<DisabledRegistration />);
    const icon = screen.getByTestId("alert-icon");
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveClass("text-orange");
  });

  it("renders a link to the home page", () => {
    render(<DisabledRegistration />);
    const homeLink = screen.getByRole("link", { name: /home/i });
    expect(homeLink).toBeInTheDocument();
    expect(homeLink).toHaveAttribute("href", "/home");
  });

  it("applies correct styling classes", () => {
    const { container } = render(<DisabledRegistration />);
    const component = container.querySelector('[data-cy="disabled-registration"]');
    expect(component).toHaveClass("flex", "flex-col", "gap-12");
  });

  it("renders the border container with orange theme", () => {
    const { container } = render(<DisabledRegistration />);
    const borderContainer = container.querySelector(".border.border-orange");
    expect(borderContainer).toBeInTheDocument();
  });
});
