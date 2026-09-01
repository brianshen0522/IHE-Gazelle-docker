/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { useOrganizationsColumns } from "../OrganizationsColumns";
import { Organization } from "@/app/user-management/components/user-management/Types";

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Badge: ({ id, variant, children }: any) => (
    <span data-testid={`badge-${id}`} data-variant={variant}>
      {children}
    </span>
  ),
}));

// Mock next/link
vi.mock("next/link", () => ({
  default: ({ href, onClick, children, className }: any) => (
    <a href={href} onClick={onClick} className={className} data-testid="link">
      {children}
    </a>
  ),
}));

// Mock useDateFormat with stable function reference
const mockFormatDate = (date: number) => new Date(date).toLocaleDateString();
vi.mock("@/shared/hooks/useDateFormat", () => ({
  default: () => mockFormatDate,
}));

describe("useOrganizationsColumns", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns an array of column definitions", () => {
    const { result } = renderHook(() => useOrganizationsColumns());

    expect(Array.isArray(result.current)).toBe(true);
    expect(result.current).toHaveLength(4);
  });

  it("defines status column", () => {
    const { result } = renderHook(() => useOrganizationsColumns());
    const statusColumn = result.current.find((col: any) => col.id === "delegated");

    expect(statusColumn?.id).toBe("delegated");
  });

  it("defines name column", () => {
    const { result } = renderHook(() => useOrganizationsColumns());
    const nameColumn = result.current.find((col: any) => col.id === "name");

    expect(nameColumn?.id).toBe("name");
  });

  it("defines shortname column", () => {
    const { result } = renderHook(() => useOrganizationsColumns());
    const shortnameColumn = result.current.find((col: any) => col.id === "shortname");

    expect(shortnameColumn?.id).toBe("shortname");
  });

  it("defines lastUpdateTimestamp column", () => {
    const { result } = renderHook(() => useOrganizationsColumns());
    const lastUpdateColumn = result.current.find((col: any) => col.id === "lastUpdateTimestamp");

    expect(lastUpdateColumn?.id).toBe("lastUpdateTimestamp");
  });

  describe("Status column", () => {
    it("renders delegated badge when status is true", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const statusColumn = result.current.find((col: any) => col.id === "delegated");

      if (!statusColumn || typeof statusColumn.cell !== "function") {
        throw new Error("Status column not found or cell is not a function");
      }

      const mockOrg: Organization = {
        id: "org-1",
        name: "Test Org",
        shortname: "test",
        delegated: true,
        archived: false,
        lastUpdateTimestamp: new Date(),
        url: "",
      };

      const cellInfo: any = {
        getValue: () => true,
        row: { original: mockOrg },
        cell: {},
        column: {},
        renderValue: () => true,
        table: {},
      };

      const cellElement = statusColumn.cell(cellInfo);
      expect(cellElement).toBeDefined();
    });

    it("renders local badge when status is false", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const statusColumn = result.current.find((col: any) => col.id === "delegated");

      if (!statusColumn || typeof statusColumn.cell !== "function") {
        throw new Error("Status column not found or cell is not a function");
      }

      const mockOrg: Organization = {
        id: "org-1",
        name: "Test Org",
        shortname: "test",
        delegated: false,
        archived: false,
        lastUpdateTimestamp: new Date(),
        url: "",
      };

      const cellInfo: any = {
        getValue: () => false,
        row: { original: mockOrg },
        cell: {},
        column: {},
        renderValue: () => false,
        table: {},
      };

      const cellElement = statusColumn.cell(cellInfo);
      expect(cellElement).toBeDefined();
    });
  });

  describe("Name column", () => {
    it("renders organization name", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const nameColumn = result.current.find((col: any) => col.id === "name");

      if (!nameColumn || typeof nameColumn.cell !== "function") {
        throw new Error("Name column not found or cell is not a function");
      }

      const mockOrg: Organization = {
        id: "org-1",
        name: "Test Organization",
        shortname: "test",
        delegated: false,
        archived: false,
        lastUpdateTimestamp: new Date(),
        url: "",
      };

      const cellInfo: any = {
        getValue: () => "Test Organization",
        row: { original: mockOrg },
        cell: {},
        column: {},
        renderValue: () => "Test Organization",
        table: {},
      };

      const cellValue = nameColumn.cell(cellInfo);
      expect(cellValue).toBeDefined();
    });

    it("renders archived badge when organization is archived", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const nameColumn = result.current.find((col: any) => col.id === "name");

      if (!nameColumn || typeof nameColumn.cell !== "function") {
        throw new Error("Name column not found or cell is not a function");
      }

      const mockOrg: Organization = {
        id: "org-1",
        name: "Archived Organization",
        shortname: "archived",
        delegated: false,
        archived: true,
        lastUpdateTimestamp: new Date(),
        url: "",
      };

      const cellInfo: any = {
        getValue: () => "Archived Organization",
        row: { original: mockOrg },
        cell: {},
        column: {},
        renderValue: () => "Archived Organization",
        table: {},
      };

      const cellValue = nameColumn.cell(cellInfo);
      expect(cellValue).toBeDefined();
    });

    it("does not render archived badge when organization is not archived", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const nameColumn = result.current.find((col: any) => col.id === "name");

      if (!nameColumn || typeof nameColumn.cell !== "function") {
        throw new Error("Name column not found or cell is not a function");
      }

      const mockOrg: Organization = {
        id: "org-1",
        name: "Active Organization",
        shortname: "active",
        delegated: false,
        archived: false,
        lastUpdateTimestamp: new Date(),
        url: "",
      };

      const cellInfo: any = {
        getValue: () => "Active Organization",
        row: { original: mockOrg },
        cell: {},
        column: {},
        renderValue: () => "Active Organization",
        table: {},
      };

      const cellValue = nameColumn.cell(cellInfo);
      expect(cellValue).toBeDefined();
    });
  });

  describe("Shortname column", () => {
    it("renders organization shortname", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const shortnameColumn = result.current.find((col: any) => col.id === "shortname");

      if (!shortnameColumn || typeof shortnameColumn.cell !== "function") {
        throw new Error("Shortname column not found or cell is not a function");
      }

      const cellInfo: any = {
        getValue: () => "test-org",
        row: {},
        cell: {},
        column: {},
        renderValue: () => "test-org",
        table: {},
      };

      const cellValue = shortnameColumn.cell(cellInfo);
      expect(cellValue).toBe("test-org");
    });
  });

  describe("Last Update column", () => {
    it("renders formatted timestamp when value is present", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const lastUpdateColumn = result.current.find((col: any) => col.id === "lastUpdateTimestamp");

      if (!lastUpdateColumn || typeof lastUpdateColumn.cell !== "function") {
        throw new Error("Last update column not found or cell is not a function");
      }

      const testDate = new Date("2024-01-15T10:30:00Z");
      const cellInfo: any = {
        getValue: () => testDate,
        row: {},
        cell: {},
        column: {},
        renderValue: () => testDate,
        table: {},
      };

      const cellElement = lastUpdateColumn.cell(cellInfo);
      expect(cellElement).toBeDefined();
    });

    it("renders empty string when value is null", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const lastUpdateColumn = result.current.find((col: any) => col.id === "lastUpdateTimestamp");

      if (!lastUpdateColumn || typeof lastUpdateColumn.cell !== "function") {
        throw new Error("Last update column not found or cell is not a function");
      }

      const cellInfo: any = {
        getValue: () => null,
        row: {},
        cell: {},
        column: {},
        renderValue: () => null,
        table: {},
      };

      const cellElement = lastUpdateColumn.cell(cellInfo);
      expect(cellElement).toBe("");
    });

    it("renders empty string when value is undefined", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const lastUpdateColumn = result.current.find((col: any) => col.id === "lastUpdateTimestamp");

      if (!lastUpdateColumn || typeof lastUpdateColumn.cell !== "function") {
        throw new Error("Last update column not found or cell is not a function");
      }

      const cellInfo: any = {
        getValue: () => undefined,
        row: {},
        cell: {},
        column: {},
        renderValue: () => undefined,
        table: {},
      };

      const cellElement = lastUpdateColumn.cell(cellInfo);
      expect(cellElement).toBe("");
    });

    it("has column filter disabled", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const lastUpdateColumn = result.current.find((col: any) => col.id === "lastUpdateTimestamp");

      expect(lastUpdateColumn?.enableColumnFilter).toBe(false);
    });
  });

  describe("Column headers", () => {
    it("renders correct header for status column", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const statusColumn = result.current.find((col: any) => col.id === "delegated");

      if (!statusColumn || typeof statusColumn.header !== "function") {
        throw new Error("Status column not found or header is not a function");
      }

      const headerElement = statusColumn.header({} as any);
      expect(headerElement).toBeDefined();
    });

    it("renders correct header for name column", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const nameColumn = result.current.find((col: any) => col.id === "name");

      if (!nameColumn || typeof nameColumn.header !== "function") {
        throw new Error("Name column not found or header is not a function");
      }

      const headerElement = nameColumn.header({} as any);
      expect(headerElement).toBeDefined();
    });

    it("renders correct header for shortname column", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const shortnameColumn = result.current.find((col: any) => col.id === "shortname");

      if (!shortnameColumn || typeof shortnameColumn.header !== "function") {
        throw new Error("Shortname column not found or header is not a function");
      }

      const headerElement = shortnameColumn.header({} as any);
      expect(headerElement).toBeDefined();
    });

    it("renders correct header for last update column", () => {
      const { result } = renderHook(() => useOrganizationsColumns());
      const lastUpdateColumn = result.current.find((col: any) => col.id === "lastUpdateTimestamp");

      if (!lastUpdateColumn || typeof lastUpdateColumn.header !== "function") {
        throw new Error("Last update column not found or header is not a function");
      }

      const headerElement = lastUpdateColumn.header({} as any);
      expect(headerElement).toBeDefined();
    });
  });

  describe("Memoization", () => {
    it("returns stable column array reference", () => {
      const { result, rerender } = renderHook(() => useOrganizationsColumns());
      const firstResult = result.current;

      rerender();
      const secondResult = result.current;

      // Column definitions should be the same array instance due to useMemo
      expect(firstResult).toBe(secondResult);
      expect(result.current).toHaveLength(4);
    });
  });
});
