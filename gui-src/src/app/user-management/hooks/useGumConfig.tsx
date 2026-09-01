import { useState, useEffect } from "react";
import { getGUMConfigurations } from "@/app/user-management/actions";

/**
 * Custom hook to fetch GUM configuration using server action
 */
export function useGumConfig() {
  const [data, setData] = useState<any>(null);
  const [isError, setIsError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchConfig = async () => {
      setIsLoading(true);
      try {
        const result = await getGUMConfigurations();
        if (result.error) {
          setIsError(result.error);
        } else {
          setData(result);
        }
      } catch (error: any) {
        setIsError(error.message || "An error occurred");
      } finally {
        setIsLoading(false);
      }
    };

    fetchConfig();
  }, []);

  return { data, isError, isLoading };
}
