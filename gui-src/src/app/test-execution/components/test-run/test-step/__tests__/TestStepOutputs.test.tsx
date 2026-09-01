import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import TestStepOutputs, { Output } from "../TestStepOutputs";

vi.mock("lucide-react", () => ({
  FileCheck: (props: { className?: string }) => <svg data-testid="file-check-icon" className={props.className} />,
}));

vi.mock("../OutputItem", () => ({
  default: (props: { output: Output; inline?: boolean; itemId?: string }) => (
    <div
      data-testid="output-item"
      data-output-name={props.output.name}
      {...(props.inline !== undefined && { "data-inline": String(props.inline) })}
      data-item-id={String(props.itemId)}
    >
      {props.output.fileName || props.output.name}
    </div>
  ),
}));

describe("TestStepOutputs", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Empty/null outputs", () => {
    it("should return null when executionResult is undefined", () => {
      const { container } = render(<TestStepOutputs />);
      expect(container.firstChild).toBeNull();
    });

    it("should return null when outputs array is undefined", () => {
      const { container } = render(<TestStepOutputs executionResult={{}} />);
      expect(container.firstChild).toBeNull();
    });

    it("should return null when outputs array is empty", () => {
      const { container } = render(<TestStepOutputs executionResult={{ outputs: [] }} />);
      expect(container.firstChild).toBeNull();
    });
  });

  describe("Inline rendering mode", () => {
    it("should render FileCheck icon in inline mode", () => {
      const outputs: Output[] = [{ name: "output1", fileName: "file1.txt" }];
      render(<TestStepOutputs executionResult={{ outputs }} inline />);
      const icon = screen.getByTestId("file-check-icon");
      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass("w-4", "h-4", "text-blue");
    });

    it("should render single output item in inline mode", () => {
      const outputs: Output[] = [{ name: "single-output", fileName: "single.pdf" }];
      render(<TestStepOutputs executionResult={{ outputs }} inline itemId="item-123" />);
      const outputItem = screen.getByTestId("output-item");
      expect(outputItem).toBeInTheDocument();
      expect(outputItem).toHaveAttribute("data-output-name", "single-output");
      expect(outputItem).toHaveAttribute("data-inline", "true");
      expect(outputItem).toHaveAttribute("data-item-id", "item-123");
    });

    it("should render multiple outputs with separators in inline mode", () => {
      const outputs: Output[] = [
        { name: "output1", fileName: "file1.txt" },
        { name: "output2", fileName: "file2.pdf" },
        { name: "output3", fileName: "file3.xml" },
      ];
      render(<TestStepOutputs executionResult={{ outputs }} inline />);
      const outputItems = screen.getAllByTestId("output-item");
      expect(outputItems).toHaveLength(3);
      // Check separators - should have 2 separators for 3 items
      const separators = screen.getAllByText("|");
      expect(separators).toHaveLength(2);
    });

    it("should not render separator after last item in inline mode", () => {
      const outputs: Output[] = [{ name: "output1" }, { name: "output2" }];
      const { container } = render(<TestStepOutputs executionResult={{ outputs }} inline />);
      // Get all span elements containing separators
      const spans = container.querySelectorAll("span.mx-1");
      expect(spans).toHaveLength(1); // Only one separator for 2 items
    });

    it("should apply correct container classes in inline mode", () => {
      const outputs: Output[] = [{ name: "output" }];
      const { container } = render(<TestStepOutputs executionResult={{ outputs }} inline />);
      const wrapper = container.querySelector(".pt-2.flex.items-center.gap-2.justify-center");
      expect(wrapper).toBeInTheDocument();
    });

    it("should apply text-sm class to output spans in inline mode", () => {
      const outputs: Output[] = [{ name: "output1" }];
      const { container } = render(<TestStepOutputs executionResult={{ outputs }} inline />);
      const span = container.querySelector("span.text-sm");
      expect(span).toBeInTheDocument();
    });
  });

  describe("List rendering mode (default)", () => {
    it("should render outputs heading with translation", () => {
      const outputs: Output[] = [{ name: "output" }];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      expect(screen.getByText("Output(s):")).toBeInTheDocument();
    });

    it("should render outputs in list with correct structure", () => {
      const outputs: Output[] = [
        { name: "output1", fileName: "file1.txt" },
        { name: "output2", fileName: "file2.pdf" },
      ];
      const { container } = render(<TestStepOutputs executionResult={{ outputs }} />);
      const ul = container.querySelector("ul.list-disc.pl-5.space-y-1");
      expect(ul).toBeInTheDocument();
      const listItems = container.querySelectorAll("li.text-sm.flex");
      expect(listItems).toHaveLength(2);
    });

    it("should render OutputItem with itemId in list mode", () => {
      const outputs: Output[] = [{ name: "test-output" }];
      render(<TestStepOutputs executionResult={{ outputs }} itemId="report-789" />);
      const outputItem = screen.getByTestId("output-item");
      expect(outputItem).toHaveAttribute("data-item-id", "report-789");
      expect(outputItem).not.toHaveAttribute("data-inline"); // inline prop not passed in list mode
    });

    it("should not render mimeType in list mode", () => {
      const outputs: Output[] = [{ name: "output1", fileName: "file.pdf", mimeType: "application/pdf" }];
      const { container } = render(<TestStepOutputs executionResult={{ outputs }} />);
      expect(container.textContent).not.toContain("application/pdf");
      expect(container.querySelector(".text-xs")).not.toBeInTheDocument();
    });
  });

  describe("Key generation", () => {
    it("should use name and reference for key", () => {
      const outputs: Output[] = [{ name: "output1", reference: "/attachments/abc" }];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      const span = screen.getByTestId("output-item");
      // Note: React doesn't expose keys in the DOM, but we can verify rendering works
      expect(span).toBeInTheDocument();
    });

    it("should use name and fileName for key when reference is missing", () => {
      const outputs: Output[] = [{ name: "output1", fileName: "file.txt" }];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      expect(screen.getByTestId("output-item")).toBeInTheDocument();
    });

    it("should handle duplicate keys gracefully", () => {
      const outputs: Output[] = [
        { name: "output1", fileName: "file.txt" },
        { name: "output1", fileName: "file.txt" }, // Duplicate
      ];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      const items = screen.getAllByTestId("output-item");
      expect(items).toHaveLength(2);
    });
  });

  describe("Props passing to OutputItem", () => {
    it("should pass output object to OutputItem", () => {
      const output: Output = {
        name: "test-output",
        fileName: "test.pdf",
        reference: "/attachments/123",
        mimeType: "application/pdf",
        itemType: "TEST_REPORT",
      };
      render(<TestStepOutputs executionResult={{ outputs: [output] }} />);
      const item = screen.getByTestId("output-item");
      expect(item).toHaveAttribute("data-output-name", "test-output");
    });

    it("should pass inline prop correctly", () => {
      const outputs: Output[] = [{ name: "output" }];
      const { rerender } = render(<TestStepOutputs executionResult={{ outputs }} inline />);
      expect(screen.getByTestId("output-item")).toHaveAttribute("data-inline", "true");
      rerender(<TestStepOutputs executionResult={{ outputs }} />);
      expect(screen.getByTestId("output-item")).not.toHaveAttribute("data-inline"); // inline not passed
    });

    it("should pass itemId to all OutputItems", () => {
      const outputs: Output[] = [{ name: "output1" }, { name: "output2" }, { name: "output3" }];
      render(<TestStepOutputs executionResult={{ outputs }} itemId="shared-item-id" />);
      const items = screen.getAllByTestId("output-item");
      items.forEach((item) => {
        expect(item).toHaveAttribute("data-item-id", "shared-item-id");
      });
    });

    it("should handle undefined itemId", () => {
      const outputs: Output[] = [{ name: "output" }];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      const item = screen.getByTestId("output-item");
      expect(item).toHaveAttribute("data-item-id", "undefined");
    });
  });

  describe("Mixed output scenarios", () => {
    it("should render outputs with various properties", () => {
      const outputs: Output[] = [
        { name: "output1", fileName: "file1.pdf", mimeType: "application/pdf" },
        { name: "output2", reference: "/attachments/abc" },
        { name: "output3", itemType: "VALIDATION_REPORT" },
        { name: "output4" },
      ];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      const items = screen.getAllByTestId("output-item");
      expect(items).toHaveLength(4);
    });

    it("should handle outputs with empty strings", () => {
      const outputs: Output[] = [{ name: "", fileName: "" }];

      render(<TestStepOutputs executionResult={{ outputs }} />);

      expect(screen.getByTestId("output-item")).toBeInTheDocument();
    });

    it("should handle outputs with special characters in names", () => {
      const outputs: Output[] = [{ name: 'output<>with&special"chars' }];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      const item = screen.getByTestId("output-item");
      expect(item).toHaveAttribute("data-output-name", 'output<>with&special"chars');
    });
  });

  describe("Container structure", () => {
    it("should have correct container structure in list mode", () => {
      const outputs: Output[] = [{ name: "output" }];
      const { container } = render(<TestStepOutputs executionResult={{ outputs }} />);
      const wrapper = container.querySelector(".pt-2");
      expect(wrapper).toBeInTheDocument();
      const heading = wrapper?.querySelector(".text-sm.font-medium.mb-2");
      expect(heading).toBeInTheDocument();
      const list = wrapper?.querySelector("ul.list-disc.pl-5.space-y-1");
      expect(list).toBeInTheDocument();
    });

    it("should have correct container structure in inline mode", () => {
      const outputs: Output[] = [{ name: "output" }];
      const { container } = render(<TestStepOutputs executionResult={{ outputs }} inline />);
      const wrapper = container.querySelector(".pt-2.flex.items-center.gap-2.justify-center");
      expect(wrapper).toBeInTheDocument();
    });
  });

  describe("Edge cases", () => {
    it("should handle single output without separator", () => {
      const outputs: Output[] = [{ name: "single" }];
      render(<TestStepOutputs executionResult={{ outputs }} inline />);
      const separators = screen.queryAllByText("|");
      expect(separators).toHaveLength(0);
    });

    it("should render many outputs efficiently", () => {
      const outputs: Output[] = Array.from({ length: 100 }, (_, i) => ({
        name: `output${i}`,
        fileName: `file${i}.txt`,
      }));
      render(<TestStepOutputs executionResult={{ outputs }} />);
      const items = screen.getAllByTestId("output-item");
      expect(items).toHaveLength(100);
    });

    it("should handle outputs with null/undefined optional fields", () => {
      const outputs: Output[] = [{ name: "output", reference: undefined, fileName: undefined, mimeType: undefined, itemType: undefined }];
      render(<TestStepOutputs executionResult={{ outputs }} />);
      expect(screen.getByTestId("output-item")).toBeInTheDocument();
    });
  });
});
