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

package net.ihe.gazelle.maestro.api.business.test;

import net.ihe.gazelle.framework.modelvalidator.business.ObjectResult;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.wildfly.common.Assert.assertFalse;

class StepValidatorTest {

   @Test
   void testStepMustHaveName() {
      Step step = new Step().setName(null).setType("ACTION");
      StepValidator validator = new StepValidator();
      assertInvalid(validator.validate(step), "Step Name must be set and not empty => invalid");

   }

   @Test
   void testStepMustHaveType() {
      Step step = new Step().setName("First Step").setType(null);
      StepValidator validator = new StepValidator();
      assertInvalid(validator.validate(step), "Step Type must be set and not empty => invalid");
   }

   @Test
   void testStepInvalidOutputMapping() {
      StepValidator validator = new StepValidator();
      Map<String, String> mappings1 = new HashMap<>();
      mappings1.put("validKey", "validValue");
      mappings1.put("keyOnly", "");
      Map<String, String> mappings2 = new HashMap<>();
      mappings2.put("validKey", "validValue");
      mappings2.put("", "valueOnly");

      Step step = new Step()
            .setName("Step with empty output name")
            .setType("ACTION")
            .setOutputMappings(mappings1);
      assertInvalid(validator.validate(step), "Both key and value of an OutputMapping must be defined if present => invalid");

      step = new Step()
            .setName("Step with empty property name")
            .setType("ACTION")
            .setOutputMappings(mappings2);
      assertInvalid(validator.validate(step), "Both key and value of an OutputMapping must be defined if present => invalid");
   }

   @Test
   void testInvalidProperties() {
      Step step = new Step()
            .setName("Step with valid properties")
            .setType("ACTION")
            .setProperties(List.of(
                  new StringProperty("  ", "invalidKey"),
                  new StringProperty("prop2", "value2")
            ));
      StepValidator validator = new StepValidator();
      assertInvalid(validator.validate(step), "Step.properties[0] Name must be set and not empty => invalid");
   }

   private static void assertInvalid(ObjectResult report, String expectedMessage) {
      assertThat(
            report.toString(),
            containsString(expectedMessage)
      );
      assertFalse(report.isValid());
   }

}
