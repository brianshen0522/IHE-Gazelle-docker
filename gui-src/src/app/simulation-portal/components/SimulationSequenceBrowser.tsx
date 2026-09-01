"use client";

import { Route } from "next";
import { useEffect } from "react";
import { cantDesignTests } from "@shared/utils/permissions";
import ContentHeader from "@shared/components/layout/ContentHeader";
import { usePathname, useRouter } from "next/navigation";
import { sequenceIndexNameMapping, sequenceKeys } from "@simulation-portal/service/constants";
import { sequenceColumns } from "@simulation-portal/components/simulation-sequence/SequenceTableColumns";
import SequenceSidePanel from "@simulation-portal/components/simulation-sequence/SequenceSidePanel";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";
import { Session } from "next-auth";
import { useSession } from "next-auth/react";
import TablePaginationWrapper from "@/shared/components/table/TablePaginationWrapper";
import { ScrollTop, useSmallScreen, SidePanelProvider } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import GenericFilters from "@/shared/components/filter/GenericFilters";
import { useSearchParamsUrl } from "@shared/hooks/useSearchParamsUrl";

const SimulationSequenceBrowser = () => {
  const router = useRouter();
  const pathname = usePathname();
  const { searchParameters } = useSearchParamsUrl();
  const { data: session } = useSession() as { data: Session };
  const isSmallScreen = useSmallScreen();
  const { t } = useTranslation();

  const breadcrumbs = [
    { label: t("gzl.user.interface.home"), url: "/home" },
    { label: t("gzl.simulation_portal.simulation"), url: "/" },
  ];

  useEffect(() => {
    if (cantDesignTests(session)) {
      const params = new URLSearchParams(searchParameters);
      params.set(sequenceKeys.valid, "true");
      router.push(`${pathname}?${params.toString()}` as Route);
    }
  }, [session, searchParameters, pathname, router]);

  return (
    <SidePanelProvider>
      <ContentHeader breadcrumbsItems={breadcrumbs} title={t("gzl.simulation_portal.simulation_sequences")} secured>
        <div className="flex flex-grow overflow-hidden">
          <div className="flex flex-col flex-grow overflow-hidden gap-2 p-1">
            <GenericFilters searchParameters={searchParameters} indexNameMapping={sequenceIndexNameMapping} type="simulation" isDateFilter={false} />

            <TablePaginationWrapper<SimulationSequence>
              tableColumns={sequenceColumns}
              baseUrl="/gazelle/simulation-portal/api"
              apiFolder="sequences"
              emptyDataMessage={t("gzl.simulation_portal.no_sequences_available")}
              paramPrefix="_"
              paramMap={{ sortBy: "_sort" }}
            />
          </div>
          <ScrollTop />
          {!isSmallScreen && <SequenceSidePanel />}
        </div>
      </ContentHeader>
    </SidePanelProvider>
  );
};

export default SimulationSequenceBrowser;
