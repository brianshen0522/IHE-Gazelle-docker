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

import net.ihe.gazelle.framework.modelvalidator.business.ObjectResult;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.test.Test;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.business.test.TestSuite;
import net.ihe.gazelle.maestro.api.business.testreport.EntityIdentification;
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;
import net.ihe.gazelle.security.business.acl.AccessControlList;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TestSuiteRunValidatorTest {

   private final TestSuiteRunValidator validator = new TestSuiteRunValidator();

   @org.junit.jupiter.api.Test
   void testValid() {
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun();
      assertDoesNotThrow(() -> validator.assertValid(testSuiteRun));
   }

   @org.junit.jupiter.api.Test
   void testMissingTestSuite() {
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun()
            .setTestSuite(null);

      var result = validator.validate(testSuiteRun);
      assertFailure(result, "Test Suite must be set => invalid");
   }

   @org.junit.jupiter.api.Test
   void testMissingReferencedTest() {
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun();
      testSuiteRun.getTestSuite().setTestReferences(List.of(
            new TestReference().setTestId("non-existing-test-id")
      ));

      var result = validator.validate(testSuiteRun);
      assertFailure(result,
            "All Tests referenced in the Test Suite must be attached to the Test Suite Run => invalid");
   }

   @org.junit.jupiter.api.Test
   void testInvalidTestSuite() {
      TestSuite invalidTestSuite = new TestSuite().setId("suite-1");
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun()
            .setTestSuite(invalidTestSuite);

      var result = validator.validate(testSuiteRun);
      assertFailure(result, "testSuite Test Suite name must be set and not empty => invalid");
   }

   @org.junit.jupiter.api.Test
   void testInvalidTest() {
      Test invalidTest = new Test().setId("test-1");
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun()
            .setTests(List.of(invalidTest));

      var result = validator.validate(testSuiteRun);
      assertFailure(result, "tests[0] Test must contain at least one step => invalid");
   }

   @org.junit.jupiter.api.Test
   void testInvalidInput() {
      Property invalidInput = new StringProperty("", "value");
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun()
            .setInputs(List.of(invalidInput));

      var result = validator.validate(testSuiteRun);
      assertFailure(result, "inputs[0] Name must be set and not empty => invalid");
   }

   @org.junit.jupiter.api.Test
   void testInvalidSUT() {
      SystemUnderTest invalidSUT = new SystemUnderTest();
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun()
            .setSystemsUnderTest(List.of(invalidSUT));

      var result = validator.validate(testSuiteRun);
      assertFailure(result, "systemUnderTest[0] systemIdentification must be set => invalid");
   }

   @org.junit.jupiter.api.Test
   void testInvalidACL() {
      AccessControlList invalidACL = new AccessControlList().setPublic(false);
      TestSuiteRun testSuiteRun = buildValidTestSuiteRun()
            .setAccessControlList(invalidACL);

      var result = validator.validate(testSuiteRun);
      assertFailure(result, "[SEC-004] Owner must be set if ACL is private => invalid");
   }

   private static void assertFailure(ObjectResult result, String expectedError) {
      assertFalse(result.isValid(), result::toString);
      assertThat(
            result.toString(),
            containsString(expectedError)
      );
   }

   private TestSuiteRun buildValidTestSuiteRun() {
      Property input = new StringProperty("suite-input", "value");
      SystemUnderTest systemUnderTest = new SystemUnderTest()
            .setSystemIdentification(new EntityIdentification("sut-1"));

      Test test = buildValidTest();

      TestSuite testSuite = new TestSuite()
            .setId("suite-1")
            .setName("Suite")
            .setTestReferences(List.of(new TestReference().setTestId(test.getId())));

      return new TestSuiteRun()
            .setTestSuite(testSuite)
            .setTests(List.of(test))
            .setInputs(List.of(input))
            .setSystemsUnderTest(List.of(systemUnderTest))
            .setAccessControlList(new AccessControlList().setPublic(true));
   }

   private Test buildValidTest() {

      return new Test()
            .setId("test-1")
            .setName("Sample test")
            .setSteps(List.of(
                  new Step()
                        .setName("Validate something")
                        .setType("validation"),
                  new Step()
                        .setName("Send message")
                        .setType("send")
            ));
   }
}
