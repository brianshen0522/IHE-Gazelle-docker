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
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;
import net.ihe.gazelle.security.business.acl.AccessControlList;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TestRun is the execution of a Test with specific inputs.
 */
public class TestRun implements Serializable {

   @Serial
   private static final long serialVersionUID = -6069446503327802519L;

   /**
    * The test to execute.
    */
   private Test test = null;

   /**
    * The inputs to the test.
    */
   private List<Property> inputs;

   /**
    * The systems under test.
    */
   private List<SystemUnderTest> systemsUnderTest;

   /**
    * The access control list of the test run.
    */
   private AccessControlList accessControlList;

   /**
    * Default constructor
    */
   public TestRun() {
      this.inputs = new ArrayList<>();
      this.systemsUnderTest = new ArrayList<>();
   }

   /**
    * Creates a new TestRun instance by copying the data from another TestRun instance.
    *
    * @param other the TestRun instance to copy. Must not be null.
    */
   public TestRun(TestRun other) {
      this.test = other.test;
      this.inputs = new ArrayList<>(other.inputs);
      this.systemsUnderTest = new ArrayList<>(other.systemsUnderTest);
      this.accessControlList = other.accessControlList;
   }

   /**
    * Retrieves the {@code Test} instance associated with this {@code TestRun}.
    *
    * @return the {@code Test} instance representing the test to execute
    */
   public Test getTest() {
      return test;
   }

   /**
    * Sets the {@code Test} instance associated with this {@code TestRun} and returns
    * the current {@code TestRun} instance for method chaining.
    *
    * @param test the {@code Test} instance to be associated with this {@code TestRun}.
    *             This represents the test configuration and steps.
    * @return the current instance of {@code TestRun} for method chaining.
    */
   public TestRun setTest(Test test) {
      this.test = test;
      return this;
   }

   /**
    * Retrieves the list of input properties associated with the TestRun.
    *
    * @return a new List containing all input properties. Modifications to the returned
    *         list do not affect the internal list maintained by the TestRun instance.
    */
   public List<Property> getInputs() {
      return new ArrayList<>(inputs);
   }

   /**
    * Sets the list of input properties for this TestRun instance and returns the
    * updated instance for method chaining.
    *
    * @param inputs the list of {@code Property} objects to be set as inputs.
    *               A new list is created internally to ensure immutability of
    *               the provided list.
    * @return the current {@code TestRun} instance after setting the inputs,
    *         allowing for method chaining.
    */
   public TestRun setInputs(List<Property> inputs) {
      this.inputs = new ArrayList<>(inputs);
      return this;
   }

   /**
    * Adds the specified {@code Property} object to the list of input properties associated
    * with this {@code TestRun} instance and returns the current instance for method chaining.
    *
    * @param property the {@code Property} object to be added to the list of inputs.
    *                 This represents an input parameter to be used during the test run.
    * @return the current {@code TestRun} instance after adding the input property,
    *         allowing for method chaining.
    */
   public TestRun addInput(Property property) {
      inputs.add(property);
      return this;
   }

   /**
    * Retrieves the list of systems under test associated with this {@code TestRun} instance.
    *
    * @return a new List containing all systems under test.
    *         Modifications to the returned list do not affect the internal list
    *         maintained by the {@code TestRun} instance.
    */
   public List<SystemUnderTest> getSystemsUnderTest() {
      return new ArrayList<>(systemsUnderTest);
   }

   /**
    * Sets the list of systems under test for this {@code TestRun} instance and returns
    * the updated instance for method chaining.
    *
    * @param systemsUnderTest the list of {@code SystemUnderTest} instances to associate
    *                         with this test run. If {@code null}, an empty list is used.
    * @return the current {@code TestRun} instance after setting the systems under test,
    *         allowing for method chaining.
    */
   public TestRun setSystemsUnderTest(List<SystemUnderTest> systemsUnderTest) {
      this.systemsUnderTest = systemsUnderTest != null
              ? new ArrayList<>(systemsUnderTest)
              : new ArrayList<>();
      return this;
   }

   /**
    * Retrieves the access control list associated with this {@code TestRun}.
    *
    * @return the {@code AccessControlList} defining permissions for this test run,
    *         or {@code null} if no access control list is defined.
    */
   public AccessControlList getAccessControlList() {
      return accessControlList;
   }

   /**
    * Sets the access control list for this {@code TestRun} instance and returns
    * the updated instance for method chaining.
    *
    * @param accessControlList the {@code AccessControlList} defining permissions
    *                          for this test run.
    * @return the current {@code TestRun} instance after setting the access control list,
    *         allowing for method chaining.
    */
   public TestRun setAccessControlList(AccessControlList accessControlList) {
      this.accessControlList = accessControlList;
      return this;
   }

   /**
    * Indicates whether a {@code Test} is defined for this {@code TestRun}.
    *
    * @return {@code true} if a test is associated with this test run,
    *         {@code false} otherwise.
    */
   public boolean isTestDefined() {
      return test != null;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof TestRun testRun)) {
         return false;
      }
      return Objects.equals(test, testRun.test) && Objects.equals(inputs,
            testRun.inputs) && Objects.equals(systemsUnderTest,
            testRun.systemsUnderTest) && Objects.equals(accessControlList, testRun.accessControlList);
   }

   @Override
   public int hashCode() {
      return Objects.hash(test, inputs, systemsUnderTest, accessControlList);
   }
}

