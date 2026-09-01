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

package net.ihe.gazelle.simulation;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.simulation.business.SimulationSequenceDAO;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimulationSequenceClientIT {

    @Inject
    SimulationSequenceDAO simulationSequenceDAO;

    @ConfigProperty(name = "gzl.it-port")
    int port;

    @BeforeAll
    public void setup() throws IOException {
        WireMockSingleton.startServer(port);
        WireMockSingleton.mockSimulationSequence();
    }

    @Test
    void testWithValidSequence() {
        String baseUrl = WireMockSingleton.getWireMockServer().baseUrl();
        List<SimulationSequence> simulationSequences = simulationSequenceDAO.getSimulationSequences(baseUrl);
        assertEquals(3, simulationSequences.size());
    }
}
