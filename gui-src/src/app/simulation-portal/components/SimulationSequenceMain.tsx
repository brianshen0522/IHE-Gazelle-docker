"use client";
import { useParams } from "next/navigation";
import { sequence } from "@simulation-portal/service/constants";
import ContentHeader from "@shared/components/layout/ContentHeader";
import { SimulationFormContextProvider } from "@simulation-portal/context/SimulationFormContext";
import SimulationSequenceContent from "@simulation-portal/components/simulation-execution/SimulationSequenceContent";
import { useTranslation } from "react-i18next";

const SimulationSequenceMain = () => {
  const { t } = useTranslation();
  const { sequenceId } = useParams<{ sequenceId: string }>();
  const id = decodeURIComponent(sequenceId);

  const breadcrumbs = [
    { label: "Home", url: "/home" },
    { label: "Simulation", url: `${sequence.basePath}` },
    { label: `${id}`, url: "/" },
  ];

  return (
    <ContentHeader breadcrumbsItems={breadcrumbs} title={t("gzl.simulation_portal.sequence_name", { id })} secured>
      <SimulationFormContextProvider>
        <SimulationSequenceContent />
      </SimulationFormContextProvider>
    </ContentHeader>
  );
};

export default SimulationSequenceMain;
