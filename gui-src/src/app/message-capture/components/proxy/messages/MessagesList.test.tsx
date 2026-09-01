/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import MessagesList from "./MessagesList";
import { useSmallScreen } from "@gazelle/gazelle-component-ui";
import { beforeEach, describe, expect, it, Mock, vi } from "vitest";

// Mock necessary hooks and components
vi.mock("@gazelle/gazelle-component-ui", async () => {
  const actual = await vi.importActual<any>("@gazelle/gazelle-component-ui");
  return {
    ...actual,
    useSmallScreen: vi.fn(),
    useSidePanel: vi.fn(() => ({ setIsOpen: vi.fn() })),
    ScrollTop: () => <div data-testid="scroll-top" />,
  };
});
vi.mock("@message-capture/hooks/usePresentationSchemaUrl", () => ({ default: () => ({ presentationSchemaUrl: "" }) }));
vi.mock("@/shared/hooks/useSearchParamsUrl", () => ({
  useSearchParamsUrl: () => ({ searchParameters: {} }),
}));
vi.mock("@message-capture/components/proxy/messages/MessagesColumns", () => ({
  useMessagesColumns: () => [],
}));
vi.mock("@/shared/components/table/TablePaginationWrapper", () => ({
  default: (props: any) => <div data-testid="table-pagination-wrapper" data-param-prefix={props.paramPrefix} />,
}));
vi.mock("@/shared/components/filter/GenericFilters", () => ({ default: () => <div data-testid="generic-filters" /> }));
vi.mock("@message-capture/components/proxy/messages/MessageSidePanel", () => ({ default: () => <div data-testid="message-side-panel" /> }));
vi.mock("next/navigation", () => ({
  useSearchParams: () => ({}),
}));
vi.mock("react-i18next", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

describe("MessagesList", () => {
  beforeEach(() => {
    (useSmallScreen as unknown as Mock).mockReturnValue(false);
  });

  it("renders GenericFilters and TablePaginationWrapper", () => {
    render(<MessagesList />);
    expect(screen.getByTestId("generic-filters")).toBeInTheDocument();
    expect(screen.getByTestId("table-pagination-wrapper")).toBeInTheDocument();
  });

  it("passes paramPrefix prop as '_' to TablePaginationWrapper", () => {
    render(<MessagesList />);
    const wrapper = screen.getByTestId("table-pagination-wrapper");
    expect(wrapper.dataset.paramPrefix).toBe("_");
  });

  it("renders MessageSidePanel when not small screen", () => {
    render(<MessagesList />);
    expect(screen.getByTestId("message-side-panel")).toBeInTheDocument();
  });

  it("does not render MessageSidePanel on small screen", () => {
    (useSmallScreen as unknown as Mock).mockReturnValue(true);
    render(<MessagesList />);
    expect(screen.queryByTestId("message-side-panel")).not.toBeInTheDocument();
  });

  it("renders ScrollTop component", () => {
    render(<MessagesList />);
    expect(screen.getByTestId("scroll-top")).toBeInTheDocument();
  });
});
