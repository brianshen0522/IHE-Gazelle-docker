/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from "@testing-library/react";
import { useMessagesColumns } from "./MessagesColumns";
import { ProxyMessages } from "@/app/message-capture/components/proxy/Types";
import { describe, expect, it, vi } from "vitest";

// Mock dependencies
vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));
vi.mock("@message-capture/hooks/useMsgTypeAccessor", () => ({
  useMsgTypeAccessor: () => (row: ProxyMessages) => row.type,
}));
vi.mock("@/shared/hooks/useDateFormat", () => ({
  default: () => (date: string) => `formatted-${date}`,
}));
vi.mock("@gazelle/gazelle-component-ui", () => ({
  AccessDetailsLink: ({ id }: { id: string }) => <div>AccessDetailsLink-{id}</div>,
}));

describe("MessagesColumns", () => {
  it("useMessagesColumns returns correct columns", () => {
    const { result } = renderHook(() => useMessagesColumns());
    const columns = result.current;
    expect(Array.isArray(columns)).toBe(true);
    expect(columns.length).toBeGreaterThan(0);
    // Check column ids
    const ids = columns.map((col) => col.id);
    expect(ids).toContain("channel_type");
    expect(ids).toContain("capture_date");
    expect(ids).toContain("sender_hostname");
    expect(ids).toContain("proxy_port");
    expect(ids).toContain("receiver_hostname");
    expect(ids).toContain("message_type");
    expect(ids).toContain("action");
  });

  it("channel_type column renders secured protocol", () => {
    const { result } = renderHook(() => useMessagesColumns());
    const columns = result.current;
    const channelTypeCol = columns.find((col) => col.id === "channel_type");
    const row = {
      original: {
        content: {
          channelType: "HTTP",
          secured: true,
          mtls: true,
        },
      },
    };
    const info = {
      getValue: () => "HTTP",
      row,
    } as any;
    const cell = (channelTypeCol as any).cell(info);
    expect(cell.props.children[2].props.title).toContain("mTLS");
  });

  it("capture_date column formats date", () => {
    const { result } = renderHook(() => useMessagesColumns());
    const columns = result.current;
    const captureDateCol = columns.find((col) => col.id === "capture_date");
    const info = { getValue: () => "2024-01-01" } as any;
    expect((captureDateCol as any).cell(info)).toBe("formatted-2024-01-01");
  });

  it("message_type column renders error style", () => {
    const { result } = renderHook(() => useMessagesColumns());
    const columns = result.current;
    const messageTypeCol = columns.find((col) => col.id === "message_type");
    const info = {
      getValue: () => "Validation failed",
      row: {
        original: {
          content: {
            type: "HTTP_MESSAGE",
            additionalParameters: { message_type: "Validation failed" },
          },
        },
      },
    } as any;
    const cell = (messageTypeCol as any).cell(info);
    expect(cell.props.className).toContain("text-red");
  });

  it("action column renders AccessDetailsLink", () => {
    const { result } = renderHook(() => useMessagesColumns());
    const columns = result.current;
    const actionCol = columns.find((col) => col.id === "action");
    const info = {
      row: {
        original: {
          id: "123",
          references: [{ value: "abc" }],
        },
      },
    } as any;
    const cell = (actionCol as any).cell(info);
    expect(cell.props.children.props.id).toBe("123");
  });
});
