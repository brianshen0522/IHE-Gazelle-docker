/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.maestro.assertion.step.business.contains;

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.test.SupportedTextInput;
import net.ihe.gazelle.maestro.spi.business.StepDefinition;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * Step definition for assertion that checks if the actual value contains the expected value.
 * This step verifies that a given string contains a specific substring.
 */
public class AssertContainsStepDefinition implements StepDefinition {

    @Serial
    private static final long serialVersionUID = 2642778063856137804L;

   /**
    * The type identifier for the assert contains step.
    */
   public static final String TYPE = "ASSERT_CONTAINS";

   // inputs
   /**
    * Input property key for the expected value (substring to search for).
    */
   public static final String EXPECTED = "expected";

   /**
    * Input property key for the actual value (string to search in).
    */
   public static final String ACTUAL = "actual";

   // outputs
   /**
    * Output property key for the failure message when the assertion fails.
    */
   public static final String FAILURE_MESSAGE = "failureMessage";

   /**
    * Default constructor.
    */
   public AssertContainsStepDefinition() {/* Default constructor */}


    @Override
   public String getType() {
      return TYPE;
   }

   @Override
   public List<SupportedInput> getSupportedInputs() {
      return List.of(
            new SupportedTextInput().setId(EXPECTED).setLabel("Expected").setRequired(true),
            new SupportedTextInput().setId(ACTUAL).setLabel("Actual").setRequired(true)
      );
   }

   @Override
   public Map<String, Class<? extends Property>> getOutputsDefinition() {
      return Map.of(
            FAILURE_MESSAGE, StringProperty.class
      );
   }
}
