/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import ValidationReportFiles from "../ValidationReportFiles";
import { useReportAssertions } from "@validation-portal/context/selectedAssertionContext";
import { ValidationInput } from "@/shared/types/validation/types";

// Mock dependencies
vi.mock("@validation-portal/context/selectedAssertionContext", () => ({
  useReportAssertions: vi.fn(),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  CollapsableSubCard: ({ title, children, className }: any) => (
    <div data-testid="collapsable-sub-card" className={className}>
      <div data-testid="card-title">{title}</div>
      <div data-testid="card-content">{children}</div>
    </div>
  ),
}));

vi.mock("../FileRenderer", () => ({
  default: ({ fileName, content, attachmentId, itemId }: any) => (
    <div data-testid="file-renderer">
      <span data-testid="file-name">{fileName}</span>
      <span data-testid="file-content">{content}</span>
      <span data-testid="attachment-id">{attachmentId}</span>
      <span data-testid="item-id">{itemId}</span>
    </div>
  ),
}));

describe("ValidationReportFiles", () => {
  const mockUseReportAssertions = useReportAssertions as any;
  let scrollIntoViewMock: any;

  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks();

    // Mock scrollIntoView
    scrollIntoViewMock = vi.fn();
    Element.prototype.scrollIntoView = scrollIntoViewMock;

    // Default mock implementation
    mockUseReportAssertions.mockReturnValue({
      selectedAssertion: null,
    });
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  const createValidationInput = (id: string, content?: string, itemId?: string): ValidationInput => ({
    id,
    itemId: itemId ?? `item-${id}`,
    content: content ?? btoa(`Content for ${id}`),
  });

  describe("rendering", () => {
    it("renders validation items", () => {
      const validationItems = [createValidationInput("file1.xml"), createValidationInput("file2.json")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const cards = screen.getAllByTestId("collapsable-sub-card");
      expect(cards).toHaveLength(2);
    });

    it("renders empty when no validation items", () => {
      render(<ValidationReportFiles itemId="report-123" validationItems={[]} />);

      const cards = screen.queryAllByTestId("collapsable-sub-card");
      expect(cards).toHaveLength(0);
    });

    it("renders item with correct title format", () => {
      const validationItems = [createValidationInput("test-file.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const title = screen.getByTestId("card-title");
      expect(title.textContent).toContain("Validated file #1 (test-file.xml)");
      expect(title.textContent).toContain("#1");
      expect(title.textContent).toContain("(test-file.xml)");
    });

    it("renders multiple items with correct numbering", () => {
      const validationItems = [createValidationInput("file1.xml"), createValidationInput("file2.xml"), createValidationInput("file3.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const titles = screen.getAllByTestId("card-title");
      expect(titles[0].textContent).toContain("#1");
      expect(titles[1].textContent).toContain("#2");
      expect(titles[2].textContent).toContain("#3");
    });

    it("applies correct className to CollapsableSubCard", () => {
      const validationItems = [createValidationInput("file1.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const card = screen.getByTestId("collapsable-sub-card");
      expect(card.className).toContain("border-lightpurple");
    });
  });

  describe("FileRenderer integration", () => {
    it("passes correct props to FileRenderer", () => {
      const validationItems = [createValidationInput("test-file.xml", btoa("test content"), "attachment-123")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      expect(screen.getByTestId("file-name").textContent).toBe("test-file.xml");
      expect(screen.getByTestId("file-content").textContent).toBe(btoa("test content"));
      expect(screen.getByTestId("attachment-id").textContent).toBe("attachment-123");
      expect(screen.getByTestId("item-id").textContent).toBe("report-123");
    });

    it("renders FileRenderer for each validation item", () => {
      const validationItems = [createValidationInput("file1.xml"), createValidationInput("file2.json"), createValidationInput("file3.txt")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const fileRenderers = screen.getAllByTestId("file-renderer");
      expect(fileRenderers).toHaveLength(3);
    });
  });

  describe("scroll behavior on assertion selection", () => {
    beforeEach(() => {
      vi.useFakeTimers();
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it("scrolls to first selected line when FAILED assertion is selected", () => {
      mockUseReportAssertions.mockReturnValue({
        selectedAssertion: {
          result: "FAILED",
          assertionID: "A1",
        },
      });

      const mockElement = document.createElement("div");
      mockElement.classList.add("gzl-selected-line");
      document.body.appendChild(mockElement);

      const validationItems = [createValidationInput("file1.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      vi.advanceTimersByTime(100);

      expect(scrollIntoViewMock).toHaveBeenCalledWith({
        behavior: "smooth",
        block: "center",
      });

      mockElement.remove();
    });

    it("does not scroll when assertion is not FAILED", () => {
      mockUseReportAssertions.mockReturnValue({
        selectedAssertion: {
          result: "PASSED",
          assertionID: "A1",
        },
      });

      const mockElement = document.createElement("div");
      mockElement.classList.add("gzl-selected-line");
      document.body.appendChild(mockElement);

      const validationItems = [createValidationInput("file1.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      vi.advanceTimersByTime(100);

      expect(scrollIntoViewMock).not.toHaveBeenCalled();

      mockElement.remove();
    });

    it("does not scroll when no selected lines exist", () => {
      mockUseReportAssertions.mockReturnValue({
        selectedAssertion: {
          result: "FAILED",
          assertionID: "A1",
        },
      });

      const validationItems = [createValidationInput("file1.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      vi.advanceTimersByTime(100);

      expect(scrollIntoViewMock).not.toHaveBeenCalled();
    });

    it("does not scroll when validationItems is empty", () => {
      mockUseReportAssertions.mockReturnValue({
        selectedAssertion: {
          result: "FAILED",
          assertionID: "A1",
        },
      });

      const mockElement = document.createElement("div");
      mockElement.classList.add("gzl-selected-line");
      document.body.appendChild(mockElement);

      render(<ValidationReportFiles itemId="report-123" validationItems={[]} />);

      vi.advanceTimersByTime(100);

      expect(scrollIntoViewMock).not.toHaveBeenCalled();

      mockElement.remove();
    });

    it("scrolls to first element when multiple selected lines exist", () => {
      mockUseReportAssertions.mockReturnValue({
        selectedAssertion: {
          result: "FAILED",
          assertionID: "A1",
        },
      });

      const mockElement1 = document.createElement("div");
      mockElement1.classList.add("gzl-selected-line");
      mockElement1.textContent = "First";
      document.body.appendChild(mockElement1);

      const mockElement2 = document.createElement("div");
      mockElement2.classList.add("gzl-selected-line");
      mockElement2.textContent = "Second";
      document.body.appendChild(mockElement2);

      const validationItems = [createValidationInput("file1.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      vi.advanceTimersByTime(100);

      expect(scrollIntoViewMock).toHaveBeenCalledTimes(1);

      mockElement1.remove();
      mockElement2.remove();
    });

    it("does not scroll when selectedAssertion is null", () => {
      mockUseReportAssertions.mockReturnValue({
        selectedAssertion: null,
      });

      const mockElement = document.createElement("div");
      mockElement.classList.add("gzl-selected-line");
      document.body.appendChild(mockElement);

      const validationItems = [createValidationInput("file1.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      vi.advanceTimersByTime(100);

      expect(scrollIntoViewMock).not.toHaveBeenCalled();

      mockElement.remove();
    });
  });

  describe("edge cases", () => {
    it("handles validation items without content", () => {
      const validationItems = [
        {
          id: "file1.xml",
          itemId: "item-1",
        } as ValidationInput,
      ];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      expect(screen.getByTestId("file-renderer")).toBeInTheDocument();
    });

    it("handles validation items without itemId", () => {
      const validationItems = [
        {
          id: "file1.xml",
          content: btoa("content"),
        } as ValidationInput,
      ];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      expect(screen.getByTestId("file-renderer")).toBeInTheDocument();
    });

    it("renders with item ID in title when id is present", () => {
      const validationItems = [
        {
          id: "my-file.xml",
          itemId: "item-123",
          content: btoa("content"),
        } as ValidationInput,
      ];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const title = screen.getByTestId("card-title");
      expect(title.textContent).toContain("(my-file.xml)");
    });

    it("renders without id in parentheses when id is empty", () => {
      const validationItems = [
        {
          id: "",
          itemId: "item-123",
          content: btoa("content"),
        } as ValidationInput,
      ];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const title = screen.getByTestId("card-title");
      expect(title.textContent).not.toContain("()");
    });
  });

  describe("title generation", () => {
    it("generates correct h3 id for each item", () => {
      const validationItems = [createValidationInput("file1.xml"), createValidationInput("file2.xml")];

      render(<ValidationReportFiles itemId="report-123" validationItems={validationItems} />);

      const titles = screen.getAllByTestId("card-title");
      const h3Elements = titles.map((title) => title.querySelector("h3"));

      expect(h3Elements[0]?.id).toBe("validated-file-0");
      expect(h3Elements[1]?.id).toBe("validated-file-1");
    });
  });
});
