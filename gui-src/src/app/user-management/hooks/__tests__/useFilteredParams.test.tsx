/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from "@testing-library/react";
import { useFilteredParams } from "../useFilteredParams";
import { describe, it, beforeEach, vi, expect } from "vitest";

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useSearchParams: vi.fn(),
}));

import { useSearchParams } from "next/navigation";

describe("useFilteredParams", () => {
  const getMockSearchParams = (params: Record<string, string | null>) => ({
    get: (key: string) => params[key] ?? null,
  });

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns filteredParams with only non-empty values", () => {
    vi.mocked(useSearchParams).mockReturnValue(
      getMockSearchParams({
        search: "john",
        delegated: "",
        organizationId: "123",
        groupIds: null,
        activated: "true",
      }) as any
    );

    const { result } = renderHook(() => useFilteredParams());
    expect(result.current.filteredParams).toEqual({
      search: "john",
      organizationId: "123",
      activated: "true",
    });
  });

  it("returns empty object when all params are empty or null", () => {
    vi.mocked(useSearchParams).mockReturnValue(
      getMockSearchParams({
        search: "",
        delegated: "",
        organizationId: "",
        groupIds: null,
        activated: "",
      }) as any
    );

    const { result } = renderHook(() => useFilteredParams());
    expect(result.current.filteredParams).toEqual({});
  });

  it("returns all params if all are present and non-empty", () => {
    vi.mocked(useSearchParams).mockReturnValue(
      getMockSearchParams({
        search: "test",
        delegated: "yes",
        organizationId: "456",
        groupIds: "1,2,3",
        activated: "false",
      }) as any
    );

    const { result } = renderHook(() => useFilteredParams());
    expect(result.current.filteredParams).toEqual({
      search: "test",
      delegated: "yes",
      organizationId: "456",
      groupIds: "1,2,3",
      activated: "false",
    });
  });

  it("handles missing searchParams gracefully", () => {
    vi.mocked(useSearchParams).mockReturnValue(getMockSearchParams({}) as any);

    const { result } = renderHook(() => useFilteredParams());
    expect(result.current.filteredParams).toEqual({});
  });
});
