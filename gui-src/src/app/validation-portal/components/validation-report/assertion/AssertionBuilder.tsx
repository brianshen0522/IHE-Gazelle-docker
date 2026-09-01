import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useInfiniteScroll, CollapsableSubCard } from "@gazelle/gazelle-component-ui";
import { BuildAssertionsProps } from "../types";
import GroupedAssertions from "./GroupedAssertions";
import { useAssertionsData } from "@/app/validation-portal/hooks/useAssertionData";
import GroupingToggleAssertion from "./GroupingToggleAssertion";

const AssertionBuilder = ({
  validationReport,
  buildGroupedAssertionByReport,
  type,
  title,
  description,
  initiallyExpanded = true,
  allowGroupBySeverity = false,
}: BuildAssertionsProps) => {
  const { t } = useTranslation();
  const [groupBy, setGroupBy] = useState<"report" | "severity">("severity");

  const groupedReports = useMemo(() => {
    if (!buildGroupedAssertionByReport || !validationReport?.reports) {
      return [];
    }
    return buildGroupedAssertionByReport(validationReport.reports, type);
  }, [validationReport?.reports, type, buildGroupedAssertionByReport]);

  const { assertions, total } = useAssertionsData(groupedReports, type);

  const { visibleItems, hasMore, isLoadingMore, loadingTriggerId } = useInfiniteScroll({
    data: assertions,
    itemsPerPage: 20,
    loadingDelay: 300,
    threshold: 0.1,
  });

  if (!total) return null;

  return (
    <CollapsableSubCard
      title={
        <h4 id={`${type.toLowerCase()}-assertions`}>
          {title} ({total})
        </h4>
      }
      defaultExpanded={initiallyExpanded}
      className="border-lightpurple"
    >
      <div className="max-h-[500px] overflow-y-auto p-1.5">
        <p className="text-grey-500">{description}</p>

        {allowGroupBySeverity && <GroupingToggleAssertion value={groupBy} onChange={setGroupBy} />}

        <div className="flex flex-col my-1 gap-2">
          <GroupedAssertions items={visibleItems} groupBy={groupBy} />

          {hasMore && (
            <div id={loadingTriggerId} className="flex justify-center py-2">
              {isLoadingMore ? <div className="animate-pulse">{t("gzl.user.interface.loading")}</div> : <div className="h-8 w-full" />}
            </div>
          )}
        </div>
      </div>
    </CollapsableSubCard>
  );
};

export default AssertionBuilder;
