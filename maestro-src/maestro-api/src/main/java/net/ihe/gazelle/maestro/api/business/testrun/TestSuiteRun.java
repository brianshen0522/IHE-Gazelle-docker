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
import net.ihe.gazelle.maestro.api.business.test.Test;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.business.test.TestSuite;
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;
import net.ihe.gazelle.security.business.acl.AccessControlList;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A TestSuiteRun is a structure containing several TestRun that will be executed iteratively. {@link TestRun} testSetId
 * an id that will be spread to each element of testRuns <br> testRuns the list of testRuns to execute
 * {@link AccessControlList} accessControlList an ACL that will be spread to each element of testRuns <br>
 * systemsUnderTest the list of systems under test that will be spread to each element of testRuns <br> testSetRunId
 * today maps the TestInstance ID in Gazelle Test Management
 */
public class TestSuiteRun implements Serializable {

   @Serial
   private static final long serialVersionUID = -4027777114820632058L;

   private static final int TIMEOUT_MARGIN_OFFSET = 3000;

   /**
    * Timeout margin added to the computed test suite timeout.
    */
   private TestSuite testSuite;

   /**
    * The list of tests associated with this test suite run.
    */
   private List<Test> tests;

   /**
    * The input properties shared by all test runs.
    */
   private List<Property> inputs;

   /**
    * The systems under test shared by all test runs.
    */
   private List<SystemUnderTest> systemsUnderTest;

   /**
    * The access control list applied to all test runs.
    */
   private AccessControlList accessControlList;

   /**
    * Default constructor
    */
   public TestSuiteRun() {
      this.tests = new ArrayList<>();
      this.inputs = new ArrayList<>();
      this.systemsUnderTest = new ArrayList<>();
   }

   /**
    * Computes the total timeout for this test suite run.
    *
    * @return the computed timeout including a safety margin
    */
   public long computeTestSuiteTimeout() {
      return testSuite.getTestReferences().stream()
            .map(testReference -> getTestById(testReference.getTestId()))
            .mapToLong(Test::computeTestRunTimeout)
            .sum() + TIMEOUT_MARGIN_OFFSET;
   }

   private Test getTestById(String testId) {
      return tests.stream()
            .filter(test -> testId.equals(test.getId()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No test with id " + testId + " found."));
   }

   /**
    * Retrieves the test suite definition.
    *
    * @return the test suite
    */
   public TestSuite getTestSuite() {
      return testSuite;
   }

   /**
    * Sets the test suite definition.
    *
    * @param testSuite the test suite to set
    * @return the current {@code TestSuiteRun} instance
    */
   public TestSuiteRun setTestSuite(TestSuite testSuite) {
      this.testSuite = testSuite;
      return this;
   }

   /**
    * Retrieves the list of tests.
    *
    * @return a copy of the list of tests
    */
   public List<Test> getTests() {
      return new ArrayList<>(tests);
   }

   /**
    * Sets the list of tests.
    *
    * @param tests the tests to associate
    * @return the current {@code TestSuiteRun} instance
    */
   public TestSuiteRun setTests(List<Test> tests) {
      this.tests = tests != null
            ? new ArrayList<>(tests)
            : new ArrayList<>();
      return this;
   }

   /**
    * Retrieves the input properties.
    *
    * @return a copy of the input properties
    */
   public List<Property> getInputs() {
      return new ArrayList<>(inputs);
   }

   /**
    * Sets the input properties.
    *
    * @param inputs the input properties to set
    * @return the current {@code TestSuiteRun} instance
    */
   public TestSuiteRun setInputs(List<Property> inputs) {
      this.inputs = inputs != null
            ? new ArrayList<>(inputs)
            : new ArrayList<>();
      return this;
   }

   /**
    * Retrieves the systems under test.
    *
    * @return a copy of the systems under test
    */
   public List<SystemUnderTest> getSystemsUnderTest() {
      return new ArrayList<>(systemsUnderTest);
   }

   /**
    * Sets the systems under test.
    *
    * @param systemsUnderTest the systems under test to set
    * @return the current {@code TestSuiteRun} instance
    */
   public TestSuiteRun setSystemsUnderTest(List<SystemUnderTest> systemsUnderTest) {
      this.systemsUnderTest = systemsUnderTest != null
            ? new ArrayList<>(systemsUnderTest)
            : new ArrayList<>();
      return this;
   }

   /**
    * Retrieves the access control list.
    *
    * @return the access control list
    */
   public AccessControlList getAccessControlList() {
      return accessControlList;
   }

   /**
    * Sets the access control list.
    *
    * @param accessControlList the access control list to set
    * @return the current {@code TestSuiteRun} instance
    */
   public TestSuiteRun setAccessControlList(AccessControlList accessControlList) {
      this.accessControlList = accessControlList != null ? accessControlList : new AccessControlList();
      return this;
   }

   /**
    * Retrieves a test by its identifier.
    *
    * @param id the test identifier
    * @return the matching test
    * @throws NoSuchElementException if no test matches the given identifier
    */
   public Test getTest(String id) {
      return tests.stream()
            .filter(test -> test.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No test with id " + id + " found in this TestSuiteRun"));
   }

   /**
    * Indicates whether a test suite is defined.
    *
    * @return {@code true} if a test suite is defined, {@code false} otherwise
    */
   public boolean isTestSuiteDefined() {
      return testSuite != null;
   }

   /**
    * Checks whether all tests referenced by the test suite are attached.
    *
    * @return {@code true} if all referenced tests are attached, {@code false} otherwise
    */
   public boolean areAllReferencedTestsAttached() {
      if (testSuite == null || tests == null) {
         return false;
      }
      List<String> includedTestIds = tests.stream().map(Test::getId).toList();
      for (TestReference testRef : testSuite.getTestReferences()) {
         if (!includedTestIds.contains(testRef.getTestId())) {
            return false;
         }
      }
      return true;
   }
}
