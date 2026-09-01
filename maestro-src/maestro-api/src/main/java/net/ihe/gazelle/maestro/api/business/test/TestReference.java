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

import net.ihe.gazelle.maestro.api.business.property.Property;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A structure containing a reference to a test.
 */
public class TestReference implements Serializable {

   @Serial
   private static final long serialVersionUID = 1312276848438863528L;

   /**
    * The id of the referenced test.
    */
   private String testId;

   /**
    * The properties of the referenced test.
    */
   private List<Property> properties = new ArrayList<>();

   /**
    * Default constructor
    */
   public TestReference() {
      // Default constructor
   }

   /**
    * Retrieves the identifier of the referenced test.
    *
    * @return the test identifier as a String
    */
   public String getTestId() {
      return testId;
   }

   /**
    * Sets the identifier for the referenced test.
    *
    * @param testId the identifier of the test to be set
    * @return the current instance of {@code TestReference} for method chaining
    */
   public TestReference setTestId(String testId) {
      this.testId = testId;
      return this;
   }

   /**
    * Retrieves a list of properties associated with the referenced test.
    *
    * @return a list of {@code Property} elements representing the properties
    *         of the referenced test; the list is always non-null but may be empty
    */
   public List<Property> getProperties() {
      return new ArrayList<>(properties);
   }

   /**
    * Sets the properties for the referenced test.
    *
    * @param properties a list of {@code Property} elements to be assigned to the referenced test;
    *                   may be null, in which case the properties field is set to an empty list
    * @return the current instance of {@code TestReference} for method chaining
    */
   public TestReference setProperties(List<Property> properties) {
      this.properties = properties != null
              ? new ArrayList<>(properties)
              : new ArrayList<>();
      return this;
   }

   /**
    * Determines whether the test identifier has been defined.
    *
    * @return {@code true} if the test identifier is not null and not empty; {@code false} otherwise
    */
   public boolean isTestIdDefined() {
      return testId != null && !testId.isEmpty();
   }
}
