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

package net.ihe.gazelle.maestro.assertion.step.business;

import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.assertion.step.business.contains.AssertContainsStepDefinition;
import net.ihe.gazelle.maestro.assertion.step.business.equals.AssertEqualsStepDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssertionStepDefinitionTest {

    @Test
    void assert_contains_definition_test() {
        AssertContainsStepDefinition definition = new AssertContainsStepDefinition();
        assertEquals(AssertContainsStepDefinition.TYPE, definition.getType());
        assertEquals(2, definition.getSupportedInputs().size());
        assertEquals(AssertContainsStepDefinition.EXPECTED, definition.getSupportedInputs().getFirst().getId());
        assertEquals(AssertContainsStepDefinition.ACTUAL, definition.getSupportedInputs().get(1).getId());
        assertEquals(1, definition.getOutputsDefinition().size());
        assertEquals(StringProperty.class, definition.getOutputsDefinition().get(AssertContainsStepDefinition.FAILURE_MESSAGE));
    }

    @Test
    void assert_equals_definition_test() {
        AssertEqualsStepDefinition definition = new AssertEqualsStepDefinition();
        assertEquals(AssertEqualsStepDefinition.TYPE, definition.getType());
        assertEquals(2, definition.getSupportedInputs().size());
        assertEquals(AssertContainsStepDefinition.EXPECTED, definition.getSupportedInputs().getFirst().getId());
        assertEquals(AssertContainsStepDefinition.ACTUAL, definition.getSupportedInputs().get(1).getId());
        assertEquals(1, definition.getOutputsDefinition().size());
        assertEquals(StringProperty.class, definition.getOutputsDefinition().get(AssertContainsStepDefinition.FAILURE_MESSAGE));
    }
}
