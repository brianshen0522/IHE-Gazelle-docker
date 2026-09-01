import useSWR from "swr";
import { BaseAttachmentProps } from "@/app/message-capture/components/proxy/Types";
import { base64Fetcher } from "@shared/services/fetcher";

type GetAttachmentAsBase64Props = Pick<BaseAttachmentProps, "itemId" | "attachmentId"> & {
  fetchSectionId: string | null;
};

export function useGetAttachmentAsBase64({ fetchSectionId, itemId, attachmentId }: GetAttachmentAsBase64Props) {
  const shouldFetch = Boolean(fetchSectionId && itemId && attachmentId);
  const url = shouldFetch
    ? `/gazelle/message-capture/api/attachmentBase64/${itemId}?attachmentId=${attachmentId}`
    : null;

  const { data, error, isLoading } = useSWR(url, base64Fetcher);

  return {
    data: shouldFetch ? data : null,
    isError: shouldFetch ? error : null,
    isLoading: shouldFetch ? isLoading : false,
  };
}
