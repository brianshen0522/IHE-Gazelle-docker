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

package net.ihe.gazelle.maestro.userinteraction.step.business;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserInteractionStepDefinitionTest {

    @Test
    void user_interaction_step_definition_test() {
        UserInteractionStepDefinition definition = new UserInteractionStepDefinition();
        assertEquals(UserInteractionStepDefinition.TYPE, definition.getType());
        assertEquals(2, definition.getSupportedInputs().size());
        assertEquals(UserInteractionStepDefinition.INTERACTION_TITLE, definition.getSupportedInputs().getFirst().getId());
        assertEquals(UserInteractionStepDefinition.MESSAGE, definition.getSupportedInputs().get(1).getId());
        assertTrue(definition.getOutputsDefinition().isEmpty());
    }
}
