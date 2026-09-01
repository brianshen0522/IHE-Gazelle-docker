import { render, screen } from "@testing-library/react";
import { useSession, signIn } from "next-auth/react";
import SignInAutoRedirectWrapper from "../SignInAutoRedirectWrapper";
import { vi, describe, beforeEach, it, expect, Mock } from "vitest";

// Mock dependencies
vi.mock("next-auth/react");
vi.mock("@hooks/useSignInAutoRedirect", () => ({
  default: vi.fn(),
}));
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Skeleton: (props: any) => <div data-testid="skeleton" {...props} />,
}));

describe("SignInAutoRedirectWrapper", () => {
  const mockUseSession = useSession as Mock;

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders skeletons when session is null", () => {
    mockUseSession.mockReturnValue({ data: null, status: "loading" });

    render(
      <SignInAutoRedirectWrapper>
        <div>Child</div>
      </SignInAutoRedirectWrapper>
    );

    // Should render two Skeletons
    const skeletons = screen.getAllByTestId("skeleton");
    expect(skeletons).toHaveLength(2);
    expect(skeletons[0]).toHaveClass("h-8");
  });

  it("renders children when session exists", () => {
    mockUseSession.mockReturnValue({
      data: { access_token: "token" },
      status: "authenticated",
    });

    render(
      <SignInAutoRedirectWrapper>
        <div data-testid="child">Child</div>
      </SignInAutoRedirectWrapper>
    );

    expect(screen.getByTestId("child")).toBeInTheDocument();
  });

  it("calls signIn if authenticated but access_token is missing", () => {
    mockUseSession.mockReturnValue({
      data: {},
      status: "authenticated",
    });
    const mockSignIn = signIn as Mock;
    render(
      <SignInAutoRedirectWrapper>
        <div>Child</div>
      </SignInAutoRedirectWrapper>
    );
    expect(mockSignIn).toHaveBeenCalledWith("keycloak");
  });
});
