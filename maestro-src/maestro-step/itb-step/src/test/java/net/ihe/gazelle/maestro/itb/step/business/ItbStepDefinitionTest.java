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
package net.ihe.gazelle.maestro.itb.step.business;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItbStepDefinitionTest {

    @Test
    void simulation_step_definition_test() {
        ItbStepDefinition definition = new ItbStepDefinition();
        assertEquals(ItbStepDefinition.TYPE, definition.getType());
        assertEquals(4, definition.getSupportedInputs().size());
        assertEquals(ItbStepDefinition.TEST_CASE, definition.getSupportedInputs().getFirst().getId());
        assertEquals(ItbStepDefinition.ACTOR_ID, definition.getSupportedInputs().get(1).getId());
        assertEquals(ItbStepDefinition.EXECUTION_MODE, definition.getSupportedInputs().get(2).getId());
        assertEquals(ItbStepDefinition.ITB_PAYLOAD_MODE, definition.getSupportedInputs().get(3).getId());
        assertEquals(3, definition.getOutputsDefinition().size());
        assertEquals(ByteArrayProperty.class, definition.getOutputsDefinition().get(ItbStepDefinition.XML_REPORT));
        assertEquals(ByteArrayProperty.class, definition.getOutputsDefinition().get(ItbStepDefinition.PDF_REPORT));
        assertEquals(StringProperty.class, definition.getOutputsDefinition().get(ItbStepDefinition.LOGS));
    }
}
