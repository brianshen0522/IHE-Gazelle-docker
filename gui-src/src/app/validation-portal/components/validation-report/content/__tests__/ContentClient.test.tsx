import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ContentClient from "../ContentClient";
import { ReadonlyURLSearchParams, useSearchParams } from "next/navigation";

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useSearchParams: vi.fn(),
}));

// Mock components
vi.mock("@/shared/components/layout/ContentHeaderWrapper", () => ({
  default: ({ title }: { title: string }) => <div data-testid="content-header">{title}</div>,
}));

vi.mock("../FileRenderer", () => ({
  default: ({ fileName }: { fileName: string }) => <div data-testid="file-renderer">{fileName}</div>,
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  ScrollTop: () => <div data-testid="scroll-top" />,
}));

describe("ContentClient", () => {
  const mockUseSearchParams = vi.mocked(useSearchParams);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders with search params", () => {
    // Mock search params
    const mockSearchParams = new URLSearchParams();
    mockSearchParams.set("itemId", "test-item");
    mockSearchParams.set("attachmentId", "test-attachment");
    mockSearchParams.set("fileName", "test-file.json");
    mockUseSearchParams.mockReturnValue(mockSearchParams as ReadonlyURLSearchParams);

    render(<ContentClient />);

    expect(screen.getByTestId("content-header")).toHaveTextContent("Validated file (test-file.json)");
    expect(screen.getByTestId("file-renderer")).toHaveTextContent("test-file.json");
    expect(screen.getByTestId("scroll-top")).toBeInTheDocument();
  });

  it("renders without itemId", () => {
    const mockSearchParams = new URLSearchParams();
    mockSearchParams.set("attachmentId", "test-attachment");
    mockSearchParams.set("fileName", "test-file.json");
    mockUseSearchParams.mockReturnValue(mockSearchParams as ReadonlyURLSearchParams);

    render(<ContentClient />);

    expect(screen.getByTestId("content-header")).toHaveTextContent("Validated file");
    expect(screen.getByTestId("file-renderer")).toHaveTextContent("test-file.json");
  });

  it("handles empty search params", () => {
    const mockSearchParams = new URLSearchParams();
    mockUseSearchParams.mockReturnValue(mockSearchParams as ReadonlyURLSearchParams);

    render(<ContentClient />);

    expect(screen.getByTestId("content-header")).toHaveTextContent("Validated file");
    expect(screen.getByTestId("file-renderer")).toHaveTextContent("");
  });
});
