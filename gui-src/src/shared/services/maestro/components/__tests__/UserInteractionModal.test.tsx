/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { describe, it, expect, vi, beforeEach } from "vitest";
import UserInteractionModal from "../UserInteractionModal";
import { InteractWithUser } from "@maestro/types/message/WebSocketMessage";

// Mock dependencies
vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Modal: ({ isOpen, title, children, toggleModal, id }: any) =>
    isOpen ? (
      <div data-testid="modal-root" id={id}>
        <div data-testid="modal-title">{title}</div>
        <button data-testid="modal-close" onClick={toggleModal}>
          Close
        </button>
        {children}
      </div>
    ) : null,
  Button: ({ children, onClick, id, variant }: any) => (
    <button onClick={onClick} id={id} data-variant={variant}>
      {children}
    </button>
  ),
}));

vi.mock("@shared/services/RenderSanitizedHTML", () => ({
  default: ({ untrustedHTML }: any) => <div data-testid="sanitized-html">{untrustedHTML}</div>,
}));

vi.mock("../CountdownTimer", () => ({
  default: ({ onTimeout }: any) => (
    <div data-testid="countdown-timer">
      <button data-testid="trigger-timeout" onClick={onTimeout}>
        Trigger Timeout
      </button>
    </div>
  ),
}));

describe("UserInteractionModal", () => {
  const mockInteractWithUser: InteractWithUser = {
    interactionTitle: "Test Interaction",
    message: "Please confirm this action",
    timeout: 30000,
    type: "INTERACT_WITH_USER",
  };

  const mockOnComplete = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Initial render", () => {
    it("should render the modal when component mounts", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);
      expect(screen.getByTestId("modal-root")).toBeInTheDocument();
    });

    it("should display the interaction title", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);
      expect(screen.getByText("Test Interaction")).toBeInTheDocument();
    });

    it("should display the sanitized message", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);
      expect(screen.getByTestId("sanitized-html")).toHaveTextContent("Please confirm this action");
    });

    it("should show the countdown timer before timeout", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);
      expect(screen.getByTestId("countdown-timer")).toBeInTheDocument();
    });

    it("should render continue button with primary variant", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);
      const continueButton = screen.getByRole("button", { name: "gzl.user.interface.continue" });
      expect(continueButton).toBeInTheDocument();
      expect(continueButton).toHaveAttribute("data-variant", "primary");
    });
  });

  describe("Continue button interaction", () => {
    it("should call onComplete and close modal when continue button is clicked", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      const continueButton = screen.getByRole("button", { name: "gzl.user.interface.continue" });
      fireEvent.click(continueButton);

      expect(mockOnComplete).toHaveBeenCalledTimes(1);
      expect(screen.queryByTestId("modal-root")).not.toBeInTheDocument();
    });

    it("should work without onComplete callback", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} />);

      const continueButton = screen.getByRole("button", { name: "gzl.user.interface.continue" });
      expect(() => fireEvent.click(continueButton)).not.toThrow();
      expect(screen.queryByTestId("modal-root")).not.toBeInTheDocument();
    });
  });

  describe("Modal close behavior before timeout", () => {
    it("should not close modal when X button is clicked before timeout", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      const closeButton = screen.getByTestId("modal-close");
      fireEvent.click(closeButton);

      // Modal should still be open
      expect(screen.getByTestId("modal-root")).toBeInTheDocument();
      expect(mockOnComplete).not.toHaveBeenCalled();
    });
  });

  describe("Timeout behavior", () => {
    it("should display timeout message when timeout occurs", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);

      expect(screen.getByText("gzl.user.interface.execution_timed_out")).toBeInTheDocument();
      expect(screen.getByText("gzl.user.interface.please_click_continue_or_rerun")).toBeInTheDocument();
    });

    it("should hide countdown timer after timeout", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);

      expect(screen.queryByTestId("countdown-timer")).not.toBeInTheDocument();
    });

    it("should change continue button variant to danger after timeout", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);

      const continueButton = screen.getByRole("button", { name: "gzl.user.interface.continue" });
      expect(continueButton).toHaveAttribute("data-variant", "danger");
    });

    it("should allow closing modal via X button after timeout", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      // Trigger timeout
      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);

      // Now close button should work
      const closeButton = screen.getByTestId("modal-close");
      fireEvent.click(closeButton);

      expect(screen.queryByTestId("modal-root")).not.toBeInTheDocument();
      expect(mockOnComplete).toHaveBeenCalledTimes(1);
    });

    it("should still allow continue button after timeout", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      // Trigger timeout
      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);

      // Continue button should still work
      const continueButton = screen.getByRole("button", { name: "gzl.user.interface.continue" });
      fireEvent.click(continueButton);

      expect(mockOnComplete).toHaveBeenCalledTimes(1);
      expect(screen.queryByTestId("modal-root")).not.toBeInTheDocument();
    });
  });

  describe("State reset on new interaction", () => {
    it("should reopen modal when interactWithUser prop changes", async () => {
      const { rerender } = render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      // Close the modal
      const continueButton = screen.getByRole("button", { name: "gzl.user.interface.continue" });
      fireEvent.click(continueButton);
      expect(screen.queryByTestId("modal-root")).not.toBeInTheDocument();

      // New interaction arrives
      const newInteraction: InteractWithUser = {
        interactionTitle: "New Interaction",
        message: "New message",
        timeout: 60000,
        type: "INTERACT_WITH_USER",
      };

      rerender(<UserInteractionModal interactWithUser={newInteraction} onComplete={mockOnComplete} />);

      // Modal should be open again
      await waitFor(() => {
        expect(screen.getByTestId("modal-root")).toBeInTheDocument();
      });
      expect(screen.getByText("New Interaction")).toBeInTheDocument();
      expect(screen.getByTestId("countdown-timer")).toBeInTheDocument();
    });

    it("should reset timeout state when interactWithUser prop changes", async () => {
      const { rerender } = render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      // Trigger timeout
      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);
      expect(screen.getByText("gzl.user.interface.execution_timed_out")).toBeInTheDocument();

      // New interaction arrives
      const newInteraction: InteractWithUser = {
        interactionTitle: "Fresh Interaction",
        message: "Fresh message",
        timeout: 45000,
        type: "INTERACT_WITH_USER",
      };

      rerender(<UserInteractionModal interactWithUser={newInteraction} onComplete={mockOnComplete} />);

      // Should show the new title, not the timeout message
      await waitFor(() => {
        expect(screen.getByText("Fresh Interaction")).toBeInTheDocument();
      });
      expect(screen.queryByText("gzl.user.interface.execution_timed_out")).not.toBeInTheDocument();
      expect(screen.getByTestId("countdown-timer")).toBeInTheDocument();
    });

    it("should not allow closing via X button after state reset", async () => {
      const { rerender } = render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      // Trigger timeout (which allows closing)
      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);

      // New interaction arrives (resets state)
      const newInteraction: InteractWithUser = {
        interactionTitle: "Reset Interaction",
        message: "Reset message",
        timeout: 20000,
        type: "INTERACT_WITH_USER",
      };

      rerender(<UserInteractionModal interactWithUser={newInteraction} onComplete={mockOnComplete} />);

      await waitFor(() => {
        expect(screen.getByText("Reset Interaction")).toBeInTheDocument();
      });

      // Clear the mock to track new calls
      mockOnComplete.mockClear();

      // Try to close - should not work since state is reset
      const closeButton = screen.getByTestId("modal-close");
      fireEvent.click(closeButton);

      expect(screen.getByTestId("modal-root")).toBeInTheDocument();
      expect(mockOnComplete).not.toHaveBeenCalled();
    });
  });

  describe("CSS classes", () => {
    it("should apply red text class to title when timed out", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      const triggerTimeoutButton = screen.getByTestId("trigger-timeout");
      fireEvent.click(triggerTimeoutButton);

      const timeoutTitle = screen.getByText("gzl.user.interface.execution_timed_out");
      expect(timeoutTitle.className).toContain("text-red");
    });

    it("should not apply red text class to title before timeout", () => {
      render(<UserInteractionModal interactWithUser={mockInteractWithUser} onComplete={mockOnComplete} />);

      const title = screen.getByText("Test Interaction");
      expect(title.className).not.toContain("text-red");
    });
  });
});
