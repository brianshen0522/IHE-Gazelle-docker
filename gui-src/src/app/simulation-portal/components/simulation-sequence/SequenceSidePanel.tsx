"use client";
import { PropsWithChildren } from "react";
import { sequence } from "@simulation-portal/service/constants";
import { useSidePanel } from "@gazelle/gazelle-component-ui";
import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";
import SidePanel from "@simulation-portal/components/simulation-sequence/SidePanel";
import {
  SimulatedRoles,
  TestedRoles,
  TestedStandards,
  TestedTransactions,
} from "@simulation-portal/components/simulation-sequence/SimulationMetadata";
import NoticeBanner from "@shared/components/banner/NoticeBanner";
import { useTranslation } from "react-i18next";

type SidePanelItemType = {
  name?: string;
};

const SidePanelItem = ({ name, children }: PropsWithChildren<SidePanelItemType>) => {
  return (
    <div className="ml-1 mr-1">
      <span className="font-semibold whitespace-nowrap text-purple">{name}</span>
      <div>{children}</div>
    </div>
  );
};

const SequenceSidePanel = () => {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, selectedRow } = useSidePanel<SimulationSequence>();
  const sequenceId = selectedRow?.id!;

  const accessDetailsProps = {
    id: sequenceId,
    pathname: `${sequence.basePath}/${sequenceId}`,
    redirectLink: `${selectedRow?.runnable && selectedRow?.valid ? "Run" : "View"}`,
  };

  const openClass = isOpen ? "bg-white ml-2" : "";

  const hasContent = !!selectedRow;

  if (!hasContent) {
    return <></>;
  }

  return (
    <SidePanel isOpen={isOpen} className={openClass}>
      <SidePanel.Header accessDetailsProps={accessDetailsProps} onClose={() => setIsOpen(false)} />
      <SidePanel.Section id="sequence-general-information" title={t("gzl.simulation_portal.general_information")}>
        <SidePanelItem>
          {!selectedRow?.runnable && (
            <NoticeBanner color="blue" weight="semibold">
              {t("gzl.simulation_portal.sequence_documentation")}
            </NoticeBanner>
          )}
          {!selectedRow?.valid && (
            <NoticeBanner color="red" weight="semibold">
              This Simulation sequence is invalid and cannot be executed for the following reason :{selectedRow?.validReportMessage}
            </NoticeBanner>
          )}
        </SidePanelItem>
        <SidePanelItem name={t("gzl.simulation_portal.sequence_name")}>{selectedRow?.id}</SidePanelItem>
        <SidePanelItem name={t("gzl.simulation_portal.sequence_version")}>{selectedRow?.version}</SidePanelItem>
        <SidePanelItem name={t("gzl.simulation_portal.simulation_service")}>{selectedRow?.simulatorName}</SidePanelItem>
        <SidePanelItem name={t("gzl.user.interface.summary")}>{selectedRow?.shortDescription}</SidePanelItem>
      </SidePanel.Section>
      <SidePanel.Section id="simulation-information" title={t("gzl.simulation_portal.simulation_configuration")}>
        {selectedRow?.simulatedRoles?.length > 0 && (
          <SidePanelItem name="Simulated roles">
            <SimulatedRoles simulationSequence={selectedRow} />
          </SidePanelItem>
        )}
        {selectedRow?.testedRoles?.length > 0 && (
          <SidePanelItem name="Tested roles">
            <TestedRoles simulationSequence={selectedRow} />
          </SidePanelItem>
        )}
        {selectedRow?.transactions && (
          <SidePanelItem name="Tested transactions">
            <TestedTransactions simulationSequence={selectedRow} />
          </SidePanelItem>
        )}
        {selectedRow?.standards && (
          <SidePanelItem name="Tested standards">
            <TestedStandards simulationSequence={selectedRow} />
          </SidePanelItem>
        )}
      </SidePanel.Section>
    </SidePanel>
  );
};

export default SequenceSidePanel;
