"use client";
import { ScrollTop, Skeleton, PaginationIndex } from "@gazelle/gazelle-component-ui";
import { ToastContainer } from "react-toastify";
import ConnectionInfoWrapper from "@message-capture/components/proxy/connections/ConnectionInfoWrapper";
import MessageMetadata from "@message-capture/components/proxy/messages/MessageMetadata";
import ValidationWrapper from "@message-capture/components/proxy/validation/ValidationWrapper";
import MessageItemContent from "@message-capture/components/proxy/messages/MessageItemContent";
import { AclDisplay } from "@/shared/components/ACL/AclDisplay";
import { useCreateQuery } from "@shared/hooks/useCreateQuery";
import { useGetConnectionIndexes } from "@message-capture/hooks/swr-requests/useGetConnectionIndexes";
import { useGetItem } from "@hooks/useGetItem";
import { useGetConnection } from "@message-capture/hooks/swr-requests/useGetConnection";
import { useSearchParams } from "next/navigation";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";
import { currentItemType, ItemType } from "@/app/message-capture/components/proxy/Types";
import { textFormatter } from "@/app/message-capture/utils/labelFormatter";
import Unauthorized from "@shared/components/auth/Unauthorized";
import { ItemContext } from "@/shared/context/ItemContext";
import ContentHeaderWrapper from "@/shared/components/layout/ContentHeaderWrapper";

const MessageWrapper = () => {
  const searchParams = useSearchParams();
  const { createQuery } = useCreateQuery();
  const id = searchParams.get("id") as string;
  const connectionId = searchParams.get("connectionId") as string;
  const readAccessKeySearchParam = searchParams.get("readAccessKey") as string;
  const { data: session } = useSession();
  const { data: itemConnection, isError, isLoading } = useGetConnectionIndexes(connectionId, session);
  const { data: itemMessage, isLoading: itemLoading, isError: itemError } = useGetItem(id, session, readAccessKeySearchParam ?? null);
  const { connection, isConnectionLoading, isError: connectionError } = useGetConnection(connectionId, session, readAccessKeySearchParam ?? null);
  const { t } = useTranslation();

  let currentItem: currentItemType<ItemType>;
  // Find the index of the selected message in the connection array
  const selectedObjectIndex = itemConnection ? itemConnection.findIndex((obj: { id: string | null }) => obj.id === id) : -1;
  const currentPage = selectedObjectIndex === -1 ? 1 : selectedObjectIndex + 1;
  currentItem = itemConnection?.[currentPage - 1] ?? undefined;

  const handlePageChange = (newPage: number) => {
    if (itemConnection && newPage > 0 && newPage <= itemConnection.length) {
      currentItem = itemConnection[newPage - 1];
      createQuery({ id: currentItem.id });
    }
  };

  const renderTitle = (currentItem: currentItemType<{ content: any; type?: string }>) => {
    if (!currentItem?.content) return null;
    return currentItem.type === "CONNECTION_ERROR" ? textFormatter(currentItem.content.rootType) : textFormatter(currentItem.content.type);
  };

  const connectionItem = connection?.[0];

  const messageItem = itemMessage?.[0];
  const aclConnection = connectionItem?.accessControlList;

  if (connectionError) {
    const isForbidden = connectionError.message.includes("403");
    const isUnauthorized = connectionError.message.includes("401");
    if (isUnauthorized || isForbidden) {
      return <Unauthorized />;
    }
  }

  if (itemError) {
    const isForbidden = itemError.message.includes("403");
    const isUnauthorized = itemError.message.includes("401");
    if (isUnauthorized || isForbidden) {
      return <Unauthorized />;
    }
  }

  if (isError) return <div>{t("gzl.message.capture.error_while_loading_the_message_data_please_refresh")}.</div>;
  if (isLoading || itemLoading || isConnectionLoading)
    return (
      <div className="p-1">
        <Skeleton className="h-screen" />
      </div>
    );

  return (
    <ItemContext.Provider value={messageItem}>
      <ContentHeaderWrapper id="message-header" title={t("gzl.message.capture.message")} enableAutoGoBack={true} />
      <div className="flex flex-col w-full p-2 gap-4 overflow-x-hidden">
        <div className="flex items-center gap-2 ">
          <h3 className="font-semibold">{renderTitle(messageItem)}</h3>
          <PaginationIndex currentPage={currentPage} totalPages={itemConnection.length} onPageChange={handlePageChange} />
        </div>

        <ConnectionInfoWrapper connectionItem={connectionItem} messageItem={messageItem} />
        {aclConnection && <AclDisplay acl={aclConnection} itemId={connectionId} />}

        <div className="flex flex-col gap-4">
          <div className="flex flex-col md:flex-row md:justify-between gap-2">
            <MessageMetadata data={messageItem} />
            <ValidationWrapper messageItem={messageItem} />
          </div>

          {itemMessage?.content?.rootType === "TLS_ERROR" && (
            <div className="text-red border border-red rounded-lg p-2">{t("gzl.message.capture.tls_handshake_has_failed_for_this_connection")}.</div>
          )}

          <MessageItemContent itemConnection={itemConnection} connectionItem={connectionItem} messageItem={messageItem} />
          <ScrollTop />
        </div>
        <ToastContainer />
      </div>
    </ItemContext.Provider>
  );
};

export default MessageWrapper;
