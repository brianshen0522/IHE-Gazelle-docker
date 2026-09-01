/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, Mock } from "vitest";
import "@testing-library/jest-dom";
import ProfileSidePanel from "../ProfileSidePanel";
import { useSession } from "next-auth/react";
import { useGetValidationProfile } from "@validation-portal/hooks/SWR/useGetValidationProfile";
import { useSidePanel } from "@gazelle/gazelle-component-ui";
import { ValidationProfileResponse } from "@validation-portal/types/ValidationProfile";

// Mock dependencies
vi.mock("next-auth/react");
vi.mock("@validation-portal/hooks/SWR/useGetValidationProfile");

vi.mock("@gazelle/gazelle-component-ui", () => {
  const SidePanelComponent = ({ isOpen, className, children }: any) => (
    <div data-testid="side-panel" data-open={isOpen} className={className}>
      {children}
    </div>
  );
  SidePanelComponent.Header = ({ accessDetailsProps, onClose }: any) => (
    <div data-testid="side-panel-header">
      <button data-testid="close-button" onClick={onClose}>
        Close
      </button>
      <span data-testid="header-id">{accessDetailsProps?.id}</span>
    </div>
  );
  SidePanelComponent.Section = ({ id, title, children }: any) => (
    <div data-testid={`section-${id}`}>
      <h3>{title}</h3>
      {children}
    </div>
  );

  return {
    SidePanel: SidePanelComponent,
    Skeleton: ({ className }: any) => <div data-testid="skeleton" className={className} />,
    useSmallScreen: () => false,
    useSidePanel: vi.fn(),
  };
});

vi.mock("../ProfileDetails", () => ({
  __esModule: true,
  default: ({ profile, validationService }: any) => (
    <div data-testid="profile-details">
      <span data-testid="profile-id">{profile?.profileID}</span>
      <span data-testid="validation-service">{validationService}</span>
    </div>
  ),
}));

describe("ProfileSidePanel", () => {
  const mockUseSession = useSession as Mock;
  const mockUseGetValidationProfile = useGetValidationProfile as Mock;
  const mockUseSidePanel = useSidePanel as Mock;

  const mockSession = {
    access_token: "test-token",
    user: { name: "Test User" },
  };

  const mockProfileResponse: ValidationProfileResponse = {
    profile: {
      profileID: "profile-123",
      profileName: "Test Profile",
      version: "1.0",
    },
    validationService: "Certificate Validator",
  };

  const mockFullProfile = {
    profileID: "profile-123",
    profileName: "Test Profile",
    version: "1.0",
    domain: "Healthcare",
    standards: ["HL7", "FHIR"],
    tags: ["test", "validation"],
    coveredItems: ["item1", "item2"],
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders closed panel when not open", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: false,
      setIsOpen: vi.fn(),
      selectedRow: null,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: null,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    const panel = screen.getByTestId("side-panel");
    expect(panel).toHaveAttribute("data-open", "false");
  });

  it("renders panel with skeleton when loading", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: mockProfileResponse,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: null,
      isLoading: true,
      isError: null,
    });

    render(<ProfileSidePanel />);

    expect(screen.getByTestId("side-panel")).toHaveAttribute("data-open", "true");
    expect(screen.getByTestId("skeleton")).toBeInTheDocument();
    expect(screen.queryByTestId("profile-details")).not.toBeInTheDocument();
  });

  it("renders profile details with full profile data when loaded", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: mockProfileResponse,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: mockFullProfile,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    expect(screen.getByTestId("side-panel")).toHaveAttribute("data-open", "true");
    expect(screen.getByTestId("profile-details")).toBeInTheDocument();
    expect(screen.getByTestId("profile-id")).toHaveTextContent("profile-123");
    expect(screen.getByTestId("validation-service")).toHaveTextContent("Certificate Validator");
    expect(screen.queryByTestId("skeleton")).not.toBeInTheDocument();
  });

  it("falls back to table data when full profile not loaded yet", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: mockProfileResponse,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: null,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    expect(screen.getByTestId("profile-details")).toBeInTheDocument();
    expect(screen.getByTestId("profile-id")).toHaveTextContent("profile-123");
  });

  it("calls useGetValidationProfile with correct parameters", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: mockProfileResponse,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: mockFullProfile,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    expect(mockUseGetValidationProfile).toHaveBeenCalledWith("profile-123", "Certificate Validator", mockSession);
  });

  it("handles missing profileResponse gracefully", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: null,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: null,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    // Panel should be open but empty
    expect(screen.getByTestId("side-panel")).toHaveAttribute("data-open", "true");
    expect(screen.queryByTestId("profile-details")).not.toBeInTheDocument();
  });

  it("passes empty strings to hook when profileResponse data is missing", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: null,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: null,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    expect(mockUseGetValidationProfile).toHaveBeenCalledWith("", "", mockSession);
  });

  it("renders side panel header with correct access details", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: mockProfileResponse,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: mockFullProfile,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    expect(screen.getByTestId("side-panel-header")).toBeInTheDocument();
    expect(screen.getByTestId("header-id")).toHaveTextContent("profile-123");
  });

  it("calls setIsOpen(false) when close button is clicked", () => {
    const mockSetIsOpen = vi.fn();
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: mockSetIsOpen,
      selectedRow: mockProfileResponse,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: mockFullProfile,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    const closeButton = screen.getByTestId("close-button");
    closeButton.click();

    expect(mockSetIsOpen).toHaveBeenCalledWith(false);
  });

  it("handles null session", () => {
    mockUseSession.mockReturnValue({ data: null });
    mockUseSidePanel.mockReturnValue({
      isOpen: true,
      setIsOpen: vi.fn(),
      selectedRow: mockProfileResponse,
    });
    mockUseGetValidationProfile.mockReturnValue({
      data: null,
      isLoading: false,
      isError: null,
    });

    render(<ProfileSidePanel />);

    expect(mockUseGetValidationProfile).toHaveBeenCalledWith("profile-123", "Certificate Validator", null);
  });
});
