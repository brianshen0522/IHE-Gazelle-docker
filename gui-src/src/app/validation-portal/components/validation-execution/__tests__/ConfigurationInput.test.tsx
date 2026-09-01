import React from "react";
import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";
import "@testing-library/jest-dom";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ConfigurationInput from "../ConfigurationInput";
import { SupportedInput } from "../../../types/ValidationProfile";

// Mock dependencies
vi.mock("@gazelle/gazelle-component-ui", () => ({
  UploadFile: ({ id, fileName, onFileChange }: { id: string; fileName?: string; onFileChange: (file: File | null) => void }) => (
    <div data-testid={`upload-file-${id}`}>
      <span>{fileName ?? "No file"}</span>
      <input
        type="file"
        data-testid={`file-input-${id}`}
        onChange={(e) => {
          const file = e.target.files?.[0] || null;
          onFileChange(file);
        }}
      />
    </div>
  ),
  CollapsableSubCard: ({ title, children, className }: { title: React.ReactNode; children: React.ReactNode; className?: string }) => (
    <div data-testid="collapsable-card" className={className}>
      <div data-testid="card-title">{title}</div>
      <div data-testid="card-content">{children}</div>
    </div>
  ),
  Skeleton: ({ className }: { className?: string }) => (
    <div data-testid="skeleton" className={className}>
      Loading...
    </div>
  ),
}));

// Mock renderer components
vi.mock("@shared/components/renderers/json/JsonRenderer", () => ({
  JsonRenderer: ({ base64Data }: { base64Data: string }) => {
    const content = Buffer.from(base64Data, "base64").toString("utf-8");
    return <div data-testid="json-renderer">{content}</div>;
  },
}));

vi.mock("@shared/components/renderers/xml/XmlRenderer", () => ({
  XmlRenderer: ({ base64Data }: { base64Data: string }) => {
    const content = Buffer.from(base64Data, "base64").toString("utf-8");
    return <div data-testid="xml-renderer">{content}</div>;
  },
}));

vi.mock("@shared/components/renderers/raw/RawRenderer", () => ({
  RawRenderer: ({ base64Data }: { base64Data: string }) => {
    const content = Buffer.from(base64Data, "base64").toString("utf-8");
    return <div data-testid="raw-renderer">{content}</div>;
  },
}));

describe("ConfigurationInput", () => {
  const mockSetFileData = vi.fn();

  // Helper to create a mock file for testing
  const createMockFile = (content: string, name: string, type: string = "text/xml"): File => {
    const file = new File([content], name, { type });
    // Add .text() method for compatibility with file.text()
    Object.defineProperty(file, "text", {
      value: () => Promise.resolve(content),
      writable: true,
      configurable: true,
    });
    return file;
  };

  const defaultProps = {
    inputs: [
      { id: "input1", label: "Test Input 1", required: true },
      { id: "input2", label: "Test Input 2", required: false },
    ] as SupportedInput[],
    fileData: new Map<string, { file: File; content: string }>(),
    setFileData: mockSetFileData,
    reviewEnabled: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders all inputs with labels", () => {
    render(<ConfigurationInput {...defaultProps} />);

    expect(screen.getByText("Test Input 1")).toBeInTheDocument();
    expect(screen.getByText("Test Input 2")).toBeInTheDocument();
  });

  it("displays asterisk for required fields", () => {
    render(<ConfigurationInput {...defaultProps} />);

    const titles = screen.getAllByTestId("card-title");
    expect(titles[0]).toHaveTextContent("*");
    expect(titles[1]).not.toHaveTextContent("*");
  });

  it("displays uploaded file name in title", () => {
    const file = createMockFile("content", "test.xml");
    const fileData = new Map([["input1", { file, content: "content" }]]);

    render(<ConfigurationInput {...defaultProps} fileData={fileData} />);

    const titles = screen.getAllByTestId("card-title");
    expect(titles[0]).toHaveTextContent("test.xml");
  });

  it("calls setFileData when file is selected", async () => {
    render(<ConfigurationInput {...defaultProps} />);

    const fileContent = "test file content";
    const file = createMockFile(fileContent, "test.xml");

    const fileInput = screen.getByTestId("file-input-input1");
    await act(async () => {
      fireEvent.change(fileInput, { target: { files: [file] } });
    });

    await waitFor(() => {
      expect(mockSetFileData).toHaveBeenCalled();
      const callArg = mockSetFileData.mock.calls[0][0];
      expect(callArg.get("input1")).toEqual({
        file,
        content: Buffer.from(fileContent, "utf-8").toString("base64"),
      });
    });
  });

  it("removes file when null is passed", async () => {
    const file = createMockFile("content", "test.xml");
    const fileData = new Map([["input1", { file, content: "content" }]]);

    render(<ConfigurationInput {...defaultProps} fileData={fileData} />);

    const fileInput = screen.getByTestId("file-input-input1");
    fireEvent.change(fileInput, { target: { files: [] } });

    await waitFor(() => {
      expect(mockSetFileData).toHaveBeenCalled();
      const callArg = mockSetFileData.mock.calls[0][0];
      expect(callArg.has("input1")).toBe(false);
    });
  });

  it("displays file preview when reviewEnabled is true and file is uploaded", () => {
    const fileContent = "This is the file content";
    const file = createMockFile(fileContent, "test.xml");
    const fileData = new Map([["input1", { file, content: Buffer.from(fileContent, "utf-8").toString("base64") }]]);

    render(<ConfigurationInput {...defaultProps} fileData={fileData} reviewEnabled={true} />);

    expect(screen.getByText(fileContent)).toBeInTheDocument();
  });

  it("does not display file preview when reviewEnabled is false", () => {
    const fileContent = "This is the file content";
    const file = createMockFile(fileContent, "test.xml");
    const fileData = new Map([["input1", { file, content: Buffer.from(fileContent, "utf-8").toString("base64") }]]);

    render(<ConfigurationInput {...defaultProps} fileData={fileData} reviewEnabled={false} />);

    expect(screen.queryByText(fileContent)).not.toBeInTheDocument();
  });

  it("renders multiple inputs correctly", () => {
    const multipleInputs = [
      { id: "input1", label: "Input 1", required: true },
      { id: "input2", label: "Input 2", required: true },
      { id: "input3", label: "Input 3", required: false },
    ] as SupportedInput[];

    render(<ConfigurationInput {...defaultProps} inputs={multipleInputs} />);

    expect(screen.getAllByTestId("collapsable-card")).toHaveLength(3);
    expect(screen.getByText("Input 1")).toBeInTheDocument();
    expect(screen.getByText("Input 2")).toBeInTheDocument();
    expect(screen.getByText("Input 3")).toBeInTheDocument();
  });

  it("passes className to CollapsableSubCard", () => {
    render(<ConfigurationInput {...defaultProps} />);

    const cards = screen.getAllByTestId("collapsable-card");
    cards.forEach((card) => {
      expect(card).toHaveClass("space-y-1");
    });
  });

  it("handles file read error gracefully", async () => {
    const consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    // Create a file that will fail to read
    const file = new File(["content"], "test.xml", { type: "text/xml" });
    Object.defineProperty(file, "arrayBuffer", {
      value: vi.fn().mockRejectedValue(new Error("Read error")),
      configurable: true,
      writable: true,
    });

    render(<ConfigurationInput {...defaultProps} />);

    const fileInput = screen.getByTestId("file-input-input1");
    Object.defineProperty(fileInput, "files", {
      value: [file],
      writable: false,
    });

    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(consoleErrorSpy).toHaveBeenCalledWith("Input input1: Error reading file:", expect.any(Error));
      // setFileData should NOT be called when there's an error
      expect(mockSetFileData).not.toHaveBeenCalled();
    });

    consoleErrorSpy.mockRestore();
  });
});
