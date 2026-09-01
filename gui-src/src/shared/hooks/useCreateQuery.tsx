import { useCallback } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Route } from "next";

export const useCreateQuery = () => {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const hasSearchParams = searchParams.toString().length > 0;

  const createQuery = useCallback(
    (paramsObject: Record<string, string | null | undefined>, clearExistingParams = false) => {
      const params = new URLSearchParams(clearExistingParams ? "" : searchParams.toString());

      for (const [name, value] of Object.entries(paramsObject)) {
        if (value === null || value === undefined || value === "") {
          params.delete(name);
        } else {
          params.set(name, value);
        }
      }

      // Do not reload the page when we select a row
      if (params.has("row")) {
        globalThis.history.replaceState(globalThis.history.state, "", `/gazelle${pathname}?${params.toString()}`);
      } else {
        router.push(`${pathname}?${params.toString()}` as Route);
      }
      return params.toString();
    },
    [pathname, router, searchParams]
  );

  const clearQueryString = useCallback(() => {
    router.replace(`${pathname}` as Route, { scroll: false });
  }, [pathname, router]);

  return { createQuery, pathname, router, hasSearchParams, searchParams, clearQueryString };
};
