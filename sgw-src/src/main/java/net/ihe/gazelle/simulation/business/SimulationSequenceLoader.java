/*
 * Copyright 2025-2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.simulation.business;

import net.ihe.gazelle.framework.modelvalidator.business.RuleResult;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.model.SimulationServiceInfo;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequenceValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link LoadSimulationSequence} interface.
 */
public class SimulationSequenceLoader implements LoadSimulationSequence {

   private final ServiceRegistryDAO serviceRegistryDAO;
   private final SimulationSequenceDAO simulationSequenceDAO;

   /**
    * Constructs a new instance of SimulationSequenceLoader.
    *
    * @param serviceRegistryDAO    the DAO to interact with the service registry,
    *                              used to retrieve available simulation services
    * @param simulationSequenceDAO the DAO to interact with simulation sequences,
    *                              used to retrieve sequences for specified simulation services
    */
   public SimulationSequenceLoader(ServiceRegistryDAO serviceRegistryDAO, SimulationSequenceDAO simulationSequenceDAO) {
      this.serviceRegistryDAO = serviceRegistryDAO;
      this.simulationSequenceDAO = simulationSequenceDAO;
   }

   @Override
   public List<SimulationSequenceExtended> getSupportedSequences() {
      List<SimulationSequenceExtended> simulationSequencesExtended = new ArrayList<>();
      List<SimulationServiceInfo> simulationServiceInfos = serviceRegistryDAO.getAvailableSimulationServices();
      for (SimulationServiceInfo simulationServiceInfo : simulationServiceInfos) {
         List<SimulationSequence> simulationSequences = simulationSequenceDAO.getSimulationSequences(simulationServiceInfo.getSimulatorUrl());
         for (SimulationSequence simulationSequence : simulationSequences) {
            SimulationSequenceExtended simulationSequenceExtended = new SimulationSequenceExtended(simulationSequence)
                  .setSimulatorName(simulationServiceInfo.getSimulatorName())
                  .setSimulatorVersion(simulationServiceInfo.getSimulatorVersion())
                  .setSimulatorUrl(simulationServiceInfo.getSimulatorUrl());
            validateSequence(simulationSequence, simulationSequenceExtended);
            simulationSequencesExtended.add(simulationSequenceExtended);
         }
      }
      return simulationSequencesExtended;
   }

   private void validateSequence(SimulationSequence simulationSequence, SimulationSequenceExtended simulationSequenceExtended) {
      List<RuleResult> invalidRules = new SimulationSequenceValidator()
            .validate(simulationSequence)
            .getAllInvalidRules();
      if (!invalidRules.isEmpty()) {
         simulationSequenceExtended.setValid(false);
         StringBuilder result = new StringBuilder().append("\n");
         for (RuleResult ruleResult : invalidRules) {
            result.append(ruleResult.toString()).append("\n");
         }
         simulationSequenceExtended.setValidReportMessage(result.toString());
      }
   }
}
