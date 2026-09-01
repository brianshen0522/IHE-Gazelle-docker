import { useMemo } from "react";
import { useSearchParams } from "next/navigation";

export const useFilteredParams = () => {
  const searchParams = useSearchParams();

  const filterParam = useMemo(
    () => ({
      search: searchParams.get("search") ?? "",
      delegated: searchParams.get("delegated") ?? "",
      organizationId: searchParams.get("organizationId") ?? "",
      groupIds: searchParams.get("groupIds") ?? "",
      activated: searchParams.get("activated") ?? "",
    }),
    [searchParams]
  );

  const filteredParams = filterParam ? Object.fromEntries(Object.entries(filterParam).filter(([_, value]) => value !== null && value !== "")) : {};

  return { filteredParams };
};
