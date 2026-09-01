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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A structure containing a list of tests.
 */
public class TestSuite implements Serializable {

   @Serial
   private static final long serialVersionUID = 6316961081179645096L;

   /**
    * The id of the test suite.
    */
   private String id;

   /**
    * The name of the test suite.
    */
   private String name;

   /**
    * The list of tests.
    */
   private List<TestReference> testReferences = new ArrayList<>();

   /**
    * The list of supported inputs.
    */
   private List<SupportedInput> supportedInputs = new ArrayList<>();

   /**
    * Default constructor
    */
   public TestSuite() {
      // Default constructor
   }

   /**
    * Retrieves the identifier of the test suite.
    *
    * @return the identifier of the test suite as a String
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the identifier of the test suite.
    *
    * @param id the identifier to be set for the test suite
    * @return the current instance of TestSuite
    */
   public TestSuite setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Retrieves the name of the test suite.
    *
    * @return the name of the test suite as a String
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name of the test suite.
    *
    * @param name the name to be set for the test suite
    * @return the current instance of TestSuite
    */
   public TestSuite setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Retrieves a list of {@code TestReference} objects associated with the test suite.
    *
    * @return a list of {@code TestReference} objects; the list is always non-null but may be empty
    */
   public List<TestReference> getTestReferences() {
      return new ArrayList<>(testReferences);
   }

   /**
    * Sets the list of test references for the TestSuite.
    *
    * @param testReferences a list of {@code TestReference} objects to be assigned to the test suite;
    *                       if null, the internal list will be initialized as an empty list
    * @return the current instance of {@code TestSuite} for method chaining
    */
   public TestSuite setTestReferences(List<TestReference> testReferences) {
      this.testReferences = testReferences != null
              ? new ArrayList<>(testReferences)
              : new ArrayList<>();
      return this;
   }

   /**
    * Retrieves a list of supported inputs associated with the test suite.
    *
    * @return a list of {@code SupportedInput} objects. The list is a copy of the internal
    * representation to ensure encapsulation; it is always non-null but may be empty.
    */
   public List<SupportedInput> getSupportedInputs() {
      return new ArrayList<>(supportedInputs);
   }

   /**
    * Sets the list of supported inputs for the test suite.
    *
    * @param supportedInputs a list of {@code SupportedInput} objects to be assigned to the test suite;
    *                        if null, the internal list will be set to a new empty list
    * @return the current instance of {@code TestSuite} for method chaining
    */
   public TestSuite setSupportedInputs(List<SupportedInput> supportedInputs) {
       this.supportedInputs = supportedInputs != null
               ? new ArrayList<>(supportedInputs)
               : new ArrayList<>();
       return this;
   }

   /**
    * Determines whether the name of the test suite has been defined.
    *
    * @return true if the name is not null and is not blank, false otherwise
    */
   public boolean isNameDefined() {
      return name != null && !name.isBlank();
   }

   /**
    * Determines whether the identifier (ID) of the test suite has been defined.
    *
    * @return true if the ID is not null and is not blank; false otherwise
    */
   public boolean isIdDefined() {
      return id != null && !id.isBlank();
   }

   /**
    * Determines whether the test suite contains at least one test reference.
    *
    * @return true if the test suite has at least one test reference, false otherwise
    */
   public boolean hasAtLeastOneTest() {
      return testReferences != null && !testReferences.isEmpty();
   }
}
