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

package net.ihe.gazelle.maestro.simulation.step.technical.factory;

import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.userinteract.UserInteractionHandler;
import net.ihe.gazelle.maestro.api.business.userinteract.stub.UserInteractionHandlerStub;
import net.ihe.gazelle.maestro.simulation.step.business.SimulationStepDefinition;
import net.ihe.gazelle.maestro.simulation.step.business.SimulationStepExecutor;
import net.ihe.gazelle.maestro.simulation.step.technical.handler.GazelleSimulationServiceHandler;
import net.ihe.gazelle.maestro.simulation.step.technical.mock.SimulationClientMock;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimulationStepExecutorFactoryTest {

    private final SimulationStepExecutorFactory factory = new SimulationStepExecutorFactory();

    @Test
    void stepTypeTest() {
        assertEquals(SimulationStepDefinition.TYPE, factory.getSupportedStep());
    }

    @Test
    void getRequiredServicesOfSimulationStep(){
        Step simulationStep = getSimulationStep();
        assertEquals(2, factory.getRequiredServices(simulationStep).size());
    }

    @Test
    void createStepExecutorTest() {
        Step simulationStep = getSimulationStep();
        Map<String, Handler> onlyUserInteractionService = Map.of(
                UserInteractionHandler.SERVICE_NAME,
                new UserInteractionHandlerStub()
        );
        Map<String, Handler> onlySimulationService = Map.of(
                SimulationStepDefinition.SIMULATION_SERVICE,
                new GazelleSimulationServiceHandler(new SimulationClientMock())
        );

        assertThrows(IllegalArgumentException.class,
                () -> factory.createStepExecutor(simulationStep, onlyUserInteractionService));
        assertThrows(IllegalArgumentException.class,
                () -> factory.createStepExecutor(simulationStep, onlySimulationService));

        StepExecutor executor = factory.createStepExecutor(simulationStep, Map.of(
                SimulationStepDefinition.SIMULATION_SERVICE,
                new GazelleSimulationServiceHandler(new SimulationClientMock()),
                UserInteractionHandler.SERVICE_NAME,
                new UserInteractionHandlerStub()
        ));
        assertInstanceOf(SimulationStepExecutor.class, executor);
    }

    private Step getSimulationStep() {
        return new Step()
                .setName("simulationStep")
                .setType(SimulationStepDefinition.TYPE)
                .setProperties(List.of(
                        new StringProperty(SimulationStepDefinition.SIMULATION_SERVICE, SimulationStepDefinition.SIMULATION_SERVICE),
                        new StringProperty(SimulationStepDefinition.SEQUENCE_ID, "sequenceId")
                ));
    }

}
