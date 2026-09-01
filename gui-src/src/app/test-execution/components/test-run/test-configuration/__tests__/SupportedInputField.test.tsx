/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { SupportedInputField } from "../SupportedInputField";
import { SupportedInput } from "@/app/test-execution/types/TestModel";

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Input: (props: any) => (
    <input
      data-testid={`input-${props.id}`}
      value={props.value}
      onChange={(e) => props.setValue(e.target.value)}
      placeholder={props.placeholder}
      disabled={props.disabled}
    />
  ),
  ToggleSwitch: (props: any) => (
    <button data-testid={`toggle-${props.id}`} onClick={props.onChange} disabled={props.disabled} data-checked={props.checked}>
      {props.status}
    </button>
  ),
  UploadFile: (props: any) => (
    <div data-testid={`upload-file-${props.id}`}>
      <span data-testid={`file-name-${props.id}`}>{props.fileName}</span>
      <input data-testid={`file-input-${props.id}`} type="file" onChange={(e) => props.onFileChange(e.target.files?.[0] || null)} />
    </div>
  ),
}));

describe("SupportedInputField", () => {
  const mockOnFileChange = vi.fn();
  const mockOnInputChange = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("FILE Input Type", () => {
    const fileInput: SupportedInput = {
      id: "file-1",
      type: "FILE",
      label: "Upload Document",
      required: true,
    };

    it("renders file input with label", () => {
      render(
        <SupportedInputField input={fileInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      expect(screen.getByText("Upload Document")).toBeInTheDocument();
      expect(screen.getByTestId("upload-file-file-1")).toBeInTheDocument();
    });

    it("shows required asterisk for required file inputs", () => {
      render(
        <SupportedInputField input={fileInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      expect(screen.getByText("*")).toBeInTheDocument();
    });

    it("does not show asterisk for non-required inputs", () => {
      const optionalInput = { ...fileInput, required: false };
      render(
        <SupportedInputField
          input={optionalInput}
          inputValue=""
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.queryByText("*")).not.toBeInTheDocument();
    });

    it("displays uploaded file name", () => {
      render(
        <SupportedInputField
          input={fileInput}
          inputValue=""
          uploadedFileName="uploaded.pdf"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.getByTestId("file-name-file-1")).toHaveTextContent("uploaded.pdf");
    });

    it("displays persisted file name when no uploaded file", () => {
      render(
        <SupportedInputField
          input={fileInput}
          inputValue=""
          persistedFileName="persisted.pdf"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.getByTestId("file-name-file-1")).toHaveTextContent("persisted.pdf");
    });

    it("prefers uploaded file name over persisted", () => {
      render(
        <SupportedInputField
          input={fileInput}
          inputValue=""
          uploadedFileName="uploaded.pdf"
          persistedFileName="persisted.pdf"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.getByTestId("file-name-file-1")).toHaveTextContent("uploaded.pdf");
    });

    it("calls onFileChange when file is selected", () => {
      render(
        <SupportedInputField input={fileInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      const mockFile = new File(["content"], "test.pdf", { type: "application/pdf" });
      const fileInputElement = screen.getByTestId("file-input-file-1");
      fireEvent.change(fileInputElement, { target: { files: [mockFile] } });

      expect(mockOnFileChange).toHaveBeenCalledWith(mockFile);
    });

    it("calls onFileChange with null when file is cleared", () => {
      render(
        <SupportedInputField
          input={fileInput}
          inputValue=""
          uploadedFileName="test.pdf"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const fileInputElement = screen.getByTestId("file-input-file-1");
      fireEvent.change(fileInputElement, { target: { files: null } });

      expect(mockOnFileChange).toHaveBeenCalledWith(null);
    });
  });

  describe("TEXT Input Type", () => {
    const textInput: SupportedInput = {
      id: "text-1",
      type: "TEXT",
      label: "Enter Name",
      required: true,
    };

    it("renders text input with label", () => {
      render(
        <SupportedInputField input={textInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      expect(screen.getByText("Enter Name")).toBeInTheDocument();
      expect(screen.getByTestId("input-text-1")).toBeInTheDocument();
    });

    it("displays current input value", () => {
      render(
        <SupportedInputField
          input={textInput}
          inputValue="John Doe"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const input = screen.getByTestId("input-text-1") as HTMLInputElement;
      expect(input.value).toBe("John Doe");
    });

    it("calls onInputChange when text is typed", () => {
      render(
        <SupportedInputField input={textInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      const input = screen.getByTestId("input-text-1");
      fireEvent.change(input, { target: { value: "New Value" } });

      expect(mockOnInputChange).toHaveBeenCalledWith("New Value");
    });

    it("disables text input when isRunning is true", () => {
      render(
        <SupportedInputField input={textInput} inputValue="" isRunning={true} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      const input = screen.getByTestId("input-text-1");
      expect(input).toBeDisabled();
    });

    it("enables text input when isRunning is false", () => {
      render(
        <SupportedInputField input={textInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      const input = screen.getByTestId("input-text-1");
      expect(input).not.toBeDisabled();
    });

    it("uses input id as placeholder", () => {
      render(
        <SupportedInputField input={textInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      const input = screen.getByTestId("input-text-1") as HTMLInputElement;
      expect(input.placeholder).toBe("text-1");
    });
  });

  describe("BOOLEAN Input Type", () => {
    const booleanInput: SupportedInput = {
      id: "bool-1",
      type: "BOOLEAN",
      label: "Enable Feature",
      required: false,
    };

    it("renders toggle switch with label", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="false"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.getByText("Enable Feature")).toBeInTheDocument();
      expect(screen.getByTestId("toggle-default-toggle-bool-1")).toBeInTheDocument();
    });

    it("shows 'Enabled' status when value is 'true'", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="true"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.getByText("Enabled")).toBeInTheDocument();
    });

    it("shows 'Disabled' status when value is 'false'", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="false"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.getByText("Disabled")).toBeInTheDocument();
    });

    it("toggle is checked when value is 'true'", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="true"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-1");
      expect(toggle).toHaveAttribute("data-checked", "true");
    });

    it("toggle is not checked when value is 'false'", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="false"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-1");
      expect(toggle).toHaveAttribute("data-checked", "false");
    });

    it("calls onInputChange with 'true' when toggled from 'false'", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="false"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-1");
      fireEvent.click(toggle);

      expect(mockOnInputChange).toHaveBeenCalledWith("true");
    });

    it("calls onInputChange with 'false' when toggled from 'true'", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="true"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-1");
      fireEvent.click(toggle);

      expect(mockOnInputChange).toHaveBeenCalledWith("false");
    });

    it("disables toggle when isRunning is true", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="false"
          isRunning={true}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-1");
      expect(toggle).toBeDisabled();
    });

    it("enables toggle when isRunning is false", () => {
      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="false"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-1");
      expect(toggle).not.toBeDisabled();
    });
  });

  describe("Edge Cases", () => {
    it("handles empty string input value for text input", () => {
      const textInput: SupportedInput = {
        id: "text-1",
        type: "TEXT",
        label: "Text",
        required: false,
      };

      render(
        <SupportedInputField input={textInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      const input = screen.getByTestId("input-text-1") as HTMLInputElement;
      expect(input.value).toBe("");
    });

    it("handles non-'true' value as false for boolean input", () => {
      const booleanInput: SupportedInput = {
        id: "bool-1",
        type: "BOOLEAN",
        label: "Toggle",
        required: false,
      };

      render(
        <SupportedInputField
          input={booleanInput}
          inputValue="anything"
          isRunning={false}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
        />,
      );

      expect(screen.getByText("Disabled")).toBeInTheDocument();
    });

    it("handles undefined file names gracefully", () => {
      const fileInput: SupportedInput = {
        id: "file-1",
        type: "FILE",
        label: "File",
        required: false,
      };

      render(
        <SupportedInputField input={fileInput} inputValue="" isRunning={false} onFileChange={mockOnFileChange} onInputChange={mockOnInputChange} />,
      );

      expect(screen.getByTestId("file-name-file-1")).toHaveTextContent("");
    });
  });
});
