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

package net.ihe.gazelle.simulation.technical.dto;

import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.utils.ResourceRetriever;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResolvedSimulationSequenceDTOTest {

    private static String sequence;
    private static ResolvedSimulationSequenceDTO dto;

    @BeforeAll
    static void setUp() throws IOException {
        sequence = ResourceRetriever.getResourceAsString("ut/resolved-sequence.json");
        dto = new JacksonSerDes().deserialize(sequence, ResolvedSimulationSequenceDTO.class);
    }

    @Test
    void should_serialize_simulation_sequence() {
        ResolvedSimulationSequence simulationSequence = dto.getBusinessObject();
        ResolvedSimulationSequenceDTO newDto = new ResolvedSimulationSequenceDTO(simulationSequence);
        String newSequence = new JacksonSerDes().serializeAsString(newDto);
        assertEquals(sequence, newSequence);
    }
}
