import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TestSessionContent from "../TestSessionContent";

// Mock next-auth
const mockUseSession = vi.fn();
vi.mock("next-auth/react", () => ({
  useSession: () => mockUseSession(),
}));

// Mock TestSessionContext
const mockSetTestSessionId = vi.fn();
const mockUseTestSession = vi.fn();
vi.mock("@test-execution/context/TestSessionContext", () => ({
  useTestSession: () => mockUseTestSession(),
}));

// Mock TestSuiteContent
vi.mock("@test-execution/components/test-suite/TestSuiteContent", () => ({
  default: vi.fn(() => React.createElement("div", { "data-testid": "test-suite-content" }, "TestSuiteContent")),
}));

describe("TestSessionContent", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders TestSuiteContent", () => {
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    render(<TestSessionContent />);

    expect(screen.getByTestId("test-suite-content")).toBeInTheDocument();
  });

  it("sets testSessionId when session is available and testSessionId is null", async () => {
    mockUseSession.mockReturnValue({
      data: {
        user: {
          organization: "org-123",
        },
      },
      status: "authenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    render(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).toHaveBeenCalledWith("adhoc-org-123");
      expect(mockSetTestSessionId).toHaveBeenCalledTimes(1);
    });
  });

  it("does not set testSessionId when testSessionId already exists", async () => {
    mockUseSession.mockReturnValue({
      data: {
        user: {
          organization: "org-123",
        },
      },
      status: "authenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: "existing-session",
      setTestSessionId: mockSetTestSessionId,
    });

    render(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).not.toHaveBeenCalled();
    });
  });

  it("does not set testSessionId when organization is not available", async () => {
    mockUseSession.mockReturnValue({
      data: {
        user: {},
      },
      status: "authenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    render(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).not.toHaveBeenCalled();
    });
  });

  it("does not set testSessionId when session is null", async () => {
    mockUseSession.mockReturnValue({
      data: null,
      status: "unauthenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    render(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).not.toHaveBeenCalled();
    });
  });

  it("constructs adhoc session id with organization", async () => {
    mockUseSession.mockReturnValue({
      data: {
        user: {
          organization: "my-org",
        },
      },
      status: "authenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    render(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).toHaveBeenCalledWith("adhoc-my-org");
    });
  });

  it("reacts to changes in session organization", async () => {
    const { rerender } = render(<TestSessionContent />);

    mockUseSession.mockReturnValue({
      data: {
        user: {
          organization: "org-1",
        },
      },
      status: "authenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    rerender(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).toHaveBeenCalledWith("adhoc-org-1");
    });

    mockSetTestSessionId.mockClear();

    mockUseSession.mockReturnValue({
      data: {
        user: {
          organization: "org-2",
        },
      },
      status: "authenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    rerender(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).toHaveBeenCalledWith("adhoc-org-2");
    });
  });

  it("only sets testSessionId once when conditions are met", async () => {
    mockUseSession.mockReturnValue({
      data: {
        user: {
          organization: "org-123",
        },
      },
      status: "authenticated",
    });

    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      setTestSessionId: mockSetTestSessionId,
    });

    const { rerender } = render(<TestSessionContent />);

    await waitFor(() => {
      expect(mockSetTestSessionId).toHaveBeenCalledTimes(1);
    });

    // Re-render with same props
    rerender(<TestSessionContent />);

    // Should still only be called once
    expect(mockSetTestSessionId).toHaveBeenCalledTimes(1);
  });
});
