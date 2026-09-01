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

/**
 * A structure containing information about a supported input
 */
public abstract class SupportedInput implements Serializable {

   @Serial
   private static final long serialVersionUID = 5443082512534032315L;

   /**
    * The id of the input.
    */
   private String id;

   /**
    * The label of the input.
    */
   private String label;

   /**
    * The group name of the input.
    */
   private String groupName;

   /**
    * The description of the input.
    */
   private String description;

   /**
    * Whether the input is required or not.
    */
   private boolean required;

   /**
    * Default constructor
    */
   protected SupportedInput() {
      // Default constructor
   }

   /**
    * Retrieves the unique identifier of the input.
    *
    * @return the identifier of the input as a String.
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the unique identifier of the input.
    *
    * @param id the identifier of the input as a String
    * @return the current instance of SupportedInput
    */
   public SupportedInput setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Retrieves the label associated with the input.
    *
    * @return the label of the input as a String.
    */
   public String getLabel() {
      return label;
   }

   /**
    * Sets the label of the input.
    *
    * @param label the label of the input as a String
    * @return the current instance of SupportedInput
    */
   public SupportedInput setLabel(String label) {
      this.label = label;
      return this;
   }

   /**
    * Retrieves the group name associated with the input.
    *
    * @return the group name of the input as a String.
    */
   public String getGroupName() {
      return groupName;
   }

   /**
    * Sets the group name associated with the input.
    *
    * @param groupName the group name of the input as a String
    * @return the current instance of SupportedInput
    */
   public SupportedInput setGroupName(String groupName) {
      this.groupName = groupName;
      return this;
   }

   /**
    * Retrieves the description of the input.
    *
    * @return the description of the input as a String.
    */
   public String getDescription() {
      return description;
   }

   /**
    * Sets the description of the input.
    *
    * @param description the description of the input as a String
    * @return the current instance of SupportedInput
    */
   public SupportedInput setDescription(String description) {
      this.description = description;
      return this;
   }

   /**
    * Determines whether the input is required.
    *
    * @return true if the input is required, false otherwise.
    */
   public boolean isRequired() {
      return required;
   }

   /**
    * Sets whether the input is required and updates the current instance of SupportedInput.
    *
    * @param required a boolean indicating if the input should be marked as required
    * @return the current instance of SupportedInput
    */
   public SupportedInput setRequired(boolean required) {
      this.required = required;
      return this;
   }

   /**
    * Checks if the unique identifier for the input is defined.
    *
    * @return true if the identifier is not null and not empty, false otherwise.
    */
   public boolean isIdDefined() {
      return id != null && !id.isEmpty();
   }

   /**
    * Checks if the label for the input is defined.
    *
    * @return true if the label is not null and not empty, false otherwise.
    */
   public boolean isLabelDefined() {
      return label != null && !label.isEmpty();
   }
}
