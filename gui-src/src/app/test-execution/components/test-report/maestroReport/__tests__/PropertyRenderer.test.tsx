import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { PropertyRenderer } from "../PropertyRenderer";
import type { ByteArrayItemProperty, ByteArrayProperty, StringProperty, BooleanProperty } from "@maestro/types/report/Property";
import { handleDownloadItem } from "../utils/propertyUtils";

// Mock dependencies
vi.mock("../AttachmentRenderer", () => ({
  AttachmentRenderer: (props: { name: string; reference: string; itemId: string }) => (
    <div data-testid="attachment-renderer" data-name={props.name} data-reference={props.reference} data-item-id={props.itemId}>
      {props.name}
    </div>
  ),
}));

vi.mock("../utils/propertyUtils", () => ({
  handleDownloadItem: vi.fn(),
  getTypeFromReferences: vi.fn(() => "application/pdf"),
}));

describe("PropertyRenderer", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("ByteArrayItemProperty with attachments/datahouse references", () => {
    it("should render AttachmentRenderer for attachment reference", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "test-attachment",
        fileName: "report.pdf",
        reference: "/attachments/abc123",
        mimeType: "application/pdf",
        itemType: "TEST_REPORT",
      };

      render(<PropertyRenderer property={property} defaultName="Default Name" isInput={false} itemId="item-789" references={[]} />);

      const renderer = screen.getByTestId("attachment-renderer");
      expect(renderer).toBeInTheDocument();
      expect(renderer).toHaveAttribute("data-name", "report.pdf");
      expect(renderer).toHaveAttribute("data-reference", "/attachments/abc123");
      expect(renderer).toHaveAttribute("data-item-id", "item-789");

      // Should be wrapped in <li>
      const listItem = renderer.closest("li");
      expect(listItem).toBeInTheDocument();
      expect(listItem).toHaveClass("list-item", "ml-5");
    });

    it("should render AttachmentRenderer for datahouse reference", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "validation-report",
        reference: "/datahouse/items/dh-123",
        itemType: "VALIDATION_REPORT",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-456" references={[]} />);

      const renderer = screen.getByTestId("attachment-renderer");
      expect(renderer).toBeInTheDocument();
      expect(renderer).toHaveAttribute("data-name", "validation-report");
    });

    it("should use fileName over name for label", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "name-value",
        fileName: "file-name.pdf",
        reference: "/attachments/xyz",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-123" references={[]} />);

      expect(screen.getByTestId("attachment-renderer")).toHaveAttribute("data-name", "file-name.pdf");
    });

    it("should use defaultName when name and fileName are missing", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "",
        reference: "/attachments/abc",
      };

      render(<PropertyRenderer property={property} defaultName="Fallback Name" isInput={false} itemId="item-999" references={[]} />);

      expect(screen.getByTestId("attachment-renderer")).toHaveAttribute("data-name", "Fallback Name");
    });
  });

  describe("ByteArrayItemProperty with inline value", () => {
    it("should render download button for property with value", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "inline-file",
        fileName: "document.xml",
        value: "base64encodedcontent",
        mimeType: "application/xml",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-123" references={[]} />);

      const button = screen.getByRole("button", { name: "document.xml" });
      expect(button).toBeInTheDocument();
      expect(button).toHaveClass("text-blue", "align-middle");
      expect(button).toHaveAttribute("title", "Download file");
    });

    it("should call handleDownloadItem when download button clicked", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "clickable-file",
        value: "content123",
        mimeType: "application/json",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-456" references={[]} />);

      const button = screen.getByRole("button");
      fireEvent.click(button);

      expect(handleDownloadItem).toHaveBeenCalledWith("content123", "clickable-file", "application/json");
    });

    it("should use getTypeFromReferences when mimeType is missing", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "no-mime-file",
        value: "content",
      };

      const references = [{ id: "item-123", type: "application/pdf" }];

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-123" references={references as any} />);

      const button = screen.getByRole("button");
      fireEvent.click(button);

      expect(handleDownloadItem).toHaveBeenCalledWith("content", "no-mime-file", "application/pdf");
    });
  });

  describe("ByteArrayItemProperty without reference or value", () => {
    it("should render grey span when no reference or value", () => {
      const property: ByteArrayItemProperty = {
        type: "BYTE_ARRAY_ITEM",
        name: "empty-property",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-123" references={[]} />);

      const span = screen.getByText("empty-property");
      expect(span).toBeInTheDocument();
      expect(span.tagName).toBe("SPAN");
      expect(span).toHaveClass("text-grey-500");
    });
  });

  describe("ByteArrayProperty (inline file content)", () => {
    it("should render download button for ByteArrayProperty with value", () => {
      const property: ByteArrayProperty = {
        type: "BYTE_ARRAY",
        name: "byte-array-file",
        fileName: "data.bin",
        value: "binarydata",
        mimeType: "application/octet-stream",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-123" references={[]} />);

      const button = screen.getByRole("button", { name: "data.bin" });
      expect(button).toBeInTheDocument();
      fireEvent.click(button);

      expect(handleDownloadItem).toHaveBeenCalledWith("binarydata", "data.bin", "application/octet-stream");
    });

    it("should use default mimeType when not provided", () => {
      const property: ByteArrayProperty = {
        type: "BYTE_ARRAY",
        name: "no-mime",
        value: "data",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-123" references={[]} />);

      const button = screen.getByRole("button");
      fireEvent.click(button);

      expect(handleDownloadItem).toHaveBeenCalledWith("data", "no-mime", "application/octet-stream");
    });

    it("should render grey span when ByteArrayProperty has no value", () => {
      const property: ByteArrayProperty = {
        type: "BYTE_ARRAY",
        name: "empty-byte-array",
        value: "",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={false} itemId="item-123" references={[]} />);

      const span = screen.getByText("empty-byte-array");
      expect(span).toHaveClass("text-grey-500");
    });
  });

  describe("StringProperty", () => {
    it("should render inline string property", () => {
      const property: StringProperty = {
        type: "STRING",
        name: "username",
        value: "john.doe@example.com",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      expect(screen.getByText("username:")).toBeInTheDocument();
      expect(screen.getByText("john.doe@example.com")).toBeInTheDocument();

      const valueSpan = screen.getByText("john.doe@example.com");
      expect(valueSpan).toHaveClass("font-mono", "break-all", "bg-grey-100", "p-1", "rounded-small");
    });

    it("should render string with special characters", () => {
      const property: StringProperty = {
        type: "STRING",
        name: "complex-string",
        value: 'Hello <World> & "Test"',
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      expect(screen.getByText('Hello <World> & "Test"')).toBeInTheDocument();
    });
  });

  describe("BooleanProperty", () => {
    it("should render true boolean value", () => {
      const property: BooleanProperty = {
        type: "BOOLEAN",
        name: "isActive",
        value: true,
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      expect(screen.getByText("isActive:")).toBeInTheDocument();
      expect(screen.getByText("true")).toBeInTheDocument();
    });

    it("should render false boolean value", () => {
      const property: BooleanProperty = {
        type: "BOOLEAN",
        name: "isDisabled",
        value: false,
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      expect(screen.getByText("isDisabled:")).toBeInTheDocument();
      expect(screen.getByText("false")).toBeInTheDocument();
    });
  });

  describe("Other property types (INTEGER, FLOAT, DATE)", () => {
    it("should render fallback for INTEGER property (not STRING or BOOLEAN)", () => {
      const property = {
        type: "INTEGER" as const,
        name: "count",
        value: 42,
      };

      render(<PropertyRenderer property={property as any} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      // INTEGER properties fall through to fallback since only STRING and BOOLEAN are rendered inline
      const span = screen.getByText("count");
      expect(span).toBeInTheDocument();
      expect(span).toHaveClass("text-grey-500");
    });

    it("should render fallback for FLOAT property", () => {
      const property = {
        type: "FLOAT" as const,
        name: "percentage",
        value: 99.99,
      };

      render(<PropertyRenderer property={property as any} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      // FLOAT properties fall through to fallback
      const span = screen.getByText("percentage");
      expect(span).toBeInTheDocument();
      expect(span).toHaveClass("text-grey-500");
    });

    it("should render fallback for property with undefined value", () => {
      const property = {
        type: "INTEGER" as const,
        name: "undefined-value",
        value: undefined,
      };

      const { container } = render(
        <PropertyRenderer property={property as any} defaultName="Default" isInput={true} itemId="item-123" references={[]} />,
      );

      // Should render fallback
      const span = container.querySelector(".text-grey-500");
      expect(span).toBeInTheDocument();
    });

    it("should render fallback for property with null value", () => {
      const property = {
        type: "INTEGER" as const,
        name: "null-value",
        value: null,
      };

      const { container } = render(
        <PropertyRenderer property={property as any} defaultName="Default" isInput={true} itemId="item-123" references={[]} />,
      );

      const span = container.querySelector(".text-grey-500");
      expect(span).toBeInTheDocument();
    });
  });

  describe("Fallback rendering", () => {
    it("should render fallback for unknown property type", () => {
      const property = {
        type: "UNKNOWN" as any,
        name: "unknown-property",
      };

      render(<PropertyRenderer property={property} defaultName="Default Name" isInput={false} itemId="item-123" references={[]} />);

      const span = screen.getByText("unknown-property");
      expect(span).toHaveClass("text-grey-500");
    });

    it("should use defaultName when property name is empty in fallback", () => {
      const property = {
        type: "UNKNOWN" as any,
        name: "",
      };

      render(<PropertyRenderer property={property} defaultName="Fallback Default" isInput={false} itemId="item-123" references={[]} />);

      expect(screen.getByText("Fallback Default")).toBeInTheDocument();
    });
  });

  describe("List item wrapping", () => {
    it("should wrap all property types in <li> with correct classes", () => {
      const property: StringProperty = {
        type: "STRING",
        name: "test",
        value: "value",
      };

      const { container } = render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      const listItem = container.querySelector("li");
      expect(listItem).toBeInTheDocument();
      expect(listItem).toHaveClass("list-item", "ml-5");
    });

    it("should use property name as key", () => {
      const property: StringProperty = {
        type: "STRING",
        name: "unique-key",
        value: "test",
      };

      const { container } = render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      const listItem = container.querySelector("li");
      expect(listItem).toBeInTheDocument();
    });
  });

  describe("Edge cases", () => {
    it("should handle property with empty string name", () => {
      const property: StringProperty = {
        type: "STRING",
        name: "",
        value: "some-value",
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      // Should still render the value
      expect(screen.getByText("some-value")).toBeInTheDocument();
    });

    it("should handle very long string values", () => {
      const longString = "a".repeat(1000);
      const property: StringProperty = {
        type: "STRING",
        name: "long-value",
        value: longString,
      };

      render(<PropertyRenderer property={property} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      expect(screen.getByText(longString)).toBeInTheDocument();
      const valueSpan = screen.getByText(longString);
      expect(valueSpan).toHaveClass("break-all");
    });

    it("should handle zero as a valid number value (renders as fallback)", () => {
      const property = {
        type: "INTEGER" as const,
        name: "zero-value",
        value: 0,
      };

      render(<PropertyRenderer property={property as any} defaultName="Default" isInput={true} itemId="item-123" references={[]} />);

      // INTEGER type falls through to fallback, displaying only the name
      expect(screen.getByText("zero-value")).toBeInTheDocument();
    });
  });
});
