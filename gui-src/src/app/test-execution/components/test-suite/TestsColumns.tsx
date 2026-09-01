"use client";
import { useCallback, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { createColumnHelper } from "@tanstack/react-table";
import { Test } from "@/app/test-execution/types/TestCase";
import Link from "next/link";
import { CirclePlay } from "lucide-react";
import { Badge, Tags } from "@gazelle/gazelle-component-ui";
import { useTestSession } from "@test-execution/context/TestSessionContext";
import { Route } from "next";
import { useCreateQuery } from "@/shared/hooks/useCreateQuery";

const columnHelper = createColumnHelper<Test>();

export function useTestsColumns() {
  const { t } = useTranslation();
  const { testSessionId } = useTestSession();
  const { createQuery } = useCreateQuery();
  const [expandedTags, setExpandedTags] = useState<Record<string, boolean>>({});

  const handleTagClick = useCallback(
    (tag: string) => {
      createQuery({ tags: tag, offset: null, row: null });
    },
    [createQuery],
  );

  const columns = useMemo(
    () => [
      columnHelper.accessor((row) => row.name, {
        id: "name",
        header: () => <span>{t("gzl.texec.test_name")}</span>,
        cell: (info) => info.getValue(),
        enableColumnFilter: false,
      }),
      columnHelper.accessor((row) => row.tags, {
        id: "tags",
        header: () => <span>{t("gzl.validation_portal.tags")}</span>,
        cell: (info) => {
          const tags = info.getValue() ?? [];
          if (tags.length === 0) return null;

          const rowId = info.row.id;
          const isExpanded = expandedTags[rowId] ?? false;
          const visibleTags = isExpanded ? tags : tags.slice(0, 2);
          const hiddenCount = tags.length - visibleTags.length;

          return (
            <Tags
              tags={tags}
              visibleTags={visibleTags}
              hiddenCount={hiddenCount}
              isExpanded={isExpanded}
              setExpandedTags={setExpandedTags}
              rowId={rowId}
              isClickable={true}
              onTagClick={handleTagClick}
            />
          );
        },
        enableColumnFilter: false,
        enableSorting: false,
      }),
      columnHelper.display({
        id: "actions",
        header: () => <span>{t("gzl.user.interface.action")}</span>,
        cell: (info) => {
          const { testId, inScope } = info.row.original;

          if (!testId || !testSessionId) {
            return null;
          }

          return (
            <>
              {inScope ? (
                <Link
                  href={`/test-execution/test-run-execution?testId=${testId}` as Route}
                  onClick={(e) => e.stopPropagation()}
                  className="inline-flex items-center gap-2 text-blue hover:text-visited_link transition-colors duration-200"
                >
                  <CirclePlay />
                  {t("gzl.texec.new_execution")}
                </Link>
              ) : (
                <Badge id="scope" variant="default">
                  {t("gzl.user.interface.deprecated")}
                </Badge>
              )}
            </>
          );
        },
        enableColumnFilter: false,
        enableSorting: false,
      }),
    ],
    [t, expandedTags, testSessionId, handleTagClick],
  );

  return columns;
}
