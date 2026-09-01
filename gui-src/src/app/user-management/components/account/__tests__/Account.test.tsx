/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import Account from "../Account";

// Create mock functions
const mockPush = vi.fn();
const mockBack = vi.fn();
const mockMutate = vi.fn();
const mockHandleNavigation = vi.fn((callback: () => void) => callback());
const mockSearchParams = new URLSearchParams();

// Mock module implementations
const mockUseSession = vi.fn();
const mockUseRouter = vi.fn();
const mockUseSearchParams = vi.fn();
const mockUseGetUserById = vi.fn();
const mockUseGetUserPreferencesById = vi.fn();
const mockUseGetUserPicture = vi.fn();

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useRouter: () => mockUseRouter(),
  useSearchParams: () => mockUseSearchParams(),
}));

// Mock next-auth
vi.mock("next-auth/react", () => ({
  useSession: () => mockUseSession(),
}));

// Mock custom hooks
vi.mock("@/app/user-management/hooks/swr/useGetUser", () => ({
  useGetUserById: (id: string) => mockUseGetUserById(id),
  useGetUserPreferencesById: (id: string) => mockUseGetUserPreferencesById(id),
  useGetUserPicture: (id: string, size: string) => mockUseGetUserPicture(id, size),
}));

vi.mock("@shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: () => ({
    hasUnsavedChanges: false,
    setHasUnsavedChanges: vi.fn(),
    handleNavigation: mockHandleNavigation,
  }),
}));

// Mock i18n
vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        "gzl.gum.my_account": "My account",
        "gzl.gum.go_back": "Go back",
      };
      return translations[key] || key;
    },
  }),
}));

// Mock EditUserContextProvider
vi.mock("@user-management/context/EditUserContext", () => ({
  EditUserContextProvider: ({ children }: any) => <div data-testid="edit-user-context-provider">{children}</div>,
}));

// Mock child components
vi.mock("@gazelle/gazelle-component-ui", () => ({
  ContentHeader: ({ title, onGoBack, content, isGoBack }: any) => (
    <div data-testid="content-header">
      <h1 data-testid="header-title">{title}</h1>
      {isGoBack && (
        <button data-testid="go-back-button" onClick={onGoBack}>
          {content}
        </button>
      )}
    </div>
  ),
  Skeleton: ({ className }: any) => <div data-testid="skeleton" className={className}></div>,
}));

vi.mock("@user-management/components/user-management/EditUser", () => ({
  default: ({ userPictureUrl, isSidePanelContext, account }: any) => (
    <div data-testid="edit-user">
      <div data-testid="user-picture-url">{userPictureUrl}</div>
      <div data-testid="is-side-panel">{isSidePanelContext.toString()}</div>
      <div data-testid="is-account">{account.toString()}</div>
    </div>
  ),
}));

describe("Account Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSearchParams.delete("origin");

    // Setup router mock
    mockUseRouter.mockReturnValue({
      push: mockPush,
      back: mockBack,
      refresh: vi.fn(),
      replace: vi.fn(),
      prefetch: vi.fn(),
      forward: vi.fn(),
    });

    // Setup session mock
    mockUseSession.mockReturnValue({
      data: {
        user: {
          id: "user-123",
          gazelleId: "user-123",
          name: "John Doe",
          email: "test@example.com",
          groups: ["group-1", "group-2"],
          organization: "org-1",
        },
        expires: "2026-12-31",
      },
    });

    // Setup search params mock
    mockUseSearchParams.mockReturnValue(mockSearchParams);

    // Setup user data mocks
    mockUseGetUserById.mockReturnValue({
      data: {
        data: {
          id: "user-123",
          firstName: "John",
          lastName: "Doe",
          email: "test@example.com",
          delegated: false,
        },
      },
      isLoading: false,
      mutate: mockMutate,
    });

    mockUseGetUserPreferencesById.mockReturnValue({
      data: {
        data: {
          tableLabel: "compact",
          notificationByEmail: true,
          languagesSpoken: ["en"],
        },
      },
      isLoading: false,
    });

    mockUseGetUserPicture.mockReturnValue({
      data: {
        data: "https://example.com/picture.jpg",
      },
      isLoading: false,
    });
  });

  it("should render skeleton when session is not available", () => {
    mockUseSession.mockReturnValue({ data: null });

    render(<Account />);

    expect(screen.getAllByTestId("skeleton")).toHaveLength(2);
  });

  it("should render skeleton when user data is loading", () => {
    mockUseGetUserById.mockReturnValue({
      data: undefined,
      isLoading: true,
      mutate: mockMutate,
    });

    render(<Account />);

    expect(screen.getAllByTestId("skeleton")).toHaveLength(2);
  });

  it("should render skeleton when user preferences are loading", () => {
    mockUseGetUserPreferencesById.mockReturnValue({
      data: undefined,
      isLoading: true,
    });

    render(<Account />);

    expect(screen.getAllByTestId("skeleton")).toHaveLength(2);
  });

  it("should render skeleton when user picture is loading", () => {
    mockUseGetUserPicture.mockReturnValue({
      data: undefined,
      isLoading: true,
    });

    render(<Account />);

    expect(screen.getAllByTestId("skeleton")).toHaveLength(2);
  });

  it("should render account page with all components when data is loaded", () => {
    render(<Account />);

    expect(screen.getByTestId("content-header")).toBeInTheDocument();
    expect(screen.getByTestId("header-title")).toHaveTextContent("My account");
    expect(screen.getByTestId("edit-user-context-provider")).toBeInTheDocument();
    expect(screen.getByTestId("edit-user")).toBeInTheDocument();
  });

  it("should render EditUser with correct props", () => {
    render(<Account />);

    expect(screen.getByTestId("user-picture-url")).toHaveTextContent("https://example.com/picture.jpg");
    expect(screen.getByTestId("is-side-panel")).toHaveTextContent("false");
    expect(screen.getByTestId("is-account")).toHaveTextContent("true");
  });

  it("should call router.back when go back button is clicked and no origin param", () => {
    render(<Account />);

    const goBackButton = screen.getByTestId("go-back-button");
    goBackButton.click();

    expect(mockHandleNavigation).toHaveBeenCalled();
    expect(mockBack).toHaveBeenCalled();
    expect(mockPush).not.toHaveBeenCalled();
  });

  it("should call router.push with origin param when go back button is clicked and origin exists", () => {
    mockSearchParams.set("origin", "/test-execution");

    render(<Account />);

    const goBackButton = screen.getByTestId("go-back-button");
    goBackButton.click();

    expect(mockHandleNavigation).toHaveBeenCalled();
    expect(mockPush).toHaveBeenCalledWith("/test-execution");
    expect(mockBack).not.toHaveBeenCalled();
  });

  it("should fetch user data with correct userId from session", () => {
    render(<Account />);

    expect(mockUseGetUserById).toHaveBeenCalledWith("user-123");
  });

  it("should fetch user preferences with correct userId from session", () => {
    render(<Account />);

    expect(mockUseGetUserPreferencesById).toHaveBeenCalledWith("user-123");
  });

  it("should fetch user picture with correct userId and size", () => {
    render(<Account />);

    expect(mockUseGetUserPicture).toHaveBeenCalledWith("user-123", "normal");
  });

  it("should render content header with go back button", () => {
    render(<Account />);

    expect(screen.getByTestId("go-back-button")).toBeInTheDocument();
    expect(screen.getByTestId("go-back-button")).toHaveTextContent("Go back");
  });

  it("should handle undefined origin search param correctly", () => {
    // origin is already null by default from beforeEach
    render(<Account />);

    const goBackButton = screen.getByTestId("go-back-button");
    goBackButton.click();

    expect(mockHandleNavigation).toHaveBeenCalled();
    expect(mockBack).toHaveBeenCalled();
  });
});
