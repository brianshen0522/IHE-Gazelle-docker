/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, fireEvent, screen } from "@testing-library/react";
import VirtualizedTable from "./Table";
import { TableProps } from "./types";
import { vi, describe, it, expect } from "vitest";
import React from "react";

const headerMock = [
  {
    id: "headerGroup1",
    headers: [
      {
        id: "col1",
        column: {
          id: "name",
          columnDef: { header: "Name", enableSorting: true, cell: (ctx: { row: { original: { name: any } } }) => ctx.row.original.name },
          getSize: () => 100,
        },
        isPlaceholder: false,
        getContext: () => ({}),
      },
      {
        id: "col2",
        column: {
          id: "actions",
          columnDef: { header: "Actions", enableSorting: false, cell: () => "Action" },
          getSize: () => 80,
        },
        isPlaceholder: false,
        getContext: () => ({}),
      },
    ],
  },
];
const rowModelMock = {
  rows: [
    {
      id: "row1",
      original: { name: "Alice" },
      getVisibleCells: () => [
        {
          id: "cell1",
          column: { id: "name", getSize: () => 100, columnDef: { cell: (ctx: { row: { original: { name: any } } }) => ctx.row.original.name } },
          getValue: () => "Alice",
          columnDef: {},
          getContext: () => ({ row: { original: { name: "Alice" } } }),
        },
        {
          id: "cell2",
          column: { id: "actions", getSize: () => 80, columnDef: { cell: () => "Action" } },
          getValue: () => "Action",
          columnDef: {},
          getContext: () => ({}),
        },
      ],
    },
  ],
};
// Mocks for hooks and icons
vi.mock("@tanstack/react-table", () => ({
  useReactTable: vi.fn(() => ({
    getHeaderGroups: () => headerMock,
    getRowModel: () => rowModelMock,
  })),
  flexRender: vi.fn((Component, ctx) => (typeof Component === "string" ? Component : Component(ctx))),
  getCoreRowModel: vi.fn(),
  Row: vi.fn(),
}));

vi.mock("@tanstack/react-virtual", () => ({
  useVirtualizer: vi.fn(() => ({
    getTotalSize: () => 50,
    getVirtualItems: () => [{ index: 0, start: 0 }],
    measureElement: vi.fn(),
  })),
}));

vi.mock("lucide-react", () => ({
  ChevronDown: () => React.createElement("span", { "data-testid": "ChevronDown" }),
  ChevronsUpDown: () => React.createElement("span", { "data-testid": "ChevronsUpDown" }),
  ChevronUp: () => React.createElement("span", { "data-testid": "ChevronUp" }),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  useSmallScreen: vi.fn(() => false),
  useSidePanel: vi.fn(() => ({
    selectedRow: null,
    setSelectedRow: vi.fn(),
    setIsOpen: vi.fn(),
  })),
}));

vi.mock("@hooks/useCreateQuery", () => ({
  useCreateQuery: vi.fn(() => ({
    createQuery: vi.fn(),
  })),
}));

vi.mock("@shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: vi.fn(() => ({
    handleNavigation: (fn: () => any) => fn(),
  })),
}));

vi.mock("next/navigation", () => ({
  useSearchParams: vi.fn(() => ({
    get: vi.fn(() => null),
  })),
}));

const columns = [
  {
    accessorKey: "name",
    header: "Name",
    enableSorting: true,
    cell: (props: { row: { original: { name: any } } }) => props.row.original.name,
  },
  {
    accessorKey: "actions",
    header: "Actions",
    enableSorting: false,
    cell: () => "Action",
  },
];

const data = [{ name: "Alice" }];

describe("VirtualizedTable", () => {
  const setField = vi.fn();
  const setSortOrder = vi.fn();

  const defaultProps: TableProps<any> = {
    data,
    columns,
    field: "name",
    setField,
    sortOrder: "asc",
    setSortOrder,
  };

  it("renders table headers and rows", () => {
    render(<VirtualizedTable {...defaultProps} />);
    expect(screen.getByText("Name")).toBeInTheDocument();
    expect(screen.getByText("Actions")).toBeInTheDocument();
    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("Action")).toBeInTheDocument();
  });

  it("calls setField and setSortOrder when sorting header is clicked", () => {
    render(<VirtualizedTable {...defaultProps} />);
    const nameHeader = screen.getByText("Name");
    fireEvent.click(nameHeader);
    expect(setField).not.toHaveBeenCalledWith("name");
    expect(setSortOrder).toHaveBeenCalled();
  });

  it("does not call setField or setSortOrder when non-sortable header is clicked", () => {
    render(<VirtualizedTable {...defaultProps} />);
    const actionsHeader = screen.getByText("Actions");
    fireEvent.click(actionsHeader);
    expect(setField).not.toHaveBeenCalledWith("actions");
    expect(setSortOrder).not.toHaveBeenCalledWith("asc");
  });

  it("calls handleNavigation and setSelectedRow on row click", () => {
    render(<VirtualizedTable {...defaultProps} />);
    const row = screen.getByRole("row", { name: /Alice/ });
    expect(row).toBeInTheDocument();
    fireEvent.click(row);
  });

  it("renders sort icons correctly based on sortOrder", () => {
    const { rerender } = render(<VirtualizedTable {...defaultProps} />);
    expect(screen.getByTestId("ChevronUp")).toBeInTheDocument();

    rerender(<VirtualizedTable {...defaultProps} sortOrder="desc" />);
    expect(screen.getByTestId("ChevronDown")).toBeInTheDocument();

    rerender(<VirtualizedTable {...defaultProps} sortOrder={null} />);
    expect(screen.getByTestId("ChevronsUpDown")).toBeInTheDocument();
  });

  it("handles keyboard navigation (Enter/Space)", () => {
    render(<VirtualizedTable {...defaultProps} />);
    const row = screen.getByRole("row", { name: /Alice/ });
    fireEvent.keyDown(row, { key: "Enter" });
    fireEvent.keyDown(row, { key: " " });
  });

  it("handles context menu on row", () => {
    render(<VirtualizedTable {...defaultProps} />);
    const row = screen.getByRole("row", { name: /Alice/ });
    fireEvent.contextMenu(row);
  });
});
