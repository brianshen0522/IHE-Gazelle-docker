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

import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.client.business.SimulationServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SimulationSequenceDAOImplTest {

    private SimulationSequenceDAOImpl dao;

    @BeforeEach
    void setUp() {
        dao = new SimulationSequenceDAOImpl() {
            @Override
            protected SimulationServiceClient createClient(String serviceUrl) {
                return new SimulationServiceClientMock(serviceUrl);
            }
        };
    }

    @Test
    void should_get_simulation_sequences() {
        List<SimulationSequence> result = dao.getSimulationSequences("XDS_Simulator.json");
        assertEquals(2, result.size());

        SimulationSequence first = result.getFirst();
        assertFalse(first.getDescription().contains("<script>"));
    }
}
