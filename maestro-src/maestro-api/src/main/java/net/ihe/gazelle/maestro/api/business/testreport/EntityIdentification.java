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
 * Identification of an entity.
 * Business rules: <br> name is mandatory
 */
public class EntityIdentification implements Serializable {

   @Serial
   private static final long serialVersionUID = 8403054616731250869L;

   /**
    * The version of the entity.
    */
   private String version;

   /**
    * The name of the entity.
    */
   private String name;

   /**
    * Default constructor
    */
   public EntityIdentification() {
   }

   /**
    * Constructs an EntityIdentification with the specified name.
    *
    * @param name the name of the entity; must not be null or blank
    */
   public EntityIdentification(String name) {
      this.name = name;
   }

   /**
    * Constructs an EntityIdentification with the specified version and name.
    *
    * @param version the version of the entity; can be null or blank
    * @param name the name of the entity; must not be null or blank
    */
   public EntityIdentification(String version, String name) {
      this(name);
      this.version = version;
   }

   /**
    * Retrieves the version of the entity.
    *
    * @return the version of the entity, or null if not defined
    */
   public String getVersion() {
      return version;
   }

   /**
    * Sets the version of the entity.
    *
    * @param version the version of the entity, which can be null or blank
    * @return the current instance of {@code EntityIdentification} for method chaining
    */
   public EntityIdentification setVersion(String version) {
      this.version = version;
      return this;
   }

   /**
    * Retrieves the name of the entity.
    *
    * @return the name of the entity, or null if not defined
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name of the entity.
    *
    * @param name the name to set; must not be null or blank
    * @return the current instance of {@code EntityIdentification} for method chaining
    */
   public EntityIdentification setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Determines whether the version is null or not blank.
    *
    * @return {@code true} if the version is null or not blank, {@code false} otherwise
    */
   public boolean isVersionDefinedIfPresent() {
      return version == null || !version.isBlank();
   }

   /**
    * Determines whether the name of the entity is defined and not blank.
    *
    * @return {@code true} if the name is not null and not blank, {@code false} otherwise
    */
   public boolean isNameDefined() {
      return name != null && !name.isBlank();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof EntityIdentification that)) {
         return false;
      }
      return Objects.equals(version, that.version)
             && Objects.equals(name, that.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(version, name);
   }
}
