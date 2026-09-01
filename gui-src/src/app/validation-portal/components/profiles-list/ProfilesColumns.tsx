"use client";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { createColumnHelper } from "@tanstack/react-table";
import { ValidationProfileResponse } from "@validation-portal/types/ValidationProfile";
import Link from "next/link";
import { Route } from "next";
import { CirclePlay } from "lucide-react";
import { Tags } from "@gazelle/gazelle-component-ui";

const columnHelper = createColumnHelper<ValidationProfileResponse>();

export function useProfilesColumns() {
  const { t } = useTranslation();
  const [expandedTags, setExpandedTags] = useState<Record<string, boolean>>({});
  const columns = useMemo(
    () => [
      columnHelper.accessor((row) => row.profile.profileName, {
        id: "profileName",
        header: () => <span>{t("gzl.validation_portal.validation_profile")}</span>,
        cell: (info) => {
          const { profile } = info.row.original;
          const profileName = profile.profileName;
          const version = profile.version;

          return (
            <>
              {profileName}
              {version && <span className="ml-1">({version})</span>}
            </>
          );
        },
        enableColumnFilter: false,
      }),
      columnHelper.accessor((row) => row.profile.tags, {
        id: "tags",
        header: () => <span>{t("gzl.validation_portal.tags")}</span>,
        cell: (info) => {
          const tags = info.getValue() || [];
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
            />
          );
        },
        enableColumnFilter: false,
        enableSorting: false,
      }),
      columnHelper.display({
        id: "actions",
        header: () => <span>{t("gzl.validation_portal.action")}</span>,
        cell: (info) => {
          const { validationService, profile } = info.row.original;
          const href = `/validation-portal/validate?profileId=${profile.profileID}&serviceName=${validationService}`;
          return (
            <Link
              href={href as Route}
              onClick={(e) => e.stopPropagation()}
              className="inline-flex items-center gap-2 text-blue hover:text-visited_link transition-colors duration-200"
            >
              <CirclePlay />
              {t("gzl.validation_portal.new_validation")}
            </Link>
          );
        },
        enableColumnFilter: false,
        enableSorting: false,
      }),
    ],
    [expandedTags, t],
  );

  return columns;
}
