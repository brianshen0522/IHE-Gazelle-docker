/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import MessageSidePanel from "./MessageSidePanel";
import { useSidePanel } from "@gazelle/gazelle-component-ui";

// Mock child components
vi.mock("@message-capture/components/proxy/messages/MessageOverview", () => ({
  default: () => React.createElement("div", { "data-testid": "message-overview" }),
}));
vi.mock("@message-capture/components/proxy/connections/ConnectionDetails", () => ({
  default: () => React.createElement("div", { "data-testid": "connection-details" }),
}));
vi.mock("@message-capture/components/proxy/connections/SenderReceiver", () => ({
  default: (props: { host: any }) => React.createElement("div", { "data-testid": `sender-receiver-${props.host}` }),
}));
vi.mock("@gazelle/gazelle-component-ui", () => {
  const MockSidePanel = ({ isOpen, children, className }: any) =>
    isOpen ? React.createElement("div", { className, "data-testid": "sidepanel" }, children) : null;

  MockSidePanel.Header = ({ accessDetailsProps }: any) =>
    React.createElement("div", { "data-testid": "side-panel-header", "data-pathname": accessDetailsProps.pathname });
  MockSidePanel.Section = ({ id, title, children }: any) =>
    React.createElement("section", { "data-testid": `side-panel-section-${id}`, "data-title": title }, children);

  return {
    SidePanel: MockSidePanel,
    useSidePanel: vi.fn(),
  };
});

// Mock useSidePanel
const mockSetIsOpen = vi.fn();
const mockSelectedRow = {
  id: "row1",
  references: [{ value: "conn1" }],
  content: {
    sender: { hostname: "sender-host", ip: "192.0.2.1", port: 1234 },
    receiver: { hostname: "receiver-host", ip: "198.51.100.1", port: 5678 },
    channelType: "TCP",
  },
};

describe("MessageSidePanel", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("does not render content when no row is selected", () => {
    (useSidePanel as any).mockReturnValue({
      selectedRow: undefined,
      isOpen: true,
      setIsOpen: mockSetIsOpen,
    });
    render(<MessageSidePanel />);
    expect(screen.getByTestId("sidepanel")).toBeInTheDocument();
    expect(screen.queryByTestId("side-panel-header")).not.toBeInTheDocument();
    expect(screen.queryByTestId("message-overview")).not.toBeInTheDocument();
  });

  it("renders all sections when a row is selected", () => {
    (useSidePanel as any).mockReturnValue({
      selectedRow: mockSelectedRow,
      isOpen: true,
      setIsOpen: mockSetIsOpen,
    });
    render(<MessageSidePanel />);
    expect(screen.getByTestId("sidepanel")).toBeInTheDocument();
    expect(screen.getByTestId("side-panel-header")).toBeInTheDocument();
    expect(screen.getByTestId("side-panel-header")).toHaveAttribute("data-pathname", "/message-capture/message");
    expect(screen.getByTestId("side-panel-section-TCP message")).toBeInTheDocument();
    expect(screen.getByTestId("message-overview")).toBeInTheDocument();
    expect(screen.getByTestId("side-panel-section-connection-details")).toBeInTheDocument();
    expect(screen.getByTestId("connection-details")).toBeInTheDocument();
    expect(screen.getByTestId("side-panel-section-sender")).toBeInTheDocument();
    expect(screen.getByTestId("sender-receiver-sender")).toBeInTheDocument();
    expect(screen.getByTestId("side-panel-section-receiver")).toBeInTheDocument();
    expect(screen.getByTestId("sender-receiver-receiver")).toBeInTheDocument();
  });

  it("passes correct props to MessageOverview", () => {
    (useSidePanel as any).mockReturnValue({
      selectedRow: mockSelectedRow,
      isOpen: true,
      setIsOpen: mockSetIsOpen,
    });
    render(<MessageSidePanel />);
    expect(screen.getByTestId("message-overview")).toBeInTheDocument();
  });

  it("side panel is not rendered when isOpen is false", () => {
    (useSidePanel as any).mockReturnValue({
      selectedRow: mockSelectedRow,
      isOpen: false,
      setIsOpen: mockSetIsOpen,
    });
    render(<MessageSidePanel />);
    expect(screen.queryByTestId("side-panel")).not.toBeInTheDocument();
  });
});
