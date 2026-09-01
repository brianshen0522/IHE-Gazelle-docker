import { ColumnDef } from "@tanstack/react-table";

export type TablePaginationProps<T> = {
  tableColumns: ColumnDef<T, any>[];
  /** @deprecated: For backward compatibility only, will be removed */
  baseUrl?: string;
  /** @deprecated: For backward compatibility only, will be removed */
  serverAction?: () => Promise<any>;
  /** @deprecated: For backward compatibility only, will be removed */
  apiFolder?: string;
  emptyDataMessage?: string;
  initialField?: string;
  initialSortOrder?: "asc" | "desc" | null;
  initialLimit?: number;
  loadMoreStep?: number;
  paramPrefix?: string;
  paramMap?: Partial<Record<"offset" | "limit" | "sortBy" | "sortOrder", string>>;
  searchParameters?: Record<string, string>;
  getRowId?: (row: T, index: number) => string;
  fields?: string[];
  // Temporary optional: type for generic API route (e.g., 'simulation', 'validation', 'test_execution')
  // Will be mandatory after migration when the generic /api/items?type=X route is used by all components
  type?: string;
  path?: string; // Optional path to append to backend URL (e.g., '/sessions/123/tests')
};

export interface TableProps<T> {
  data: T[];
  columns: ColumnDef<T, string>[];
  field: string;
  setField: (field: string) => void;
  sortOrder: "asc" | "desc" | null;
  setSortOrder: (order: "asc" | "desc" | null) => void;
  isValidating?: boolean;
  getRowId?: (row: T, index: number) => string;
}
