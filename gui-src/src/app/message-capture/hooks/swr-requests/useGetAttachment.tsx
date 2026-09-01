import useSWR from "swr";
import { BaseAttachmentProps } from "@/app/message-capture/components/proxy/Types";
import { blobFetcher } from "@shared/services/fetcher";

interface GetAttachmentProps extends BaseAttachmentProps {
  downloadAttachment: boolean;
}

export function useGetAttachment({ downloadAttachment, itemId, attachmentId }: GetAttachmentProps) {
  const url = `/gazelle/message-capture/api/attachment/${itemId}?attachmentId=${attachmentId}`;
  const { data, error, isLoading } = useSWR(downloadAttachment ? url : null, blobFetcher);
  return { data, isError: error, isLoading };
}
