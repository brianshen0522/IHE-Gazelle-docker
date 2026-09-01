/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import OrganizationArchiveModal from "../OrganizationArchiveModal";

// Mock SWR
const mockMutate = vi.fn();
vi.mock("swr", () => ({
  default: vi.fn(),
  mutate: (...args: any[]) => mockMutate(...args),
}));

// Mock actions
const mockArchiveOrganizationAction = vi.fn();
vi.mock("../../actions", () => ({
  archiveOrganizationAction: (id: string) => mockArchiveOrganizationAction(id),
}));

// Mock ConfirmModal
vi.mock("../../../ConfirmModal", () => ({
  default: ({ title, isOpen, onCancel, onContinue, toggleModal, textOnContinue, disableConfirm, confirmVariant, children }: any) => (
    <div data-testid="confirm-modal" data-open={String(isOpen)}>
      <h2>{title}</h2>
      <div data-testid="modal-content">{children}</div>
      <button data-testid="cancel-button" onClick={onCancel}>
        Cancel
      </button>
      <button data-testid="confirm-button" onClick={onContinue} disabled={disableConfirm}>
        {textOnContinue}
      </button>
      <button data-testid="toggle-button" onClick={toggleModal}>
        Toggle
      </button>
      <div data-testid="confirm-variant">{confirmVariant}</div>
    </div>
  ),
}));

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Input: ({ id, value, setValue, placeholder }: any) => (
    <input data-testid={id} value={value} onChange={(e) => setValue(e.target.value)} placeholder={placeholder} />
  ),
}));

describe("OrganizationArchiveModal", () => {
  const mockOrganization = {
    id: "org-123",
    name: "Test Organization",
  };

  const mockToggleModal = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Rendering", () => {
    it("renders the modal when isOpen is true", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      expect(screen.getByTestId("confirm-modal")).toHaveAttribute("data-open", "true");
    });

    it("renders with correct title", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      expect(screen.getByText("Confirm archive operation")).toBeInTheDocument();
    });

    it("renders confirmation input field", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      expect(screen.getByTestId("archive-confirm-input")).toBeInTheDocument();
      expect(screen.getByPlaceholderText("ARCHIVE")).toBeInTheDocument();
    });

    it("renders organization name in confirm button text", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      expect(screen.getByTestId("confirm-button")).toHaveTextContent("Test Organization");
    });

    it("uses danger variant for confirm button", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      expect(screen.getByTestId("confirm-variant")).toHaveTextContent("danger");
    });

    it("renders warning messages", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const content = screen.getByTestId("modal-content");
      expect(content).toHaveTextContent("This action is irreversible");
    });
  });

  describe("Input validation", () => {
    it("disables confirm button when input is empty", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      expect(screen.getByTestId("confirm-button")).toBeDisabled();
    });

    it("disables confirm button when input does not match ARCHIVE", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");
      fireEvent.change(input, { target: { value: "archive" } });

      expect(screen.getByTestId("confirm-button")).toBeDisabled();
    });

    it("enables confirm button when input matches ARCHIVE exactly", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");
      fireEvent.change(input, { target: { value: "ARCHIVE" } });

      expect(screen.getByTestId("confirm-button")).not.toBeDisabled();
    });

    it("is case-sensitive for ARCHIVE validation", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");

      fireEvent.change(input, { target: { value: "Archive" } });
      expect(screen.getByTestId("confirm-button")).toBeDisabled();

      fireEvent.change(input, { target: { value: "ARCHIVE" } });
      expect(screen.getByTestId("confirm-button")).not.toBeDisabled();
    });
  });

  describe("Modal interactions", () => {
    it("calls toggleModal when cancel button is clicked", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      fireEvent.click(screen.getByTestId("cancel-button"));
      expect(mockToggleModal).toHaveBeenCalled();
    });

    it("resets input when cancel button is clicked", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input") as HTMLInputElement;
      fireEvent.change(input, { target: { value: "ARCHIVE" } });
      expect(input.value).toBe("ARCHIVE");

      fireEvent.click(screen.getByTestId("cancel-button"));
      expect(input.value).toBe("");
    });

    it("calls toggleModal when toggle button is clicked", () => {
      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      fireEvent.click(screen.getByTestId("toggle-button"));
      expect(mockToggleModal).toHaveBeenCalled();
    });
  });

  describe("Archive action", () => {
    it("calls archiveOrganizationAction when confirm is clicked with valid input", async () => {
      mockArchiveOrganizationAction.mockResolvedValue({
        success: true,
        message: "Organization archived successfully",
      });

      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");
      fireEvent.change(input, { target: { value: "ARCHIVE" } });

      fireEvent.click(screen.getByTestId("confirm-button"));

      await waitFor(() => {
        expect(mockArchiveOrganizationAction).toHaveBeenCalledWith("org-123");
      });
    });

    it("invalidates SWR cache on successful archive", async () => {
      mockArchiveOrganizationAction.mockResolvedValue({
        success: true,
        message: "Organization archived successfully",
      });

      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");
      fireEvent.change(input, { target: { value: "ARCHIVE" } });

      fireEvent.click(screen.getByTestId("confirm-button"));

      await waitFor(() => {
        expect(mockMutate).toHaveBeenCalled();
      });
    });

    it("closes modal on successful archive", async () => {
      mockArchiveOrganizationAction.mockResolvedValue({
        success: true,
        message: "Organization archived successfully",
      });

      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");
      fireEvent.change(input, { target: { value: "ARCHIVE" } });

      fireEvent.click(screen.getByTestId("confirm-button"));

      await waitFor(() => {
        expect(mockToggleModal).toHaveBeenCalled();
      });
    });

    it("resets input on successful archive", async () => {
      mockArchiveOrganizationAction.mockResolvedValue({
        success: true,
        message: "Organization archived successfully",
      });

      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input") as HTMLInputElement;
      fireEvent.change(input, { target: { value: "ARCHIVE" } });

      fireEvent.click(screen.getByTestId("confirm-button"));

      await waitFor(() => {
        expect(input.value).toBe("");
      });
    });

    it("does not close modal on failed archive", async () => {
      mockArchiveOrganizationAction.mockResolvedValue({
        success: false,
        message: "Failed to archive organization",
      });

      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");
      fireEvent.change(input, { target: { value: "ARCHIVE" } });

      fireEvent.click(screen.getByTestId("confirm-button"));

      await waitFor(() => {
        expect(mockArchiveOrganizationAction).toHaveBeenCalled();
      });

      expect(mockToggleModal).not.toHaveBeenCalled();
    });

    it("does not invalidate cache on failed archive", async () => {
      mockArchiveOrganizationAction.mockResolvedValue({
        success: false,
        message: "Failed to archive organization",
      });

      render(<OrganizationArchiveModal organization={mockOrganization} isOpen={true} toggleModal={mockToggleModal} />);

      const input = screen.getByTestId("archive-confirm-input");
      fireEvent.change(input, { target: { value: "ARCHIVE" } });

      fireEvent.click(screen.getByTestId("confirm-button"));

      await waitFor(() => {
        expect(mockArchiveOrganizationAction).toHaveBeenCalled();
      });

      expect(mockMutate).not.toHaveBeenCalled();
    });
  });
});
