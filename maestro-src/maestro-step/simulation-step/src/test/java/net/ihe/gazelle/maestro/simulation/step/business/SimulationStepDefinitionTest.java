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

package net.ihe.gazelle.maestro.simulation.step.business;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationStepDefinitionTest {

    @Test
    void simulation_step_definition_test() {
        SimulationStepDefinition definition = new SimulationStepDefinition();
        assertEquals(SimulationStepDefinition.TYPE, definition.getType());
        assertEquals(2, definition.getSupportedInputs().size());
        assertEquals(SimulationStepDefinition.SIMULATION_SERVICE, definition.getSupportedInputs().getFirst().getId());
        assertEquals(SimulationStepDefinition.SEQUENCE_ID, definition.getSupportedInputs().get(1).getId());
        assertEquals(1, definition.getOutputsDefinition().size());
        assertEquals(ByteArrayProperty.class, definition.getOutputsDefinition().get(SimulationStepDefinition.REPORT));
    }
}
