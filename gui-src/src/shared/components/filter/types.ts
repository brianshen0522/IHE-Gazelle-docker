export type IndexItem = {
  name: string;
  fieldType: string;
};

export type IndexWithDisplayName = IndexItem & { displayName: string };

export interface GenericFilterProps {
  /** Current search parameters from URL */
  searchParameters: Record<string, string>;

  /** Default filter values to apply on mount if not present in URL */
  defaultFilters?: Record<string, string>;

  /** Mapping of field names to their display labels (e.g., { organizationId: "Organization" }) */
  indexNameMapping: Record<string, string>;

  /** @deprecated Use 'type' instead. Legacy option for specifying backend URL or server action */
  fetchOption?: { baseUrl?: string } | { serverAction?: () => Promise<any> };
  // Optional separate fetch option for index values to be removed once all components are migrated to use the generic API route
  // currently needed to support components that use a custom API route for fetching index values (e.g., Message Capture with its /possibleValues route)
  /** @deprecated */
  indexValuesFetchOption?: { baseUrl?: string } | { serverAction?: (field: string, searchParameters: Record<string, string>) => Promise<any> };

  /** Custom function to filter which indexes should be available (e.g., exclude certain fields) */
  customIndexFilter?: (item: IndexItem) => boolean;

  /** List of field names to exclude from the filter dropdown */
  excludedKeys?: string[];

  /** Whether to show date range filter */
  isDateFilter?: boolean;

  /** Parameter name for date filter (default: "capture_date") */
  dateFilterParamName?: string;

  /** Display label for date filter */
  dateFilterLabel?: string;

  /** Field names that should not appear in the active filters display */
  hiddenActiveFilterKeys?: string[];

  /** Additional fields to add to the filter dropdown that aren't fetched from backend */
  additionalFields?: IndexItem[];

  /** Override possible values for specific fields instead of fetching from backend */
  possibleValuesOverride?: Record<string, string[]>;

  /**
   * Mapping to transform field values for display
   * Example: { organizationId: { "abc-123": "Organization Name" }, delegated: { "true": "Delegated", "false": "Local" } }
   * Used to show human-readable names instead of technical IDs
   */
  valueDisplayMapping?: Record<string, Record<string, string>>;

  /** Parameters that should persist across filter operations */
  persistentParams?: Record<string, string>;

  /**
   * Type identifier for the generic API route (e.g., 'users', 'organizations', 'simulation')
   * Used to determine which backend to query via /api/indexes?type=X
   */
  type?: string;

  /** Optional path to append to backend URL for nested resources (e.g., '/sessions/123/tests') */
  indexPath?: string;

  /** Field names that should not be cleared when clicking "Clear All" button (e.g., 'testsessionid') */
  excludedFromClear?: string[];

  /**
   * Field names that provide default free-text search without selecting an index (e.g., 'search')
   * These fields allow users to type directly in the search bar without selecting an index first.
   * They won't fetch predefined values and typically should be excluded from the index dropdown via customIndexFilter.
   */
  globalSearch?: string[];
}
