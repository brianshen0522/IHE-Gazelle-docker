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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TestSuiteValidatorTest {

   private final TestSuiteValidator validator = new TestSuiteValidator();

   @Test
   void testMinimalValid() {
      TestSuite testSuite = minimalTestSuite();
      assertDoesNotThrow(() -> validator.assertValid(testSuite));
   }

   @Test
   void testMissingID() {
      TestSuite testSuite = minimalTestSuite().setId(null);
      var result = validator.validate(testSuite);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("Test Suite ID must be set and not empty => invalid")
      );
   }

   @Test
   void testMissingName() {
      TestSuite testSuite = minimalTestSuite().setName(null);
      var result = validator.validate(testSuite);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("Test Suite name must be set and not empty => invalid")
      );
   }

   @Test
   void testNoTests() {
      TestSuite testSuite = minimalTestSuite().setTestReferences(List.of());
      var result = validator.validate(testSuite);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("There must be at least one Test in the Test Suite => invalid")
      );
   }

   @Test
   void testMissingTestReferenceID() {
      TestSuite testSuite = minimalTestSuite()
            .setTestReferences(List.of(new TestReference().setTestId(null)));
      var result = validator.validate(testSuite);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("testReferences[0] Test ID must be set and not empty => invalid")
      );
   }

   @Test
   void testInvalidSupportedInput() {
      TestSuite testSuite = minimalTestSuite()
            .setSupportedInputs(List.of(
                  new SupportedTextInput().setId(null).setLabel("a label")
            ));
      var result = validator.validate(testSuite);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("supportedInputs[0] ID must be set and not empty => invalid")
      );
   }

   private static TestSuite minimalTestSuite() {
      return new TestSuite()
            .setId("123456")
            .setName("My amazing Test Suite")
            .setTestReferences(List.of(new TestReference().setTestId("test1")));
   }

}
