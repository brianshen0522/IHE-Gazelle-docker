import { getUserSummaryById } from "@/app/message-capture/services/getUserSummaryById";
import { useEffect, useState } from "react";

export const useOwnerName = (ownerId: string | undefined) => {
  const [ownerName, setOwnerName] = useState("");

  useEffect(() => {
    if (!ownerId) {
      setOwnerName("");
      return;
    }
    getUserSummaryById(ownerId).then(({ userSummary }) => {
      setOwnerName(userSummary ? `${userSummary.firstName} ${userSummary.lastName}` : ownerId);
    });
  }, [ownerId]);

  return ownerName;
};
