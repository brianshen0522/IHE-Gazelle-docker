import useSWR from "swr";
import { getDatahouseAttachment } from "@shared/actions/getDatahouseAttachment";

/**
 * Configuration options for fetching a Datahouse attachment
 */
export interface UseDatahouseAttachmentOptions {
  /**
   * The ID of the Datahouse item containing the attachment
   */
  itemId?: string;

  /**
   * The ID of the attachment to fetch
   */
  attachmentId?: string;

  /**
   * Optional read access key for public/shared access
   * When provided, allows unauthenticated access to the attachment
   */
  readAccessKey?: string;

  /**
   * Whether to fetch the attachment (allows conditional fetching)
   * @default true
   */
  enabled?: boolean;

  /**
   * Whether to return the attachment as a Blob instead of base64 string
   * Useful for file downloads
   * @default false
   */
  asBlob?: boolean;
}

/**
 * Response from useDatahouseAttachment hook
 */
export interface UseDatahouseAttachmentResult<T = string> {
  /**
   * The fetched attachment data (base64 string or Blob depending on options)
   * Undefined while loading or on error
   */
  data?: T;

  /**
   * Loading state
   */
  isLoading: boolean;

  /**
   * Error state (Error object if request failed)
   */
  error?: Error;

  /**
   * Convenience boolean for error state
   */
  isError: boolean;

  /**
   * SWR mutate function for manual revalidation
   */
  mutate: () => void;
}

/**
 * Unified hook for fetching Datahouse attachments using server actions
 *
 */
// Overloaded signatures for better type inference
export function useDatahouseAttachment(options: UseDatahouseAttachmentOptions & { asBlob: true }): UseDatahouseAttachmentResult<Blob>;
export function useDatahouseAttachment(options: UseDatahouseAttachmentOptions & { asBlob?: false }): UseDatahouseAttachmentResult<string>;
export function useDatahouseAttachment(options: UseDatahouseAttachmentOptions): UseDatahouseAttachmentResult<string | Blob> {
  const { itemId, attachmentId, readAccessKey, enabled = true, asBlob = false } = options;

  // SWR key - null disables fetching
  const swrKey = enabled && itemId && attachmentId ? ["datahouse-attachment", itemId, attachmentId, readAccessKey, asBlob ? "blob" : "base64"] : null;

  const { data, error, isLoading, mutate } = useSWR<string | Blob | null>(
    swrKey,
    async () => {
      const result = await getDatahouseAttachment({
        itemId: itemId!,
        attachmentId: attachmentId!,
        readAccessKey,
      });

      if (!result) {
        return null;
      }

      if (result.error) {
        throw new Error(result.error);
      }

      const base64Data = result.data ?? null;
      if (!base64Data) {
        return null;
      }

      // Convert to Blob if requested
      if (asBlob) {
        return base64ToBlob(base64Data);
      }

      return base64Data;
    },
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
      // Attachments are typically large and immutable, so we can cache them longer
      dedupingInterval: 60000, // 1 minute
    },
  );

  return {
    data: data ?? undefined,
    isLoading,
    error,
    isError: !!error,
    mutate,
  };
}

/**
 * Converts a base64 string to a Blob
 */
function base64ToBlob(base64: string): Blob {
  const binaryString = atob(base64);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.codePointAt(i)!;
  }
  return new Blob([bytes]);
}
