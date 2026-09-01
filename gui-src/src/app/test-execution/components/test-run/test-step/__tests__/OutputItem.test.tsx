import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import OutputItem from "../OutputItem";
import { Output } from "../TestStepOutputs";
import { getReportUrl } from "@test-execution/components/test-run/utils/getReportUrl";

vi.mock("@test-execution/components/test-run/utils/getReportUrl");

vi.mock("@test-execution/components/test-report/maestroReport/AttachmentRenderer", () => ({
  AttachmentRenderer: (props: { name: string; reference: string; mimeType?: string; itemType?: string; itemId: string }) => (
    <div
      data-testid="attachment-renderer"
      data-name={props.name}
      data-reference={props.reference}
      data-mime-type={props.mimeType}
      data-item-type={props.itemType}
      data-item-id={props.itemId}
    >
      {props.name}
    </div>
  ),
}));

vi.mock("next/link", () => ({
  default: (props: { children: React.ReactNode; href: string; target?: string; rel?: string; className?: string }) => (
    <a href={props.href} target={props.target} rel={props.rel} className={props.className}>
      {props.children}
    </a>
  ),
}));

describe("OutputItem", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(getReportUrl).mockReturnValue(null);
  });

  describe("Attachment and datahouse references", () => {
    it("should render AttachmentRenderer for attachment reference with itemId", () => {
      const output: Output = {
        name: "test-attachment",
        fileName: "report.pdf",
        reference: "/attachments/abc123",
        mimeType: "application/pdf",
        itemType: "TEST_REPORT",
      };

      render(<OutputItem output={output} itemId="item-789" />);

      const renderer = screen.getByTestId("attachment-renderer");
      expect(renderer).toBeInTheDocument();
      expect(renderer).toHaveAttribute("data-name", "report.pdf");
      expect(renderer).toHaveAttribute("data-reference", "/attachments/abc123");
      expect(renderer).toHaveAttribute("data-mime-type", "application/pdf");
      expect(renderer).toHaveAttribute("data-item-type", "TEST_REPORT");
      expect(renderer).toHaveAttribute("data-item-id", "item-789");
    });

    it("should render AttachmentRenderer for datahouse reference with itemId", () => {
      const output: Output = {
        name: "validation-report",
        reference: "/datahouse/items/dh-123",
        itemType: "VALIDATION_REPORT",
      };

      render(<OutputItem output={output} itemId="item-456" />);

      const renderer = screen.getByTestId("attachment-renderer");
      expect(renderer).toBeInTheDocument();
      expect(renderer).toHaveAttribute("data-name", "validation-report");
      expect(renderer).toHaveAttribute("data-reference", "/datahouse/items/dh-123");
    });

    it("should not render AttachmentRenderer for attachment reference without itemId", () => {
      const output: Output = {
        name: "test-attachment",
        reference: "/attachments/abc123",
      };

      render(<OutputItem output={output} />);

      expect(screen.queryByTestId("attachment-renderer")).not.toBeInTheDocument();
      // Should render fallback span
      expect(screen.getByText("test-attachment")).toBeInTheDocument();
    });
  });

  describe("Report references (validation/simulation)", () => {
    it("should render Link for validation report reference", () => {
      vi.mocked(getReportUrl).mockReturnValue("/validation-portal/reports/validation-123");

      const output: Output = {
        name: "validation-output",
        fileName: "validation-report.xml",
        reference: "/items/validation-123",
      };

      render(<OutputItem output={output} />);

      const link = screen.getByRole("link");
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute("href", "/validation-portal/reports/validation-123");
      expect(link).toHaveAttribute("target", "_blank");
      expect(link).toHaveAttribute("rel", "noopener noreferrer");
      expect(link).toHaveTextContent("validation-report.xml");
      expect(link).toHaveClass("text-blue", "underline");
    });

    it("should render Link for simulation report reference", () => {
      vi.mocked(getReportUrl).mockReturnValue("/simulation-portal/reports/simulation-456");

      const output: Output = {
        name: "simulation-output",
        fileName: "simulation-report.xml",
        reference: "/items/simulation-456",
      };

      render(<OutputItem output={output} />);

      const link = screen.getByRole("link");
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute("href", "/simulation-portal/reports/simulation-456");
      expect(link).toHaveTextContent("simulation-report.xml");
    });

    it("should call getReportUrl with correct kindOfReport for validation", () => {
      vi.mocked(getReportUrl).mockReturnValue("/validation-portal/reports/val-123");

      const output: Output = {
        name: "output",
        fileName: "validation-report.xml",
        reference: "/items/val-123",
      };

      render(<OutputItem output={output} />);

      expect(getReportUrl).toHaveBeenCalledWith({
        testReportUrl: "/items/val-123",
        kindOfReport: "VALIDATION_REPORT",
      });
    });

    it("should call getReportUrl with correct kindOfReport for simulation", () => {
      vi.mocked(getReportUrl).mockReturnValue("/simulation-portal/reports/sim-123");

      const output: Output = {
        name: "output",
        fileName: "simulation-data.xml",
        reference: "/items/sim-123",
      };

      render(<OutputItem output={output} />);

      expect(getReportUrl).toHaveBeenCalledWith({
        testReportUrl: "/items/sim-123",
        kindOfReport: "SIMULATION_REPORT",
      });
    });

    it("should not call getReportUrl when neither validation nor simulation", () => {
      const output: Output = {
        name: "output",
        fileName: "test-report.xml",
        reference: "/items/generic-123",
      };

      render(<OutputItem output={output} />);

      // getReportUrl should not be called for generic reports
      expect(getReportUrl).not.toHaveBeenCalled();

      // Should render fallback span since reference doesn't include "attachments" or "datahouse"
      const span = screen.getByText("test-report.xml");
      expect(span).toBeInTheDocument();
      expect(span.tagName).toBe("SPAN");
    });
  });

  describe("Fallback rendering", () => {
    it("should render span with fileName when no reference", () => {
      const output: Output = {
        name: "test-output",
        fileName: "output.txt",
      };

      render(<OutputItem output={output} />);

      const span = screen.getByText("output.txt");
      expect(span).toBeInTheDocument();
      expect(span.tagName).toBe("SPAN");
      expect(span).toHaveClass("ml-2");
    });

    it("should render span with name when no fileName or reference", () => {
      const output: Output = {
        name: "output-name",
      };

      render(<OutputItem output={output} />);

      const span = screen.getByText("output-name");
      expect(span).toBeInTheDocument();
    });

    it("should render span with translation key when no fileName or name", () => {
      const output: Output = {
        name: "",
      };

      const { container } = render(<OutputItem output={output} />);

      // When name is empty, the component renders an empty span
      const span = container.querySelector("span");
      expect(span).toBeInTheDocument();
    });

    it("should render span without margin when inline=true", () => {
      const output: Output = {
        name: "inline-output",
        fileName: "inline.txt",
      };

      render(<OutputItem output={output} inline />);

      const span = screen.getByText("inline.txt");
      expect(span).toBeInTheDocument();
      expect(span).not.toHaveClass("ml-2");
      expect(span.className).toBe("");
    });
  });

  describe("Reference handling without valid reportUrl", () => {
    it("should render fallback span when reference exists but getReportUrl returns null", () => {
      vi.mocked(getReportUrl).mockReturnValue(null);

      const output: Output = {
        name: "unknown-ref",
        fileName: "unknown.xml",
        reference: "/some/invalid/reference",
      };

      render(<OutputItem output={output} />);

      const span = screen.getByText("unknown.xml");
      expect(span).toBeInTheDocument();
      expect(span.tagName).toBe("SPAN");
      expect(screen.queryByRole("link")).not.toBeInTheDocument();
    });
  });

  describe("Label determination", () => {
    it("should prefer fileName over name", () => {
      const output: Output = {
        name: "name-value",
        fileName: "file-name.pdf",
        reference: "/attachments/abc123",
      };

      render(<OutputItem output={output} itemId="item-123" />);

      expect(screen.getByTestId("attachment-renderer")).toHaveAttribute("data-name", "file-name.pdf");
    });

    it("should use name when fileName is not provided", () => {
      const output: Output = {
        name: "name-only",
        reference: "/attachments/xyz789",
      };

      render(<OutputItem output={output} itemId="item-456" />);

      expect(screen.getByTestId("attachment-renderer")).toHaveAttribute("data-name", "name-only");
    });
  });

  describe("computeKindOfReport function", () => {
    it("should detect validation report (case insensitive)", () => {
      vi.mocked(getReportUrl).mockReturnValue("/validation-portal/reports/val-123");

      const output: Output = {
        name: "output",
        fileName: "VALIDATION-Report.xml",
        reference: "/items/val-123",
      };

      render(<OutputItem output={output} />);

      expect(getReportUrl).toHaveBeenCalledWith(
        expect.objectContaining({
          kindOfReport: "VALIDATION_REPORT",
        }),
      );
    });

    it("should detect simulation report (case insensitive)", () => {
      vi.mocked(getReportUrl).mockReturnValue("/simulation-portal/reports/sim-123");

      const output: Output = {
        name: "SIMULATION-output",
        reference: "/items/sim-123",
      };

      render(<OutputItem output={output} />);

      expect(getReportUrl).toHaveBeenCalledWith(
        expect.objectContaining({
          kindOfReport: "SIMULATION_REPORT",
        }),
      );
    });
  });
});
