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

import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.simulation.business.sequence.SimulationChecksumService;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.business.setup.SetupOutcome;
import net.ihe.gazelle.simulation.business.setup.SimulationRequest;
import net.ihe.gazelle.simulation.client.business.SimulationServiceClient;
import net.ihe.gazelle.simulation.jaxrs.api.technical.dto.sequence.SimulationSequenceDTO;
import net.ihe.gazelle.simulation.utils.ResourceRetriever;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SimulationServiceClientMock implements SimulationServiceClient {

    private final String serviceUrl;

    public SimulationServiceClientMock(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    @Override
    public String getChecksum(List<SimulationSequence> simulationSequences) {
        String sequences = new JacksonSerDes().serializeAsString(
                simulationSequences.stream()
                        .map(SimulationSequenceDTO::new)
                        .toList()
        );
        return SimulationChecksumService.computeChecksum(sequences);
    }

    @Override
    public List<SimulationSequence> getSimulationSequences() {
        List<SimulationSequenceDTO> simulationSequenceDTOList = getSimulationSequenceDTOs(serviceUrl);
        return simulationSequenceDTOList.stream()
                .map(SimulationSequenceDTO::getBusinessObject)
                .toList();
    }

    public List<SimulationSequenceDTO> getSimulationSequenceDTOs(String serviceUrl) {
        try {
            String sequences = ResourceRetriever.getResourceAsString("ut/" + serviceUrl);
            return Arrays.stream(new JacksonSerDes().deserialize(sequences, SimulationSequenceDTO[].class)).toList();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public SetupOutcome setup(SimulationRequest simulationRequest, String sessionId) {
        return null;
    }

    @Override
    public SetupOutcome resume(String simulationId) {
        return null;
    }
}
