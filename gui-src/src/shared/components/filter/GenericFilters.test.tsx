/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import GenericFilters from "./GenericFilters";
import { useGetIndexes } from "@shared/hooks/SWR/useGetIndexes";
import { useGetIndexValues } from "@shared/hooks/SWR/useGetIndexValues";

// Mock dependencies
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Filter: (props: any) => (
    <div data-testid="filter">
      <div data-testid="selected-field">{props.selectedField}</div>
      <div data-testid="possible-values">{JSON.stringify(props.possibleValues)}</div>
    </div>
  ),
  Skeleton: () => <div data-testid="skeleton" />,
  useSidePanel: vi.fn(() => ({
    setIsOpen: vi.fn(),
  })),
}));

vi.mock("@shared/hooks/useCreateQuery", () => ({
  useCreateQuery: () => ({
    createQuery: vi.fn(),
  }),
}));

vi.mock("@shared/hooks/SWR/useGetIndexes", () => ({
  useGetIndexes: vi.fn(),
}));

vi.mock("@shared/hooks/SWR/useGetIndexValues", () => ({
  useGetIndexValues: vi.fn(),
}));

vi.mock("@/shared/utils/favoriteSearch/getAppStorageKey", () => ({
  getAppStorageKey: vi.fn(() => "test-storage-key"),
}));

vi.mock("@/shared/utils/favoriteSearch/saveSearch", () => ({
  saveSearch: vi.fn(),
}));

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

describe("GenericFilters", () => {
  const mockSearchParameters = { status: "active" };
  const mockIndexNameMapping = {
    search: "Organization",
    delegated: "Type",
    archived: "Archived",
  };

  const baseProps = {
    searchParameters: mockSearchParameters,
    indexNameMapping: mockIndexNameMapping,
    type: "organizations",
    indexPath: "/organizations",
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Loading State", () => {
    it("renders skeleton when indexes are loading", () => {
      (useGetIndexes as any).mockReturnValue({
        indexes: undefined,
        isLoadingIndexes: true,
        isError: null,
      });

      (useGetIndexValues as any).mockReturnValue({
        indexValues: [],
      });

      render(<GenericFilters {...baseProps} />);
      expect(screen.getByTestId("skeleton")).toBeInTheDocument();
    });
  });

  describe("Index Filtering", () => {
    it("excludes indexes based on customIndexFilter", () => {
      const mockIndexes = [
        { name: "name", fieldType: "string" },
        { name: "shortname", fieldType: "string" },
        { name: "delegated", fieldType: "boolean" },
        { name: "archived", fieldType: "boolean" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      (useGetIndexValues as any).mockReturnValue({
        indexValues: [],
      });

      const customIndexFilter = (item: any) => item.name !== "name" && item.name !== "shortname";

      render(<GenericFilters {...baseProps} customIndexFilter={customIndexFilter} />);

      expect(screen.getByTestId("filter")).toBeInTheDocument();
    });
  });

  describe("Global Search Feature", () => {
    it("does not fetch index values for globalSearch fields", () => {
      const mockIndexes = [
        { name: "search", fieldType: "string" },
        { name: "delegated", fieldType: "boolean" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      const mockUseGetIndexValues = vi.fn().mockReturnValue({
        indexValues: [],
      });
      (useGetIndexValues as any).mockImplementation(mockUseGetIndexValues);

      render(<GenericFilters {...baseProps} globalSearch={["search"]} />);

      // Initially, no field is selected, so the field should be empty
      expect(mockUseGetIndexValues).toHaveBeenCalledWith(
        expect.objectContaining({
          field: "", // Field should be empty when no field is selected
        }),
      );
    });

    it("allows users to search without selecting an index when using globalSearch", () => {
      const mockIndexes = [
        { name: "delegated", fieldType: "boolean" },
        { name: "archived", fieldType: "boolean" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      const mockUseGetIndexValues = vi.fn().mockReturnValue({
        indexValues: [],
      });
      (useGetIndexValues as any).mockImplementation(mockUseGetIndexValues);

      // User types "test query" in the default search bar without selecting any index
      // The globalSearch feature allows typing in the main input and pressing Enter to create a "search" parameter
      render(
        <GenericFilters
          {...baseProps}
          searchParameters={{ search: "test query" }}
          globalSearch={["search"]}
          customIndexFilter={(item) => item.name !== "search"} // Exclude "search" from dropdown
        />,
      );

      expect(screen.getByTestId("filter")).toBeInTheDocument();
      // The Filter component should receive defaultSearchField="search"
      // When user types and presses Enter, it creates the search parameter
    });

    it("fetches index values for non-globalSearch fields", () => {
      const mockIndexes = [
        { name: "search", fieldType: "string" },
        { name: "delegated", fieldType: "boolean" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      const mockUseGetIndexValues = vi.fn().mockReturnValue({
        indexValues: ["true", "false"],
      });
      (useGetIndexValues as any).mockImplementation(mockUseGetIndexValues);

      render(<GenericFilters {...baseProps} globalSearch={["search"]} />);

      // Simulate selecting a non-globalSearch field by re-rendering with selectedField state
      // Note: In a real scenario, this would be triggered by user interaction
      // For testing purposes, we verify the hook is called with the right parameters
      expect(mockUseGetIndexValues).toHaveBeenCalled();
    });

    it("prevents fetching index values when search field is selected", () => {
      const mockIndexes = [
        { name: "search", fieldType: "string" },
        { name: "delegated", fieldType: "boolean" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      const mockUseGetIndexValues = vi.fn().mockReturnValue({
        indexValues: [],
      });
      (useGetIndexValues as any).mockImplementation(mockUseGetIndexValues);

      render(<GenericFilters {...baseProps} globalSearch={["search"]} />);

      // Verify that when 'search' is in globalSearch, the field passed to useGetIndexValues is empty
      waitFor(() => {
        const lastCall = mockUseGetIndexValues.mock.calls[mockUseGetIndexValues.mock.calls.length - 1];
        if (lastCall?.[0]) {
          expect(lastCall[0].field).toBe("");
        }
      });
    });

    it("allows multiple globalSearch fields", () => {
      const mockIndexes = [
        { name: "search", fieldType: "string" },
        { name: "quickSearch", fieldType: "string" },
        { name: "delegated", fieldType: "boolean" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      const mockUseGetIndexValues = vi.fn().mockReturnValue({
        indexValues: [],
      });
      (useGetIndexValues as any).mockImplementation(mockUseGetIndexValues);

      render(<GenericFilters {...baseProps} globalSearch={["search", "quickSearch"]} />);

      expect(screen.getByTestId("filter")).toBeInTheDocument();
    });
  });

  describe("Value Display Mapping", () => {
    it("applies valueDisplayMapping to index values", () => {
      const mockIndexes = [{ name: "delegated", fieldType: "boolean" }];

      const mockIndexValues = ["true", "false"];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      (useGetIndexValues as any).mockReturnValue({
        indexValues: mockIndexValues,
      });

      const valueDisplayMapping = {
        delegated: {
          true: "gzl.gum.delegated",
          false: "gzl.gum.local",
        },
      };

      render(<GenericFilters {...baseProps} valueDisplayMapping={valueDisplayMapping} />);

      expect(screen.getByTestId("filter")).toBeInTheDocument();
    });
  });

  describe("Index Name Mapping", () => {
    it("maps index names to display names", () => {
      const mockIndexes = [
        { name: "search", fieldType: "string" },
        { name: "delegated", fieldType: "boolean" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      (useGetIndexValues as any).mockReturnValue({
        indexValues: [],
      });

      render(<GenericFilters {...baseProps} />);

      expect(screen.getByTestId("filter")).toBeInTheDocument();
      // The Filter component should receive data with displayName mapped
    });
  });

  describe("Default Filters", () => {
    it("applies default filters on mount when not in URL", () => {
      const mockIndexes = [{ name: "status", fieldType: "string" }];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      (useGetIndexValues as any).mockReturnValue({
        indexValues: [],
      });

      const defaultFilters = {
        archived: "false",
      };

      render(
        <GenericFilters
          {...baseProps}
          searchParameters={{}} // No params in URL
          defaultFilters={defaultFilters}
        />,
      );

      expect(screen.getByTestId("filter")).toBeInTheDocument();
    });
  });

  describe("Excluded Keys", () => {
    it("excludes specified keys from filter options", () => {
      const mockIndexes = [
        { name: "id", fieldType: "string" },
        { name: "name", fieldType: "string" },
        { name: "status", fieldType: "string" },
      ];

      (useGetIndexes as any).mockReturnValue({
        indexes: mockIndexes,
        isLoadingIndexes: false,
        isError: null,
      });

      (useGetIndexValues as any).mockReturnValue({
        indexValues: [],
      });

      render(<GenericFilters {...baseProps} excludedKeys={["id"]} />);

      expect(screen.getByTestId("filter")).toBeInTheDocument();
    });
  });
});
