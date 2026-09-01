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

package net.ihe.gazelle.maestro.api.business.property;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Ephemeral payload used during recording to keep provenance bindings between step properties and reported targets.
 */
public class PropertyBindingPayload implements Serializable {

   /**
    * Name of the ephemeral step output property carrying this payload.
    */
   public static final String PROPERTY_NAME = "propertyBinding";

   /**
    * Current schema version for serialization.
    */
   public static final int SCHEMA_VERSION = 1;

   @Serial
   private static final long serialVersionUID = 5301730568159111578L;

   /**
    * The schema version
    */
   private int schemaVersion = SCHEMA_VERSION;

   /**
    * The step type.
    */
   private String stepType;

   /**
    * The list of bindings.
    */
   private List<Binding> bindings = new ArrayList<>();

   /**
    * Default constructor
    */
   public PropertyBindingPayload() {
      // Default constructor
   }

   /**
    * Retrieves the schema version associated with the payload.
    *
    * @return the schema version as an integer
    */
   public int getSchemaVersion() {
      return schemaVersion;
   }

   /**
    * Sets the schema version for the {@code PropertyBindingPayload} object.
    *
    * @param schemaVersion the schema version to set, represented as an integer
    * @return the updated {@code PropertyBindingPayload} instance
    */
   public PropertyBindingPayload setSchemaVersion(int schemaVersion) {
      this.schemaVersion = schemaVersion;
      return this;
   }

   /**
    * Retrieves the step type associated with this instance.
    *
    * @return the step type as a string, or {@code null} if the step type has not been set
    */
   public String getStepType() {
      return stepType;
   }

   /**
    * Sets the step type for the {@code PropertyBindingPayload} object.
    *
    * @param stepType the step type to set, represented as a {@code String}
    * @return the updated {@code PropertyBindingPayload} instance
    */
   public PropertyBindingPayload setStepType(String stepType) {
      this.stepType = stepType;
      return this;
   }

   /**
    * Retrieves the list of bindings associated with this instance. Each binding represents
    * a mapping between a target in an external report and a local step property.
    *
    * @return a list of {@code Binding} objects representing the binding entries. If no bindings
    * are present, an empty list is returned.
    */
   public List<Binding> getBindings() {
      return new ArrayList<>(bindings);
   }

   /**
    * Sets the list of bindings for the {@code PropertyBindingPayload} object.
    *
    * @param bindings the list of {@code Binding} objects to set.
    *
    * @return the updated {@code PropertyBindingPayload} instance with the new list of bindings.
    */
   public PropertyBindingPayload setBindings(List<Binding> bindings) {
      this.bindings = bindings == null ? new ArrayList<>() : new ArrayList<>(bindings);
      return this;
   }

   /**
    * Adds a {@code Binding} instance to the list of bindings for the {@code PropertyBindingPayload}.
    *
    * @param binding the {@code Binding} instance to add to the payload.
    * @return the updated {@code PropertyBindingPayload} instance with the new binding added.
    */
   public PropertyBindingPayload addBinding(Binding binding) {
      if (binding != null) {
         bindings.add(binding);
      }
      return this;
   }

   /**
    * One binding entry between a target in an external report and a local step property.
    */
   public static class Binding implements Serializable {
      @Serial
      private static final long serialVersionUID = 3190953897475394209L;

      /**
       * The target kind.
       */
      private String targetKind;

      /**
       * The target identifier.
       */
      private String targetId;

      /**
       * The reference name.
       */
      private String referenceName;

      /**
       * The property name.
       */
      private String propertyName;

      /**
       * The file name.
       */
      private String fileName;

      /**
       * The MIME type.
       */
      private String mimeType;

      /**
       * Default constructor
       */
      public Binding() {
         // Default constructor
      }

      /**
       * Retrieves the kind of the target associated with this binding.
       *
       * @return the target kind as a String, or {@code null} if the target kind is not set
       */
      public String getTargetKind() {
         return targetKind;
      }

      /**
       * Sets the target kind for this binding and returns the updated Binding object.
       *
       * @param targetKind the kind of the target to be associated with this binding
       * @return the updated Binding object with the specified target kind
       */
      public Binding setTargetKind(String targetKind) {
         this.targetKind = targetKind;
         return this;
      }

      /**
       * Retrieves the identifier of the target associated with this binding.
       *
       * @return the target identifier as a String, or {@code null} if the target identifier is not set
       */
      public String getTargetId() {
         return targetId;
      }

      /**
       * Sets the identifier of the target associated with this binding and returns the updated Binding object.
       *
       * @param targetId the identifier of the target to be associated with this binding
       * @return the updated Binding object with the specified target identifier
       */
      public Binding setTargetId(String targetId) {
         this.targetId = targetId;
         return this;
      }

      /**
       * Retrieves the reference name associated with this binding.
       *
       * @return the reference name as a String, or {@code null} if the reference name is not set
       */
      public String getReferenceName() {
         return referenceName;
      }

      /**
       * Sets the reference name for this binding and returns the updated Binding object.
       *
       * @param referenceName the reference name to be associated with this binding
       * @return the updated Binding object with the specified reference name
       */
      public Binding setReferenceName(String referenceName) {
         this.referenceName = referenceName;
         return this;
      }

      /**
       * Retrieves the property name associated with this binding.
       *
       * @return the property name as a String, or {@code null} if the property name is not set
       */
      public String getPropertyName() {
         return propertyName;
      }

      /**
       * Sets the property name for this binding and returns the updated Binding object.
       *
       * @param propertyName the property name to be associated with this binding
       * @return the updated Binding object with the specified property name
       */
      public Binding setPropertyName(String propertyName) {
         this.propertyName = propertyName;
         return this;
      }

      /**
       * Retrieves the file name associated with this binding.
       *
       * @return the file name as a String, or {@code null} if the file name is not set
       */
      public String getFileName() {
         return fileName;
      }

      /**
       * Sets the file name associated with this binding and returns the updated Binding object.
       *
       * @param fileName the file name to be associated with this binding
       * @return the updated Binding object with the specified file name
       */
      public Binding setFileName(String fileName) {
         this.fileName = fileName;
         return this;
      }

      /**
       * Retrieves the MIME (Multipurpose Internet Mail Extensions) type associated with this binding.
       *
       * @return the MIME type as a String, or {@code null} if the MIME type is not set
       */
      public String getMimeType() {
         return mimeType;
      }

      /**
       * Sets the MIME (Multipurpose Internet Mail Extensions) type for this binding and
       * returns the updated Binding object.
       *
       * @param mimeType the MIME type to be associated with this binding
       * @return the updated Binding object with the specified MIME type
       */
      public Binding setMimeType(String mimeType) {
         this.mimeType = mimeType;
         return this;
      }
   }
}
