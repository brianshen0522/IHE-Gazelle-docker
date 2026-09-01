import { ReactElement, useState, useEffect } from "react";
import Table from "./Table";
import { Button, Skeleton } from "@gazelle/gazelle-component-ui";
import { useGetData } from "@shared/hooks/SWR/useGetData";
import { useSearchParamsUrl } from "@shared/hooks/useSearchParamsUrl";
import InternalErrors from "@shared/components/errors/InternalError";
import { usePageReset } from "@shared/hooks/usePageReset";
import { FileX } from "lucide-react";
import { useTranslation } from "react-i18next";
import { TablePaginationProps } from "./types";
import { parseContentRange } from "@shared/utils/parseContentRange";

const TablePaginationWrapper = <T,>({
  tableColumns,
  baseUrl,
  apiFolder,
  emptyDataMessage,
  initialField = "",
  initialSortOrder = "desc",
  initialLimit = 50,
  loadMoreStep = 50,
  paramPrefix,
  paramMap,
  searchParameters: customSearchParameters,
  getRowId,
  fields,
  type,
  path,
}: TablePaginationProps<T>): ReactElement => {
  const { t } = useTranslation();
  const { searchParameters: defaultSearchParameters } = useSearchParamsUrl();
  const searchParameters = customSearchParameters ?? defaultSearchParameters;
  const [field, setField] = useState(initialField);
  const [sortOrder, setSortOrder] = useState<"asc" | "desc" | null>(initialSortOrder);
  const [limit, setLimit] = useState(initialLimit);
  const [offset, setOffset] = useState(0);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const { data, contentRange, isLoading, isValidating, error } = useGetData<T>({
    searchParameters,
    field,
    sortOrder,
    offset,
    limit,
    baseUrl,
    apiFolder,
    paramPrefix,
    paramMap,
    fields,
    type,
    path,
  });

  // Track when loading more data is complete
  useEffect(() => {
    if (isLoadingMore && !isLoading) {
      setIsLoadingMore(false);
    }
  }, [isLoadingMore, isLoading]);

  const rangeInfo = contentRange ? parseContentRange(contentRange) : null;
  const hasMoreData = rangeInfo ? rangeInfo.end < rangeInfo.total : false;
  const buttonState = hasMoreData ? t("gzl.user.interface.load_more_data") : t("gzl.user.interface.all_data_loaded");

  usePageReset({
    offset,
    setOffset,
    items: data,
  });

  const handleLoadMore = () => {
    setIsLoadingMore(true);
    setLimit((l) => l + loadMoreStep);
  };

  const noDataMessage = emptyDataMessage || t("gzl.there_are_no_data_available_to_be_displayed_here");

  const isInitialLoad = !data || data.length === 0;

  if (isInitialLoad && isLoading) {
    return (
      <div className="p-1 w-full">
        <Skeleton className="h-screen" />
      </div>
    );
  }

  if (error) return <InternalErrors message={error.message} />;

  return (
    <>
      {!data || data.length === 0 ? (
        <div className="flex items-center flex-col gap-4 pt-8 text-purple">
          <FileX size={36} />
          <p>{noDataMessage}</p>
        </div>
      ) : (
        <>
          <Table
            data={data}
            columns={tableColumns}
            field={field}
            setField={setField}
            sortOrder={sortOrder}
            setSortOrder={setSortOrder}
            isValidating={isValidating}
            getRowId={getRowId}
          />
          <div className="flex items-center justify-center gap-4">
            <div className="flex flex-col items-center justify-center space-y-2">
              <span className="font-semibold">{contentRange && `${t("gzl.user.interface.showing_results")} ${contentRange}`}</span>
              <Button id="load-more-data" type="button" onClick={handleLoadMore} variant="secondary" disabled={!hasMoreData || isValidating}>
                {isValidating ? t("gzl.user.interface.loading") : buttonState}
              </Button>
            </div>
          </div>
        </>
      )}
    </>
  );
};

export default TablePaginationWrapper;
