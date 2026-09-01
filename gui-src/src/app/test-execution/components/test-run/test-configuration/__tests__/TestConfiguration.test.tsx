import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, Mock } from "vitest";
import TestConfiguration from "../TestConfiguration";
import { SupportedInput } from "@/app/test-execution/types/TestModel";
import { UploadedInput } from "@/app/test-execution/types/TestRun";
import { TestRunExecution } from "@/app/test-execution/types/TestRunExecution";
import * as fileHandling from "@/shared/utils/fileHandling/fileHandling";

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Button: ({ id, onClick, disabled, children }: { id: string; onClick?: () => void; disabled?: boolean; children: React.ReactNode }) => (
    <button data-testid={id} onClick={onClick} disabled={disabled}>
      {children}
    </button>
  ),
  CollapsableCard: ({ title, children }: { title: string; children: React.ReactNode }) => (
    <div data-testid="collapsable-card">
      <div>{title}</div>
      {children}
    </div>
  ),
  Input: ({ id, value, setValue, placeholder }: { id: string; value: string; setValue: (v: string) => void; placeholder: string }) => (
    <input data-testid={`input-${id}`} value={value} onChange={(e) => setValue(e.target.value)} placeholder={placeholder} />
  ),
  NoticeBanner: ({ children }: { children: React.ReactNode }) => <div data-testid="notice-banner">{children}</div>,
  ToggleSwitch: ({
    id,
    status,
    checked,
    disabled,
    onChange,
  }: {
    id: string;
    status: string;
    checked: boolean;
    disabled?: boolean;
    onChange: () => void;
  }) => (
    <button data-testid={`toggle-${id}`} onClick={onChange} disabled={disabled} data-checked={checked}>
      {status}
    </button>
  ),
  UploadFile: ({ id, fileName, onFileChange }: { id: string; fileName: string; onFileChange: (f: File | null) => void }) => (
    <div data-testid={`upload-file-${id}`}>
      <span data-testid={`file-name-${id}`}>{fileName}</span>
      <input data-testid={`file-input-${id}`} type="file" onChange={(e) => onFileChange(e.target.files?.[0] || null)} />
    </div>
  ),
}));

vi.mock("lucide-react", () => ({
  Info: () => <svg data-testid="info-icon" />,
}));

vi.mock("@/shared/utils/fileHandling/fileHandling", () => ({
  readFileWithContent: vi.fn(),
  handleFileError: vi.fn(),
}));

describe("TestConfiguration", () => {
  const mockOnFileChange = vi.fn();
  const mockOnInputChange = vi.fn();
  const mockOnRun = vi.fn();

  const createMockExecution = (inputs: unknown[] = []): TestRunExecution =>
    ({
      id: "exec-1",
      testId: "test-1",
      inputs,
      testSessionId: "session-1",
      testSuiteId: "suite-1",
      testSpecification: "{}",
      testName: "Test",
      status: "NOT_STARTED",
      startedAt: "2026-01-01",
      executedBy: "user-1",
    }) as TestRunExecution;

  const supportedInputs: SupportedInput[] = [
    { id: "file-input", type: "FILE", label: "File Input", required: true },
    { id: "text-input", type: "TEXT", label: "Text Input", required: false },
    { id: "bool-input", type: "BOOLEAN", label: "Boolean Input", required: false },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Rendering", () => {
    it("renders the component with title", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("Test run configuration")).toBeInTheDocument();
    });

    it("renders notice banner when required inputs are not filled", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={false}
          isRunning={false}
        />,
      );

      expect(screen.getByTestId("notice-banner")).toBeInTheDocument();
      expect(
        screen.getByText(
          "To run this test, first provide the inputs as requested below. The inputs marked with an asterisk (*) are mandatory to run the test.",
        ),
      ).toBeInTheDocument();
    });

    it("does not render notice banner when required inputs are filled", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.queryByTestId("notice-banner")).not.toBeInTheDocument();
    });

    it("renders supported inputs correctly", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("File Input")).toBeInTheDocument();
      expect(screen.getByText("Text Input")).toBeInTheDocument();
      expect(screen.getByText("Boolean Input")).toBeInTheDocument();
      expect(screen.getByTestId("upload-file-file-input")).toBeInTheDocument();
      expect(screen.getByTestId("input-text-input")).toBeInTheDocument();
      expect(screen.getByTestId("toggle-default-toggle-bool-input")).toBeInTheDocument();
    });

    it("renders no input message when no supported inputs", () => {
      const execution = createMockExecution();
      render(
        <TestConfiguration
          currentExecution={execution}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("No input expected for this test")).toBeInTheDocument();
    });

    it("marks required inputs with asterisk", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText(/File Input/i)).toBeInTheDocument();
      expect(screen.getByText("*")).toBeInTheDocument();
    });
  });

  describe("File Handling", () => {
    it("handles file upload successfully", async () => {
      const mockFile = new File(["content"], "test.txt", { type: "text/plain" });
      const mockReadFileWithContent = fileHandling.readFileWithContent as Mock;
      mockReadFileWithContent.mockResolvedValue({
        file: mockFile,
        content: "base64content",
      });

      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const fileInput = screen.getByTestId("file-input-file-input");
      fireEvent.change(fileInput, { target: { files: [mockFile] } });

      await waitFor(() => {
        expect(mockReadFileWithContent).toHaveBeenCalledWith(mockFile);
        expect(mockOnFileChange).toHaveBeenCalledWith("file-input", "test.txt", "base64content");
      });
    });

    it("handles file upload error", async () => {
      const mockFile = new File(["content"], "test.txt", { type: "text/plain" });
      const mockReadFileWithContent = fileHandling.readFileWithContent as Mock;
      const mockHandleFileError = fileHandling.handleFileError as Mock;
      mockReadFileWithContent.mockRejectedValue(new Error("Read error"));

      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const fileInput = screen.getByTestId("file-input-file-input");
      fireEvent.change(fileInput, { target: { files: [mockFile] } });

      await waitFor(() => {
        expect(mockHandleFileError).toHaveBeenCalled();
        expect(mockOnFileChange).toHaveBeenCalledWith("file-input", "test.txt", null);
      });
    });

    it("handles file removal", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const fileInput = screen.getByTestId("file-input-file-input");
      fireEvent.change(fileInput, { target: { files: null } });

      expect(mockOnFileChange).toHaveBeenCalledWith("file-input", "", "");
    });
  });

  describe("Text Input Handling", () => {
    it("handles text input changes", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const textInput = screen.getByTestId("input-text-input");
      fireEvent.change(textInput, { target: { value: "new value" } });

      expect(mockOnInputChange).toHaveBeenCalledWith("text-input", "new value");
    });

    it("displays current session text input value", () => {
      const uploadedInputs: UploadedInput[] = [{ id: "text-input", value: "session value" }];

      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const textInput = screen.getByTestId("input-text-input") as HTMLInputElement;
      expect(textInput.value).toBe("session value");
    });
  });

  describe("Boolean Input Handling", () => {
    it("renders boolean toggle switch", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("Boolean Input")).toBeInTheDocument();
      expect(screen.getByTestId("toggle-default-toggle-bool-input")).toBeInTheDocument();
    });

    it("handles boolean toggle changes from false to true", () => {
      const uploadedInputs: UploadedInput[] = [{ id: "bool-input", value: "false" }];

      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-input");
      fireEvent.click(toggle);

      expect(mockOnInputChange).toHaveBeenCalledWith("bool-input", "true");
    });

    it("handles boolean toggle changes from true to false", () => {
      const uploadedInputs: UploadedInput[] = [{ id: "bool-input", value: "true" }];

      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const toggle = screen.getByTestId("toggle-default-toggle-bool-input");
      fireEvent.click(toggle);

      expect(mockOnInputChange).toHaveBeenCalledWith("bool-input", "false");
    });

    it("displays enabled status when value is 'true'", () => {
      const uploadedInputs: UploadedInput[] = [{ id: "bool-input", value: "true" }];

      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("Enabled")).toBeInTheDocument();
    });

    it("displays disabled status when value is 'false'", () => {
      const uploadedInputs: UploadedInput[] = [{ id: "bool-input", value: "false" }];

      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("Disabled")).toBeInTheDocument();
    });
  });

  describe("Persisted Data", () => {
    it("displays persisted file name from execution", () => {
      const execution = createMockExecution([{ type: "BYTE_ARRAY", name: "File Input", value: "base64data" }]);

      render(
        <TestConfiguration
          currentExecution={execution}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByTestId("file-name-file-input")).toHaveTextContent("File Input");
    });

    it("displays persisted text value from execution", () => {
      const execution = createMockExecution([{ type: "STRING", name: "text-input", value: "persisted value" }]);

      render(
        <TestConfiguration
          currentExecution={execution}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const textInput = screen.getByTestId("input-text-input") as HTMLInputElement;
      expect(textInput.value).toBe("persisted value");
    });

    it("prefers session data over persisted data for text inputs", () => {
      const execution = createMockExecution([{ type: "STRING", name: "text-input", value: "persisted value" }]);
      const uploadedInputs: UploadedInput[] = [{ id: "text-input", value: "session value" }];

      render(
        <TestConfiguration
          currentExecution={execution}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const textInput = screen.getByTestId("input-text-input") as HTMLInputElement;
      expect(textInput.value).toBe("session value");
    });

    it("allows clearing text input even when persisted value exists", () => {
      const execution = createMockExecution([{ type: "STRING", name: "text-input", value: "persisted value" }]);
      const uploadedInputs: UploadedInput[] = [{ id: "text-input", value: "" }];

      render(
        <TestConfiguration
          currentExecution={execution}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const textInput = screen.getByTestId("input-text-input") as HTMLInputElement;
      expect(textInput.value).toBe("");
    });

    it("prefers uploaded file name over persisted file name", async () => {
      const execution = createMockExecution([{ type: "BYTE_ARRAY", name: "File Input", value: "old-file-data" }]);
      const mockFile = new File(["new content"], "new-file.txt", { type: "text/plain" });
      const mockReadFileWithContent = fileHandling.readFileWithContent as Mock;
      mockReadFileWithContent.mockResolvedValue({
        file: mockFile,
        content: "base64newcontent",
      });

      render(
        <TestConfiguration
          currentExecution={execution}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      // Initially shows persisted file name
      expect(screen.getByTestId("file-name-file-input")).toHaveTextContent("File Input");

      // Upload new file
      const fileInput = screen.getByTestId("file-input-file-input");
      fireEvent.change(fileInput, { target: { files: [mockFile] } });

      // Should now show new file name
      await waitFor(() => {
        expect(screen.getByTestId("file-name-file-input")).toHaveTextContent("new-file.txt");
      });
    });

    it("allows clearing file even when persisted file exists", () => {
      const execution = createMockExecution([{ type: "BYTE_ARRAY", name: "File Input", value: "persisted-file-data" }]);

      render(
        <TestConfiguration
          currentExecution={execution}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      // Initially shows persisted file name
      expect(screen.getByTestId("file-name-file-input")).toHaveTextContent("File Input");

      // Clear the file
      const fileInput = screen.getByTestId("file-input-file-input");
      fireEvent.change(fileInput, { target: { files: null } });

      // Should clear the file name
      expect(screen.getByTestId("file-name-file-input")).toHaveTextContent("");
      expect(mockOnFileChange).toHaveBeenCalledWith("file-input", "", "");
    });

    it("shows no input message when no supported inputs even with persisted data", () => {
      const execution = createMockExecution([{ type: "BYTE_ARRAY", name: "uploaded-file.txt", value: "base64data" }]);

      render(
        <TestConfiguration
          currentExecution={execution}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("No input expected for this test")).toBeInTheDocument();
    });

    it("handles execution with no inputs gracefully", () => {
      const execution = createMockExecution([]);

      expect(() =>
        render(
          <TestConfiguration
            currentExecution={execution}
            supportedInputs={supportedInputs}
            onFileChange={mockOnFileChange}
            onInputChange={mockOnInputChange}
            uploadedInputs={[]}
            areRequiredInputsFilled={true}
            isRunning={false}
          />,
        ),
      ).not.toThrow();
    });
  });

  describe("Edge Cases", () => {
    it("handles undefined supportedInputs", () => {
      expect(() =>
        render(
          <TestConfiguration
            currentExecution={createMockExecution()}
            onFileChange={mockOnFileChange}
            onInputChange={mockOnInputChange}
            uploadedInputs={[]}
            areRequiredInputsFilled={true}
            isRunning={false}
          />,
        ),
      ).not.toThrow();
    });

    it("handles empty supportedInputs array", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={[]}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      expect(screen.getByText("No input expected for this test")).toBeInTheDocument();
    });

    it("returns empty string for non-existent input value", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          isRunning={false}
        />,
      );

      const textInput = screen.getByTestId("input-text-input") as HTMLInputElement;
      expect(textInput.value).toBe("");
    });
  });

  describe("Run Button Behavior", () => {
    it("should call onRun when run button is clicked", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          onRun={mockOnRun}
          isRunning={false}
        />,
      );

      const runButton = screen.getByTestId("test-run");
      fireEvent.click(runButton);

      expect(mockOnRun).toHaveBeenCalledTimes(1);
    });

    it("should disable run button when required inputs are not filled", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={false}
          onRun={mockOnRun}
          isRunning={false}
        />,
      );

      const runButton = screen.getByTestId("test-run");
      expect(runButton).toBeDisabled();
    });

    it("should disable run button when isRunning is true", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          onRun={mockOnRun}
          isRunning={true}
        />,
      );

      const runButton = screen.getByTestId("test-run");
      expect(runButton).toBeDisabled();
    });

    it("should enable run button when not running and required inputs are filled", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          onRun={mockOnRun}
          isRunning={false}
        />,
      );

      const runButton = screen.getByTestId("test-run");
      expect(runButton).not.toBeDisabled();
    });

    it("should display 'Running' text when isRunning is true", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          onRun={mockOnRun}
          isRunning={true}
        />,
      );

      expect(screen.getByText("Running")).toBeInTheDocument();
    });

    it("should display 'Run' text when isRunning is false", () => {
      render(
        <TestConfiguration
          currentExecution={createMockExecution()}
          supportedInputs={supportedInputs}
          onFileChange={mockOnFileChange}
          onInputChange={mockOnInputChange}
          uploadedInputs={[]}
          areRequiredInputsFilled={true}
          onRun={mockOnRun}
          isRunning={false}
        />,
      );

      expect(screen.getByText("Run")).toBeInTheDocument();
    });
  });
});
