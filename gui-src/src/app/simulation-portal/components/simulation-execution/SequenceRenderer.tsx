"use client"

import ChevronCard from "@shared/components/boxes/ChevronCard";
import RenderSanitizedHTML from "@/shared/services/RenderSanitizedHTML";
import SimulatedRoles from "@simulation-portal/components/simulation-execution/SimulatedRoles";
import { useSequenceExecutionContext } from "@simulation-portal/context/SequenceExecutionContext";
import SimulationRunner from "@simulation-portal/components/simulation-execution/SimulationRunner";
import SimulationContext from "@simulation-portal/components/simulation-execution/SimulationContext";
import SequenceConfiguration from "@simulation-portal/components/simulation-execution/SequenceConfiguration";
import {ToastContainer} from "react-toastify";
import {useTranslation} from "react-i18next";

const SequenceRenderer = () => {
  const {simulationSequence} = useSequenceExecutionContext();
  const { t } = useTranslation();

  return (
    <div className="flex flex-col">
      <SimulationContext />
      <ChevronCard title={t("gzl.simulation_portal.description")}>
        <RenderSanitizedHTML untrustedHTML={simulationSequence.description} />
      </ChevronCard>
      <ChevronCard title={t("gzl.simulation_portal.simulation_service_configuration")}>
        <SimulatedRoles />
      </ChevronCard>
      {simulationSequence?.supportedParameters &&
        <ChevronCard title={t("gzl.simulation_portal.sequence_configuration")}>
          <SequenceConfiguration />
        </ChevronCard>
      }
      <SimulationRunner />
      <ToastContainer />
    </div>
  )
}

export default SequenceRenderer;