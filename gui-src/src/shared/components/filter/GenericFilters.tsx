"use client";
import { Suspense, useEffect, useState } from "react";
import { Filter, Skeleton, useSidePanel } from "@gazelle/gazelle-component-ui";
import { useCreateQuery } from "@shared/hooks/useCreateQuery";
import { useGetIndexes } from "@shared/hooks/SWR/useGetIndexes";
import { useGetIndexValues } from "@shared/hooks/SWR/useGetIndexValues";
import { getAppStorageKey } from "@/shared/utils/favoriteSearch/getAppStorageKey";
import { saveSearch } from "@/shared/utils/favoriteSearch/saveSearch";
import { useTranslation } from "react-i18next";
import DateFilterContent from "../dates/DateFilterContent";
import { GenericFilterProps, IndexItem, IndexWithDisplayName } from "./types";

const GenericFilters = ({
  searchParameters,
  defaultFilters = {},
  indexNameMapping,
  fetchOption,
  indexValuesFetchOption,
  customIndexFilter,
  excludedKeys,
  isDateFilter = false,
  dateFilterParamName = "capture_date",
  dateFilterLabel,
  hiddenActiveFilterKeys = [],
  additionalFields = [],
  possibleValuesOverride = {},
  valueDisplayMapping = {},
  persistentParams = {},
  type = "",
  indexPath,
  excludedFromClear = [],
  globalSearch = [],
}: GenericFilterProps) => {
  const { t } = useTranslation();
  const { setIsOpen } = useSidePanel();
  const [selectedField, setSelectedField] = useState<string>("");
  const [likeSearchValue, setLikeSearchValue] = useState<string>("");
  const { createQuery } = useCreateQuery();

  // Reset LIKE search when the selected field changes
  useEffect(() => {
    setLikeSearchValue("");
  }, [selectedField]);

  // TODO: Remove fetchOption prop once all components are migrated
  // After migration, only type will be used with the generic /api/indexes?type=X route
  /** @deprecated indexes option => only type will be used */
  const indexesOptions = fetchOption && "baseUrl" in fetchOption ? { baseUrl: fetchOption.baseUrl } : { type };
  const { indexes, isLoadingIndexes } = useGetIndexes(indexesOptions);

  /** @deprecated fetch option */
  const fetchOptionDeprecated = fetchOption && "baseUrl" in fetchOption ? { baseUrl: fetchOption.baseUrl } : { type, indexPath };
  // Handle index values fetch option: use indexValuesFetchOption if provided, else use same logic as indexes
  const indexValuesOptions =
    indexValuesFetchOption && "baseUrl" in indexValuesFetchOption ? { baseUrl: indexValuesFetchOption.baseUrl } : fetchOptionDeprecated; // fallback to fetchOption logic for backward compatibility, will be removed once all components are migrated to use the generic API route with type param

  // Skip fetching index values for globalSearch search fields
  const shouldFetchIndexValues = selectedField && !globalSearch.includes(selectedField);

  const { indexValues } = useGetIndexValues({
    field: shouldFetchIndexValues ? selectedField : "",
    searchParameters,
    searchValue: likeSearchValue,
    ...indexValuesOptions,
  });
  const [storageKey, setStorageKey] = useState<string>("savedSearch");

  useEffect(() => {
    const missingDefaults = Object.entries(defaultFilters).filter(([key]) => !(key in searchParameters));
    if (missingDefaults.length > 0) {
      createQuery(Object.fromEntries(missingDefaults));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // intentionally run once on mount to initialize default filter values

  useEffect(() => {
    setIsOpen(false);
  }, [searchParameters, setIsOpen]);

  useEffect(() => {
    // Set the storage key on the client side
    setStorageKey(getAppStorageKey());
  }, []);

  const url = typeof globalThis !== "undefined" && "location" in globalThis ? globalThis.location.href : "";

  const handleSaveSearch = (searchName: string) => {
    saveSearch({
      searchName,
      url,
      filters: searchParameters,
      storageKey,
      successMessage:
        t("gzl.user.interface.search_saved_successfully") + ". " + t("gzl.user.interface.you_can_access_it_from_your_favorite_searches"),
    });
  };

  // Map indexes to the format needed by Filter
  const pickIndexes = [...(indexes ?? []).filter((item: IndexItem) => !customIndexFilter || customIndexFilter(item)), ...additionalFields]
    .map((item: IndexItem) => ({
      ...item,
      displayName: indexNameMapping[item.name] ?? item.name,
    }))
    .sort((a: IndexWithDisplayName, b: IndexWithDisplayName) => a.displayName.localeCompare(b.displayName));

  const normalizePossibleValue = (value: string): string => {
    const trimmed = value.trim();
    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
      const content = trimmed.slice(1, -1).trim();
      if (!/[,[\]]/.test(content)) return content;
    }
    return value;
  };

  const normalizedIndexValues = (indexValues ?? []).map((value: string) => normalizePossibleValue(value));

  // Apply valueDisplayMapping to possible values if available for the selected field
  // If mapping doesn't exist for a value, keep the original value (ID)
  const transformedIndexValues = valueDisplayMapping[selectedField]
    ? normalizedIndexValues.map((value: string) => valueDisplayMapping[selectedField]?.[value] ?? value)
    : normalizedIndexValues;

  const effectivePossibleValues = possibleValuesOverride[selectedField] ?? transformedIndexValues;

  const visibleSearchParameters = Object.fromEntries(Object.entries(searchParameters).filter(([key]) => !hiddenActiveFilterKeys.includes(key)));
  const displaySearchParameters = Object.fromEntries(
    Object.entries(visibleSearchParameters).map(([key, value]) => [key, valueDisplayMapping[key]?.[value] ?? value]),
  );
  const displayToRawValueMapping = Object.fromEntries(
    Object.entries(valueDisplayMapping).map(([key, mapping]) => [
      key,
      Object.fromEntries(Object.entries(mapping).map(([rawValue, displayValue]) => [displayValue, rawValue])),
    ]),
  ) as Record<string, Record<string, string>>;

  const createQueryWithValueMapping = (paramsObject: Record<string, string | null | undefined>, clearExistingParams?: boolean) => {
    const nextParams = Object.fromEntries(
      Object.entries(paramsObject).map(([key, value]) => [
        key,
        value === null || value === undefined ? value : (displayToRawValueMapping[key]?.[value] ?? value),
      ]),
    );

    return createQuery({ ...persistentParams, ...nextParams }, clearExistingParams);
  };

  if (isLoadingIndexes) {
    return <Skeleton className="h-10 p-2" />;
  }

  return (
    <div className="space-y-2 p-2">
      <Filter
        data={pickIndexes}
        filteredParams={displaySearchParameters}
        selectedField={selectedField}
        setSelectedField={setSelectedField}
        possibleValues={effectivePossibleValues}
        createQueryString={createQueryWithValueMapping}
        paramNameMapping={indexNameMapping}
        excludedKeys={excludedKeys}
        saveSearch={handleSaveSearch}
        storageKey={storageKey}
        excludedFromClear={excludedFromClear}
        onSearchChange={setLikeSearchValue}
        enableGlobalShortcut={true}
        shortcutKey="k"
        globalSearch={globalSearch[0]}
      />

      <div className="flex">
        {isDateFilter && (
          <Suspense>
            <DateFilterContent
              paramName={dateFilterParamName}
              label={dateFilterLabel ?? t("gzl.message.capture.date_from_to")}
              persistentParams={persistentParams}
            />
          </Suspense>
        )}
      </div>
    </div>
  );
};

export default GenericFilters;
