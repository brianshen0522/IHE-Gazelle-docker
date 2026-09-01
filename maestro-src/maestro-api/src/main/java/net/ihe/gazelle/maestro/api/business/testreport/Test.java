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

package net.ihe.gazelle.maestro.api.business.testreport;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A structure for a test.
 * Business rules <br>
 * id is mandatory
 */
public class Test implements Serializable {

   @Serial
   private static final long serialVersionUID = -1682879799680526090L;

   /**
    * The id of the test.
    */
   private String id;

   /**
    * The name of the test.
    */
   private String name;

   /**
    * The version of the test.
    */
   private String version;

   /**
    * The description of the test.
    */
   private String description;

   /**
    * Default constructor.
    */
   public Test() {
   }

   /**
    * Constructs a new Test instance with the specified id.
    *
    * @param id the unique identifier for the test; must not be null or blank
    */
   public Test(String id) {
      this.id = id;
   }

   /**
    * Retrieves the ID of the test.
    *
    * @return the ID of the test as a String
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the ID of the test.
    *
    * @param id the unique identifier for the test; must not be null or blank
    * @return the current instance of {@code Test} to allow method chaining
    */
   public Test setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Retrieves the name of the test.
    *
    * @return the name of the test as a String
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name of the test.
    *
    * @param name the name of the test
    * @return the current instance of {@code Test} to allow method chaining
    */
   public Test setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Retrieves the version of the test.
    *
    * @return the version of the test as a String
    */
   public String getVersion() {
      return version;
   }

   /**
    * Sets the version of the test.
    *
    * @param version the version of the test as a String
    * @return the current instance of {@code Test} to allow method chaining
    */
   public Test setVersion(String version) {
      this.version = version;
      return this;
   }

   /**
    * Retrieves the description of the current instance.
    *
    * @return the description as a String
    */
   public String getDescription() {
      return description;
   }

   /**
    * Sets the description of the test.
    *
    * @param description the description of the test
    * @return the current instance of {@code Test} to allow method chaining
    */
   public Test setDescription(String description) {
      this.description = description;
      return this;
   }

   /**
    * Checks if the ID is defined for this instance.
    *
    * @return true if the ID is defined, false otherwise
    */
   public boolean isIdDefined() {
      return id != null && !id.isBlank();
   }

   /**
    * Checks whether the name is defined if it is present.
    *
    * @return true if the name is either null or non-blank; false if the name is blank
    */
   public boolean isNameDefinedIfPresent() {
      return name == null || !name.isBlank();
   }

   /**
    * Checks whether the version is defined if it is present.
    *
    * @return true if the version is either null or non-blank; false otherwise
    */
   public boolean isVersionDefinedIfPresent() {
      return version == null || !version.isBlank();
   }

   /**
    * Checks whether the description is considered defined if it is present.
    *
    * @return true if the description is null or non-blank; false if the description is blank
    */
   public boolean isDescriptionDefinedIfPresent() {
      return description == null || !description.isBlank();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Test test)) return false;
      return Objects.equals(id, test.id)
            && Objects.equals(name, test.name)
            && Objects.equals(version, test.version)
            && Objects.equals(description, test.description);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, name, version, description);
   }
}
