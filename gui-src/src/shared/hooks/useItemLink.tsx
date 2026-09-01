import { Route } from "next";

/**
 * Item type constants that match the backend values
 */
export const ITEM_TYPE = {
  TEST_REPORT: "TEST_REPORT",
  VALIDATION_REPORT: "VALIDATION_REPORT",
  SIMULATION_REPORT: "SIMULATION_REPORT",
} as const;

export type ItemType = (typeof ITEM_TYPE)[keyof typeof ITEM_TYPE];

/**
 * Configuration for item type to portal path mapping
 */
interface ItemTypeConfig {
  portalPath: string;
  displayName: string;
}

/**
 * Mapping of item types to their portal configurations
 * This can be easily extended for new item types in the future
 */
const ITEM_TYPE_CONFIG: Record<string, ItemTypeConfig> = {
  [ITEM_TYPE.VALIDATION_REPORT]: {
    portalPath: "/validation-portal/reports",
    displayName: "Validation Report",
  },
  [ITEM_TYPE.TEST_REPORT]: {
    portalPath: "/test-execution/reports",
    displayName: "Test Report",
  },
  [ITEM_TYPE.SIMULATION_REPORT]: {
    portalPath: "/simulation-portal/reports",
    displayName: "Simulation Report",
  },
};

/**
 * Input for constructing a datahouse item link
 * This is a generic interface that any portal can use
 */
export interface DatahouseItemLinkInput {
  reference?: string | null;
  itemType?: string | null;
  displayName?: string | null;
}

/**
 * Result of constructing an item link
 */
export interface ItemLinkResult {
  path: Route | null;
  itemId: string | null;
  itemType: string | null;
  displayName: string | null;
  hasReference: boolean;
}

/**
 * Hook to construct URLs from datahouse item references based on itemType
 *
 * This hook is domain-agnostic and can be used across all portals
 * (test-execution, validation-portal, simulation-portal, etc.)
 *
 */
export function useItemLink(currentReportId?: string) {
  /**
   * Constructs a link result from a datahouse item reference
   */
  const getItemLink = (input: DatahouseItemLinkInput): ItemLinkResult => {
    // Check if input has a reference
    if (!input.reference) {
      return {
        path: null,
        itemId: null,
        itemType: input.itemType || null,
        displayName: input.displayName || null,
        hasReference: false,
      };
    }

    // Extract item ID from reference URL
    // Reference format: "http://datahouse/rest/v1/items/{itemId}"
    const itemId = extractItemIdFromReference(input.reference);
    if (!itemId) {
      return {
        path: null,
        itemId: null,
        itemType: input.itemType || null,
        displayName: input.displayName || null,
        hasReference: true,
      };
    }

    // Get configuration for this item type
    const itemType = input.itemType;
    const config = itemType ? ITEM_TYPE_CONFIG[itemType] : null;

    if (!config) {
      // Unknown item type - return basic info without path
      return {
        path: null,
        itemId,
        itemType: itemType || null,
        displayName: input.displayName || null,
        hasReference: true,
      };
    }

    // Construct the full path
    const queryParams = currentReportId ? `?previousReportId=${currentReportId}` : "";
    const path = `${config.portalPath}/${itemId}${queryParams}` as Route;

    return {
      path,
      itemId,
      itemType: itemType || null,
      displayName: input.displayName || config.displayName,
      hasReference: true,
    };
  };

  return getItemLink;
}

/**
 * Extracts the item ID from a datahouse reference URL
 * @param reference - The reference URL
 * @returns The item ID or null if not found
 */
function extractItemIdFromReference(reference: string): string | null {
  if (!reference) return null;

  // Handle both full URLs and relative paths
  // Examples:
  // - "http://datahouse/rest/v1/items/abc123"
  // - "/datahouse/rest/v1/items/abc123"
  // - "items/abc123"

  const parts = reference.split("/");
  const itemId = parts.at(-1);

  return itemId && itemId.trim() !== "" ? itemId : null;
}

/**
 * Helper function to check if an item type is supported
 */
export function isSupportedItemType(itemType: string | undefined | null): boolean {
  if (!itemType) return false;
  return itemType in ITEM_TYPE_CONFIG;
}

/**
 * Helper function to get display name for an item type
 */
export function getItemTypeDisplayName(itemType: string | undefined | null): string | null {
  if (!itemType || !ITEM_TYPE_CONFIG[itemType]) return null;
  return ITEM_TYPE_CONFIG[itemType].displayName;
}
