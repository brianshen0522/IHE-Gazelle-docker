"use client";
import { useSearchParams } from "next/navigation";
import { useMemo } from "react";

export type KeyNormalizer = (key: string) => string;

/**
 * A hook to extract and process URL search parameters
 * @param keyNormalizer Optional function to normalize keys (e.g., convert dot notation to camelCase)
 * @returns An object containing filtered search parameters (empty strings removed)
 */
export const useSearchParamsUrl = (keyNormalizer?: KeyNormalizer) => {
  const searchParams = useSearchParams();

  const searchParameters: Record<string, string> = useMemo(() => {
    const paramsObject: Record<string, string> = {};

    const excludedKeys = new Set(["row", "limit", "offset"]);
    for (const [key, value] of searchParams.entries()) {
      if (value !== "" && !excludedKeys.has(key)) {
        // If a key normalizer is provided, use it; otherwise use the original key
        const normalizedKey = keyNormalizer ? keyNormalizer(key) : key;
        paramsObject[normalizedKey] = value;
      }
    }

    return paramsObject;
  }, [searchParams, keyNormalizer]);

  return { searchParameters };
};
