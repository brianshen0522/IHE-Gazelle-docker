import { useEffect, useRef } from "react";

interface UsePageResetProps<T> {
  offset: number;
  setOffset: (newOffset: number) => void;
  items: T[] | undefined;
  enabled?: boolean;
}

/**
 * Hook to handle pagination reset when a page becomes empty after deletion
 *
 * It detects when the current page is empty after previously having data
 * and automatically resets to the first page.
 */
export const usePageReset = <T,>({ offset, setOffset, items, enabled = true }: UsePageResetProps<T>) => {
  const resetRef = useRef(false);
  const prevItemsLengthRef = useRef<number | null>(null);
  const prevOffsetRef = useRef<number>(0);

  useEffect(() => {
    if (!enabled) {
      return;
    }
    // Check if we're dealing with a user-initiated pagination change
    const isPaginationChange = prevOffsetRef.current !== offset;
    prevOffsetRef.current = offset;

    // Don't perform automatic resets during user navigation
    if (isPaginationChange) {
      return;
    }

    if (offset > 0 && items?.length === 0 && prevItemsLengthRef.current !== null && prevItemsLengthRef.current > 0 && !resetRef.current) {
      resetRef.current = true;
      setOffset(0);
    } else if (items && items.length > 0) {
      resetRef.current = false;
    }

    // Store current items length for next comparison
    if (items) {
      prevItemsLengthRef.current = items.length;
    }
  }, [enabled, items, offset, setOffset]);
};
