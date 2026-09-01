import React from "react";
import { render, screen } from "@testing-library/react";
import ContentHeaderWrapper from "./ContentHeader";
import * as BreadcrumbsContext from "@shared/components/breadcrumbs/BreadcrumbsContext";
import { describe, it, beforeEach, vi, expect } from "vitest";

// Mock dependencies
vi.mock("@shared/components/breadcrumbs/OptionalBreadcrumbs", () => ({
  OptionalBreadcrumbs: () => <div data-testid="optional-breadcrumbs" />,
}));
vi.mock("@auth/SignInAutoRedirectWrapper", () => ({
  __esModule: true,
  default: ({ children }: { children: React.ReactNode }) => <div data-testid="signin-wrapper">{children}</div>,
}));

describe("ContentHeaderWrapper", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders title and children", () => {
    render(
      <ContentHeaderWrapper title="Test Title">
        <div>Child Content</div>
      </ContentHeaderWrapper>
    );
    expect(screen.getByText("Test Title")).toBeInTheDocument();
    expect(screen.getByText("Child Content")).toBeInTheDocument();
  });

  it("renders OptionalBreadcrumbs", () => {
    render(<ContentHeaderWrapper />);
    expect(screen.getByTestId("optional-breadcrumbs")).toBeInTheDocument();
  });

  it("wraps content with SignInAutoRedirectWrapper when secured is true", () => {
    render(<ContentHeaderWrapper title="Secured" secured={true} />);
    expect(screen.getByTestId("signin-wrapper")).toBeInTheDocument();
    expect(screen.getByText("Secured")).toBeInTheDocument();
  });

  it("does not wrap content with SignInAutoRedirectWrapper when secured is false", () => {
    render(<ContentHeaderWrapper title="Not Secured" secured={false} />);
    expect(screen.queryByTestId("signin-wrapper")).not.toBeInTheDocument();
    expect(screen.getByText("Not Secured")).toBeInTheDocument();
  });

  it("calls setBreadCrumbsProps and setBreadCrumbsItems when props are provided", () => {
    const setBreadCrumbsProps = vi.fn();
    const setBreadCrumbsItems = vi.fn();
    vi.spyOn(BreadcrumbsContext, "useBreadCrumbsContext").mockReturnValue({
      setBreadCrumbsProps,
      setBreadCrumbsItems,
    } as any);

    const breadcrumbsProps = { id: "test-breadcrumbs", ariaLabel: "Breadcrumb", items: [{ label: "Home", url: "/" }] };
    const breadcrumbsItems = [{ label: "Home", url: "/" }];

    render(<ContentHeaderWrapper breadcrumbsProps={breadcrumbsProps} breadcrumbsItems={breadcrumbsItems} />);

    expect(setBreadCrumbsProps).toHaveBeenCalledWith(breadcrumbsProps);
    expect(setBreadCrumbsItems).toHaveBeenCalledWith(breadcrumbsItems);
  });
});
