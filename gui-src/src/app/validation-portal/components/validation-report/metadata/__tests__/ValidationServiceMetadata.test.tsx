/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { ValidationServiceMetadata } from "../ValidationServiceMetadata";
import { ValidationReportDTO } from "@/shared/types/validation/types";

vi.mock("@gazelle/gazelle-component-ui", () => ({
  CollapsableSubCard: ({ title, children, defaultExpanded }: any) => (
    <div data-testid="collapsable-sub-card" data-expanded={defaultExpanded}>
      <div data-testid="card-title">{title}</div>
      <div data-testid="card-content">{children}</div>
    </div>
  ),
  InfoRow: ({ label, value }: any) => (
    <div data-testid="info-row">
      <span data-testid="info-label">{label}</span>
      <span data-testid="info-value">{value}</span>
    </div>
  ),
}));

describe("ValidationServiceMetadata", () => {
  const createValidationReport = (metadata: any[] = []): ValidationReportDTO => ({
    modelVersion: "1.0",
    uuid: "test-uuid",
    dateTime: "2026-02-02T00:00:00Z",
    disclaimer: "Test disclaimer",
    overallResult: "PASSED",
    validationMethod: {
      validationServiceName: "Test Service",
      validationServiceVersion: "1.0",
      validationProfileID: "profile-1",
      validationProfileVersion: "1.0",
    },
    reports: [],
    counters: {
      numberOfAssertions: 0,
      numberOfFailedWithInfos: 0,
      numberOfFailedWithWarnings: 0,
      numberOfFailedWithErrors: 0,
      numberOfUnexpectedErrors: 0,
      numberOfUndefined: 0,
    },
    additionalMetadata: metadata,
  });

  describe("rendering", () => {
    it("renders metadata when additionalMetadata is present", () => {
      const validationReport = createValidationReport([{ name: "Test Key", value: "Test Value" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      expect(screen.getByTestId("collapsable-sub-card")).toBeInTheDocument();
      expect(screen.getByTestId("info-row")).toBeInTheDocument();
    });

    it("renders multiple metadata items", () => {
      const validationReport = createValidationReport([
        { name: "Key 1", value: "Value 1" },
        { name: "Key 2", value: "Value 2" },
        { name: "Key 3", value: "Value 3" },
      ]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const infoRows = screen.getAllByTestId("info-row");
      expect(infoRows).toHaveLength(3);
    });

    it("renders correct title", () => {
      const validationReport = createValidationReport([{ name: "Test Key", value: "Test Value" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const title = screen.getByTestId("card-title");
      const h3 = title.querySelector("h3");
      expect(h3?.textContent).toBe("Validation service metadata");
      expect(h3?.id).toBe("validation-service-metadata");
    });

    it("sets defaultExpanded to false", () => {
      const validationReport = createValidationReport([{ name: "Test Key", value: "Test Value" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const card = screen.getByTestId("collapsable-sub-card");
      expect(card.dataset.expanded).toBe("false");
    });
  });

  describe("null rendering", () => {
    it("returns null when additionalMetadata is undefined", () => {
      const validationReport = createValidationReport();
      validationReport.additionalMetadata = undefined as any;

      const { container } = render(<ValidationServiceMetadata validationReport={validationReport} />);

      expect(container.firstChild).toBeNull();
    });

    it("returns null when additionalMetadata is empty array", () => {
      const validationReport = createValidationReport([]);

      const { container } = render(<ValidationServiceMetadata validationReport={validationReport} />);

      expect(container.firstChild).toBeNull();
    });

    it("returns null when additionalMetadata is null", () => {
      const validationReport = createValidationReport();
      validationReport.additionalMetadata = null as any;

      const { container } = render(<ValidationServiceMetadata validationReport={validationReport} />);

      expect(container.firstChild).toBeNull();
    });
  });

  describe("metadata rendering", () => {
    it("renders label and value correctly", () => {
      const validationReport = createValidationReport([{ name: "Test Label", value: "Test Value" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      expect(screen.getByTestId("info-label").textContent).toBe("Test Label");
      expect(screen.getByTestId("info-value").textContent).toBe("Test Value");
    });

    it("renders each metadata with correct label and value", () => {
      const validationReport = createValidationReport([
        { name: "Key 1", value: "Value 1" },
        { name: "Key 2", value: "Value 2" },
      ]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const labels = screen.getAllByTestId("info-label");
      const values = screen.getAllByTestId("info-value");

      expect(labels[0].textContent).toBe("Key 1");
      expect(values[0].textContent).toBe("Value 1");
      expect(labels[1].textContent).toBe("Key 2");
      expect(values[1].textContent).toBe("Value 2");
    });
  });

  describe("link detection and rendering", () => {
    it("renders http link as anchor tag", () => {
      const validationReport = createValidationReport([{ name: "Website", value: "http://example.com" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const link = screen.getByRole("link");
      expect(link).toHaveAttribute("href", "http://example.com");
      expect(link.textContent).toBe("http://example.com");
      expect(link.className).toContain("text-blue");
    });

    it("renders https link as anchor tag", () => {
      const validationReport = createValidationReport([{ name: "Website", value: "https://example.com" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const link = screen.getByRole("link");
      expect(link).toHaveAttribute("href", "https://example.com");
      expect(link.textContent).toBe("https://example.com");
    });

    it("renders non-link value as plain text", () => {
      const validationReport = createValidationReport([{ name: "Key", value: "Plain text value" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const value = screen.getByTestId("info-value");
      expect(value.textContent).toBe("Plain text value");
      expect(value.querySelector("a")).toBeNull();
    });

    it("treats value without http/https as plain text", () => {
      const validationReport = createValidationReport([{ name: "Key", value: "ftp://example.com" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const value = screen.getByTestId("info-value");
      expect(value.textContent).toBe("ftp://example.com");
      expect(value.querySelector("a")).toBeNull();
    });

    it("treats value starting with 'http' but not 'http://' as plain text", () => {
      const validationReport = createValidationReport([{ name: "Key", value: "httpNotALink" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const value = screen.getByTestId("info-value");
      expect(value.textContent).toBe("httpNotALink");
      expect(value.querySelector("a")).toBeNull();
    });

    it("handles mixed links and plain text", () => {
      const validationReport = createValidationReport([
        { name: "Website", value: "https://example.com" },
        { name: "Description", value: "Plain text" },
        { name: "API", value: "http://api.example.com" },
      ]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const links = screen.getAllByRole("link");
      expect(links).toHaveLength(2);
      expect(links[0]).toHaveAttribute("href", "https://example.com");
      expect(links[1]).toHaveAttribute("href", "http://api.example.com");
    });
  });

  describe("edge cases", () => {
    it("handles empty string value", () => {
      const validationReport = createValidationReport([{ name: "Empty Key", value: "" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const value = screen.getByTestId("info-value");
      expect(value.textContent).toBe("");
    });

    it("handles undefined value", () => {
      const validationReport = createValidationReport([{ name: "Undefined Key", value: undefined as any }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      expect(screen.getByTestId("info-row")).toBeInTheDocument();
    });

    it("handles special characters in values", () => {
      const validationReport = createValidationReport([{ name: "Special", value: "Value with <special> & characters" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const value = screen.getByTestId("info-value");
      expect(value.textContent).toBe("Value with <special> & characters");
    });

    it("handles special characters in names", () => {
      const validationReport = createValidationReport([{ name: "Key with <special> & characters", value: "Value" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const label = screen.getByTestId("info-label");
      expect(label.textContent).toBe("Key with <special> & characters");
    });

    it("handles duplicate metadata names", () => {
      const validationReport = createValidationReport([
        { name: "Duplicate", value: "Value 1" },
        { name: "Duplicate", value: "Value 2" },
      ]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const infoRows = screen.getAllByTestId("info-row");
      expect(infoRows).toHaveLength(2);
    });

    it("handles long URL values", () => {
      const longUrl = "https://example.com/very/long/path/with/many/segments?query=param&another=value#fragment";
      const validationReport = createValidationReport([{ name: "Long URL", value: longUrl }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const link = screen.getByRole("link");
      expect(link).toHaveAttribute("href", longUrl);
      expect(link.textContent).toBe(longUrl);
    });

    it("handles numeric values", () => {
      const validationReport = createValidationReport([{ name: "Number", value: "12345" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const value = screen.getByTestId("info-value");
      expect(value.textContent).toBe("12345");
    });

    it("handles URL-like string without protocol", () => {
      const validationReport = createValidationReport([{ name: "Domain", value: "example.com" }]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const value = screen.getByTestId("info-value");
      expect(value.textContent).toBe("example.com");
      expect(value.querySelector("a")).toBeNull();
    });
  });

  describe("key generation", () => {
    it("generates unique keys for metadata items with same name", () => {
      const validationReport = createValidationReport([
        { name: "Same", value: "Value 1" },
        { name: "Same", value: "Value 2" },
        { name: "Same", value: "Value 3" },
      ]);

      render(<ValidationServiceMetadata validationReport={validationReport} />);

      const infoRows = screen.getAllByTestId("info-row");
      expect(infoRows).toHaveLength(3);

      // All rows should be rendered (no key collision)
      const values = screen.getAllByTestId("info-value");
      expect(values[0].textContent).toBe("Value 1");
      expect(values[1].textContent).toBe("Value 2");
      expect(values[2].textContent).toBe("Value 3");
    });
  });
});
