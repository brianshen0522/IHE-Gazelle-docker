import { Session } from "next-auth";
import { useState, useEffect } from "react";
import "@/shared/components/auth/types";

export function useUserPicture(session: Session | null, format: string) {
  const userId = session?.user?.gazelleId;
  const accessToken = session?.access_token;
  const [pictureUrl, setPictureUrl] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!userId) {
      setIsLoading(false);
      return;
    }

    const fetchPicture = async () => {
      try {
        setIsLoading(true);
        const response = await fetch(`/gazelle/api/users/${userId}/picture?format=${format}`, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });

        if (!response.ok) {
          console.error(`Failed to fetch user picture: ${response.status} ${response.statusText}`);
          setError(true);
          return;
        }

        const data = await response.json();
        setPictureUrl(data.data);
      } catch (err) {
        console.error("Error fetching user picture:", err);
        setError(true);
      } finally {
        setIsLoading(false);
      }
    };

    fetchPicture();
  }, [userId]);

  return { pictureUrl, isLoading, error };
}
