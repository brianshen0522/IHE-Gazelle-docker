/* eslint-disable @typescript-eslint/no-explicit-any */
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import FileRenderer from "../FileRenderer";
import { useReportAssertions } from "@validation-portal/context/selectedAssertionContext";
import { AssertionReportDTO } from "@/shared/types/validation/types";
import { usePathname } from "next/navigation";
import { useDatahouseAttachment } from "@hooks/useDatahouseAttachment";
import { getLineNumberFromSubjectLocation } from "../utils/getLineNumberFromSubjectLocation";
import { JsonRenderer } from "@/shared/components/renderers/json/JsonRenderer";
import { inspectForRendering } from "@/shared/utils/fileInspection/detectContent";
import type { TestResult } from "@maestro/types/report/Result";

// Mock hooks and components
vi.mock("@validation-portal/context/selectedAssertionContext", () => ({
  useReportAssertions: vi.fn(),
}));

vi.mock("next/navigation", () => ({
  usePathname: vi.fn(),
}));

vi.mock("@hooks/useDatahouseAttachment", () => ({
  useDatahouseAttachment: vi.fn(),
}));

vi.mock("@shared/components/renderers/json/JsonRenderer", () => ({
  JsonRenderer: vi.fn(({ base64Data }: { base64Data: string; linesProperties: any[] }) => <div data-testid="json-renderer">{base64Data}</div>),
}));

vi.mock("@shared/components/renderers/xml/XmlRenderer", () => ({
  XmlRenderer: ({ base64Data }: { base64Data: string }) => <div data-testid="xml-renderer">{base64Data}</div>,
}));

vi.mock("@shared/components/renderers/raw/RawRenderer", () => ({
  RawRenderer: ({ base64Data }: { base64Data: string }) => <div data-testid="raw-renderer">{base64Data}</div>,
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Skeleton: () => <div data-testid="skeleton" />,
}));

vi.mock("../../../types/ValidationReportDTO", () => ({
  isEqualAssertion: vi.fn(),
}));

vi.mock("../utils/getLineNumberFromSubjectLocation", () => ({
  getLineNumberFromSubjectLocation: vi.fn(),
}));

vi.mock("@shared/utils/fileInspection/detectContent", () => ({
  inspectForRendering: vi.fn(),
}));

vi.mock("../utils/getColorToHighlight", () => ({
  getColorToHighlight: vi.fn(),
}));

describe("FileRenderer", () => {
  const validJsonBase64 = btoa('{"hello":"world"}');
  const validXmlBase64 = btoa('<?xml version="1.0"?><root><child>value</child></root>');
  const validCdaBase64 = btoa('<?xml version="1.0"?><ClinicalDocument xmlns="urn:hl7-org:v3"><title>Test CDA</title></ClinicalDocument>');
  const mockUseReportAssertions = vi.mocked(useReportAssertions);
  const mockUsePathname = vi.mocked(usePathname);
  const mockUseDatahouseAttachment = vi.mocked(useDatahouseAttachment);
  const mockInspectForRendering = vi.mocked(inspectForRendering);
  const mockJsonRenderer = vi.mocked(JsonRenderer);

  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("open", vi.fn());
    mockUseReportAssertions.mockReturnValue({
      selectedAssertion: undefined,
      setSelectedAssertion: vi.fn(),
      assertionsWithLocation: [],
      setAssertionsWithLocation: function (): void {
        throw new Error("Function not implemented.");
      },
    });

    mockUsePathname.mockReturnValue("/some-other-page");
    mockUseDatahouseAttachment.mockReturnValue({
      data: validJsonBase64,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });
    mockInspectForRendering.mockReturnValue({ renderer: "JSON", base64Data: validJsonBase64 });
  });

  it("shows loading skeleton when isLoading is true", () => {
    mockUseDatahouseAttachment.mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });

    render(<FileRenderer fileName="test.json" itemId="item1" attachmentId="attach1" />);

    expect(screen.getByTestId("skeleton")).toBeInTheDocument();
  });

  it("shows no content message when no data", () => {
    mockUseDatahouseAttachment.mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });

    render(<FileRenderer fileName="test.json" itemId="item1" attachmentId="attach1" />);

    expect(screen.getByText("No content available")).toBeInTheDocument();
  });

  it("renders JSON renderer for JSON files", () => {
    render(<FileRenderer fileName="test.json" itemId="item1" attachmentId="attach1" />);

    expect(screen.getByTestId("json-renderer")).toHaveTextContent(validJsonBase64);
  });

  it("renders XML renderer for XML files", () => {
    mockUseDatahouseAttachment.mockReturnValue({
      data: validXmlBase64,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });
    mockInspectForRendering.mockReturnValue({ renderer: "XML", base64Data: validXmlBase64, dataType: "HTTP" });

    render(<FileRenderer fileName="test.xml" itemId="item1" attachmentId="attach1" />);

    expect(screen.getByTestId("xml-renderer")).toHaveTextContent(validXmlBase64);
  });

  it("renders raw renderer for other files", () => {
    mockInspectForRendering.mockReturnValue({ renderer: "RAW", base64Data: validJsonBase64 });

    render(<FileRenderer fileName="test.txt" itemId="item1" attachmentId="attach1" />);

    expect(screen.getByTestId("raw-renderer")).toHaveTextContent(validJsonBase64);
  });

  it("shows open in new tab link when not on content page", () => {
    render(<FileRenderer fileName="test.json" itemId="item1" attachmentId="attach1" />);

    expect(screen.getByText("Open in a new tab")).toBeInTheDocument();
  });

  it("hides open in new tab link when on content page", () => {
    mockUsePathname.mockReturnValue("/validation-portal/content");

    render(<FileRenderer fileName="test.json" itemId="item1" attachmentId="attach1" />);

    expect(screen.queryByText("Open in a new tab")).not.toBeInTheDocument();
  });

  it("uses provided content over fetched data", () => {
    render(<FileRenderer fileName="test.json" content={validJsonBase64} itemId="item1" />);

    expect(screen.getByTestId("json-renderer")).toHaveTextContent(validJsonBase64);
  });

  it("shows the display with stylesheet action for CDA files", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: { get: vi.fn().mockReturnValue("HIT") },
    });
    vi.stubGlobal("fetch", fetchMock);
    mockUseDatahouseAttachment.mockReturnValue({
      data: validCdaBase64,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });
    mockInspectForRendering.mockReturnValue({ renderer: "XML", base64Data: validCdaBase64, dataType: "HTTP" });

    render(<FileRenderer fileName="test.xml" itemId="item1" attachmentId="attach1" inputId="input-1" />);

    await waitFor(() => expect(screen.getByText("Display with stylesheet")).toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, expect.stringContaining("/validation-portal/api/styled-cda?"), { method: "HEAD" });
  });

  it("shows generating state during first styled CDA render and marks ready only on success", async () => {
    let resolveFetch: ((value: { ok: boolean; status: number }) => void) | undefined;
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: { get: vi.fn().mockReturnValue("MISS") },
      })
      .mockImplementationOnce(() => new Promise((resolve) => {
        resolveFetch = resolve;
      }));
    vi.stubGlobal("fetch", fetchMock);
    mockUseDatahouseAttachment.mockReturnValue({
      data: validCdaBase64,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });
    mockInspectForRendering.mockReturnValue({ renderer: "XML", base64Data: validCdaBase64, dataType: "HTTP" });

    render(<FileRenderer fileName="test.xml" itemId="item1" attachmentId="attach1" inputId="input-1" />);

    await waitFor(() => expect(screen.getByRole("button", { name: "Display with stylesheet" })).not.toBeDisabled());

    const actionButton = screen.getByRole("button", { name: "Display with stylesheet" });
    fireEvent.click(actionButton);

    await waitFor(() => expect(screen.getByRole("button", { name: "Generating" })).toBeDisabled());

    resolveFetch?.({
      ok: true,
      status: 200,
    });

    await waitFor(() => expect(screen.getByRole("button", { name: "Display with stylesheet" })).toBeInTheDocument());
    expect(screen.getByRole("button", { name: "Display with stylesheet" })).not.toBeDisabled();
    expect(fetchMock).toHaveBeenNthCalledWith(1, expect.stringContaining("/validation-portal/api/styled-cda?"), { method: "HEAD" });
    expect(fetchMock).toHaveBeenNthCalledWith(2, expect.stringContaining("/validation-portal/api/styled-cda?"));
    expect(globalThis.open).not.toHaveBeenCalled();
  });

  it("marks the styled CDA as ready during initial cache check when a cached version already exists", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: { get: vi.fn().mockReturnValue("HIT") },
    });
    vi.stubGlobal("fetch", fetchMock);
    mockUseDatahouseAttachment.mockReturnValue({
      data: validCdaBase64,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });
    mockInspectForRendering.mockReturnValue({ renderer: "XML", base64Data: validCdaBase64, dataType: "HTTP" });

    render(<FileRenderer fileName="test.xml" itemId="item1" attachmentId="attach1" inputId="input-1" />);

    await waitFor(() => expect(screen.getByRole("button", { name: "Display with stylesheet" })).not.toBeDisabled());
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenNthCalledWith(1, expect.stringContaining("/validation-portal/api/styled-cda?"), { method: "HEAD" });
    expect(globalThis.open).not.toHaveBeenCalled();
  });

  it("opens the styled CDA on manual click after the initial cache check marks it ready", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: { get: vi.fn().mockReturnValue("HIT") },
    });
    vi.stubGlobal("fetch", fetchMock);
    mockUseDatahouseAttachment.mockReturnValue({
      data: validCdaBase64,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });
    mockInspectForRendering.mockReturnValue({ renderer: "XML", base64Data: validCdaBase64, dataType: "HTTP" });

    render(<FileRenderer fileName="test.xml" itemId="item1" attachmentId="attach1" inputId="input-1" />);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    await waitFor(() => expect(globalThis.open).not.toHaveBeenCalled());

    fireEvent.click(screen.getByRole("button", { name: "Display with stylesheet" }));

    expect(globalThis.open).toHaveBeenCalledTimes(1);
    expect(globalThis.open).toHaveBeenNthCalledWith(1, expect.stringContaining("/validation-portal/api/styled-cda?"), "_blank", "noopener,noreferrer");
  });

  it("keeps the action retryable when styled CDA rendering fails", async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: { get: vi.fn().mockReturnValue("MISS") },
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
      });
    vi.stubGlobal("fetch", fetchMock);
    mockUseDatahouseAttachment.mockReturnValue({
      data: validCdaBase64,
      isLoading: false,
      isError: false,
      mutate: function (): void {
        throw new Error("Function not implemented.");
      },
    });
    mockInspectForRendering.mockReturnValue({ renderer: "XML", base64Data: validCdaBase64, dataType: "HTTP" });

    render(<FileRenderer fileName="test.xml" itemId="item1" attachmentId="attach1" inputId="input-1" />);

    await waitFor(() => expect(screen.getByRole("button", { name: "Display with stylesheet" })).not.toBeDisabled());
    fireEvent.click(screen.getByRole("button", { name: "Display with stylesheet" }));

    await waitFor(() => expect(screen.getByRole("button", { name: "Display with stylesheet" })).toBeInTheDocument());
    expect(screen.getByRole("button", { name: "Display with stylesheet" })).not.toBeDisabled();
    expect(globalThis.open).not.toHaveBeenCalled();
  });

  it("builds line properties from assertions", () => {
    const mockAssertion: AssertionReportDTO = {
      subjectLocations: [{ type: "line-column", value: "line 5" }],
      severity: "ERROR",
      result: "FAILED" as TestResult,
      requirementIDs: ["req1"],
      unexpectedErrors: [{ name: "error", message: "error1" }],
    };
    mockUseReportAssertions.mockReturnValue({
      selectedAssertion: undefined,
      setSelectedAssertion: vi.fn(),
      assertionsWithLocation: [mockAssertion],
      setAssertionsWithLocation: function (): void {
        throw new Error("Function not implemented.");
      },
    });

    // Mock getLineNumberFromSubjectLocation to return 5
    vi.mocked(getLineNumberFromSubjectLocation).mockReturnValue(5);

    render(<FileRenderer fileName="test.json" itemId="item1" attachmentId="attach1" />);

    expect(mockJsonRenderer).toHaveBeenCalledWith(
      {
        base64Data: validJsonBase64,
        dataType: "HTTP",
        linesProperties: [
          {
            lineNumber: 5,
            severity: "ERROR",
            color: undefined,
            onClickHandler: expect.any(Function),
            selected: false,
          },
        ],
      },
      undefined,
    );
  });
});
