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

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.test.SupportedTextInput;
import net.ihe.gazelle.maestro.spi.business.StepDefinition;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * Step definition for user interaction operations.
 * This step displays a message to the user and waits for acknowledgment.
 */
public class UserInteractionStepDefinition implements StepDefinition {

    @Serial
    private static final long serialVersionUID = 5740485033132348151L;

    /**
     * The type identifier for the user interaction step.
     */
    public static final String TYPE = "USER_INTERACTION";

    /**
     * Input property key for the interaction title.
     */
    public static final String INTERACTION_TITLE = "interactionTitle";

    /**
     * Input property key for the message to display to the user.
     */
    public static final String MESSAGE = "message";

    /**
     * Default constructor.
     */
    public UserInteractionStepDefinition() { /* Default constructor */ }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<SupportedInput> getSupportedInputs() {
        return List.of(
                new SupportedTextInput().setId(INTERACTION_TITLE).setLabel("Title").setRequired(true),
                new SupportedTextInput().setId(MESSAGE).setLabel("Message").setRequired(true)
        );
    }

    @Override
    public Map<String, Class<? extends Property>> getOutputsDefinition() {
        return Map.of();
    }
}
