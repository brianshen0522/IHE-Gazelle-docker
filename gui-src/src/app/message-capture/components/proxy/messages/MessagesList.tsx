"use client";
import { useEffect } from "react";
import { ScrollTop, useSmallScreen, useSidePanel } from "@gazelle/gazelle-component-ui";
import usePresentationSchemaUrl from "@/shared/hooks/usePresentationSchemaUrl";
import { ProxyMessages } from "@/app/message-capture/components/proxy/Types";
import { useTranslation } from "react-i18next";
import TablePaginationWrapper from "@/shared/components/table/TablePaginationWrapper";
import { useSearchParamsUrl } from "@/shared/hooks/useSearchParamsUrl";
import { indexNameMapping } from "@message-capture/utils/indexNameMapping";
import GenericFilters from "@/shared/components/filter/GenericFilters";
import { normalizeKey } from "@/shared/utils/normalizeKey";
import { useMessagesColumns } from "@message-capture/components/proxy/messages/MessagesColumns";
import MessageSidePanel from "@message-capture/components/proxy/messages/MessageSidePanel";
import { useSearchParams } from "next/navigation";

const MESSAGE_LIST_SUMMARY_SCHEMA = "message_list_summary";

const MessagesList = () => {
  const baseUrl = "/gazelle/message-capture/api";
  const { searchParameters } = useSearchParamsUrl(normalizeKey);
  const searchParams = useSearchParams();
  const { setIsOpen } = useSidePanel();
  const isSmallScreen = useSmallScreen();
  const { t } = useTranslation();
  const columns = useMessagesColumns();
  const { presentationSchemaUrl } = usePresentationSchemaUrl(MESSAGE_LIST_SUMMARY_SCHEMA, "message-capture");

  useEffect(() => {
    if (isSmallScreen) {
      setIsOpen(false);
    }
  }, [isSmallScreen, setIsOpen]);

  useEffect(() => {
    localStorage.setItem("prevUrl", globalThis.location.href);
  }, [searchParams]);

  return (
    <div className="flex flex-grow overflow-hidden">
      <div className="flex flex-col flex-grow overflow-hidden gap-2 p-1">
        <GenericFilters
          searchParameters={searchParameters}
          indexNameMapping={indexNameMapping}
          fetchOption={{ baseUrl }}
          indexValuesFetchOption={{ baseUrl: `${baseUrl}/possibleValues` }}
          customIndexFilter={(item) => item.name in indexNameMapping}
          excludedKeys={["capture_date"]}
          isDateFilter={true}
        />

        <TablePaginationWrapper<ProxyMessages>
          tableColumns={columns}
          baseUrl={baseUrl}
          apiFolder="items"
          emptyDataMessage={t("gzl.message.capture.no_messages_available")}
          paramPrefix="_"
          paramMap={{ sortBy: "_sort" }}
          initialField="capture_date"
          initialSortOrder="desc"
          searchParameters={presentationSchemaUrl ? { ...searchParameters, _presentation: presentationSchemaUrl } : searchParameters}
        />
      </div>

      {!isSmallScreen && <MessageSidePanel />}
      <ScrollTop />
    </div>
  );
};

export default MessagesList;
