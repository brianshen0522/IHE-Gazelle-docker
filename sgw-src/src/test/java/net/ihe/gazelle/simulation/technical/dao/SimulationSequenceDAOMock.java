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

package net.ihe.gazelle.simulation.technical.dao;

import net.ihe.gazelle.simulation.business.SimulationSequenceDAO;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.jaxrs.api.technical.dto.sequence.SimulationSequenceDTO;

import java.util.ArrayList;
import java.util.List;

public class SimulationSequenceDAOMock implements SimulationSequenceDAO {

    @Override
    public List<SimulationSequence> getSimulationSequences(String serviceUrl) {
        return new SimulationServiceClientMock(serviceUrl)
                .getSimulationSequenceDTOs(serviceUrl)
                .stream()
                .map(SimulationSequenceDTO::getBusinessObject)
                .toList();
    }

    @Override
    public String getServiceChecksum(String serviceUrl) {
        return  new SimulationServiceClientMock(serviceUrl)
                .getChecksum(new ArrayList<>());
    }
}
