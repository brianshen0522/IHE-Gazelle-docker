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
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.model.ResolvedSupportedParameter;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.sequence.*;
import net.ihe.gazelle.simulation.business.setup.ParameterType;
import net.ihe.gazelle.simulation.business.svs.SimulationSVSService;
import net.ihe.gazelle.simulation.business.svs.SimulationSVSServiceImpl;
import net.ihe.gazelle.simulation.technical.config.ApplicationConfigMock;
import net.ihe.gazelle.svs.client.business.SVSServiceImpl;
import net.ihe.gazelle.svs.client.technical.SVSHttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static net.ihe.gazelle.simulation.WireMockSingleton.SVS_VALUE_SET_ID;
import static net.ihe.gazelle.svs.client.business.SVSNotReachableException.SERVER_UNREACHABLE;
import static net.ihe.gazelle.svs.client.business.UnknownValueSetException.UNKNOWN_VALUE_SET;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimulationSVSServiceTest {

    @ConfigProperty(name = "gzl.it-port")
    int port;

    @ConfigProperty(name = "gzl.svs.url")
    String svsUrl;

    private SimulationSVSService svsService;

    @BeforeAll
    void setup() throws IOException {
        WireMockSingleton.startServer(port);
        WireMockSingleton.mockSVSSimulator();
        WireMockSingleton.mockResolvedSimulationSequence();
        svsService = new SimulationSVSServiceImpl(getSvsService(svsUrl));
    }

    @Test
    void should_retrieve_value_set() {
        SimulationSequence simulationSequence = getSimulationSequence(SVS_VALUE_SET_ID);
        SimulationSequenceExtended simulationSequenceExtended = new SimulationSequenceExtended(simulationSequence);
        ResolvedSimulationSequence resolved = svsService.resolveValueSets(simulationSequenceExtended);
        assertEquals(1, resolved.getSupportedParameters().size());
        ResolvedSupportedParameter parameter = resolved.getSupportedParameters().getFirst();
        assertEquals(3, parameter.getOptions().size());
        assertEquals("AUCUN", parameter.getOptions().getFirst().getValue());
    }

    @Test
    void should_return_unreachable_error() {
        SimulationSequence simulationSequence = getSimulationSequence(SVS_VALUE_SET_ID);
        SimulationSVSService svsService = new SimulationSVSServiceImpl(getSvsService(new ApplicationConfigMock().getSvsUrl()));
        SimulationSequenceExtended simulationSequenceExtended = new SimulationSequenceExtended(simulationSequence);
        ResolvedSimulationSequence resolved = svsService.resolveValueSets(simulationSequenceExtended);
        assertEquals(1, resolved.getSupportedParameters().size());
        ResolvedSupportedParameter parameter = resolved.getSupportedParameters().getFirst();
        assertEquals(SERVER_UNREACHABLE, parameter.getError());
    }

    @Test
    void should_return_unknown_value_set_error() {
        String valueSetId = "unknown";
        SimulationSequence simulationSequence = getSimulationSequence(valueSetId);
        SimulationSequenceExtended simulationSequenceExtended = new SimulationSequenceExtended(simulationSequence);
        ResolvedSimulationSequence resolved = svsService.resolveValueSets(simulationSequenceExtended);
        assertEquals(1, resolved.getSupportedParameters().size());
        ResolvedSupportedParameter parameter = resolved.getSupportedParameters().getFirst();
        assertTrue(parameter.getError().contains(String.format(UNKNOWN_VALUE_SET, valueSetId)));
    }

    @Test
    void should_throw_unexpected_error() {
        SimulationSequence simulationSequence = getSimulationSequence("invalid");
        SimulationSequenceExtended simulationSequenceExtended = new SimulationSequenceExtended(simulationSequence);
        ResolvedSimulationSequence resolved = svsService.resolveValueSets(simulationSequenceExtended);
        assertEquals(1, resolved.getSupportedParameters().size());
        ResolvedSupportedParameter parameter = resolved.getSupportedParameters().getFirst();
        assertNotNull(parameter.getError());
    }

    @Test
    void should_throw_http_error() {
        SimulationSequence simulationSequence = getSimulationSequence("500");
        SimulationSequenceExtended simulationSequenceExtended = new SimulationSequenceExtended(simulationSequence);
        ResolvedSimulationSequence resolved = svsService.resolveValueSets(simulationSequenceExtended);
        assertEquals(1, resolved.getSupportedParameters().size());
        ResolvedSupportedParameter parameter = resolved.getSupportedParameters().getFirst();
        assertNotNull(parameter.getError());
    }

    private SimulationSequence getSimulationSequence(String valueSetId) {
        return new SimulationSequenceBuilder()
                .setId("value set sequence")
                .addSimulatedRole(new SimulatedRoleBuilder()
                        .setName("simulated role")
                        .setType(RoleType.INITIATOR)
                )
                .addTestRole(new TestedRoleBuilder()
                        .setName("tested role")
                        .setType(RoleType.RESPONDER)
                )
                .addSupportedParameter(new SupportedParameterBuilder()
                        .setName("simulated parameter")
                        .setGroupName("group name")
                        .setDescription("value set parameter")
                        .setType(ParameterType.TEXT)
                        .setValueSetId(valueSetId)
                )
                .build();
    }

    private SVSServiceImpl getSvsService(String svsUrl) {
        return new SVSServiceImpl(new SVSHttpClient(svsUrl));
    }
}
