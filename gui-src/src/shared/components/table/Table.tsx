"use client";
import { useEffect, useRef, useState } from "react";
import { Row, flexRender, useReactTable, getCoreRowModel } from "@tanstack/react-table";
import { useVirtualizer } from "@tanstack/react-virtual";
import { ChevronDown, ChevronsUpDown, ChevronUp } from "lucide-react";
import { useSmallScreen, useSidePanel } from "@gazelle/gazelle-component-ui";
import { useCreateQuery } from "@hooks/useCreateQuery";
import { useSearchParams } from "next/navigation";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";
import { TableProps } from "./types";
import { useTranslation } from "react-i18next";

function VirtualizedTable<T>({ data, columns, field, setField, sortOrder, setSortOrder, isValidating, getRowId }: Readonly<TableProps<T>>) {
  const { t } = useTranslation();
  const isSmallScreen = useSmallScreen();
  const { setSelectedRow, setIsOpen } = useSidePanel<T>();
  const { createQuery } = useCreateQuery();
  const { handleNavigation } = useUnsavedChanges();
  const searchParams = useSearchParams();
  const [selectedRowId, setSelectedRowId] = useState<string | null>(null);

  const parentRef = useRef<HTMLTableSectionElement>(null);

  const table = useReactTable({
    data,
    columns,
    state: {},
    getCoreRowModel: getCoreRowModel(),
    getRowId,
  });

  // Virtualizer for tbody rows
  const rowVirtualizer = useVirtualizer({
    count: table.getRowModel().rows.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 60,
    overscan: 5,
    measureElement: (element) => element.getBoundingClientRect().height,
    enabled: true,
  });

  // Optional: select row based on URL param
  useEffect(() => {
    const selectedRowIndex = searchParams.get("row");
    if (selectedRowIndex) {
      setSelectedRowId(selectedRowIndex);
      const row: Row<T> | undefined = table.getRowModel().rows.find((r) => r.id === selectedRowIndex);
      if (row) {
        setSelectedRow(row.original);
        if (!isSmallScreen) setIsOpen(true);
      }
    }
  }, [table, searchParams, isSmallScreen, setSelectedRow, setIsOpen]);

  const descSortOrder = sortOrder === "desc" ? <ChevronDown size={14} /> : <ChevronsUpDown size={14} />;
  const sortOrderFn = sortOrder === "asc" ? <ChevronUp size={14} /> : descSortOrder;

  return (
    <div className="w-full rounded-xl border border-lightpurple shadow-lg overflow-hidden">
      <div ref={parentRef} className="w-full max-h-[90vh] overflow-auto scrollbar-rounded">
        <table className="w-full bg-white">
          <thead className="sticky top-0 z-20 shadow-[0_1px_0_0_#b48ead] bg-white">
            {table.getHeaderGroups()?.map((headerGroup) => (
              <tr key={headerGroup.id} className="font-semibold">
                {headerGroup.headers?.map((header) => {
                  const colId = header.column.id;
                  const canSort = header.column.columnDef.enableSorting !== false;
                  const isActionColumn = colId.includes("action");
                  const isActive = field === colId;
                  return (
                    <th key={header.id} className="px-1 py-2 items-center text-left" style={{ minWidth: header.column.getSize() }}>
                      {header.isPlaceholder ? null : (
                        <button
                          className={`flex w-full items-center gap-1 select-none text-left ${
                            canSort ? "cursor-pointer" : "cursor-default"
                          } ${isActionColumn ? "justify-center" : ""}`}
                          type="button"
                          onClick={() => {
                            if (!canSort) return;
                            if (field === colId) {
                              const AscSortOrder = sortOrder === "asc" ? "desc" : null;
                              setSortOrder(sortOrder === null ? "asc" : AscSortOrder);
                            } else {
                              setField(colId);
                              setSortOrder("asc");
                            }
                          }}
                        >
                          {isValidating ? (
                            <span className="text-grey_disabled">{t("gzl.user.interface.loading")}</span>
                          ) : (
                            <>
                              {flexRender(header.column.columnDef.header, header.getContext())}
                              {canSort && (isActive ? sortOrderFn : <ChevronsUpDown size={14} />)}
                            </>
                          )}
                        </button>
                      )}
                    </th>
                  );
                })}
              </tr>
            ))}
          </thead>
          <tbody
            style={{
              // display:block so Safari honours position:relative and height here:
              // Safari ignores positioning on table-internal boxes (tbody/tr), which
              // otherwise lets the absolutely-positioned virtual rows escape the tbody
              // and overlap the content below the table.
              display: "block",
              position: "relative",
              height: `${rowVirtualizer.getTotalSize()}px`,
            }}
          >
            {rowVirtualizer.getVirtualItems().map((virtualRow) => {
              const row = table.getRowModel().rows[virtualRow.index];
              if (!row) return null;

              return (
                <tr
                  data-index={virtualRow.index} //needed for dynamic row height measurement
                  ref={(node) => rowVirtualizer.measureElement(node)} //measure dynamic row height
                  key={row.id}
                  style={{ transform: `translateY(${virtualRow.start}px)` }}
                  className={`absolute w-full flex cursor-pointer hover:bg-lightpurple/25 ${
                    row.id === selectedRowId ? "bg-lightpurple/25" : ""
                  } ${isValidating ? "pointer-events-none opacity-50" : ""}`}
                  tabIndex={0}
                  onClick={() => {
                    handleNavigation(() => {
                      setSelectedRowId(row.id);
                      setSelectedRow(row.original);
                      if (!isSmallScreen) setIsOpen(true);
                      createQuery({ row: row.id });
                    });
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") {
                      e.preventDefault();
                      handleNavigation(() => {
                        setSelectedRowId(row.id);
                        setSelectedRow(row.original);
                        if (!isSmallScreen) setIsOpen(true);
                        createQuery({ row: row.id });
                      });
                    }
                  }}
                  onContextMenu={() => {
                    handleNavigation(() => {
                      setSelectedRowId(row.id);
                      setSelectedRow(row.original);
                      if (!isSmallScreen) setIsOpen(true);
                      createQuery({ row: row.id });
                    });
                  }}
                >
                  {row.getVisibleCells()?.map((cell) => {
                    const isTags = cell.column.id === "tags";
                    return (
                      <td
                        key={cell.id}
                        className={`p-1 flex flex-1 items-center ${isTags ? "" : "overflow-hidden"} ${cell.column.id.includes("action") ? "justify-center" : ""}`}
                        style={{ minWidth: cell.column.getSize() }}
                        title={cell.getValue() ? String(cell.getValue()) : ""}
                      >
                        <div
                          className={isTags ? "w-full" : "text-ellipsis whitespace-nowrap overflow-hidden"}
                          onClick={cell.column.id === "actions" ? (e) => e.stopPropagation() : undefined}
                        >
                          {flexRender(cell.column.columnDef.cell, cell.getContext())}
                        </div>
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default VirtualizedTable;
