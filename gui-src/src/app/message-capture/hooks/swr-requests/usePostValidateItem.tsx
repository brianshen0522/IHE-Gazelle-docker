import { useState } from "react";
import axios from "axios";
import { Session } from "next-auth";
import { ItemValidationResult, ValidationRequest } from "@/app/message-capture/components/proxy/validation/Types";

export function usePostValidation(session: Session | null | undefined) {
  const [isLoading, setIsLoading] = useState(false);
  const [isValidationError, setIsValidationError] = useState(false);

  const validateItem = async ({ itemId, validator, serviceName, contentPath, syntax, selector }: ValidationRequest) => {
    if (!itemId || !validator?.keyword) {
      console.error("Invalid itemdId or profileId");
      return;
    }

    const accessToken = session?.access_token;

    setIsLoading(true);
    setIsValidationError(false);
    const validationRequestBody = {
      validationServiceProfile: {
        validator: {
          keyword: validator?.keyword,   // profileId is now the keyword
        },
        serviceName,
      },
      contentPath: {
        path: contentPath,
        syntax: syntax,
        selector,
      },
    };
    try {
      const response = await axios.post(`/gazelle/message-capture/api/validateItem/${itemId}`, validationRequestBody, {
        headers: {
          "Content-Type": "application/json",
          Authorization: accessToken ? `Bearer ${accessToken}` : "",
        },
      });
      if (!response.data) {
        throw new Error("Request failed");
      }
      const validationItemResult: ItemValidationResult = (await response.data) as ItemValidationResult;

      return validationItemResult;
    } catch (error) {
      setIsValidationError(true);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  return { isValidationError, isLoading, validateItem };
}
