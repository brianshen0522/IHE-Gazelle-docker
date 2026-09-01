"use client";
import GenericFilters from "@/shared/components/filter/GenericFilters";
import { useSearchParamsUrl } from "@shared/hooks/useSearchParamsUrl";
import { indexNameMapping } from "./indexNameMapping";

export default function ProfileFilters() {
  const { searchParameters } = useSearchParamsUrl();

  return (
    <GenericFilters
      searchParameters={searchParameters}
      indexNameMapping={indexNameMapping}
      fetchOption={{ baseUrl: "/gazelle/validation-portal/api" }}
      indexValuesFetchOption={{ baseUrl: "/gazelle/validation-portal/api/possibleValues" }}
      isDateFilter={false}
    />
  );
}
