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

package net.ihe.gazelle.maestro.engine.business.stub;

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.test.SupportedTextInput;
import net.ihe.gazelle.maestro.spi.business.StepDefinition;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class Instrumented4TestStepDefinition implements StepDefinition {

    @Serial
    private static final long serialVersionUID = -2058386010449962425L;

    public static final String TYPE = "UNIT_TEST";

    public static final String TEST_INPUT = "testInput";
    public static final String REQUIRED_INPUT = "requiredInput";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<SupportedInput> getSupportedInputs() {
        return List.of(
                new SupportedTextInput().setId(TEST_INPUT).setLabel("Test input").setRequired(false),
                new SupportedTextInput().setId(REQUIRED_INPUT).setLabel("Required input").setRequired(true)
        );
    }

    @Override
    public Map<String, Class<? extends Property>> getOutputsDefinition() {
        return Map.of();
    }
}
