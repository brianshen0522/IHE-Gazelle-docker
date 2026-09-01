import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { AttachmentRenderer } from "../AttachmentRenderer";
import { useItemLink } from "@shared/hooks/useItemLink";
import { getTypeFromReferences } from "../utils/propertyUtils";

// Mock dependencies
vi.mock("@shared/hooks/useItemLink");

vi.mock("@test-execution/components/test-report/ButtonDownloadAttachment", () => ({
  ButtonDownloadAttachment: (props: { reportName: string; itemId: string; attachmentId: string; attachmentType: string }) => (
    <button
      data-testid="download-button"
      data-name={props.reportName}
      data-item-id={props.itemId}
      data-attachment-id={props.attachmentId}
      data-type={props.attachmentType}
    >
      {props.reportName}
    </button>
  ),
}));

vi.mock("../utils/propertyUtils");

vi.mock("next/link", () => ({
  default: (props: { children: React.ReactNode; href: string; className?: string; title?: string }) => (
    <a href={props.href} className={props.className} title={props.title}>
      {props.children}
    </a>
  ),
}));

vi.mock("lucide-react", () => ({
  ExternalLink: (props: { size?: number }) => <svg data-testid="external-link-icon" data-size={props.size} />,
}));

describe("AttachmentRenderer", () => {
  const mockGetItemLinkUrl = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useItemLink).mockReturnValue(mockGetItemLinkUrl);
    vi.mocked(getTypeFromReferences).mockReturnValue("application/pdf");
  });

  describe("Attachment references", () => {
    it("should render ButtonDownloadAttachment for attachment reference with mimeType", () => {
      render(<AttachmentRenderer name="test-report.pdf" reference="/attachments/abc123" mimeType="application/pdf" itemId="report-456" />);

      const button = screen.getByTestId("download-button");
      expect(button).toBeInTheDocument();
      expect(button).toHaveAttribute("data-name", "test-report.pdf");
      expect(button).toHaveAttribute("data-item-id", "report-456");
      expect(button).toHaveAttribute("data-attachment-id", "abc123");
      expect(button).toHaveAttribute("data-type", "application/pdf");
    });

    it("should render ButtonDownloadAttachment for attachment reference without mimeType", () => {
      render(<AttachmentRenderer name="document.xml" reference="/attachments/xyz789" itemId="report-789" />);

      const button = screen.getByTestId("download-button");
      expect(button).toBeInTheDocument();
      expect(button).toHaveAttribute("data-name", "document.xml");
      expect(button).toHaveAttribute("data-attachment-id", "xyz789");
      expect(button).toHaveAttribute("data-type", "application/pdf"); // from mocked getTypeFromReferences
    });

    it("should handle attachment reference with full URL", () => {
      render(
        <AttachmentRenderer
          name="full-url.pdf"
          reference="http://api.example.com/attachments/full123"
          mimeType="application/pdf"
          itemId="report-999"
        />,
      );

      const button = screen.getByTestId("download-button");
      expect(button).toBeInTheDocument();
      expect(button).toHaveAttribute("data-attachment-id", "full123");
    });
  });

  describe("Datahouse references", () => {
    it("should render Link when datahouse reference has valid path", () => {
      mockGetItemLinkUrl.mockReturnValue({
        path: "/test-execution/reports/dh-item-123",
        displayName: "Validation Report",
      });

      render(
        <AttachmentRenderer name="validation-report" reference="/datahouse/items/dh-item-123" itemType="VALIDATION_REPORT" itemId="report-456" />,
      );

      const link = screen.getByRole("link");
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute("href", "/test-execution/reports/dh-item-123");
      expect(link).toHaveAttribute("title", "Validation Report");
      expect(link).toHaveTextContent("validation-report");
      expect(screen.getByTestId("external-link-icon")).toBeInTheDocument();
    });

    it("should render external link when datahouse reference has no valid path", () => {
      mockGetItemLinkUrl.mockReturnValue({
        path: null,
        displayName: null,
      });

      render(<AttachmentRenderer name="unknown-report" reference="/datahouse/items/unknown-123" itemType="UNKNOWN_TYPE" itemId="report-456" />);

      const link = screen.getByRole("link");
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute("href", "/datahouse/items/unknown-123");
      expect(link).toHaveAttribute("target", "_blank");
      expect(link).toHaveAttribute("rel", "noopener noreferrer");
      expect(link).toHaveTextContent("unknown-report");
    });

    it("should call useItemLink with correct parameters", () => {
      mockGetItemLinkUrl.mockReturnValue({ path: "/reports/123", displayName: "Report" });

      render(<AttachmentRenderer name="test-report" reference="/datahouse/items/ref-123" itemType="TEST_REPORT" itemId="item-789" />);

      expect(mockGetItemLinkUrl).toHaveBeenCalledWith({
        reference: "/datahouse/items/ref-123",
        itemType: "TEST_REPORT",
        displayName: "test-report",
      });
    });
  });

  describe("Unknown reference types", () => {
    it("should render fallback span for unknown reference type", () => {
      render(<AttachmentRenderer name="unknown-file" reference="/some/unknown/reference" itemId="report-456" />);

      const span = screen.getByText("unknown-file");
      expect(span).toBeInTheDocument();
      expect(span.tagName).toBe("SPAN");
      expect(span).toHaveClass("text-grey-500");
    });
  });

  describe("References with additional data", () => {
    it("should pass references array to getTypeFromReferences", () => {
      const references = [{ id: "abc123", type: "application/json", name: "test.json" }];

      render(<AttachmentRenderer name="test.json" reference="/attachments/abc123" itemId="report-456" references={references as any} />);

      expect(getTypeFromReferences).toHaveBeenCalledWith("abc123", references);
    });

    it("should default references to empty array if not provided", () => {
      render(<AttachmentRenderer name="test.pdf" reference="/attachments/xyz789" itemId="report-456" />);

      expect(getTypeFromReferences).toHaveBeenCalledWith("xyz789", []);
    });
  });

  describe("Edge cases", () => {
    it("should handle attachment reference without valid ID", () => {
      render(<AttachmentRenderer name="invalid-attachment" reference="/attachments/" itemId="report-456" />);

      // Should render fallback since attachmentId will be empty string
      const span = screen.getByText("invalid-attachment");
      expect(span).toBeInTheDocument();
      expect(span.tagName).toBe("SPAN");
    });

    it("should handle empty reference string", () => {
      render(<AttachmentRenderer name="empty-ref" reference="" itemId="report-456" />);

      const span = screen.getByText("empty-ref");
      expect(span).toBeInTheDocument();
      expect(span).toHaveClass("text-grey-500");
    });
  });
});
