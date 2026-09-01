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

package net.ihe.gazelle.maestro.api.business.testrun;

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.test.Test;
import net.ihe.gazelle.maestro.api.business.testreport.EntityIdentification;
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import org.junit.jupiter.api.Assertions;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TestRunValidatorTest {

   private final TestRunValidator validator = new TestRunValidator();

   @org.junit.jupiter.api.Test
   void shouldValidateTestRunWhenMandatoryMembersPresent() {
      TestRun testRun = buildValidTestRun();
      Assertions.assertDoesNotThrow(() -> validator.assertValid(testRun));
   }

   @org.junit.jupiter.api.Test
   void shouldRejectTestRunWithoutTest() {
      TestRun testRun = buildValidTestRun().setTest(null);

      var result = validator.validate(testRun);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("Test must be defined => invalid")
      );
   }

   @org.junit.jupiter.api.Test
   void testInvalidAcl() {
      TestRun testRun = buildValidTestRun()
            .setAccessControlList(new AccessControlList().setPublic(false));

      var result = validator.validate(testRun);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("[SEC-004] Owner must be set if ACL is private => invalid")
      );
   }

   @org.junit.jupiter.api.Test
   void testInvalidSUT() {
      TestRun testRun = buildValidTestRun()
            .setSystemsUnderTest(List.of(new SystemUnderTest()));

      var result = validator.validate(testRun);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("systemsUnderTest[0] systemIdentification must be set => invalid")
      );
   }

   @org.junit.jupiter.api.Test
   void testInvalidInput() {
      TestRun testRun = buildValidTestRun()
            .setInputs(List.of(new StringProperty(null, "value")));

      var result = validator.validate(testRun);
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString("inputs[0] Name must be set and not empty => invalid")
      );
   }

   private TestRun buildValidTestRun() {
      Property input = new StringProperty("input-key", "value");
      SystemUnderTest systemUnderTest = new SystemUnderTest()
            .setSystemIdentification(new EntityIdentification("sut-1"));

      return new TestRun()
            .setTest(buildValidTest())
            .setInputs(List.of(input))
            .setSystemsUnderTest(List.of(systemUnderTest))
            .setAccessControlList(new AccessControlList().setPublic(true));
   }

   private Test buildValidTest() {
      Step step = new Step()
            .setName("Validate something")
            .setType("validation");

      return new Test()
            .setId("test-1")
            .setName("Sample test")
            .setSteps(List.of(step));
   }
}
